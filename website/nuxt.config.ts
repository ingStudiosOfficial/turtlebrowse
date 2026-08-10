// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
	compatibilityDate: '2025-07-15',
	devtools: { enabled: true },
	modules: ['@nuxt/content', '@nuxt/eslint', '@nuxtjs/seo'],
	css: ['@/assets/css/main.css'],

	site: {
		url: 'https://turtlebrowse.ingstudios.dev',
		name: 'Turtlebrowse - The Open Source Web Browser Built for the Agentic Era',
	},

	app: {
		head: {
			link: [
				{ rel: 'icon', type: 'image/png', href: '/logo_full_trans.png' },
				{
					rel: 'stylesheet',
					type: 'text/css',
					href: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/devicon.min.css',
				},
			],
		},
	},

	build: {
		transpile: ['@m3e/icons'],
	},

	nitro: {
		experimental: {
			wasm: true,
		},
	},
});
