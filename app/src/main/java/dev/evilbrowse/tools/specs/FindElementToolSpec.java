package dev.evilbrowse.tools.specs;

import java.util.Map;

import dev.evilbrowse.tools.FindElementTool;
import dev.evilbrowse.windows.MainWindow;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.tools.Tools;

public class FindElementToolSpec {
	private final MainWindow parent;
	private final FindElementTool findElementTool;

	public FindElementToolSpec(MainWindow parent) throws OllamaException {
		this.parent = parent;

		findElementTool = new FindElementTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("find_element")
				.description("Finds an element's backend node ID from the DOM given a query.")
				.parameters(Tools.Parameters.of(
						Map.of(
								"query",
								Tools.Property.builder()
										.type("string")
										.description("A description of the element you want to find.")
										.required(true)
										.build())))
				.build()).toolFunction(args -> {
					final String description = args.get("query").toString();

					final String nodeId = findElementTool.findElement(description);

					return "Use this found backend node ID: '"
							+ nodeId + "'\nThe user's original prompt was: '" + parent.getLatestAIMessage()
							+ "'. Use this new data to fufill the user's request.";
				}).build();
	}
}
