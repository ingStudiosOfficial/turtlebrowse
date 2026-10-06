import type { AISettings } from '@/interfaces/AISettings';
import type { HistoryItem } from '@/interfaces/HistoryItem';
import type { NewtabSettings } from '@/interfaces/NewtabSettings';
import type { UpdateInfo } from '@/interfaces/UpdateInfo';
import type { Appearance } from '@/types/Appearance';
import type { SearchEngine } from '@/types/SearchEngine';
import type { TabPosition } from '@/types/TabPosition';

async function communicateWithBackend(
	request: string,
	params?: Record<string, string>,
): Promise<Response> {
	const response = await fetch(`evilbrowse://api/${request}`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(params),
	});
	return response;
}

export async function fetchFromJavaText(
	request: string,
	params?: Record<string, string>,
): Promise<string | void> {
	const response = await communicateWithBackend(request, params);
	return response.text();
}

export async function fetchFromJavaJson(
	request: string,
	params?: Record<string, string>,
): Promise<unknown> {
	const response = await communicateWithBackend(request, params);
	return await response.json();
}

export async function getUserName(): Promise<string> {
	try {
		const name = await fetchFromJavaText('GET_NAME');
		return name || 'Guest';
	} catch (error) {
		console.error('Error while fetching name:', error);
		return 'Guest';
	}
}

export async function searchWeb(query: string) {
	await fetchFromJavaText('SEARCH_WEB', { query: query });
}

export async function getTheme(): Promise<string | undefined> {
	try {
		return (await fetchFromJavaText('GET_THEME')) as string | undefined;
	} catch (error) {
		console.error('Error while getting theme:', error);
		return undefined;
	}
}

export async function getDefaultSearchEngine(): Promise<SearchEngine> {
	try {
		const searchEngine = (await fetchFromJavaText('GET_SEARCH_ENGINE')) as
			| SearchEngine
			| undefined;

		if (!searchEngine) {
			return 'brave';
		}

		return searchEngine;
	} catch (error) {
		console.error('Error while getting default search engine:', error);
		return 'brave';
	}
}

export async function setDefaultSearchEngine(engine: SearchEngine) {
	try {
		await fetchFromJavaText('SET_SEARCH_ENGINE', { engine: engine });
	} catch (error) {
		console.error('Failed to set search engine:', error);
	}
}

export async function getDiscordPresenceSetting(): Promise<boolean> {
	try {
		const enabled = (await fetchFromJavaText('GET_DISCORD_SETTING')) as string | undefined;

		if (enabled === undefined) {
			return false;
		}

		return enabled.toLowerCase() === 'true';
	} catch (error) {
		console.error('Error while getting Discord setting:', error);
		return false;
	}
}

export async function setDiscordPresenceSetting(enabled: boolean) {
	try {
		await fetchFromJavaText('SET_DISCORD_SETTING', { enabled: enabled.toString() });
	} catch (error) {
		console.error('Failed to set Discord setting:', error);
	}
}

export async function getDiscordClientId(): Promise<string> {
	try {
		const raw = (await fetchFromJavaText('GET_DISCORD_CLIENT_ID')) as string | undefined;
		return raw ?? '';
	} catch {
		return '';
	}
}

export async function setDiscordClientId(clientId: string) {
	try {
		await fetchFromJavaText('SET_DISCORD_CLIENT_ID', { clientId });
	} catch (error) {
		console.error('Failed to set Discord client ID:', error);
	}
}

export async function getAppearance(): Promise<Appearance> {
	try {
		const appearance = (await fetchFromJavaText('GET_APPEARANCE')) as Appearance | undefined;

		if (appearance === undefined) {
			return 'system';
		}

		return appearance;
	} catch (error) {
		console.error('Error while getting dark mode setting:', error);
		return 'system';
	}
}

export async function setAppearance(appearance: Appearance) {
	try {
		await fetchFromJavaText('SET_APPEARANCE', { theme: appearance });
	} catch (error) {
		console.error('Failed to set dark mode setting:', error);
	}
}

export async function getAISettings(): Promise<AISettings> {
	try {
		const settingsString = (await fetchFromJavaText('GET_AI_SETTINGS')) as string | undefined;
		if (!settingsString) {
			return {
				enabled: false,
				model: 'gemma4:e2b',
			};
		}

		const settings = JSON.parse(settingsString);

		return settings;
	} catch (error) {
		console.error('Error while getting AI settings:', error);
		return {
			enabled: false,
			model: 'gemma4:e2b',
		};
	}
}

export async function setAISettings(settings: AISettings) {
	try {
		await fetchFromJavaText('SET_AI_SETTINGS', {
			enabled: settings.enabled.toString(),
			model: settings.model,
		});
	} catch (error) {
		console.error('Failed to set AI setting:', error);
	}
}

export async function getNewtabSettings(): Promise<NewtabSettings> {
	try {
		const settingsString = (await fetchFromJavaText('GET_NEWTAB_SETTINGS')) as
			| string
			| undefined;
		if (!settingsString) {
			return {
				greetingText: '',
			};
		}

		const settings = JSON.parse(settingsString);
		console.log('Newtab settings:', settings);

		return settings;
	} catch (error) {
		console.error('Error while getting New Tab settings:', error);
		return {
			greetingText: '',
		};
	}
}

export async function setNewtabSettings(settings: NewtabSettings) {
	try {
		await fetchFromJavaText('SET_NEWTAB_SETTINGS', { greetingText: settings.greetingText });
	} catch (error) {
		console.error('Failed to set New Tab setting:', error);
	}
}

export async function getSearchSuggestions(query: string): Promise<string[]> {
	try {
		const suggestions: string[] = (await fetchFromJavaJson('AUTOCOMPLETER', {
			query: query,
		})) as string[];
		return suggestions;
	} catch (error) {
		console.error(error);
		return [];
	}
}

export async function getHistory(index = 0): Promise<HistoryItem[]> {
	try {
		const history = (await fetchFromJavaJson('GET_HISTORY', {
			index: index.toString(),
		})) as HistoryItem[];
		return history;
	} catch (error) {
		console.error(error);
		return [];
	}
}

export async function navigateToSite(url: string) {
	console.log('URL:', url);

	try {
		await fetchFromJavaText('NAVIGATE_TO_SITE', {
			url: url,
		});
	} catch (error) {
		console.error(error);
	}
}

export async function deleteSiteFromHistory(id: string) {
	try {
		await fetchFromJavaText('DELETE_HISTORY_ITEM', {
			id: id,
		});
	} catch (error) {
		console.error(error);
	}
}

export async function getUpdateInfo(refresh: boolean = false): Promise<UpdateInfo | null> {
	try {
		const info = (await fetchFromJavaJson('GET_UPDATE_INFO', {
			refresh: refresh.toString(),
		})) as UpdateInfo;
		return info;
	} catch (error) {
		console.error(error);
		return null;
	}
}

export async function getSidebarCollapsed(): Promise<boolean> {
	try {
		const raw = (await fetchFromJavaText('GET_SIDEBAR_COLLAPSED')) as string | undefined;
		return raw?.toLowerCase() === 'true';
	} catch {
		return false;
	}
}

export async function setSidebarCollapsed(collapsed: boolean) {
	try {
		await fetchFromJavaText('SET_SIDEBAR_COLLAPSED', { collapsed: collapsed.toString() });
	} catch (error) {
		console.error(error);
	}
}

export async function getTabPosition(): Promise<TabPosition> {
	try {
		const raw = (await fetchFromJavaText('GET_TAB_POSITION')) as string | undefined;
		return raw === 'horizontal' ? 'horizontal' : 'vertical';
	} catch {
		return 'vertical';
	}
}

export async function setTabPosition(position: TabPosition) {
	try {
		await fetchFromJavaText('SET_TAB_POSITION', { position });
	} catch (error) {
		console.error(error);
	}
}

export async function getAdblockEnabled(): Promise<boolean> {
	try {
		const raw = (await fetchFromJavaText('GET_ADBLOCK_ENABLED')) as string | undefined;
		return raw?.toLowerCase() !== 'false';
	} catch {
		return true;
	}
}

export async function setAdblockEnabled(enabled: boolean) {
	try {
		await fetchFromJavaText('SET_ADBLOCK_ENABLED', { enabled: enabled.toString() });
	} catch (error) {
		console.error(error);
	}
}

export interface AdblockStatus {
	enabled: boolean;
	rules: number;
	blocked: number;
	lastUpdate: number;
	lists: string[];
}

export async function getAdblockStatus(): Promise<AdblockStatus | null> {
	try {
		return (await fetchFromJavaJson('GET_ADBLOCK_STATUS')) as AdblockStatus;
	} catch {
		return null;
	}
}

export async function refreshFilterLists() {
	try {
		await fetchFromJavaText('REFRESH_FILTER_LISTS');
	} catch (error) {
		console.error(error);
	}
}
