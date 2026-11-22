package dev.ingstudios.turtlebrowse.lua;

import dev.ingstudios.turtlebrowse.components.PluginSidebar;
import dev.ingstudios.turtlebrowse.lua.elements.LuaCanvasElement;
import dev.ingstudios.turtlebrowse.lua.elements.LuaColumnElement;
import dev.ingstudios.turtlebrowse.lua.elements.LuaRowElement;
import dev.ingstudios.turtlebrowse.lua.elements.LuaTextElement;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LuaCanvasApi {
	public static String testApi() {
		return "api reachable";
	}

	public static LuaCanvasElement getCanvas() {
		return new LuaCanvasElement(PluginSidebar.instance.getCanvas());
	}

	public static LuaRowElement createRow() {
		return new LuaRowElement(new HBox());
	}

	public static LuaColumnElement createColumn() {
		return new LuaColumnElement(new VBox());
	}

	public static LuaTextElement createText(String text) {
		return new LuaTextElement(new Label(text));
	}
}
