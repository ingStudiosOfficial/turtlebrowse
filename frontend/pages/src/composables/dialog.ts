import { M3eDialogElement } from '@m3e/web/dialog';
import { ref } from 'vue';

const privacyDialog = ref<M3eDialogElement | null>(null);
const appearanceDialog = ref<M3eDialogElement | null>(null);
const searchEnginesDialog = ref<M3eDialogElement | null>(null);
const aiDialog = ref<M3eDialogElement | null>(null);
const newtabSettingsDialog = ref<M3eDialogElement | null>(null);
const updatesDialog = ref<M3eDialogElement | null>(null);

export function useDialog() {
	function showDialog(dialog: 'privacy' | 'appearance' | 'search' | 'ai' | 'ns' | 'updates') {
		console.log('Showing dialog:', dialog);

		switch (dialog) {
			case 'privacy': {
				privacyDialog.value?.show();
				break;
			}
			case 'appearance': {
				appearanceDialog.value?.show();
				break;
			}
			case 'search': {
				searchEnginesDialog.value?.show();
				break;
			}
			case 'ai': {
				aiDialog.value?.show();
				break;
			}
			case 'ns': {
				newtabSettingsDialog.value?.show();
				break;
			}
			case 'updates': {
				updatesDialog.value?.show();
				break;
			}
		}
	}

	return {
		privacyDialog,
		appearanceDialog,
		searchEnginesDialog,
		aiDialog,
		newtabSettingsDialog,
		updatesDialog,
		showDialog,
	};
}
