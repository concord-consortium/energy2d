/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.analytics.simulation2d.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;

import org.concord.analytics.math.Polygon2D;

/**
 * @author Charles Xie
 * 
 */
public abstract class Manipulable {

	private boolean selected;
	private boolean filled = true;
	private Shape shape;
	private Color color = Color.gray;
	private boolean draggable = true;
	private boolean visible = true;

	public Manipulable(Shape shape) {
		setShape(shape);
	}

	public boolean contains(float x, float y) {
		return shape.contains(x, y);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public Shape getShape() {
		return shape;
	}

	public Point2D.Float getCenter() {
		if (shape instanceof Polygon2D)
			return ((Polygon2D) shape).getCenter();
		return new Point2D.Float((float) shape.getBounds2D().getCenterX(),
				(float) shape.getBounds2D().getCenterY());
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setFilled(boolean b) {
		filled = b;
	}

	public boolean isFilled() {
		return filled;
	}

}
