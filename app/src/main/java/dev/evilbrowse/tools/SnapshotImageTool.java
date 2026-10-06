package dev.evilbrowse.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.evilbrowse.Main;
import dev.evilbrowse.windows.MainWindow;

public class SnapshotImageTool {
	private final MainWindow parent;

	public SnapshotImageTool(MainWindow parent) {
		this.parent = parent;
	}

	public CompletableFuture<byte[]> takeSnapshotImage() {
		return parent.currentBrowser.getDevToolsClient().executeDevToolsMethod("Page.captureScreenshot", null)
				.thenApply(response -> {
					final JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
					final String base64 = obj.get("data").getAsString();
					final byte[] bytes = Base64.getDecoder().decode(base64);
					saveImageToDisk(bytes);
					System.out.println("Took screenshot.");
					return bytes;
				});
	}

	private void saveImageToDisk(byte[] bytes) {
		System.out.println("Saving image to disk...");
		final Path imagePath = Main.getStoragePath("debug", "ai", "latest-screenshot.png");
		final Path parentDir = imagePath.getParent();
		try {
			if (parentDir != null && Files.notExists(parentDir)) {
				Files.createDirectories(parentDir);
			}

			Files.write(imagePath, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
