package dev.evilbrowse.tools;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FetchTool {
	private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	private final String userAgent;

	public FetchTool(String userAgent) {
		this.userAgent = userAgent;
	}

	public String fetch(String url) throws Exception {
		System.out.printf("URL to fetch: %s\n", url);

		try {
			final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", userAgent)
					.GET().build();
			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			final String body = response.body();
			return body;
		} catch (IOException e) {
			System.err.println("Fetch failed (IO Exception): " + e.getMessage());
			throw new IOException("An IO exception occurred while fetching results");
		} catch (InterruptedException e) {
			System.err.println("Search failed (Interrupted Exception): " + e.getMessage());
			throw new InterruptedException("The network connection was interrupted while fetching results");
		} catch (Exception e) {
			System.err.println("Search failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
			throw new Exception("An unknown error occurred while fetching results");
		}
	}
}
