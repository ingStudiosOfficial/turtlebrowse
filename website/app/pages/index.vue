<script setup lang="ts">
useSeoMeta({
	title: 'Turtlebrowse - The Open Source Web Browser Built for the Agentic Era',
	description:
		'Download Turtlebrowse, the Material You themed JVM based agentic browser made with the Java Chromium Embedded Framework. Turtlebrowse is powered by the latest on device Gemma 4 models and is built by (ing) Studios.',
	ogType: 'website',
	ogUrl: 'https://turtlebrowse.ingstudios.dev',
	ogTitle: 'Turtlebrowse - The Open Source Web Browser Built for the Agentic Era',
	ogDescription:
		'Turtlebrowse is a Material You themed JVM based agentic browser made with the Java Chromium Embedded Framework.',
	ogImage: 'https://turtlebrowse.ingstudios.dev/logo_full_trans.png',
	ogImageWidth: 400,
	ogImageHeight: 400,
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
