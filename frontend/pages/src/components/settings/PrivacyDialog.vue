<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/switch';
import { getDiscordPresenceSetting, setDiscordPresenceSetting } from '@/utils/java_bridge';
import type { M3eCheckboxElement } from '@m3e/web/checkbox';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const discordPresenceEnabled = ref<boolean>(false);

const { privacyDialog } = useDialog();

async function toggleDiscordPresence(target: M3eCheckboxElement) {
	const checked = target.checked;
	console.log('Enabled:', checked);

	discordPresenceEnabled.value = checked;

	await setDiscordPresenceSetting(checked);
}

onMounted(async () => {
	privacyDialog.value = dialog.value;

	discordPresenceEnabled.value = await getDiscordPresenceSetting();
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Privacy & security</span>
		<div class="privacy-dialog">
			<div class="toggle-setting">
				<p>Enable Discord Presence</p>
				<m3e-switch icons="both" :checked="discordPresenceEnabled" @change="toggleDiscordPresence($event.target)"></m3e-switch>
			</div>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.privacy-dialog {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8px;
	box-sizing: border-box;
	padding: 8px;
}

.toggle-setting {
	display: flex;
	flex-direction: row;
	gap: 15px;
	align-items: center;
	justify-content: space-between;
}
</style>
