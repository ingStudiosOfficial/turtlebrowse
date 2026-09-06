package dev.ingstudios.turtlebrowse.handlers;

import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

import dev.ingstudios.turtlebrowse.windows.MainWindow;

import java.awt.AWTEvent;

public class SwingKeyboardHandler {
	public SwingKeyboardHandler(MainWindow parent, String startUrl) {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				if (event instanceof KeyEvent) {
					KeyEvent keyEvent = (KeyEvent) event;
					if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {
						int keyCode = keyEvent.getKeyCode();
						System.out.println("Global key pressed: " + KeyEvent.getKeyText(keyCode));

						if (keyCode == KeyEvent.VK_I && keyEvent.isControlDown() && keyEvent.isShiftDown()) { // DevTools
																												// (Ctrl
																												// +
																												// Shift
																												// + I)
							keyEvent.consume();
							parent.createDevTools();
						} else if (keyCode == KeyEvent.VK_T && keyEvent.isControlDown()) { // New tab (Ctrl + T)
							keyEvent.consume();
							System.out.println("Ctrl + T detected, creating a new tab.");
							parent.createTab(startUrl, true);
						} else if (keyCode == KeyEvent.VK_W && keyEvent.isControlDown()) { // Close current tab (Ctrl
																							// + W)
							keyEvent.consume();
							System.out.println("Ctrl + W pressed.");
							parent.closeCurrentTab();
						} else if (keyCode == KeyEvent.VK_L && keyEvent.isControlDown()) { // Focus address field (Ctrl
																							// + L)
							keyEvent.consume();
							System.out.println("Ctrl + L pressed.");
							parent.addressBar.focusAddressField();
						} else if (keyCode == KeyEvent.VK_LEFT && keyEvent.isAltDown()) { // Navigates back (Alt + <)
							keyEvent.consume();
							if (parent.currentBrowser.canGoBack())
								parent.currentBrowser.goBack();
						} else if (keyCode == KeyEvent.VK_RIGHT && keyEvent.isAltDown()) { // Navigates forward (Alt +
																							// >)
							keyEvent.consume();
							if (parent.currentBrowser.canGoForward())
								parent.currentBrowser.goForward();
						} else if (keyCode == KeyEvent.VK_R && keyEvent.isControlDown()) { // Reloads the page (Ctrl +
							// R)
							keyEvent.consume();
							parent.currentBrowser.reload();
						} else if (keyCode == KeyEvent.VK_TAB && keyEvent.isControlDown() && keyEvent.isShiftDown()) { // Switches
																														// to
																														// the
																														// previous
																														// tab
							// (Ctrl + Shift + Tab)
							keyEvent.consume();
							System.out.println("Ctrl + Shift + Tab pressed.");
							final int currentIndex = parent.openedBrowserTabs.indexOf(parent.currentBrowser);
							final int size = parent.openedBrowserTabs.size();
							if (size > 0) {
								final int previousIndex = (currentIndex - 1 + size) % size;
								parent.showTab(parent.openedBrowserTabs.get(previousIndex));
							}
						} else if (keyCode == KeyEvent.VK_TAB && keyEvent.isControlDown()) { // Switches to the next tab
																								// (Ctrl + Tab)
							keyEvent.consume();
							System.out.println("Ctrl + Tab pressed.");
							final int currentIndex = parent.openedBrowserTabs.indexOf(parent.currentBrowser);
							final int size = parent.openedBrowserTabs.size();
							if (size > 0) {
								final int nextIndex = (currentIndex + 1) % size;
								parent.showTab(parent.openedBrowserTabs.get(nextIndex));
							}
						} else if (keyCode == KeyEvent.VK_Q && keyEvent.isControlDown()) { // Quits the browser
							keyEvent.consume();
							System.out.println("Ctrl + Q pressed.");
							parent.dispose();
						}
					}
				}
			}
		}, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	}
}
