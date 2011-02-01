/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.math;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Mutatable implementation of polygon (GeneralPath is immutatable).
 * 
 * @author Charles Xie
 * 
 */
public class Polygon2D implements Shape {

	private Point2D.Float[] vertex;
	private GeneralPath path;

	/** the coordinates of the vertices of this polygon. */
	public Polygon2D(float[] x, float[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException(
					"the number of x coodinates must be equal to that of the y coordinates.");
		if (x.length < 3)
			throw new IllegalArgumentException("the number of vertices must be no less than 3.");
		vertex = new Point2D.Float[x.length];
		for (int i = 0; i < x.length; i++)
			setVertex(i, x[i], y[i]);
		path = new GeneralPath();
	}

	public Polygon2D duplicate() {
		int n = vertex.length;
		float[] x = new float[n];
		float[] y = new float[n];
		for (int i = 0; i < n; i++) {
			x[i] = vertex[i].x;
			y[i] = vertex[i].y;
		}
		return new Polygon2D(x, y);
	}

	private void updatePath() {
		path.reset();
		path.moveTo(vertex[0].x, vertex[0].y);
		for (int i = 1; i < vertex.length; i++)
			path.lineTo(vertex[i].x, vertex[i].y);
		path.closePath();
	}

	public void setVertex(int i, float x, float y) {
		if (i < 0 || i >= vertex.length)
			throw new IllegalArgumentException("index of vertex is out of bound.");
		if (vertex[i] == null)
			vertex[i] = new Point2D.Float(x, y);
		else
			vertex[i].setLocation(x, y);
	}

	public Point2D.Float getVertex(int i) {
		if (i < 0 || i >= vertex.length)
			throw new IllegalArgumentException("index of vertex is out of bound.");
		return vertex[i];
	}

	public int getVertexCount() {
		return vertex.length;
	}

	public void translateBy(float dx, float dy) {
		for (int i = 0; i < vertex.length; i++) {
			vertex[i].x += dx;
			vertex[i].y += dy;
		}
	}

	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}

	public boolean intersects(Rectangle r) {
		updatePath();
		return path.intersects(r);
	}

	public boolean contains(double x, double y) {
		updatePath();
		return path.contains(x, y);
	}

	public Point2D.Float getCenter() {
		float xc = 0;
		float yc = 0;
		for (Point2D.Float v : vertex) {
			xc += v.x;
			yc += v.y;
		}
		return new Point2D.Float(xc / vertex.length, yc / vertex.length);
	}

	public Rectangle getBounds() {
		int xmin = Integer.MAX_VALUE;
		int ymin = xmin;
		int xmax = -xmin;
		int ymax = -xmin;
		for (Point2D.Float v : vertex) {
			if (xmin > v.x)
				xmin = (int) v.x;
			if (ymin > v.y)
				ymin = (int) v.y;
			if (xmax < v.x)
				xmax = (int) v.x;
			if (ymax < v.y)
				ymax = (int) v.y;
		}
		return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	public Rectangle2D getBounds2D() {
		return getBounds();
	}

	public boolean contains(Rectangle2D r) {
		updatePath();
		return path.contains(r);
	}

	public boolean contains(double x, double y, double w, double h) {
		updatePath();
		return path.contains(x, y, w, h);
	}

	public PathIterator getPathIterator(AffineTransform at) {
		updatePath();
		return path.getPathIterator(at);
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		updatePath();
		return path.getPathIterator(at, flatness);
	}

	public boolean intersects(Rectangle2D r) {
		updatePath();
		return intersects(r);
	}

	public boolean intersects(double x, double y, double w, double h) {
		updatePath();
		return intersects(x, y, w, h);
	}

}
