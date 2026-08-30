package dev.ingstudios.turtlebrowse.tools;

import java.util.ArrayList;
import java.util.List;

import dev.ingstudios.turtlebrowse.windows.MainWindow;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.request.ThinkMode;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.models.response.OllamaResult;

public class FindElementTool {
	private final SnapshotTool snapshotTool;
	private final Ollama ollama = new Ollama();
	private final OllamaGenerateRequest builder;

	public FindElementTool(MainWindow parent) throws OllamaException {
		snapshotTool = new SnapshotTool(parent);

		ollama.setRequestTimeoutSeconds(120);

		try {
			final List<Model> models = ollama.listModels();

			final String chatModel = parent.aiSettings.model();

			final List<String> modelNames = new ArrayList<>();
			models.forEach(model -> {
				final String modelName = model.getName();
				modelNames.add(modelName);
			});

			if (!modelNames.contains(chatModel)) {
				ollama.pullModel(chatModel, (model, resp) -> {
					System.out.printf("Pulling %s: %s\n", model, resp.getStatus());
				});
			}

			builder = OllamaGenerateRequest.builder().withModel(chatModel).withThink(ThinkMode.ENABLED);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public String findElement(String element) {
		try {
			final String snapshot = snapshotTool.takeSnapshot().get();

			final String prompt = """
					Task: Find the backendNodeId of the element from the DOM snapshot that matches the element description best.

					Description:
					%s

					DOM Snapshot:
					%s

					Instructions:
					1. Analyze the snapshot to return the element that matches the description best.
					2. Return only the backendNodeId and nothing else.
					"""
					.formatted(snapshot, element);

			final OllamaGenerateRequest chatRequest = builder.withPrompt(prompt)
					.build();
			final OllamaResult result = ollama.generate(chatRequest, null);
			final String response = result.getResponse();
			System.out.printf("Backend node ID: %s\n", response);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while finding element.";
		}
	}
}
