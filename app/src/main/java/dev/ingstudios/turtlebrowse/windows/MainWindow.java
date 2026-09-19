package dev.ingstudios.turtlebrowse.windows;

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
import org.glavo.monetfx.ColorScheme;
import org.glavo.monetfx.beans.property.ColorSchemeProperty;
import org.glavo.monetfx.beans.property.SimpleColorSchemeProperty;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.components.AISidebar;
import dev.ingstudios.turtlebrowse.components.AddressBar;
import dev.ingstudios.turtlebrowse.components.FindSidebar;
import dev.ingstudios.turtlebrowse.components.MoreSidebar;
import dev.ingstudios.turtlebrowse.components.TabBar;
import dev.ingstudios.turtlebrowse.components.ToolSidebar;
import dev.ingstudios.turtlebrowse.components.YtdlpSidebar;
import dev.ingstudios.turtlebrowse.db.ProfileDatabase;
import dev.ingstudios.turtlebrowse.db.MainDatabase.ProfileStructureWithId;
import dev.ingstudios.turtlebrowse.db.ProfileDatabase.AISettings;
import dev.ingstudios.turtlebrowse.db.ProfileDatabase.HistoryItem;
import dev.ingstudios.turtlebrowse.db.ProfileDatabase.NewtabSettings;
import dev.ingstudios.turtlebrowse.handlers.CefKeyboardHandler;
import dev.ingstudios.turtlebrowse.handlers.SwingKeyboardHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseContextMenuHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseDialogHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseDisplayHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseDownloadHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseFocusHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseLifeSpanHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseLoadHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowsePrintHandler;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseRequestHandler;
import dev.ingstudios.turtlebrowse.managers.CefAppManager;
import dev.ingstudios.turtlebrowse.managers.DiscordPresenceManager;
import dev.ingstudios.turtlebrowse.managers.InstanceManager;
import dev.ingstudios.turtlebrowse.managers.WallpaperManager;
import dev.ingstudios.turtlebrowse.managers.WindowsManager;
import dev.ingstudios.turtlebrowse.managers.WindowsManager.WindowItem;
import dev.ingstudios.turtlebrowse.ollama.OllamaChat;
import dev.ingstudios.turtlebrowse.search.SearchAutosuggest;
import dev.ingstudios.turtlebrowse.search.SearchURLTemplates;
import io.github.ollama4j.exceptions.OllamaException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

public class MainWindow extends JFrame {
	private final boolean USE_OSR = false;

	final public String startUrl = "turtlebrowse://newtab";
	private CefClient cefClient;
	public CefBrowser currentBrowser;
	public ArrayList<CefBrowser> openedBrowserTabs = new ArrayList<>();
	private JPanel root;
	private final CardLayout browserLayout = new CardLayout();
	private final JPanel browserContainer = new JPanel(browserLayout);
	public AddressBar addressBar;
	public TabBar tabBar;
	public final Map<CefBrowser, String> titleMap = new HashMap<>();
	public final BooleanProperty isUiFocused = new SimpleBooleanProperty(false);
	public OllamaChat ollamaSession;
	private ToolSidebar toolSidebar;
	public final AISidebar aiSidebar;
	public final YtdlpSidebar ytdlpSidebar;
	public final FindSidebar findSidebar;
	public final MoreSidebar moreSidebar;
	private final JPanel sidePanel;
	private final Gson gson = new Gson();
	public final TurtlebrowseLoadHandler loadHandler = new TurtlebrowseLoadHandler();
	public final TurtlebrowseRequestHandler requestHandler = new TurtlebrowseRequestHandler(this);
	public final ProfileStructureWithId currentProfile;
	public ColorSchemeProperty profileMaterialColorScheme = new SimpleColorSchemeProperty(
			ColorScheme.fromSeed(Color.web("#BDCF47")));
	public String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.1.0 Safari/537.36";
	private final String windowId;
	private final CefAppManager cefAppManager = CefAppManager.getInstance(this);
	private final CefApp cefApp = cefAppManager.getCefApp();
	public final ProfileDatabase profileDatabase;
	public String defaultSearchProvider = SearchURLTemplates.searchTemplates.get("brave");
	public boolean enableDiscordPresence = false;
	public AISettings aiSettings = new AISettings(false, "gemma4:e2b");
	public NewtabSettings newtabSettings = new NewtabSettings("");
	private final SearchAutosuggest searchAutosuggest;

	public MainWindow(ProfileStructureWithId profile) {
		this(profile, "turtlebrowse://newtab");
	}

	public MainWindow(ProfileStructureWithId profile, String launchUrl) {
		super("Turtlebrowse");

		System.out.println("Creating main window for profile: " + profile.getIdAsString());

		currentProfile = profile;

		profileDatabase = ProfileDatabase.getInstance(currentProfile.getIdAsString());

		defaultSearchProvider = SearchURLTemplates.searchTemplates.get(profileDatabase.getDefaultSearchEngine());
		enableDiscordPresence = profileDatabase.getDiscordPresenceSetting();
		aiSettings = profileDatabase.getAISettings();
		newtabSettings = profileDatabase.getNewtabSettings();

		windowId = "%s_main_window".formatted(profile.getIdAsString());
		WindowsManager.getInstance()
				.addWindow(new WindowItem(windowId, MainWindow.class));

		System.out.println("AWT Toolkit: " + java.awt.Toolkit.getDefaultToolkit().getClass().getName());
		System.out.println("DISPLAY: " + System.getenv("DISPLAY"));
		System.out.println("WAYLAND_DISPLAY: " + System.getenv("WAYLAND_DISPLAY"));

		InstanceManager.getInstance().startListening(profile.getIdAsString(),
				this);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		final Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo_full_trans.png"));
		setIconImage(icon);

		root = new JPanel(new BorderLayout());
		setContentPane(root);

		setSize(1200, 800);
		setLocationRelativeTo(null);

		setUserAgent();
		setMaterialColorSchemeFromProfile();

		searchAutosuggest = new SearchAutosuggest(userAgent);

		// Address bar
		addressBar = new AddressBar(cefClient, this, startUrl);

		cefClient = cefApp.createClient();

		// Tab bar
		tabBar = new TabBar(cefClient, openedBrowserTabs, this);

		// AI sidebar
		aiSidebar = new AISidebar(cefClient, this, USE_OSR, isUiFocused);

		// yt-dlp sidebar
		ytdlpSidebar = new YtdlpSidebar(this);

		// Find sidebar
		findSidebar = new FindSidebar(this);

		// More sidebar
		moreSidebar = new MoreSidebar(this);

		// Keyboard handler (JCEF)
		cefClient.addKeyboardHandler(new CefKeyboardHandler(this, startUrl));

		// Keyboard handler (Swing)
		new SwingKeyboardHandler(this, startUrl);

		cefClient.addFocusHandler(new TurtlebrowseFocusHandler(this));

		// Top panel (address + tab)
		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tabBar, BorderLayout.NORTH);
		topPanel.add(addressBar, BorderLayout.SOUTH);

		sidePanel = new JPanel(new BorderLayout());
		sidePanel.add(aiSidebar, BorderLayout.CENTER);
		sidePanel.add(moreSidebar, BorderLayout.EAST);

		setSidebar(aiSidebar);

		// Bottom panel (main browser + AI sidebar)
		final JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(browserContainer, BorderLayout.CENTER);
		bottomPanel.add(sidePanel, BorderLayout.EAST);

		root.add(topPanel, BorderLayout.NORTH);
		root.add(bottomPanel, BorderLayout.CENTER);

		cefClient.addDisplayHandler(new TurtlebrowseDisplayHandler(this));
		cefClient.addLifeSpanHandler(new TurtlebrowseLifeSpanHandler(this));
		cefClient.addDialogHandler(new TurtlebrowseDialogHandler());
		cefClient.addDownloadHandler(new TurtlebrowseDownloadHandler());
		cefClient.addContextMenuHandler(new TurtlebrowseContextMenuHandler(this));
		cefClient.addPrintHandler(new TurtlebrowsePrintHandler());
		cefClient.addLoadHandler(loadHandler);
		cefClient.addRequestHandler(requestHandler);

		try {
			ollamaSession = new OllamaChat(userAgent, this);
			System.out.println("Successfully created Ollama chat session.");
		} catch (OllamaException e) {
			e.printStackTrace();
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				CefApp.getInstance().dispose();
				dispose();
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

	public void updateWindowTitle(String pageTitle) {
		this.setTitle(pageTitle + " - Turtlebrowse");
		if (enableDiscordPresence)
			DiscordPresenceManager.getInstance().updateDiscordPresence("Browsing " + pageTitle);
	}

	public void createTab(String url) {
		createTab(url, false);
	}

	public void createTab(String url, boolean selectAllField) {
		System.out.printf("Creating tab for URL: %s\n", url);

		CefBrowser browser = cefClient.createBrowser(url, USE_OSR, false);

		openedBrowserTabs.add(browser);

		Component ui = browser.getUIComponent();
		browserContainer.add(ui, String.valueOf(System.identityHashCode(browser)));

		Platform.runLater(() -> {
			tabBar.addTabToUI(browser);
			addressBar.focusAddressField(selectAllField);
		});

		showTab(browser);
	}

	public void closeTab(CefBrowser browser) {
		final int indexToClose = openedBrowserTabs.indexOf(browser);
		if (indexToClose == -1) {
			return;
		}

		CefBrowser nextBrowserToSelect = null;
		if (browser == currentBrowser && openedBrowserTabs.size() > 1) {
			int nextIndex = (indexToClose > 0) ? indexToClose - 1 : 1;
			nextBrowserToSelect = openedBrowserTabs.get(nextIndex);
		}

		browserContainer.remove(browser.getUIComponent());
		openedBrowserTabs.remove(browser);
		titleMap.remove(browser);

		browserContainer.revalidate();
		browserContainer.repaint();

		if (openedBrowserTabs.isEmpty()) {
			currentBrowser = null;
			browser.close(true);
			dispose();
		} else {
			if (nextBrowserToSelect != null) {
				showTab(nextBrowserToSelect);
			}
			browser.close(true);
		}
	}

	public void closeCurrentTab() {
		Platform.runLater(() -> tabBar.closeTab(currentBrowser));
	}

	public void showTab(CefBrowser browser) {
		if (browser == null || !openedBrowserTabs.contains(browser)) {
			return;
		}

		SwingUtilities.invokeLater(() -> {
			currentBrowser = browser;
			final Component ui = browser.getUIComponent();

			final Color bgColor = profileMaterialColorScheme.getSurface().get();

			ui.setBackground(new java.awt.Color(
					(float) bgColor.getRed(),
					(float) bgColor.getGreen(),
					(float) bgColor.getBlue(),
					(float) bgColor.getOpacity()));

			if (ui.getMouseListeners().length == 0) {
				ui.addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mousePressed(java.awt.event.MouseEvent event) {
						SwingUtilities.invokeLater(() -> {
							isUiFocused.set(false);
							browser.setFocus(true);
						});
					}
				});
			}

			final String browserTitle = titleMap.get(browser);
			updateWindowTitle(browserTitle != null ? browserTitle : "Loading...");

			final String cardKey = String.valueOf(System.identityHashCode(browser));
			browserLayout.show(browserContainer, cardKey);

			browserContainer.revalidate();
			browserContainer.repaint();

			Platform.runLater(() -> {
				addressBar.updateUrl(browser.getURL());
				tabBar.setCurrentTab(browser);
			});
		});
	}

	public void createDevTools() {
		currentBrowser.openDevTools();
	}

	@Override
	public void dispose() {
		System.out.println("Closing...");

		WindowsManager.getInstance().removeWindow(windowId);

		for (final CefBrowser browser : openedBrowserTabs) {
			if (browser != null)
				browser.close(true);
		}

		if (cefApp != null) {
			cefApp.dispose();
		}

		super.dispose();

		System.out.println("Successfully closed browser.");

		if (WindowsManager.getInstance().getWindows().size() == 0) {
			Platform.exit();
			System.exit(0);
		}
	}

	public String formatURL(String url, Boolean isSearching) {
		if (isSearching == null || isSearching) {
			return defaultSearchProvider.formatted(URLEncoder.encode(url, StandardCharsets.UTF_8));
		}

		final Set<String> allowedSchemes = Set.of(
				"http", "https", "file", "about", "localhost", "turtlebrowse");

		final String trimmedUrl = url.trim();

		if (trimmedUrl.startsWith("about:"))
			return trimmedUrl;

		try {
			URI uri = new URI(trimmedUrl);
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

		return defaultSearchProvider.formatted(URLEncoder.encode(url, StandardCharsets.UTF_8));
	}

	public void searchWeb(String query) {
		currentBrowser.loadURL(formatURL(query, true));
	}

	public String handleApiFromClient(String action, String body) {
		@SuppressWarnings("null")
		JsonObject params = gson.fromJson(body, JsonObject.class);

		switch (action) {
			case "GET_NAME": {
				System.out.println("GET_NAME called.");
				return currentProfile.name();
			}

			case "SEARCH_WEB": {
				final String query = params.get("query").getAsString();
				SwingUtilities.invokeLater(() -> searchWeb(query));
				return "\"ok\"";
			}

			case "GET_THEME": {
				final Color profileColor = currentProfile.seedColor();
				System.out.printf("Profile color: %s", profileColor);
				final String hex = String.format("#%02x%02x%02x",
						(int) (profileColor.getRed() * 255),
						(int) (profileColor.getGreen() * 255),
						(int) (profileColor.getBlue() * 255));
				return hex;
			}

			case "GET_SEARCH_ENGINE": {
				final String searchEngine = profileDatabase.getDefaultSearchEngine();
				return searchEngine;
			}

			case "SET_SEARCH_ENGINE": {
				final String searchEngineToSet = params.get("engine").getAsString();
				defaultSearchProvider = SearchURLTemplates.searchTemplates.get(searchEngineToSet);
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
					DiscordPresenceManager.getInstance().init();
				} else {
					DiscordPresenceManager.getInstance().disableDiscordPresence();
				}
				profileDatabase.setDiscordPresenceSetting(discordSettingToSet);
				return "\"ok\"";
			}

			case "GET_AI_SETTINGS": {
				final AISettings aiSettings = profileDatabase.getAISettings();
				return gson.toJson(aiSettings);
			}

			case "SET_AI_SETTINGS": {
				final boolean enabled = params.get("enabled").getAsBoolean();
				final String model = params.get("model").getAsString();
				final AISettings settings = new AISettings(enabled, model);
				aiSettings = settings;
				profileDatabase.setAISettings(settings);
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
				final String query = params.get("query").getAsString();
				final List<String> suggestions = searchAutosuggest.fetchAndProcess(query);
				return gson.toJson(suggestions);
			}

			case "GET_HISTORY": {
				final int index = params.get("index").getAsInt();
				final List<HistoryItem> history = profileDatabase.getHistory(index, 50);
				return gson.toJson(history);
			}

			case "NAVIGATE_TO_SITE": {
				final String url = params.get("url").getAsString();
				System.out.println("Site URL: " + url);
				createTab(url);
				return "\"ok\"";
			}

			case "DELETE_HISTORY_ITEM": {
				final UUID id = UUID.fromString(params.get("id").getAsString());
				profileDatabase.deleteFromHistory(id);
			}

			default:
				return "\"Unknown action\"";
		}
	}

	public OllamaChat getOllamaSession() {
		return ollamaSession;
	}

	private void setMaterialColorSchemeFromProfile() {
		final Color accentColor = currentProfile.seedColor();
		if (accentColor == null) {
			profileMaterialColorScheme.set(ColorScheme.fromSeed(Color.web("#BDCF47")));
		} else {
			profileMaterialColorScheme
					.set(ColorScheme.fromSeed(accentColor));
		}
	}

	private void setUserAgent() {
		userAgent = Main.getUserAgent();
	}

	public void openHistory() {
		createTab("turtlebrowse://history");
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
}
