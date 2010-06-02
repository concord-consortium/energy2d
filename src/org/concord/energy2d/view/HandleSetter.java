/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.model.Manipulable;

import static org.concord.energy2d.view.View2D.BOTTOM;
import static org.concord.energy2d.view.View2D.LEFT;
import static org.concord.energy2d.view.View2D.LOWER_LEFT;
import static org.concord.energy2d.view.View2D.LOWER_RIGHT;
import static org.concord.energy2d.view.View2D.RIGHT;
import static org.concord.energy2d.view.View2D.TOP;
import static org.concord.energy2d.view.View2D.UPPER_LEFT;
import static org.concord.energy2d.view.View2D.UPPER_RIGHT;

/**
 * @author Charles Xie
 * 
 */
class HandleSetter {

	static void setRects(View2D view, Manipulable m, Rectangle[] handle) {

		int h = handle[0].width / 2;
		Shape s = m.getShape();

		if (s instanceof RectangularShape) {
			Rectangle2D bound = s.getBounds2D();
			handle[UPPER_LEFT].x = view.convertPointToPixelX((float) bound
					.getMinX())
					- h;
			handle[UPPER_LEFT].y = view.convertPointToPixelY((float) bound
					.getMinY())
					- h;
			handle[LOWER_LEFT].x = view.convertPointToPixelX((float) bound
					.getMinX())
					- h;
			handle[LOWER_LEFT].y = view.convertPointToPixelY((float) bound
					.getMaxY())
					- h;
			handle[UPPER_RIGHT].x = view.convertPointToPixelX((float) bound
					.getMaxX())
					- h;
			handle[UPPER_RIGHT].y = view.convertPointToPixelY((float) bound
					.getMinY())
					- h;
			handle[LOWER_RIGHT].x = view.convertPointToPixelX((float) bound
					.getMaxX())
					- h;
			handle[LOWER_RIGHT].y = view.convertPointToPixelY((float) bound
					.getMaxY())
					- h;
			handle[TOP].x = view.convertPointToPixelX((float) bound
					.getCenterX())
					- h;
			handle[TOP].y = view.convertPointToPixelY((float) bound.getMinY())
					- h;
			handle[BOTTOM].x = view.convertPointToPixelX((float) bound
					.getCenterX())
					- h;
			handle[BOTTOM].y = view.convertPointToPixelY((float) bound
					.getMaxY())
					- h;
			handle[LEFT].x = view.convertPointToPixelX((float) bound.getMinX())
					- h;
			handle[LEFT].y = view.convertPointToPixelY((float) bound
					.getCenterY())
					- h;
			handle[RIGHT].x = view
					.convertPointToPixelX((float) bound.getMaxX())
					- h;
			handle[RIGHT].y = view.convertPointToPixelY((float) bound
					.getCenterY())
					- h;
		}

		else if (s instanceof Polygon2D) {

			Polygon2D p = (Polygon2D) s;
			int n = p.getVertexCount();
			Point2D.Float point;
			if (n <= handle.length) {
				for (int i = 0; i < handle.length; i++) {
					if (i < n) {
						point = p.getVertex(i);
						handle[i].x = view.convertPointToPixelX(point.x) - h;
						handle[i].y = view.convertPointToPixelY(point.y) - h;
					} else {
						handle[i].x = handle[i].y = -100;
					}
				}
			} else {
				float k = (float) n / (float) handle.length;
				for (int i = 0; i < handle.length; i++) {
					point = p.getVertex((int) (i * k));
					handle[i].x = view.convertPointToPixelX(point.x) - h;
					handle[i].y = view.convertPointToPixelY(point.y) - h;
				}
			}

		}

	}

}
