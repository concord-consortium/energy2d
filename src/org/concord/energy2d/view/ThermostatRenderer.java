/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.concord.energy2d.model.Thermostat;

/**
 * @author Charles Xie
 * 
 */
class ThermostatRenderer {

	private Stroke stroke = new BasicStroke(2);
	private Color color = Color.white;

	ThermostatRenderer() {
	}

	void render(Thermostat t, View2D v, Graphics2D g) {

		if (!v.isVisible())
			return;

		Stroke oldStroke = g.getStroke();
		Color oldColor = g.getColor();
		g.setStroke(stroke);
		g.setColor(color);

		int x1 = v.convertPointToPixelX(t.getThermometer().getX());
		int y1 = v.convertPointToPixelY(t.getThermometer().getY());
		int x2 = v.convertPointToPixelX(t.getPowerSource().getCenter().x);
		int y2 = v.convertPointToPixelX(t.getPowerSource().getCenter().y);
		g.drawLine(x1, y1, x1, y2);
		g.drawLine(x1, y2, x2, y2);
		g.setColor(Color.lightGray);
		g.fillOval(x1 - 3, y1 - 3, 6, 6);
		g.fillOval(x2 - 3, y2 - 3, 6, 6);

		g.setStroke(oldStroke);
		g.setColor(oldColor);

	}

}
