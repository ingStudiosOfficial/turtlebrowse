package dev.evilbrowse.handlers;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;

import dev.evilbrowse.db.ProfileDatabase.HistoryItem;
import dev.evilbrowse.windows.MainWindow;
import javafx.application.Platform;

public class EvilBrowseDisplayHandler extends CefDisplayHandlerAdapter {
	private final MainWindow parent;
	// Serializes history writes off the CEF IO threads: rapid tabs opening at
	// once used to hammer Nitrite with concurrent write txns and stall loads.
	private final ExecutorService historyWriter = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "evilbrowse-history");
		t.setDaemon(true);
		return t;
	});
	private final ConcurrentHashMap<String, Long> lastRecordedByUrl = new ConcurrentHashMap<>();

	public EvilBrowseDisplayHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onTitleChange(CefBrowser browser, String title) {
		if (browser == null || title == null || title.isBlank()) {
			return;
		}
		if (browser != parent.currentBrowser)
			return;

		try {
			parent.titleMap.put(browser, title);
		} catch (Exception ignored) {
			return;
		}

		Platform.runLater(() -> {
			try {
				parent.tabBar.setTabTitle(browser, title);
			} catch (Exception ignored) {
			}
		});

		SwingUtilities.invokeLater(() -> {
			try {
				parent.updateWindowTitle(title);
			} catch (Exception ignored) {
			}
		});
	}

	@Override
	public void onAddressChange(CefBrowser cefBrowser, CefFrame frame, String url) {
		if (cefBrowser == null || url == null) {
			return;
		}
		// Only record main-frame navigations for the active tab. Previously every
		// sub-frame fired and background tabs were filtered by currentBrowser check
		// racing with tab switches, causing missing/duplicated history entries.
		if (frame != null && !frame.isMain()) {
			return;
		}
		if (cefBrowser != parent.currentBrowser)
			return;
		System.out.print("Navigated to: ");
		System.out.println(url);

		if (!url.startsWith("evilbrowse://") && !url.startsWith("turtlebrowse://")
				&& !url.startsWith("about:blank")) {
			// Dedupe: same URL re-reported within 5s (redirects/reloads/rapid
			// tab storms) is recorded once.
			final long now = System.currentTimeMillis();
			final Long last = lastRecordedByUrl.get(url);
			if (last == null || now - last > 5000) {
				lastRecordedByUrl.put(url, now);
				if (lastRecordedByUrl.size() > 200) {
					lastRecordedByUrl.clear();
				}
				final String titleGuess;
				try {
					String t = parent.titleMap.get(cefBrowser);
					titleGuess = (t != null && !t.isBlank()) ? t : url;
				} catch (Exception e) {
					continueRecording(url, url);
					return;
				}
				continueRecording(url, titleGuess);
			}
		}

		Platform.runLater(() -> {
			try {
				parent.addressBar.updateUrl(url);
			} catch (Exception ignored) {
			}
		});
	}

	private void continueRecording(String url, String title) {
		try {
			historyWriter.execute(() -> {
				try {
					parent.profileDatabase.addHistory(new HistoryItem(url, title,
							Instant.now().toEpochMilli(), UUID.randomUUID()));
				} catch (Exception e) {
					System.err.println("History write failed: " + e.getMessage());
				}
			});
		} catch (Exception e) {
			System.err.println("History queue failed: " + e.getMessage());
		}
	}

	@Override
	public void onFullscreenModeChange(CefBrowser browser, boolean fullscreen) {
		SwingUtilities.invokeLater(() -> parent.toggleFullscreen(fullscreen));
	}
}
