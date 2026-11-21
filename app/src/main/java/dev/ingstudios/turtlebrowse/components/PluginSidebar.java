package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.jfoenix.controls.JFXButton;

import dev.ingstudios.turtlebrowse.lua.elements.LuaTextElement;
import dev.ingstudios.turtlebrowse.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class PluginSidebar extends ToolSidebar {
	public static PluginSidebar instance;
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final MainWindow parent;
	private VBox canvas;

	public PluginSidebar(MainWindow parent) {
		instance = this;

		this.parent = parent;

		this.setLayout(new java.awt.BorderLayout());
		this.setPreferredSize(preferredDim);

		final JFXPanel sidebarPanel = new JFXPanel();
		sidebarPanel.setFocusable(true);
		sidebarPanel.setPreferredSize(new java.awt.Dimension(preferredDim.width, preferredDim.height));

		Platform.runLater(() -> {
			final VBox root = new VBox();
			root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.profileMaterialColorScheme.getSurface()));

			final Scene sidebarScene = new Scene(root);
			sidebarScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			sidebarPanel.setScene(sidebarScene);

			root.prefWidthProperty().bind(sidebarScene.widthProperty());

			final HBox actionsBar = new HBox();
			actionsBar.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
			actionsBar.setAlignment(Pos.CENTER_RIGHT);

			final JFXButton closeButton = buildButton(Material2OutlinedAL.CLOSE);
			closeButton.setOnAction(event -> {
				closeSidebar();
			});

			actionsBar.getChildren().addAll(closeButton);

			canvas = new VBox();

			root.getChildren().addAll(actionsBar, canvas);

			parent.luaPluginRunner.run("""
						plugin.canvas.createText('hi'):setColor('#11bedd')
					""");
		});

		this.add(sidebarPanel, BorderLayout.CENTER);
	}

	private JFXButton buildButton(Ikon iconName) {
		final JFXButton button = new JFXButton("");
		final FontIcon icon = new FontIcon(iconName);
		icon.setIconColor(parent.profileMaterialColorScheme.getOnPrimaryContainer().get());
		parent.profileMaterialColorScheme.getOnPrimaryContainer().addListener((observable, oldPaint, newPaint) -> {
			icon.setIconColor(newPaint);
		});
		button.setGraphic(icon);
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setStyle("-fx-padding: 10px;");
		button.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getPrimaryContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, parent.profileMaterialColorScheme.getPrimaryContainer()));
		button.setOnMouseEntered(event -> {
			button.setCursor(Cursor.HAND);
		});
		button.setOnMouseExited(event -> {
			button.setCursor(Cursor.DEFAULT);
		});
		return button;
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
		preferredDim.width = 500;
		isOpen = true;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void closeSidebar() {
		preferredDim.width = 0;
		isOpen = false;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	public VBox getCanvas() {
		return canvas;
	}

	public HBox createRow() {
		return new HBox();
	}

	public VBox createColumn() {
		return new VBox();
	}

	public LuaTextElement createText(String text) {
		System.out.println("Creating text: " + text);
		final Label label = new Label(text);
		canvas.getChildren().add(label);
		return new LuaTextElement(label);
	}
}
