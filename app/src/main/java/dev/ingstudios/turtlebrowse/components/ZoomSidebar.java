package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class ZoomSidebar extends ToolSidebar {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final MainWindow parent;
	private double zoom = 100;
	TextField zoomField;

	public ZoomSidebar(MainWindow parent) {
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

			final VBox contentBox = new VBox();
			contentBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");

			final Label titleLabel = new Label("Page zoom");
			titleLabel.textFillProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint fillColor = parent.profileMaterialColorScheme.getOnSurface().get();
				return fillColor;
			}, parent.profileMaterialColorScheme.getOnSurface()));
			titleLabel.setStyle("-fx-font-weight: bold;");

			final Label zoomLabel = new Label("Zoom level");
			zoomLabel.textFillProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint fillColor = parent.profileMaterialColorScheme.getOnSurface().get();
				return fillColor;
			}, parent.profileMaterialColorScheme.getOnSurface()));

			zoomField = new TextField();
			zoomField.styleProperty().bind(Bindings.createStringBinding(() -> {
				final Paint color = parent.profileMaterialColorScheme.getOnPrimaryContainer().get();
				if (color instanceof Color c) {
					return "-fx-text-inner-color: %s; -fx-padding: 10px;".formatted(parent.colorToHex(c));
				}
				return "";
			}, parent.profileMaterialColorScheme.getOnPrimaryContainer()));
			zoomField.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getPrimaryContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getPrimaryContainer()));
			zoomField.textProperty().addListener((obs, oldText, newText) -> {
				if (newText == oldText)
					return;

				if (newText.isBlank())
					newText = "100";

				zoom = Double.parseDouble(newText);
				setZoom();
			});

			final HBox zoomInOutBox = new HBox();
			zoomInOutBox.setStyle("-fx-spacing: 10px;");

			final JFXButton decreaseZoom = buildButton(Material2OutlinedMZ.ZOOM_OUT);
			decreaseZoom.setOnAction(event -> {
				zoom -= 1;
				zoomField.setText(String.valueOf(zoom));
				setZoom();
			});

			final JFXButton increaseZoom = buildButton(Material2OutlinedMZ.ZOOM_IN);
			increaseZoom.setOnAction(event -> {
				zoom += 1;
				zoomField.setText(String.valueOf(zoom));
				setZoom();
			});

			zoomInOutBox.getChildren().addAll(decreaseZoom, increaseZoom);

			contentBox.getChildren().addAll(titleLabel, zoomLabel, zoomField, zoomInOutBox);

			root.getChildren().addAll(actionsBar, contentBox);
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
		System.out.println("Opening zoom sidebar.");
		preferredDim.width = 500;
		isOpen = true;
		zoom = getZoom();
		zoomField.setText(String.valueOf(zoom));
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

	private double getZoom() {
		final double zoomLevel = parent.currentBrowser.getZoomLevel();
		return (zoomLevel == 0 ? 1 : zoomLevel) * 100;
	}

	private void setZoom() {
		parent.currentBrowser.setZoomLevel(zoom / 100);
	}
}
