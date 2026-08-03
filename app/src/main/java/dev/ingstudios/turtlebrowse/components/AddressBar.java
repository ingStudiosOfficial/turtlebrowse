package dev.ingstudios.turtlebrowse.components;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.*;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import com.jfoenix.controls.JFXButton;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class AddressBar extends JPanel {
	private TextField addressField;
	private MainWindow parent;
	private boolean addressFieldFocused = false;
	private JFXPanel addressBarPanel;

	public AddressBar(CefClient client, MainWindow parent, String startUrl) {
		this.parent = parent;

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
				CefBrowser browser = this.parent.currentBrowser;

				String enteredUrl = this.parent.formatURL(addressField.getText(), false);

				System.out.print("Entered URL:");
				System.out.println(enteredUrl);

				if (browser != null)
					browser.loadURL(enteredUrl);
				else
					System.out.println("Browser is null.");
			});

			addressField.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue) {
					System.out.println("Address field has lost focus.");
					parent.isUiFocused.set(false);
					addressFieldFocused = false;
					return;
				} else {
					addressFieldFocused = true;
				}
			});

			addressField.setOnMouseClicked(event -> {
				focusAddressField();
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
		System.out.println("Focus address field called.");

		SwingUtilities.invokeLater(() -> {
			this.parent.requestFocus(); // IMPORTANT DO NOT REMOVE THIS ENSURES THAT THE MAIN PARENT GAINS FOCUS FIRST

			addressBarPanel.requestFocusInWindow();

			Platform.runLater(() -> {
				this.parent.isUiFocused.set(true);

				CefBrowser browser = this.parent.currentBrowser;
				if (browser != null) {
					browser.setFocus(false);
				}

				Platform.runLater(() -> {
					addressField.requestFocus();
					if (addressFieldFocused == false)
						addressField.selectAll();
					System.out.println("Address field focused and selected.");
				});

				addressFieldFocused = true;
			});
		});
	}
}