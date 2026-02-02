package ingstudios.turtlebrowse;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javax.swing.JPanel;

import org.cef.CefClient;
import org.cef.browser.*;

public class AddressBar extends JPanel {
    TextField addressField;

    public AddressBar(CefClient client, CefBrowser browser, String startUrl, BrowserState browserFocused) {
        this.setLayout(new java.awt.BorderLayout());

        JFXPanel addressBarPanel = new JFXPanel();
        addressBarPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        Platform.runLater(() -> {
            HBox root = new HBox();
            root.setAlignment(Pos.CENTER);
            root.setSpacing(10);
            root.setPadding(new Insets(10));

            Button backButton = new Button("<");
            backButton.setOnAction(event -> {
                System.out.println("Back button clicked.");
                if (browser.canGoBack()) browser.goBack();
            });

            Button forwardButton = new Button(">");
            forwardButton.setOnAction(event -> {
                System.out.println("Forward button clicked.");
                if (browser.canGoForward()) browser.goForward();
            });

            Button reloadButton = new Button("↻");
            reloadButton.setOnAction(event -> {
                System.out.println("Reload button clicked.");
                browser.reload();
            });

            this.addressField = new TextField(startUrl);
            this.addressField.setOnAction(event -> {
                String enteredUrl = this.addressField.getText();

                System.out.print("Entered URL:");
                System.out.println(enteredUrl);

                browser.loadURL(enteredUrl);
            });

            this.addressField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    if (!browserFocused.getBrowserFocus()) return;
                    browserFocused.setBrowserFocus(false);
                    java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                    Platform.runLater(() -> this.addressField.requestFocus());
                }
            });

            root.getChildren().addAll(backButton, forwardButton, reloadButton, this.addressField);

            backButton.prefWidthProperty().bind(backButton.heightProperty());
            forwardButton.prefWidthProperty().bind(forwardButton.heightProperty());
            reloadButton.prefWidthProperty().bind(reloadButton.heightProperty());

            HBox.setHgrow(this.addressField, Priority.ALWAYS);
            this.addressField.setMaxWidth(Double.MAX_VALUE);

            root.setOnMouseClicked(event -> {
                this.addressField.requestFocus();
            });

            Scene addressBarScene = new Scene(root);
            addressBarPanel.setScene(addressBarScene);
            root.prefWidthProperty().bind(addressBarScene.widthProperty());
            root.prefHeightProperty().bind(addressBarScene.heightProperty());
        });

        this.add(addressBarPanel);
    }

    public void updateUrl(String newUrl) {
        this.addressField.setText(newUrl);
    }
}
