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
 * @author Charles Xie
 * 
 */
public class Cloud extends Manipulable {

	private float x;
	private float y;
	private float speed; // clouds only move in the horizontal direction
	private Rectangle2D.Float boundingBox;

	public Cloud(Shape bb) {
		super(bb);
		if (!(bb instanceof Rectangle2D.Float))
			throw new IllegalArgumentException("Shape must be a Rectangle2D.Float");
		boundingBox = (Rectangle2D.Float) bb;
		float x2 = boundingBox.x;
		float y2 = boundingBox.y;
		float w2 = boundingBox.width;
		float h2 = boundingBox.height;
		float max = Math.max(w2, h2);
		Area a = new Area(new Ellipse2D.Float(x2, y2 + h2 / 2, max / 2, max / 2));
		a.add(new Area(new Ellipse2D.Float(x2 + w2 / 3, y2 + h2 / 3, max / 2, max / 2)));
		a.add(new Area(new Ellipse2D.Float(x2 + 2 * w2 / 3, y2 + 2 * h2 / 3, max / 3, max / 3)));
		a.intersect(new Area(boundingBox));
		setShape(a);
	}

	public void move(float timeStep, float lx) {
		if (speed == 0)
			return;
		x += speed * timeStep;
		// apply periodic boundary condition
		if (x > lx + boundingBox.width)
			x -= lx + 2 * boundingBox.width;
		else if (x < -boundingBox.width)
			x += lx + 2 * boundingBox.width;
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
		return null;
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
