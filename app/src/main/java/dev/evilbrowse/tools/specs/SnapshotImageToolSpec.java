package dev.evilbrowse.tools.specs;

import dev.evilbrowse.tools.SnapshotImageTool;
import dev.evilbrowse.windows.MainWindow;
import io.github.ollama4j.tools.Tools;

public class SnapshotImageToolSpec {
	private final MainWindow parent;
	private final SnapshotImageTool snapshotImageTool;

	public SnapshotImageToolSpec(MainWindow parent) {
		this.parent = parent;
		snapshotImageTool = new SnapshotImageTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("get_page_screenshot")
				.description(
						"Gets a screenshot of the current page the user is on. Use this tool if the user requests a screenshot of the current page, or you need additional context of the current page that get_dom_snapshot does not provide to do actions such as navigation, summarization, and more. Use this when the user does not specify which page in their prompt as well.")
				.build()).toolFunction(args -> {
					try {
						final String url = parent.currentBrowser.getURL();
						dev.evilbrowse.ollama.OllamaChat _s = parent.getOllamaSession();
						if (_s == null) {
							return "Error: AI session is not available.";
						}
						_s.pageScreenshot = snapshotImageTool.takeSnapshotImage().get();
						System.out.println("Attached screenshot to pageScreenshot variable.");
						return "Successfully took a screenshot of the current page (" + url
								+ "), attaching on next prompt. Do not try to elaborate further on this prompt. Provide a response like 'I have taken a screenshot, analyzing on next prompt.'. There will be no screenshot provided in this response. End the response quickly so the system can attach the screenshot faster.";
					} catch (Exception e) {
						return "Error: " + e.getMessage();
					}
				}).build();
	}
}
