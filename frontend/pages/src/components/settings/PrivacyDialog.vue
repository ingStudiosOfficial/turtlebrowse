<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/switch';
import { getAdblockEnabled, getAdblockStatus, getDiscordClientId, getDiscordPresenceSetting, refreshFilterLists, setAdblockEnabled, setDiscordClientId, setDiscordPresenceSetting } from '@/utils/java_bridge';
import type { AdblockStatus } from '@/utils/java_bridge';
import '@m3e/web/button';
import type { M3eCheckboxElement } from '@m3e/web/checkbox';

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const discordPresenceEnabled = ref<boolean>(false);
const adblockEnabled = ref<boolean>(true);
const discordClientId = ref<string>('');
const adblockStatus = ref<AdblockStatus | null>(null);

const { privacyDialog } = useDialog();

async function toggleDiscordPresence(target: M3eCheckboxElement) {
	const checked = target.checked;
	console.log('Enabled:', checked);

	discordPresenceEnabled.value = checked;

	await setDiscordPresenceSetting(checked);
}

async function saveDiscordClientId() {
	console.log('Client ID:', discordClientId.value);

	await setDiscordClientId(discordClientId.value);
}

async function refreshStatus() {
	adblockStatus.value = await getAdblockStatus();
}

async function updateFilterLists() {
	adblockStatus.value = null;
	await refreshFilterLists();

	// Loading 6 lists takes a few seconds; poll until the engine reports rules.
	for (let i = 0; i < 12; i++) {
		await new Promise((r) => setTimeout(r, 1500));
		const status = await getAdblockStatus();
		if (status && status.rules > 0) {
			adblockStatus.value = status;
			return;
		}
	}
	await refreshStatus();
}

async function toggleAdblock(target: M3eCheckboxElement) {
	const checked = target.checked;
	console.log('Adblock:', checked);

	adblockEnabled.value = checked;

	await setAdblockEnabled(checked);
}

onMounted(async () => {
	privacyDialog.value = dialog.value;

	discordPresenceEnabled.value = await getDiscordPresenceSetting();
	adblockEnabled.value = await getAdblockEnabled();
	discordClientId.value = await getDiscordClientId();
	await refreshStatus();
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Privacy & security</span>
		<div class="privacy-dialog">
			<div class="toggle-setting">
				<p>Enable Discord Presence</p>
				<m3e-switch
					icons="both"
					:checked="discordPresenceEnabled"
					@change="toggleDiscordPresence($event.target)"
				></m3e-switch>
			</div>
			<div class="toggle-setting">
				<p>Block ads &amp; trackers (uBlock lists)</p>
				<m3e-switch
					icons="both"
					:checked="adblockEnabled"
					@change="toggleAdblock($event.target)"
				></m3e-switch>
			</div>
			<div v-if="adblockEnabled" class="filter-status">
				<p v-if="adblockStatus">
					{{ adblockStatus.rules.toLocaleString() }} rules loaded ·
					{{ adblockStatus.blocked.toLocaleString() }} blocked this session
				</p>
				<p v-else class="muted">Loading filter lists…</p>
				<ul v-if="adblockStatus" class="filter-list">
					<li v-for="entry in adblockStatus.lists" :key="entry">{{ entry }}</li>
				</ul>
				<m3e-button variant="outlined" @click="updateFilterLists()">
					<m3e-icon slot="icon" name="refresh"></m3e-icon>
					Update filter lists now
				</m3e-button>
			</div>
			<div v-if="discordPresenceEnabled" class="client-id-setting">
				<m3e-form-field>
					<label slot="label">Discord App ID (optional)</label>
					<input
						v-model="discordClientId"
						placeholder="1527974656840044696"
						@change="saveDiscordClientId()"
					/>
				</m3e-form-field>
				<p class="client-id-note">Blank = default. The game name Discord shows comes from this app — create your own EvilBrowse app in the Discord developer portal to fully rebrand it.</p>
			</div>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.privacy-dialog {
	display: flex;
	flex-direction: column;
	align-items: left;
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

.client-id-setting {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.client-id-note {
	opacity: 0.7;
	font-size: 0.85rem;
	margin: 0;
}

.filter-status {
	display: flex;
	flex-direction: column;
	gap: 6px;
	align-items: flex-start;
	padding: 4px 0;
}

.filter-list {
	margin: 0;
	padding-inline-start: 18px;
	opacity: 0.75;
	font-size: 0.8rem;
}

.filter-status p {
	margin: 0;
	font-size: 0.85rem;
}

.muted {
	opacity: 0.7;
}
</style>
