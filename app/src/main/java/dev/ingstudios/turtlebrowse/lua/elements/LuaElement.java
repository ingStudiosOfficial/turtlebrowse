package dev.ingstudios.turtlebrowse.lua.elements;

import javafx.scene.Node;

public abstract class LuaElement<T extends Node> {
	protected final T node;

	protected LuaElement(T node) {
		this.node = node;
	}

	public LuaElement<T> focus() {
		if (node == null)
			return this;
		node.requestFocus();
		return this;
	}
}
