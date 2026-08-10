<script setup lang="ts"></script>

<template>
	<div class="oss-section-wrapper">
		<div class="section-title-container">
			<h2 class="section-title">Open-source and built</h2>
			<h2 class="section-title">with the user in mind</h2>
		</div>
		<m3e-list class="feat-list">
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<Icon name="material-symbols:code" />
				</m3e-avatar>
				<span class="feat-label">Free and open-source</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<Icon name="mdi:google-chrome" />
				</m3e-avatar>
				<span class="feat-label">Built with the Java CEF framework</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<Icon name="material-symbols:palette" />
				</m3e-avatar>
				<span class="feat-label">Material 3 Expressive user interface</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<Icon name="material-symbols:privacy-tip" />
				</m3e-avatar>
				<span class="feat-label">Always private</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<Icon name="material-symbols:robot-2" />
				</m3e-avatar>
				<span class="feat-label">Opt-in on-device agentic AI features</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<Icon name="ic:baseline-discord" />
				</m3e-avatar>
				<span class="feat-label">Connects to Discord Presence</span>
			</m3e-list-item>
		</m3e-list>
		<m3e-button variant="filled" size="medium" @click="navigateTo('/download')">
			<Icon slot="icon" name="material-symbols:download" />
			Download Turtlebrowse
		</m3e-button>
	</div>
</template>

<style scoped>
.oss-section-wrapper {
	width: 100%;
	min-height: 100svh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: flex-start;
	padding: 20px;
	box-sizing: border-box;
	gap: 30px;
}

.feat-avtr {
	--m3e-avatar-size: 3rem;
}

.feat-label {
	font-size: 1.5rem;
}

@supports (animation-timeline: scroll()) {
	.feat-list {
		will-change: transform;
		animation: featSlide ease-in-out both;
		animation-timeline: view();
	}
}

@keyframes featSlide {
	0% {
		transform: translateX(-50svw);
	}
	100% {
		transform: translateX(50svw);
	}
}
</style>
