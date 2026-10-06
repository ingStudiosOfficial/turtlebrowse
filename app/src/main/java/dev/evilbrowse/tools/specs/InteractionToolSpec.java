package dev.evilbrowse.tools.specs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.evilbrowse.tools.InteractionTool;
import dev.evilbrowse.windows.MainWindow;
import io.github.ollama4j.tools.Tools;

public class InteractionToolSpec {
	private final InteractionTool interactionTool;
	private final List<String> actions = new ArrayList<>();
	private final MainWindow parent;

	public InteractionToolSpec(MainWindow parent) {
		this.parent = parent;
		interactionTool = new InteractionTool(parent);
	}

	public final Tools.Tool getSpecification() {
		actions.add("click");
		actions.add("input");
		actions.add("enter");
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("interact_with_page")
				.description(
						"Creates an interaction with the current page or element (e.g. click an element). Call get_dom_snapshot to get context on the backend node ID and the current page. The valid actions are: "
								+ actions.toString())
				.parameters(
						Tools.Parameters.of(
								Map.of(
										"action",
										Tools.Property.builder()
												.enumValues(actions)
												.description("The action you want to perform on the element.")
												.required(true)
												.build(),
										"backendNodeId",
										Tools.Property.builder()
												.type("string")
												.description(
														"The backend node ID element you want to perform an action on.")
												.required(true)
												.build(),
										"inputText",
										Tools.Property.builder()
												.type("string")
												.description("The text you want to type if you want to input text.")
												.required(false)
												.build())))
				.build()).toolFunction(args -> {
					final String action = args.get("action").toString();
					final String nodeId = args.get("backendNodeId").toString();

					switch (action) {
						case "click": {
							final String result = interactionTool.clickElement(nodeId);
							return "Response from action executed on "
									+ parent.currentBrowser.getURL() + ": '"
									+ result + "'\nThe user's original prompt was: '"
									+ parent.getLatestAIMessage()
									+ "'. Use this new data to fufill the user's request.";
						}

						case "input": {
							final String input = args.get("inputText").toString();
							if (input == null) {
								return "The input is empty. An input must be provided.";
							}
							final String result = interactionTool.typeElement(nodeId, input);
							return "Response from action executed on "
									+ parent.currentBrowser.getURL() + ": '"
									+ result + "'\nThe user's original prompt was: '"
									+ parent.getLatestAIMessage()
									+ "'. Use this new data to fufill the user's request.";
						}

						case "enter": {
							final String result = interactionTool.pressEnter();
							return "Response from action executed on "
									+ parent.currentBrowser.getURL() + ": '"
									+ result + "'\nThe user's original prompt was: '"
									+ parent.getLatestAIMessage()
									+ "'. Use this new data to fufill the user's request.";
						}

						default: {
							System.err.printf("A valid action was not provided: %s\n", action);
							return "A valid action was not provided. Hint: the valid actions are: "
									+ actions.toString();
						}
					}
				}).build();
	}
}
