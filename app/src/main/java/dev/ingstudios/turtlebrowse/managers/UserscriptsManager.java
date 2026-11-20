package dev.ingstudios.turtlebrowse.managers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.cef.OS;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dev.ingstudios.turtlebrowse.userscript.UserscriptParser;
import dev.ingstudios.turtlebrowse.userscript.UserscriptParser.UserscriptMetadata;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class UserscriptsManager {
	private static UserscriptsManager instance;
	private final MainWindow parent;
	private List<UserscriptMetadata> cachedUserscripts = new ArrayList<>();
	private final Gson gson = new Gson();

	private UserscriptsManager(MainWindow parent) {
		this.parent = parent;
		cachedUserscripts = getUserscripts();
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

	public List<UserscriptMetadata> getCachedUserscripts() {
		return cachedUserscripts;
	}

	public void upsertUserscript(UserscriptMetadata userscript) {
		final Path userscriptPath = parent.getProfileCachePath("userscripts", userscript.filename());
		try {
			Files.write(userscriptPath, userscript.content().getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

			upsertConfig(new UserscriptConfig(userscript.filename(), true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		cachedUserscripts = getUserscripts();
	}

	public void deleteUserscript(String filename) {
		final Path userscriptPath = parent.getProfileCachePath("userscripts", filename);
		try {
			Files.deleteIfExists(userscriptPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cachedUserscripts = getUserscripts();
	}

	public void openInTextEditor(String filename) {
		Thread.ofVirtual().start(() -> {
			final Path userscriptPath = parent.getProfileCachePath("userscripts", filename);
			final File file = userscriptPath.toFile();

			if (Desktop.isDesktopSupported()) {
				final Desktop desktop = Desktop.getDesktop();
				try {
					desktop.edit(file);
				} catch (Exception e) {
					e.printStackTrace();
					fallbackEdit(userscriptPath);
				}
			} else {
				System.out.println("Desktop not supported.");
				fallbackEdit(userscriptPath);
			}
		});
	}

	private void fallbackEdit(Path path) {
		try {
			if (OS.isLinux()) {
				final ProcessBuilder processBuilder = new ProcessBuilder("xdg-open", path.toString());
				processBuilder.inheritIO();
				processBuilder.start();
			} else if (OS.isWindows()) {
				final ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "", path.toString());
				processBuilder.inheritIO();
				processBuilder.start();
			} else if (OS.isMacintosh()) {
				final ProcessBuilder processBuilder = new ProcessBuilder("open", path.toString());
				processBuilder.inheritIO();
				processBuilder.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<UserscriptConfig> getConfigs() {
		try {
			final Path configPath = getConfigPath();
			final String raw = Files.readString(configPath);
			final List<UserscriptConfig> configs = gson.fromJson(raw, new TypeToken<List<UserscriptConfig>>() {
			}.getType());
			return configs;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public void setEnabled(String id, boolean enabled) {
		final List<UserscriptConfig> configs = getConfigs();
		final UserscriptConfig config = configs.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
		if (config != null) {
			final int index = configs.indexOf(config);
			configs.set(index, new UserscriptConfig(id, enabled));
		} else {
			System.out.println("Could not find config.");
		}
		saveConfigs(configs);
	}

	public boolean isEnabled(String id) {
		final List<UserscriptConfig> configs = getConfigs();
		final UserscriptConfig config = configs.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
		return config != null ? config.enabled() : true;
	}

	private void upsertConfig(UserscriptConfig config) {
		final List<UserscriptConfig> configs = getConfigs();
		final UserscriptConfig found = configs.stream().filter(c -> c.id().equals(config.id())).findFirst()
				.orElse(null);
		if (found != null) {
			final int index = configs.indexOf(config);
			configs.set(index, config);
		} else {
			configs.add(config);
		}
		saveConfigs(configs);
	}

	private void saveConfigs(List<UserscriptConfig> configs) {
		final Path configPath = getConfigPath();
		try {
			Files.writeString(configPath, gson.toJson(configs));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Path getConfigPath() {
		return parent.getProfileCachePath("userscripts.json");
	}

	public record UserscriptConfig(String id, boolean enabled) { // IMPORTANT: ID IS FILENAME
	}
}
