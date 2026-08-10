import { defineCollection, defineContentConfig, z } from '@nuxt/content';

export default defineContentConfig({
	collections: {
		docs: defineCollection({
			type: 'page',
			source: 'blog/**/*.md',
			schema: z.object({
				title: z.string(),
				description: z.string().optional(),
				date: z.string().optional(),
			}),
		}),
	},
});
