<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/form-field';
import { getAISettings, setAISettings } from '@/utils/java_bridge';
import type { M3eSwitchElement } from '@m3e/web/switch';
import { M3eSnackbar } from '@m3e/web/snackbar';
import '@m3e/web/heading';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const aiEnabled = ref<boolean>(false);
const aiModel = ref<string>('gemma4:e2b');

const { aiDialog } = useDialog();

async function toggleAIEnabled(target: M3eSwitchElement) {
	const checked = target.checked;
	console.log('Enabled:', checked);

	aiEnabled.value = checked;

	M3eSnackbar.open('Restart Turtlebrowse to apply changes');

	await setAISettings({
		enabled: aiEnabled.value,
		model: aiModel.value,
	});
}

onMounted(async () => {
	aiDialog.value = dialog.value;

	const { enabled, model } = await getAISettings();
	aiEnabled.value = enabled;
	aiModel.value = model;
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">AI integrations</span>
		<div class="settings-item-dialog">
			<m3e-heading variant="title" size="large">Local AI integrations</m3e-heading>
			<div class="toggle-setting">
				<p>Enable local AI integrations</p>
				<m3e-switch
					icons="both"
					:checked="aiEnabled"
					@change="toggleAIEnabled($event.target)"
				></m3e-switch>
			</div>
			<div v-if="aiEnabled" class="ai-enabled">
				<m3e-form-field>
					<label slot="label">Ollama AI model</label>
					<input
						v-model="aiModel"
						@change="
							setAISettings({
								enabled: aiEnabled,
								model: aiModel,
							})
						"
					/>
				</m3e-form-field>
			</div>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.toggle-setting {
	display: flex;
	flex-direction: row;
	gap: 15px;
	align-items: center;
	justify-content: space-between;
}

.ai-enabled {
	all: inherit;
}
</style>
