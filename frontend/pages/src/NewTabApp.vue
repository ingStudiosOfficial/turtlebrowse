<script setup lang="ts">
import { getSearchSuggestions, getUserName, searchWeb } from '@/utils/java_bridge';
import { onMounted, onUnmounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/form-field';
import '@m3e/web/icon';
import '@m3e/web/icon-button';
import NewtabSettingsDialog from './components/newtab/NewtabSettingsDialog.vue';
import { useDialog } from './composables/dialog';
import '@m3e/web/tooltip';
import { useNewtab } from './composables/newtab.ts';
import '@m3e/web/autocomplete';
import '@m3e/web/loading-indicator';
import { M3eAutocompleteElement, type AutocompleteQueryEventDetail } from '@m3e/web/autocomplete';

const { showDialog } = useDialog();

const searchEl = useTemplateRef<HTMLInputElement>('searchInput');
const autocompleteEl = useTemplateRef<M3eAutocompleteElement>('autocomplete');
const userName = ref<string>('');
const searchQuery = ref<string>('');
const errorMessage = ref<string>('');
const { wallpaperUrl, newtabSettings, refreshWallpaper, refreshSettings } = useNewtab();

let debounceTimer: number = -1;
let activeRequest: AbortController | null = null;
let currentFocused: string | null = null;

async function searchWebWrapper() {
	console.log(searchQuery.value);

	console.log('Current focused:', currentFocused);

	if (currentFocused) {
		try {
			await searchWeb(currentFocused);
		} catch (error) {
			console.error(error);
		}
		return;
	}

	try {
		await searchWeb(searchQuery.value);
	} catch (error) {
		console.error('Error while searching web:', error);
		errorMessage.value = (error as Error).message;
	}
}

function onQuery(e: CustomEvent<AutocompleteQueryEventDetail>) {
	if (!autocompleteEl.value) return;

	currentFocused = null;

	const term = e.detail.term.trim().toLowerCase();

	clearTimeout(debounceTimer);

	if (!term) {
		if (activeRequest) activeRequest.abort();

		autocompleteEl.value.querySelectorAll('m3e-option').forEach((x) => x.remove());
		autocompleteEl.value.hideNoData = true;
		autocompleteEl.value.loading = false;
		return;
	}

	autocompleteEl.value.loading = true;
	autocompleteEl.value.hideNoData = false;

	if (activeRequest) activeRequest.abort();

	debounceTimer = setTimeout(async () => {
		if (!autocompleteEl.value) return;

		autocompleteEl.value.querySelectorAll('m3e-option').forEach((x) => x.remove());

		const controller = new AbortController();
		activeRequest = controller;

		const suggestions = await getSearchSuggestions(term);
		suggestions.forEach((s) => {
			const option = document.createElement('m3e-option');
			option.innerText = s;
			autocompleteEl.value?.appendChild(option);
		});

		autocompleteEl.value.loading = false;

		activeRequest = null;
	}, 300);
}

function onAutcompleteChange() {
	if (!autocompleteEl.value) return;

	currentFocused = autocompleteEl.value.value;
	console.log('Current focused:', currentFocused);
}

function onKey(event: KeyboardEvent) {
	console.log('Active element:', document.activeElement);
	console.log('Search element:', searchEl.value);

	if (event.key === '/' && document.activeElement !== searchEl.value) {
		event.preventDefault();
		searchEl.value?.focus();
	}
}

onMounted(async () => {
	userName.value = await getUserName();
	await refreshWallpaper();
	await refreshSettings();

	autocompleteEl.value?.addEventListener('query', onQuery);
	autocompleteEl.value?.addEventListener('change', onAutcompleteChange);

	document.addEventListener('keydown', onKey);
});

onUnmounted(() => {
	autocompleteEl.value?.removeEventListener('query', onQuery);
	autocompleteEl.value?.removeEventListener('change', onAutcompleteChange);

	document.removeEventListener('keydown', onKey);
});
</script>

<template>
	<div class="newtab-wrapper" :class="wallpaperUrl !== null ? 'bg-wallpaper' : ''">
		<div class="center-wrapper">
			<div class="wrapper-bg"></div>
			<h1>
				{{
					newtabSettings?.greetingText
						? newtabSettings.greetingText
						: `Hello, ${userName}!`
				}}
			</h1>
			<m3e-form-field
				class="search-bar"
				variant="outlined"
				@keydown.enter.prevent="searchWebWrapper()"
			>
				<label slot="label">Search the web</label>
				<input v-model="searchQuery" id="search-fld" ref="searchInput" />
				<m3e-icon slot="prefix" name="search"></m3e-icon>
				<label slot="hint">{{ errorMessage }}</label>
			</m3e-form-field>
			<m3e-autocomplete for="search-fld" ref="autocomplete">
				<m3e-loading-indicator slot="loading"></m3e-loading-indicator>
			</m3e-autocomplete>
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
