import { promptStreaming } from '@/utils/chat';
import { M3eSnackbar } from '@m3e/web/snackbar';
import { ref } from 'vue';
import type { Conversation } from '@/interfaces/Conversation';

const prompt = ref<string>('');
const prompts = ref<Conversation[]>([]);
const isGenerating = ref<boolean>(false);

function sendPrompt(conversation: Conversation) {
	prompt.value = conversation.user;

	prompts.value.push(conversation);

	const currentConversation = prompts.value[prompts.value.length - 1] as Conversation;

	try {
		promptStreaming(
			prompt.value,
			(chunk) => {
				console.log('Received chunk:', chunk);
				currentConversation.assistant.thinking += chunk;
			},
			(chunk) => {
				console.log('Received chunk:', chunk);
				currentConversation.assistant.response += chunk;
			},
			(response) => {
				currentConversation.assistant.response = response;
				isGenerating.value = false;
			},
		);

		prompt.value = '';
	} catch (error) {
		console.error('An error occurred while generating:', error);
		M3eSnackbar.open((error as Error).message, {
			duration: 4000,
		});
		isGenerating.value = false;
	}
}

function sendPromptKeyboard(event: KeyboardEvent) {
	if (event.key.toLowerCase() !== 'enter' || event.shiftKey) return;

	event.preventDefault();

	isGenerating.value = true;

	const conversation: Conversation = {
		key: window.crypto.randomUUID(),
		user: prompt.value,
		assistant: {
			thinking: '',
			response: '',
		},
	};

	sendPrompt(conversation);
}

export function usePrompt() {
	return {
		prompt,
		prompts,
		isGenerating,
		sendPrompt,
		sendPromptKeyboard,
	};
}
