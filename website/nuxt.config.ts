// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
	compatibilityDate: '2025-07-15',
	devtools: { enabled: true },
	modules: ['@nuxt/eslint', '@nuxtjs/seo', '@nuxt/icon'],
	css: ['@/assets/css/main.css'],

	site: {
		url: 'https://turtlebrowse.ingstudios.dev',
		name: 'Turtlebrowse - The Open Source Web Browser Built for the Agentic Era',
	},

	app: {
		head: {
			link: [
				{ rel: 'icon', type: 'image/png', href: '/logo_full_trans.png' },
			],
		},
	},

	vue: {
		compilerOptions: {
			isCustomElement: (tag) => tag.startsWith('m3e-'),
		},
	},
});
