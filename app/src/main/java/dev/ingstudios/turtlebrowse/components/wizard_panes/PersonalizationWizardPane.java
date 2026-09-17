package dev.ingstudios.turtlebrowse.components.wizard_panes;

import org.controlsfx.dialog.WizardPane;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.wizard.WizardData;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PersonalizationWizardPane extends WizardPane {
	public PersonalizationWizardPane(WizardData wizardData) {
		setHeaderText("Browser Personalization");
		setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/logo_full_trans.png"))));
		backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getSurface()));
		getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
		getStylesheets().add(getClass().getResource("/css/wizard.css").toExternalForm());

		final VBox mainBox = new VBox();
		mainBox.setStyle("-fx-spacing: 16px;");
		mainBox.setAlignment(Pos.CENTER_LEFT);

		final Label nameLabel = new Label("What should we call you?");
		nameLabel.setFont(Font.font("Google Sans Flex", FontWeight.NORMAL, 15));

		final TextField nameTextField = new TextField();
		nameTextField.setText(wizardData.name);
		nameTextField.setStyle("-fx-padding: 10px;");
		nameTextField.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getSurfaceContainer()));
		nameTextField.setPromptText("Enter your preferred name");
		nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			wizardData.name = newValue;
		});

		mainBox.getChildren().addAll(nameLabel, nameTextField);

		setContent(mainBox);
	}
}
