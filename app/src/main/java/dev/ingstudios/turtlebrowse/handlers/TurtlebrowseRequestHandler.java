package dev.ingstudios.turtlebrowse.handlers;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.network.CefRequest;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseRequestHandler extends CefRequestHandlerAdapter {
	private CefBrowser aiSidebarBrowser;
	private final MainWindow parent;

	public TurtlebrowseRequestHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture,
			boolean is_redirect) {
		System.out.printf("Browser identifier: %s\n", Integer.toString(browser.getIdentifier()));
		if (browser == aiSidebarBrowser) {
			if (!request.getURL().contains("turtlebrowse://chat")) {
				SwingUtilities.invokeLater(() -> {
					parent.createTab(request.getURL());
				});
				return true;
			}
		}

		return false;
	}

	public void setAiBrowser(CefBrowser browser) {
		aiSidebarBrowser = browser;
	}
}
