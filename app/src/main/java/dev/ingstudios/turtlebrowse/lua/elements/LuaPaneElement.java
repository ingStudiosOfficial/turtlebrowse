package dev.ingstudios.turtlebrowse.lua.elements;

import javafx.scene.layout.Pane;

public abstract class LuaPaneElement<T extends Pane> extends LuaElement<Pane> {
	protected LuaPaneElement(T node) {
		super(node);
	}

	public LuaPaneElement<?> appendChild(LuaElement<?> child) {
		node.getChildren().add(child.node);
		return this;
	}
}
