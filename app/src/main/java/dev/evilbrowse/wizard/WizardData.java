package dev.evilbrowse.wizard;

import dev.evilbrowse.Main;
import dev.evilbrowse.db.MainDatabase;
import dev.evilbrowse.db.MainDatabase.ProfileStructure;
import javafx.scene.paint.Color;

public class WizardData {
	private final MainDatabase db = MainDatabase.getInstance();
	public String name = "";
	public Color themeColor = Main.mainMaterialColorScheme.getPrimary().get();
	public boolean enableAI = false;

	public WizardData() {
	}

	public void saveData() {
		db.createProfile(new ProfileStructure(name, themeColor));
		db.setFeature("ai", enableAI);
	}
}
