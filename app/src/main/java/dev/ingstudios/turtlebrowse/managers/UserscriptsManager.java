package dev.ingstudios.turtlebrowse.managers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import dev.ingstudios.turtlebrowse.userscript.UserscriptParser;
import dev.ingstudios.turtlebrowse.userscript.UserscriptParser.UserscriptMetadata;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class UserscriptsManager {
	private static UserscriptsManager instance;
	private final MainWindow parent;

	private UserscriptsManager(MainWindow parent) {
		this.parent = parent;
	}

	public List<UserscriptMetadata> getUserscripts() {
		final Path dir = parent.getProfileCachePath("userscripts");
		try (Stream<Path> stream = Files.list(dir)) {
			final List<UserscriptMetadata> metadatas = new ArrayList<>();
			stream.filter(path -> !Files.isDirectory(path)).forEach(path -> {
				try {
					System.out.println("Found userscript: " + path.toAbsolutePath());
					final String content = Files.readString(path);
					final UserscriptMetadata parsed = UserscriptParser.parse(content);
					metadatas.add(parsed);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			return metadatas;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public static synchronized UserscriptsManager getInstance(MainWindow parent) {
		if (instance == null) {
			instance = new UserscriptsManager(parent);
		}
		return instance;
	}
}
