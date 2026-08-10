<script setup lang="ts"></script>

<template>
	<div class="theme-section-wrapper">
		<div class="section-title-container">
			<h2 class="section-title">Beautiful and cohesive</h2>
			<h2 class="section-title">
				<span class="m3e-emph">Material 3 Expressive</span> themeing
			</h2>
		</div>

		<div class="personalization" style="text-align: left">
			<div class="feature-box">
				<h3 class="feature-header">Personalize Turtlebrowse to your liking</h3>
				<p class="feature-text">
					Turtlebrowse dynamically generates a Material You theme from your system accent
					color or a color of your liking.
				</p>
			</div>
			<div class="theme-combo">
				<img class="theme-img" style="--i: 1" src="/theme1.png" loading="lazy" />
				<img class="theme-img" style="--i: 2" src="/theme2.png" loading="lazy" />
				<img class="theme-img" style="--i: 3" src="/theme3.png" loading="lazy" />
				<img class="theme-img" style="--i: 4" src="/theme4.png" loading="lazy" />
			</div>
		</div>
	</div>
</template>

<style scoped>
.theme-section-wrapper {
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

.m3e-emph {
	background-color: var(--md-sys-color-primary-container);
	color: var(--md-sys-color-on-primary-container);
	padding: 10px;
	border-radius: 100px;
	font-size: 3rem;
	font-weight: 500;
}

.personalization {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	gap: 10px;
	width: 70%;
	box-sizing: border-box;
}

.theme-combo {
	width: 70%;
	position: relative;
	box-sizing: border-box;
	display: grid;
}

.theme-img {
	position: absolute;
	top: 0;
	left: 0;
	height: 100%;
	object-fit: cover;
	object-position: left center;
	border-radius: 25px;
	box-shadow: 0 0 10px 2px rgba(0, 0, 0, 0.2);
}

.feature-box {
	width: 30%;
}

@supports (animation-timeline: scroll()) {
	.personalization {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
		gap: 10px;
		width: 70%;
		box-sizing: border-box;
		will-change: transform;
		animation: browserSlideUp ease-in-out both;
		animation-timeline: view();
	}
}

@media (max-width: 768px) {
	.personalization {
		width: 90%;
		flex-direction: column;
		align-items: flex-start;
	}

	.feature-box {
		width: 100%;
	}

	.theme-combo {
		width: 100%;
	}
}

.theme-img:nth-child(1) {
	width: 25%;
	z-index: 4;
}

.theme-img:nth-child(2) {
	width: 50%;
	z-index: 3;
}

.theme-img:nth-child(3) {
	width: 75%;
	z-index: 2;
}

.theme-img:nth-child(4) {
	width: 100%;
	position: relative;
	z-index: 1;
}

@keyframes browserSlideUp {
	0% {
		transform: translateY(50svh);
	}
	35% {
		transform: translateY(0%);
	}
	65% {
		transform: translateY(0%);
	}
	100% {
		transform: translateY(-50svh);
	}
}
</style>
