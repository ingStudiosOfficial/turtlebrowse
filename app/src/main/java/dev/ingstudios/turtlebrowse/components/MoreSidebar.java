package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class MoreSidebar extends JPanel {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final MainWindow parent;

	public MoreSidebar(MainWindow parent) {
		this.parent = parent;

		this.setLayout(new java.awt.BorderLayout());
		this.setPreferredSize(preferredDim);

		final JFXPanel morePanel = new JFXPanel();
		morePanel.setFocusable(true);
		morePanel.setPreferredSize(preferredDim);

		Platform.runLater(() -> {
			final VBox actionsBar = new VBox();
			actionsBar.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
			actionsBar.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.profileMaterialColorScheme.getSurface()));
			actionsBar.setAlignment(Pos.TOP_CENTER);

			final Scene actionsBarScene = new Scene(actionsBar);
			actionsBarScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			morePanel.setScene(actionsBarScene);
			actionsBar.prefWidthProperty().bind(actionsBarScene.widthProperty());
			actionsBar.prefHeightProperty().bind(actionsBarScene.heightProperty());

			final JFXButton settingsButton = createMenuItem(new FontIcon(Material2OutlinedMZ.SETTINGS));
			settingsButton.setOnAction(event -> {
				System.out.println("Settings button clicked.");
				SwingUtilities.invokeLater(() -> {
					parent.createTab("turtlebrowse://settings");
				});
			});

			final JFXButton profileButton = createMenuItem(new FontIcon(Material2OutlinedAL.ACCOUNT_CIRCLE));
			profileButton.setOnAction(event -> {
				System.out.println("Profile menu item clicked.");
				Main.createProfilePickerWindow();
			});

			final JFXButton ytdlpButton = createMenuItem(new FontIcon(Material2OutlinedAL.CLOUD_DOWNLOAD));
			ytdlpButton.setOnAction(event -> {
				System.out.println("yt-dlp download button clicked.");
				if (parent.getSidebar() != parent.ytdlpSidebar) {
					parent.setSidebar(parent.ytdlpSidebar);
					parent.ytdlpSidebar.openSidebar();
				} else {
					parent.ytdlpSidebar.toggleSidebar();
				}
			});

			final JFXButton historyButton = createMenuItem(new FontIcon(Material2OutlinedAL.HISTORY));
			historyButton.setOnAction(event -> {
				System.out.println("History button clicked.");
				parent.openHistory();
			});

			final JFXButton findButton = createMenuItem(new FontIcon(Material2OutlinedAL.FIND_IN_PAGE));
			findButton.setOnAction(event -> {
				System.out.println("Find button clicked.");
				if (parent.getSidebar() != parent.findSidebar) {
					parent.setSidebar(parent.findSidebar);
					parent.findSidebar.openSidebar();
				} else {
					parent.findSidebar.toggleSidebar();
				}
			});

			final JFXButton closeButton = createMenuItem(new FontIcon(Material2OutlinedAL.CLOSE));
			closeButton.setOnAction(event -> {
				closeSidebar();
			});

			actionsBar.getChildren().addAll(settingsButton, profileButton, ytdlpButton, historyButton, findButton,
					closeButton);
		});

		this.add(morePanel, BorderLayout.CENTER);
	}

	public void toggleSidebar() {
		if (isOpen) {
			closeSidebar();
		} else {
			openSidebar();
		}
	}

	public void openSidebar() {
		System.out.println("Opening sidebar...");
		preferredDim.width = 50;
		isOpen = true;
		this.revalidate();
	}

	public void closeSidebar() {
		preferredDim.width = 0;
		isOpen = false;
		this.revalidate();
	}

	private JFXButton createMenuItem(FontIcon icon) {
		final JFXButton item = new JFXButton("");
		item.setGraphic(icon);
		item.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		item.setStyle("-fx-padding: 10px;");
		item.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer()));
		item.setOnMouseEntered(event -> {
			item.setCursor(Cursor.HAND);
		});
		item.setOnMouseExited(event -> {
			item.setCursor(Cursor.DEFAULT);
		});
		return item;
	}
}
