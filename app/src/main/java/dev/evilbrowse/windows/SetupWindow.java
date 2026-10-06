package dev.evilbrowse.windows;

import org.controlsfx.dialog.Wizard;

import dev.evilbrowse.components.wizard_panes.AIWizardPane;
import dev.evilbrowse.components.wizard_panes.PersonalizationWizardPane;
import dev.evilbrowse.components.wizard_panes.StartWizardPane;
import dev.evilbrowse.components.wizard_panes.ThemeWizardPane;
import dev.evilbrowse.managers.DiscordPresenceManager;
import dev.evilbrowse.wizard.WizardData;

public class SetupWindow extends Wizard {
	public final WizardData wizardData = new WizardData();

	public SetupWindow() {
		final StartWizardPane startWizardPane = new StartWizardPane();
		final PersonalizationWizardPane personalizationWizardPane = new PersonalizationWizardPane(wizardData);
		final ThemeWizardPane themeWizardPane = new ThemeWizardPane(wizardData);
		final AIWizardPane aiWizardPane = new AIWizardPane(wizardData);

		final LinearFlow flow = new LinearFlow(startWizardPane, personalizationWizardPane, themeWizardPane,
				aiWizardPane);

		setFlow(flow);

		DiscordPresenceManager.getInstance().updateDiscordPresence("Setting up EvilBrowse");
	}
}
