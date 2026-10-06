package dev.evilbrowse.managers;

import org.kordamp.ikonli.IkonResolver;
import org.kordamp.ikonli.IkonResolverProvider;
import org.kordamp.ikonli.javafx.JavaFXFontLoader;
import org.kordamp.ikonli.material2.Material2OutlinedALIkonHandler;
import org.kordamp.ikonli.material2.Material2OutlinedMZIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignAIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignBIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignCIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignDIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignEIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignFIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignGIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignHIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignIIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignJIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignKIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignLIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignMIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignNIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignOIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignPIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignQIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignRIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignSIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignTIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignUIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignVIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignWIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignXIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignYIkonHandler;
import org.kordamp.ikonli.materialdesign2.MaterialDesignZIkonHandler;

public class IkonliManager {
	private static IkonliManager instance;
	private final IkonResolver resolver;

	private IkonliManager() {
		final JavaFXFontLoader loader = new JavaFXFontLoader();
		resolver = IkonResolverProvider.getInstance(loader);
	}

	public static synchronized IkonliManager getInstance() {
		if (instance == null) {
			instance = new IkonliManager();
		}
		return instance;
	}

	public void registerHandlers() {
		resolver.registerHandler(new Material2OutlinedALIkonHandler());
		resolver.registerHandler(new Material2OutlinedMZIkonHandler());
		resolver.registerHandler(new MaterialDesignAIkonHandler());
		resolver.registerHandler(new MaterialDesignBIkonHandler());
		resolver.registerHandler(new MaterialDesignCIkonHandler());
		resolver.registerHandler(new MaterialDesignDIkonHandler());
		resolver.registerHandler(new MaterialDesignEIkonHandler());
		resolver.registerHandler(new MaterialDesignFIkonHandler());
		resolver.registerHandler(new MaterialDesignGIkonHandler());
		resolver.registerHandler(new MaterialDesignHIkonHandler());
		resolver.registerHandler(new MaterialDesignIIkonHandler());
		resolver.registerHandler(new MaterialDesignJIkonHandler());
		resolver.registerHandler(new MaterialDesignKIkonHandler());
		resolver.registerHandler(new MaterialDesignLIkonHandler());
		resolver.registerHandler(new MaterialDesignMIkonHandler());
		resolver.registerHandler(new MaterialDesignNIkonHandler());
		resolver.registerHandler(new MaterialDesignOIkonHandler());
		resolver.registerHandler(new MaterialDesignPIkonHandler());
		resolver.registerHandler(new MaterialDesignQIkonHandler());
		resolver.registerHandler(new MaterialDesignRIkonHandler());
		resolver.registerHandler(new MaterialDesignSIkonHandler());
		resolver.registerHandler(new MaterialDesignTIkonHandler());
		resolver.registerHandler(new MaterialDesignUIkonHandler());
		resolver.registerHandler(new MaterialDesignVIkonHandler());
		resolver.registerHandler(new MaterialDesignWIkonHandler());
		resolver.registerHandler(new MaterialDesignXIkonHandler());
		resolver.registerHandler(new MaterialDesignYIkonHandler());
		resolver.registerHandler(new MaterialDesignZIkonHandler());
	}
}
