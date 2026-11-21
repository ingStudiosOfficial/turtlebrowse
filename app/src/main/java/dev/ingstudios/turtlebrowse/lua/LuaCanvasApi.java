package dev.ingstudios.turtlebrowse.lua;

import dev.ingstudios.turtlebrowse.components.PluginSidebar;
import dev.ingstudios.turtlebrowse.lua.elements.LuaTextElement;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LuaCanvasApi {
	public static String testApi() {
		return "api reachable";
	}

	public static HBox createRow() {
		return PluginSidebar.instance.createRow();
	}

	public static VBox createColumn() {
		return PluginSidebar.instance.createColumn();
	}

	public static LuaTextElement createText(String text) {
		return PluginSidebar.instance.createText(text);
	}
}
