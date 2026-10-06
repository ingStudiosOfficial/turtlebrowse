package dev.evilbrowse.tools.specs;

import java.util.Map;

import dev.evilbrowse.tools.NavigateSiteTool;
import dev.evilbrowse.windows.MainWindow;
import io.github.ollama4j.tools.Tools;

public class NavigateSiteToolSpec {
	private final NavigateSiteTool navigateSiteTool;

	public NavigateSiteToolSpec(MainWindow parent) {
		navigateSiteTool = new NavigateSiteTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("navigate_to_site")
				.description(
						"Navigates to a given URL in the current browser context. Use this tool when you want to change the site the browser is currently on.")
				.parameters(Tools.Parameters.of(
						Map.of(
								"url",
								Tools.Property.builder()
										.type("string")
										.description(
												"The URL of the site you want to navigate to including the URL scheme.")
										.required(true)
										.build())))
				.build()).toolFunction(args -> {
					final String url = args.get("url").toString();
					navigateSiteTool.navigateTo(url);
					return "Successfully navigated to '" + url + "'.";
				}).build();
	}
}
