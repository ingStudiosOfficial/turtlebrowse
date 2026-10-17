import type { Userscript } from '@/interfaces/Userscript';
import { ref } from 'vue';

const userscripts = ref<Userscript[]>([
	{
		meta: {
			name: 'Sample Userscript',
			icon: 'https://ingstudios.dev/logo.png',
		},
		metablock: '',
		content: 'async function() {}',
	},
]);

export function useUserscripts() {
	return {
		userscripts,
	};
}
