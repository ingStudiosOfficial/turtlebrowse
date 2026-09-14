package dev.ingstudios.turtlebrowse.managers;

import java.io.File;
import java.nio.file.Files;

import org.controlsfx.control.PopOver;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import dev.ingstudios.turtlebrowse.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

import com.jfoenix.controls.JFXButton;
import com.jfposton.ytdlp.DownloadProgressCallback;
import com.jfposton.ytdlp.YtDlp;
import com.jfposton.ytdlp.YtDlpException;
import com.jfposton.ytdlp.YtDlpRequest;
import com.jfposton.ytdlp.YtDlpResponse;
import com.jfposton.ytdlp.mapper.VideoInfo;

public class YtdlpManager {
	private static YtdlpManager instance;
	private final MainWindow parent;
	private PopOver downloadPopup;
	private ProgressCallback onProgress;
	private ErrorCallback onError;
	private DoneCallback onDone;
	private TitleCallback onTitle;

	private YtdlpManager(MainWindow parent) {
		this.parent = parent;
	}

	public static synchronized YtdlpManager getInstance(MainWindow parent) {
		if (instance == null) {
			instance = new YtdlpManager(parent);
		}
		return instance;
	}

	public void createDownloadPopup() {
		if (downloadPopup != null)
			return;

		final VBox downloadBox = new VBox();
		downloadBox.setStyle("-fx-spacing: 16px; -fx-padding: 16px;");

		final String url = parent.currentBrowser.getURL();

		final Label titleLabel = new Label("Downloading %s".formatted(url));
		titleLabel.setStyle("-fx-font-weight: bold;");

		final Label downloadLabel = new Label("Downloading video via yt-dlp...");

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
		closeButton.setOnMouseDragExited(event -> {
			closeButton.setCursor(Cursor.DEFAULT);
		});
		closeButton.setOnAction(event -> {
			downloadPopup.hide();
			downloadPopup = null;
		});

		downloadBox.getChildren().addAll(titleLabel, downloadLabel, closeButton);

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

		downloadPopup = new PopOver(downloadBox);
		downloadPopup.setTitle("Download video");
		downloadPopup.setDetachable(false);
		downloadPopup.setAutoHide(true);
		downloadPopup.setHideOnEscape(true);
		downloadPopup.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
		downloadPopup.setCornerRadius(12);
		downloadPopup.setOnHidden(event -> {
			downloadPopup = null;
		});
		downloadPopup.show(parent.addressBar.root);

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
