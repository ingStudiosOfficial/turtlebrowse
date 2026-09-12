<script setup lang="ts">
import '@m3e/web/expansion-panel';
import { parseMessage } from '@/utils/parse';
import { ref, watch } from 'vue';
import '@m3e/web/loading-indicator';

interface ComponentProps {
	message: string;
	thinking?: string;
	sender: 'user' | 'assistant';
}

const props = defineProps<ComponentProps>();

const messageMessage = ref<string>('');
const thinkingMessage = ref<string | null>(null);

watch(
	() => props.message,
	async (message) => {
		messageMessage.value = await parseMessage(message, props.sender);
	},
	{ immediate: true },
);

watch(
	() => props.thinking,
	async (thinking) => {
		if (!thinking) return;
		thinkingMessage.value = await parseMessage(thinking, props.sender);
	},
	{ immediate: true },
);
</script>

<template>
	<div class="thinking" v-if="!props.message && !props.thinking">
		<m3e-loading-indicator variant="contained"></m3e-loading-indicator>
		<p>Thinking</p>
	</div>
	<div v-else class="conv-bubble" :class="props.sender === 'user' ? 'user-message' : ''">
		<m3e-expansion-panel
			v-if="props.thinking && props.message"
			class="think-expand"
			toggle-position="before"
			toggle-direction="horizontal"
		>
			<span slot="header">Show thinking</span>
			<div class="conv-thinking" v-html="thinkingMessage"></div>
		</m3e-expansion-panel>
		<div v-else-if="props.thinking" class="conv-thinking" v-html="thinkingMessage"></div>

		<div class="conv-message" v-html="messageMessage"></div>
	</div>
</template>

<style scoped>
.conv-bubble {
	min-width: 0;
	display: flex;
	flex-direction: column;
	align-items: v-bind("props.sender === 'user' ? 'flex-end' : 'flex-start'");
	width: fit-content;
	max-width: 50%;
	padding: 10px;
	gap: 10px;
	box-sizing: border-box;
	border-radius: 25px;
	background-color: v-bind(
		"props.sender === 'user' ? 'var(--md-sys-color-primary-container)' : 'transparent'"
	);
	align-self: v-bind("props.sender === 'user' ? 'flex-end' : 'flex-start'");
	color: v-bind(
		"props.sender === 'user' ? 'var(--md-sys-color-on-primary-container)' : 'var(--md-sys-color-on-surface)'"
	);
}

.thinking {
	box-sizing: border-box;
	gap: 15px;
	font-size: 1.2rem;
	display: flex;
	flex-direction: row;
	align-items: center;
	background: linear-gradient(
		to right,
		var(--md-sys-color-primary),
		var(--md-sys-color-primary-container),
		var(--md-sys-color-secondary),
		var(--md-sys-color-secondary-container),
		var(--md-sys-color-tertiary),
		var(--md-sys-color-tertiary-container)
	);
	background-size: 200% auto;
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
	animation: color-flow 3s linear infinite;
}

@keyframes color-flow {
	to {
		background-position: -200% center;
	}
}

.conv-message,
.conv-thinking {
	width: 100%;
	text-align: v-bind("props.sender === 'user' ? 'right' : 'left'");
	display: -webkit-box;
	-webkit-box-orient: vertical;
	overflow-wrap: normal;
}

.conv-message *,
.conv-thinking * {
	width: 100%;
	text-align: v-bind("props.sender === 'user' ? 'right' : 'left'");
	display: -webkit-box;
	-webkit-box-orient: vertical;
	overflow-wrap: normal;
}

.conv-message {
	font-size: 1rem;
}

.conv-thinking {
	color: var(--md-sys-color-on-surface-variant);
	font-size: 1rem;
}

.think-expand {
	--m3e-expansion-panel-shape: 10px;
	--m3e-expansion-panel-open-shape: 10px;
}

.user-message {
	margin: 0;
}

:deep(.message-extract) {
	background-color: var(--md-sys-color-secondary-container);
	color: var(--md-sys-color-on-secondary-container);
	padding: 10px;
	text-align: left !important;
	box-sizing: border-box;
	border-radius: 10px;
}

:deep(.message-extract span) {
	width: 100%;
	display: -webkit-box;
	-webkit-line-clamp: 4;
	-webkit-box-orient: vertical;
	overflow: hidden;
	line-clamp: 4;
	text-align: left !important;
	-webkit-mask-image: linear-gradient(to bottom, black 20%, transparent 100%);
	mask-image: linear-gradient(to bottom, black 20%, transparent 100%);
}
</style>
