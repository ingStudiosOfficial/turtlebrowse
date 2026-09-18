import type { HistoryItem } from '@/interfaces/HistoryItem';
import { deleteSiteFromHistory, getHistory } from '@/utils/java_bridge';
import { ref } from 'vue';

const history = ref<HistoryItem[]>([]);
const currentFetchIndex = ref<number>(0);

export function useHistory() {
	async function refreshHistory() {
		history.value = await getHistory();
	}

	async function removeFromHistory(id: string) {
		history.value = history.value.filter((h) => h.id !== id);
		await deleteSiteFromHistory(id);
	}

	async function fetchMore() {
		currentFetchIndex.value += 1;
		history.value.push(...(await getHistory(currentFetchIndex.value)));
	}

	return { history, refreshHistory, removeFromHistory, fetchMore };
}
