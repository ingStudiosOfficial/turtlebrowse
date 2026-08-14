<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/form-field';
import '@m3e/web/button-group';
import '@m3e/web/button';
import '@m3e/web/divider';
import { useWallpaper } from '@/composables/wallpaper';

const { newtabSettingsDialog } = useDialog();
const { wallpaperUrl, saveWallpaper } = useWallpaper();

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const greetingText = ref<string>('');
const imageInput = useTemplateRef<HTMLInputElement>('imageInput');

async function onImageUpload(el: HTMLInputElement) {
	const file = el?.files?.[0];
	if (!file) {
		console.error('File not uploaded.');
		return;
	}

	await saveWallpaper(file);
}

function clearImage() {
	wallpaperUrl.value = null;
}

onMounted(async () => {
	newtabSettingsDialog.value = dialog.value;
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Customize New Tab</span>
		<div class="ns-dialog">
			<m3e-form-field>
				<label slot="label">Greeting text</label>
				<input v-model="greetingText" />
			</m3e-form-field>
			<m3e-divider></m3e-divider>
			<span>Wallpaper</span>
			<img v-if="wallpaperUrl" :src="wallpaperUrl" class="uploaded-wallpaper" />
			<m3e-button-group variant="connected">
				<m3e-button variant="filled" @click="imageInput?.click()">
					<m3e-icon slot="icon" name="image"></m3e-icon>
					Change
				</m3e-button>
				<m3e-button variant="outlined" @click="clearImage()">
					<m3e-icon slot="icon" name="clear"></m3e-icon>
					Clear
				</m3e-button>
			</m3e-button-group>
			<input type="file" style="display: none;" ref="imageInput" @input="onImageUpload($event.target as HTMLInputElement)" />
		</div>
	</m3e-dialog>
</template>

<style scoped>
.ns-dialog {
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

.uploaded-wallpaper {
	width: 100%;
	aspect-ratio: 16 / 9;
	object-fit: cover;
	object-position: center;
	border-radius: 20px;
}
</style>
