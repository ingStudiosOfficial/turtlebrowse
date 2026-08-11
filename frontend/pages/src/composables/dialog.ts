import { M3eDialogElement } from "@m3e/web/dialog";
import { ref } from "vue";

const privacyDialog = ref<M3eDialogElement | null>(null);
const searchEnginesDialog = ref<M3eDialogElement | null>(null);
const aiDialog = ref<M3eDialogElement | null>(null);

export function useDialog() {
	function showDialog(dialog: 'privacy' | 'search' | 'ai') {
		console.log('Showing dialog:', dialog);

		switch (dialog) {
			case 'privacy': {
				privacyDialog.value?.show();
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
		}
	}

	return { privacyDialog, searchEnginesDialog, aiDialog, showDialog };
}
