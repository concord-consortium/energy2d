/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.math;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
public class Ring2D extends Area {

	private float x, y, outerDiameter, innerDiameter;

	public Ring2D(float x, float y, float innerDiameter, float outerDiameter) {
		super(new Ellipse2D.Float(x - 0.5f * outerDiameter, y - 0.5f
				* outerDiameter, outerDiameter, outerDiameter));
		subtract(new Area(new Ellipse2D.Float(x - 0.5f * innerDiameter, y
				- 0.5f * innerDiameter, innerDiameter, innerDiameter)));
		this.x = x;
		this.y = y;
		this.innerDiameter = innerDiameter;
		this.outerDiameter = outerDiameter;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getInnerDiameter() {
		return innerDiameter;
	}

	public float getOuterDiameter() {
		return outerDiameter;
	}

}
