package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.concord.energy2d.event.MeasurementEvent;
import org.concord.energy2d.event.MeasurementListener;

/**
 * @author Charles Xie
 * 
 */
public abstract class Sensor extends Manipulable {

	public final static byte ONE_POINT = 1;
	public final static byte FIVE_POINT = 5;
	public final static byte NINE_POINT = 9;

	byte stencil = ONE_POINT;

	final static int MAX = 1000;
	List<TimedData> data;
	private List<MeasurementListener> listeners;

	public Sensor(Shape shape) {
		super(shape);
		data = Collections.synchronizedList(new ArrayList<TimedData>());
		listeners = new ArrayList<MeasurementListener>();
	}

	public void setCenter(float x, float y) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.x = x - 0.5f * r.width;
			r.y = y - 0.5f * r.height;
		} else {
			// TODO: none-rectangular shape
		}
	}

	public void translateBy(float dx, float dy) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.x += dx;
			r.y += dy;
		} else {
			// TODO: none-rectangular shape
		}
	}

	public void setX(float x) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.x = x - 0.5f * r.width;
		} else {
			// TODO: none-rectangular shape
		}
	}

	public void setY(float y) {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			r.y = y - 0.5f * r.height;
		} else {
			// TODO: none-rectangular shape
		}
	}

	/** returns the x coordinate of the center */
	public float getX() {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			return r.x + 0.5f * r.width;
		}
		return (float) getShape().getBounds2D().getCenterX();
	}

	/** returns the y coordinate of the center */
	public float getY() {
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			return r.y + 0.5f * r.height;
		}
		return (float) getShape().getBounds2D().getCenterY();
	}

	public void setStencil(byte stencil) {
		this.stencil = stencil;
	}

	public byte getStencil() {
		return stencil;
	}

	public void addMeasurementListener(MeasurementListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
	}

	public void removeMeasurementListener(MeasurementListener l) {
		listeners.remove(l);
	}

	private void notifyMeasurementListeners() {
		if (listeners.isEmpty())
			return;
		MeasurementEvent e = new MeasurementEvent(this);
		for (MeasurementListener x : listeners)
			x.measurementTaken(e);
	}

	public void clear() {
		data.clear();
		notifyMeasurementListeners();
	}

	public List<TimedData> getData() {
		return data;
	}

	public float getCurrentData() {
		if (data.isEmpty())
			return Float.NaN;
		return data.get(data.size() - 1).getValue();
	}

	public void addData(float time, float x) {
		data.add(new TimedData(time, x));
		notifyMeasurementListeners();
		if (data.size() > MAX)
			data.remove(0);
	}

	public abstract String toXml();

}
