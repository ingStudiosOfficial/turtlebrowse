package dev.ingstudios.turtlebrowse.handlers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseLoadHandler extends CefLoadHandlerAdapter {
	private final List<JSQueueItem> queueStack = new java.util.concurrent.CopyOnWriteArrayList<>();
	private final List<Integer> readyBrowsers = new ArrayList<>();
	private final Gson gson = new Gson();
	private final MainWindow parent;

	public TurtlebrowseLoadHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {

	}

	@Override
	public void onLoadEnd(CefBrowser browser, CefFrame frame, int statusCode) {
		readyBrowsers.add(browser.getIdentifier());
		final List<JSQueueItem> toRemove = new ArrayList<>();
		for (final JSQueueItem item : queueStack) {
			System.out.printf("Item: %s\n", item.code);
			if (item.isSame(browser.getIdentifier())) {
				browser.executeJavaScript(item.code, item.url, 0);
				toRemove.add(item);
			}
		}
		queueStack.removeAll(toRemove);

		updateSiteFavicon(browser);
	}

	public void addToQueueStack(JSQueueItem item) {
		queueStack.add(item);
	}

	public boolean isBrowserReady(Integer id) {
		return readyBrowsers.contains(id);
	}

	public void removeBrowser(Integer id) {
		readyBrowsers.remove(id);
	}

	public record JSQueueItem(int identifier, String code, String url) {
		public boolean isSame(int id) {
			return id == identifier;
		}
	}

	private void updateSiteFavicon(CefBrowser browser) {
		System.out.println("Getting site favicons...");

		final JsonObject paramsAsJson = new JsonObject();
		paramsAsJson.addProperty("expression",
				"Array.from(document.querySelectorAll('link[rel~=\"icon\"]')).map(el => el.href)");
		paramsAsJson.addProperty("returnByValue", true);

		final CompletableFuture<String> response = browser.getDevToolsClient()
				.executeDevToolsMethod("Runtime.evaluate", gson.toJson(paramsAsJson)).thenApply(r -> {
					return r;
				});

		Thread.ofVirtual().start(() -> {
			try {
				final String rawResponse = response.get();

				final JsonObject json = JsonParser.parseString(rawResponse).getAsJsonObject();
				final JsonObject result = json.getAsJsonObject("result");
				final JsonArray iconsArray = result.getAsJsonArray("value");

				final ArrayList<String> urls = gson.fromJson(iconsArray.toString(), new TypeToken<ArrayList<String>>() {
				}.getType());

				System.out.println("Favicon URLs: " + urls.toString());

				urls.sort(Comparator.comparingInt(url -> {
					final String lower = url.toString().toLowerCase();
					if (lower.endsWith(".svg")) {
						return 1;
					} else if (lower.endsWith(".png")) {
						return 2;
					} else if (lower.endsWith(".ico")) {
						return 4;
					} else {
						return 3;
					}
				}));

				parent.tabBar.updateFavicon(browser, urls.get(0));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
