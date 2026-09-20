<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { nextTick, onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/select';
import '@m3e/web/option';
import '@m3e/web/icon';
import { getAppearance, setAppearance } from '@/utils/java_bridge';
import type { M3eSelectElement } from '@m3e/web/select';
import type { Appearance } from '@/types/Appearance';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const appearance = ref<Appearance>('system');

const { appearanceDialog } = useDialog();

async function changeAppearance(target: M3eSelectElement) {
	await nextTick();

	const chosen = target.value as Appearance;
	console.log('Appearance:', chosen);

	appearance.value = chosen;

	await setAppearance(chosen);
}

onMounted(async () => {
	appearanceDialog.value = dialog.value;

	appearance.value = await getAppearance();
	console.log('Fetched appearance:', appearance);
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Appearance</span>
		<div class="appearance-dialog">
			<m3e-form-field>
				<label slot="label">Theme</label>
				<m3e-select :key="appearance" @change="changeAppearance($event.target)">
					<m3e-option value="system" :selected="appearance === 'system'">
						System
					</m3e-option>
					<m3e-option value="light" :selected="appearance === 'light'">
						Light
					</m3e-option>
					<m3e-option value="dark" :selected="appearance === 'dark'">
						Dark
					</m3e-option>
				</m3e-select>
			</m3e-form-field>
			<span>Profile theme color can be changed via profile settings</span>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.appearance-dialog {
	display: flex;
	flex-direction: column;
	align-items: left;
	justify-content: center;
	gap: 8px;
	box-sizing: border-box;
	padding: 8px;
}
</style>
