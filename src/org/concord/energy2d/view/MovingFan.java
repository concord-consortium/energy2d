/*
 *   Copyright (C) 2013  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.geom.Rectangle2D;

import org.concord.energy2d.model.Fan;

/**
 * @author Charles Xie
 * 
 */
class MovingFan extends ComplexMovingShape {

	private Rectangle2D.Float boundingBox;

	MovingFan(Rectangle2D.Float boundingBox, float speed) {
		this.boundingBox = boundingBox;
		area = Fan.getShape(boundingBox, speed);
	}

	Rectangle2D.Float getBounds() {
		return boundingBox;
	}

}