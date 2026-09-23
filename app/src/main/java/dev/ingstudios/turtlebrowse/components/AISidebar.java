package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseLoadHandler.JSQueueItem;
import dev.ingstudios.turtlebrowse.tools.SummarizePageTool;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class AISidebar extends ToolSidebar {
	private final Component ui;
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final CefBrowser aiBrowser;
	private final ScheduledExecutorService summarizeScheduler = Executors.newSingleThreadScheduledExecutor();
	private final ScheduledExecutorService rewriteScheduler = Executors.newSingleThreadScheduledExecutor();
	private final ScheduledExecutorService summarizePageScheduler = Executors.newSingleThreadScheduledExecutor();
	private final Gson gson = new Gson();
	private final SummarizePageTool summarizePageTool;
	private final JFXPanel actionsBarJfxPanel;
	private final MainWindow parent;

	public AISidebar(CefClient client, MainWindow parent, boolean useOsr, BooleanProperty isUiFocused) {
		this.parent = parent;

		this.setLayout(new java.awt.BorderLayout());
		this.setPreferredSize(preferredDim);

		summarizePageTool = new SummarizePageTool(parent);

		actionsBarJfxPanel = new JFXPanel();
		actionsBarJfxPanel.setFocusable(true);
		actionsBarJfxPanel.setPreferredSize(new java.awt.Dimension(preferredDim.width, 50));

		Platform.runLater(() -> {
			final HBox actionsBar = new HBox();
			actionsBar.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
			actionsBar.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.profileMaterialColorScheme.getSurface()));
			actionsBar.setAlignment(Pos.CENTER_RIGHT);

			final Scene actionsBarScene = new Scene(actionsBar);
			actionsBarScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			actionsBarJfxPanel.setScene(actionsBarScene);
			actionsBar.prefWidthProperty().bind(actionsBarScene.widthProperty());
			actionsBar.prefHeightProperty().bind(actionsBarScene.heightProperty());

			final JFXButton closeButton = buildButton(Material2OutlinedAL.CLOSE);
			closeButton.setOnAction(event -> {
				closeSidebar();
			});

			actionsBar.getChildren().addAll(closeButton);
		});

		aiBrowser = client.createBrowser("turtlebrowse://chat", useOsr, false);
		parent.requestHandler.setAiBrowser(aiBrowser);
		final Component browserComponent = aiBrowser.getUIComponent();
		ui = browserComponent;

		if (browserComponent.getMouseListeners().length == 0) {
			browserComponent.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent event) {
					SwingUtilities.invokeLater(() -> {
						isUiFocused.set(false);
						browserComponent.requestFocusInWindow();
						aiBrowser.setFocus(true);
					});
				}
			});
		}

		this.add(actionsBarJfxPanel, BorderLayout.NORTH);
		this.add(browserComponent, BorderLayout.CENTER);
	}

	private JFXButton buildButton(Ikon iconName) {
		final JFXButton button = new JFXButton("");
		final FontIcon icon = new FontIcon(iconName);
		icon.setIconColor(parent.profileMaterialColorScheme.getOnSurface().get());
		parent.profileMaterialColorScheme.getOnSurface().addListener((observable, oldPaint, newPaint) -> {
			icon.setIconColor(newPaint);
		});
		button.setGraphic(icon);
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setStyle("-fx-padding: 10px;");
		button.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer()));
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
		System.out.println("Opening AI sidebar.");
		preferredDim.width = 500;
		ui.setVisible(true);
		isOpen = true;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void closeSidebar() {
		System.out.println("Closing AI sidebar.");
		preferredDim.width = 0;
		ui.setVisible(false);
		isOpen = false;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	public void summarize(String text) {
		if (!isOpen)
			SwingUtilities.invokeLater(() -> {
				if (parent.getSidebar() != parent.aiSidebar) {
					parent.setSidebar(parent.aiSidebar);
					openSidebar();
				} else {
					toggleSidebar();
				}
			});

		summarizeScheduler.schedule(() -> {
			final String jsonText = gson.toJson("Summarize this:\n:::extract\n" + text + "\n:::");
			final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
					"window.addPrompt(" + jsonText + ");", "turtlebrowse://chat");

			aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
		}, 500, TimeUnit.MILLISECONDS);
	}

	public void rewrite(String text) {
		if (!isOpen)
			SwingUtilities.invokeLater(() -> {
				if (parent.getSidebar() != parent.aiSidebar) {
					parent.setSidebar(parent.aiSidebar);
					openSidebar();
				} else {
					toggleSidebar();
				}
			});

		rewriteScheduler.schedule(() -> {
			final String jsonText = gson.toJson("Rewrite\n:::extract" + text + "\n:::\nto be");
			final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
					"window.addPromptRewrite(" + jsonText + ");", "turtlebrowse://chat");

			aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
		}, 1000, TimeUnit.MILLISECONDS);
	}

	public void summarizePage(String html) {
		System.out.printf("Summarizing page...\n");

		if (!isOpen)
			SwingUtilities.invokeLater(() -> {
				if (parent.getSidebar() != parent.aiSidebar) {
					parent.setSidebar(parent.aiSidebar);
					openSidebar();
				} else {
					toggleSidebar();
				}
			});

		summarizePageScheduler.schedule(() -> {
			if (html == null) {
				return;
			}

			final String markdown = summarizePageTool.summarizePage(html);
			System.out.printf("Converted Markdown: %s\n", markdown);

			String jsonText = gson.toJson("Summarize this page:\n:::extract" + markdown + "\n:::");
			final JSQueueItem item = new JSQueueItem(aiBrowser.getIdentifier(),
					"window.addPrompt(" + jsonText + ");", "turtlebrowse://chat");

			aiBrowser.getMainFrame().executeJavaScript(item.code(), item.url(), 0);
		}, 500, TimeUnit.MILLISECONDS);
	}
}
