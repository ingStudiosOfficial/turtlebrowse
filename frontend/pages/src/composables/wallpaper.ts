import { getWallpaper, setWallpaper } from "@/utils/wallpaper";
import { ref } from "vue";

const wallpaper = ref<File | null>(null);
const wallpaperUrl = ref<string | null>(null);

export function useWallpaper() {
	async function refreshWallpaper() {
		wallpaper.value = await getWallpaper();
		if (wallpaper.value) wallpaperUrl.value = `turtlebrowse://api/get-wallpaper?t=${Date.now()}`
	}

	async function saveWallpaper(file: File) {
		await setWallpaper(file);
		await refreshWallpaper();
	}

	return { wallpaper, wallpaperUrl, refreshWallpaper, saveWallpaper };
}
