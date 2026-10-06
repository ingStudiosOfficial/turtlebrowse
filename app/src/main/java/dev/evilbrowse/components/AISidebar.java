package dev.evilbrowse.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import dev.evilbrowse.handlers.EvilBrowseLoadHandler.JSQueueItem;
import dev.evilbrowse.tools.SummarizePageTool;
import dev.evilbrowse.windows.MainWindow;

/**
 * Opt-in AI sidebar. Construction is cheap: no CEF browser and no Ollama
 * connection are created until the user enables AI and opens the panel.
 * This keeps browser cold startup fast.
 */
public class AISidebar extends ToolSidebar {
	private volatile Component ui;
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public volatile boolean isOpen = false;
	private volatile CefBrowser aiBrowser;
	private final CefClient cefClient;
	private final boolean useOsr;
	private final BooleanProperty isUiFocused;
	private final ScheduledExecutorService summarizeScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "evilbrowse-ai-summarize");
		t.setDaemon(true);
		return t;
	});
	private final ScheduledExecutorService rewriteScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "evilbrowse-ai-rewrite");
		t.setDaemon(true);
		return t;
	});
	private final ScheduledExecutorService summarizePageScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "evilbrowse-ai-summarize-page");
		t.setDaemon(true);
		return t;
	});
	private final Gson gson = new Gson();
	private final SummarizePageTool summarizePageTool;
	private final JFXPanel actionsBarJfxPanel;
	private final MainWindow parent;
	private volatile boolean browserReady = false;
	private volatile boolean shutdown = false;

	public AISidebar(CefClient client, MainWindow parent, boolean useOsr, BooleanProperty isUiFocused) {
		this.parent = parent;
		this.cefClient = client;
		this.useOsr = useOsr;
		this.isUiFocused = isUiFocused;

		this.setLayout(new java.awt.BorderLayout());
		this.setPreferredSize(preferredDim);

		summarizePageTool = new SummarizePageTool(parent);

		actionsBarJfxPanel = new JFXPanel();
		actionsBarJfxPanel.setFocusable(true);
		actionsBarJfxPanel.setPreferredSize(new java.awt.Dimension(preferredDim.width, 50));

		Platform.runLater(() -> {
			final HBox actionsBar = new HBox();
			actionsBar.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
			actionsBar.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.profileMaterialColorScheme.getSurface()));
			actionsBar.setAlignment(Pos.CENTER_RIGHT);

			final Scene actionsBarScene = new Scene(actionsBar);
			actionsBarScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			actionsBarJfxPanel.setScene(actionsBarScene);
			actionsBar.prefWidthProperty().bind(actionsBarScene.widthProperty());
			actionsBar.prefHeightProperty().bind(actionsBarScene.heightProperty());

			final JFXButton closeButton = buildButton(Material2OutlinedAL.CLOSE);
			closeButton.setOnAction(event -> {
				closeSidebar();
			});

			actionsBar.getChildren().addAll(closeButton);
		});

		this.add(actionsBarJfxPanel, BorderLayout.NORTH);
		// NOTE: browser component is added lazily in ensureBrowser() on first open.
	}

	private JFXButton buildButton(Ikon iconName) {
		final JFXButton button = new JFXButton("");
		final FontIcon icon = new FontIcon(iconName);
		icon.setIconColor(parent.profileMaterialColorScheme.getOnSurface().get());
		parent.profileMaterialColorScheme.getOnSurface().addListener((observable, oldPaint, newPaint) -> {
			icon.setIconColor(newPaint);
		});
		button.setGraphic(icon);
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setStyle("-fx-padding: 10px;");
		button.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer()));
		button.setOnMouseEntered(event -> {
			button.setCursor(Cursor.HAND);
		});
		button.setOnMouseExited(event -> {
			button.setCursor(Cursor.DEFAULT);
		});
		return button;
	}

	/**
	 * Create the chat browser on first open (EDT). No-op when AI is disabled.
	 *
	 * @return true when the browser exists and is usable.
	 */
	private synchronized boolean ensureBrowser() {
		if (shutdown) {
			return false;
		}
		if (browserReady && aiBrowser != null && ui != null) {
			return true;
		}
		if (!parent.isAIEnabled()) {
			return false;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			final java.util.concurrent.atomic.AtomicBoolean result = new java.util.concurrent.atomic.AtomicBoolean(false);
			try {
				SwingUtilities.invokeAndWait(() -> result.set(ensureBrowser()));
			} catch (Exception ignored) {
			}
			return result.get();
		}
		try {
			CefBrowser browser = cefClient.createBrowser("evilbrowse://chat", useOsr, false);
			if (browser == null) {
				return false;
			}
			aiBrowser = browser;
			parent.requestHandler.setAiBrowser(browser);
			final Component browserComponent = browser.getUIComponent();
			ui = browserComponent;

			if (browserComponent.getMouseListeners().length == 0) {
				browserComponent.addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mousePressed(java.awt.event.MouseEvent event) {
						SwingUtilities.invokeLater(() -> {
							try {
								isUiFocused.set(false);
							} catch (Exception ignored) {
								// isUiFocused is FX-affine; set on FX thread instead.
								Platform.runLater(() -> {
									try {
										isUiFocused.set(false);
									} catch (Exception ignored2) {
									}
								});
							}
							browserComponent.requestFocusInWindow();
							try {
								browser.setFocus(true);
							} catch (Exception ignored) {
							}
						});
					}
				});
			}

			this.add(browserComponent, BorderLayout.CENTER);
			this.revalidate();
			browserReady = true;

			// Kick off the Ollama session in the background so the first prompt
			// does not pay the model-list/pull cost synchronously.
			parent.ensureOllamaSessionAsync(ready -> {
			}, err -> System.err.println("AI background init: " + err));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void toggleSidebar() {
		if (isOpen) {
			closeSidebar();
		} else {
			openSidebar();
		}
	}

	@Override
	public void openSidebar() {
		System.out.println("Opening AI sidebar.");
		if (!parent.isAIEnabled()) {
			// Secondary setting: redirect to Settings instead of showing a dead panel.
			try {
				parent.createTab("evilbrowse://settings");
			} catch (Exception ignored) {
			}
			return;
		}
		if (!ensureBrowser()) {
			return;
		}
		preferredDim.width = 500;
		Component localUi = ui;
		if (localUi != null) {
			localUi.setVisible(true);
		}
		isOpen = true;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void closeSidebar() {
		System.out.println("Closing AI sidebar.");
		preferredDim.width = 0;
		Component localUi = ui;
		if (localUi != null) {
			localUi.setVisible(false);
		}
		isOpen = false;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	/** Release scheduler threads; called from MainWindow.dispose(). */
	public void shutdown() {
		shutdown = true;
		try {
			summarizeScheduler.shutdownNow();
		} catch (Exception ignored) {
		}
		try {
			rewriteScheduler.shutdownNow();
		} catch (Exception ignored) {
		}
		try {
			summarizePageScheduler.shutdownNow();
		} catch (Exception ignored) {
		}
	}

	private void openPanelForAction() {
		SwingUtilities.invokeLater(() -> {
			if (!parent.isAIEnabled()) {
				try {
					parent.createTab("evilbrowse://settings");
				} catch (Exception ignored) {
				}
				return;
			}
			if (parent.getSidebar() != this) {
				parent.setSidebar(this);
				openSidebar();
			} else if (!isOpen) {
				openSidebar();
			}
		});
	}

	private boolean checkReady() {
		if (shutdown || !parent.isAIEnabled()) {
			return false;
		}
		return ensureBrowser() && aiBrowser != null;
	}

	public void summarize(String text) {
		if (text == null || text.isBlank()) {
			return;
		}
		if (!parent.isAIEnabled()) {
			openPanelForAction();
			return;
		}
		if (!isOpen) {
			openPanelForAction();
		}

		try {
			summarizeScheduler.schedule(() -> {
				try {
					if (!checkReady()) {
						return;
					}
					final String jsonText = gson.toJson("Summarize this:\n:::extract\n" + text + "\n:::");
					final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
							"window.addPrompt(" + jsonText + ");", "evilbrowse://chat");

					try {
						aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 500, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rewrite(String text) {
		if (text == null || text.isBlank()) {
			return;
		}
		if (!parent.isAIEnabled()) {
			openPanelForAction();
			return;
		}
		if (!isOpen) {
			openPanelForAction();
		}

		try {
			rewriteScheduler.schedule(() -> {
				try {
					if (!checkReady()) {
						return;
					}
					final String jsonText = gson.toJson("Rewrite\n:::extract" + text + "\n:::\nto be");
					final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
							"window.addPromptRewrite(" + jsonText + ");", "evilbrowse://chat");

					try {
						aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 1000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void summarizePage(String html) {
		System.out.printf("Summarizing page...\n");

		if (!parent.isAIEnabled()) {
			openPanelForAction();
			return;
		}
		if (!isOpen) {
			openPanelForAction();
		}

		try {
			summarizePageScheduler.schedule(() -> {
				if (html == null) {
					return;
				}
				try {
					if (!checkReady()) {
						return;
					}
					final String markdown = summarizePageTool.summarizePage(html);
					System.out.printf("Converted Markdown: %s\n", markdown);

					String jsonText = gson.toJson("Summarize this page:\n:::extract" + markdown + "\n:::");
					final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
							"window.addPrompt(" + jsonText + ");", "evilbrowse://chat");

					try {
						aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 500, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
