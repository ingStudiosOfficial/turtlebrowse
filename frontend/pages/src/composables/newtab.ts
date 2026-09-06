import type { NewtabSettings } from "@/interfaces/NewtabSettings";
import { getNewtabSettings, setNewtabSettings } from "@/utils/java_bridge";
import { getWallpaper, setWallpaper } from "@/utils/wallpaper";
import { ref } from "vue";

const wallpaper = ref<File | null>(null);
const wallpaperUrl = ref<string | null>(null);
const newtabSettings = ref<NewtabSettings | null>(null);

export function useNewtab() {
	async function refreshWallpaper() {
		wallpaper.value = await getWallpaper();
		console.log('Wallpaper:', wallpaper.value);
		if (wallpaper.value) wallpaperUrl.value = `turtlebrowse://api/get-wallpaper?t=${Date.now()}`
	}

	async function saveWallpaper(file: File) {
		await setWallpaper(file);
		await refreshWallpaper();
	}

	async function refreshSettings() {
		newtabSettings.value = await getNewtabSettings();
	}

	async function saveSettings(settings?: NewtabSettings) {
		if (settings) {
			await setNewtabSettings(settings);
			await refreshSettings();
		} else if (newtabSettings.value) {
			await setNewtabSettings(newtabSettings.value);
		}
	}

	return { wallpaper, wallpaperUrl, newtabSettings, refreshWallpaper, saveWallpaper, refreshSettings, saveSettings };
}
