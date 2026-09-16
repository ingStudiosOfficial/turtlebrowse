package dev.ingstudios.turtlebrowse.managers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.cef.CefApp;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.CefApp.CefAppState;
import org.cef.callback.CefSchemeRegistrar;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.db.MainDatabase;
import dev.ingstudios.turtlebrowse.handlers.TurtlebrowseSchemeHandlerFactory;
import dev.ingstudios.turtlebrowse.windows.MainWindow;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;

public class CefAppManager {
	private final boolean USE_OSR = false;

	private static CefAppManager instance;
	private CefSettings cefSettings;
	private final CefApp cefApp;
	private TurtlebrowseSchemeHandlerFactory turtlebrowseSchemeHandlerFactory;

	private CefAppManager(MainWindow parent) {
		cefApp = createCefApp(parent);
	}

	public static synchronized CefAppManager getInstance(MainWindow parent) {
		if (instance == null) {
			instance = new CefAppManager(parent);
		}
		return instance;
	}

	private CefApp createCefApp(MainWindow parent) {
		CefAppBuilder builder = new CefAppBuilder();
		builder.addJcefArgs("--enable-media-stream");

		if (OS.isLinux()) {
			builder.addJcefArgs("--single-process", "--ozone-platform=x11");
		}

		cefSettings = builder.getCefSettings();
		builder.setInstallDir(getInstallDir());
		cefSettings.windowless_rendering_enabled = USE_OSR;
		cefSettings.remote_debugging_port = 6767;

		cefSettings.user_agent = Main.getUserAgent();

		try {
			final String cachePath = Main.getStoragePath("cef-cache", Main.currentProfile.getIdAsString()).toString();
			System.out.printf("Cache path: %s\n", cachePath);
			cefSettings.cache_path = cachePath;
		} catch (Exception error) {
			System.out.print("Error while getting cache path, defaulting: ");
			System.out.println(error);
		}

		builder.setAppHandler(new MavenCefAppHandlerAdapter() {
			@Override
			public void stateHasChanged(CefAppState state) {
				if (state == CefAppState.TERMINATED) {
					MainDatabase.getInstance().closeDb();
					System.exit(0);
				}
			}

			@Override
			public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
				registrar.addCustomScheme("turtlebrowse", true, true, false, true, true, true, true);
			}

			@Override
			public void onContextInitialized() {
				turtlebrowseSchemeHandlerFactory = new TurtlebrowseSchemeHandlerFactory(parent);
				cefApp.registerSchemeHandlerFactory("turtlebrowse", "",
						turtlebrowseSchemeHandlerFactory);
			}
		});

		try {
			CefApp cefApp = builder.build();
			return cefApp;
		} catch (Exception error) {
			System.out.print("Error while building CEF app:");
			System.out.println(error);
			throw new RuntimeException("Error while building CEF app:", error);
		}
	}

	public CefApp getCefApp() {
		return cefApp;
	}

	private File getInstallDir() {
		Path installPath;

		final String appName = "Turtlebrowse";
		final String installDir = "cef-install";

		final String userHome = System.getProperty("user.home");

		if (OS.isWindows()) {
			String localAppData = System.getenv("LOCALAPPDATA");
			installPath = Paths.get(localAppData, "ingStudios", appName, installDir);
		} else if (OS.isLinux()) {
			String xdgDataHome = System.getenv("XDG_DATA_HOME");
			if (xdgDataHome == null || xdgDataHome.isBlank()) {
				xdgDataHome = userHome + "/.local/share";
			}
			installPath = Paths.get(xdgDataHome, "ingStudios", appName, installDir);
		} else if (OS.isMacintosh()) {
			installPath = Paths.get(userHome, "Library", "Application Support", appName, installDir);
		} else {
			throw new RuntimeException("Unknown operating system");
		}

		final File installFile = installPath.toFile();

		if (!installFile.exists()) {
			installFile.mkdirs();
		}

		return installFile;
	}
}
