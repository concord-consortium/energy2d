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
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */

abstract class Symbol implements Icon {

	int w = 8, h = 8;
	Color color = Color.white;
	Stroke stroke = new BasicStroke(1);

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

	public final static Symbol get(String s) {
		if ("Thermometer".equals(s))
			return Thermometer.sharedInstance();
		if ("Sun".equals(s))
			return new Sun(Color.yellow, 16, 16);
		if ("Moon".equals(s))
			return new Moon(Color.white, 16, 16);
		return null;
	}

	static class Moon extends Symbol {

		public Moon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			int w = getIconWidth();
			int h = getIconHeight();
			g2.setColor(color);
			g2.setStroke(stroke);
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
			Graphics2D g2 = (Graphics2D) g;
			int w = getIconWidth();
			int h = getIconHeight();
			g2.setColor(color);
			g2.setStroke(stroke);
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

		// since they are many thermometers, we want to make a singleton.
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

}
