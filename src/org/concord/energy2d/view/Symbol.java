/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

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

		public Thermometer() {
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.white);
			g.fillRect(x, y, w - 1, h - 1);
			g.setColor(Color.red);
			g.drawLine(x + w / 2 - 1, y + h / 2, x + w / 2 - 1, y + h - 1);
			g.drawLine(x + w / 2, y + h / 2, x + w / 2, y + h - 1);
			g.drawLine(x + w / 2 + 1, y + h / 2, x + w / 2 + 1, y + h - 1);
			g.setColor(Color.black);
			g.drawRect(x, y, w - 1, h - 1);
			int n = h / 2;
			for (int i = 1; i < n; i++) {
				g.drawLine(x, y + i * 2, Math.round(x + 0.2f * w), y + i * 2);
				g.drawLine(x + w - 1, y + i * 2, Math.round(x + w - 1 - 0.2f * w), y + i * 2);
			}
		}

		public byte getType() {
			return THERMOMETER;
		}

	}

}
