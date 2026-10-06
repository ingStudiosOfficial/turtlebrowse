package dev.evilbrowse.handlers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.network.CefRequest;
import org.cef.network.CefRequest.ResourceType;

import com.example.adblock.AdvtBlocker;

import dev.evilbrowse.Main;
import dev.evilbrowse.windows.MainWindow;

/**
 * EvilBrowse network filter: uBlock Origin (uAssets) lists plus EasyList and
 * EasyPrivacy, evaluated by the adblock-coffee engine.
 *
 * <p>
 * The engine starts empty and loads rules on a background thread so browser
 * startup never blocks on filter downloads. Lists are cached on disk for 24h
 * so cold starts work offline. Toggling happens live via Settings → Privacy.
 */
public class EvilBrowseResourceRequestHandler extends CefResourceRequestHandlerAdapter {
	private static final List<String> FILTER_LIST_URLS = List.of(
			"https://easylist.to/easylist/easylist.txt",
			"https://easylist.to/easylist/easyprivacy.txt",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/filters.txt",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/badware.txt",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/privacy.txt",
			"https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/unbreak.txt");

	private static final Duration CACHE_TTL = Duration.ofHours(24);
	private static final Duration FETCH_TIMEOUT = Duration.ofSeconds(15);

	private final MainWindow parent;
	private final String userAgent;
	private final HttpClient httpClient = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private volatile AdvtBlocker blocker;
	private volatile int ruleCount = 0;
	private volatile long lastUpdateEpochMs = 0;
	// Cached off the CEF IO threads: a Nitrite read per blocked-resource
	// check stalled page loads under contention. Refreshed on toggle + init.
	private volatile boolean adblockEnabled = true;
	private volatile long blockedCount = 0;
	private final List<String> listStatus = java.util.Collections.synchronizedList(new ArrayList<>());

	public EvilBrowseResourceRequestHandler(MainWindow parent) {
		this.parent = parent;
		String ua = "";
		try {
			ua = parent.userAgent;
		} catch (Exception ignored) {
		}
		this.userAgent = ua != null ? ua : "";
		try {
			blocker = AdvtBlocker.createInstance(List.of());
		} catch (Exception e) {
			System.err.println("Adblock engine init failed: " + e.getMessage());
			blocker = null;
		}
		try {
			adblockEnabled = parent.profileDatabase.getAdblockEnabled();
		} catch (Exception ignored) {
		}
		// Never block the EDT/constructor: rules load in the background.
		Thread.ofVirtual().start(this::refreshRules);
	}

	@Override
	public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) {
		if (request == null) {
			return false;
		}
		if (!isEnabled()) {
			return false;
		}
		final AdvtBlocker engine = blocker;
		if (engine == null) {
			return false;
		}
		String url;
		String pageUrl = "";
		try {
			url = request.getURL();
			if (url == null || url.isBlank()) {
				return false;
			}
			if (browser != null) {
				try {
					String bUrl = browser.getURL();
					if (bUrl != null) {
						pageUrl = bUrl;
					}
				} catch (Exception ignored) {
				}
			}
			final boolean blocked = engine.checkUrls(url, pageUrl,
					resourceTypeToString(request.getResourceType()));
			if (blocked) {
				blockedCount++;
				return true;
			}
		} catch (Exception e) {
			// A broken rule/engine must never break page loads.
			System.err.println("Adblock check failed: " + e.getMessage());
		}
		return false;
	}

	public boolean isEnabled() {
		return adblockEnabled;
	}

	public void setEnabled(boolean enabled) {
		adblockEnabled = enabled;
	}

	public int getRuleCount() {
		return ruleCount;
	}

	public long getBlockedCount() {
		return blockedCount;
	}

	public List<String> getListStatus() {
		synchronized (listStatus) {
			return new ArrayList<>(listStatus);
		}
	}

	private static String listLabel(String listUrl) {
		if (listUrl.contains("uAssets")) {
			if (listUrl.endsWith("badware.txt")) {
				return "uBO badware";
			}
			if (listUrl.endsWith("privacy.txt")) {
				return "uBO privacy";
			}
			if (listUrl.endsWith("unbreak.txt")) {
				return "uBO unbreak";
			}
			return "uBO filters";
		}
		if (listUrl.endsWith("easyprivacy.txt")) {
			return "EasyPrivacy";
		}
		return "EasyList";
	}

	/** Force a filter refresh from Settings. Runs off the caller's thread. */
	public void requestRefresh() {
		Thread.ofVirtual().start(this::refreshRules);
	}

	public long getLastUpdateEpochMs() {
		return lastUpdateEpochMs;
	}

	private void refreshRules() {
		try {
			listStatus.clear();
			final List<String> allRules = new ArrayList<>();
			for (int i = 0; i < FILTER_LIST_URLS.size(); i++) {
				final String listUrl = FILTER_LIST_URLS.get(i);
				try {
					final List<String> rules = loadList(i, listUrl);
					allRules.addAll(rules);
					listStatus.add(String.format("%s: %d rules", listLabel(listUrl), rules.size()));
				} catch (Exception e) {
					System.err.printf("Filter list failed, skipping %s: %s\n", listUrl, e.getMessage());
					listStatus.add(String.format("%s: FAILED (%s)", listLabel(listUrl), e.getMessage()));
				}
			}
			if (allRules.isEmpty()) {
				System.err.println("No filter rules loaded; adblock stays passive.");
				return;
			}
			final AdvtBlocker fresh = AdvtBlocker.createInstance(allRules);
			final AdvtBlocker old = blocker;
			blocker = fresh;
			ruleCount = allRules.size();
			lastUpdateEpochMs = System.currentTimeMillis();
			System.out.printf("Adblock engine ready: %d rules.\n", allRules.size());
			if (old != null) {
				try {
					old.destroyInstance();
				} catch (Exception ignored) {
				}
			}
		} catch (Exception e) {
			System.err.println("Adblock refresh failed: " + e.getMessage());
		}
	}

	private List<String> loadList(int index, String listUrl) throws Exception {
		final Path cacheDir = cacheDir();
		final Path cached = cacheDir.resolve("list-" + index + ".txt");
		final long now = System.currentTimeMillis();
		boolean stale = true;
		try {
			if (Files.isRegularFile(cached)
					&& now - Files.getLastModifiedTime(cached).toMillis() < CACHE_TTL.toMillis()) {
				stale = false;
			}
		} catch (Exception ignored) {
		}
		if (!stale) {
			try {
				return parseRules(Files.readString(cached, StandardCharsets.UTF_8));
			} catch (Exception e) {
				System.err.println("Filter cache unreadable, refetching: " + cached);
			}
		}
		try {
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(listUrl))
					.header("User-Agent", userAgent)
					.timeout(FETCH_TIMEOUT)
					.GET()
					.build();
			final HttpResponse<String> response = httpClient.send(request,
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (response.statusCode() != 200 || response.body() == null) {
				throw new IllegalStateException("HTTP " + response.statusCode());
			}
			final String body = response.body();
			try {
				Files.createDirectories(cacheDir);
				Files.writeString(cached, body, StandardCharsets.UTF_8);
			} catch (Exception e) {
				System.err.println("Filter cache write failed: " + e.getMessage());
			}
			return parseRules(body);
		} catch (Exception fetchError) {
			// Offline: fall back to stale cache instead of no protection.
			try {
				if (Files.isRegularFile(cached)) {
					System.out.println("Using stale filter cache for " + listUrl);
					return parseRules(Files.readString(cached, StandardCharsets.UTF_8));
				}
			} catch (Exception ignored) {
			}
			throw fetchError;
		}
	}

	private Path cacheDir() {
		return Main.getStoragePath("filter-cache");
	}

	private List<String> parseRules(String body) {
		if (body == null || body.isBlank()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(Arrays.asList(body.split("\n")));
	}

	private String resourceTypeToString(ResourceType resourceType) {
		if (resourceType == null) {
			return "other";
		}
		switch (resourceType) {
			case RT_SCRIPT: {
				return "script";
			}
			case RT_IMAGE: {
				return "image";
			}
			case RT_STYLESHEET: {
				return "stylesheet";
			}
			case RT_OBJECT: {
				return "object";
			}
			case RT_SUB_FRAME: {
				return "subdocument";
			}
			case RT_XHR: {
				return "xmlhttprequest";
			}
			case RT_PING: {
				return "ping";
			}
			case RT_MEDIA: {
				return "media";
			}
			case RT_FONT_RESOURCE: {
				return "font";
			}
			case RT_FAVICON: {
				return "image";
			}
			case RT_PLUGIN_RESOURCE: {
				return "object";
			}
			default: {
				return "other";
			}
		}
	}
}
