package dev.ingstudios.turtlebrowse.managers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

	public static synchronized UserscriptsManager getInstance(MainWindow parent) {
		if (instance == null) {
			instance = new UserscriptsManager(parent);
		}
		return instance;
	}

	public List<UserscriptMetadata> getUserscripts() {
		final Path dir = parent.getProfileCachePath("userscripts");
		try (Stream<Path> stream = Files.list(dir)) {
			final List<UserscriptMetadata> metadatas = new ArrayList<>();
			stream.filter(path -> !Files.isDirectory(path)).forEach(path -> {
				try {
					System.out.println("Found userscript: " + path.toAbsolutePath());
					final String content = Files.readString(path);
					final UserscriptMetadata parsed = UserscriptParser.parse(content, path.getFileName().toString());
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

	public void upsertUserscript(UserscriptMetadata userscript) {
		final Path userscriptPath = parent.getProfileCachePath("userscripts", userscript.filename());
		try {
			Files.write(userscriptPath, userscript.content().getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteUserscript(String filename) {
		final Path userscriptPath = parent.getProfileCachePath("userscripts", filename);
		try {
			Files.deleteIfExists(userscriptPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
