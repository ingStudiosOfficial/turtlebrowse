export async function getWallpaper(): Promise<File | null> {
	const response = await fetch('turtlebrowse://api/wallpaper', {
		method: 'GET',
	});

	if (!response.ok) {
		return null;
	}

	const blob = await response.blob();

	return new File([blob], 'wallpaper', {
		type: blob.type,
		lastModified: Date.now(),
	});
}

export async function setWallpaper(image: File) {
	const formData = new FormData();
	formData.append('wallpaper', image);

	try {
		const response = await fetch(`turtlebrowse://api/wallpaper`, {
			method: 'POST',
			body: formData,
		});

		if (!response.ok) {
			console.error('Failed to set wallpaper:', await response.json());
		}
	} catch (error) {
		console.error('Failed to set wallpaper:', error);
	}
}
