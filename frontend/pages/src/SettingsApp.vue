<script setup lang="ts">
import '@m3e/web/card';
import '@m3e/web/list';
import '@m3e/web/avatar';
import '@m3e/web/icon';
import { useDialog } from './composables/dialog';
import PrivacyDialog from './components/settings/PrivacyDialog.vue';
import SearchEnginesDialog from './components/settings/SearchEnginesDialog.vue';
import AIDialog from './components/settings/AIDialog.vue';
import UpdatesDialog from './components/settings/UpdatesDialog.vue';
import AppearanceDialog from './components/settings/AppearanceDialog.vue';

const { showDialog } = useDialog();

function scrollToAbout() {
	document.querySelector('.about-card')?.scrollIntoView({ behavior: 'smooth' });
}
</script>

<template>
	<div class="settings-wrapper">
		<h1 class="settings-header">Settings</h1>
		<p class="settings-sub">EvilBrowse — a stabilized, modern Turtlebrowse fork.</p>
		<m3e-card>
			<m3e-action-list slot="content" variant="segmented">
				<m3e-list-action @click="showDialog('privacy')">
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="shield"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">Privacy & security</span>
				</m3e-list-action>
				<m3e-list-action @click="showDialog('appearance')">
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="palette"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">Appearance</span>
				</m3e-list-action>
				<m3e-list-action @click="showDialog('search')">
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="search"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">Search engines</span>
				</m3e-list-action>
				<m3e-list-action @click="showDialog('ai')">
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="robot_2"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">AI integrations (optional)</span>
				</m3e-list-action>
				<m3e-list-action @click="showDialog('updates')">
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="update"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">Browser updates</span>
				</m3e-list-action>
				<m3e-list-action @click="scrollToAbout()">
					<m3e-avatar slot="leading">
						<m3e-icon optical-size="30" weight="600" name="info"></m3e-icon>
					</m3e-avatar>
					<span class="settings-text">About EvilBrowse</span>
				</m3e-list-action>
			</m3e-action-list>
		</m3e-card>

		<m3e-card class="about-card">
			<div slot="content" class="about-content">
				<h2>About EvilBrowse</h2>
				<p>
					EvilBrowse is a maintenance, stabilization, and modernization fork of
					Turtlebrowse. The name is playful: the browser is "evil" because it
					fights bugs and refuses to behave badly.
				</p>
				<p class="muted">Vertical tabs • Local-first AI • Private by default</p>
			</div>
		</m3e-card>

		<PrivacyDialog></PrivacyDialog>
		<AppearanceDialog></AppearanceDialog>
		<SearchEnginesDialog></SearchEnginesDialog>
		<AIDialog></AIDialog>
		<UpdatesDialog></UpdatesDialog>
	</div>
</template>

<style scoped>
.settings-header {
	margin: 16px 0 4px 0;
	font-size: 2.5rem;
}

.settings-sub {
	margin: 0 0 12px 0;
	opacity: 0.7;
}

.settings-wrapper {
	display: flex;
	flex-direction: row;
	width: 100dvw;
	height: 100dvh;
	box-sizing: border-box;
	padding: 16px;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.settings-text {
	font-size: 1.7rem;
}

.about-card {
	margin-top: 16px;
	width: 100%;
	max-width: 720px;
}

.about-content {
	padding: 16px 20px;
}

.about-content h2 {
	margin: 0 0 8px 0;
}

.about-content p {
	margin: 6px 0;
}

.muted {
	opacity: 0.7;
}

m3e-avatar {
	--m3e-avatar-size: 50px;
}
</style>
