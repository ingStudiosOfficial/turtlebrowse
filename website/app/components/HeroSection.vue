<script setup lang="ts">
function openGithub() {
	window.open('https://github.com/ingStudiosOfficial/turtlebrowse', '_blank');
}
</script>

<template>
	<div class="hero-wrapper">
		<img class="browser-logo" src="/logo_full_trans.png" loading="lazy" />
		<div class="hero-title-container">
			<h1 class="hero-title">The browser</h1>
			<h1 class="hero-title">built to be <span class="browser-feat">agentic</span></h1>
		</div>
		<m3e-button variant="filled" size="medium" @click="navigateTo('/download')">
			<Icon slot="icon" name="material-symbols:download" />
			Download Turtlebrowse
		</m3e-button>
		<m3e-button variant="text" size="medium" @click="openGithub()">
			<Icon slot="icon" name="mdi:github" />
			View project on GitHub
		</m3e-button>
		<img class="newtab-image" src="/newtab.png" loading="lazy" />
	</div>
</template>

<style scoped>
.hero-wrapper {
	width: 100%;
	min-height: 100svh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 20px;
	box-sizing: border-box;
	gap: 30px;
}

.hero-title-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	width: 100%;
	box-sizing: border-box;
}

.hero-title {
	font-size: 4.5rem;
	font-weight: 600;
	margin: 0;
	text-align: center;
}

.browser-logo {
	aspect-ratio: 1 / 1;
	max-width: 90%;
	width: 150px;
	height: 150px;
}

.browser-feat {
	background-color: var(--md-sys-color-primary-container);
	color: var(--md-sys-color-on-primary-container);
	padding: 10px;
	border-radius: 100px;
	font-size: 4rem;
	font-weight: 500;
}

.newtab-image {
	width: 70%;
}

@supports (animation-timeline: scroll()) {
	.newtab-image {
		width: 70%;
		will-change: transform opacity;
		animation: imageScale ease-in both;
		animation-timeline: view();
	}
}

@media (max-width: 768px) {
	.newtab-image {
		width: 90%;
	}
}

@keyframes imageScale {
	0% {
		transform: scale(0.25);
	}
	30% {
		transform: scale(1);
	}
	70% {
		transform: scale(1);
	}
	100% {
		transform: scale(0.25);
	}
}
</style>
