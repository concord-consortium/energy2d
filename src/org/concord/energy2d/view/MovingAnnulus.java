/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
class MovingAnnulus implements MovingShape {

	private Area area;

	MovingAnnulus(Ellipse2D.Float outer, Ellipse2D.Float inner) {
		area = new Area(outer);
		area.subtract(new Area(inner));
	}

	public Shape getShape() {
		return area;
	}

	public void render(Graphics2D g) {
		g.draw(area);
	}

}