export async function getWallpaper(): Promise<File | null> {
	const response = await fetch('turtlebrowse://api/get-wallpaper', {
		method: 'GET',
	});

	if (!response.ok) {
		return null;
	}

	const blob = await response.blob();

	console.log('Successfully fetched wallpaper.');

	return new File([blob], 'wallpaper', {
		type: blob.type,
		lastModified: Date.now(),
	});
}

export async function setWallpaper(image: File) {
	try {
		const arrayBuffer = await image.arrayBuffer();

		const response = await fetch(`turtlebrowse://api/set-wallpaper`, {
			method: 'POST',
			headers: {
				'Content-Type': image.type,
			},
			body: arrayBuffer,
		});

		if (!response.ok) {
			console.error('Failed to set wallpaper:', await response.json());
		}
	} catch (error) {
		console.error('Failed to set wallpaper:', error);
	}
}
