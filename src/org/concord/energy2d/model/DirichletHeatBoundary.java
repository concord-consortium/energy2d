/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public class DirichletHeatBoundary implements HeatBoundary {

	// unit: centigrade
	private float[] temperatureAtBorder;

	public DirichletHeatBoundary() {
		temperatureAtBorder = new float[4];
		// by default all temperatures are zero
		setTemperatureAtBorder(UPPER, 0);
		setTemperatureAtBorder(LOWER, 0);
		setTemperatureAtBorder(LEFT, 0);
		setTemperatureAtBorder(RIGHT, 0);
	}

	public void setTemperatureAtBorder(byte side, float value) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		temperatureAtBorder[side] = value;
	}

	public float getTemperatureAtBorder(byte side) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		return temperatureAtBorder[side];
	}

	public String toXml() {
		String s = "<temperature_at_border upper=\""
				+ temperatureAtBorder[UPPER] + "\"";
		s += " lower=\"" + temperatureAtBorder[LOWER] + "\"";
		s += " left=\"" + temperatureAtBorder[LEFT] + "\"";
		s += " right=\"" + temperatureAtBorder[RIGHT] + "\"";
		s += "/>\n";
		return s;
	}

}
