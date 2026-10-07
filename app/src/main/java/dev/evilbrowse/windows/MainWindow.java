package dev.evilbrowse.windows;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.glavo.monetfx.Brightness;
import org.glavo.monetfx.ColorScheme;
import org.glavo.monetfx.Contrast;
import org.glavo.monetfx.beans.property.ColorSchemeProperty;
import org.glavo.monetfx.beans.property.SimpleColorSchemeProperty;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jthemedetecor.OsThemeDetector;

import dev.evilbrowse.Main;
import dev.evilbrowse.components.AISidebar;
import dev.evilbrowse.components.AddressBar;
import dev.evilbrowse.components.FindSidebar;
import dev.evilbrowse.components.MoreSidebar;
import dev.evilbrowse.components.TabBar;
import dev.evilbrowse.components.TerminalSidebar;
import dev.evilbrowse.components.ToolSidebar;
import dev.evilbrowse.components.WindowControlBar;
import dev.evilbrowse.components.YtdlpSidebar;
import dev.evilbrowse.components.ZoomSidebar;
import dev.evilbrowse.db.ProfileDatabase;
import dev.evilbrowse.db.MainDatabase.ProfileStructureWithId;
import dev.evilbrowse.db.ProfileDatabase.AISettings;
import dev.evilbrowse.db.ProfileDatabase.HistoryItem;
import dev.evilbrowse.db.ProfileDatabase.NewtabSettings;
import dev.evilbrowse.handlers.CefKeyboardHandler;
import dev.evilbrowse.handlers.SwingKeyboardHandler;
import dev.evilbrowse.handlers.EvilBrowseContextMenuHandler;
import dev.evilbrowse.handlers.EvilBrowseDialogHandler;
import dev.evilbrowse.handlers.EvilBrowseDisplayHandler;
import dev.evilbrowse.handlers.EvilBrowseDownloadHandler;
import dev.evilbrowse.handlers.EvilBrowseFocusHandler;
import dev.evilbrowse.handlers.EvilBrowseLifeSpanHandler;
import dev.evilbrowse.handlers.EvilBrowseLoadHandler;
import dev.evilbrowse.handlers.EvilBrowsePrintHandler;
import dev.evilbrowse.handlers.EvilBrowseRequestHandler;
import dev.evilbrowse.managers.CefAppManager;
import dev.evilbrowse.managers.DiscordPresenceManager;
import dev.evilbrowse.managers.InstanceManager;
import dev.evilbrowse.managers.UpdateManager;
import dev.evilbrowse.managers.WallpaperManager;
import dev.evilbrowse.managers.WindowsManager;
import dev.evilbrowse.managers.WindowsManager.WindowItem;
import dev.evilbrowse.ollama.OllamaChat;
import dev.evilbrowse.search.SearchAutosuggest;
import dev.evilbrowse.search.SearchURLTemplates;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

public class MainWindow extends JFrame {
	private final boolean USE_OSR = false;

	final public String startUrl = "evilbrowse://newtab";
	public static final String APP_NAME = "EvilBrowse";
	private CefClient cefClient;
	public volatile CefBrowser currentBrowser;
	public final List<CefBrowser> openedBrowserTabs = java.util.Collections.synchronizedList(new ArrayList<>());
	private JPanel root;
	private final CardLayout browserLayout = new CardLayout();
	private final JPanel browserContainer = new JPanel(browserLayout);
	public AddressBar addressBar;
	public TabBar tabBar;
	public final Map<CefBrowser, String> titleMap = new java.util.concurrent.ConcurrentHashMap<>();
	public final BooleanProperty isUiFocused = new SimpleBooleanProperty(false);
	public volatile OllamaChat ollamaSession;
	private final Object ollamaInitLock = new Object();
	private volatile boolean ollamaInitInFlight = false;
	private ToolSidebar toolSidebar;
	public volatile AISidebar aiSidebar;
	public final YtdlpSidebar ytdlpSidebar;
	public final ZoomSidebar zoomSidebar;
	public final FindSidebar findSidebar;
	public final TerminalSidebar terminalSidebar;
	public final MoreSidebar moreSidebar;
	private final JPanel sidePanel;
	private final JPanel centerPanel;
	private WindowControlBar windowControlBar;
	private JPanel sidebarPanel;
	private final Gson gson = new Gson();
	public final EvilBrowseLoadHandler loadHandler = new EvilBrowseLoadHandler();
	public final EvilBrowseRequestHandler requestHandler;
	public final ProfileStructureWithId currentProfile;
	public ColorSchemeProperty profileMaterialColorScheme = new SimpleColorSchemeProperty(
			ColorScheme.fromSeed(Color.web("#BDCF47")));
	public String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.1.0 Safari/537.36";
	private final String windowId;
	private final CefAppManager cefAppManager;
	private final CefApp cefApp;
	public final ProfileDatabase profileDatabase;
	public String defaultSearchProvider = SearchURLTemplates.getTemplate("brave");
	public boolean enableDiscordPresence = false;
	private String browserAppearance = "system";
	public AISettings aiSettings = new AISettings(false, "gemma4:e2b");
	public NewtabSettings newtabSettings = new NewtabSettings("");
	public final SearchAutosuggest searchAutosuggest;
	private final OsThemeDetector themeDetector = OsThemeDetector.getDetector();
	private boolean isFullscreen = false;
	private volatile String tabPosition = "vertical";
	private JPanel topPanel;

	public MainWindow(ProfileStructureWithId profile) {
		this(profile, "evilbrowse://newtab");
	}

	public MainWindow(ProfileStructureWithId profile, String launchUrl) {
		super("EvilBrowse");

		System.out.println("Creating main window for profile: " + profile.getIdAsString());

		currentProfile = profile;

		profileDatabase = ProfileDatabase.getInstance(currentProfile.getIdAsString());

		defaultSearchProvider = SearchURLTemplates.getTemplate(profileDatabase.getDefaultSearchEngine());
		enableDiscordPresence = profileDatabase.getDiscordPresenceSetting();
		if (enableDiscordPresence) {
			// Apply a custom EvilBrowse Discord app ID if configured, so the
			// Rich Presence game name matches the browser brand.
			try {
				DiscordPresenceManager.getInstance()
						.init(profileDatabase.getDiscordClientId());
			} catch (Exception ignored) {
			}
		}
		browserAppearance = profileDatabase.getAppearance();
		aiSettings = profileDatabase.getAISettings();
		newtabSettings = profileDatabase.getNewtabSettings();
		try {
			tabPosition = profileDatabase.getTabPosition();
		} catch (Exception ignored) {
			tabPosition = "vertical";
		}

		cefAppManager = CefAppManager.getInstance(this);
		cefApp = cefAppManager.getCefApp();
		loadHandler.setParent(this);

		windowId = "%s_main_window".formatted(profile.getIdAsString());
		WindowsManager.getInstance()
				.addWindow(new WindowItem(windowId, MainWindow.class));

		System.out.println("AWT Toolkit: " + java.awt.Toolkit.getDefaultToolkit().getClass().getName());
		System.out.println("DISPLAY: " + System.getenv("DISPLAY"));
		System.out.println("WAYLAND_DISPLAY: " + System.getenv("WAYLAND_DISPLAY"));

		InstanceManager.getInstance().startListening(profile.getIdAsString(),
				this);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Firefox-style: the tab strip IS the title bar (client-side
		// decorations). No separate OS titlebar.
		setUndecorated(true);
		setLayout(new BorderLayout());

		final Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo_full_trans.png"));
		applyAppIcons(icon);
		setIconImage(icon);

		root = new JPanel(new BorderLayout());
		setContentPane(root);

		setSize(1200, 800);
		setLocationRelativeTo(null);

		setUserAgent();
		setMaterialColorSchemeFromProfile();

		// Request handler - keep here after user agent initializes but before address
		// bar
		requestHandler = new EvilBrowseRequestHandler(this);

		searchAutosuggest = new SearchAutosuggest(userAgent, this);

		// Address bar
		addressBar = new AddressBar(cefClient, this, startUrl);

		cefClient = cefApp.createClient();

		// Tab bar
		tabBar = new TabBar(cefClient, openedBrowserTabs, this);

		// AI is opt-in and lazy: no CEF browser, no Ollama init at startup.
		// The sidebar and Ollama session are created on first use after the
		// user enables AI in Settings. This keeps cold startup fast even when
		// Ollama is not installed or unreachable.
		aiSidebar = null;
		ollamaSession = null;

		// yt-dlp sidebar
		ytdlpSidebar = new YtdlpSidebar(this);

		// Zoom sidebar
		zoomSidebar = new ZoomSidebar(this);

		// Find sidebar
		findSidebar = new FindSidebar(this);

		// Terminal sidebar
		terminalSidebar = new TerminalSidebar(this);

		// More sidebar
		moreSidebar = new MoreSidebar(this);

		// Keyboard handler (JCEF)
		cefClient.addKeyboardHandler(new CefKeyboardHandler(this, startUrl));

		// Keyboard handler (Swing)
		new SwingKeyboardHandler(this, startUrl);

		cefClient.addFocusHandler(new EvilBrowseFocusHandler(this));

		// Modern browser shell: vertical tab sidebar on the left,
		// address/toolbar on top, web viewport in the center.
		// No tool sidebar is open at startup: AI is lazy/opt-in.
		sidePanel = new JPanel(new BorderLayout());
		sidePanel.add(moreSidebar, BorderLayout.EAST);

		toolSidebar = null;

		// Tab chrome placement depends on the persisted tab position:
		// vertical sidebar (WEST) or classic horizontal strip (NORTH).
		centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(browserContainer, BorderLayout.CENTER);
		centerPanel.add(sidePanel, BorderLayout.EAST);

		// In-window window controls (replaces the OS titlebar).
		windowControlBar = new WindowControlBar(new WindowControlBar.WindowActions() {
			@Override
			public void minimize() {
				setExtendedState(getExtendedState() | JFrame.ICONIFIED);
			}

			@Override
			public void toggleMaximize() {
				toggleMaximizeWindow();
			}

			@Override
			public void close() {
				dispose();
			}

			@Override
			public void dragBy(int dx, int dy) {
				moveWindowBy(dx, dy);
			}

			@Override
			public void beginDrag(double screenX, double screenY) {
				beginWindowDrag(screenX, screenY);
			}
		}, colorToAwt(profileMaterialColorScheme.getSurface().get()),
				colorToAwt(profileMaterialColorScheme.getOnSurface().get()));

		// Sidebar wrapper so controls can sit at the top in vertical mode.
		sidebarPanel = new JPanel(new BorderLayout());
		sidebarPanel.add(tabBar, BorderLayout.CENTER);
		sidebarPanel.add(windowControlBar, BorderLayout.NORTH);

		applyTabBarLayout();

		cefClient.addDisplayHandler(new EvilBrowseDisplayHandler(this));
		cefClient.addLifeSpanHandler(new EvilBrowseLifeSpanHandler(this));
		cefClient.addDialogHandler(new EvilBrowseDialogHandler());
		cefClient.addDownloadHandler(new EvilBrowseDownloadHandler());
		cefClient.addContextMenuHandler(new EvilBrowseContextMenuHandler(this));
		cefClient.addPrintHandler(new EvilBrowsePrintHandler());
		cefClient.addLoadHandler(loadHandler);
		cefClient.addRequestHandler(requestHandler);

		// NOTE: no Ollama/AI init here by design. AI starts only after the
		// user enables it in Settings -> AI integrations (see ensureOllamaSessionAsync).

		themeDetector.registerListener(isDark -> {
			System.out.println("Browser appearance: " + browserAppearance);
			System.out.println("Browser appearance equals system: " + browserAppearance.equals("system"));
			if (browserAppearance.equals("system")) {
				System.out.println("System is dark: " + isDark);
				setMaterialColorSchemeFromProfile();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Let dispose() own shutdown. Previously this called
				// CefApp.dispose() and then dispose() disposed again,
				// killing other windows sharing the singleton CefApp.
				dispose();
			}
		});

		addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent event) {
				try {
					if (!isWindowMaximized() && !isFullscreen
							&& getWidth() > 0 && getHeight() > 0) {
						lastNormalSize = getSize();
					}
				} catch (Exception ignored) {
				}
				updateWindowShape();
			}
		});

		SwingUtilities.invokeLater(() -> {
			createTab(launchUrl);
			setVisible(true);
		});
	}

	public String getCachePath() {
		return Main.getStoragePath("cef-cache", currentProfile.getIdAsString()).toString();
	}

	/**
	 * Publish a multi-resolution icon list. A single setIconImage() call makes
	 * X11/KDE pick one size and usually renders a blurry or generic taskbar
	 * icon; Window#setIconImages (Java 9+) publishes all sizes properly.
	 */
	private void applyAppIcons(Image base) {
		if (base == null) {
			return;
		}
		try {
			final int[] sizes = { 16, 24, 32, 48, 64, 128, 256 };
			final List<Image> icons = new ArrayList<>();
			for (final int size : sizes) {
				try {
					icons.add(base.getScaledInstance(size, size, Image.SCALE_SMOOTH));
				} catch (Exception ignored) {
				}
			}
			if (!icons.isEmpty()) {
				this.setIconImages(icons);
			}
		} catch (Throwable ignored) {
			// Older/limited AWT: the single setIconImage() call remains as fallback.
		}
	}

	public void updateWindowTitle(String pageTitle) {
		String safeTitle = (pageTitle == null || pageTitle.isBlank()) ? "New Tab" : pageTitle;
		this.setTitle(safeTitle + " - " + APP_NAME);
		if (enableDiscordPresence) {
			// IPC must never block the EDT on every tab switch.
			Thread.ofVirtual().start(() -> {
				try {
					DiscordPresenceManager.getInstance().updateDiscordPresence("Browsing " + safeTitle);
				} catch (Exception ignored) {
				}
			});
		}
	}

	public void createTab(String url) {
		createTab(url, false);
	}

	public void createTab(String url, boolean selectAllField) {
		// JCEF Swing browsers must be created on the EDT. JS-bridge and CEF
		// threads call this off-EDT; hop there to avoid native window races.
		if (!SwingUtilities.isEventDispatchThread()) {
			final String targetUrl = url;
			SwingUtilities.invokeLater(() -> createTab(targetUrl, selectAllField));
			return;
		}
		String safeUrl = (url == null || url.isBlank()) ? startUrl : url;
		System.out.printf("Creating tab for URL: %s\n", safeUrl);

		CefBrowser browser;
		try {
			browser = cefClient.createBrowser(safeUrl, USE_OSR, false);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (browser == null) {
			return;
		}

		openedBrowserTabs.add(browser);

		Component ui = browser.getUIComponent();
		browserContainer.add(ui, String.valueOf(System.identityHashCode(browser)));

		Platform.runLater(() -> {
			try {
				tabBar.addTabToUI(browser);
			} catch (Exception ignored) {
			}
			try {
				addressBar.focusAddressField(selectAllField);
			} catch (Exception ignored) {
			}
		});

		showTab(browser);
	}

	public void closeTab(CefBrowser browser) {
		if (browser == null) {
			return;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> closeTab(browser));
			return;
		}
		final int indexToClose;
		synchronized (openedBrowserTabs) {
			indexToClose = openedBrowserTabs.indexOf(browser);
		}
		if (indexToClose == -1) {
			return;
		}

		// Remember URL for "Reopen closed tab" before disposal.
		try {
			String closedUrl = browser.getURL();
			if (closedUrl != null && !closedUrl.isBlank()
					&& !closedUrl.startsWith("about:blank")) {
				tabBar.rememberClosedUrl(closedUrl);
			}
		} catch (Exception ignored) {
		}

		CefBrowser nextBrowserToSelect = null;
		synchronized (openedBrowserTabs) {
			if (browser == currentBrowser && openedBrowserTabs.size() > 1) {
				int nextIndex = (indexToClose > 0) ? indexToClose - 1 : 1;
				if (nextIndex >= 0 && nextIndex < openedBrowserTabs.size()) {
					nextBrowserToSelect = openedBrowserTabs.get(nextIndex);
					if (nextBrowserToSelect == browser && openedBrowserTabs.size() > 1) {
						nextBrowserToSelect = openedBrowserTabs.get(indexToClose == 0 ? 1 : 0);
					}
				}
			}
		}

		try {
			browserContainer.remove(browser.getUIComponent());
		} catch (Exception ignored) {
		}
		synchronized (openedBrowserTabs) {
			openedBrowserTabs.remove(browser);
		}
		titleMap.remove(browser);
		loadHandler.removeBrowser(browser.getIdentifier());

		browserContainer.revalidate();
		browserContainer.repaint();

		boolean empty;
		synchronized (openedBrowserTabs) {
			empty = openedBrowserTabs.isEmpty();
		}
		if (empty) {
			currentBrowser = null;
			try {
				browser.close(true);
			} catch (Exception ignored) {
			}
			dispose();
		} else {
			if (nextBrowserToSelect != null) {
				showTab(nextBrowserToSelect);
			}
			try {
				browser.close(true);
			} catch (Exception ignored) {
			}
		}
	}

	public void duplicateTab(CefBrowser browser) {
		if (browser == null) {
			return;
		}
		String url = startUrl;
		try {
			String current = browser.getURL();
			if (current != null && !current.isBlank()) {
				url = current;
			}
		} catch (Exception ignored) {
		}
		createTab(url);
	}

	public void closeOtherTabs(CefBrowser keep) {
		if (keep == null) {
			return;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> closeOtherTabs(keep));
			return;
		}
		final List<CefBrowser> snapshot;
		synchronized (openedBrowserTabs) {
			snapshot = new ArrayList<>(openedBrowserTabs);
		}
		for (CefBrowser b : snapshot) {
			if (b != keep) {
				try {
					tabBar.removeTabUiOnly(b);
				} catch (Exception ignored) {
				}
				closeTabBackendOnly(b);
			}
		}
		showTab(keep);
	}

	public void closeTabsToRight(CefBrowser pivot) {
		if (pivot == null) {
			return;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> closeTabsToRight(pivot));
			return;
		}
		final List<CefBrowser> order = tabBar.getTabOrderSnapshot();
		final int pivotIdx = order.indexOf(pivot);
		if (pivotIdx == -1) {
			return;
		}
		final List<CefBrowser> toClose = new ArrayList<>(order.subList(pivotIdx + 1, order.size()));
		for (CefBrowser b : toClose) {
			try {
				tabBar.removeTabUiOnly(b);
			} catch (Exception ignored) {
			}
			closeTabBackendOnly(b);
		}
		showTab(pivot);
	}

	private void closeTabBackendOnly(CefBrowser browser) {
		if (browser == null) {
			return;
		}
		try {
			String closedUrl = browser.getURL();
			if (closedUrl != null && !closedUrl.isBlank()
					&& !closedUrl.startsWith("about:blank")) {
				tabBar.rememberClosedUrl(closedUrl);
			}
		} catch (Exception ignored) {
		}
		try {
			browserContainer.remove(browser.getUIComponent());
		} catch (Exception ignored) {
		}
		synchronized (openedBrowserTabs) {
			openedBrowserTabs.remove(browser);
		}
		titleMap.remove(browser);
		try {
			loadHandler.removeBrowser(browser.getIdentifier());
		} catch (Exception ignored) {
		}
		try {
			browser.close(true);
		} catch (Exception ignored) {
		}
		browserContainer.revalidate();
		browserContainer.repaint();
		synchronized (openedBrowserTabs) {
			if (openedBrowserTabs.isEmpty()) {
				currentBrowser = null;
			}
		}
	}

	public void reopenClosedTab() {
		String url = null;
		try {
			url = tabBar.pollClosedUrl();
		} catch (Exception ignored) {
		}
		if (url == null || url.isBlank()) {
			return;
		}
		createTab(url);
	}

	public void closeCurrentTab() {
		final CefBrowser browser = currentBrowser;
		if (browser == null) {
			return;
		}
		Platform.runLater(() -> tabBar.closeTab(browser));
	}

	public void showTab(CefBrowser browser) {
		if (browser == null || !openedBrowserTabs.contains(browser)) {
			return;
		}

		SwingUtilities.invokeLater(() -> {
			currentBrowser = browser;
			final Component ui;
			try {
				ui = browser.getUIComponent();
			} catch (Exception e) {
				return;
			}
			if (ui == null) {
				return;
			}

			final Color bgColor = profileMaterialColorScheme.getSurface().get();

			try {
				ui.setBackground(colorToAwt(bgColor));
			} catch (Exception ignored) {
			}

			if (ui.getMouseListeners().length == 0) {
				ui.addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mousePressed(java.awt.event.MouseEvent event) {
						// isUiFocused is a JavaFX property: always flip it on the FX thread.
						Platform.runLater(() -> {
							try {
								isUiFocused.set(false);
							} catch (Exception ignored) {
							}
						});
						SwingUtilities.invokeLater(() -> {
							try {
								browser.setFocus(true);
							} catch (Exception ignored) {
							}
						});
					}
				});
			}

			final String browserTitle = titleMap.get(browser);
			try {
				updateWindowTitle(browserTitle != null ? browserTitle : "Loading...");
			} catch (Exception ignored) {
			}

			final String cardKey = String.valueOf(System.identityHashCode(browser));
			try {
				browserLayout.show(browserContainer, cardKey);
			} catch (Exception ignored) {
			}

			browserContainer.revalidate();
			browserContainer.repaint();

			Platform.runLater(() -> {
				try {
					addressBar.updateUrl(browser.getURL());
				} catch (Exception ignored) {
				}
				try {
					tabBar.setCurrentTab(browser);
				} catch (Exception ignored) {
				}
			});
		});
	}

	public void createDevTools() {
		final CefBrowser browser = currentBrowser;
		if (browser == null) {
			return;
		}
		try {
			browser.openDevTools();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private volatile boolean isDisposing = false;

	@Override
	public void dispose() {
		if (isDisposing) {
			return;
		}
		isDisposing = true;
		System.out.println("Closing...");

		try {
			WindowsManager.getInstance().removeWindow(windowId);
		} catch (Exception ignored) {
		}

		final List<CefBrowser> toClose;
		synchronized (openedBrowserTabs) {
			toClose = new ArrayList<>(openedBrowserTabs);
			openedBrowserTabs.clear();
		}
		currentBrowser = null;
		for (final CefBrowser browser : toClose) {
			if (browser != null) {
				try {
					browserContainer.remove(browser.getUIComponent());
				} catch (Exception ignored) {
				}
				try {
					browser.close(true);
				} catch (Exception ignored) {
				}
			}
		}

		try {
			terminalSidebar.shutdown();
		} catch (Exception ignored) {
		}

		try {
			AISidebar ai = aiSidebar;
			if (ai != null) {
				ai.shutdown();
			}
		} catch (Exception ignored) {
		}
		ollamaSession = null;

		// Only dispose the shared CefApp when the last window is gone.
		// Previously every window disposed the singleton, killing siblings.
		try {
			if (WindowsManager.getInstance().getWindows().isEmpty() && cefApp != null) {
				cefApp.dispose();
			}
		} catch (Exception ignored) {
		}

		try {
			super.dispose();
		} catch (Exception ignored) {
		}

		System.out.println("Successfully closed browser.");

		try {
			if (WindowsManager.getInstance().getWindows().isEmpty()) {
				try {
					Platform.exit();
				} catch (Exception ignored) {
				}
				System.exit(0);
			}
		} catch (Exception ignored) {
		}
	}

	public String formatURL(String url, Boolean isSearching) {
		String fallback = SearchURLTemplates.getTemplate(defaultSearchProvider);
		if (fallback == null) {
			fallback = "https://search.brave.com/search?q=%s";
		}
		if (isSearching == null || isSearching) {
			if (url == null) {
				url = "";
			}
			return fallback.formatted(URLEncoder.encode(url, StandardCharsets.UTF_8));
		}

		if (url == null || url.isBlank()) {
			return startUrl;
		}

		final Set<String> allowedSchemes = Set.of(
				"http", "https", "file", "about", "evilbrowse", "turtlebrowse");

		final String trimmedUrl = url.trim();

		if (trimmedUrl.startsWith("about:"))
			return trimmedUrl;

		// Internal pages without an explicit scheme prefix.
		if (trimmedUrl.startsWith("evilbrowse://") || trimmedUrl.startsWith("turtlebrowse://")) {
			return trimmedUrl;
		}

		// Bare hostnames like "localhost:3000" or "192.168.1.1:8080".
		if (trimmedUrl.startsWith("localhost") || trimmedUrl.matches("^(\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?(/.*)?$")) {
			if (!trimmedUrl.contains("://")) {
				return "http://" + trimmedUrl;
			}
			return trimmedUrl;
		}

		try {
			URI uri = URI.create(trimmedUrl);
			String scheme = uri.getScheme();

			if (scheme != null) {
				if (allowedSchemes.contains(scheme.toLowerCase())) {
					return trimmedUrl;
				}
			} else {
				if (trimmedUrl.contains(".") && !trimmedUrl.contains(" ")) {
					return "https://" + trimmedUrl;
				}
			}
		} catch (Exception e) {
		}

		String provider = defaultSearchProvider;
		if (provider == null) {
			provider = fallback;
		}
		return provider.formatted(URLEncoder.encode(url, StandardCharsets.UTF_8));
	}

	public void searchWeb(String query) {
		final CefBrowser browser = currentBrowser;
		if (browser == null) {
			// No tab yet: open one with the query instead of NPE-ing.
			createTab(formatURL(query != null ? query : "", true));
			return;
		}
		try {
			browser.loadURL(formatURL(query != null ? query : "", true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String handleApiFromClient(String action, String body) {
		JsonObject params;
		try {
			params = gson.fromJson(body != null ? body : "{}", JsonObject.class);
			if (params == null) {
				params = new JsonObject();
			}
		} catch (Exception e) {
			return "\"Invalid request\"";
		}
		if (action == null) {
			return "\"Unknown action\"";
		}

		switch (action) {
			case "GET_NAME": {
				System.out.println("GET_NAME called.");
				return currentProfile.name();
			}

			case "SEARCH_WEB": {
				if (!params.has("query")) {
					return "\"Missing query\"";
				}
				final String query = params.get("query").getAsString();
				SwingUtilities.invokeLater(() -> searchWeb(query));
				return "\"ok\"";
			}

			case "GET_THEME": {
				final Color profileColor = currentProfile.seedColor();
				System.out.printf("Profile color: %s", profileColor);
				if (profileColor == null) {
					return "\"#BDCF47\"";
				}
				final String hex = colorToHex(profileColor);
				return hex;
			}

			case "GET_SEARCH_ENGINE": {
				final String searchEngine = profileDatabase.getDefaultSearchEngine();
				return searchEngine;
			}

			case "SET_SEARCH_ENGINE": {
				if (!params.has("engine")) {
					return "\"Missing engine\"";
				}
				final String searchEngineToSet = params.get("engine").getAsString();
				defaultSearchProvider = SearchURLTemplates.getTemplate(searchEngineToSet);
				profileDatabase.setDefaultSearchEngine(searchEngineToSet);
				return "\"ok\"";
			}

			case "GET_DISCORD_SETTING": {
				final boolean discordSetting = profileDatabase.getDiscordPresenceSetting();
				return String.valueOf(discordSetting);
			}

			case "SET_DISCORD_SETTING": {
				final boolean discordSettingToSet = params.get("enabled").getAsBoolean();
				enableDiscordPresence = discordSettingToSet;
				if (discordSettingToSet) {
					String clientId = "";
					try {
						clientId = profileDatabase.getDiscordClientId();
					} catch (Exception ignored) {
					}
					DiscordPresenceManager.getInstance().init(clientId);
				} else {
					DiscordPresenceManager.getInstance().disableDiscordPresence();
				}
				profileDatabase.setDiscordPresenceSetting(discordSettingToSet);
				return "\"ok\"";
			}

			case "GET_DISCORD_CLIENT_ID": {
				try {
					return profileDatabase.getDiscordClientId();
				} catch (Exception e) {
					return "";
				}
			}

			case "SET_DISCORD_CLIENT_ID": {
				String clientId = "";
				try {
					if (params.has("clientId") && !params.get("clientId").isJsonNull()) {
						String raw = params.get("clientId").getAsString();
						if (raw != null) {
							clientId = raw.trim();
						}
					}
				} catch (Exception ignored) {
				}
				try {
					profileDatabase.setDiscordClientId(clientId);
				} catch (Exception ignored) {
				}
				// Reconnect live so the new app name applies immediately.
				if (enableDiscordPresence) {
					try {
						DiscordPresenceManager.getInstance().init(clientId);
					} catch (Exception ignored) {
					}
				}
				return "\"ok\"";
			}

			case "GET_APPEARANCE": {
				final String appearance = profileDatabase.getAppearance();
				return appearance;
			}

			case "SET_APPEARANCE": {
				if (!params.has("theme")) {
					return "\"Missing theme\"";
				}
				final String appearance = params.get("theme").getAsString();
				browserAppearance = appearance != null ? appearance : "system";
				setMaterialColorSchemeFromProfile();
				profileDatabase.setAppearance(browserAppearance);
				return "\"ok\"";
			}

			case "GET_AI_SETTINGS": {
				final AISettings aiSettings = profileDatabase.getAISettings();
				return gson.toJson(aiSettings);
			}

			case "SET_AI_SETTINGS": {
				boolean enabled = false;
				String model = "gemma4:e2b";
				try {
					if (params.has("enabled")) {
						enabled = params.get("enabled").getAsBoolean();
					}
					if (params.has("model") && !params.get("model").isJsonNull()) {
						String m = params.get("model").getAsString();
						if (m != null && !m.isBlank()) {
							model = m;
						}
					}
				} catch (Exception ignored) {
				}
				final AISettings settings = new AISettings(enabled, model);
				aiSettings = settings;
				try {
					profileDatabase.setAISettings(settings);
				} catch (Exception ignored) {
				}
				if (enabled) {
					// Pre-warm Ollama off the CEF IO thread. Never blocks the
					// settings save or browser startup; failures surface when
					// the user actually opens the AI panel/chat.
					Thread.ofVirtual().start(() -> {
						try {
							ensureOllamaSessionAsync(ready -> {
							}, err -> System.err.println("AI pre-warm failed: " + err));
						} catch (Exception ignored) {
						}
					});
				} else {
					// Disable: drop the session so no Ollama connection is held.
					// The sidebar browser (if created) stays for the session but
					// will report AI-disabled until re-enabled.
					ollamaSession = null;
					ollamaInitInFlight = false;
				}
				return "\"ok\"";
			}

			case "GET_NEWTAB_SETTINGS": {
				final NewtabSettings settings = profileDatabase.getNewtabSettings();
				return gson.toJson(settings);
			}

			case "SET_NEWTAB_SETTINGS": {
				final String greetingText = params.get("greetingText").getAsString();
				final NewtabSettings settings = new NewtabSettings(greetingText);
				newtabSettings = settings;
				profileDatabase.setNewtabSettings(settings);
				return "\"ok\"";
			}

			case "CLEAR_WALLPAPER": {
				WallpaperManager.getInstance(this).clearWallpaper();
				return "\"ok\"";
			}

			case "AUTOCOMPLETER": {
				if (!params.has("query")) {
					return "[]";
				}
				final String query = params.get("query").getAsString();
				try {
					final List<String> suggestions = searchAutosuggest.fetchAndProcess(query != null ? query : "");
					return gson.toJson(suggestions);
				} catch (Exception e) {
					return "[]";
				}
			}

			case "GET_HISTORY": {
				int index = 0;
				try {
					if (params.has("index")) {
						index = params.get("index").getAsInt();
					}
				} catch (Exception ignored) {
				}
				if (index < 0) {
					index = 0;
				}
				try {
					final List<HistoryItem> history = profileDatabase.getHistory(index, 50);
					return gson.toJson(history);
				} catch (Exception e) {
					return "[]";
				}
			}

			case "NAVIGATE_TO_SITE": {
				if (!params.has("url")) {
					return "\"Missing url\"";
				}
				final String url = params.get("url").getAsString();
				System.out.println("Site URL: " + url);
				// createTab is EDT-bound internally; safe to call from the CEF IO thread.
				try {
					createTab(url != null ? url : startUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "\"ok\"";
			}

			case "DELETE_HISTORY_ITEM": {
				if (!params.has("id")) {
					return "\"Missing id\"";
				}
				try {
					final UUID id = UUID.fromString(params.get("id").getAsString());
					profileDatabase.deleteFromHistory(id);
				} catch (Exception e) {
					return "\"Invalid id\"";
				}
				return "\"ok\"";
			}

			case "GET_UPDATE_INFO": {
				boolean refresh = false;
				try {
					if (params.has("refresh")) {
						refresh = params.get("refresh").getAsBoolean();
					}
				} catch (Exception ignored) {
				}

				final UpdateManager updateManager = UpdateManager.getInstance(this);

				boolean shouldUpdate;

				if (refresh) {
					shouldUpdate = updateManager.refreshShouldUpdate();
				} else {
					shouldUpdate = updateManager.shouldUpdate();
				}

				final String currentVersion = updateManager.currentVersion;
				final String latestVersion = updateManager.latestVersion;

				final Map<String, Object> updateInfoMap = new HashMap<>();
				updateInfoMap.put("currentVersion", currentVersion);
				updateInfoMap.put("latestVersion", latestVersion);
				updateInfoMap.put("needsUpdate", shouldUpdate);

				return gson.toJson(updateInfoMap);
			}

			case "GET_SIDEBAR_COLLAPSED": {
				try {
					return String.valueOf(profileDatabase.getSidebarCollapsed());
				} catch (Exception e) {
					return "false";
				}
			}

			case "SET_SIDEBAR_COLLAPSED": {
				try {
					boolean collapsed = false;
					if (params.has("collapsed")) {
						collapsed = params.get("collapsed").getAsBoolean();
					}
					profileDatabase.setSidebarCollapsed(collapsed);
				} catch (Exception ignored) {
				}
				return "\"ok\"";
			}

			case "GET_TAB_POSITION": {
				try {
					return profileDatabase.getTabPosition();
				} catch (Exception e) {
					return "vertical";
				}
			}

			case "SET_TAB_POSITION": {
				String position = "vertical";
				try {
					if (params.has("position") && !params.get("position").isJsonNull()) {
						position = params.get("position").getAsString();
					}
				} catch (Exception ignored) {
				}
				// setTabPosition is thread-safe (CEF IO thread calls this).
				try {
					setTabPosition(position);
				} catch (Exception ignored) {
				}
				return "\"ok\"";
			}

			case "GET_ADBLOCK_ENABLED": {
				try {
					return String.valueOf(profileDatabase.getAdblockEnabled());
				} catch (Exception e) {
					return "true";
				}
			}

			case "GET_ADBLOCK_STATUS": {
				final java.util.Map<String, Object> status = new HashMap<>();
				try {
					dev.evilbrowse.handlers.EvilBrowseResourceRequestHandler h = requestHandler
							.getResourceRequestHandler();
					status.put("enabled", h.isEnabled());
					status.put("rules", h.getRuleCount());
					status.put("blocked", h.getBlockedCount());
					status.put("lastUpdate", h.getLastUpdateEpochMs());
					status.put("lists", h.getListStatus());
				} catch (Exception e) {
					status.put("enabled", false);
					status.put("rules", 0);
					status.put("blocked", 0);
					status.put("lastUpdate", 0L);
					status.put("lists", new ArrayList<String>());
				}
				return gson.toJson(status);
			}

			case "REFRESH_FILTER_LISTS": {
				try {
					requestHandler.getResourceRequestHandler().requestRefresh();
				} catch (Exception ignored) {
				}
				return "\"ok\"";
			}

			case "SET_ADBLOCK_ENABLED": {
				boolean enabled = true;
				try {
					if (params.has("enabled")) {
						enabled = params.get("enabled").getAsBoolean();
					}
				} catch (Exception ignored) {
				}
				try {
					profileDatabase.setAdblockEnabled(enabled);
				} catch (Exception ignored) {
				}
				// Push to the live engine: no DB read on the resource path.
				try {
					requestHandler.setAdblockEnabled(enabled);
				} catch (Exception ignored) {
				}
				return "\"ok\"";
			}

			default:
				return "\"Unknown action\"";
		}
	}

	public OllamaChat getOllamaSession() {
		return ollamaSession;
	}

	/**
	 * AI is an opt-in secondary feature. It is disabled by default and never
	 * initializes at startup, keeping cold open fast.
	 */
	public boolean isAIEnabled() {
		try {
			return aiSettings != null && aiSettings.enabled();
		} catch (Exception e) {
			return false;
		}
	}

	/** Null-safe accessor for AI tool specs when no session exists. */
	public String getLatestAIMessage() {
		try {
			OllamaChat session = ollamaSession;
			if (session != null && session.latestMessage != null) {
				return session.latestMessage;
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	/**
	 * Create the AI sidebar on first use (EDT). Returns null when AI is
	 * disabled so callers can redirect to Settings instead of crashing.
	 */
	public AISidebar ensureAISidebar() {
		if (!isAIEnabled()) {
			return null;
		}
		AISidebar existing = aiSidebar;
		if (existing != null) {
			return existing;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			final java.util.concurrent.atomic.AtomicReference<AISidebar> ref = new java.util.concurrent.atomic.AtomicReference<>();
			try {
				SwingUtilities.invokeAndWait(() -> ref.set(ensureAISidebar()));
			} catch (Exception ignored) {
			}
			return ref.get();
		}
		try {
			AISidebar created = new AISidebar(cefClient, this, USE_OSR, isUiFocused);
			aiSidebar = created;
			return created;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * AI toolbar/panel entry point. When AI is disabled, opens Settings so the
	 * user can opt in (AI is a secondary setting, not a startup cost).
	 */
	public void toggleAISidebar() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::toggleAISidebar);
			return;
		}
		if (!isAIEnabled()) {
			createTab("evilbrowse://settings");
			return;
		}
		AISidebar sidebar = ensureAISidebar();
		if (sidebar == null) {
			createTab("evilbrowse://settings");
			return;
		}
		if (getSidebar() != sidebar) {
			setSidebar(sidebar);
			sidebar.openSidebar();
		} else {
			sidebar.toggleSidebar();
		}
	}

	/**
	 * Build the Ollama session off-thread on first AI use. Single-flight:
	 * concurrent callers share one init. Never called at startup.
	 */
	public void ensureOllamaSessionAsync(java.util.function.Consumer<OllamaChat> onReady,
			java.util.function.Consumer<String> onError) {
		OllamaChat existing = ollamaSession;
		if (existing != null) {
			try {
				onReady.accept(existing);
			} catch (Exception ignored) {
			}
			return;
		}
		if (!isAIEnabled()) {
			try {
				onError.accept("AI is disabled. Enable it in Settings -> AI integrations.");
			} catch (Exception ignored) {
			}
			return;
		}
		synchronized (ollamaInitLock) {
			existing = ollamaSession;
			if (existing != null) {
				try {
					onReady.accept(existing);
				} catch (Exception ignored) {
				}
				return;
			}
			if (ollamaInitInFlight) {
				// Another thread is initializing; poll briefly then settle.
				Thread.ofVirtual().start(() -> {
					for (int i = 0; i < 600; i++) {
						OllamaChat s = ollamaSession;
						if (s != null) {
							try {
								onReady.accept(s);
							} catch (Exception ignored) {
							}
							return;
						}
						if (!ollamaInitInFlight) {
							break;
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							break;
						}
					}
					OllamaChat s = ollamaSession;
					if (s != null) {
						try {
							onReady.accept(s);
						} catch (Exception ignored) {
						}
					} else {
						try {
							onError.accept("AI is still starting. Try again in a moment.");
						} catch (Exception ignored) {
						}
					}
				});
				return;
			}
			ollamaInitInFlight = true;
		}
		final String ua = userAgent;
		Thread.ofVirtual().start(() -> {
			try {
				OllamaChat created = new OllamaChat(ua, this);
				ollamaSession = created;
				System.out.println("Ollama chat session initialized on demand.");
				try {
					onReady.accept(created);
				} catch (Exception ignored) {
				}
			} catch (Exception e) {
				System.err.println("Failed to initialize Ollama session: " + e.getMessage());
				try {
					onError.accept("Could not reach Ollama. Is Ollama running with model '"
							+ (aiSettings != null ? aiSettings.model() : "?") + "'?");
				} catch (Exception ignored) {
				}
			} finally {
				synchronized (ollamaInitLock) {
					ollamaInitInFlight = false;
				}
			}
		});
	}

	private void setMaterialColorSchemeFromProfile() {
		final Color accentColor = currentProfile != null ? currentProfile.seedColor() : null;

		System.out.println("Browser appearance: " + browserAppearance);

		boolean isDark = false;
		String appearance = browserAppearance != null ? browserAppearance : "system";
		if (appearance.equals("system")) {
			if (OsThemeDetector.isSupported()) {
				System.out.println("Theme detector is supported.");
				try {
					isDark = themeDetector.isDark();
				} catch (Exception ignored) {
				}
				System.out.println("Theme detector is dark: " + isDark);
			} else {
				System.out.println("Theme detector is not supported.");
				isDark = false;
			}
		} else {
			isDark = appearance.equals("dark");
		}

		System.out.println("Is dark: " + isDark);

		final Brightness brightness = isDark ? Brightness.DARK : Brightness.LIGHT;
		final Contrast contrast = isDark ? Contrast.HIGH : Contrast.DEFAULT;

		if (accentColor == null) {
			profileMaterialColorScheme
					.set(ColorScheme.newBuilder().setBrightness(brightness).setContrast(contrast)
							.setPrimaryColorSeed(Color.web("#BDCF47")).build());
		} else {
			profileMaterialColorScheme
					.set(ColorScheme.newBuilder().setBrightness(brightness).setContrast(contrast)
							.setPrimaryColorSeed(accentColor).build());
		}
		refreshWindowControls();
	}

	private void refreshWindowControls() {
		try {
			Platform.runLater(() -> {
				try {
					if (windowControlBar != null) {
						windowControlBar.refreshColors(
								colorToAwt(profileMaterialColorScheme.getSurface().get()),
								colorToAwt(profileMaterialColorScheme.getOnSurface().get()));
					}
				} catch (Exception ignored) {
				}
			});
		} catch (Exception ignored) {
		}
	}

	private void setUserAgent() {
		userAgent = Main.getUserAgent();
		System.out.println("User agent: " + userAgent);
	}

	public void openHistory() {
		createTab("evilbrowse://history");
	}

	/** Place tab chrome per tabPosition. Must run on the EDT. */
	private void applyTabBarLayout() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::applyTabBarLayout);
			return;
		}
		try {
			root.remove(tabBar);
		} catch (Exception ignored) {
		}
		try {
			if (sidebarPanel != null) {
				root.remove(sidebarPanel);
			}
		} catch (Exception ignored) {
		}
		try {
			root.remove(addressBar);
		} catch (Exception ignored) {
		}
		try {
			root.remove(centerPanel);
		} catch (Exception ignored) {
		}
		if (topPanel != null) {
			try {
				root.remove(topPanel);
			} catch (Exception ignored) {
			}
		}
		if ("horizontal".equals(tabPosition)) {
			if (topPanel == null) {
				topPanel = new JPanel(new BorderLayout());
			}
			// Firefox layout: tabs and window controls share the top row.
			final JPanel stripRow = new JPanel(new BorderLayout());
			stripRow.add(tabBar, BorderLayout.CENTER);
			// Window controls are now rendered by JavaFX natively inside TabBar in horizontal mode.
			// No need to add the Swing windowControlBar here.
			topPanel.add(stripRow, BorderLayout.NORTH);
			topPanel.add(addressBar, BorderLayout.SOUTH);
			root.add(topPanel, BorderLayout.NORTH);
		} else {
			if (sidebarPanel == null) {
				sidebarPanel = new JPanel(new BorderLayout());
				sidebarPanel.add(tabBar, BorderLayout.CENTER);
			}
			if (windowControlBar != null) {
				sidebarPanel.add(windowControlBar, BorderLayout.NORTH);
			}
			root.add(sidebarPanel, BorderLayout.WEST);
			root.add(addressBar, BorderLayout.NORTH);
		}
		root.add(centerPanel, BorderLayout.CENTER);
		root.revalidate();
		root.repaint();
	}

	public String getTabPosition() {
		return "horizontal".equalsIgnoreCase(tabPosition) ? "horizontal" : "vertical";
	}

	/** Called from the JavaFX tab strip to drag an undecorated window. */
	public void moveWindowBy(int dx, int dy) {
		SwingUtilities.invokeLater(() -> {
			try {
				if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
					return; // Never move a maximized window.
				}
				setLocation(getX() + dx, getY() + dy);
			} catch (Exception ignored) {
			}
		});
	}

	private java.awt.Dimension lastNormalSize = new java.awt.Dimension(1280, 800);

	/**
	 * Firefox behavior: starting a drag on a maximized window restores it
	 * first, keeping the cursor at the same relative strip position so the
	 * grab feels continuous.
	 */
	public void beginWindowDrag(double screenX, double screenY) {
		SwingUtilities.invokeLater(() -> {
			try {
				if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
					return;
				}
				final double w = getWidth();
				double fx = w > 0 ? (screenX - getX()) / w : 0.2;
				fx = Math.min(0.95, Math.max(0.05, fx));
				setExtendedState(JFrame.NORMAL);
				final java.awt.Dimension size = lastNormalSize != null
						? lastNormalSize
						: new java.awt.Dimension(1280, 800);
				setSize(size);
				final int nx = (int) Math.round(screenX - fx * size.width);
				final int ny = (int) Math.round(screenY - 20);
				setLocation(nx, ny);
			} catch (Exception ignored) {
			}
		});
	}

	private static final double WINDOW_CORNER_RADIUS = 14;

	/**
	 * Rounded window corners for the undecorated frame. Cleared whenever the
	 * window is maximized or fullscreen so content uses the full screen.
	 */
	private void updateWindowShape() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::updateWindowShape);
			return;
		}
		try {
			if (isWindowMaximized() || isFullscreen) {
				setShape(null);
				return;
			}
			final int w = getWidth();
			final int h = getHeight();
			if (w <= 0 || h <= 0) {
				return;
			}
			setShape(new java.awt.geom.RoundRectangle2D.Double(
					0, 0, w, h, WINDOW_CORNER_RADIUS, WINDOW_CORNER_RADIUS));
		} catch (Exception ignored) {
		}
	}

	public boolean isWindowMaximized() {
		return (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
	}

	public void toggleMaximizeWindow() {
		SwingUtilities.invokeLater(() -> {
			try {
				if (isWindowMaximized()) {
					setExtendedState(JFrame.NORMAL);
					// Restore below the WM titlebar-free area.
					setVisible(true);
				} else {
					if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0
							&& getWidth() > 0 && getHeight() > 0) {
						lastNormalSize = getSize();
					}
					setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
				}
				updateWindowShape();
			} catch (Exception ignored) {
			}
		});
	}

	/**
	 * Switch tab chrome live. Safe from any thread: persistence happens here,
	 * Swing re-layout on the EDT, FX scene rebuild on the FX thread.
	 */
	public void setTabPosition(String position) {
		String next = "horizontal".equalsIgnoreCase(position) ? "horizontal" : "vertical";
		tabPosition = next;
		try {
			profileDatabase.setTabPosition(next);
		} catch (Exception ignored) {
		}
		applyTabBarLayout();
		try {
			tabBar.setOrientation(next);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// setOrientation hops to the FX thread internally. If the FX rebuild
		// did not land (previously swallowed silently, leaving Swing/FX
		// desynced with a black strip), retry once instead of staying broken.
		Thread.ofVirtual().start(() -> {
			try {
				Thread.sleep(800);
				boolean wantHorizontal = "horizontal".equals(next);
				if (tabBar.isHorizontal() != wantHorizontal) {
					System.err.println("Tab orientation mismatch, retrying: " + next);
					tabBar.setOrientation(next);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void setSidebar(ToolSidebar sidebar) {
		if (sidebar == toolSidebar)
			return;

		if (toolSidebar != null) {
			toolSidebar.closeSidebar();
			sidePanel.remove(toolSidebar);
		}

		toolSidebar = sidebar;
		if (toolSidebar != null) {
			sidePanel.add(toolSidebar, BorderLayout.CENTER);
		}

		sidePanel.revalidate();
		sidePanel.repaint();
	}

	public ToolSidebar getSidebar() {
		return toolSidebar;
	}

	public String colorToHex(Color color) {
		if (color == null) {
			return "#BDCF47";
		}
		final String hex = String.format("#%02x%02x%02x",
				(int) (color.getRed() * 255),
				(int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
		return hex;
	}

	public java.awt.Color colorToAwt(Color color) {
		if (color == null) {
			return new java.awt.Color(0xBD, 0xCF, 0x47);
		}
		return new java.awt.Color(
				(float) color.getRed(),
				(float) color.getGreen(),
				(float) color.getBlue(),
				(float) color.getOpacity());
	}

	public void toggleFullscreen() {
		toggleFullscreen(!isFullscreen);
	}

	public void toggleFullscreen(boolean fullscreen) {
		final boolean fs = fullscreen;
		SwingUtilities.invokeLater(() -> {
			try {
				if (fs) {
					isFullscreen = true;
					addressBar.setVisible(false);
					tabBar.setVisible(false);
				} else {
					isFullscreen = false;
					addressBar.setVisible(true);
					tabBar.setVisible(true);
				}
				root.revalidate();
				root.repaint();
			} catch (Exception ignored) {
			}
			updateWindowShape();
		});
	}
}
