package dev.ingstudios.turtlebrowse.tools;

import java.util.concurrent.TimeUnit;

import org.cef.browser.CefDevToolsClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.ingstudios.turtlebrowse.windows.MainWindow;
import dev.kreuzberg.htmltomarkdown.HtmlToMarkdown;

public class SummarizePageTool {
	private final MainWindow parent;

	public SummarizePageTool(MainWindow parent) {
		this.parent = parent;
	}

	public String summarizePage() {
		final CefDevToolsClient client = parent.currentBrowser.getDevToolsClient();

		try {
			final String html = client.executeDevToolsMethod("DOM.getDocument", "{\"depth\": -1, \"pierce\": false}")
					.thenCompose(response -> {
						final JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
						final int rootId = jsonObject.get("root").getAsJsonObject().get("nodeId").getAsInt();

						final JsonObject params = new JsonObject();
						params.addProperty("nodeId", rootId);

						return client.executeDevToolsMethod("DOM.getOuterHTML", params.toString());
					})
					.thenApply(res -> {
						final JsonObject htmlJsonObject = JsonParser.parseString(res).getAsJsonObject();
						return htmlJsonObject.get("outerHTML").getAsString();
					})
					.get(10, TimeUnit.SECONDS);

			final String rawMarkdown = summarizePage(html);

			final String markdown = "Website title: %s\nWebsite URL: %s\nPage:\n%s".formatted(getPageTitle(),
					parent.currentBrowser.getURL(),
					rawMarkdown);

			return markdown;
		} catch (Exception e) {
			e.printStackTrace();
			return "An error occurred while extracting page content.";
		}
	}

	public String summarizePage(String html) {
		if (html == null) {
			return "";
		}

		final Document doc = Jsoup.parse(html);

		final String[] selectors = {
				"article",
				"main",
				"#content",
				".content",
				"#main",
				"#app",
				"#root"
		};

		Element contentElement = doc.body();

		for (final String selector : selectors) {
			if (selector == null)
				continue;
			final Element found = doc.selectFirst(selector);
			if (found != null && !found.text().isBlank()) {
				contentElement = found;
				break;
			}
		}

		final Elements possibleAds = contentElement
				.select("[class*=ad], [id*=ad], [class*=advert], [id*=advert]");
		for (final Element ad : possibleAds) {
			ad.remove();
		}

		contentElement.select("script, style, svg, canvas, iframe, noscript, img, video, audio").remove();
		contentElement.select("*").forEach((el) -> {
			el.clearAttributes();
		});
		final String cleanHtml = contentElement.html();
		System.out.printf("Clean HTML: %s\n", cleanHtml);
		System.out.println("Attempting to convert HTML to Markdown...");
		final String markdown = HtmlToMarkdown.convert(cleanHtml);
		System.out.printf("Converted Markdown: %s\n", markdown);

		return markdown;
	}

	private String getPageTitle() {
		final CefDevToolsClient client = parent.currentBrowser.getDevToolsClient();

		try {
			final JsonObject params = new JsonObject();
			params.addProperty("expression", "document.title");
			params.addProperty("returnByValue", true);

			final String response = client.executeDevToolsMethod("Runtime.evaluate", params.toString())
					.thenApply(res -> {
						final JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
						final JsonObject result = jsonObject.getAsJsonObject("result");

						if (result.has("value")) {
							return result.get("value").getAsString();
						}
						return "";
					})
					.get(5, TimeUnit.SECONDS);

			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
