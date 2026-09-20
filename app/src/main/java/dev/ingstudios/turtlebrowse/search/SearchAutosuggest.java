package dev.ingstudios.turtlebrowse.search;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import dev.ingstudios.turtlebrowse.db.ProfileDatabase.HistoryItem;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class SearchAutosuggest {
	private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	private final Gson gson = new Gson();
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> pendingTask;
	private final String userAgent;
	private final MainWindow parent;

	public SearchAutosuggest(String userAgent, MainWindow parent) {
		this.parent = parent;
		this.userAgent = userAgent;
	}

	public void getSuggestion(String query, Consumer<List<String>> onResults) {
		if (pendingTask != null && !pendingTask.isDone()) {
			System.out.println("Canceled suggestion fetch.");
			pendingTask.cancel(false);
		}

		pendingTask = scheduler.schedule(() -> {
			final List<String> results = fetchAndProcess(query);
			onResults.accept(results);
		}, 100, TimeUnit.MILLISECONDS);
	}

	public List<String> fetchAndProcess(String query) {
		final List<String> historySuggestions = fetchHistorySuggestions(query);
		final List<String> remoteSuggestions = fetchRemoteSuggestions(query);
		final List<String> combinedSuggestions = Stream.of(historySuggestions, remoteSuggestions)
				.flatMap(c -> c.stream()).distinct().collect(Collectors.toList());
		System.out.println("Combined suggestions: " + combinedSuggestions);
		return combinedSuggestions;
	}

	private List<String> fetchRemoteSuggestions(String query) {
		try {
			System.out.printf("Fetching for query %s\n", query);

			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://duckduckgo.com/ac/?q=%s"
							.formatted(URLEncoder.encode(query, StandardCharsets.UTF_8))))
					.header("User-Agent", userAgent)
					.build();

			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			System.out.println("Sent response for autosuggestion.");

			final int statusCode = response.statusCode();
			System.out.printf("Status code: %s\n", statusCode);

			if (statusCode == 200) {
				final String json = response.body();
				System.out.printf("Suggestions: %s\n", json);
				final Type listType = new TypeToken<ArrayList<AutocompleteItem>>() {
				}.getType();
				final List<AutocompleteItem> itemsList = gson.fromJson(json, listType);

				final List<String> itemStringList = new ArrayList<>();
				itemsList.forEach(item -> {
					itemStringList.add(item.phrase());
				});
				return itemStringList;
			} else {
				return new ArrayList<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	private List<String> fetchHistorySuggestions(String query) {
		final List<HistoryItem> items = parent.profileDatabase.getHistorySuggestions(query, 5);

		final List<String> historySuggestions = new ArrayList<>();
		for (HistoryItem item : items) {
			historySuggestions.add(item.url());
		}

		return historySuggestions;
	}

	public void shutdown() {
		scheduler.shutdown();
	}

	private record AutocompleteItem(String phrase) {
	}
}
