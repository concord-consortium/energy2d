/*
 *   Copyright (C) 2013  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.model;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * Fans are massless objects that change the flow speed of the fluid passing them. They could be used as windbreaks that reduce the fluid speed as well.
 * 
 * @author Charles Xie
 * 
 */
public class Fan extends Manipulable {

	private float speed = 0.01f;
	private Color color = Color.GRAY;

	public Fan(Rectangle2D.Float shape) {
		super(shape);
	}

	public static Area getShape(Rectangle2D.Float r, float speed) {
		int deg = (int) Math.round(Math.toDegrees(Math.asin(r.height / Math.hypot(r.width, r.height))));
		if (r.height > r.width) {
			Area a = new Area(new Arc2D.Float(r, deg, 180 - 2 * deg, Arc2D.PIE));
			a.add(new Area(new Arc2D.Float(r, -deg, 2 * deg - 180, Arc2D.PIE)));
			a.add(new Area(new Rectangle2D.Float(speed >= 0 ? r.x : r.x + r.width * 0.5f, r.y + r.height * 0.5f - 2, r.width * 0.5f, 4)));
			return a;
		}
		Area a = new Area(new Arc2D.Float(r, deg, -2 * deg, Arc2D.PIE));
		a.add(new Area(new Arc2D.Float(r, 180 - deg, 2 * deg, Arc2D.PIE)));
		a.add(new Area(new Rectangle2D.Float(r.x + r.width * 0.5f - 2, speed > 0 ? r.y : r.y + r.height * 0.5f, 4, r.height * 0.5f)));
		return a;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void translateBy(float dx, float dy) {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		r.x += dx;
		r.y += dy;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}

	@Override
	public Manipulable duplicate(float x, float y) {
		Fan fan = new Fan((Rectangle2D.Float) getShape());
		fan.speed = speed;
		fan.setLabel(getLabel());
		fan.color = color;
		return fan;
	}

	public String toXml() {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		String xml = "<fan";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + uid + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		if (!Color.GRAY.equals(color))
			xml += " color=\"" + Integer.toHexString(0x00ffffff & color.getRGB()) + "\"";
		xml += " x=\"" + r.x + "\"";
		xml += " y=\"" + r.y + "\"";
		xml += " width=\"" + r.width + "\"";
		xml += " height=\"" + r.height + "\"";
		xml += " speed=\"" + speed + "\"/>";
		return xml;
	}

}
