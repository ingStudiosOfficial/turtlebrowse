<script setup lang="ts">
useSeoMeta({
	title: 'Home',
	description:
		'Download EvilBrowse, the Material You themed JVM based agentic browser made with the Java Chromium Embedded Framework. EvilBrowse is powered by the latest on device Gemma 4 models and is built by (ing) Studios.',
	ogType: 'website',
	ogUrl: 'https://turtlebrowse.ingstudios.dev',
	ogTitle: 'EvilBrowse - The Open Source Web Browser Built for the Agentic Era',
	ogDescription:
		'EvilBrowse is a Material You themed JVM based agentic browser made with the Java Chromium Embedded Framework.',
	ogImage: 'https://turtlebrowse.ingstudios.dev/logo_full_trans.png',
	ogImageWidth: 500,
	ogImageHeight: 500,
	ogSiteName: 'EvilBrowse',
});

definePageMeta({
    sitemap: {
        images: [
			{
				loc: '/logo_full_trans.png',
				title: 'EvilBrowse Logo',
			},
            {
                loc: '/newtab.png',
                title: 'EvilBrowse Preview',
            },
			{
                loc: '/ai_sidebar.png',
                title: 'EvilBrowse AI Sidebar',
            },
        ],
		videos: [
			{
				content_loc: '/agentic.mp4',
				thumbnail_loc: '/agentic_thumbnail.png',
				description: 'EvilBrowse agent in action',
				title: 'EvilBrowse Agent',
			},
		],
    },
});
</script>

<template>
	<div class="home-wrapper">
		<HeroSection />
		<AISection />
		<ThemeSection />
		<OpenSourceSection />
	</div>
</template>

<style scoped>
.home-wrapper {
	width: 100svw;
	height: auto;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: flex-start;
	padding: 20px;
	box-sizing: border-box;
	overflow-x: clip;
}
</style>
