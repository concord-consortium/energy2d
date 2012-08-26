/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */

public abstract class Symbol implements Icon {

	protected int x = 0, y = 0, w = 8, h = 8;
	protected Color color = Color.white;
	protected Stroke stroke = new BasicStroke(1);
	protected boolean paintBorder;
	protected boolean pressed;

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setBorderPainted(boolean paintBorder) {
		this.paintBorder = paintBorder;
	}

	public boolean isBorderPainted() {
		return paintBorder;
	}

	public void setIconWidth(int width) {
		w = width;
	}

	public int getIconWidth() {
		return w;
	}

	public void setIconHeight(int height) {
		h = height;
	}

	public int getIconHeight() {
		return h;
	}

	public boolean contains(int rx, int ry) {
		return rx > x && rx < x + w && ry > y && ry < y + h;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		this.x = x;
		this.y = y;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		if (paintBorder) {
			g.drawRoundRect(x, y, w, h, 10, 10);
		}
		g2.setStroke(stroke);
	}

	public Symbol getScaledInstance(float scale) {
		try {
			Symbol icon = getClass().newInstance();
			icon.setIconWidth((int) (scale * icon.getIconWidth()));
			icon.setIconHeight((int) (scale * icon.getIconHeight()));
			return icon;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Image createImage(Component c) {
		BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		try {
			paintIcon(c, g, 0, 0);
			return image;
		} finally {
			g.dispose();
		}
	}

	public final static Symbol get(String s) {
		if ("Thermometer".equals(s))
			return Thermometer.sharedInstance();
		if ("Anemometer".equals(s))
			return Anemometer.sharedInstance();
		if ("Sun".equals(s))
			return new Sun(Color.yellow, 16, 16);
		if ("Moon".equals(s))
			return new Moon(Color.white, 16, 16);
		if ("Switch".equals(s))
			return new SwitchIcon(Color.white, 24, 24);
		if ("Start".equals(s))
			return new StartIcon(Color.white, 24, 24);
		if ("Reset".equals(s))
			return new ResetIcon(Color.white, 24, 24);
		if ("Graph".equals(s))
			return new GraphIcon(Color.white, 24, 24);
		return null;
	}

	static class Moon extends Symbol {

		public Moon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Area a = new Area(new Ellipse2D.Float(x, y, w, h));
			a.subtract(new Area(new Ellipse2D.Float(x + w * 0.25f, y, w, h)));
			g2.fill(a);
		}

	}

	static class Sun extends Symbol {

		public Sun(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
			setStroke(new BasicStroke(2));
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Ellipse2D.Float s = new Ellipse2D.Float(x, y, w * 0.75f, h * 0.75f);
			g2.fill(s);
			int x1, y1;
			double angle = 0;
			int n = 8;
			for (int i = 0; i < n; i++) {
				angle = i * Math.PI * 2 / n;
				x1 = (int) (s.getCenterX() + 10 * Math.cos(angle));
				y1 = (int) (s.getCenterY() + 10 * Math.sin(angle));
				g2.drawLine(x1, y1, (int) s.getCenterX(), (int) s.getCenterY());
			}
		}

	}

	static class Thermometer extends Symbol {

		// since there can be many thermometers, we want to make a singleton.
		private final static Thermometer instance = new Thermometer();

		public static Thermometer sharedInstance() {
			return instance;
		}

		private int value;

		public Thermometer() {
		}

		public void setValue(int value) {
			this.value = value;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.white);
			g2.fillRect(x, y, w - 1, h - 1);
			if (value != 0) {
				g2.setColor(Color.red);
				BasicStroke bs = new BasicStroke(getIconWidth() / 3);
				g2.setStroke(bs);
				g2.drawLine(x + w / 2, (int) (y + h - bs.getLineWidth() - value), x + w / 2, (int) (y + h - bs.getLineWidth()));
			}
			g2.setColor(Color.black);
			g2.setStroke(stroke);
			g2.drawRect(x, y, w - 1, h - 1);
			int n = h / 2;
			for (int i = 1; i < n; i++) {
				g2.drawLine(x, y + i * 2, Math.round(x + 0.2f * w), y + i * 2);
				g2.drawLine(x + w - 1, y + i * 2, Math.round(x + w - 1 - 0.2f * w), y + i * 2);
			}
		}

	}

	static class Anemometer extends Symbol {

		private float angle;

		// since there can be many anemometers, we want to make a singleton.
		private final static Anemometer instance = new Anemometer();

		public static Anemometer sharedInstance() {
			return instance;
		}

		public Anemometer() {
		}

		public void setAngle(float angle) {
			this.angle = angle;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {

			super.paintIcon(c, g, x, y);

			Graphics2D g2 = (Graphics2D) g;
			double xc = x + w * 0.5;
			double yc = y + h * 0.5;
			g2.setColor(Color.white);
			g.fillOval(Math.round(x + w * 0.4f), Math.round(y + h * 0.4f), Math.round(w * 0.2f), Math.round(h * 0.2f));
			g2.setColor(Color.black);
			g.drawOval(Math.round(x + w * 0.4f), Math.round(y + h * 0.4f), Math.round(w * 0.2f), Math.round(h * 0.2f));

			g2.rotate(angle, xc, yc);

			int[] xPoints = new int[] { (int) xc, Math.round(x + w * 0.4f), Math.round(x + w * 0.6f) };
			int[] yPoints = new int[] { y, Math.round(y + h * 0.4f), Math.round(y + h * 0.4f) };
			g2.setColor(Color.white);
			g.fillPolygon(xPoints, yPoints, 3);
			g2.setColor(Color.black);
			g.drawPolygon(xPoints, yPoints, 3);

			double theta = 2.0 * Math.PI / 3.0;
			g2.rotate(theta, xc, yc);
			g2.setColor(Color.white);
			g.fillPolygon(xPoints, yPoints, 3);
			g2.setColor(Color.black);
			g.drawPolygon(xPoints, yPoints, 3);

			g2.rotate(theta, xc, yc);
			g2.setColor(Color.white);
			g.fillPolygon(xPoints, yPoints, 3);
			g2.setColor(Color.black);
			g.drawPolygon(xPoints, yPoints, 3);

			g2.rotate(-angle - 2 * theta, xc, yc);

		}

	}

	static class SwitchIcon extends Symbol {

		public SwitchIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			float thickness = ((BasicStroke) stroke).getLineWidth();
			Rectangle2D.Float s = new Rectangle2D.Float(x + w * 0.2f, y + h * 0.2f, w * 0.6f, h * 0.6f);
			Arc2D.Float a = new Arc2D.Float(s, 80 - thickness * 10, thickness * 20 - 340, Arc2D.OPEN);
			g2.draw(a);
			g2.drawLine((int) (x + w * 0.5f), (int) (y + h * 0.1f), (int) (x + w * 0.5f), (int) (y + h * 0.4f));
		}

	}

	static class GraphIcon extends Symbol {

		private Stroke s2 = new BasicStroke(1);

		public GraphIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			int x0 = (int) (x + w * 0.25f);
			int y0 = (int) (y + h * 0.25f);
			g2.drawRect(x0 - 1, y0 - 1, (int) (w * 0.5f) + 2, (int) (h * 0.5f) + 2);
			g2.setStroke(s2);
			if (pressed) {
				g2.drawLine(x0, y0, (int) (x0 + w * 0.5f), (int) (y0 + h * 0.5f));
				g2.drawLine(x0, (int) (y0 + h * 0.5f), (int) (x0 + w * 0.5f), y0);
			} else {
				g2.drawLine(x0, (int) (y0 + h * 0.25f), (int) (x0 + w * 0.25f), (int) (y0 + h * 0.5f));
				g2.drawLine((int) (x0 + w * 0.25f), (int) (y0 + h * 0.5f), (int) (x0 + w * 0.5f), y0);
			}
		}

	}

	static class ResetIcon extends Symbol {

		public ResetIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Rectangle2D.Float s = new Rectangle2D.Float(x + w * 0.4f, y + h * 0.3f, w * 0.4f, h * 0.4f);
			Arc2D.Float a = new Arc2D.Float(s, -90, 180, Arc2D.OPEN);
			g2.draw(a);
			int x0 = (int) (x + w * 0.3f);
			int y0 = (int) (y + h * 0.3f);
			g2.drawLine((int) (x + w * 0.5f), y0, x0, y0);
			g2.drawLine(x0, y0, x0 + 2, y0 - 2);
			g2.drawLine(x0, y0, x0 + 2, y0 + 2);
			y0 += h * 0.4f;
			g2.drawLine((int) (x + w * 0.5f), y0, x0, y0);
		}

	}

	static class StartIcon extends Symbol {

		private int d = 4;
		private Stroke s2 = new BasicStroke(d);

		public StartIcon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			if (pressed) {
				((Graphics2D) g).setStroke(s2);
				g.drawLine(x + w / 2 - d + 1, y + d * 2, x + w / 2 - d + 1, y + h - d * 2);
				g.drawLine(x + w / 2 + d - 1, y + d * 2, x + w / 2 + d - 1, y + h - d * 2);
			} else {
				int[] xpoints = new int[] { x + w / 2 - d, x + w - d, x + w / 2 - d };
				int[] ypoints = new int[] { y + d, y + h / 2, y + h - d };
				g.fillPolygon(new Polygon(xpoints, ypoints, 3));
			}
		}

	}

}
