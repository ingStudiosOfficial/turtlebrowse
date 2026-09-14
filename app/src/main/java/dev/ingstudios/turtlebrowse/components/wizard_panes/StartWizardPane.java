package dev.ingstudios.turtlebrowse.components.wizard_panes;

import org.controlsfx.dialog.WizardPane;

import dev.ingstudios.turtlebrowse.Main;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StartWizardPane extends WizardPane {
	public StartWizardPane() {
		setHeaderText("Set Turtlebrowse Up");
		backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getSurface()));
		getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
		getStylesheets().add(getClass().getResource("/css/wizard.css").toExternalForm());

		final VBox mainBox = new VBox();
		mainBox.setStyle("-fx-spacing: 16px;");
		mainBox.setAlignment(Pos.CENTER_LEFT);

		final Label welcomeText = new Label("Welcome to Turtlebrowse");
		welcomeText.setFont(Font.font("Google Sans Flex", FontWeight.BOLD, 30));

		final Label setupLabel = new Label("Please follow this wizard to set Turtlebrowse up.");
		setupLabel.setFont(Font.font("Google Sans Flex", FontWeight.NORMAL, 15));

		mainBox.getChildren().addAll(welcomeText, setupLabel);

		setContent(mainBox);
	}
}
