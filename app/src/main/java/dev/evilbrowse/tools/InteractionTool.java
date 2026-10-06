package dev.evilbrowse.tools;

import org.cef.browser.CefDevToolsClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.evilbrowse.windows.MainWindow;

public class InteractionTool {
	private final MainWindow parent;

	public InteractionTool(MainWindow parent) {
		this.parent = parent;
	}

	public String clickElement(String backendNodeId) {
		final CefDevToolsClient devToolsClient = parent.currentBrowser.getDevToolsClient();

		System.out.printf("Backend node ID: %s\n", backendNodeId);

		try {
			final JsonObject resolveResult = devToolsClient
					.executeDevToolsMethod("DOM.getBoxModel", "{\"backendNodeId\":" + backendNodeId + "}")
					.thenApply(response -> JsonParser.parseString(response).getAsJsonObject())
					.get();

			final JsonArray content = resolveResult.get("model").getAsJsonObject().get("content").getAsJsonArray();
			final double x = (content.get(0).getAsDouble() + content.get(2).getAsDouble()) / 2;
			final double y = (content.get(1).getAsDouble() + content.get(5).getAsDouble()) / 2;

			final String[] types = { "mousePressed", "mouseReleased" };
			for (String type : types) {
				JsonObject p = new JsonObject();
				p.addProperty("type", type);
				p.addProperty("x", x);
				p.addProperty("y", y);
				p.addProperty("button", "left");
				p.addProperty("clickCount", 1);
				devToolsClient.executeDevToolsMethod("Input.dispatchMouseEvent", p.toString());
			}

			try {
				return "Successfully clicked the element. You may need to call get_dom_snapshot again to get the latest changes if needed.";
			} catch (Exception e) {
				e.printStackTrace();
				return "An unexpected error occurred while taking a snapshot.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while clicking the element.";
		}
	}

	public String typeElement(String backendNodeId, String input) {
		final CefDevToolsClient devToolsClient = parent.currentBrowser.getDevToolsClient();

		try {
			devToolsClient.executeDevToolsMethod("DOM.focus", "{\"backendNodeId\":" + backendNodeId + "}");
			Thread.sleep(500);
			final JsonObject params = new JsonObject();
			params.addProperty("text", input);
			devToolsClient.executeDevToolsMethod("Input.insertText", params.toString());
			return "Successfully typed '" + input
					+ "' in the element. Use the interact_with_page tool and 'enter' action if you need to press enter to trigger a follow up action.";
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while inputting in the element.";
		}
	}

	public String pressEnter() {
		final CefDevToolsClient devToolsClient = parent.currentBrowser.getDevToolsClient();

		try {
			final JsonObject params = new JsonObject();
			params.addProperty("key", "Enter");
			params.addProperty("code", "Enter");
			params.addProperty("windowsVirtualKeyCode", 13);
			params.addProperty("nativeVirtualKeyCode", 13);

			params.addProperty("type", "keyDown");
			devToolsClient.executeDevToolsMethod("Input.dispatchKeyEvent", params.toString());

			Thread.sleep(100);

			params.addProperty("type", "keyUp");

			return "Succesafully executed enter event.";
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while pressing enter.";
		}
	}
}
