import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueDevTools from 'vite-plugin-vue-devtools';
import { resolve } from 'node:path';

// https://vite.dev/config/
export default defineConfig({
	plugins: [
		vue({
			template: {
				compilerOptions: {
					isCustomElement: (tag) => tag.startsWith('m3e-'),
				},
			},
		}),
		vueDevTools(),
	],
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url)),
		},
	},
	base: './',
	build: {
		outDir: '../../app/src/main/resources/web',
		emptyOutDir: true,
		rollupOptions: {
			input: {
				newtab: resolve(__dirname, 'newtab.html'),
				chat: resolve(__dirname, 'chat.html'),
				settings: resolve(__dirname, 'settings.html'),
			},
		},
	},
});
