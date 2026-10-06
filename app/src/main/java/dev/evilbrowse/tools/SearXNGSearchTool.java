package dev.evilbrowse.tools;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SearXNGSearchTool {
	private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	private String userAgent;

	public SearXNGSearchTool(String userAgent) {
		this.userAgent = userAgent;
	}

	public SearchResult searchWeb(String query) throws Exception {
		String url = "https://searxng.ingstudios.dev/search?q=" +
				URLEncoder.encode(query, StandardCharsets.UTF_8) +
				"&format=json";

		System.out.printf("Navigated to: %s\n", url);

		try {
			System.out.printf("User Agent: %s\n", userAgent);
			System.out.println("Creating request...");
			HttpRequest request;
			if (userAgent != null) {
				request = HttpRequest.newBuilder()
						.uri(URI.create(url))
						.header("User-Agent", userAgent)
						.header("Accept", "application/json, text/plain, */*")
						.header("Accept-Language", "en-US,en;q=0.9")
						.header("Sec-Ch-Ua", "\"Chromium\";v=\"152\", \"Not?A_Brand\";v=\"24\"")
						.header("Sec-Ch-Ua-Mobile", "?0")
						.header("Sec-Ch-Ua-Platform", "\"Linux\"")
						.header("Sec-Fetch-Dest", "empty")
						.header("Sec-Fetch-Mode", "cors")
						.header("Sec-Fetch-Site", "same-origin")
						.GET()
						.build();
			} else {
				request = HttpRequest.newBuilder()
						.uri(URI.create(url))
						.header("Accept", "application/json, text/plain, */*")
						.header("Accept-Language", "en-US,en;q=0.9")
						.header("Sec-Ch-Ua", "\"Chromium\";v=\"152\", \"Not?A_Brand\";v=\"24\"")
						.header("Sec-Ch-Ua-Mobile", "?0")
						.header("Sec-Ch-Ua-Platform", "\"Linux\"")
						.header("Sec-Fetch-Dest", "empty")
						.header("Sec-Fetch-Mode", "cors")
						.header("Sec-Fetch-Site", "same-origin")
						.GET()
						.build();
			}
			System.out.println("After creating request.");

			System.out.printf("request is null: %b\n", request == null);

			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.printf("response is null: %b\n", response == null);

			if (response != null && response.statusCode() == 200) {
				System.out.printf("response body is null: %b\n", response.body() == null);
				return parseWithGson(response.body());
			} else if (response != null) {
				System.out.printf("Response code: %d\n", response.statusCode());
			}
		} catch (IOException e) {
			System.err.println("Search failed (IO Exception): " + e.getClass().getName() + " " + e.getMessage());
			throw new IOException("An IO exception occurred while fetching results");
		} catch (InterruptedException e) {
			System.err.println("Search failed (Interrupted Exception): " + e.getMessage());
			throw new InterruptedException("The network connection was interrupted while fetching results");
		} catch (Exception e) {
			System.err.println("Search failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
			throw new Exception("An unknown error occurred while fetching results");
		}

		return new SearchResult(new ArrayList<>());
	}

	private SearchResult parseWithGson(String jsonBody) {
		System.out.printf("JSON body: %s\n", jsonBody);

		JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();

		List<ResultItem> resultItems = new ArrayList<>();

		JsonArray results = json.getAsJsonArray("results");
		if (results != null && results.size() > 0) {
			for (final JsonElement res : results) {
				final JsonObject jsonObject = res.getAsJsonObject();
				final String title = jsonObject.get("title").getAsString();
				final String content = jsonObject.get("content").getAsString();
				final String url = jsonObject.get("url").getAsString();
				resultItems.add(new ResultItem(title, content, url));
			}
		}

		return new SearchResult(resultItems);
	}

	public record SearchResult(List<ResultItem> links) {
		@Override
		public String toString() {
			System.out.printf("Links: %s", links.toString());
			final List<String> stringLinks = new ArrayList<>();
			links.forEach((link) -> {
				stringLinks.add(link.toString());
			});
			return stringLinks.toString();
		}
	}

	public record ResultItem(String title, String content, String url) {
		@Override
		public String toString() {
			return "{title:" + title + ",content:" + content + "url:" + url + "}";
		}
	}
}
