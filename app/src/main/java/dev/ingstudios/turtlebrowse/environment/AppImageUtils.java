package dev.ingstudios.turtlebrowse.environment;

public class AppImageUtils {
	public static String getAppImagePath() {
		return System.getenv("APPIMAGE");
	}

	public static boolean isAppImage() {
		final String appImageEnv = getAppImagePath();
		return appImageEnv != null && !appImageEnv.isBlank();
	}
}
