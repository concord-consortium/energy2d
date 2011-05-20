/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JComponent;

import org.concord.energy2d.model.TimedData;

/**
 * @author Charles Xie
 * 
 */
class GraphRenderer {

	final static byte CLOSE_BUTTON = 0;
	final static byte X_EXPAND_BUTTON = 1;
	final static byte X_SHRINK_BUTTON = 2;
	final static byte Y_EXPAND_BUTTON = 3;
	final static byte Y_SHRINK_BUTTON = 4;

	private final static DecimalFormat FORMAT = new DecimalFormat("##.##");
	private Font smallFont = new Font(null, Font.PLAIN, 9);
	private Font labelFont = new Font(null, Font.PLAIN | Font.BOLD, 12);
	private Stroke frameStroke = new BasicStroke(2);
	private Stroke thinStroke = new BasicStroke(1);
	private Stroke curveStroke = new BasicStroke(1.5f);
	private Color bgColor = new Color(255, 255, 225, 128);
	private Color fgColor = Color.black;
	private Color frameColor = new Color(205, 205, 205, 128);
	private int x, y, w, h;
	private float scopeX = 360000; // 100 hours
	private float scopeY = 50;
	private boolean drawFrame = true;
	private Rectangle closeButton;
	private Rectangle xExpandButton, xShrinkButton;
	private Rectangle yExpandButton, yShrinkButton;
	private String xLabel = View2D.DEFAULT_XLABEL, yLabel = View2D.DEFAULT_YLABEL;

	GraphRenderer(int x, int y, int w, int h) {
		closeButton = new Rectangle();
		xExpandButton = new Rectangle();
		xShrinkButton = new Rectangle();
		yExpandButton = new Rectangle();
		yShrinkButton = new Rectangle();
		setFrame(x, y, w, h);
	}

	void setLabelX(String xLabel) {
		this.xLabel = xLabel;
	}

	String getLabelX() {
		return xLabel;
	}

	void setLabelY(String yLabel) {
		this.yLabel = yLabel;
	}

	String getLabelY() {
		return yLabel;
	}

	void setScopeX(float scopeX) {
		this.scopeX = scopeX;
	}

	void expandScopeX() {
		scopeX *= 2;
	}

	void shrinkScopeX() {
		scopeX *= 0.5f;
	}

	void expandScopeY() {
		scopeY *= 2;
	}

	void shrinkScopeY() {
		scopeY *= 0.5f;
	}

	void setDrawFrame(boolean b) {
		drawFrame = b;
	}

	void setFrame(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		closeButton.setBounds(x + w - 20, y, 20, 20);
		xExpandButton.setBounds(x + w - 44, y, 20, 20);
		xShrinkButton.setBounds(x + w - 68, y, 20, 20);
		yExpandButton.setBounds(x + w - 92, y, 20, 20);
		yShrinkButton.setBounds(x + w - 116, y, 20, 20);
	}

	boolean buttonContains(byte button, int x0, int y0) {
		switch (button) {
		case CLOSE_BUTTON:
			return closeButton.contains(x0, y0);
		case X_EXPAND_BUTTON:
			return xExpandButton.contains(x0, y0);
		case X_SHRINK_BUTTON:
			return xShrinkButton.contains(x0, y0);
		case Y_EXPAND_BUTTON:
			return yExpandButton.contains(x0, y0);
		case Y_SHRINK_BUTTON:
			return yShrinkButton.contains(x0, y0);
		default:
			return false;
		}
	}

	private static void centerString(String s, Graphics2D g, int x, int y) {
		int stringWidth = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x - stringWidth / 2, y);
	}

	void render(JComponent c, Graphics2D g, List<TimedData> data, String label, boolean highlight) {

		if (!c.isVisible())
			return;

		if (drawFrame) {

			// draw graph canvas
			g.setColor(bgColor);
			g.fillRoundRect(x - 10, y - 10, w + 20, h + 20, 20, 20);
			g.setStroke(frameStroke);
			g.setColor(frameColor);
			g.drawRoundRect(x - 10, y - 10, w + 20, h + 20, 20, 20);

			g.setStroke(thinStroke);

			// draw close button
			g.setColor(fgColor);
			g.fillRect(closeButton.x + 2, closeButton.y + 2, closeButton.width, closeButton.height);
			g.setColor(Color.lightGray);
			g.fill(closeButton);
			g.setColor(fgColor);
			g.draw(closeButton);
			g.drawLine(closeButton.x + 4, closeButton.y + 4, closeButton.x + closeButton.width - 4, closeButton.y + closeButton.height - 4);
			g.drawLine(closeButton.x + 4, closeButton.y + closeButton.height - 4, closeButton.x + closeButton.width - 4, closeButton.y + 4);

			// draw x scope control buttons
			g.setColor(fgColor);
			g.fillRect(xExpandButton.x + 2, xExpandButton.y + 2, xExpandButton.width, xExpandButton.height);
			g.setColor(Color.lightGray);
			g.fill(xExpandButton);
			g.setColor(fgColor);
			g.draw(xExpandButton);
			int y2 = xExpandButton.y + xExpandButton.height / 2;
			int x2 = xExpandButton.x + xExpandButton.width - 4;
			g.drawLine(xExpandButton.x + 3, y2, x2, y2);
			x2 = xExpandButton.x + 3;
			g.drawLine(x2, y2, x2 + 4, y2 - 4);
			g.drawLine(x2, y2, x2 + 4, y2 + 4);

			g.setColor(fgColor);
			g.fillRect(xShrinkButton.x + 2, xShrinkButton.y + 2, xShrinkButton.width, xShrinkButton.height);
			g.setColor(Color.lightGray);
			g.fill(xShrinkButton);
			g.setColor(fgColor);
			g.draw(xShrinkButton);
			x2 = xShrinkButton.x + xShrinkButton.width - 4;
			g.drawLine(xShrinkButton.x + 3, y2, x2, y2);
			g.drawLine(x2, y2, x2 - 4, y2 - 4);
			g.drawLine(x2, y2, x2 - 4, y2 + 4);

			// draw y scope control buttons
			g.setColor(fgColor);
			g.fillRect(yExpandButton.x + 2, yExpandButton.y + 2, yExpandButton.width, yExpandButton.height);
			g.setColor(Color.lightGray);
			g.fill(yExpandButton);
			g.setColor(fgColor);
			g.draw(yExpandButton);
			x2 = yExpandButton.x + yExpandButton.width / 2;
			y2 = yExpandButton.y + yExpandButton.height - 4;
			g.drawLine(x2, yExpandButton.y + 3, x2, y2);
			g.drawLine(x2, y2 + 1, x2 + 4, y2 - 3);
			g.drawLine(x2, y2 + 1, x2 - 4, y2 - 3);

			g.setColor(fgColor);
			g.fillRect(yShrinkButton.x + 2, yShrinkButton.y + 2, yShrinkButton.width, yShrinkButton.height);
			g.setColor(Color.lightGray);
			g.fill(yShrinkButton);
			g.setColor(fgColor);
			g.draw(yShrinkButton);
			x2 = yShrinkButton.x + yShrinkButton.width / 2;
			g.drawLine(x2, yShrinkButton.y + 3, x2, y2);
			g.drawLine(x2, yShrinkButton.y + 3, x2 + 4, yShrinkButton.y + 6);
			g.drawLine(x2, yShrinkButton.y + 3, x2 - 4, yShrinkButton.y + 6);

			// draw axes
			g.drawLine(x, y, x, y + h);
			g.drawLine(x, y + h, x + w, y + h);
			g.drawLine(x, y, x - 2, y + 4);
			g.drawLine(x, y, x + 2, y + 4);
			g.drawLine(x + w, y + h, x + w - 4, y + h - 2);
			g.drawLine(x + w, y + h, x + w - 4, y + h + 2);
			g.setFont(smallFont);
			int k;
			for (int i = 1; i < 10; i++) {
				k = x + Math.round(i * w * 0.1f);
				if (i % 2 == 0) {
					g.drawLine(k, y + h, k, y + h - 4);
					centerString(FORMAT.format(scopeX * i * 0.1f / 3600f), g, k + 3, y + h - 8);
				} else {
					g.drawLine(k, y + h, k, y + h - 2);
				}
			}
			centerString(xLabel, g, x + w - 10, y + h - 4);

			for (int i = 1; i < 10; i++) {
				k = y + Math.round(i * h * 0.1f);
				if (i % 2 == 0) {
					g.drawLine(x, k, x + 4, k);
					centerString(FORMAT.format(scopeY * (1 - i * 0.1f)), g, x + 12, k + 3);
				} else {
					g.drawLine(x, k, x + 2, k);
				}
			}
			centerString(yLabel, g, x + 40, y + 10);
			drawFrame = false;

		}

		g.setStroke(curveStroke);
		g.setColor(highlight ? Color.yellow : fgColor);

		int n = data.size();
		if (n > 0) {
			int m = Math.max(1, (int) (n / w));
			TimedData d = data.get(0);
			float t1 = d.getTime();
			float v1 = d.getValue();
			float t2, v2;
			int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
			float scaleX = w / scopeX;
			float scaleY = h / scopeY;
			synchronized (data) {
				for (int i = m; i < n - m; i += m) {
					x1 = (int) (x + t1 * scaleX);
					y1 = (int) (y + h - v1 * scaleY);
					if (x1 > x + w)
						break;
					d = data.get(i);
					t2 = d.getTime();
					v2 = d.getValue();
					x2 = (int) (x + t2 * scaleX);
					y2 = (int) (y + h - v2 * scaleY);
					g.drawLine(x1, y1, x2, y2);
					t1 = t2;
					v1 = v2;
				}
			}
			if (label != null) {
				g.setFont(labelFont);
				g.drawString(label, x2 + 5, y2);
			}
		}

	}

}
