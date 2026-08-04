package dev.ingstudios.turtlebrowse.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.jfoenix.controls.JFXButton;

import dev.ingstudios.turtlebrowse.windows.MainWindow;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

public class TabBar extends JPanel {
	private final Map<CefBrowser, HBox> tabMap = new HashMap<>();
	private HBox root;
	private MainWindow parent;

	public TabBar(CefClient client, ArrayList<CefBrowser> tabs, MainWindow parent) {
		this.parent = parent;

		this.setLayout(new java.awt.BorderLayout());

		final JFXPanel tabPanel = new JFXPanel();
		tabPanel.setPreferredSize(new java.awt.Dimension(1200, 50));

		Platform.runLater(() -> {
			root = new HBox();
			root.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			root.setFillHeight(true);
			root.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
			root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.profileMaterialColorScheme.getSurface()));
			root.setAlignment(Pos.CENTER_LEFT);

			final Scene tabBarScene = new Scene(root);
			tabBarScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			tabPanel.setScene(tabBarScene);
			root.prefWidthProperty().bind(tabBarScene.widthProperty());
			root.prefHeightProperty().bind(tabBarScene.heightProperty());

			final JFXButton createTabButton = new JFXButton("+");
			createTabButton.setGraphic(new FontIcon(Material2OutlinedAL.ADD));
			createTabButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			createTabButton.setStyle("-fx-padding: 10px;");
			createTabButton.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
				return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer()));
			createTabButton.setMaxHeight(Double.MAX_VALUE);
			createTabButton.setOnMouseEntered(event -> {
				createTabButton.setCursor(Cursor.HAND);
			});
			createTabButton.setOnMouseDragExited(event -> {
				createTabButton.setCursor(Cursor.DEFAULT);
			});
			createTabButton.setOnAction(event -> {
				this.parent.createTab(this.parent.START_URL);
			});
			root.getChildren().add(createTabButton);

			for (final CefBrowser browser : tabs) {
				addTabToUI(browser);
			}
		});

		this.add(tabPanel);
	}

	public void addTabToUI(CefBrowser browser) {
		final Label tabTitle = new Label("Loading...");

		final JFXButton closeButton = new JFXButton("X");
		closeButton.setGraphic(new FontIcon(Material2OutlinedAL.CLOSE));
		closeButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		closeButton.setStyle("-fx-background-color: transparent;");

		final HBox tabBox = new HBox(10);
		tabBox.setStyle("-fx-padding: 10px; -fx-pref-width: 150px;");
		tabBox.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer()));
		tabBox.setMaxHeight(Double.MAX_VALUE);
		final Region tabSpacer = new Region();
		tabBox.setAlignment(Pos.CENTER);
		HBox.setHgrow(tabSpacer, Priority.ALWAYS);
		tabBox.setOnMouseEntered(event -> {
			tabBox.setCursor(Cursor.HAND);
		});
		tabBox.setOnMouseDragExited(event -> {
			tabBox.setCursor(Cursor.DEFAULT);
		});
		tabBox.setOnMouseClicked(event -> {
			event.consume();

			if (event.getButton() == MouseButton.PRIMARY)
				this.parent.showTab(browser);
			else if (event.getButton() == MouseButton.MIDDLE)
				Platform.runLater(() -> {
					event.consume();
					closeTab(tabBox, browser);
				});
		});

		tabBox.setTranslateY(100);
		final TranslateTransition boxTransition = new TranslateTransition(Duration.seconds(0.3), tabBox);
		boxTransition.setFromY(100);
		boxTransition.setToY(0);

		closeButton.prefHeightProperty().bind(tabBox.heightProperty().multiply(0.8));
		closeButton.setOnMouseEntered(event -> {
			closeButton.setCursor(Cursor.HAND);
		});
		closeButton.setOnMouseDragExited(event -> {
			closeButton.setCursor(Cursor.DEFAULT);
		});
		closeButton.setOnAction(event -> {
			Platform.runLater(() -> {
				event.consume();
				closeTab(tabBox, browser);
			});
		});

		tabBox.getChildren().addAll(tabTitle, tabSpacer, closeButton);

		tabMap.put(browser, tabBox);

		root.getChildren().add(Math.max(0, root.getChildren().size() - 1), tabBox);

		boxTransition.play();
	}

	private void closeTab(HBox tabBox, CefBrowser browser) {
		final TranslateTransition boxTransition = new TranslateTransition(Duration.seconds(0.3), tabBox);
		boxTransition.setFromY(0);
		boxTransition.setToY(100);
		boxTransition.setOnFinished(e -> {
			root.getChildren().remove(tabBox);
		});
		boxTransition.play();

		SwingUtilities.invokeLater(() -> {
			tabMap.remove(browser);
			this.parent.closeTab(browser);
		});
	}

	public void closeTab(CefBrowser browser) {
		final HBox tabBox = tabMap.get(browser);
		closeTab(tabBox, browser);
	}

	public void setTabTitle(CefBrowser browser, String title) {
		final HBox box = tabMap.get(browser);
		if (box == null)
			return; // Temporary fix for threading issues
		final Label tabTitle = (Label) box.getChildren().get(0);
		tabTitle.setText(title);
	}

	public void setCurrentTab(CefBrowser currentBrowser) {
		for (final Map.Entry<CefBrowser, HBox> entry : tabMap.entrySet()) {
			final HBox tabBox = entry.getValue();
			final CefBrowser browserKey = entry.getKey();

			tabBox.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				boolean isActive = (browserKey == currentBrowser);
				Paint color = isActive ? parent.profileMaterialColorScheme.getSurfaceContainer().get()
						: parent.profileMaterialColorScheme.getSurface().get();

				return new Background(new BackgroundFill(color, new CornerRadii(25), null));
			}, parent.profileMaterialColorScheme.getSurfaceContainer(),
					parent.profileMaterialColorScheme.getSurface()));
		}
	}
}
