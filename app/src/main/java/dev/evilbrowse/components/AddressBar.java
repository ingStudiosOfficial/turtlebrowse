package dev.evilbrowse.components;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Popup;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.*;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import com.jfoenix.controls.JFXButton;

import dev.evilbrowse.search.SearchAutosuggest;
import dev.evilbrowse.windows.MainWindow;

public class AddressBar extends JPanel {
	private TextField addressField;
	private final MainWindow parent;
	private JFXPanel addressBarPanel;
	private boolean wasFocused = false;
	private final SearchAutosuggest autosuggester;
	public HBox root;
	private boolean isProgrammaticChange = false;
	private volatile JFXButton securityButton;
	private volatile FontIcon securityIcon;

	public AddressBar(CefClient client, MainWindow parent, String startUrl) {
		this.parent = parent;

		autosuggester = parent.searchAutosuggest;

		this.setLayout(new java.awt.BorderLayout());

		addressBarPanel = new JFXPanel();
		addressBarPanel.setFocusable(true);
		addressBarPanel.setPreferredSize(new java.awt.Dimension(1200, 44));

		Platform.runLater(() -> {
			root = new HBox();
			root.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			root.setStyle("-fx-spacing: 6px; -fx-padding: 6px;");
			root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.profileMaterialColorScheme.getSurface()));
			root.setAlignment(Pos.CENTER);

			final Scene addressBarScene = new Scene(root);
			addressBarScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			addressBarPanel.setScene(addressBarScene);
			root.prefWidthProperty().bind(addressBarScene.widthProperty());
			root.prefHeightProperty().bind(addressBarScene.heightProperty());

			final JFXButton backButton = buildButton(Material2OutlinedAL.ARROW_BACK);
			backButton.setTooltip(new javafx.scene.control.Tooltip("Back (Alt+Left)"));
			backButton.setOnAction(event -> {
				System.out.println("Back button clicked.");
				CefBrowser browser = this.parent.currentBrowser;
				if (browser == null) {
					return;
				}
				try {
					if (browser.canGoBack())
						browser.goBack();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			final JFXButton forwardButton = buildButton(Material2OutlinedAL.ARROW_FORWARD);
			forwardButton.setTooltip(new javafx.scene.control.Tooltip("Forward (Alt+Right)"));
			forwardButton.setOnAction(event -> {
				System.out.println("Forward button clicked.");
				CefBrowser browser = this.parent.currentBrowser;
				if (browser == null) {
					return;
				}
				try {
					if (browser.canGoForward())
						browser.goForward();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			final JFXButton reloadButton = buildButton(Material2OutlinedMZ.REFRESH);
			reloadButton.setTooltip(new javafx.scene.control.Tooltip("Reload (Ctrl+R)"));
			reloadButton.setOnAction(event -> {
				System.out.println("Reload button clicked.");
				CefBrowser browser = this.parent.currentBrowser;
				if (browser == null) {
					return;
				}
				try {
					browser.reload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			// Site identity / security indicator: lock for https, warning for http,
			// gear for internal pages. Behaves like a modern omnibox padlock.
			securityButton = buildButton(Material2OutlinedAL.LOCK);
			securityButton.setTooltip(new javafx.scene.control.Tooltip("Site information"));
			securityButton.setOnAction(event -> {
				CefBrowser browser = this.parent.currentBrowser;
				String url = browser != null ? safeBrowserUrl(browser) : "";
				try {
					securityButton.setTooltip(new javafx.scene.control.Tooltip(siteTooltipFor(url)));
				} catch (Exception ignored) {
				}
			});

			addressField = new TextField(startUrl);
			addressField.setStyle("-fx-background-color: transparent; -fx-padding: 10px;");
			addressField.styleProperty().bind(Bindings.createStringBinding(() -> {
				final Paint color = parent.profileMaterialColorScheme.getOnSurface().get();
				if (color instanceof Color c) {
					return "-fx-background-color: transparent; -fx-text-inner-color: %s; -fx-padding: 6px 10px; -fx-prompt-text-fill: gray;"
							.formatted(parent.colorToHex(c));
				}
				return "-fx-background-color: transparent;";
			}, parent.profileMaterialColorScheme.getOnSurface()));
			addressField.setPromptText("Search or enter address");
			addressField.setOnAction(event -> {
				onAddressEnter();
			});
			addressField.setOnMousePressed(event -> {
				if (!wasFocused) {
					addressField.requestFocus();
					event.consume();
				}
			});

			final JFXButton aiButton = buildButton(Material2OutlinedAL.ASSISTANT);
			aiButton.setTooltip(new javafx.scene.control.Tooltip("AI sidebar (opt-in)"));
			aiButton.setOnAction(event -> {
				System.out.println("AI button clicked.");
				parent.toggleAISidebar();
			});

			final JFXButton moreButton = buildButton(Material2OutlinedMZ.MORE_VERT);
			moreButton.setTooltip(new javafx.scene.control.Tooltip("Browser menu"));
			moreButton.setOnAction(event -> {
				parent.moreSidebar.toggleSidebar();
			});

			// Omnibox: [padlock] [address field] grouped visually.
			final HBox omnibox = new HBox(4);
			omnibox.setAlignment(Pos.CENTER);
			omnibox.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint bg = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(bg, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			omnibox.setStyle("-fx-padding: 2 4 2 4;");
			securityButton.setStyle("-fx-background-color: transparent; -fx-padding: 4;");
			// NOTE: do NOT call addressField.setStyle here: its styleProperty is
			// bound above (text color theming). setStyle on a bound property
			// throws and would abort this whole toolbar build, leaving an empty bar.
			// Address field keeps text color binding; container provides the pill.
			omnibox.getChildren().addAll(securityButton, addressField);
			HBox.setHgrow(addressField, Priority.ALWAYS);
			HBox.setHgrow(omnibox, Priority.ALWAYS);
			omnibox.setMaxWidth(Double.MAX_VALUE);

			root.getChildren().addAll(backButton, forwardButton, reloadButton, omnibox, aiButton, moreButton);

			backButton.prefWidthProperty().bind(backButton.heightProperty());
			forwardButton.prefWidthProperty().bind(forwardButton.heightProperty());
			reloadButton.prefWidthProperty().bind(reloadButton.heightProperty());
			aiButton.prefWidthProperty().bind(aiButton.prefHeightProperty());

			addressField.setMaxWidth(Double.MAX_VALUE);

			final ListView<String> autoSuggestList = new ListView<>();
			autoSuggestList.setFocusTraversable(false);

			final Popup autoSuggestPopup = new Popup();
			autoSuggestPopup.getContent().add(autoSuggestList);
			autoSuggestPopup.setAutoHide(true);

			final Runnable showAutoSuggest = () -> {
				System.out.println("Show auto suggest called.");

				if (!addressField.isFocused() || autoSuggestList.getItems().isEmpty()) {
					autoSuggestPopup.hide();
					return;
				}

				System.out.println("Showing auto suggest...");

				autoSuggestList.getSelectionModel().clearSelection();
				autoSuggestList.getFocusModel().focus(-1);

				Point2D screenPos = null;
				try {
					screenPos = addressField.localToScreen(0, addressField.getHeight());
				} catch (Exception ignored) {
				}
				if (screenPos == null) {
					return;
				}
				System.out.printf("Screen position: %f, %f\n", screenPos.getX(), screenPos.getY());

				autoSuggestPopup.setX(screenPos.getX());
				autoSuggestPopup.setY(screenPos.getY() + 10);
				autoSuggestList.setPrefWidth(addressField.getWidth());
				try {
					autoSuggestList.prefHeightProperty().unbind();
				} catch (Exception ignored) {
				}
				autoSuggestList.prefHeightProperty().bind(
						Bindings.size(autoSuggestList.getItems()).multiply(25));

				if (!autoSuggestPopup.isShowing()) {
					System.out.println("Auto suggest popup is not showing.");
					try {
						autoSuggestPopup.show(addressBarScene.getWindow());
					} catch (Exception ignored) {
					}
				}
			};

			addressField.textProperty().addListener((obs, oldText, newText) -> {
				if (isProgrammaticChange)
					return;

				if (newText.isBlank()) {
					autoSuggestPopup.hide();
				} else {
					System.out.println("Getting autosuggestions...");
					autosuggester.getSuggestion(newText, results -> {
						Platform.runLater(() -> {
							autoSuggestList.getItems().setAll(results);
							System.out.printf("Auto suggest list items: %s\n", autoSuggestList.getItems().toString());
							showAutoSuggest.run();
						});
					});
				}
			});

			addressField.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue) {
					parent.isUiFocused.set(true);

					if (!wasFocused) {
						Platform.runLater(() -> addressField.selectAll());
						wasFocused = true;
					}
				} else {
					System.out.println("Address field lost focus.");
					Platform.runLater(() -> autoSuggestPopup.hide());
					parent.isUiFocused.set(false);
					wasFocused = false;
				}
			});

			final Runnable searchSuggestedResult = () -> {
				final String selected = autoSuggestList.getSelectionModel().getSelectedItem();
				if (selected != null) {
					addressField.setText(selected);
					autoSuggestPopup.hide();
					addressField.fireEvent(new ActionEvent());
				}
			};

			autoSuggestList.setOnMouseClicked(event -> {
				searchSuggestedResult.run();
			});

			autoSuggestList.setOnKeyPressed(event -> {
				final KeyCode eventCode = event.getCode();

				if (eventCode == KeyCode.ENTER) {
					final int focusedIndex = autoSuggestList.getFocusModel().getFocusedIndex();
					System.out.printf("Focused index: %d\n", focusedIndex);
					if (focusedIndex < 0) {
						onAddressEnter();
					} else {
						searchSuggestedResult.run();
					}
					event.consume();
				} else if (eventCode == KeyCode.ESCAPE) {
					autoSuggestPopup.hide();
					event.consume();
				} else if (eventCode == KeyCode.UP || eventCode == KeyCode.DOWN) {
					final int focusIndex = autoSuggestList.getFocusModel().getFocusedIndex();
					final int itemsLength = autoSuggestList.getItems().size();

					int targetIndex = focusIndex;

					if (eventCode == KeyCode.UP) {
						if (focusIndex <= 0) {
							targetIndex = itemsLength - 1;
						} else {
							targetIndex = focusIndex - 1;
						}
					} else if (eventCode == KeyCode.DOWN) {
						if (focusIndex < 0 || focusIndex >= itemsLength - 1) {
							targetIndex = 0;
						} else {
							targetIndex = focusIndex + 1;
						}
					}

					autoSuggestList.getFocusModel().focus(targetIndex);
					autoSuggestList.getSelectionModel().select(targetIndex);
					autoSuggestList.scrollTo(targetIndex);

					event.consume();
				} else {
					System.out.println("Handing event over to address field.");
				}
			});

			addressField.setOnKeyPressed(event -> {
				final KeyCode eventCode = event.getCode();

				if (eventCode == KeyCode.ENTER) {
					final int focusedIndex = autoSuggestList.getFocusModel().getFocusedIndex();
					System.out.printf("Focused index: %d\n", focusedIndex);
					if (focusedIndex < 0) {
						onAddressEnter();
					} else {
						searchSuggestedResult.run();
					}
					event.consume();
				} else if (eventCode == KeyCode.UP || eventCode == KeyCode.DOWN) {
					final int focusIndex = autoSuggestList.getFocusModel().getFocusedIndex();
					final int itemsLength = autoSuggestList.getItems().size();

					int targetIndex = focusIndex;

					if (eventCode == KeyCode.UP) {
						if (focusIndex <= 0) {
							targetIndex = itemsLength - 1;
						} else {
							targetIndex = focusIndex - 1;
						}
					} else if (eventCode == KeyCode.DOWN) {
						if (focusIndex < 0 || focusIndex >= itemsLength - 1) {
							targetIndex = 0;
						} else {
							targetIndex = focusIndex + 1;
						}
					}

					autoSuggestList.getFocusModel().focus(targetIndex);
					autoSuggestList.getSelectionModel().select(targetIndex);
					autoSuggestList.scrollTo(targetIndex);

					event.consume();
				} else {
					System.out.println("Handing event over to address field.");
				}
			});
		});

		this.add(addressBarPanel);
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
		button.setStyle("-fx-padding: 6px;");
		button.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(10), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer()));
		button.setOnMouseEntered(event -> {
			button.setCursor(Cursor.HAND);
		});
		button.setOnMouseExited(event -> {
			button.setCursor(Cursor.DEFAULT);
		});
		return button;
	}

	public void updateUrl(String newUrl) {
		final String safeUrl;
		try {
			safeUrl = this.parent.formatURL(newUrl != null ? newUrl : "", false);
		} catch (Exception e) {
			return;
		}
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> updateUrl(newUrl));
			return;
		}
		if (addressField == null) {
			return;
		}
		isProgrammaticChange = true;
		try {
			addressField.setText(safeUrl);
		} finally {
			isProgrammaticChange = false;
		}
		updateSecurityIndicator(safeUrl);
	}

	private void updateSecurityIndicator(String url) {
		if (!Platform.isFxApplicationThread() || securityButton == null) {
			return;
		}
		try {
			Ikon icon = Material2OutlinedAL.LOCK;
			String tip = siteTooltipFor(url);
			if (url != null) {
				if (url.startsWith("https://")) {
					icon = Material2OutlinedAL.LOCK;
				} else if (url.startsWith("http://")) {
					icon = Material2OutlinedAL.LOCK_OPEN;
				} else if (url.startsWith("evilbrowse://") || url.startsWith("turtlebrowse://")
						|| url.startsWith("about:")) {
					icon = Material2OutlinedAL.INFO;
				} else if (url.startsWith("file://")) {
					icon = Material2OutlinedAL.FOLDER;
				}
			}
			final FontIcon fi = new FontIcon(icon);
			try {
				fi.setIconColor(parent.profileMaterialColorScheme.getOnSurface().get());
			} catch (Exception ignored) {
			}
			securityButton.setGraphic(fi);
			securityButton.setTooltip(new javafx.scene.control.Tooltip(tip));
		} catch (Exception ignored) {
		}
	}

	private String safeBrowserUrl(CefBrowser browser) {
		try {
			String u = browser.getURL();
			return u != null ? u : "";
		} catch (Exception e) {
			return "";
		}
	}

	private String siteTooltipFor(String url) {
		if (url == null || url.isBlank()) {
			return "Site information";
		}
		if (url.startsWith("https://")) {
			return "Secure connection (HTTPS)\n" + url;
		}
		if (url.startsWith("http://")) {
			return "Not secure (HTTP)\n" + url;
		}
		if (url.startsWith("evilbrowse://") || url.startsWith("turtlebrowse://")) {
			return "EvilBrowse internal page\n" + url;
		}
		if (url.startsWith("file://")) {
			return "Local file\n" + url;
		}
		return url;
	}

	public void focusAddressField() {
		focusAddressField(false);
	}

	public void focusAddressField(boolean forceSelect) {
		System.out.println("Focus address field called.");

		SwingUtilities.invokeLater(() -> {
			this.parent.requestFocus();

			addressBarPanel.requestFocusInWindow();

			Platform.runLater(() -> {
				this.parent.isUiFocused.set(true);

				if (!addressField.isFocused()) {
					addressField.requestFocus();
				} else if (forceSelect) {
					addressField.selectAll();
				}

				System.out.println("Address field focused and selected.");
			});
		});
	}

	private void onAddressEnter() {
		CefBrowser browser = this.parent.currentBrowser;

		String enteredUrl;
		try {
			enteredUrl = this.parent.formatURL(addressField.getText(), false);
		} catch (Exception e) {
			return;
		}

		System.out.print("Entered URL:");
		System.out.println(enteredUrl);

		if (browser != null) {
			try {
				browser.loadURL(enteredUrl);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			try {
				this.parent.isUiFocused.set(false);
			} catch (Exception ignored) {
			}
			SwingUtilities.invokeLater(() -> {
				try {
					browser.setFocus(true);
				} catch (Exception ignored) {
				}
			});
		} else {
			System.out.println("Browser is null.");
		}
	}
}
