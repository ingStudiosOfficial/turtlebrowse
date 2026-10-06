package dev.ingstudios.turtlebrowse.handlers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.network.CefRequest;
import org.cef.network.CefRequest.ResourceType;

import com.example.adblock.AdvtBlocker;

public class TurtlebrowseResourceRequestHandler extends CefResourceRequestHandlerAdapter {
	private final AdvtBlocker blocker;
	private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

	public TurtlebrowseResourceRequestHandler(String userAgent) {
		final List<String> rules = getEasyListRules(userAgent);
		blocker = AdvtBlocker.createInstance(rules);
	}

	@Override
	public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) {
		final String resourceType = resourceTypeToString(request.getResourceType());

		final boolean result = blocker.checkUrls(request.getURL(), browser.getURL(), resourceType);
		if (result) {
			System.out.println("Blocked resource: " + request.getURL());
			return true;
		}

		return false;
	}

	private List<String> getEasyListRules(String userAgent) {
		try {
			System.out.println("Initializing EasyList rules.");

			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://easylist.to/easylist/easylist.txt"))
					.header("User-Agent", userAgent)
					.build();

			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			final int statusCode = response.statusCode();

			if (statusCode != 200) {
				System.out.println("EasyList fetch unsuccessful.");
				return new ArrayList<>();
			}

			final String body = response.body();
			if (body == null) {
				System.out.println("EasyList body is null.");
				return new ArrayList<>();
			}

			final List<String> lines = new ArrayList<>(Arrays.asList(body.split("\n")));

			return lines;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	private String resourceTypeToString(ResourceType resourceType) {
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
