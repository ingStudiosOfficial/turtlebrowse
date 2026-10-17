package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
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
import javafx.stage.FileChooser;
import dev.ingstudios.turtlebrowse.userscript.UserscriptParser;
import dev.ingstudios.turtlebrowse.userscript.UserscriptParser.UserscriptMetadata;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class UserscriptsSidebar extends ToolSidebar {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final MainWindow parent;

	public UserscriptsSidebar(MainWindow parent) {
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

			final Label titleLabel = new Label("Userscripts");
			titleLabel.textFillProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint fillColor = parent.profileMaterialColorScheme.getOnSurface().get();
				return fillColor;
			}, parent.profileMaterialColorScheme.getOnSurface()));
			titleLabel.setStyle("-fx-font-weight: bold;");

			final VBox userscriptInfo = new VBox();

			final JFXButton addButton = new JFXButton("Add userscript");
			addButton.setGraphic(new FontIcon(Material2OutlinedAL.ADD));
			addButton.setOnAction(event -> {
				final FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Choose userscript");

				final File selectedFile = fileChooser.showOpenDialog(null);
				if (selectedFile != null) {
					try {
						final String userscriptContent = Files.readString(Path.of(selectedFile.getAbsolutePath()));
						final UserscriptMetadata metadata = UserscriptParser.parse(userscriptContent);
						addUserscriptDetails(metadata, userscriptInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			contentBox.getChildren().addAll(titleLabel, addButton, userscriptInfo);

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

	private void addUserscriptDetails(UserscriptMetadata metadata, VBox metadataBox) {
		System.out.println(metadata.meta().toString());
		final Label userscriptName = buildMetadataItem(metadata, "name");
		final Label namespace = buildMetadataItem(metadata, "namespace");
		final Label version = buildMetadataItem(metadata, "version");
		final Label description = buildMetadataItem(metadata, "description");
		final Label author = buildMetadataItem(metadata, "author");
		metadataBox.getChildren().addAll(userscriptName, namespace, version,
				description, author);
	}

	private Label buildMetadataItem(UserscriptMetadata metadata, String key) {
		return new Label(key + ": " + metadata.meta().get(key).getAsJsonArray().get(0).getAsString());
	}
}
