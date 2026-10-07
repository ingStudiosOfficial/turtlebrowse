package dev.evilbrowse.components;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.jfoenix.controls.JFXButton;

import dev.evilbrowse.windows.MainWindow;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

/**
 * EvilBrowse vertical tab sidebar.
 *
 * <p>
 * Replaces the old horizontal tab strip with a left-side vertical list.
 * All JavaFX mutations run on the FX thread; all browser disposal runs on the
 * Swing EDT via {@link MainWindow}. The map is a ConcurrentHashMap so CEF
 * callback threads can look up entries safely.
 */
public class TabBar extends JPanel {
	private final Map<CefBrowser, HBox> tabMap = new java.util.concurrent.ConcurrentHashMap<>();
	private final Map<CefBrowser, Label> titleLabelMap = new java.util.concurrent.ConcurrentHashMap<>();
	private final Map<CefBrowser, Label> faviconLabelMap = new java.util.concurrent.ConcurrentHashMap<>();
	private final List<CefBrowser> tabOrder = java.util.Collections.synchronizedList(new ArrayList<>());
	private final Deque<String> closedTabUrls = new ArrayDeque<>();
	private volatile VBox tabList;
	private volatile HBox tabStrip;
	private volatile BorderPane root;
	private volatile JFXPanel tabPanel;
	private volatile JFXButton collapseButton;
	private volatile JFXButton newTabButton;
	private volatile HBox header;
	private volatile boolean collapsed = false;
	private volatile boolean horizontal = false;
	private MainWindow parent;
	private volatile CefBrowser activeBrowser;

	private static final int EXPANDED_WIDTH = 232;
	private static final int COLLAPSED_WIDTH = 60;
	private static final int STRIP_HEIGHT = 40;
	private static final double H_TAB_WIDTH = 160;
	private static final double H_TAB_MAX_WIDTH = 220;
	private static final double H_TAB_MIN_WIDTH = 52;
	private static final String SHRINK_LISTENER_KEY = "evilbrowseShrinkListener";

	public TabBar(CefClient client, List<CefBrowser> tabs, MainWindow parent) {
		this.parent = parent;
		try {
			this.collapsed = parent.profileDatabase.getSidebarCollapsed();
		} catch (Exception ignored) {
			this.collapsed = false;
		}
		try {
			this.horizontal = "horizontal".equalsIgnoreCase(parent.profileDatabase.getTabPosition());
		} catch (Exception ignored) {
			this.horizontal = false;
		}

		this.setLayout(new java.awt.BorderLayout());

		tabPanel = new JFXPanel();
		if (horizontal) {
			tabPanel.setPreferredSize(new java.awt.Dimension(0, STRIP_HEIGHT));
			this.setPreferredSize(new java.awt.Dimension(0, STRIP_HEIGHT));
		} else {
			final int initialWidth = collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;
			tabPanel.setPreferredSize(new java.awt.Dimension(initialWidth, 800));
			this.setPreferredSize(new java.awt.Dimension(initialWidth, 800));
		}

		Platform.runLater(() -> {
			rebuildScene();
			synchronized (tabOrder) {
				for (final CefBrowser browser : tabs) {
					if (browser != null && !tabMap.containsKey(browser)) {
						addTabToUI(browser);
					}
				}
			}
		});

		this.add(tabPanel, java.awt.BorderLayout.CENTER);
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	/**
	 * Switch between vertical sidebar and horizontal strip. Rebuilds the FX
	 * scene and reparents existing tab rows in order; no browser is touched.
	 */
	public void setOrientation(String position) {
		final boolean wantHorizontal = "horizontal".equalsIgnoreCase(position);
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> setOrientation(position));
			return;
		}
		if (wantHorizontal == horizontal && root != null) {
			return;
		}
		horizontal = wantHorizontal;
		// Persist off the FX thread.
		Thread.ofVirtual().start(() -> {
			try {
				parent.profileDatabase.setTabPosition(wantHorizontal ? "horizontal" : "vertical");
			} catch (Exception ignored) {
			}
		});
		rebuildScene();
		for (final CefBrowser b : getTabOrderSnapshot()) {
			final HBox box = tabMap.get(b);
			if (box != null) {
				styleTabRow(b, box, b == activeBrowser);
			}
		}
		rebuildTabListOrder();
	}

	/** The container currently holding tab rows (VBox or HBox). */
	private javafx.scene.layout.Pane activeContainer() {
		if (horizontal) {
			return tabStrip;
		}
		return tabList;
	}

	private void rebuildScene() {
		if (horizontal) {
			buildHorizontalScene();
		} else {
			buildVerticalScene();
		}
		if (tabPanel.getScene() == null) {
			final Scene scene = horizontal
					? new Scene(root, 800, STRIP_HEIGHT)
					: new Scene(root, collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH, 800);
			scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			tabPanel.setScene(scene);
		} else {
			tabPanel.getScene().setRoot(root);
		}
		// Swing sizing on the EDT.
		SwingUtilities.invokeLater(() -> {
			if (horizontal) {
				tabPanel.setPreferredSize(new java.awt.Dimension(0, STRIP_HEIGHT));
				TabBar.this.setPreferredSize(new java.awt.Dimension(0, STRIP_HEIGHT));
			} else {
				final int w = collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;
				tabPanel.setPreferredSize(new java.awt.Dimension(w, 800));
				TabBar.this.setPreferredSize(new java.awt.Dimension(w, 800));
			}
			TabBar.this.revalidate();
		});
		applyCollapsedVisuals();
	}

	private void styleRoot(javafx.scene.layout.Region node) {
		node.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
		node.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(bg, null, null));
		}, parent.profileMaterialColorScheme.getSurface()));
	}

	private JFXButton makeNewTabButton(boolean stretch) {
		final JFXButton button = buildButton(Material2OutlinedAL.ADD);
		button.setTooltip(new Tooltip("New tab (Ctrl+T)"));
		if (stretch) {
			button.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(button, Priority.ALWAYS);
		}
		button.setOnAction(event -> {
			SwingUtilities.invokeLater(() -> {
				try {
					this.parent.createTab(this.parent.startUrl, true);
				} catch (Exception ignored) {
				}
			});
		});
		return button;
	}

	private void buildVerticalScene() {
		root = new BorderPane();
		styleRoot(root);

		// Header: new-tab + collapse toggle.
		header = new HBox(8);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(8, 10, 4, 10));
		header.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(bg, null, null));
		}, parent.profileMaterialColorScheme.getSurface()));

		newTabButton = makeNewTabButton(true);

		collapseButton = buildButton(collapsed ? Material2OutlinedAL.CHEVRON_RIGHT
				: Material2OutlinedAL.CHEVRON_LEFT);
		collapseButton.setTooltip(new Tooltip(collapsed ? "Expand sidebar" : "Collapse sidebar"));
		collapseButton.setOnAction(event -> setCollapsed(!collapsed));

		header.getChildren().addAll(newTabButton, collapseButton);

		tabList = new VBox(4);
		tabList.setPadding(new Insets(2, 8, 6, 8));
		tabList.setFillWidth(true);
		tabList.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(bg, null, null));
		}, parent.profileMaterialColorScheme.getSurface()));

		final ScrollPane scroll = new ScrollPane(tabList);
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		scroll.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(bg, null, null));
		}, parent.profileMaterialColorScheme.getSurface()));
		VBox.setVgrow(scroll, Priority.ALWAYS);

		root.setTop(header);
		root.setCenter(scroll);

		tabStrip = null;
	}

	private void buildHorizontalScene() {
		root = new BorderPane();
		styleRoot(root);

		// Firefox-style: tabs first, New Tab pinned after the strip.
		header = new HBox(4);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(4, 8, 4, 2));
		header.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(bg, null, null));
		}, parent.profileMaterialColorScheme.getSurface()));

		newTabButton = makeNewTabButton(false);
		collapseButton = null;
		header.getChildren().add(newTabButton);

		tabStrip = new HBox(4);
		tabStrip.setPadding(new Insets(4, 2, 4, 8));
		tabStrip.setAlignment(Pos.CENTER_LEFT);
		tabStrip.setFillHeight(true);
		tabStrip.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(bg, null, null));
		}, parent.profileMaterialColorScheme.getSurface()));
		HBox.setHgrow(tabStrip, Priority.ALWAYS);

		final HBox strip = new HBox(0);
		strip.setAlignment(Pos.CENTER_LEFT);
		strip.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(bg, null, null));
		}, parent.profileMaterialColorScheme.getSurface()));
		// Firefox order: shrinking tabs first, New Tab button trailing.
		// No scrollbar: tabs flex and shrink like Firefox instead of scrolling.
		strip.getChildren().addAll(tabStrip, header, createWindowControls());
		HBox.setHgrow(tabStrip, Priority.ALWAYS);

		root.setCenter(strip);
		tabList = null;
		installDragHandlers(root);
	}

	private HBox createWindowControls() {
		HBox container = new HBox(0);
		container.setAlignment(Pos.CENTER);
		container.setMinHeight(STRIP_HEIGHT);
		container.setMaxHeight(STRIP_HEIGHT);

		SVGPath minPath = new SVGPath();
		minPath.setContent("M 0 5 L 10 5");
		minPath.setStrokeWidth(1.3);
		minPath.setFill(javafx.scene.paint.Color.TRANSPARENT);
		minPath.strokeProperty().bind(parent.profileMaterialColorScheme.getOnSurface());

		Region minBtn = createControlBtn(minPath, () -> {
			SwingUtilities.invokeLater(() -> parent.setExtendedState(parent.getExtendedState() | javax.swing.JFrame.ICONIFIED));
		}, false);

		SVGPath maxPath = new SVGPath();
		maxPath.setContent("M 0 0 h 10 v 10 h -10 z");
		maxPath.setStrokeWidth(1.3);
		maxPath.setFill(javafx.scene.paint.Color.TRANSPARENT);
		maxPath.strokeProperty().bind(parent.profileMaterialColorScheme.getOnSurface());

		Region maxBtn = createControlBtn(maxPath, () -> {
			parent.toggleMaximizeWindow();
		}, false);

		SVGPath closePath = new SVGPath();
		closePath.setContent("M 0 0 L 10 10 M 10 0 L 0 10");
		closePath.setStrokeWidth(1.3);
		closePath.setFill(javafx.scene.paint.Color.TRANSPARENT);
		closePath.strokeProperty().bind(parent.profileMaterialColorScheme.getOnSurface());

		Region closeBtn = createControlBtn(closePath, () -> {
			SwingUtilities.invokeLater(() -> parent.dispose());
		}, true);

		closeBtn.hoverProperty().addListener((obs, old, isHover) -> {
			if (isHover) {
				closePath.strokeProperty().unbind();
				closePath.setStroke(javafx.scene.paint.Color.WHITE);
			} else {
				closePath.strokeProperty().bind(parent.profileMaterialColorScheme.getOnSurface());
			}
		});

		container.getChildren().addAll(minBtn, maxBtn, closeBtn);
		return container;
	}

	private Region createControlBtn(Node icon, Runnable action, boolean isClose) {
		StackPane pane = new StackPane(icon);
		pane.setPrefSize(34, STRIP_HEIGHT);
		pane.setMinSize(34, STRIP_HEIGHT);
		pane.setMaxSize(34, STRIP_HEIGHT);
		pane.setCursor(Cursor.DEFAULT);

		pane.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			Paint bg = parent.profileMaterialColorScheme.getSurface().get();
			if (pane.isHover()) {
				if (isClose) {
					bg = javafx.scene.paint.Color.web("#E83A3A");
				} else {
					if (bg instanceof javafx.scene.paint.Color) {
						bg = ((javafx.scene.paint.Color) bg).deriveColor(0, 1.0, 1.15, 1.0);
					}
				}
			}
			return new Background(new BackgroundFill(bg, null, null));
		}, pane.hoverProperty(), parent.profileMaterialColorScheme.getSurface()));

		pane.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				action.run();
			}
		});

		return pane;
	}

	/**
	 * Undecorated windows need an in-app drag region: pressing on the empty
	 * tab-strip area moves the window, double-click maximizes it. Tab rows opt
	 * out so normal tab drag-reorder keeps working.
	 */
	private void installDragHandlers(javafx.scene.layout.Region target) {
		final double[] pressX = { 0 };
		final double[] pressY = { 0 };
		final boolean[] dragging = { false };

		target.setOnMousePressed(event -> {
			if (isTabRowOrChild(event.getTarget())) {
				return;
			}
			if (event.getButton() != MouseButton.PRIMARY) {
				return;
			}
			pressX[0] = event.getScreenX();
			pressY[0] = event.getScreenY();
			dragging[0] = true;
			try {
				parent.beginWindowDrag(event.getScreenX(), event.getScreenY());
			} catch (Exception ignored) {
			}
		});
		target.setOnMouseDragged(event -> {
			if (!dragging[0]) {
				return;
			}
			final double dx = event.getScreenX() - pressX[0];
			final double dy = event.getScreenY() - pressY[0];
			if (dx == 0 && dy == 0) {
				return;
			}
			pressX[0] = event.getScreenX();
			pressY[0] = event.getScreenY();
			try {
				parent.moveWindowBy((int) Math.round(dx), (int) Math.round(dy));
			} catch (Exception ignored) {
			}
		});
		target.setOnMouseReleased(event -> dragging[0] = false);
		target.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && !isTabRowOrChild(event.getTarget())) {
				try {
					parent.toggleMaximizeWindow();
				} catch (Exception ignored) {
				}
			}
		});
	}

	private static final String TAB_ROW_KEY = "evilbrowseTabRow";

	private boolean isTabRowOrChild(javafx.event.EventTarget target) {
		if (!(target instanceof Node node)) {
			return false;
		}
		while (node != null) {
			if (Boolean.TRUE.equals(node.getProperties().get(TAB_ROW_KEY))) {
				return true;
			}
			if (node instanceof ButtonBase || node instanceof ScrollBar) {
				return true;
			}
			node = node.getParent();
		}
		return false;
	}

	private void applyCollapsedVisuals() {
		applyCollapsedState(false);
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean value) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> setCollapsed(value));
			return;
		}
		this.collapsed = value;
		// DB write off the FX thread: Nitrite I/O must not block UI.
		Thread.ofVirtual().start(() -> {
			try {
				parent.profileDatabase.setSidebarCollapsed(value);
			} catch (Exception ignored) {
			}
		});
		applyCollapsedState(true);
	}

	private void applyCollapsedState(boolean animate) {
		if (root == null || tabPanel == null) {
			return;
		}
		if (horizontal) {
			// No collapse in strip mode; ensure strip height on the EDT.
			SwingUtilities.invokeLater(() -> {
				tabPanel.setPreferredSize(new java.awt.Dimension(0, STRIP_HEIGHT));
				TabBar.this.setPreferredSize(new java.awt.Dimension(0, STRIP_HEIGHT));
				TabBar.this.revalidate();
			});
			return;
		}
		final int targetWidth = collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;
		// Swing sizing must happen on the EDT; this method runs on the FX thread.
		SwingUtilities.invokeLater(() -> {
			tabPanel.setPreferredSize(new java.awt.Dimension(targetWidth, 800));
			TabBar.this.setPreferredSize(new java.awt.Dimension(targetWidth, 800));
			TabBar.this.revalidate();
			java.awt.Container top = TabBar.this.getTopLevelAncestor();
			if (top != null) {
				top.validate();
			}
		});
		if (collapseButton != null) {
			final FontIcon icon = new FontIcon(
					collapsed ? Material2OutlinedAL.CHEVRON_RIGHT : Material2OutlinedAL.CHEVRON_LEFT);
			try {
				icon.setIconColor(parent.profileMaterialColorScheme.getOnSurface().get());
			} catch (Exception ignored) {
			}
			collapseButton.setGraphic(icon);
			collapseButton.setTooltip(new Tooltip(collapsed ? "Expand sidebar" : "Collapse sidebar"));
		}
		// 60px fits one button: hide New Tab while collapsed (still in
		// right-click menu + Ctrl+T). Prevents clipped/overlapping header.
		if (newTabButton != null) {
			newTabButton.setVisible(!collapsed);
			newTabButton.setManaged(!collapsed);
		}
		if (header != null) {
			header.setAlignment(Pos.CENTER);
		}
		// Refresh every tab row for the new mode.
		for (Map.Entry<CefBrowser, HBox> entry : tabMap.entrySet()) {
			styleTabRow(entry.getKey(), entry.getValue(), entry.getKey() == activeBrowser);
		}
	}

	public void addTabToUI(CefBrowser browser) {
		if (browser == null) {
			return;
		}
		if (!Platform.isFxApplicationThread()) {
			final CefBrowser b = browser;
			Platform.runLater(() -> addTabToUI(b));
			return;
		}
		if (activeContainer() == null) {
			System.err.println("addTabToUI: no container yet (scene not built), will heal on next switch.");
			return;
		}
		if (tabMap.containsKey(browser)) {
			return;
		}
		try {
			addTabToUIInner(browser);
		} catch (Exception e) {
			e.printStackTrace();
			tabMap.remove(browser);
			titleLabelMap.remove(browser);
			faviconLabelMap.remove(browser);
		}
	}

	/**
	 * Self-heal: every open browser must have a visible row. Runs on tab
	 * switches so a missed add (early return, swallowed error) repairs itself
	 * instead of leaving an empty strip with live tabs.
	 */
	private void healMissingRows() {
		if (!Platform.isFxApplicationThread()) {
			return;
		}
		if (activeContainer() == null) {
			return;
		}
		List<CefBrowser> open;
		try {
			open = new ArrayList<>(parent.openedBrowserTabs);
		} catch (Exception e) {
			return;
		}
		boolean added = false;
		for (final CefBrowser b : open) {
			if (b != null && !tabMap.containsKey(b)) {
				System.err.println("healMissingRows: re-adding row for browser " + b.getIdentifier());
				try {
					addTabToUIInner(b);
					added = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (added) {
			rebuildTabListOrder();
		}
	}

	private void addTabToUIInner(CefBrowser browser) {

		String initialTitle = "Loading...";
		try {
			String url = browser.getURL();
			if (url != null && !url.isBlank()) {
				initialTitle = shortTitleFor(url);
			}
		} catch (Exception ignored) {
		}

		final Label favicon = new Label(faviconLetter(initialTitle));
		favicon.setMinSize(28, 28);
		favicon.setMaxSize(28, 28);
		favicon.setAlignment(Pos.CENTER);
		favicon.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 14;");
		favicon.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint bg = parent.profileMaterialColorScheme.getPrimary().get();
			return new Background(new BackgroundFill(bg, new CornerRadii(14), null));
		}, parent.profileMaterialColorScheme.getPrimary()));
		favicon.textFillProperty().bind(Bindings.createObjectBinding(() -> {
			return parent.profileMaterialColorScheme.getOnPrimary().get();
		}, parent.profileMaterialColorScheme.getOnPrimary()));

		final Label tabTitle = new Label(initialTitle);
		tabTitle.setMaxWidth(Double.MAX_VALUE);
		tabTitle.setStyle("-fx-font-size: 12.5px; -fx-text-overrun: ellipsis;");
		tabTitle.textFillProperty().bind(Bindings.createObjectBinding(() -> {
			return parent.profileMaterialColorScheme.getOnSurface().get();
		}, parent.profileMaterialColorScheme.getOnSurface()));
		HBox.setHgrow(tabTitle, Priority.ALWAYS);

		final JFXButton closeButton = new JFXButton("");
		final FontIcon closeIcon = new FontIcon(Material2OutlinedAL.CLOSE);
		try {
			closeIcon.setIconSize(16);
			closeIcon.setIconColor(parent.profileMaterialColorScheme.getOnSurface().get());
		} catch (Exception ignored) {
		}
		parent.profileMaterialColorScheme.getOnSurface().addListener((obs, o, n) -> {
			try {
				closeIcon.setIconColor(n);
			} catch (Exception ignored) {
			}
		});
		closeButton.setGraphic(closeIcon);
		closeButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		closeButton.setStyle("-fx-background-color: transparent; -fx-padding: 4;");
		closeButton.setCursor(Cursor.HAND);
		closeButton.setMinSize(24, 24);
		closeButton.setMaxSize(24, 24);
		closeButton.setOnAction(event -> {
			event.consume();
			closeTab(null, browser);
		});

		final HBox tabBox = new HBox(8);
		tabBox.getProperties().put(TAB_ROW_KEY, Boolean.TRUE);
		tabBox.setAlignment(Pos.CENTER_LEFT);
		tabBox.setPadding(new Insets(7, 8, 7, 8));
		tabBox.setCursor(Cursor.HAND);
		tabBox.getChildren().addAll(favicon, tabTitle, closeButton);

		tabBox.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				event.consume();
				try {
					this.parent.showTab(browser);
				} catch (Exception ignored) {
				}
			} else if (event.getButton() == MouseButton.MIDDLE) {
				event.consume();
				closeTab(tabBox, browser);
			}
		});
		tabBox.setOnMouseEntered(event -> tabBox.setCursor(Cursor.HAND));

		// Right-click context menu with only implemented actions.
		tabBox.setOnContextMenuRequested(event -> {
			final ContextMenu menu = buildTabContextMenu(browser);
			menu.show(tabBox, event.getScreenX(), event.getScreenY());
			event.consume();
		});

		// Drag to reorder.
		tabBox.setOnDragDetected(event -> {
			final Dragboard db = tabBox.startDragAndDrop(TransferMode.MOVE);
			final ClipboardContent content = new ClipboardContent();
			content.putString(String.valueOf(System.identityHashCode(browser)));
			db.setContent(content);
			event.consume();
		});
		tabBox.setOnDragOver(event -> {
			if (event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
		tabBox.setOnDragDropped(event -> {
			final String dragged = event.getDragboard().getString();
			if (dragged != null && dragged.equals(String.valueOf(System.identityHashCode(browser)))) {
				event.setDropCompleted(false);
				event.consume();
				return;
			}
			// Find dragged browser and move it before this tab.
			CefBrowser draggedBrowser = null;
			for (CefBrowser b : tabMap.keySet()) {
				if (String.valueOf(System.identityHashCode(b)).equals(dragged)) {
					draggedBrowser = b;
					break;
				}
			}
			if (draggedBrowser != null) {
				reorderTab(draggedBrowser, browser);
				event.setDropCompleted(true);
			} else {
				event.setDropCompleted(false);
			}
			event.consume();
		});

		tabMap.put(browser, tabBox);
		titleLabelMap.put(browser, tabTitle);
		faviconLabelMap.put(browser, favicon);
		synchronized (tabOrder) {
			tabOrder.add(browser);
		}

		styleTabRow(browser, tabBox, browser == activeBrowser);

		// Subtle open animation, kept short to stay responsive with many tabs.
		tabBox.setOpacity(0);
		tabBox.setTranslateX(collapsed ? 0 : -12);
		final FadeTransition fade = new FadeTransition(Duration.seconds(0.12), tabBox);
		fade.setFromValue(0);
		fade.setToValue(1);
		final TranslateTransition slide = new TranslateTransition(Duration.seconds(0.12), tabBox);
		slide.setFromX(collapsed ? 0 : -12);
		slide.setToX(0);
		fade.play();
		slide.play();

		rebuildTabListOrder();
	}

	private ContextMenu buildTabContextMenu(CefBrowser browser) {
		final ContextMenu menu = new ContextMenu();

		final MenuItem newTab = new MenuItem("New tab");
		newTab.setOnAction(e -> SwingUtilities.invokeLater(() -> {
			try {
				parent.createTab(parent.startUrl, true);
			} catch (Exception ignored) {
			}
		}));

		final MenuItem reload = new MenuItem("Reload tab");
		reload.setOnAction(e -> {
			try {
				browser.reload();
			} catch (Exception ignored) {
			}
		});

		final MenuItem duplicate = new MenuItem("Duplicate tab");
		duplicate.setOnAction(e -> SwingUtilities.invokeLater(() -> {
			try {
				parent.duplicateTab(browser);
			} catch (Exception ignored) {
			}
		}));

		final MenuItem close = new MenuItem("Close tab");
		close.setOnAction(e -> closeTab(browser));

		final MenuItem closeOthers = new MenuItem("Close other tabs");
		closeOthers.setOnAction(e -> SwingUtilities.invokeLater(() -> {
			try {
				parent.closeOtherTabs(browser);
			} catch (Exception ignored) {
			}
		}));

		final MenuItem closeRight = new MenuItem("Close tabs to the right");
		closeRight.setOnAction(e -> SwingUtilities.invokeLater(() -> {
			try {
				parent.closeTabsToRight(browser);
			} catch (Exception ignored) {
			}
		}));

		menu.getItems().addAll(newTab, reload, duplicate, close, closeOthers, closeRight);

		synchronized (closedTabUrls) {
			if (!closedTabUrls.isEmpty()) {
				final MenuItem reopen = new MenuItem("Reopen closed tab");
				reopen.setOnAction(e -> SwingUtilities.invokeLater(() -> {
					try {
						parent.reopenClosedTab();
					} catch (Exception ignored) {
					}
				}));
				menu.getItems().add(reopen);
			}
		}
		return menu;
	}

	private void reorderTab(CefBrowser dragged, CefBrowser before) {
		synchronized (tabOrder) {
			if (!tabOrder.contains(dragged) || !tabOrder.contains(before) || dragged == before) {
				return;
			}
			tabOrder.remove(dragged);
			final int idx = tabOrder.indexOf(before);
			if (idx < 0) {
				tabOrder.add(dragged);
			} else {
				tabOrder.add(idx, dragged);
			}
		}
		if (Platform.isFxApplicationThread()) {
			rebuildTabListOrder();
		} else {
			Platform.runLater(this::rebuildTabListOrder);
		}
	}

	private void rebuildTabListOrder() {
		if (!Platform.isFxApplicationThread()) {
			return;
		}
		final javafx.scene.layout.Pane container = activeContainer();
		if (container == null) {
			return;
		}
		container.getChildren().clear();
		synchronized (tabOrder) {
			for (CefBrowser b : tabOrder) {
				final HBox box = tabMap.get(b);
				if (box != null) {
					container.getChildren().add(box);
				}
			}
		}
	}

	private void styleTabRow(CefBrowser browser, HBox tabBox, boolean isActive) {
		if (tabBox == null) {
			return;
		}
		try {
			tabBox.backgroundProperty().unbind();
		} catch (Exception ignored) {
		}
		final Label title = titleLabelMap.get(browser);
		final Label fav = faviconLabelMap.get(browser);
		final javafx.scene.Node closeBtn = tabBox.getChildren().size() >= 3
				? tabBox.getChildren().get(2)
				: null;

		if (horizontal) {
			// Strip mode: tabs flex to share the row and shrink like Firefox
			// instead of scrolling. Everything visible, no collapse.
			if (title != null) {
				title.setVisible(true);
				title.setManaged(true);
				title.setMinWidth(0);
			}
			if (closeBtn != null) {
				closeBtn.setVisible(true);
				closeBtn.setManaged(true);
				if (closeBtn instanceof Region r) {
					r.setMinSize(20, 20);
					r.setMaxSize(20, 20);
				}
			}
			if (fav != null) {
				fav.setMinSize(22, 22);
				fav.setMaxSize(22, 22);
			}
			tabBox.setAlignment(Pos.CENTER_LEFT);
			tabBox.setPadding(new Insets(3, 6, 3, 6));
			tabBox.setPrefWidth(H_TAB_WIDTH);
			tabBox.setMaxWidth(H_TAB_MAX_WIDTH);
			tabBox.setMinWidth(H_TAB_MIN_WIDTH);
			HBox.setHgrow(tabBox, Priority.ALWAYS);
			if (tabBox.getProperties().putIfAbsent(SHRINK_LISTENER_KEY, Boolean.TRUE) == null) {
				final Label titleRef = title;
				final javafx.scene.Node closeRef = closeBtn;
				tabBox.widthProperty().addListener((obs, oldW, newW) -> {
					try {
						if (!horizontal) {
							return;
						}
						final boolean roomy = newW.doubleValue() >= 120;
						if (closeRef != null) {
							closeRef.setVisible(roomy);
							closeRef.setManaged(roomy);
						}
						if (titleRef != null) {
							titleRef.setVisible(newW.doubleValue() >= 64);
							titleRef.setManaged(newW.doubleValue() >= 64);
						}
					} catch (Exception ignored) {
					}
				});
			}
		} else if (collapsed) {
			if (title != null) {
				title.setVisible(false);
				title.setManaged(false);
			}
			if (closeBtn != null) {
				closeBtn.setVisible(false);
				closeBtn.setManaged(false);
			}
			tabBox.setAlignment(Pos.CENTER);
			tabBox.setPadding(new Insets(6, 8, 6, 8));
			tabBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
			tabBox.setMaxWidth(Double.MAX_VALUE);
			tabBox.setMinWidth(0);
			HBox.setHgrow(tabBox, Priority.NEVER);
			if (fav != null) {
				fav.setMinSize(28, 28);
				fav.setMaxSize(28, 28);
			}
			if (closeBtn instanceof Region r) {
				r.setMinSize(24, 24);
				r.setMaxSize(24, 24);
			}
		} else {
			if (title != null) {
				title.setVisible(true);
				title.setManaged(true);
			}
			if (closeBtn != null) {
				closeBtn.setVisible(true);
				closeBtn.setManaged(true);
				if (closeBtn instanceof Region r2) {
					r2.setMinSize(24, 24);
					r2.setMaxSize(24, 24);
				}
			}
			if (fav != null) {
				fav.setMinSize(28, 28);
				fav.setMaxSize(28, 28);
			}
			tabBox.setAlignment(Pos.CENTER_LEFT);
			tabBox.setPadding(new Insets(6, 8, 6, 8));
			// Clear fixed strip width when back in sidebar mode.
			tabBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
			tabBox.setMaxWidth(Double.MAX_VALUE);
			tabBox.setMinWidth(0);
			HBox.setHgrow(tabBox, Priority.NEVER);
		}

		tabBox.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			Paint color = isActive ? parent.profileMaterialColorScheme.getSurfaceContainer().get()
					: parent.profileMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(color, new CornerRadii(14), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer(),
				parent.profileMaterialColorScheme.getSurface()));

		// Tooltip always shows the full title (essential in collapsed mode).
		String tip = "Loading...";
		if (title != null && title.getText() != null && !title.getText().isBlank()) {
			tip = title.getText();
		}
		try {
			String url = browser.getURL();
			if (url != null && !url.isBlank() && !tip.equals(url)) {
				tip = tip + "\n" + url;
			}
		} catch (Exception ignored) {
		}
		Tooltip.install(tabBox, new Tooltip(tip));
	}

	private String faviconLetter(String title) {
		if (title == null || title.isBlank()) {
			return "○";
		}
		String t = title.trim();
		// Strip internal scheme noise for a nicer letter.
		if (t.startsWith("evilbrowse://")) {
			t = t.substring("evilbrowse://".length());
		} else if (t.startsWith("turtlebrowse://")) {
			t = t.substring("turtlebrowse://".length());
		}
		if (t.isEmpty()) {
			return "○";
		}
		return String.valueOf(Character.toUpperCase(t.charAt(0)));
	}

	private String shortTitleFor(String url) {
		if (url == null || url.isBlank()) {
			return "New Tab";
		}
		if (url.startsWith("evilbrowse://") || url.startsWith("turtlebrowse://")) {
			final String path = url.contains("://") ? url.substring(url.indexOf("://") + 3) : url;
			if (path.startsWith("newtab")) {
				return "New Tab";
			}
			if (path.startsWith("settings")) {
				return "Settings";
			}
			if (path.startsWith("history")) {
				return "History";
			}
			if (path.startsWith("chat")) {
				return "AI Chat";
			}
			if (path.startsWith("dino")) {
				return "Dino";
			}
			return path;
		}
		try {
			final java.net.URI uri = new java.net.URI(url);
			String host = uri.getHost();
			if (host != null && !host.isBlank()) {
				if (host.startsWith("www.")) {
					host = host.substring(4);
				}
				return host;
			}
		} catch (Exception ignored) {
		}
		return url.length() > 32 ? url.substring(0, 32) + "…" : url;
	}

	private JFXButton buildButton(Ikon iconName) {
		final JFXButton button = new JFXButton("");
		final FontIcon icon = new FontIcon(iconName);
		try {
			icon.setIconColor(parent.profileMaterialColorScheme.getOnSurface().get());
		} catch (Exception ignored) {
		}
		parent.profileMaterialColorScheme.getOnSurface().addListener((observable, oldPaint, newPaint) -> {
			try {
				icon.setIconColor(newPaint);
			} catch (Exception ignored) {
			}
		});
		button.setGraphic(icon);
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setStyle("-fx-padding: 8;");
		button.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(14), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer()));
		button.setOnMouseEntered(event -> button.setCursor(Cursor.HAND));
		button.setOnMouseExited(event -> button.setCursor(Cursor.DEFAULT));
		return button;
	}

	public void rememberClosedUrl(String url) {
		if (url == null || url.isBlank()) {
			return;
		}
		synchronized (closedTabUrls) {
			closedTabUrls.addFirst(url);
			while (closedTabUrls.size() > 10) {
				closedTabUrls.removeLast();
			}
		}
	}

	public String pollClosedUrl() {
		synchronized (closedTabUrls) {
			return closedTabUrls.pollFirst();
		}
	}

	private void closeTab(HBox tabBox, CefBrowser browser) {
		if (browser == null) {
			return;
		}
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> closeTab(tabBox, browser));
			return;
		}
		final HBox box = tabBox != null ? tabBox : tabMap.get(browser);
		if (box == null) {
			tabMap.remove(browser);
			titleLabelMap.remove(browser);
			faviconLabelMap.remove(browser);
			synchronized (tabOrder) {
				tabOrder.remove(browser);
			}
			SwingUtilities.invokeLater(() -> {
				try {
					this.parent.closeTab(browser);
				} catch (Exception ignored) {
				}
			});
			return;
		}
		box.setDisable(true);
		box.setMouseTransparent(true);
		final FadeTransition fade = new FadeTransition(Duration.seconds(0.12), box);
		fade.setFromValue(1);
		fade.setToValue(0);
		fade.setOnFinished(e -> {
			try {
				final javafx.scene.layout.Pane container = activeContainer();
				if (container != null) {
					container.getChildren().remove(box);
				}
			} catch (Exception ignored) {
			}
			tabMap.remove(browser);
			titleLabelMap.remove(browser);
			faviconLabelMap.remove(browser);
			synchronized (tabOrder) {
				tabOrder.remove(browser);
			}
			SwingUtilities.invokeLater(() -> {
				try {
					this.parent.closeTab(browser);
				} catch (Exception ignored) {
				}
			});
		});
		fade.play();
	}

	public void closeTab(CefBrowser browser) {
		if (browser == null) {
			return;
		}
		HBox box = null;
		if (Platform.isFxApplicationThread()) {
			box = tabMap.get(browser);
			if (box == null) {
				tabMap.remove(browser);
				titleLabelMap.remove(browser);
				faviconLabelMap.remove(browser);
				synchronized (tabOrder) {
					tabOrder.remove(browser);
				}
				SwingUtilities.invokeLater(() -> {
					try {
						this.parent.closeTab(browser);
					} catch (Exception ignored) {
					}
				});
				return;
			}
			closeTab(box, browser);
		} else {
			Platform.runLater(() -> closeTab(browser));
		}
	}

	/** Remove the tab UI without touching the browser (used for close-others). */
	public void removeTabUiOnly(CefBrowser browser) {
		if (browser == null) {
			return;
		}
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> removeTabUiOnly(browser));
			return;
		}
		final HBox box = tabMap.remove(browser);
		titleLabelMap.remove(browser);
		faviconLabelMap.remove(browser);
		synchronized (tabOrder) {
			tabOrder.remove(browser);
		}
		if (box != null) {
			try {
				final javafx.scene.layout.Pane container = activeContainer();
				if (container != null) {
					container.getChildren().remove(box);
				}
			} catch (Exception ignored) {
			}
		}
	}

	public void setTabTitle(CefBrowser browser, String title) {
		if (browser == null || title == null) {
			return;
		}
		if (!Platform.isFxApplicationThread()) {
			final String t = title;
			Platform.runLater(() -> setTabTitle(browser, t));
			return;
		}
		final Label tabTitle = titleLabelMap.get(browser);
		final HBox box = tabMap.get(browser);
		if (tabTitle == null || box == null) {
			return;
		}
		String display = title.isBlank() ? shortTitleFor(safeUrl(browser)) : title;
		if (display.length() > 48) {
			display = display.substring(0, 48) + "…";
		}
		tabTitle.setText(display);
		final Label fav = faviconLabelMap.get(browser);
		if (fav != null) {
			fav.setText(faviconLetter(display));
		}
		styleTabRow(browser, box, browser == activeBrowser);
	}

	private String safeUrl(CefBrowser browser) {
		try {
			return browser.getURL();
		} catch (Exception e) {
			return "";
		}
	}

	public void setTabLoading(CefBrowser browser, boolean loading) {
		if (browser == null) {
			return;
		}
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> setTabLoading(browser, loading));
			return;
		}
		final HBox box = tabMap.get(browser);
		if (box == null) {
			return;
		}
		box.setOpacity(loading ? 0.7 : 1.0);
	}

	public void setCurrentTab(CefBrowser currentBrowser) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> setCurrentTab(currentBrowser));
			return;
		}
		if (activeContainer() == null) {
			return;
		}
		this.activeBrowser = currentBrowser;
		healMissingRows();
		for (final Map.Entry<CefBrowser, HBox> entry : tabMap.entrySet()) {
			styleTabRow(entry.getKey(), entry.getValue(), entry.getKey() == currentBrowser);
		}
		// Keep the active tab visible when there are many tabs.
		try {
			final HBox active = currentBrowser != null ? tabMap.get(currentBrowser) : null;
			if (active != null) {
				final javafx.scene.layout.Pane container = activeContainer();
				if (container != null) {
					container.requestLayout();
				}
			}
		} catch (Exception ignored) {
		}
	}

	public List<CefBrowser> getTabOrderSnapshot() {
		synchronized (tabOrder) {
			return new ArrayList<>(tabOrder);
		}
	}
}
