/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

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
public class Thermometer extends Sensor {

	public final static byte ONE_POINT = 1;
	public final static byte FIVE_POINT = 5;
	public final static byte NINE_POINT = 9;

	private final static int MAX = 1000;
	private List<TimedData> data;
	private List<MeasurementListener> listeners;
	private boolean thermostat;
	private float thermostatTemperature = 20;
	private byte stencil = ONE_POINT;

	public final static float RELATIVE_WIDTH = 0.025f;
	public final static float RELATIVE_HEIGHT = 0.05f;

	public Thermometer(float x, float y) {
		super(new Rectangle2D.Float());
		// should have used Point2D but it is not a Shape.
		data = Collections.synchronizedList(new ArrayList<TimedData>());
		listeners = new ArrayList<MeasurementListener>();
		setCenter(x, y);
	}

	public Thermometer(float x, float y, String label) {
		this(x, y);
		setLabel(label);
	}

	public Thermometer duplicate(float x, float y) {
		return new Thermometer(x, y);
	}

	public void setThermostat(boolean b) {
		thermostat = b;
	}

	public boolean isThermostat() {
		return thermostat;
	}

	public void setThermostatTemperature(float t) {
		thermostatTemperature = t;
	}

	public float getThermostatTemperature() {
		return thermostatTemperature;
	}

	public void setStencil(byte stencil) {
		this.stencil = stencil;
	}

	public byte getStencil() {
		return stencil;
	}

	public void setCenter(float x, float y) {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		r.x = x - 0.5f * r.width;
		r.y = y - 0.5f * r.height;
	}

	public void setX(float x) {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		r.x = x - 0.5f * r.width;
	}

	public void setY(float y) {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		r.y = y - 0.5f * r.height;
	}

	/** returns the x coordinate of the center */
	public float getX() {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		return r.x + 0.5f * r.width;
	}

	/** returns the y coordinate of the center */
	public float getY() {
		Rectangle2D.Float r = (Rectangle2D.Float) getShape();
		return r.y + 0.5f * r.height;
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

	public void addData(float time, float temperature) {
		data.add(new TimedData(time, temperature));
		notifyMeasurementListeners();
		if (data.size() > MAX)
			data.remove(0);
	}

	public String toXml() {
		String xml = "<thermometer";
		if (stencil != ONE_POINT)
			xml += " stencil=\"" + stencil + "\"";
		if (thermostat) {
			xml += " thermostat=\"true\"";
			xml += " thermostat_temperature=\"" + thermostatTemperature + "\"";
		}
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>";
		return xml;
	}

}
