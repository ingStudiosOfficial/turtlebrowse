package dev.evilbrowse.tools;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.evilbrowse.windows.MainWindow;

public class SnapshotTool {
	private final MainWindow parent;
	private static final java.util.Set<String> IGNORED_ROLES = java.util.Set.of(
			"InlineTextBox", "StaticText", "LineBreak", "none", "generic");

	public SnapshotTool(MainWindow parent) {
		this.parent = parent;
	}

	public CompletableFuture<String> takeSnapshot() {
		return parent.currentBrowser.getDevToolsClient().executeDevToolsMethod("Accessibility.getFullAXTree", null)
				.thenApply(response -> {
					final String yaml = formatSnapshot(response);
					System.out.printf("Snapshot response: %s\n", yaml);
					return yaml;
				});
	}

	private String formatSnapshot(String json) {
		final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		final JsonArray nodes = root.get("nodes").getAsJsonArray();
		final StringBuilder jsonlOutput = new StringBuilder();

		for (final JsonElement el : nodes) {
			final JsonObject node = el.getAsJsonObject();
			if (!node.has("backendDOMNodeId"))
				continue;

			if (node.has("ignored") && node.get("ignored").getAsBoolean())
				continue;

			String role = "";
			if (node.has("role")) {
				role = node.getAsJsonObject("role").get("value").getAsString();
			} else {
				continue;
			}

			if (IGNORED_ROLES.contains(role))
				continue;

			boolean isFocusable = false;
			final JsonObject elementJson = new JsonObject();

			elementJson.addProperty("backendNodeId", node.get("backendDOMNodeId").getAsString());
			elementJson.addProperty("role", role);

			if (node.has("name")) {
				String name = node.getAsJsonObject("name").get("value").getAsString();
				if (!name.isBlank())
					elementJson.addProperty("name", name);
			}

			if (node.has("value")) {
				String val = node.getAsJsonObject("value").get("value").getAsString();
				if (!val.isBlank())
					elementJson.addProperty("value", val);
			}

			if (node.has("properties")) {
				for (final JsonElement propEl : node.getAsJsonArray("properties")) {
					final JsonObject prop = propEl.getAsJsonObject();
					final String key = prop.get("name").getAsString();
					if (!java.util.Set
							.of("focusable", "focused", "disabled", "checked", "expanded", "required", "invalid")
							.contains(key))
						continue;

					final JsonElement val = prop.get("value").getAsJsonObject().get("value");
					if (key.equals("focusable") && val.getAsBoolean())
						isFocusable = true;
				}
			}

			if (!isFocusable || java.util.Set.of("listitem", "heading", "generic", "region").contains(role)
					|| !node.has("name"))
				continue;

			jsonlOutput.append(elementJson.toString()).append("\n");
		}

		System.out.printf("JSON Snapshot:\n%s\n", jsonlOutput.toString());
		return jsonlOutput.toString();
	}
}
