/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */
class GridRenderer {

	private Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1,
			new float[] { 1.5f }, 0);
	private Color color = new Color(128, 128, 225, 128);
	private int nx;
	private int ny;
	private int gridSize = 10;

	GridRenderer(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
	}

	void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	int getGridSize() {
		return gridSize;
	}

	void render(JComponent c, Graphics2D g) {

		if (!c.isVisible())
			return;

		int w = c.getWidth();
		int h = c.getHeight();
		float dx = (float) w / (float) nx;
		float dy = (float) h / (float) ny;

		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		g.setColor(color);
		g.setStroke(stroke);

		int k;
		for (int i = 0; i < nx; i += gridSize) {
			k = Math.round(i * dx);
			g.drawLine(k, 0, k, h);
		}
		for (int i = 0; i < ny; i += gridSize) {
			k = Math.round(i * dy);
			g.drawLine(0, k, w, k);
		}

		g.setColor(oldColor);
		g.setStroke(oldStroke);

	}

}
