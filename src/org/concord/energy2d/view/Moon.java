/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */
public abstract class Moon implements Icon {

	private Color color = Color.white;

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g;
		int w = getIconWidth();
		int h = getIconHeight();
		g2.setColor(color);
		Area a = new Area(new Ellipse2D.Float(x, y, w, h));
		a.subtract(new Area(new Ellipse2D.Float(x + w * 0.25f, y, w, h)));
		g2.fill(a);
	}

}
