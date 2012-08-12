/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */
public abstract class Sun implements Icon {

	private Color color = Color.yellow;
	private Stroke stroke = new BasicStroke(2);

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
