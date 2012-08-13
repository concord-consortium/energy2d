/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Clouds (three circles intersected within a rectangle) are expensive to calculate. So we use additional variables (x, y) for setting locations and avoiding recalculation of shapes when moving them.
 * 
 * @author Charles Xie
 * 
 */
public class Cloud extends Manipulable {

	private float x; // the x coordinate of the upper-left corner
	private float y; // the y coordinate of the upper-left corner
	private float speed; // clouds only move in the horizontal direction
	private Rectangle2D.Float boundingBox;

	public Cloud(Shape bb) {
		super(bb);
		if (!(bb instanceof Rectangle2D.Float))
			throw new IllegalArgumentException("Shape must be a Rectangle2D.Float");
		boundingBox = (Rectangle2D.Float) bb;
		setShape(getShape(boundingBox));
	}

	// the size and shape of a cloud are determined by its bounding box that cuts three circles
	public static Area getShape(Rectangle2D.Float r) {
		float max = Math.max(r.width, r.height);
		Area a = new Area(new Ellipse2D.Float(r.x, r.y + r.height / 2, max / 2, max / 2));
		a.add(new Area(new Ellipse2D.Float(r.x + r.width / 3, r.y + r.height / 3, max / 2, max / 2)));
		a.add(new Area(new Ellipse2D.Float(r.x + 2 * r.width / 3, r.y + 2 * r.height / 3, max / 3, max / 3)));
		a.intersect(new Area(r));
		return a;
	}

	public void move(float timeStep, float lx) {
		if (speed == 0)
			return;
		x += speed * timeStep;
		// apply periodic boundary condition
		if (x > lx)
			x -= lx + boundingBox.width;
		else if (x < -boundingBox.width)
			x += lx + boundingBox.width;
	}

	public void translateBy(float dx, float dy) {
		x += dx;
		y += dy;
	}

	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean contains(float rx, float ry) {
		return getShape().contains(rx - x, ry - y);
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public Rectangle2D.Float getBoundingBox() {
		return boundingBox;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}

	@Override
	public Manipulable duplicate(float x, float y) {
		Cloud c = new Cloud(new Rectangle2D.Float(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height));
		c.speed = speed;
		c.setLabel(getLabel());
		c.setX(x - boundingBox.width / 2); // offset to the center, since this method is called to paste.
		c.setY(y - boundingBox.height / 2);
		return c;
	}

	public String toXml() {
		String xml = "<cloud";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + uid + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		xml += " x=\"" + x + "\"";
		xml += " y=\"" + y + "\"";
		xml += " width=\"" + boundingBox.width + "\"";
		xml += " height=\"" + boundingBox.height + "\"";
		xml += " speed=\"" + speed + "\"/>";
		return xml;
	}

}
