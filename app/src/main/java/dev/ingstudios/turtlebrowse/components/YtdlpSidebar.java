package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Files;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import com.jfoenix.controls.JFXButton;
import com.jfposton.ytdlp.DownloadProgressCallback;
import com.jfposton.ytdlp.YtDlp;
import com.jfposton.ytdlp.YtDlpException;
import com.jfposton.ytdlp.YtDlpRequest;
import com.jfposton.ytdlp.YtDlpResponse;
import com.jfposton.ytdlp.mapper.VideoInfo;

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
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class YtdlpSidebar extends ToolSidebar {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private ProgressCallback onProgress;
	private ErrorCallback onError;
	private DoneCallback onDone;
	private TitleCallback onTitle;
	private final MainWindow parent;
	private Label titleLabel;
	private Label downloadLabel;

	public YtdlpSidebar(MainWindow parent) {
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

			titleLabel = new Label("Downloading video");
			titleLabel.setStyle("-fx-font-weight: bold;");

			downloadLabel = new Label("Downloading video via yt-dlp...");

			contentBox.getChildren().addAll(titleLabel, downloadLabel);

			onProgress = (progress, eta) -> {
				downloadLabel.setText("%d downloaded... (%d seconds left)".formatted(progress, eta));
			};

			onError = (error) -> {
				downloadLabel.setText("Failed to download video: %s".formatted(error));
			};

			onDone = (name) -> {
				downloadLabel.setText("Successfully downloaded %s to %s.".formatted(name, getDownloadDirectory()));
			};

			onTitle = (title) -> {
				titleLabel.setText("Downloading %s".formatted(title));
			};

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
		refresh();
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

	private void refresh() {
		final String url = parent.currentBrowser.getURL();

		titleLabel.setText("Downloading %s".formatted(url));
		downloadLabel.setText("Downloading video via yt-dlp...");

		Thread.ofVirtual().start(() -> {
			downloadVideo(url);
		});
	}

	private void downloadVideo(String url) {
		final String directory = getDownloadDirectory();

		try {
			final VideoInfo info = YtDlp.getVideoInfo(url).get(0);

			final String title = info.getTitle();

			Platform.runLater(() -> {
				onTitle.onTitle(title);
			});

			final YtDlpRequest request = new YtDlpRequest(url, directory);
			request.setOption("retries", 3);

			final YtDlpResponse response = YtDlp.execute(request, new DownloadProgressCallback() {
				@Override
				public void onProgressUpdate(float progress, long etaInSeconds) {
					System.out.printf("Progress: %d\nETA: %ds\n", progress, etaInSeconds);
					Platform.runLater(() -> {
						onProgress.onProgress(progress, etaInSeconds);
					});
				}
			});

			System.out.printf("Response: %s\n", response.getOut());

			Platform.runLater(() -> {
				onDone.onDone(title);
			});
		} catch (YtDlpException e) {
			e.printStackTrace();
			Platform.runLater(() -> {
				onError.onError(e.getMessage());
			});
		}
	}

	private String getDownloadDirectory() {
		final String home = System.getProperty("user.home");
		final File downloads = new File(home + File.separator + "Downloads");
		if (Files.isDirectory(downloads.toPath())) {
			return downloads.toString();
		} else {
			return home;
		}
	}
}

@FunctionalInterface
interface ProgressCallback {
	void onProgress(float progress, long eta);
}

@FunctionalInterface
interface ErrorCallback {
	void onError(String message);
}

@FunctionalInterface
interface DoneCallback {
	void onDone(String name);
}

@FunctionalInterface
interface TitleCallback {
	void onTitle(String title);
}
