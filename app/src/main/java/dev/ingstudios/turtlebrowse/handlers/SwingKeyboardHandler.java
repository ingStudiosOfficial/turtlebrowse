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
						final int keyCode = keyEvent.getKeyCode();
						final boolean ctrlPressed = keyEvent.isControlDown();
						final boolean shiftPressed = keyEvent.isShiftDown();
						final boolean altPressed = keyEvent.isAltDown();

						if (keyCode == KeyEvent.VK_I && ctrlPressed && shiftPressed) { // DevTools
																						// (Ctrl
																						// +
																						// Shift
																						// + I)
							keyEvent.consume();
							parent.createDevTools();
						} else if (keyCode == KeyEvent.VK_T && ctrlPressed) { // New tab (Ctrl + T)
							keyEvent.consume();
							System.out.println("Ctrl + T detected, creating a new tab.");
							parent.createTab(startUrl, true);
						} else if (keyCode == KeyEvent.VK_W && ctrlPressed) { // Close current tab (Ctrl
																				// + W)
							keyEvent.consume();
							System.out.println("Ctrl + W pressed.");
							parent.closeCurrentTab();
						} else if (keyCode == KeyEvent.VK_L && ctrlPressed) { // Focus address field (Ctrl
																				// + L)
							keyEvent.consume();
							System.out.println("Ctrl + L pressed.");
							parent.addressBar.focusAddressField();
						} else if (keyCode == KeyEvent.VK_LEFT && altPressed) { // Navigates back (Alt + <)
							keyEvent.consume();
							if (parent.currentBrowser.canGoBack())
								parent.currentBrowser.goBack();
						} else if (keyCode == KeyEvent.VK_RIGHT && altPressed) { // Navigates forward (Alt +
																					// >)
							keyEvent.consume();
							if (parent.currentBrowser.canGoForward())
								parent.currentBrowser.goForward();
						} else if (keyCode == KeyEvent.VK_R && ctrlPressed) { // Reloads the page (Ctrl +
							// R)
							keyEvent.consume();
							parent.currentBrowser.reload();
						} else if (keyCode == KeyEvent.VK_TAB && ctrlPressed && shiftPressed) { // Switches
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
						} else if (keyCode == KeyEvent.VK_TAB && ctrlPressed) { // Switches to the next tab
																				// (Ctrl + Tab)
							keyEvent.consume();
							System.out.println("Ctrl + Tab pressed.");
							final int currentIndex = parent.openedBrowserTabs.indexOf(parent.currentBrowser);
							final int size = parent.openedBrowserTabs.size();
							if (size > 0) {
								final int nextIndex = (currentIndex + 1) % size;
								parent.showTab(parent.openedBrowserTabs.get(nextIndex));
							}
						} else if (keyCode == KeyEvent.VK_Q && ctrlPressed) { // Quits the browser
							keyEvent.consume();
							System.out.println("Ctrl + Q pressed.");
							parent.dispose();
						} else if (keyCode == KeyEvent.VK_H && ctrlPressed) { // Opens history
							keyEvent.consume();
							System.out.println("Ctrl + H pressed.");
							parent.openHistory();
						}
					}
				}
			}
		}, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	}
}
