package dev.evilbrowse.tools.specs;

import dev.evilbrowse.tools.SnapshotTool;
import dev.evilbrowse.windows.MainWindow;
import io.github.ollama4j.tools.Tools;

public class SnapshotToolSpec {
	private final MainWindow parent;
	private final SnapshotTool snapshotTool;

	public SnapshotToolSpec(MainWindow parent) {
		this.parent = parent;
		snapshotTool = new SnapshotTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("get_dom_snapshot")
				.description(
						"Gets a snapshot of the DOM nodes on the current page the user is on. Use this tool sparingly and only if you really need to extract content. If you need to find an element, you should always attempt to use find_element first. Do not use this tool to find an element.")
				.build()).toolFunction(args -> {
					try {
						final String url = parent.currentBrowser.getURL();
						System.out.println("Taking snapshot...");
						final String snapshot = snapshotTool.takeSnapshot().get();
						System.out.printf("Snapshot: %s\n", snapshot);
						return "Use this JSON snapshot of the DOM to understand the page (" + url + ") better: '"
								+ snapshot + "'\nThe user's original prompt was: '" + parent.getLatestAIMessage()
								+ "'. Use this new data to fufill the user's request.";
					} catch (Exception e) {
						return "Error: " + e.getMessage();
					}
				}).build();
	}
}
