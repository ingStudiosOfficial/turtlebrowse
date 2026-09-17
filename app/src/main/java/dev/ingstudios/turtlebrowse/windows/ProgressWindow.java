package dev.ingstudios.turtlebrowse.windows;

import dev.ingstudios.turtlebrowse.Main;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ProgressWindow extends Stage {
	private final Label installLabel;
	private final ProgressBar progressBar;

	public ProgressWindow() {
		setTitle("Turtlebrowse");
		getIcons().add(new Image(getClass().getResourceAsStream("/logo_full_trans.png")));

		final BorderPane root = new BorderPane();
		root.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, null, null));
		}, Main.mainMaterialColorScheme.getSurface()));

		final VBox progressBox = new VBox();
		progressBox.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
		progressBox.setAlignment(Pos.CENTER);

		final Scene progressScene = new Scene(root, 800, 600);
		progressScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

		installLabel = new Label("Installing Java CEF");
		installLabel.setFont(Font.font("Google Sans Flex", FontWeight.BOLD, 25));

		final Label waitLabel = new Label("This should take less than a minute to download and install");
		waitLabel.setFont(Font.font("Google Sans Flex", FontWeight.BOLD, 15));

		progressBar = new ProgressBar(-1);
		progressBar.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getPrimaryContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getPrimaryContainer()));

		progressBox.getChildren().addAll(installLabel, progressBar);

		root.setCenter(progressBox);

		setScene(progressScene);

		show();
	}

	public void updateProgress(float progress) {
		if (progress < 0) {
			close();
			return;
		}

		installLabel.setText("Installing Java CEF (%d%%)".formatted(Math.round(progress)));
		progressBar.setProgress(progress / 100);
	}
}
