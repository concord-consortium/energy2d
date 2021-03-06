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
class VectorDistributionRenderer {

	private final static float COS = (float) Math.cos(Math.toRadians(30));
	private final static float SIN = (float) Math.sin(Math.toRadians(30));

	private Stroke stroke = new BasicStroke(1);
	private int nx;
	private int ny;
	private int spacing = 4;
	private float scale = 100;
	private View2D view;

	VectorDistributionRenderer(View2D view, int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
		this.view = view;
	}

	void setStroke(Stroke s) {
		stroke = s;
	}

	void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	int getSpacing() {
		return spacing;
	}

	void drawVector(Graphics2D g, int x, int y, float vx, float vy, float scale) {
		float r = 1f / (float) Math.hypot(vx, vy);
		float arrowx = vx * r;
		float arrowy = vy * r;
		r = ((BasicStroke) stroke).getLineWidth();
		float x1 = x + arrowx * (6 + r * 2) + vx * scale;
		float y1 = y + arrowy * (6 + r * 2) + vy * scale;
		g.drawLine(x, y, Math.round(x1), Math.round(y1));
		r = 4;
		float wingx = r * (arrowx * COS + arrowy * SIN);
		float wingy = r * (arrowy * COS - arrowx * SIN);
		g.drawLine(Math.round(x1), Math.round(y1), Math.round(x1 - wingx), Math.round(y1 - wingy));
		wingx = r * (arrowx * COS - arrowy * SIN);
		wingy = r * (arrowy * COS + arrowx * SIN);
		g.drawLine(Math.round(x1), Math.round(y1), Math.round(x1 - wingx), Math.round(y1 - wingy));
	}

	void renderVectors(float[][] u, float[][] v, JComponent c, Graphics2D g) {

		if (!c.isVisible())
			return;

		int w = c.getWidth();
		int h = c.getHeight();
		float dx = (float) w / (float) nx;
		float dy = (float) h / (float) ny;

		g.setStroke(stroke);
		int x, y;
		float uij, vij;
		Color color = null;
		for (int i = 1; i < nx - 1; i += spacing) {
			x = Math.round(i * dx);
			for (int j = 1; j < ny - 1; j += spacing) {
				y = Math.round(j * dy);
				uij = u[i][j];
				vij = v[i][j];
				if (uij * uij + vij * vij > 0.0000000001f) {
					color = view.getContrastColor(x, y);
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
					g.setColor(color);
					drawVector(g, x, y, uij, vij, scale);
				}
			}
		}

	}

	// special case
	void renderHeatFlux(float[][] t, float[][] k, JComponent c, Graphics2D g) {

		if (!c.isVisible())
			return;

		int w = c.getWidth();
		int h = c.getHeight();
		float dx = (float) w / (float) nx;
		float dy = (float) h / (float) ny;

		g.setStroke(stroke);
		int x, y;
		float uij, vij;
		Color color = null;
		for (int i = 1; i < nx - 1; i += spacing) {
			x = Math.round(i * dx);
			for (int j = 1; j < ny - 1; j += spacing) {
				y = Math.round(j * dy);
				uij = -k[i][j] * (t[i + 1][j] - t[i - 1][j]) / (2 * dx);
				vij = -k[i][j] * (t[i][j + 1] - t[i][j - 1]) / (2 * dy);
				if (uij * uij + vij * vij > 0.00000001f) {
					color = view.getContrastColor(x, y);
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
					g.setColor(color);
					drawVector(g, x, y, uij, vij, scale);
				}
			}
		}

	}
}
