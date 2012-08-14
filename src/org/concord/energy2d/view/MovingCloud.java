/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.concord.energy2d.model.Cloud;

/**
 * Similar to the Cloud class, this class uses a location point to avoid recalculating the area.
 * 
 * @author Charles Xie
 * 
 */
class MovingCloud implements MovingShape {

	private Area area;
	private Point location;

	MovingCloud(Rectangle2D.Float boundingBox) {
		area = Cloud.getShape(boundingBox);
		location = new Point();
	}

	public void setLocation(int x, int y) {
		location.setLocation(x, y);
	}

	public Point getLocation() {
		return location;
	}

	public int getX() {
		return location.x;
	}

	public int getY() {
		return location.y;
	}

	public Shape getShape() {
		return area;
	}

	public void render(Graphics2D g) {
		g.translate(location.x, location.y);
		g.draw(area);
		g.translate(-location.x, -location.y);
	}

}