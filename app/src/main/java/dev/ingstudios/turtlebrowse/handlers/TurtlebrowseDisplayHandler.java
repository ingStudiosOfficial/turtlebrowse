package dev.ingstudios.turtlebrowse.handlers;

import java.time.Instant;
import java.util.UUID;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;

import dev.ingstudios.turtlebrowse.db.ProfileDatabase.HistoryItem;
import dev.ingstudios.turtlebrowse.windows.MainWindow;
import javafx.application.Platform;

public class TurtlebrowseDisplayHandler extends CefDisplayHandlerAdapter {
	private final MainWindow parent;

	public TurtlebrowseDisplayHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onTitleChange(CefBrowser browser, String title) {
		if (browser != parent.currentBrowser)
			return;

		parent.titleMap.put(browser, title);

		Platform.runLater(() -> {
			parent.tabBar.setTabTitle(browser, title);
		});

		SwingUtilities.invokeLater(() -> {
			parent.updateWindowTitle(title);
		});
	}

	@Override
	public void onAddressChange(CefBrowser cefBrowser, CefFrame frame, String url) {
		if (cefBrowser != parent.currentBrowser)
			return;
		System.out.print("Navigated to: ");
		System.out.println(url);

		if (!cefBrowser.getURL().startsWith("turtlebrowse://")) {
			parent.profileDatabase.addHistory(new HistoryItem(url,
					parent.titleMap.getOrDefault(cefBrowser, cefBrowser.getURL()), Instant.now().toEpochMilli(),
					UUID.randomUUID()));
		}

		Platform.runLater(() -> parent.addressBar.updateUrl(url));
	}

	@Override
	public void onFullscreenModeChange(CefBrowser browser, boolean fullscreen) {
		parent.toggleFullscreen(fullscreen);
	}
}
