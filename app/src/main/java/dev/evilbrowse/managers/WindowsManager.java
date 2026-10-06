package dev.evilbrowse.managers;

import java.util.ArrayList;
import java.util.List;

import dev.evilbrowse.db.MainDatabase;
import javafx.application.Platform;

public class WindowsManager {
	private static WindowsManager instance;
	private final List<WindowItem> windows;

	private WindowsManager() {
		windows = java.util.Collections.synchronizedList(new ArrayList<>());
	}

	public static synchronized WindowsManager getInstance() {
		if (instance == null) {
			instance = new WindowsManager();
		}
		return instance;
	}

	public List<WindowItem> getWindows() {
		synchronized (windows) {
			return new ArrayList<>(windows);
		}
	}

	public void addWindow(WindowItem item) {
		windows.add(item);
	}

	public void removeWindow(String id) {
		System.out.printf("Removing window: %s\n", id);
		synchronized (windows) {
			windows.removeIf(window -> {
				System.out.printf("Window ID: %s\n", window.id());
				return window.id() != null && window.id().equals(id);
			});
			System.out.printf("Windows size: %s\n", String.valueOf(windows.size()));
			if (windows.isEmpty()) {
				System.out.println("Windows is empty, exiting...");
				try {
					Platform.runLater(() -> {
						try {
							Platform.exit();
						} catch (Exception ignored) {
						}
					});
				} catch (Exception ignored) {
				}
				try {
					MainDatabase.getInstance().closeDb();
				} catch (Exception ignored) {
				}
				System.exit(0);
			}
		}
	}

	public boolean hasWindows() {
		synchronized (windows) {
			return !windows.isEmpty();
		}
	}

	public record WindowItem(String id, Object classType) {
	}
}
