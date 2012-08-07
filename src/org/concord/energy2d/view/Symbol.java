/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */

abstract class Symbol implements Icon {

	final static byte THERMOMETER = 0;

	protected int w = 8, h = 8;

	public abstract byte getType();

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
			e.printStackTrace(System.err);
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

	public final static Symbol get(int type) {
		switch (type) {
		case THERMOMETER:
			return Thermometer.sharedInstance();
		default:
			return null;
		}
	}

	static class Thermometer extends Symbol {

		private final static Thermometer instance = new Thermometer();

		public static Thermometer sharedInstance() {
			return instance;
		}

		private int value;
		private Stroke stroke1 = new BasicStroke(1);

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
			g2.setStroke(stroke1);
			g2.drawRect(x, y, w - 1, h - 1);
			int n = h / 2;
			for (int i = 1; i < n; i++) {
				g2.drawLine(x, y + i * 2, Math.round(x + 0.2f * w), y + i * 2);
				g2.drawLine(x + w - 1, y + i * 2, Math.round(x + w - 1 - 0.2f * w), y + i * 2);
			}
		}

		public byte getType() {
			return THERMOMETER;
		}

	}

}
