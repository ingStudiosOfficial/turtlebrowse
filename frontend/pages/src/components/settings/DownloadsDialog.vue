<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/heading';
import '@m3e/web/button';
import '@m3e/web/icon';
import { chooseDownloadsDir, getDownloadsDir } from '@/utils/java_bridge';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const downloadsDir = ref<string>('Downloads');

const { downloadsDialog } = useDialog();

async function chooseDirectory() {
	downloadsDir.value = await chooseDownloadsDir();
}

onMounted(async () => {
	downloadsDialog.value = dialog.value;

	downloadsDir.value = await getDownloadsDir();
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Downloads</span>
		<div class="settings-item-dialog">
			<m3e-heading variant="title" size="large">Downloads directory</m3e-heading>
			<div class="directory-item">
				<p>{{ downloadsDir }}</p>
				<m3e-button variant="outlined" @click="chooseDirectory()">
					<m3e-icon slot="icon" name="folder_open" />
					Change
				</m3e-button>
			</div>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.directory-item {
	display: flex;
	flex-direction: row;
	gap: 15px;
	align-items: center;
	justify-content: space-between;
}
</style>
