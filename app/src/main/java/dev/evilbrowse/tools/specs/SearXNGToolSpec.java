package dev.evilbrowse.tools.specs;

import java.util.Map;

import dev.evilbrowse.tools.SearXNGSearchTool;
import dev.evilbrowse.windows.MainWindow;
import io.github.ollama4j.tools.Tools;

public class SearXNGToolSpec {
	private final SearXNGSearchTool searchTool;
	private final MainWindow parent;

	public SearXNGToolSpec(String userAgent, MainWindow parent) {
		searchTool = new SearXNGSearchTool(userAgent);
		this.parent = parent;
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("search_web")
				.description("Searches up-to-date information with on the web.")
				.parameters(
						Tools.Parameters.of(
								Map.of(
										"search_query",
										Tools.Property.builder()
												.type("string")
												.description("The query you want to search up on the web.")
												.required(true)
												.build())))
				.build()).toolFunction(
						args -> {
							final String searchQuery = args.get("search_query").toString();
							try {
								final String searchResult = searchTool.searchWeb(searchQuery).toString();
								System.out.printf("Search result: %s\n", searchResult);
								return "Search result: '" + searchResult + "'\nThe user's original prompt was: '"
										+ parent.getLatestAIMessage()
										+ "'. Use this new data to fufill the user's request.";
							} catch (Exception e) {
								e.printStackTrace();
								return e.getMessage();
							}
						})
				.build();
	}
}
