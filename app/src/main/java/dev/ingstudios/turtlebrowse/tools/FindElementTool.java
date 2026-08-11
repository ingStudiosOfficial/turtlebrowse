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

			final OllamaGenerateRequest chatRequest = builder.withPrompt(
					"Use the snapshot '%s' to find the backendNodeId of the elment: '%s'. Return the backendNodeId number and nothing else. Use thinking and identify what each element represents and find the most suitable one."
							.formatted(snapshot, element))
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
