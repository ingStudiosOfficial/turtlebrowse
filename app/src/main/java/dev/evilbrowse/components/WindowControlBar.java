package dev.evilbrowse.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import javax.swing.JPanel;

/**
 * Firefox-style window controls, custom-painted so they match the browser
 * theme instead of the desktop look and feel.
 *
 * <p>
 * Used with an undecorated window: the tab strip acts as the title bar, and
 * this cluster provides minimize / maximize / close.
 */
public class WindowControlBar extends JPanel {
	public interface WindowActions {
		void minimize();

		void toggleMaximize();

		void close();

		void dragBy(int dx, int dy);

		void beginDrag(double screenX, double screenY);
	}

	private static final int BTN = 34;
	public static final int BAR_HEIGHT = 40;

	private final WindowActions actions;
	private Color baseColor;
	private Color glyphColor;
	private Color hoverColor;
	private static final Color CLOSE_HOVER = new Color(0xE8, 0x3A, 0x3A);

	private int hoverIndex = -1;
	private int lastX;
	private int lastY;

	public WindowControlBar(WindowActions actions, Color baseColor, Color glyphColor) {
		this.actions = actions;
		this.baseColor = baseColor != null ? baseColor : Color.decode("#171316");
		this.glyphColor = glyphColor != null ? glyphColor : Color.LIGHT_GRAY;
		this.hoverColor = lighten(this.baseColor, 0.18f);
		setOpaque(true);
		setBackground(this.baseColor);
		setPreferredSize(new Dimension(BTN * 3, BAR_HEIGHT));
		setFocusable(false);

		MouseAdapter hover = new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int idx = indexAt(e.getX());
				if (idx != hoverIndex) {
					hoverIndex = idx;
					setCursor(idx >= 0
							? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
							: java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hoverIndex = -1;
				repaint();
			}
		};
		addMouseListener(hover);
		addMouseMotionListener(hover);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lastX = e.getXOnScreen();
				lastY = e.getYOnScreen();
				if (indexAt(e.getX()) < 0
						&& e.getButton() == MouseEvent.BUTTON1) {
					actions.beginDrag(e.getXOnScreen(), e.getYOnScreen());
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int idx = indexAt(e.getX());
				if (e.getClickCount() == 2 && idx < 0) {
					actions.toggleMaximize();
					return;
				}
				if (idx == 0) {
					actions.minimize();
				} else if (idx == 1) {
					actions.toggleMaximize();
				} else if (idx == 2) {
					actions.close();
				}
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (indexAt(e.getX()) >= 0) {
					return; // Never drag off a window button.
				}
				int dx = e.getXOnScreen() - lastX;
				int dy = e.getYOnScreen() - lastY;
				lastX = e.getXOnScreen();
				lastY = e.getYOnScreen();
				if (dx != 0 || dy != 0) {
					actions.dragBy(dx, dy);
				}
			}
		});
	}

	public void refreshColors(Color base, Color glyph) {
		this.baseColor = base != null ? base : this.baseColor;
		this.glyphColor = glyph != null ? glyph : this.glyphColor;
		this.hoverColor = lighten(this.baseColor, 0.18f);
		setBackground(this.baseColor);
		repaint();
	}

	private int indexAt(int x) {
		if (x >= getWidth() - BTN) {
			return 2;
		}
		if (x >= getWidth() - BTN * 2) {
			return 1;
		}
		if (x >= getWidth() - BTN * 3) {
			return 0;
		}
		return -1;
	}

	private static Color lighten(Color c, float amount) {
		float f = 1.0f + amount;
		return new Color(
				Math.min(255, (int) (c.getRed() * f)),
				Math.min(255, (int) (c.getGreen() * f)),
				Math.min(255, (int) (c.getBlue() * f)));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(baseColor);
		g2.fillRect(0, 0, getWidth(), getHeight());

		for (int i = 0; i < 3; i++) {
			int x = getWidth() - BTN * (3 - i);
			if (i == hoverIndex) {
				g2.setColor(i == 2 ? CLOSE_HOVER : hoverColor);
				g2.fillRect(x, 0, BTN, getHeight());
			}
			g2.setColor((i == 2 && hoverIndex == 2) ? Color.WHITE : glyphColor);
			drawGlyph(g2, i, x + BTN / 2.0, getHeight() / 2.0);
		}
		g2.dispose();
	}

	private void drawGlyph(Graphics2D g2, int type, double cx, double cy) {
		g2.setStroke(new java.awt.BasicStroke(1.3f));
		switch (type) {
			case 0 -> g2.drawLine((int) cx - 5, (int) cy, (int) cx + 5, (int) cy);
			case 1 -> g2.drawRect((int) cx - 5, (int) cy - 5, 10, 10);
			default -> {
				Path2D.Double p = new Path2D.Double();
				p.moveTo(cx - 5, cy - 5);
				p.lineTo(cx + 5, cy + 5);
				p.moveTo(cx + 5, cy - 5);
				p.lineTo(cx - 5, cy + 5);
				g2.draw(p);
			}
		}
	}
}