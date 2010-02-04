/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author Charles Xie
 * 
 */
class RainbowRenderer {

	private short[][] rgbScale;
	private int x, y, w, h;

	RainbowRenderer(short[][] rgbScale) {
		this.rgbScale = rgbScale;
	}

	void setRect(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	private int getColor(int i) {
		float v = (float) i * (float) rgbScale.length / (float) w;
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

	void render(Graphics2D g, float max, float min) {
		for (int i = 0; i < w; i++) {
			g.setColor(new Color(getColor(i)));
			g.drawLine(x + i, y, x + i, y + h);
		}
		g.setColor(Color.white);
		g.draw3DRect(x, y, w, h, true);
		for (int i = 0; i < 10; i++) {
			g.drawString((int) (min + i * 0.1f * (max - min)) + "\u2103", x + w
					* 0.1f * i, y + h + 15);
		}
	}

}
