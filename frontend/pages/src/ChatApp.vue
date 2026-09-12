<script setup lang="ts">
import ConversationComponent from './components/chat/ConversationComponent.vue';
</script>

<template>
	<div class="chat-wrapper">
		<ConversationComponent></ConversationComponent>
	</div>
</template>

<style scoped>
.chat-wrapper {
	display: flex;
	flex-direction: row;
	width: 100dvw;
	height: 100dvh;
}
</style>
