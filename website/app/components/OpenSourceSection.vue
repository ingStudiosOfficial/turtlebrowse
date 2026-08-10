<script setup lang="ts">
import '@m3e/web/list';
import '@m3e/web/avatar';
import '@m3e/web/icon';
import '@m3e/web/button';
import '@fortawesome/fontawesome-free/css/all.min.css';
</script>

<template>
	<div class="oss-section-wrapper">
		<div class="section-title-container">
			<h2 class="section-title">Open-source and built</h2>
			<h2 class="section-title">with the user in mind</h2>
		</div>
		<m3e-list class="feat-list">
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<m3e-icon name="code" />
				</m3e-avatar>
				<span class="feat-label">Free and open-source</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<i class="fa-brands fa-chrome" />
				</m3e-avatar>
				<span class="feat-label">Built with the Java CEF framework</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<m3e-icon name="colors" />
				</m3e-avatar>
				<span class="feat-label">Material 3 Expressive user interface</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<m3e-icon name="privacy_tip" />
				</m3e-avatar>
				<span class="feat-label">Always private</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<m3e-icon name="robot_2" />
				</m3e-avatar>
				<span class="feat-label">Opt-in on-device agentic AI features</span>
			</m3e-list-item>
			<m3e-list-item>
				<m3e-avatar slot="leading" class="feat-avtr">
					<i class="fa-brands fa-discord" />
				</m3e-avatar>
				<span class="feat-label">Connects to Discord Presence</span>
			</m3e-list-item>
		</m3e-list>
		<m3e-button variant="filled" size="medium" @click="navigateTo('/download')">
			<m3e-icon slot="icon" name="download" />
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
