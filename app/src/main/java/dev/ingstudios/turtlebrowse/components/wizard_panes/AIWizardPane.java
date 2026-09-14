package dev.ingstudios.turtlebrowse.components.wizard_panes;

import org.controlsfx.dialog.WizardPane;

import com.jfoenix.controls.JFXCheckBox;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.wizard.WizardData;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AIWizardPane extends WizardPane {
	public AIWizardPane(WizardData wizardData) {
		setHeaderText("Optional AI Featuures");
		backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = Main.mainMaterialColorScheme.getSurface().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, Main.mainMaterialColorScheme.getSurface()));
		getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
		getStylesheets().add(getClass().getResource("/css/wizard.css").toExternalForm());

		final VBox mainBox = new VBox();
		mainBox.setStyle("-fx-spacing: 16px;");
		mainBox.setAlignment(Pos.CENTER_LEFT);

		final JFXCheckBox enableAICheckBox = new JFXCheckBox("Enable optional AI features");
		enableAICheckBox.setSelected(wizardData.enableAI);
		enableAICheckBox.setFont(Font.font("Google Sans Flex", FontWeight.NORMAL, 15));
		enableAICheckBox.setCheckedColor(Main.mainMaterialColorScheme.getPrimary().get());
		enableAICheckBox.setOnAction(event -> {
			wizardData.enableAI = enableAICheckBox.isSelected();
		});

		mainBox.getChildren().addAll(enableAICheckBox);

		setContent(mainBox);
	}
}
