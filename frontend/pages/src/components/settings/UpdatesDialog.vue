<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/button';
import '@m3e/web/icon';
import '@m3e/web/icon-button';
import '@m3e/web/loading-indicator';
import { getUpdateInfo, navigateToSite } from '@/utils/java_bridge';

const dialog = useTemplateRef<M3eDialogElement>('dialog');

const { updatesDialog } = useDialog();

const currentVersion = ref<string>('1.0.0');
const latestVersion = ref<string>('1.0.0');
const needsUpdate = ref<boolean | null>(null);

async function refreshUpdateInfo() {
	needsUpdate.value = null;

	const updateInfo = await getUpdateInfo(true);
	if (updateInfo) {
		currentVersion.value = updateInfo.currentVersion;
		latestVersion.value = updateInfo.latestVersion;
		needsUpdate.value = updateInfo.needsUpdate;
	}
}

onMounted(async () => {
	updatesDialog.value = dialog.value;

	const updateInfo = await getUpdateInfo();
	if (updateInfo) {
		currentVersion.value = updateInfo.currentVersion;
		latestVersion.value = updateInfo.latestVersion;
		needsUpdate.value = updateInfo.needsUpdate;
	}
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Browser updates</span>
		<div class="updates-dialog">
			<div v-if="needsUpdate === null" class="update-box">
				<m3e-loading-indicator variant="contained"></m3e-loading-indicator>
				<p>Checking for updates...</p>
			</div>
			<div v-else-if="needsUpdate === true" class="update-box">
				<p>Browser update recommended (v{{ currentVersion }} to v{{ latestVersion }})</p>
				<m3e-button variant="filled" @click="navigateToSite('https://turtlebrowse.ingstudios.dev/download')">
					<m3e-icon slot="icon" name="update"></m3e-icon>
					Update Turtlebrowse
				</m3e-button>
			</div>
			<div v-else class="update-box">
				<m3e-icon name="check"></m3e-icon>
				<p>Browser up-to-date (Turtlebrowse v{{ currentVersion }})</p>
			</div>
		</div>
		<div slot="actions" end>
			<m3e-icon-button @click="refreshUpdateInfo()">
				<m3e-icon name="refresh"></m3e-icon>
			</m3e-icon-button>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.updates-dialog {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8px;
	box-sizing: border-box;
	padding: 8px;
}

.update-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
	gap: 8px;
}
</style>
