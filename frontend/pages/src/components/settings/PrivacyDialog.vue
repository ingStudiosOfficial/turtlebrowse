<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/switch';
import {
	getDiscordPresenceSetting,
	setAdblockSetting,
	setDiscordPresenceSetting,
} from '@/utils/java_bridge';
import type { M3eSwitchElement } from '@m3e/web/switch';
import '@m3e/web/heading';
import '@m3e/web/divider';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const discordPresenceEnabled = ref<boolean>(false);
const adblockEnabled = ref<boolean>(true);

const { privacyDialog } = useDialog();

async function toggleDiscordPresence(target: M3eSwitchElement) {
	const checked = target.checked;
	console.log('Enabled:', checked);

	discordPresenceEnabled.value = checked;

	await setDiscordPresenceSetting(checked);
}

async function toggleAdblock(target: M3eSwitchElement) {
	const checked = target.checked;
	console.log('Enabled:', checked);

	adblockEnabled.value = checked;

	setAdblockSetting(checked);
}

onMounted(async () => {
	privacyDialog.value = dialog.value;

	discordPresenceEnabled.value = await getDiscordPresenceSetting();
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Privacy & security</span>
		<div class="settings-item-dialog">
			<m3e-heading variant="title" size="large">Discord Rich Presence</m3e-heading>
			<div class="toggle-setting">
				<p>Enable Discord Rich Presence</p>
				<m3e-switch
					icons="both"
					:checked="discordPresenceEnabled"
					@change="toggleDiscordPresence($event.target)"
				></m3e-switch>
			</div>

			<m3e-divider></m3e-divider>

			<m3e-heading variant="title" size="large">Ad-blocking</m3e-heading>
			<div class="toggle-setting">
				<p>Enable ad-blocking</p>
				<m3e-switch
					icons="both"
					:checked="adblockEnabled"
					@change="toggleAdblock($event.target)"
				></m3e-switch>
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
</style>
