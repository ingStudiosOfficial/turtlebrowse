package dev.ingstudios.turtlebrowse.managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class AdblockManager {
	private static AdblockManager instance;
	private List<String> easyListRules = new ArrayList<>();
	private final MainWindow parent;
	private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	private final Map<String, String> rulesMap = Map.of(
			"easylist", "https://easylist.to/easylist/easylist.txt",
			"fanboy-cookiemonster", "https://secure.fanboy.co.nz/fanboy-cookiemonster.txt",
			"easyprivacy", "https://easylist.to/easylist/easyprivacy.txt",
			"fanboy-annoyance", "https://secure.fanboy.co.nz/fanboy-annoyance.txt",
			"fanboy-social", "https://easylist.to/easylist/fanboy-social.txt",
			"ubo-annoyances-cookies",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/refs/heads/master/filters/annoyances-cookies.txt",
			"ubo-annoyances-others",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/refs/heads/master/filters/annoyances-others.txt",
			"ubo-badlists",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/refs/heads/master/filters/badlists.txt",
			"ubo-badware",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/refs/heads/master/filters/badware.txt",
			"ubo-unbreak",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/refs/heads/master/filters/unbreak.txt");

	private AdblockManager(MainWindow parent) {
		this.parent = parent;
		loadBlockRules();
	}

	public static synchronized AdblockManager getInstance(MainWindow parent) {
		if (instance == null) {
			instance = new AdblockManager(parent);
		}
		return instance;
	}

	private void loadBlockRules() {
		easyListRules = loadAllRules();
	}

	private List<String> loadAllRules() {
		final List<String> rules = new ArrayList<>();
		rulesMap.forEach((rule, url) -> {
			final List<String> cached = getCachedRules(rule);
			if (!cached.isEmpty()) {
				rules.addAll(cached);
			} else {
				rules.addAll(getNetworkRules(parent.userAgent, rule));
			}
		});
		return rules;
	}

	private List<String> getNetworkRules(String userAgent, String ruleName) {
		System.out.printf("Initializing %s rules.\n", ruleName);

		final String ruleUrl = rulesMap.get(ruleName);

		try {
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(ruleUrl))
					.header("User-Agent", userAgent)
					.build();

			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			final int statusCode = response.statusCode();

			if (statusCode != 200) {
				System.out.printf("%s fetch unsuccessful.\n", ruleUrl);
				return new ArrayList<>();
			}

			final String body = response.body();
			if (body == null) {
				System.out.println("Rule body is null.");
				return new ArrayList<>();
			}

			cacheEasyListRules(body, ruleName);

			final List<String> lines = stringToLines(body);

			return lines;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.printf("Failed to fetch %s, falling back to cache.\n", ruleUrl);
			return getCachedRules(ruleName);
		}
	}

	private List<String> getCachedRules(String ruleName) {
		try {
			final Path cachePath = parent.getProfileCachePath("adblock", "%s.txt".formatted(ruleName));

			if (Files.exists(cachePath) && Files.isRegularFile(cachePath)) {
				final String content = Files.readString(cachePath, StandardCharsets.UTF_8);
				System.out.println("Successfully loaded cached %s rules.".formatted(ruleName));
				return stringToLines(content);
			}

			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	private void cacheEasyListRules(String content, String ruleName) {
		final Path cachePath = parent.getProfileCachePath("adblock", "%s.txt".formatted(ruleName));
		final Path parentDir = cachePath.getParent();

		System.out.printf("Caching %s rules: %s\n", ruleName, cachePath.toString());

		try {
			if (parentDir != null && Files.notExists(parentDir)) {
				Files.createDirectories(parentDir);
			}

			Files.writeString(cachePath, content, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<String> stringToLines(String content) {
		return new ArrayList<>(Arrays.asList(content.split("\n")));
	}

	public List<String> getRules() {
		return easyListRules;
	}
}
