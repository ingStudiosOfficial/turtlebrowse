package dev.evilbrowse.handlers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

import dev.evilbrowse.windows.MainWindow;

public class EvilBrowseSchemeHandlerFactory implements CefSchemeHandlerFactory {
	private MainWindow parent;

	public EvilBrowseSchemeHandlerFactory(MainWindow parent) {
		System.out.println("MainWindow in scheme handler factory: " + parent);
		this.parent = parent;
	}

	private static String normalizeScheme(String url) {
		if (url != null && url.startsWith("turtlebrowse://")) {
			// Legacy Turtlebrowse URLs (history/bookmarks) resolve to EvilBrowse pages.
			return "evilbrowse://" + url.substring("turtlebrowse://".length());
		}
		return url;
	}

	@Override
	public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
		final String rawUrl = request.getURL();
		final String url = normalizeScheme(rawUrl);

		if (url.startsWith("evilbrowse://api/prompt-stream")) {
			System.out.println("Prompt stream requested.");
			String prompt = "";
			String query = url.contains("?") ? url.substring(url.indexOf('?') + 1) : "";
			for (final String param : query.split("&")) {
				String[] pair = param.split("=", 2);
				if (pair.length == 2 && pair[0].equals("prompt")) {
					prompt = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
				}
			}
			return new StreamingSchemeResourceHandler(prompt, parent);
		}

		return new EvilBrowseSchemeResourceHandler(parent);
	}
}
