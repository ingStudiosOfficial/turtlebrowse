package dev.ingstudios.turtlebrowse.handlers;

import java.awt.event.KeyEvent;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefKeyboardHandlerAdapter;
import org.cef.misc.EventFlags;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class CefKeyboardHandler extends CefKeyboardHandlerAdapter {
	private MainWindow parent;
	private String startUrl;

	public CefKeyboardHandler(MainWindow parent, String startUrl) {
		System.out.println("New keyboard handler created.");
		this.parent = parent;
		this.startUrl = startUrl;
	}

	@Override
	public boolean onKeyEvent(CefBrowser browser, CefKeyEvent event) {
		System.out.println("Key pressed.");

		if (event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN) {
			boolean ctrlPressed = (event.modifiers & EventFlags.EVENTFLAG_CONTROL_DOWN) != 0;
			boolean shiftPressed = (event.modifiers & EventFlags.EVENTFLAG_SHIFT_DOWN) != 0;
			boolean altPressed = (event.modifiers & EventFlags.EVENTFLAG_ALT_DOWN) != 0;
			System.out.printf("Ctrl pressed: %s\n", ctrlPressed);

			if (ctrlPressed && shiftPressed && event.windows_key_code == KeyEvent.VK_I) { // DevTools (Ctrl + Shift + I)
				parent.createDevTools();
				return true;
			} else if (ctrlPressed && event.windows_key_code == KeyEvent.VK_T) { // New tab (Ctrl + T)
				System.out.println("Ctrl + T pressed.");
				parent.createTab(startUrl, true);
				return true;
			} else if (ctrlPressed && event.windows_key_code == KeyEvent.VK_W) { // Close current tab (Ctrl + W)
				System.out.println("Ctrl + W pressed.");
				parent.closeCurrentTab();
				return true;
			} else if (ctrlPressed && event.windows_key_code == KeyEvent.VK_L) { // Focus address field (Ctrl + L)
				System.out.println("Ctrl + L pressed.");
				parent.addressBar.focusAddressField();
				return true;
			} else if (altPressed && event.windows_key_code == KeyEvent.VK_LEFT) { // Navigates back (Alt + <)
				if (parent.currentBrowser.canGoBack())
					parent.currentBrowser.goBack();
				return true;
			} else if (altPressed && event.windows_key_code == KeyEvent.VK_RIGHT) { // Navigates forward (Alt + >)
				if (parent.currentBrowser.canGoForward())
					parent.currentBrowser.goForward();
				return true;
			} else if (ctrlPressed && event.windows_key_code == KeyEvent.VK_R) { // Reloads the page (Ctrl + R)
				parent.currentBrowser.reload();
				return true;
			} else if (ctrlPressed && shiftPressed && event.windows_key_code == KeyEvent.VK_TAB) { // Switches to the
																									// next tab (Ctrl +
				// Tab)
				System.out.println("Ctrl + Shift + Tab pressed.");

				final int currentIndex = parent.openedBrowserTabs.indexOf(parent.currentBrowser);
				final int size = parent.openedBrowserTabs.size();
				if (size > 0) {
					final int previousIndex = (currentIndex - 1 + size) % size;
					parent.showTab(parent.openedBrowserTabs.get(previousIndex));
				}
				return true;
			} else if (ctrlPressed && event.windows_key_code == KeyEvent.VK_TAB) { // Switches to the next tab (Ctrl +
																					// Tab)
				System.out.println("Ctrl + Tab pressed.");

				final int currentIndex = parent.openedBrowserTabs.indexOf(parent.currentBrowser);
				final int size = parent.openedBrowserTabs.size();
				if (size > 0) {
					final int nextIndex = (currentIndex + 1) % size;
					parent.showTab(parent.openedBrowserTabs.get(nextIndex));
				}
				return true;
			} else if (ctrlPressed && event.windows_key_code == KeyEvent.VK_Q) { // Quits the browser
				System.out.println("Ctrl + Q pressed.");
				parent.dispose();
				return true;
			}
		}

		return false;
	}
}
