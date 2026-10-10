package dev.ingstudios.turtlebrowse.handlers;

import java.util.List;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.network.CefRequest;
import org.cef.network.CefRequest.ResourceType;

import com.example.adblock.AdvtBlocker;

import dev.ingstudios.turtlebrowse.managers.AdblockManager;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseResourceRequestHandler extends CefResourceRequestHandlerAdapter {
	private AdvtBlocker blocker;
	private final MainWindow parent;
	private AdblockManager adblockManager;

	public TurtlebrowseResourceRequestHandler(MainWindow parent) {
		this.parent = parent;
		Thread.ofVirtual().start(() -> {
			adblockManager = AdblockManager.getInstance(parent);
			final List<String> rules = adblockManager.getRules();
			blocker = AdvtBlocker.createInstance(rules);
		});
	}

	@Override
	public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) {
		final String resourceType = resourceTypeToString(request.getResourceType());

		final boolean result = blocker.checkUrls(request.getURL(), browser.getURL(), resourceType);
		if (result && parent.adblockEnabled) {
			System.out.println("Blocked resource: " + request.getURL());
			return true;
		}

		return false;
	}

	private String resourceTypeToString(ResourceType resourceType) {
		switch (resourceType) {
			case RT_SCRIPT: {
				return "script";
			}
			case RT_IMAGE: {
				return "image";
			}
			case RT_STYLESHEET: {
				return "stylesheet";
			}
			case RT_OBJECT: {
				return "object";
			}
			case RT_SUB_FRAME: {
				return "subdocument";
			}
			case RT_XHR: {
				return "xmlhttprequest";
			}
			case RT_PING: {
				return "ping";
			}
			case RT_MEDIA: {
				return "media";
			}
			case RT_FONT_RESOURCE: {
				return "font";
			}
			case RT_FAVICON: {
				return "image";
			}
			case RT_PLUGIN_RESOURCE: {
				return "object";
			}
			default: {
				return "other";
			}
		}
	}
}
