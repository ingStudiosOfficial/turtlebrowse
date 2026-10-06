package dev.evilbrowse.tools.specs;

import java.util.Map;

import dev.evilbrowse.tools.FetchTool;
import dev.evilbrowse.windows.MainWindow;
import io.github.ollama4j.tools.Tools;

public class FetchToolSpec {
	private final FetchTool fetchTool;
	private final MainWindow parent;

	public FetchToolSpec(String userAgent, MainWindow parent) {
		fetchTool = new FetchTool(userAgent);
		this.parent = parent;
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("fetch_url")
				.description(
						"Fetches a URL on the internet. This is useful when you want to extract more information from a page on fetch the results from an API.")
				.parameters(
						Tools.Parameters.of(
								Map.of(
										"url",
										Tools.Property.builder()
												.type("string")
												.description(
														"The full URL you want to fetch from including the URL scheme.")
												.required(true)
												.build())))
				.build()).toolFunction(
						args -> {
							final String url = args.get("url").toString();
							try {
								final String response = fetchTool.fetch(url);
								return "Fetch response: '" + response + "'\nThe user's original prompt was: '"
										+ parent.getLatestAIMessage()
										+ "'. Use this new data to fufill the user's request.";
							} catch (Exception e) {
								return e.getMessage();
							}
						})
				.build();
	}
}
