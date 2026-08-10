<script setup lang="ts">
import '@m3e/web/list';
import '@m3e/web/card';
import '@m3e/web/avatar';

useSeoMeta({
	title: 'Download | Turtlebrowse - The Open Source Web Browser Built for the Agentic Era',
	description:
		'Download Turtlebrowse for Debian Linux, Ubuntu Linux, Fedora Linux, openSUSE Linux, Windows 10, Windows 11, and macOS. Built by (ing) Studios.',
	ogType: 'website',
	ogUrl: 'https://turtlebrowse.ingstudios.dev/download',
	ogTitle: 'Download | Turtlebrowse - The Open Source Web Browser Built for the Agentic Era',
	ogDescription:
		'Download Turtlebrowse for Debian Linux, Ubuntu Linux, Fedora Linux, openSUSE Linux, Windows 10, Windows 11, and macOS.',
	ogImage: 'https://turtlebrowse.ingstudios.dev/logo_full_trans.png',
});

const RELEASE_VERSION = 'latest';

function download(url: string) {
	window.open(
		`https://github.com/ingStudiosOfficial/turtlebrowse/releases/${RELEASE_VERSION}/download/turtlebrowse_${url}`,
		'_self',
	);
}
</script>

<template>
	<div class="download-wrapper">
		<h1 class="download-title">Download Turtlebrowse (Latest)</h1>
		<p>Download Turtlebrowse for your respective operating system and CPU architecture.</p>
		<m3e-card>
			<m3e-action-list slot="content" variant="segmented">
				<m3e-list-action @click="download('debian_amd64.deb')">
					<m3e-avatar slot="leading">
						<i class="devicon-debian-plain" />
					</m3e-avatar>
					Debian/Ubuntu Linux amd64</m3e-list-action
				>
				<m3e-list-action @click="download('debian_arm64.deb')">
					<m3e-avatar slot="leading">
						<i class="devicon-debian-plain" />
					</m3e-avatar>
					Debian/Ubuntu Linux arm64
				</m3e-list-action>
				<m3e-list-action @click="download('fedora_amd64.rpm')">
					<m3e-avatar slot="leading">
						<i class="devicon-fedora-plain" />
					</m3e-avatar>
					Fedora/openSUSE Linux amd64
				</m3e-list-action>
				<m3e-list-action @click="download('fedora_arm64.rpm')">
					<m3e-avatar slot="leading">
						<i class="devicon-fedora-plain" />
					</m3e-avatar>
					Fedora/openSUSE Linux arm64
				</m3e-list-action>
				<m3e-list-action @click="download('windows_amd64.exe')">
					<m3e-avatar slot="leading">
						<i class="devicon-windows11-original" />
					</m3e-avatar>
					Windows 10/11 amd64
				</m3e-list-action>
				<m3e-list-action @click="download('macos_arm64.pkg')">
					<m3e-avatar slot="leading">
						<i class="devicon-apple-original" />
					</m3e-avatar>
					macOS arm64
				</m3e-list-action>
			</m3e-action-list>
		</m3e-card>

		<p>
			Can't find your operating system? Use the
			<a
				:href="`https://github.com/ingStudiosOfficial/turtlebrowse/releases/${RELEASE_VERSION}/download/turtlebrowse_jar.jar`"
				>JAR</a
			>
			if you have JDK 25 installed on your system instead.
		</p>
	</div>
</template>

<style scoped>
.download-wrapper {
	width: 100svw;
	min-height: 100svh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	overflow-y: scroll;
	padding: 20px;
	box-sizing: border-box;
	text-align: center;
}

.download-title {
	font-size: 2.5rem;
	font-weight: 600;
	margin: 0;
	text-align: center;
}
</style>
