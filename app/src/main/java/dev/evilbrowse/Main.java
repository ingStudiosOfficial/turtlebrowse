package dev.evilbrowse;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cef.OS;
import org.glavo.monetfx.ColorScheme;
import org.glavo.monetfx.beans.property.ColorSchemeProperty;
import org.glavo.monetfx.beans.property.SimpleColorSchemeProperty;

import com.jagrosh.discordipc.IPCClient;

import dev.evilbrowse.db.MainDatabase;
import dev.evilbrowse.db.MainDatabase.ProfileStructureWithId;
import dev.evilbrowse.environment.AppImageUtils;
import dev.evilbrowse.managers.DiscordPresenceManager;
import dev.evilbrowse.managers.IkonliManager;
import dev.evilbrowse.managers.InstanceManager;
import dev.evilbrowse.windows.MainWindow;
import dev.evilbrowse.windows.ProfilePickerWindow;
import dev.evilbrowse.windows.SetupWindow;
import dev.evilbrowse.wizard.WizardData;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

public class Main {
	public static ColorSchemeProperty mainMaterialColorScheme = new SimpleColorSchemeProperty(
			ColorScheme.fromSeed(Color.web("#BDCF47")));
	private static final MainDatabase db = MainDatabase.getInstance();
	public static ProfilePickerWindow profilePickerWindow;
	public static ProfileStructureWithId currentProfile;
	public static boolean isGuest = false;

	public static void main(String[] args) {
		System.setProperty("javafx.platform", "dev.evilbrowse");
		System.setProperty("awt.toolkit.name", "dev.evilbrowse");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			final DiscordPresenceManager presenceManager = DiscordPresenceManager.getInstance();

			final IPCClient discordIpcClient = presenceManager.getClient();

			try {
				if (discordIpcClient != null)
					discordIpcClient.close();
			} catch (Exception e) {
				System.err.printf("Error while closing Discord client: %s\n", e.getMessage());
			}

			if (db != null) {
				db.closeDb();
			}

			if (isGuest) {
				System.out.println("Deleting guest profile data...");
				ProfilePickerWindow.deleteProfileData(currentProfile);
			}
		}));

		Platform.startup(() -> {
			Platform.setImplicitExit(false);
		});

		IkonliManager.getInstance().registerHandlers();

		setMaterialColorSchemeFromSystem();

		final List<ProfileStructureWithId> profiles = db.getAllProfiles();
		System.out.println("Profiles: " + profiles);

		final String profileId = getProfileId(args);
		System.out.println("Profile ID (after getProfileId): " + profileId);

		DiscordPresenceManager.getInstance().init();

		isGuest = checkIsGuest(args);

		if (isGuest) {
			// Guest must have a profile id; previously a bare --guest true NPE'd.
			if (profileId == null || profileId.isBlank()) {
				System.err.println("Guest mode requires --profile-id; falling back to picker.");
				isGuest = false;
				currentProfile = null;
			} else {
				try {
					currentProfile = new ProfileStructureWithId("Guest", Platform.getPreferences().getAccentColor(),
							UUID.fromString(profileId));
				} catch (Exception e) {
					System.err.println("Invalid guest profile id, falling back to picker: " + e.getMessage());
					isGuest = false;
					currentProfile = null;
				}
			}
		}
		if (!isGuest) {
			try {
				currentProfile = profiles.stream().filter(p -> {
					System.out.println("Profile (p): %s Name: %s".formatted(p.getIdAsString(), p.name()));
					System.out.println("Target profile: " + profileId);
					return p.getIdAsString().equals(profileId);
				}).findFirst()
						.orElse(null);
				System.out.println("Current profile (after filtering profiles): " + currentProfile);
			} catch (Throwable t) {
				System.err.println("An exception occurred while filtering for the current profile: " + t.getMessage());
				t.printStackTrace();
			}
		}

		final String launchUrl = getLaunchUrl(args);
		if (launchUrl != null && !launchUrl.isEmpty() && !profiles.isEmpty()) {
			// Current profile may or may not be null at this point depending on whether a
			// profile ID was provided

			db.closeDb();

			if (currentProfile == null) {
				// IMPORTANT: Assign current profile as CefAppManager looks up from
				// Main.currentProfile
				currentProfile = profiles.get(0);
			}

			final InstanceManager instanceManager = InstanceManager.getInstance();
			final int port = instanceManager.getPort(currentProfile.getIdAsString());
			final boolean portInUse = instanceManager.isPortInUse("127.0.0.1", port);
			System.out.println("Port in use: " + portInUse);
			if (portInUse) {
				instanceManager.broadcastToInstance("127.0.0.1", port, String.join("\0", args));
				System.exit(0);
			}

			final ProfileStructureWithId launchProfile = currentProfile;
			// MainWindow is Swing: construct on the EDT like every other path.
			SwingUtilities.invokeLater(() -> {
				final MainWindow mainWindow = new MainWindow(launchProfile, launchUrl);
				mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
				mainWindow.setUndecorated(false);
				mainWindow.setVisible(true);
			});
			return;
		}

		if (profileId != null) {
			System.out.println("Profile ID is not null.");
			db.closeDb();

			final InstanceManager instanceManager = InstanceManager.getInstance();
			final int port = instanceManager.getPort(profileId);
			final boolean portInUse = instanceManager.isPortInUse("127.0.0.1", port);
			System.out.println("Port in use: " + portInUse);
			if (portInUse) {
				instanceManager.broadcastToInstance("127.0.0.1", port, String.join("\0", args));
				System.exit(0);
			}

			SwingUtilities.invokeLater(() -> {
				System.out.println("Creating main window...");
				if (currentProfile == null) {
					System.err.println("No profile found for id " + profileId + "; opening picker instead.");
					createProfilePicker(true);
					return;
				}
				final MainWindow mainWindow = new MainWindow(currentProfile);
				mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
				mainWindow.setUndecorated(false);
				mainWindow.setVisible(true);
			});
		} else {
			System.out.println("Profile ID is null.");

			final boolean noProfile = profiles.isEmpty();

			final boolean alwaysOpenPicker = checkAlwaysOpenPicker(args);
			System.out.printf("Always open picker: %s\n", String.valueOf(alwaysOpenPicker));

			SwingUtilities.invokeLater(() -> {
				new JFXPanel();

				Platform.runLater(() -> {
					if (noProfile && alwaysOpenPicker == false) {
						final SetupWindow setupWindow = new SetupWindow();
						Optional<ButtonType> result = setupWindow.showAndWait();

						final WizardData wizardData = setupWindow.wizardData;

						if (result.get() == ButtonType.FINISH) {
							System.out.printf("""
									Finished wizard:
									Name: %s
									Theme: %s
									AI enabled: %s
									""", wizardData.name, wizardData.themeColor.toString(),
									String.valueOf(wizardData.enableAI));

							wizardData.saveData();
						} else if (result.get() == ButtonType.CANCEL) {
							db.closeDb();
							System.exit(0);
						}
					}

					System.out.println("Creating profile picker window...");

					createProfilePicker(alwaysOpenPicker);
				});
			});

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				MainDatabase.getInstance().closeDb();
			}));
		}
	}

	private static void createProfilePicker(boolean alwaysOpen) {
		Platform.runLater(() -> {
			if (profilePickerWindow == null)
				profilePickerWindow = new ProfilePickerWindow();
			profilePickerWindow.showProfilePickerWindow(alwaysOpen);
		});
	}

	private static void setMaterialColorSchemeFromSystem() {
		final Color accentColor = Platform.getPreferences().getAccentColor();
		if (accentColor == null) {
			Main.mainMaterialColorScheme.set(ColorScheme.fromSeed(Color.web("#BDCF47")));
		} else {
			Main.mainMaterialColorScheme.set(ColorScheme.fromSeed(accentColor));
		}
	}

	public static Path getStoragePath(String... names) {
		Path dataPath = appDataDir("EvilBrowse", null);

		// One-time brand migration: if the EvilBrowse dir does not exist yet
		// but a legacy Turtlebrowse dir does, keep using the legacy dir so
		// profiles, history and settings survive the rename.
		try {
			Path legacy = appDataDir("Turtlebrowse", "ingStudios");
			if (legacy != null && java.nio.file.Files.isDirectory(legacy)
					&& !java.nio.file.Files.isDirectory(dataPath)) {
				System.out.println("Using legacy Turtlebrowse data dir: " + legacy);
				dataPath = legacy;
			}
		} catch (Exception ignored) {
		}

		if (names != null) {
			for (final String name : names) {
				dataPath = dataPath.resolve(name);
			}
		}

		return dataPath;
	}

	private static Path appDataDir(String appName, String vendor) {
		final String userHome = System.getProperty("user.home");

		if (OS.isWindows()) {
			String localAppData = System.getenv("LOCALAPPDATA");
			if (localAppData == null || localAppData.isBlank()) {
				localAppData = userHome + "\\AppData\\Local";
			}
			return vendor != null ? Paths.get(localAppData, vendor, appName)
					: Paths.get(localAppData, appName);
		} else if (OS.isLinux()) {
			String xdgDataHome = System.getenv("XDG_DATA_HOME");
			if (xdgDataHome == null || xdgDataHome.isEmpty()) {
				xdgDataHome = userHome + "/.local/share";
			}
			return vendor != null ? Paths.get(xdgDataHome, vendor, appName)
					: Paths.get(xdgDataHome, appName);
		} else if (OS.isMacintosh()) {
			return Paths.get(userHome, "Library", "Application Support", appName);
		} else {
			throw new RuntimeException("Unknown operating system");
		}
	}

	public static String getUserAgent() {
		String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.6.0 Safari/537.36";

		if (OS.isLinux()) {
			userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.6.0 Safari/537.36";
		} else if (OS.isWindows()) {
			userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.6.0 Safari/537.36";
		} else if (OS.isMacintosh()) {
			userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.6.0 Safari/537.36";
		}

		return userAgent;
	}

	private static String getProfileId(String[] args) {
		String targetProfileId = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--profile-id") && i + 1 < args.length) {
				targetProfileId = args[i + 1];
				break;
			}
		}
		return targetProfileId;
	}

	private static boolean checkAlwaysOpenPicker(String[] args) {
		boolean alwaysOpen = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--open-picker") && i + 1 < args.length) {
				alwaysOpen = Boolean.parseBoolean(args[i + 1]);
				System.out.printf("Always open: %s\n", String.valueOf(alwaysOpen));
				break;
			}
		}
		return alwaysOpen;
	}

	public static boolean checkIsGuest(String[] args) {
		boolean isGuest = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--guest") && i + 1 < args.length) {
				isGuest = Boolean.parseBoolean(args[i + 1]);
				System.out.printf("Is guest: %s\n", String.valueOf(isGuest));
				break;
			}
		}
		return isGuest;
	}

	public static String getLaunchUrl(String[] args) {
		String url = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--url") && i + 1 < args.length) {
				url = args[i + 1];
				System.out.printf("Launch URL: %s\n", url);
				break;
			}
		}
		return url;
	}

	public static MainDatabase getDb() {
		return db;
	}

	public static void createProfilePickerWindow() {
		db.closeDb();

		final boolean isAppImage = AppImageUtils.isAppImage();
		final String appImagePath = AppImageUtils.getAppImagePath();

		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final String classpath = System.getProperty("java.class.path");
		final String appPath = System.getProperty("jpackage.app-path");

		final List<String> command = new ArrayList<>();

		if (isAppImage) {
			command.add(appImagePath);
		} else if (appPath != null && !appPath.isEmpty()) {
			command.add(appPath);
		} else {
			command.add(javaBin);

			final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
			final List<String> vmArguments = runtimeMxBean.getInputArguments();
			for (String arg : vmArguments) {
				if (!arg.contains("-agentlib") && !arg.contains("-javaagent")) {
					command.add(arg);
				}
			}

			command.add("-cp");
			command.add(classpath);
		}

		command.add("dev.evilbrowse.Main");
		command.add("--open-picker");
		command.add("true");

		System.out.println("Spawning Command: " + String.join(" ", command));

		final ProcessBuilder builder = new ProcessBuilder(command);

		if (isAppImage) {
			final Map<String, String> env = builder.environment();
			env.remove("LD_LIBRARY_PATH");
		}

		try {
			builder.start();
		} catch (IOException e) {
			System.err.printf("Failed while running process for profile picker window.");
			e.printStackTrace();
		}
	}

	public static void createMainWindow(ProfileStructureWithId profile) {
		createMainWindow(profile, false);
	}

	public static void createMainWindow(ProfileStructureWithId profile, boolean guest) {
		db.closeDb();

		final boolean isAppImage = AppImageUtils.isAppImage();
		final String appImagePath = AppImageUtils.getAppImagePath();

		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final String classpath = System.getProperty("java.class.path");
		final String appPath = System.getProperty("jpackage.app-path");

		final String profileId = profile.getIdAsString();

		final List<String> command = new ArrayList<>();

		if (isAppImage) {
			command.add(appImagePath);
		} else if (appPath != null && !appPath.isEmpty()) {
			command.add(appPath);
		} else {
			command.add(javaBin);

			final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
			final List<String> vmArguments = runtimeMxBean.getInputArguments();
			for (String arg : vmArguments) {
				if (!arg.contains("-agentlib") && !arg.contains("-javaagent")) {
					command.add(arg);
				}
			}

			command.add("-cp");
			command.add(classpath);

			command.add("dev.evilbrowse.Main");
		}

		command.add("--profile-id");
		command.add(profileId);
		if (guest == true) {
			command.add("--guest");
			command.add("true");
		}

		System.out.println("Spawning Command: " + String.join(" ", command));

		final ProcessBuilder builder = new ProcessBuilder(command);

		if (isAppImage) {
			final Map<String, String> env = builder.environment();
			env.remove("LD_LIBRARY_PATH");
		}

		// Persist child output: the spawned browser otherwise inherits (and
		// loses) its stdout, making field debugging impossible.
		File childLog = null;
		try {
			final Path logDir = getStoragePath("logs");
			java.nio.file.Files.createDirectories(logDir);
			childLog = logDir.resolve("evilbrowse-" + profileId + ".log").toFile();
			builder.redirectOutput(ProcessBuilder.Redirect.appendTo(childLog));
			builder.redirectErrorStream(true);
		} catch (Exception e) {
			System.err.println("Child log redirect failed: " + e.getMessage());
		}

		try {
			builder.start();
			System.out.printf("Successfully spawned process for profile: %s\n", profileId);
			if (childLog != null) {
				System.out.println("Child log: " + childLog.getAbsolutePath());
			}
			Platform.runLater(() -> {
				try {
					if (profilePickerWindow != null) {
						profilePickerWindow.close();
					}
				} catch (Exception ignored) {
				}
			});
		} catch (IOException e) {
			System.err.printf("Failed while running process for profile: %s\n", profileId);
			e.printStackTrace();
		}

		System.exit(0);
	}
}
