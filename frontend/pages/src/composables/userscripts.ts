import type { Userscript } from '@/interfaces/Userscript';
import { addUserscript, getUserscripts } from '@/utils/java_bridge';
import { ref } from 'vue';

const userscripts = ref<Userscript[]>([]);

async function refresh() {
	userscripts.value = (await getUserscripts()) || [];
	console.log('Userscripts:', userscripts.value);
}

async function newUserscript() {
	const script = await addUserscript();
	if (script) {
		userscripts.value.push(script);
	}
}

export function useUserscripts() {
	return {
		userscripts,
		refresh,
		newUserscript,
	};
}
