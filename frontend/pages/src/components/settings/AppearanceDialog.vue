<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { nextTick, onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/select';
import '@m3e/web/option';
import '@m3e/web/icon';
import {
	getAppearance,
	getNewtabPageUrl,
	setAppearance,
	setNewtabPageUrl,
} from '@/utils/java_bridge';
import type { M3eSelectElement } from '@m3e/web/select';
import type { Appearance } from '@/types/Appearance';
import '@m3e/web/heading';
import '@m3e/web/divider';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const appearance = ref<Appearance>('system');
const newtabUrl = ref<string>('turtlebrowse://newtab');

const { appearanceDialog } = useDialog();

async function changeAppearance(target: M3eSelectElement) {
	await nextTick();

	const chosen = target.value as Appearance;
	console.log('Appearance:', chosen);

	appearance.value = chosen;

	await setAppearance(chosen);
}

async function setNewtabUrl(url: string) {
	if (url.trim().length === 0) {
		newtabUrl.value = 'turtlebrowse://newtab';
		await setNewtabPageUrl('turtlebrowse://newtab');
		return;
	}

	newtabUrl.value = url;
	await setNewtabPageUrl(url);
}

onMounted(async () => {
	appearanceDialog.value = dialog.value;

	appearance.value = await getAppearance();
	console.log('Fetched appearance:', appearance);

	newtabUrl.value = await getNewtabPageUrl();
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Appearance</span>
		<div class="settings-item-dialog">
			<m3e-heading variant="title" size="large">Theme</m3e-heading>
			<m3e-form-field>
				<label slot="label">Theme</label>
				<m3e-select :key="appearance" @change="changeAppearance($event.target)">
					<m3e-option value="system" :selected="appearance === 'system'">
						System
					</m3e-option>
					<m3e-option value="light" :selected="appearance === 'light'">
						Light
					</m3e-option>
					<m3e-option value="dark" :selected="appearance === 'dark'"> Dark </m3e-option>
				</m3e-select>
			</m3e-form-field>

			<span>Profile theme color can be changed via profile settings</span>

			<m3e-divider></m3e-divider>

			<m3e-heading variant="title" size="large">New Tab</m3e-heading>

			<m3e-form-field>
				<label slot="label">New Tab page URL</label>
				<input
					v-model="newtabUrl"
					@change="setNewtabUrl(($event.target as HTMLInputElement).value)"
				/>
				<span slot="hint">Page URL can be a http url or a local file url</span>
			</m3e-form-field>
		</div>
	</m3e-dialog>
</template>
