package dev.ingstudios.turtlebrowse.managers;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;

public class FileSystemManager {
	private static FileSystemManager instance;
	private File downloadsDirectory = new File(System.getProperty("user.home"));

	private FileSystemManager() {
	}

	public static synchronized FileSystemManager getInstance() {
		if (instance == null) {
			instance = new FileSystemManager();
		}
		return instance;
	}

	public void setDownloadsDirectory(String dir) {
		downloadsDirectory = new File(dir);
	}

	public String chooseDownloadsDirectory() {
		try {
			@SuppressWarnings("null")
			final File directory = CompletableFuture.supplyAsync(() -> {
				final DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Downloads directory");

				final File selectedDirectory = chooser.showDialog(null);

				if (selectedDirectory == null) {
					return new File(System.getProperty("user.home"));
				}

				return selectedDirectory;
			}, Platform::runLater).get();

			downloadsDirectory = directory;
			return directory.toString();
		} catch (Exception e) {
			return System.getProperty("user.home");
		}
	}

	public File getDownloadsDirectory() {
		return downloadsDirectory;
	}
}
