package dev.evilbrowse.handlers;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import dev.evilbrowse.windows.MainWindow;

public class EvilBrowseLifeSpanHandler extends CefLifeSpanHandlerAdapter {
	private final MainWindow parent;

	public EvilBrowseLifeSpanHandler(MainWindow parent) {
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
