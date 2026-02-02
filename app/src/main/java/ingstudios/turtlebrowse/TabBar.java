package ingstudios.turtlebrowse;

import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class TabBar extends JPanel {
    public TabBar() {
        this.setLayout(new java.awt.BorderLayout());

        JFXPanel tabPanel = new JFXPanel();
        tabPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

        HBox root = new HBox();
        root.setAlignment(Pos.CENTER_LEFT);
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        Button tabIsland = new Button("Sample Tab");

        root.getChildren().addAll(tabIsland);

        Scene tabBarScene = new Scene(root);
        tabPanel.setScene(tabBarScene);
        Platform.runLater(() -> {
            root.prefWidthProperty().bind(tabBarScene.widthProperty());
            root.prefHeightProperty().bind(tabBarScene.heightProperty());
        });

        this.add(tabPanel);
    }
}
