import type { Userscript } from '@/interfaces/Userscript';
import { addUserscript, deleteUserscript, getUserscripts } from '@/utils/java_bridge';
import { computed, ref } from 'vue';

const userscripts = ref<Userscript[]>([]);
const userscriptsNotNull = computed(() => {
	return userscripts.value.filter((u) => u.meta && u.meta.name);
});

async function refresh() {
	userscripts.value = (await getUserscripts()) || [];
	console.log('Userscripts:', userscripts.value);
}

async function newUserscript(): Promise<Userscript> {
	const script = generateDefaultUserscript();
	userscripts.value.push(script);
	await addUserscript(script);
	return script;
}

async function upsertUserscript(userscript: Userscript) {
	await addUserscript(userscript);
	await refresh();
}

async function removeUserscript(userscript: Userscript) {
	await deleteUserscript(userscript);
	await refresh();
}

function generateDefaultUserscript(): Userscript {
	return {
		meta: {
			name: ['Example userscript'],
			namespace: ['https://yourgitrepo.com'],
			version: ['0.0.1'],
			description: ['Your userscript description'],
			author: ['Your name'],
			match: ['*://*/*'],
			license: ['Apache-2.0; https://www.apache.org/licenses/LICENSE-2.0'],
			'run-at': ['document-start'],
		},
		metablock: generateDefaultMetablock(),
		content: generateDefaultContent(),
		filename: `${crypto.randomUUID()}.user.js`,
	};
}

function generateDefaultContent(): string {
	return `
${generateDefaultMetablock()}

(function() {
	'use strict';
	console.log('Hello, userscripts!');
})();
	`;
}

function generateDefaultMetablock(): string {
	return `// ==UserScript==
// @name                Example userscript
// @namespace           https://yourgitrepo.com
// @version             0.0.1
// @description         Your userscript description
// @author              Your name
// @match               https://example.com/*
// @license             Apache-2.0; https://www.apache.org/licenses/LICENSE-2.0
// ==/UserScript==`;
}

export function useUserscripts() {
	return {
		userscripts,
		userscriptsNotNull,
		refresh,
		newUserscript,
		upsertUserscript,
		removeUserscript,
	};
}
