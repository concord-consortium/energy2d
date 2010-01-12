/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.analytics.simulation2d.model;

/**
 * @author Charles Xie
 * 
 */
public class NeumannHeatBoundary implements HeatBoundary {

	// heat flux: unit w/m^2
	private float[] fluxAtBorder;

	public NeumannHeatBoundary() {
		fluxAtBorder = new float[4];
		// by default all fluxes are zero, meaning that the borders are
		// completely insulative
		setFluxAtBorder(UPPER, 0);
		setFluxAtBorder(LOWER, 0);
		setFluxAtBorder(LEFT, 0);
		setFluxAtBorder(RIGHT, 0);
	}

	public void setFluxAtBorder(byte side, float value) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		fluxAtBorder[side] = value;
	}

	public float getFluxAtBorder(byte side) {
		if (side < UPPER || side > LEFT)
			throw new IllegalArgumentException("side parameter illegal");
		return fluxAtBorder[side];
	}

}
