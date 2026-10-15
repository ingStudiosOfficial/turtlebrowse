package dev.ingstudios.turtlebrowse.handlers;

import java.awt.KeyboardFocusManager;

import org.cef.browser.CefBrowser;
import org.cef.handler.CefFocusHandlerAdapter;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseFocusHandler extends CefFocusHandlerAdapter {
	public TurtlebrowseFocusHandler(MainWindow parent) {
	}

	@Override
	public void onGotFocus(CefBrowser browser) {
		System.out.println("Browser got focus.");
		KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
	}

	@Override
	public void onTakeFocus(CefBrowser browser, boolean next) {
	}

	@Override
	public boolean onSetFocus(CefBrowser browser, FocusSource source) {
		return false;
	}
}
