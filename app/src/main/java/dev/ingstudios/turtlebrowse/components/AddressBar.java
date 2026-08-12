package dev.ingstudios.turtlebrowse.components;

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
import javafx.scene.paint.Paint;
import javafx.stage.Popup;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.*;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import com.jfoenix.controls.JFXButton;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.search.SearchAutosuggest;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class AddressBar extends JPanel {
	private TextField addressField;
	private final MainWindow parent;
	private JFXPanel addressBarPanel;
	private boolean wasFocused = false;
	private final SearchAutosuggest autosuggester;

	public AddressBar(CefClient client, MainWindow parent, String startUrl) {
		this.parent = parent;

		autosuggester = new SearchAutosuggest(this.parent.userAgent);

		this.setLayout(new java.awt.BorderLayout());

		addressBarPanel = new JFXPanel();
		addressBarPanel.setFocusable(true);
		addressBarPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

		Platform.runLater(() -> {
			final HBox root = new HBox();
			root.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			root.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
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

			final JFXButton backButton = new JFXButton("<");
			backButton.setGraphic(new FontIcon(Material2OutlinedAL.ARROW_BACK));
			backButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			backButton.setStyle("-fx-padding: 10px;");
			backButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			backButton.setOnMouseEntered(event -> {
				backButton.setCursor(Cursor.HAND);
			});
			backButton.setOnMouseDragExited(event -> {
				backButton.setCursor(Cursor.DEFAULT);
			});
			backButton.setOnAction(event -> {
				System.out.println("Back button clicked.");
				CefBrowser browser = this.parent.currentBrowser;
				if (browser.canGoBack())
					browser.goBack();
			});

			final JFXButton forwardButton = new JFXButton(">");
			forwardButton.setGraphic(new FontIcon(Material2OutlinedAL.ARROW_FORWARD));
			forwardButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			forwardButton.setStyle("-fx-padding: 10px;");
			forwardButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			forwardButton.setOnMouseEntered(event -> {
				forwardButton.setCursor(Cursor.HAND);
			});
			forwardButton.setOnMouseDragExited(event -> {
				forwardButton.setCursor(Cursor.DEFAULT);
			});
			forwardButton.setOnAction(event -> {
				System.out.println("Forward button clicked.");
				CefBrowser browser = this.parent.currentBrowser;
				if (browser.canGoForward())
					browser.goForward();
			});

			final JFXButton reloadButton = new JFXButton("↻");
			reloadButton.setGraphic(new FontIcon(Material2OutlinedMZ.REFRESH));
			reloadButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			reloadButton.setStyle("-fx-padding: 10px;");
			reloadButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			reloadButton.setOnMouseEntered(event -> {
				reloadButton.setCursor(Cursor.HAND);
			});
			reloadButton.setOnMouseDragExited(event -> {
				reloadButton.setCursor(Cursor.DEFAULT);
			});
			reloadButton.setOnAction(event -> {
				System.out.println("Reload button clicked.");
				CefBrowser browser = this.parent.currentBrowser;
				browser.reload();
			});

			addressField = new TextField(startUrl);
			addressField.setStyle("-fx-padding: 10px;");
			addressField.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			addressField.setOnAction(event -> {
				onAddressEnter();
			});

			addressField.setOnMousePressed(event -> {
				if (!wasFocused) {
					addressField.requestFocus();
					event.consume();
				}
			});

			final JFXButton aiButton = new JFXButton("✨");
			aiButton.setGraphic(new FontIcon(Material2OutlinedAL.ASSISTANT));
			aiButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			aiButton.setStyle("-fx-padding: 10px;");
			aiButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			aiButton.setOnMouseEntered(event -> {
				aiButton.setCursor(Cursor.HAND);
			});
			aiButton.setOnMouseDragExited(event -> {
				aiButton.setCursor(Cursor.DEFAULT);
			});
			aiButton.setOnAction(event -> {
				System.out.println("AI button clicked.");
				parent.aiSidebar.toggleSidebar();
			});

			final JFXButton profileButton = new JFXButton("👱");
			profileButton.setGraphic(new FontIcon(Material2OutlinedAL.ACCOUNT_CIRCLE));
			profileButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			profileButton.setStyle("-fx-padding: 10px;");
			profileButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			profileButton.setOnMouseEntered(event -> {
				profileButton.setCursor(Cursor.HAND);
			});
			profileButton.setOnMouseDragExited(event -> {
				profileButton.setCursor(Cursor.DEFAULT);
			});
			profileButton.setOnAction(event -> {
				System.out.println("Profile button clicked.");
				Main.createProfilePickerWindow();
			});

			root.getChildren().addAll(backButton, forwardButton, reloadButton, addressField, aiButton, profileButton);

			backButton.prefWidthProperty().bind(backButton.heightProperty());
			forwardButton.prefWidthProperty().bind(forwardButton.heightProperty());
			reloadButton.prefWidthProperty().bind(reloadButton.heightProperty());
			aiButton.prefWidthProperty().bind(aiButton.prefHeightProperty());
			profileButton.prefWidthProperty().bind(profileButton.prefHeightProperty());

			HBox.setHgrow(addressField, Priority.ALWAYS);
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

				final Point2D screenPos = addressField.localToScreen(0, addressField.getHeight());
				System.out.printf("Screen position: %f, %f\n", screenPos.getX(), screenPos.getY());

				if (screenPos != null) {
					autoSuggestPopup.setX(screenPos.getX());
					autoSuggestPopup.setY(screenPos.getY() + 10);
					autoSuggestList.setPrefWidth(addressField.getWidth());
					autoSuggestList.prefHeightProperty().bind(
							Bindings.size(autoSuggestList.getItems()).multiply(25));

					if (!autoSuggestPopup.isShowing()) {
						System.out.println("Auto suggest popup is not showing.");
						autoSuggestPopup.show(addressBarScene.getWindow());
					}
				}
			};

			addressField.textProperty().addListener((obs, oldText, newText) -> {
				if (newText.isEmpty()) {
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
					CefBrowser browser = this.parent.currentBrowser;
					if (browser != null) {
						browser.setFocus(false);
					}

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

			root.setOnMouseClicked(event -> {
				addressField.requestFocus();
			});
		});

		this.add(addressBarPanel);
	}

	public void updateUrl(String newUrl) {
		addressField.setText(this.parent.formatURL(newUrl, false));
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

		String enteredUrl = this.parent.formatURL(addressField.getText(), false);

		System.out.print("Entered URL:");
		System.out.println(enteredUrl);

		if (browser != null)
			browser.loadURL(enteredUrl);
		else
			System.out.println("Browser is null.");
	}
}
