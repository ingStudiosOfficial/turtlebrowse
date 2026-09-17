package dev.ingstudios.turtlebrowse.handlers;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseLifeSpanHandler extends CefLifeSpanHandlerAdapter {
	private final MainWindow parent;

	public TurtlebrowseLifeSpanHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String targetUrl, String targetFrameName) {
		SwingUtilities.invokeLater(() -> {
			parent.createTab(targetUrl);
		});
		return true;
	}

	@Override
	public void onBeforeClose(CefBrowser browser) {
		parent.loadHandler.removeBrowser(browser.getIdentifier());
	}
}
