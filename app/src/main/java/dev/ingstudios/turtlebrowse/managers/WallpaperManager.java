package dev.ingstudios.turtlebrowse.managers;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;

import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class WallpaperManager {
	private static WallpaperManager instance;
	private final MainWindow parent;

	private WallpaperManager(MainWindow parent) {
		this.parent = parent;
	}

	public static synchronized WallpaperManager getInstance(MainWindow parent) {
		if (instance == null) {
			instance = new WallpaperManager(parent);
		}
		return instance;
	}

	private byte[] getRequestBodyAsBytes(CefRequest request) {
		CefPostData postData = request.getPostData();
		if (postData == null)
			return new byte[0];

		Vector<CefPostDataElement> elements = new Vector<>();
		postData.getElements(elements);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		for (CefPostDataElement element : elements) {
			if (element.getType() == CefPostDataElement.Type.PDE_TYPE_BYTES) {
				int size = element.getBytesCount();
				if (size > 0) {
					byte[] buffer = new byte[size];
					element.getBytes(size, buffer);
					baos.write(buffer, 0, size);
				}
			}
		}
		return baos.toByteArray();
	}

	private Path getWallpaperPath() {
		return Main.getStoragePath("profiles", parent.currentProfile.getIdAsString(), "wallpaper");
	}

	public WallpaperMetadata getWallpaper() {
		final Path wallpaperPath = getWallpaperPath();
		try {
			final byte[] imageBytes = Files.readAllBytes(wallpaperPath);
			final String mimeType = Files.probeContentType(wallpaperPath);
			return new WallpaperMetadata(imageBytes, mimeType != null ? mimeType : "image/png");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setWallpaper(CefRequest request) {
		final byte[] imageBytes = getRequestBodyAsBytes(request);

		final String wallpaperPath = getWallpaperPath().toString();

		try (FileOutputStream fos = new FileOutputStream(wallpaperPath)) {
			fos.write(imageBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clearWallpaper() {
		final Path wallpaperPath = getWallpaperPath();

		try {
			Files.delete(wallpaperPath);
		} catch (IOException e) {
			System.err.printf("Failed to delete wallpaper: %s".formatted(e.getMessage()));
		}
	}

	public record WallpaperMetadata(byte[] imageBytes, String mimeType) {
	}
}
