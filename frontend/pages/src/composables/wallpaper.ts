import { getWallpaper, setWallpaper } from "@/utils/wallpaper";
import { ref } from "vue";

const wallpaper = ref<File | null>(null);
const wallpaperUrl = ref<string | null>(null);

export function useWallpaper() {
	async function refreshWallpaper() {
		if (wallpaperUrl.value) URL.revokeObjectURL(wallpaperUrl.value);
		wallpaper.value = await getWallpaper();
		if (wallpaper.value) wallpaperUrl.value = URL.createObjectURL(wallpaper.value);
	}

	async function saveWallpaper(file: File) {
		if (wallpaperUrl.value) URL.revokeObjectURL(wallpaperUrl.value);
		wallpaperUrl.value = URL.createObjectURL(file);
		wallpaper.value = file;
		await setWallpaper(file);
	}

	return { wallpaper, wallpaperUrl, refreshWallpaper, saveWallpaper };
}
