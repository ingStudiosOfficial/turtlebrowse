package dev.ingstudios.turtlebrowse.lua.elements;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class LuaTextElement {
	public final Label node;

	public LuaTextElement(Label node) {
		this.node = node;
	}

	public LuaTextElement setColor(String color) {
		System.out.println("Setting color: " + color);
		node.setTextFill(Color.web(color));
		return this;
	}
}
