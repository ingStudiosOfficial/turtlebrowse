<script setup lang="ts">
import { getUserName, searchWeb } from '@/utils/java_bridge';
import { onMounted, ref } from 'vue';
import '@m3e/web/form-field';
import '@m3e/web/icon';
import '@m3e/web/icon-button';
import NewtabSettingsDialog from './components/newtab/NewtabSettingsDialog.vue';
import { useDialog } from './composables/dialog';
import '@m3e/web/tooltip';
import { useWallpaper } from './composables/wallpaper.ts';

const { showDialog } = useDialog();

const userName = ref<string>('');
const searchQuery = ref<string>('');
const errorMessage = ref<string>('');
const { wallpaperUrl, refreshWallpaper } = useWallpaper();

async function searchWebWrapper() {
	try {
		await searchWeb(searchQuery.value);
	} catch (error) {
		console.error('Error while searching web:', error);
		errorMessage.value = (error as Error).message;
	}
}

onMounted(async () => {
	userName.value = await getUserName();
	await refreshWallpaper();
});
</script>

<template>
	<div class="newtab-wrapper" :class="wallpaperUrl !== null ? 'bg-wallpaper' : ''">
		<div class="center-wrapper">
			<div class="wrapper-bg"></div>
			<h1>Hello, {{ userName }}!</h1>
			<m3e-form-field class="search-bar" variant="outlined" @keydown.enter.prevent="searchWebWrapper()">
				<label slot="label">Search the web</label>
				<input v-model="searchQuery" id="search-fld" />
				<m3e-icon slot="prefix" name="search"></m3e-icon>
				<label slot="hint">{{ errorMessage }}</label>
			</m3e-form-field>
		</div>
		<m3e-icon-button id="settings-btn" class="settings-btn" @click="showDialog('ns')">
			<m3e-icon name="settings"></m3e-icon>
		</m3e-icon-button>
		<m3e-tooltip for="settings-btn" position="before">Customize New Tab</m3e-tooltip>
		<NewtabSettingsDialog></NewtabSettingsDialog>
	</div>
</template>

<style scoped>
.newtab-wrapper {
	padding: 20px;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	width: 100dvw;
	height: 100dvh;
	box-sizing: border-box;
}

.bg-wallpaper {
	background-image: v-bind('`url("${wallpaperUrl}")`');
	background-size: cover;
	background-position: center;
}

.center-wrapper {
	position: relative;
	width: 50%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40px 20px;
	z-index: 1;
}

.wrapper-bg {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: var(--md-sys-color-surface);
	opacity: 0.9;
	z-index: -1;
	box-sizing: border-box;
	border-radius: 20px;
}

.search-bar {
	width: 100%;
}

.settings-btn {
	position: fixed;
	bottom: 20px;
	right: 20px;
}
</style>
