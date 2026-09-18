<script setup lang="ts">
import '@m3e/web/list';
import '@m3e/web/card';
import { onMounted, ref } from 'vue';
import type { HistoryItem } from './interfaces/HistoryItem';
import { getHistory, navigateToSite } from './utils/java_bridge';

const history = ref<HistoryItem[]>([]);

onMounted(async () => {
	history.value = await getHistory();
});
</script>

<template>
	<div class="history-wrapper">
		<h1 class="history-header">History</h1>
		<m3e-card class="history-card">
			<m3e-action-list slot="content" variant="segmented">
				<m3e-list-action
					v-for="item in history"
					:key="item.url + item.timestamp.toString()"
					@click="navigateToSite(item.url)"
				>
					{{ item.title }}
					<span slot="supporting-text">{{ item.url }}</span>
				</m3e-list-action>
			</m3e-action-list>
		</m3e-card>
	</div>
</template>

<style scoped>
.history-header {
	margin: 16px 0;
	font-size: 2.5rem;
}

.history-wrapper {
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

.history-card {
	width: clamp(50%, 1200px - 50vw, 100%);
}
</style>
