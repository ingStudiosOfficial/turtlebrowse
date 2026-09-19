package dev.ingstudios.turtlebrowse.components;

import javax.swing.JPanel;

public class ToolSidebar extends JPanel {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;

	public void toggleSidebar() {
		if (isOpen) {
			closeSidebar();
		} else {
			openSidebar();
		}
	}

	public void openSidebar() {
		System.out.println("Opening sidebar...");
		preferredDim.width = 50;
		isOpen = true;
		this.revalidate();
	}

	public void closeSidebar() {
		preferredDim.width = 0;
		isOpen = false;
		this.revalidate();
	}
}
