package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;

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
import javafx.scene.paint.Paint;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class FindSidebar extends ToolSidebar {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final MainWindow parent;
	private String textToFind;
	private boolean matchCase;

	public FindSidebar(MainWindow parent) {
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

			actionsBar.getChildren().addAll(closeButton);

			final VBox contentBox = new VBox();
			contentBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");

			final Label titleLabel = new Label("Find text");
			titleLabel.setStyle("-fx-font-weight: bold;");

			final Label findLabel = new Label("Find");

			final TextField findField = new TextField();
			findField.setStyle("-fx-padding: 10px;");
			findField.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			findField.textProperty().addListener((obs, oldText, newText) -> {
				textToFind = newText;
				findText();
			});

			final JFXCheckBox matchCaseCheckBox = new JFXCheckBox("Match case");
			matchCaseCheckBox.setCheckedColor(parent.profileMaterialColorScheme.getPrimary().get());
			matchCaseCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
				matchCase = newVal;
				findText();
			});

			final HBox nextPreviousBox = new HBox();
			nextPreviousBox.setStyle("-fx-spacing: 10px;");

			final JFXButton previousButton = new JFXButton("Previous");
			previousButton.setGraphic(new FontIcon(Material2OutlinedAL.ARROW_BACK));
			previousButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			previousButton.setStyle("-fx-padding: 10px;");
			previousButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			previousButton.setOnMouseEntered(event -> {
				previousButton.setCursor(Cursor.HAND);
			});
			previousButton.setOnMouseExited(event -> {
				previousButton.setCursor(Cursor.DEFAULT);
			});
			previousButton.setOnAction(event -> {
				findPrevious();
			});

			final JFXButton nextButton = new JFXButton("Next");
			nextButton.setGraphic(new FontIcon(Material2OutlinedAL.ARROW_FORWARD));
			nextButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			nextButton.setStyle("-fx-padding: 10px;");
			nextButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			nextButton.setOnMouseEntered(event -> {
				nextButton.setCursor(Cursor.HAND);
			});
			nextButton.setOnMouseExited(event -> {
				nextButton.setCursor(Cursor.DEFAULT);
			});
			nextButton.setOnAction(event -> {
				findNext();
			});

			nextPreviousBox.getChildren().addAll(previousButton, nextButton);

			contentBox.getChildren().addAll(titleLabel, findLabel, findField, matchCaseCheckBox, nextPreviousBox);

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
		System.out.println("Opening yt-dlp sidebar.");
		preferredDim.width = 500;
		isOpen = true;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void closeSidebar() {
		System.out.println("Closing yt-dlp sidebar.");
		preferredDim.width = 0;
		isOpen = false;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	private void findText() {
		parent.currentBrowser.find(textToFind, true, matchCase, false);
	}

	private void findPrevious() {
		parent.currentBrowser.find(textToFind, false, matchCase, true);
	}

	private void findNext() {
		parent.currentBrowser.find(textToFind, true, matchCase, true);
	}
}
