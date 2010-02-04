/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */
class Rainbow {

	private short[][] rgbScale;
	private Font font = new Font(null, Font.PLAIN | Font.BOLD, 8);
	private int x, y, w, h;

	Rainbow(short[][] rgbScale) {
		this.rgbScale = rgbScale;
	}

	void setRect(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	private int getColor(int i) {
		float v = (float) i * (float) rgbScale.length / (float) Math.max(w, h);
		if (v > rgbScale.length - 2)
			v = rgbScale.length - 2;
		else if (v < 0)
			v = 0;
		int iv = (int) v;
		v -= iv;
		int rc = (int) (rgbScale[iv][0] * (1 - v) + rgbScale[iv + 1][0] * v);
		int gc = (int) (rgbScale[iv][1] * (1 - v) + rgbScale[iv + 1][1] * v);
		int bc = (int) (rgbScale[iv][2] * (1 - v) + rgbScale[iv + 1][2] * v);
		return (255 << 24) | (rc << 16) | (gc << 8) | bc;
	}

	void render(JComponent c, Graphics2D g, float max, float min) {
		if (h == 0) {
			h = 20;
		}
		if (w == 0) {
			w = c.getWidth() - 100;
		}
		if (x == 0) {
			x = 50;
		}
		if (y == 0) {
			y = 20;
		}
		Font oldFont = g.getFont();
		g.setFont(font);
		if (h > w) {
			for (int i = 0; i < h; i++) {
				g.setColor(new Color(getColor(i)));
				g.drawLine(x, y + i, x + w, y + i);
			}
			g.setColor(Color.white);
			g.draw3DRect(x, y, w, h, true);
			String s = null;
			for (int i = 0; i < 11; i++) {
				s = (int) (min + i * 0.1f * (max - min)) + "\u2103";
				g.drawString(s, x + w + 15, y + h * 0.1f * i + 2.5f);
			}
		} else {
			for (int i = 0; i < w; i++) {
				g.setColor(new Color(getColor(i)));
				g.drawLine(x + i, y, x + i, y + h);
			}
			g.setColor(Color.white);
			g.draw3DRect(x, y, w, h, true);
			String s = null;
			FontMetrics fm = g.getFontMetrics();
			for (int i = 0; i < 11; i++) {
				s = (int) (min + i * 0.1f * (max - min)) + "\u2103";
				g.drawString(s, x + w * 0.1f * i - fm.stringWidth(s) * 0.5f, y
						+ h + 15);
			}
		}
		g.setFont(oldFont);
	}
}
