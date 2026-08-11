package dev.ingstudios.turtlebrowse.ollama;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import dev.ingstudios.turtlebrowse.tools.specs.FetchToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.FindElementToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.InteractionToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.NavigateSiteToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.SearXNGToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.SnapshotImageToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.SnapshotToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.SummarizePageToolSpec;
import dev.ingstudios.turtlebrowse.windows.MainWindow;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.chat.OllamaChatStreamObserver;
import io.github.ollama4j.models.generate.OllamaGenerateTokenHandler;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.tools.Tools;
import io.github.ollama4j.utils.Options;
import io.github.ollama4j.utils.OptionsBuilder;

public class OllamaChat {
	private Ollama ollama;
	private OllamaChatRequest builder;
	final private List<OllamaChatMessage> history = new ArrayList<>();
	public byte[] pageScreenshot;
	public String latestMessage;

	public OllamaChat(String userAgent, MainWindow parent) throws OllamaException {
		try {
			ollama = new Ollama();
			ollama.setRequestTimeoutSeconds(120);

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

			final Tools.Tool searchToolSpec = new SearXNGToolSpec(userAgent, parent).getSpecification();
			final Tools.Tool fetchToolSpec = new FetchToolSpec(userAgent, parent).getSpecification();
			final Tools.Tool snapshotToolSpec = new SnapshotToolSpec(parent).getSpecification();
			final Tools.Tool snapshotImageToolSpec = new SnapshotImageToolSpec(parent).getSpecification();
			final Tools.Tool interactionToolSpec = new InteractionToolSpec(parent).getSpecification();
			final Tools.Tool navigateSiteToolSpec = new NavigateSiteToolSpec(parent).getSpecification();
			final Tools.Tool summarizePageToolSpec = new SummarizePageToolSpec(parent).getSpecification();

			ollama.registerTool(searchToolSpec);
			ollama.registerTool(fetchToolSpec);
			ollama.registerTool(snapshotToolSpec);
			ollama.registerTool(snapshotImageToolSpec);
			ollama.registerTool(interactionToolSpec);
			ollama.registerTool(navigateSiteToolSpec);
			ollama.registerTool(summarizePageToolSpec);

			try {
				final Tools.Tool findElementToolSpec = new FindElementToolSpec(parent).getSpecification();
				ollama.registerTool(findElementToolSpec);
			} catch (OllamaException e) {
				System.out.printf("Failed to register findElementTool: %s\n", e.getMessage());
			}

			final Options options = new OptionsBuilder().setNumPredict(-1).build();

			builder = OllamaChatRequest.builder().withModel(chatModel).withOptions(options);
		} catch (OllamaException e) {
			System.out.printf("Error while initializing Ollama:", e.getMessage());
			throw e;
		}
	}

	public void prompt(String prompt, Consumer<String> onThinkChunk, Consumer<String> onResponseChunk,
			Consumer<String> onDone, Consumer<String> onError) {
		System.out.printf("Received prompt: %s\n", prompt);
		System.out.printf("History: %s", history.toString());

		latestMessage = prompt;

		if (history.isEmpty()) {
			final String directive = """
					Current date: %s

					Context:
					You are Tutel, an AI agent operating inside the Turtlebrowse browser.

					Rules:
					1. Try to use tools whenever possible and if required. For example, if the user wants you to search the web, use the search_web tool.
					2. If the user asks you to repeat the steps, do not try to tell the user that you have already performed the action. You should re-perform the action instead.
					3. Assume that the user wants to perform an action on the current page and browser context unlesss explicitly stated and use find_element if needed to find an element on the current page.

					Helpful Information:
					If you need to perform an action on the page, call find_element to find the element to interact with.
					Use interact_with_page to interact with elements on the page.
					"""
					.formatted(getCurrentDate());

			history.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, directive));
		}

		history.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));

		promptInternal(onThinkChunk, onResponseChunk, onDone, onError);
	}

	private void promptInternal(Consumer<String> onThinkChunk, Consumer<String> onResponseChunk,
			Consumer<String> onDone, Consumer<String> onError) {
		final OllamaChatRequest chatRequest = builder.withMessages(history).build();

		OllamaChatStreamObserver streamObserver = new OllamaChatStreamObserver();
		streamObserver.setThinkingStreamHandler(new OllamaGenerateTokenHandler() {
			@Override
			public void accept(String message) {
				onThinkChunk.accept(message);
			}
		});
		streamObserver.setResponseStreamHandler(new OllamaGenerateTokenHandler() {
			@Override
			public void accept(String message) {
				onResponseChunk.accept(message);
			}
		});

		try {
			final OllamaChatResult result = ollama.chat(chatRequest, streamObserver);
			final OllamaChatMessage assistantMessage = result.getResponseModel().getMessage();
			history.add(assistantMessage);
			System.out.printf("History: %s", history.toString());

			boolean hasScreenshotCall = pageScreenshot != null;
			System.out.printf("Has screenshot call: %b\n", hasScreenshotCall);

			if (!hasScreenshotCall) {
				onDone.accept(assistantMessage.getResponse());
				return;
			}

			System.out.println("Attaching screenshot...");

			final OllamaChatMessage screenshotSuccessMessage = new OllamaChatMessage(OllamaChatMessageRole.USER,
					"Screenshot has been attached.");
			final List<byte[]> imagestList = new ArrayList<>();
			imagestList.add(pageScreenshot);
			screenshotSuccessMessage.setImages(imagestList);
			history.add(screenshotSuccessMessage);
			pageScreenshot = null;
			promptInternal(onThinkChunk, onResponseChunk, onDone, onError);
		} catch (OllamaException e) {
			System.out.printf("Error while chatting: %s\n", e.getMessage());
			onError.accept("An unexpected error occurred while chatting.");
		}
	}

	private String getCurrentDate() {
		final LocalDateTime now = LocalDateTime.now();

		final String month = now.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH));
		final String yearAndTime = now.format(DateTimeFormatter.ofPattern("yyyy h:mm a", Locale.ENGLISH));
		final int day = now.getDayOfMonth();
		final String formattedDate = month + " " + day + " " + yearAndTime;

		return formattedDate;
	}
}
