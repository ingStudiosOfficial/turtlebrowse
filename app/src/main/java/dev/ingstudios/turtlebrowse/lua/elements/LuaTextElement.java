package dev.ingstudios.turtlebrowse.lua.elements;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class LuaTextElement extends LuaElement<Label> {
	public LuaTextElement(Label node) {
		super(node);
	}

	public LuaTextElement setText(String text) {
		node.setText(text);
		return this;
	}

	public LuaTextElement setColor(String color) {
		System.out.println("Setting color: " + color);
		node.setTextFill(Color.web(color));
		return this;
	}
}
