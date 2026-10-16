<script setup lang="ts">
import '@m3e/web/form-field';
import '@m3e/web/textarea-autosize';
import '@m3e/web/icon-button';
import '@m3e/web/icon';
import ConversationBubble from './ConversationBubble.vue';
import { usePrompt } from '@/composables/prompt';
import { getUserName } from '@/utils/java_bridge';
import { onMounted, ref } from 'vue';

const { prompt, prompts, isGenerating, sendPromptKeyboard, sendPromptButton } = usePrompt();

const userName = ref<string>('');

onMounted(async () => {
	userName.value = await getUserName();
});
</script>

<template>
	<div class="conv-wrapper">
		<div v-if="prompts.length !== 0" class="conv-box">
			<div v-for="conv in prompts" :key="conv.key" class="conv-group">
				<ConversationBubble sender="user" :message="conv.user"></ConversationBubble>
				<ConversationBubble
					sender="assistant"
					:thinking="conv.assistant.thinking"
					:message="conv.assistant.response"
				></ConversationBubble>
			</div>
		</div>
		<div v-else class="greet-box">
			<h1>What's on your mind today, {{ userName }}?</h1>
		</div>

		<div class="bottom">
			<m3e-form-field class="form-field" variant="outlined" :disabled="isGenerating">
				<label slot="label" for="prompt-field">Ask AI</label>
				<input id="prompt-field" v-model="prompt" @keydown="sendPromptKeyboard" />
			</m3e-form-field>
			<m3e-textarea-autosize for="prompt-field" max-rows="6"></m3e-textarea-autosize>
			<m3e-icon-button variant="filled" :disabled="isGenerating" @click="sendPromptButton()">
				<m3e-icon name="send"></m3e-icon>
			</m3e-icon-button>
		</div>
	</div>
</template>

<style scoped>
.conv-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	padding: 20px;
	gap: 20px;
}

.conv-box {
	flex-grow: 1;
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
	overflow: scroll;
}

.greet-box {
	flex-grow: 1;
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	text-align: center;
}

.conv-group {
	display: flex;
	flex-direction: column;
	width: 100%;
}

.form-field {
	flex-grow: 1;
}

.bottom {
	display: flex;
	flex-direction: row;
	flex-shrink: 0;
	width: 100%;
	align-items: center;
	gap: 16px;
}

.send-button {
	flex-shrink: 0;
}
</style>
