<script setup lang="ts"></script>

<template>
	<div class="ai-section-wrapper">
		<div class="section-title-container">
			<h2 class="section-title">Powered by the</h2>
			<h2 class="section-title">latest <span class="gemma-emph">Gemma 4</span> models</h2>
		</div>
		<div class="ai-summary">
			<div class="feature-box" style="text-align: left">
				<h3 class="feature-header">Explore the web with Gemma</h3>
				<p class="feature-text">
					Get Gemma to summarize entire pages for you with its 128k context window (E2B
					variant).
				</p>
			</div>
			<img class="ai-summary-img" src="/ai_sidebar.png" loading="lazy" />
		</div>
		<div class="agentic-ai">
			<video class="agentic-ai-img" src="/agentic.mp4" loading="lazy" autoplay muted loop />
			<div class="feature-box" style="text-align: right">
				<h3 class="feature-header">Automate everyday tasks with Gemma</h3>
				<p class="feature-text">
					Use Gemma 4's native tool calling and multimodal input capabilities to browse
					the web for you. Gemma can take screenshots, page snapshots, click buttons, and
					more to understand and navigate the page.
				</p>
			</div>
		</div>
	</div>
</template>

<style scoped>
.ai-section-wrapper {
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

.gemma-emph {
	background: var(--md-sys-color-primary-container);
	background: linear-gradient(142deg, rgba(9, 107, 90, 1) 0%, rgba(189, 207, 71, 1) 100%);
	color: var(--md-sys-color-on-primary);
	padding: 10px;
	border-radius: 100px;
	font-size: 3rem;
	font-weight: 500;
}

.ai-summary,
.agentic-ai {
	width: 70%;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	gap: 10px;
}

.ai-summary-img,
.agentic-ai-img {
	min-width: 70%;
}

@supports (animation-timeline: scroll()) {
	.ai-summary {
		will-change: transform;
		animation: aiSidebarSlide ease-in-out both;
		animation-timeline: view();
	}

	.agentic-ai {
		will-change: transform;
		animation: aiSidebarSlideReverse ease-in-out both;
		animation-timeline: view();
	}
}

@media (max-width: 768px) {
	.ai-summary,
	.agentic-ai {
		flex-direction: column;
		width: 90%;
	}

	.ai-summary {
		align-items: flex-start;
	}

	.agentic-ai {
		align-items: flex-end;
	}

	.ai-summary-img,
	.agentic-ai-img {
		width: 100%;
	}
}

@keyframes aiSidebarSlide {
	0% {
		transform: translateX(-50svw);
	}
	100% {
		transform: translateX(50svw);
	}
}

@keyframes aiSidebarSlideReverse {
	0% {
		transform: translateX(50svw);
	}
	100% {
		transform: translateX(-50svw);
	}
}
</style>
