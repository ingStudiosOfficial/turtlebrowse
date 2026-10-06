package dev.evilbrowse.handlers;

import java.util.ArrayList;
import java.util.List;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

public class EvilBrowseLoadHandler extends CefLoadHandlerAdapter {
	private final List<JSQueueItem> queueStack = new java.util.concurrent.CopyOnWriteArrayList<>();
	private final java.util.Set<Integer> readyBrowsers = java.util.Collections
			.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
	private volatile dev.evilbrowse.windows.MainWindow parent;

	public void setParent(dev.evilbrowse.windows.MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
		if (browser == null) {
			return;
		}
		final dev.evilbrowse.windows.MainWindow p = parent;
		if (p == null || p.tabBar == null) {
			return;
		}
		try {
			p.tabBar.setTabLoading(browser, isLoading);
		} catch (Exception ignored) {
		}
	}

	@Override
	public void onLoadEnd(CefBrowser browser, CefFrame frame, int statusCode) {
		// Only treat the main frame as "ready". Sub-frames/iframes fire onLoadEnd
		// too and previously caused duplicate JS execution and premature ready flags.
		if (frame != null && !frame.isMain()) {
			return;
		}
		if (browser == null) {
			return;
		}
		readyBrowsers.add(browser.getIdentifier());
		final List<JSQueueItem> toRemove = new ArrayList<>();
		for (final JSQueueItem item : queueStack) {
			System.out.printf("Item: %s\n", item.code);
			if (item.isSame(browser.getIdentifier())) {
				browser.getMainFrame().executeJavaScript(item.code, item.url, 0);
				toRemove.add(item);
			}
		}
		queueStack.removeAll(toRemove);
	}

	public void addToQueueStack(JSQueueItem item) {
		queueStack.add(item);
	}

	public boolean isBrowserReady(Integer id) {
		return readyBrowsers.contains(id);
	}

	public void removeBrowser(Integer id) {
		readyBrowsers.remove(id);
	}

	public record JSQueueItem(int identifier, String code, String url) {
		public boolean isSame(int id) {
			return id == identifier;
		}
	}
}
