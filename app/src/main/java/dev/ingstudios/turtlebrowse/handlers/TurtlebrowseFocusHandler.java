package dev.ingstudios.turtlebrowse.handlers;

import org.cef.browser.CefBrowser;
import org.cef.handler.CefFocusHandlerAdapter;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseFocusHandler extends CefFocusHandlerAdapter {
	private final MainWindow parent;

	public TurtlebrowseFocusHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onGotFocus(CefBrowser browser) {
		System.out.println("Browser got focus.");
		parent.isUiFocused.set(false);
	}

	@Override
	public void onTakeFocus(CefBrowser browser, boolean next) {
		parent.isUiFocused.set(false);
	}

	@Override
	public boolean onSetFocus(CefBrowser browser, FocusSource source) {
		return false;
	}
}
