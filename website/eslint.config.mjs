// @ts-check
import withNuxt from './.nuxt/eslint.config.mjs';

export default withNuxt({
	name: 'app/custom-vue-rules',
	files: ['**/*.vue'],
	rules: {
		'vue/no-deprecated-slot-attribute': 'off',
		'vue/html-self-closing': [
			'error',
			{
				html: {
					void: 'always',
					normal: 'always',
					component: 'always',
				},
			},
		],
	},
});
