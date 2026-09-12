import type { AISettings } from '@/interfaces/AISettings';
import type { NewtabSettings } from '@/interfaces/NewtabSettings';
import type { SearchEngine } from '@/types/SearchEngine';

async function communicateWithBackend(
	request: string,
	params?: Record<string, string>,
): Promise<Response> {
	const response = await fetch(`turtlebrowse://api/${request}`, {
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
