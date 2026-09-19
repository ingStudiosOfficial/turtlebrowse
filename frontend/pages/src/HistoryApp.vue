<script setup lang="ts">
import '@m3e/web/list';
import '@m3e/web/card';
import '@m3e/web/icon-button';
import '@m3e/web/icon';
import { onMounted } from 'vue';
import { navigateToSite } from './utils/java_bridge';
import { useHistory } from './composables/history';

const { history, refreshHistory, removeFromHistory, fetchMore } = useHistory();

onMounted(async () => {
	await refreshHistory();
});
</script>

<template>
	<div class="history-wrapper">
		<h1 class="history-header">History</h1>
		<m3e-card class="history-card">
			<m3e-action-list slot="content" variant="segmented">
				<m3e-list-action
					v-for="item in history"
					:key="item.id"
					@click="navigateToSite(item.url)"
				>
					<span class="item-text">{{ item.title }}</span>
					<span slot="supporting-text" class="item-text">{{ item.url }}</span>
					<m3e-icon-button slot="trailing" @click.stop="removeFromHistory(item.id)">
						<m3e-icon name="delete"></m3e-icon>
					</m3e-icon-button>
				</m3e-list-action>
				<m3e-list-action @click="fetchMore()"> Fetch more </m3e-list-action>
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
	overflow-y: scroll;
}

.history-card {
	width: clamp(50%, 1200px - 50vw, 100%);
}

.item-text {
	width: 60ch;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}
</style>
