package dev.evilbrowse.handlers;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;

import dev.evilbrowse.windows.MainWindow;

public class EvilBrowseRequestHandler extends CefRequestHandlerAdapter {
	private CefBrowser aiSidebarBrowser;
	private final MainWindow parent;
	private final EvilBrowseResourceRequestHandler resourceRequestHandler;

	public EvilBrowseRequestHandler(MainWindow parent) {
		this.parent = parent;
		resourceRequestHandler = new EvilBrowseResourceRequestHandler(parent);
	}

	@Override
	public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture,
			boolean is_redirect) {
		if (browser == null || request == null) {
			return false;
		}
		String url = null;
		try {
			url = request.getURL();
		} catch (Exception ignored) {
			return false;
		}
		if (url == null) {
			return false;
		}
		System.out.printf("Browser identifier: %s\n", Integer.toString(browser.getIdentifier()));
		// aiSidebarBrowser is null until the lazy AI sidebar creates its browser.
		final CefBrowser aiBrowser = aiSidebarBrowser;
		if (aiBrowser != null && browser == aiBrowser) {
			// Strict prefix check: the AI sidebar may only stay on evilbrowse://chat.
			// The old contains() check was bypassable (e.g. https://evil/?evilbrowse://chat)
			// and over-blocked (e.g. evilbrowse://chat.evil).
			if (!url.startsWith("evilbrowse://chat") && !url.startsWith("turtlebrowse://chat")) {
				final String target = url;
				SwingUtilities.invokeLater(() -> {
					try {
						parent.createTab(target);
					} catch (Exception ignored) {
					}
				});
				return true;
			}
		}

		return false;
	}

	public void setAiBrowser(CefBrowser browser) {
		aiSidebarBrowser = browser;
	}

	public void setAdblockEnabled(boolean enabled) {
		try {
			resourceRequestHandler.setEnabled(enabled);
		} catch (Exception ignored) {
		}
	}

	public EvilBrowseResourceRequestHandler getResourceRequestHandler() {
		return resourceRequestHandler;
	}

	@Override
	public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request,
			boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) {
		return resourceRequestHandler;
	}
}
