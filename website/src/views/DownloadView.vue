<script setup lang="ts">

import '@m3e/web/dialog';
import '@m3e/web/list';
import '@m3e/web/card';
import '@m3e/web/avatar';

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
        <m3e-dialog id="macDialog">
            <span slot="header">macOS Support</span>
            Turtlebrowse for macOS is <strong>Experimental</strong>.
            <p>
                Some of the Java configurations used by Turtlebrowse 
                are not fully compatible with macOS, which will cause:
            </p>
            <ul style="text-align: left">
                <li>Ollama and Discord not connecting up with the local API</li>
                <li>Keyboard Shortcuts not working</li>
                <li>Performance issues</li>
            </ul>
            <p>
                Windows and Linux builds are fully supported while we 
                investigate the issue.
            </p>
            <div slot="actions" end>
                <m3e-button><m3e-dialog-action return-value="ok">Download anyway</m3e-dialog-action><m3e-dialog-trigger for="macDownloadAnywayDialog"></m3e-dialog-trigger></m3e-button>
                <m3e-button variant="filled" autofocus><m3e-dialog-action return-value="ok">Understood</m3e-dialog-action></m3e-button>
            </div>
        </m3e-dialog>
        <m3e-dialog id="macDownloadAnywayDialog">
            <span slot="header">Download anyway?</span>
            <p>
                This build is <strong>experimental</strong> and is currently known
                to have issues on macOS.
            </p>

            <p>It will:</p>

            <ul style="text-align:left">
                <li>Fail to launch local AI model and</li>
                <li>Have no AI Features due to Ollama failing</li>
                <li>Have no Discord rich presence</li>
                <li>Cause lag and stutters on older hardware</li>
                <li>Require manual debugging</li>
            </ul>

            <p>
                Download this build only if you're comfortable testing 
                unfinished software.
            </p>
            <div slot="actions" end>
                <m3e-button>
                    <m3e-dialog-action return-value="cancel" autofocus>
                        Cancel
                    </m3e-dialog-action>
                </m3e-button>

                <m3e-button @click="download('macos_arm64.pkg')" variant="filled">
                    <m3e-dialog-action return-value="download">
                        Download anyway
                    </m3e-dialog-action>
                </m3e-button>
            </div>
        </m3e-dialog>
        <h1 class="download-title">Download Turtlebrowse (Latest)</h1>
        <p>Download Turtlebrowse for your respective operating system and CPU architecture.</p>
        <m3e-card>
            <m3e-action-list slot="content" variant="segmented">
                <m3e-list-action @click="download('debian_amd64.deb')">
                    <m3e-avatar slot="leading">
                        <i class="devicon-debian-plain"></i>
                    </m3e-avatar>
                    Debian/Ubuntu Linux amd64</m3e-list-action
                >
                <m3e-list-action @click="download('debian_arm64.deb')">
                    <m3e-avatar slot="leading">
                        <i class="devicon-debian-plain"></i>
                    </m3e-avatar>
                    Debian/Ubuntu Linux arm64
                </m3e-list-action>
                <m3e-list-action @click="download('fedora_amd64.rpm')">
                    <m3e-avatar slot="leading">
                        <i class="devicon-fedora-plain"></i>
                    </m3e-avatar>
                    Fedora/openSUSE Linux amd64
                </m3e-list-action>
                <m3e-list-action @click="download('fedora_arm64.rpm')">
                    <m3e-avatar slot="leading">
                        <i class="devicon-fedora-plain"></i>
                    </m3e-avatar>
                    Fedora/openSUSE Linux arm64
                </m3e-list-action>
                <m3e-list-action @click="download('windows_amd64.exe')">
                    <m3e-avatar slot="leading">
                        <i class="devicon-windows11-original"></i>
                    </m3e-avatar>
                    Windows 10/11 amd64
                </m3e-list-action>
                <m3e-list-action>
                    <m3e-dialog-trigger for="macDialog"></m3e-dialog-trigger>
                    <m3e-avatar slot="leading">
                        <i class="devicon-apple-original"></i>
                    </m3e-avatar>
                    macOS arm64 (Experimental)
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