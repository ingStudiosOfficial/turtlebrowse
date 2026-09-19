package dev.ingstudios.turtlebrowse.managers;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class UpdateManager {
	private static UpdateManager instance;
	public final String currentVersion = getVersion();
	public String latestVersion = "1.0.0";
	private final MainWindow parent;
	private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	private final String releaseEndpoint = "https://turtlebrowseupdates.ingstudios.dev";

	private UpdateManager(MainWindow parent) {
		this.parent = parent;
		latestVersion = getLatestVersion();
	}

	public static synchronized UpdateManager getInstance(MainWindow parent) {
		if (instance == null) {
			instance = new UpdateManager(parent);
		}
		return instance;
	}

	private String getVersion() {
		try (InputStream input = UpdateManager.class.getResourceAsStream("/version.properties")) {
			final Properties properties = new Properties();
			if (input != null) {
				properties.load(input);
				return properties.getProperty("version", "1.0.0");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "1.0.0";
	}

	@SuppressWarnings("null")
	private String getLatestVersion() {
		try {
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(releaseEndpoint))
					.header("User-Agent", parent.userAgent)
					.build();

			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			final int statusCode = response.statusCode();

			if (statusCode == 200) {
				final String version = response.body();
				return version.replaceAll("^v", "").split("-")[0];
			} else {
				System.out.printf("Failed to fetch version: %d\n", statusCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "1.0.0";
	}

	public boolean refreshShouldUpdate() {
		latestVersion = getLatestVersion();
		return shouldUpdate();
	}

	public boolean shouldUpdate() {
		System.out.println("Current version: " + currentVersion);
		System.out.println("Latest version: " + latestVersion);

		final String currentClean = currentVersion.replaceAll("^v", "").split("-")[0];
		final String latestClean = latestVersion.replaceAll("^v", "").split("-")[0];

		final String[] currentParts = currentClean.split("\\.");
		final String[] latestParts = latestClean.split("\\.");

		final int length = Math.max(currentParts.length, latestParts.length);
		for (int i = 0; i < length; i++) {
			final int currentNumber = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
			final int latestNumber = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

			if (currentNumber < latestNumber) {
				return true;
			}
		}

		return false;
	}
}
