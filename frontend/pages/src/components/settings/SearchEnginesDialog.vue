<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { nextTick, onMounted, ref, useTemplateRef } from 'vue';
import '@m3e/web/select';
import '@m3e/web/option';
import '@m3e/web/form-field';
import type { M3eSelectElement } from '@m3e/web/select';
import type { SearchEngine } from '@/types/SearchEngine';
import { getDefaultSearchEngine, setDefaultSearchEngine } from '@/utils/java_bridge';

const { searchEnginesDialog } = useDialog();

const dialog = useTemplateRef<M3eDialogElement>('dialog');
const searchEngine = ref<SearchEngine>('brave');

const defaultSearchEngines: { name: string; value: SearchEngine }[] = [
	{
		name: 'Brave Search',
		value: 'brave',
	},
	{
		name: 'DuckDuckGo',
		value: 'ddg',
	},
	{
		name: 'DuckDuckGo (no AI)',
		value: 'ddg-noai',
	},
	{
		name: 'Google',
		value: 'google',
	},
	{
		name: 'Startpage',
		value: 'startpage',
	},
	/*
	{
		name: 'Kagi',
		value: 'kagi',
	},
	*/
	{
		name: 'Yahoo',
		value: 'yahoo',
	},
	{
		name: 'Vyntr',
		value: 'vyntr',
	},
	{
		name: 'Microsoft Bing',
		value: 'bing',
	},
	/*
	{
		name: 'Custom',
		value: 'custom',
	},
	*/
];

async function changeSearchEngine(target: M3eSelectElement) {
	await nextTick();

	const name = target.value as SearchEngine;
	console.log('Changed search engine to:', name);

	searchEngine.value = name;

	await setDefaultSearchEngine(name);
}

onMounted(async () => {
	searchEnginesDialog.value = dialog.value;

	searchEngine.value = await getDefaultSearchEngine();
	console.log('Default search engine:', searchEngine.value);
});
</script>

<template>
	<m3e-dialog ref="dialog" dismissible>
		<span slot="header">Search engines</span>
		<div class="search-engine-dialog">
			<m3e-form-field>
				<label slot="label">Default search engine</label>
				<m3e-select :key="searchEngine" @change="changeSearchEngine($event.target)">
					<m3e-option v-for="engine in defaultSearchEngines" :key="engine.value" :value="engine.value" :selected="engine.value === searchEngine">
						{{ engine.name }}
					</m3e-option>
				</m3e-select>
			</m3e-form-field>
			<m3e-form-field v-if="searchEngine === 'custom'">
				<label slot="label">Search engine URL</label>
				<input />
			</m3e-form-field>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.search-engine-dialog {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8px;
	box-sizing: border-box;
	padding: 8px;
}
</style>
