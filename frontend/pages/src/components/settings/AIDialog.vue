<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/form-field';
import type { M3eCheckboxElement } from '@m3e/web/checkbox';
import { getAISettings, setAISettings } from '@/utils/java_bridge';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const aiEnabled = ref<boolean>(false);
const aiModel = ref<string>('gemma4:e2b');

const { aiDialog } = useDialog();

async function toggleAIEnabled(target: M3eCheckboxElement) {
	const checked = target.checked;
	console.log('Enabled:', checked);

	aiEnabled.value = checked;

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
	<m3e-dialog ref="dialog">
		<span slot="header">AI integrations</span>
		<div class="ai-dialog">
			<div class="toggle-setting">
				<p>Enable local AI integrations</p>
				<m3e-switch icons="both" :checked="aiEnabled" @change="toggleAIEnabled($event.target)"></m3e-switch>
			</div>
			<div v-if="aiEnabled" class="ai-enabled">
				<m3e-form-field>
					<label slot="label">Ollama AI model</label>
					<input v-model="aiModel" @change="setAISettings({
						enabled: aiEnabled,
						model: aiModel,
					})" />
				</m3e-form-field>
			</div>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.ai-dialog {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8px;
}

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
