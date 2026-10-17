<script setup lang="ts">
import '@m3e/web/list';
import '@m3e/web/card';
import '@m3e/web/button';
import '@m3e/web/icon';
import '@m3e/web/dialog';
import '@m3e/web/avatar';
import { ref, useTemplateRef } from 'vue';
import { useUserscripts } from './composables/userscripts';
import type { Userscript } from './interfaces/Userscript';
import type { M3eDialogElement } from '@m3e/web/dialog';
import { CodeJar, type Position } from 'codejar';
import { highlightElement } from '@speed-highlight/core';
import '@speed-highlight/core/themes/default.css';

const { userscripts } = useUserscripts();

const userscriptDialog = useTemplateRef<M3eDialogElement>('userscriptDialog');
const editor = useTemplateRef<HTMLDivElement>('editor');

const currentUserscript = ref<Userscript | null>(null);

let jar: ReturnType<typeof CodeJar> | null = null;

function openDialog(userscript: Userscript) {
	currentUserscript.value = userscript;
	userscriptDialog.value?.show();

	if (editor.value && !jar) {
		jar = CodeJar(
			editor.value,
			async (editor) => {
				const pos = jar?.save();
				await highlightElement(editor, 'js');
				jar?.restore(pos as Position);
			},
			{
				tab: ' '.repeat(4),
			},
		);
	}

	if (jar && userscript) {
		jar.updateCode(userscript.content);
	}
}

function handleDialogClosed() {
	currentUserscript.value = null;
	if (jar) {
		jar.destroy();
		jar = null;
	}
}
</script>

<template>
	<div class="history-wrapper">
		<h1 class="history-header">Userscripts</h1>
		<div class="content">
			<m3e-button variant="text">
				<m3e-icon slot="icon" name="add"></m3e-icon>
				Add userscript
			</m3e-button>
			<m3e-card>
				<m3e-action-list slot="content" variant="segmented">
					<m3e-list-action
						v-for="script in userscripts"
						:key="script.meta.namespace"
						@click="openDialog(script)"
					>
						<m3e-avatar slot="leading">
							<img
								v-if="script.meta.icon && script.meta.icon.trim().length !== 0"
								:src="script.meta.icon"
							/>
							<span v-else>{{ script.meta.name[0] }}</span>
						</m3e-avatar>
						{{ script.meta.name }}
					</m3e-list-action>
				</m3e-action-list>
			</m3e-card>

			<m3e-dialog ref="userscriptDialog" @closed="handleDialogClosed()">
				<span slot="header">{{ currentUserscript?.meta.name }}</span>
				<div ref="editor" class="code-editor"></div>
			</m3e-dialog>
		</div>
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

.content {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16px;
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

.code-editor {
	font-family: monospace;
	min-height: 300px;
	overflow-y: auto;
	padding: 16px;
	white-space: pre-wrap;
	border-radius: 16px;
}
</style>
