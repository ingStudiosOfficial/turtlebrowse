package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;

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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import dev.ingstudios.turtlebrowse.managers.UpdateManager;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class UpdateSidebar extends ToolSidebar {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final MainWindow parent;
	private UpdateManager updateManager;
	private Label updateLabel;
	private final String downloadUrl = "https://turtlebrowse.ingstudios.dev/download";
	private JFXButton updateButton;

	public UpdateSidebar(MainWindow parent) {
		this.parent = parent;

		Thread.ofVirtual().start(() -> {
			updateManager = UpdateManager.getInstance(parent);
		});

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

			final JFXButton refreshButton = new JFXButton("↻");
			refreshButton.setGraphic(new FontIcon(Material2OutlinedMZ.REFRESH));
			refreshButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			refreshButton.setStyle("-fx-padding: 10px;");
			refreshButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			refreshButton.setOnMouseEntered(event -> {
				refreshButton.setCursor(Cursor.HAND);
			});
			refreshButton.setOnMouseExited(event -> {
				refreshButton.setCursor(Cursor.DEFAULT);
			});
			refreshButton.setOnAction(event -> {
				refresh();
			});

			final JFXButton closeButton = new JFXButton("X");
			closeButton.setGraphic(new FontIcon(Material2OutlinedAL.CLOSE));
			closeButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			closeButton.setStyle("-fx-padding: 10px;");
			closeButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			closeButton.setOnMouseEntered(event -> {
				closeButton.setCursor(Cursor.HAND);
			});
			closeButton.setOnMouseExited(event -> {
				closeButton.setCursor(Cursor.DEFAULT);
			});
			closeButton.setOnAction(event -> {
				closeSidebar();
			});

			actionsBar.getChildren().addAll(refreshButton, closeButton);

			final VBox contentBox = new VBox();
			contentBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");

			final Label titleLabel = new Label("Update manager");
			titleLabel.setStyle("-fx-font-weight: bold;");

			updateLabel = new Label("Checking for updates...");

			updateButton = new JFXButton("Download update");
			updateButton.setGraphic(new FontIcon(Material2OutlinedAL.CLOUD_DOWNLOAD));
			updateButton.setContentDisplay(ContentDisplay.LEFT);
			updateButton.setStyle("-fx-padding: 10px;");
			updateButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getPrimaryContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getPrimaryContainer()));
			updateButton.textFillProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint fillColor = parent.profileMaterialColorScheme.getOnPrimaryContainer().get();
				return fillColor;
			}, parent.profileMaterialColorScheme.getOnPrimaryContainer()));
			updateButton.setOnMouseEntered(event -> {
				updateButton.setCursor(Cursor.HAND);
			});
			updateButton.setOnMouseExited(event -> {
				updateButton.setCursor(Cursor.DEFAULT);
			});
			updateButton.setOnAction(event -> {
				parent.createTab(downloadUrl);
			});
			updateButton.setVisible(false);

			contentBox.getChildren().addAll(titleLabel, updateLabel, updateButton);

			root.getChildren().addAll(actionsBar, contentBox);
		});

		this.add(sidebarPanel, BorderLayout.CENTER);
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
		System.out.println("Opening update sidebar.");
		preferredDim.width = 500;
		isOpen = true;
		final boolean shouldUpdate = updateManager.shouldUpdate();
		updateLabels(shouldUpdate);
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void closeSidebar() {
		System.out.println("Closing update sidebar.");
		preferredDim.width = 0;
		isOpen = false;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	private void refresh() {
		updateLabel.setText("Checking for updates...");
		Thread.ofVirtual().start(() -> {
			final boolean shouldUpdate = updateManager.refreshShouldUpdate();
			Platform.runLater(() -> {
				updateLabels(shouldUpdate);
			});
		});
	}

	private void updateLabels(boolean shouldUpdate) {
		final String currentVersion = updateManager.currentVersion;
		final String latestVersion = updateManager.latestVersion;

		if (shouldUpdate) {
			updateLabel.setText(
					"Browser update recommended (Update from %s to %s)".formatted(currentVersion, latestVersion));
			updateButton.setVisible(true);
		} else {
			updateLabel.setText("Browser up to date (Turtlebrowse %s)".formatted(currentVersion));
			updateButton.setVisible(false);
		}
	}
}
