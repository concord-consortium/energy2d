/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.math;

import java.awt.Polygon;
import java.awt.geom.GeneralPath;

/**
 * @author Charles Xie
 * 
 */
public class MathUtil {

	/** @return true if x is between a and b. */
	public final static boolean between(float a, float b, float x) {
		return x < Math.max(a, b) && x > Math.min(a, b);
	}

	public static float getMax(float[] array) {
		float max = -Float.MAX_VALUE;
		for (float x : array) {
			if (x > max)
				max = x;
		}
		return max;
	}

	public static float getMin(float[] array) {
		float min = Float.MAX_VALUE;
		for (float x : array) {
			if (x < min)
				min = x;
		}
		return min;
	}

	public static float getMax(float[][] array) {
		float max = -Float.MAX_VALUE;
		for (float[] a : array) {
			for (float x : a) {
				if (x > max)
					max = x;
			}
		}
		return max;
	}

	public static float getMin(float[][] array) {
		float min = Float.MAX_VALUE;
		for (float[] a : array) {
			for (float x : a) {
				if (x < min)
					min = x;
			}
		}
		return min;
	}

	public static float getAverage(float[][] array) {
		float ave = 0;
		for (float[] a : array) {
			for (float x : a) {
				ave += x;
			}
		}
		return ave / (array.length * array[0].length);
	}

	public static GeneralPath createSpline(Polygon poly, int steps) {

		if (poly == null || steps <= 1)
			throw new IllegalArgumentException("Illegal arguments: " + poly + ", " + steps);
		int n = poly.npoints;
		if (n < 3)
			throw new IllegalArgumentException("Polygon must have at least three vertices.");

		final float tension = 0.5f; // the default Catmull-Rom tension parameter

		GeneralPath path = new GeneralPath();
		path.moveTo(poly.xpoints[n - 1], poly.ypoints[n - 1]);

		float u;
		float px, py;
		int index;
		float invStep = 1f / steps;
		float[] xcr = new float[4];
		float[] ycr = new float[4];

		for (int i = 0; i < n; i++) {

			for (int j = 0; j < 4; j++) { // Initialize points m-2, m-1, m, m+1
				index = (i + j - 2 + n) % n;
				xcr[j] = poly.xpoints[index];
				ycr[j] = poly.ypoints[index];
			}

			for (int k = 0; k < steps; k++) {
				u = k * invStep;
				px = getBendingFunction(-2, u, tension) * xcr[0] + getBendingFunction(-1, u, tension) * xcr[1] + getBendingFunction(0, u, tension) * xcr[2] + getBendingFunction(1, u, tension) * xcr[3];
				py = getBendingFunction(-2, u, tension) * ycr[0] + getBendingFunction(-1, u, tension) * ycr[1] + getBendingFunction(0, u, tension) * ycr[2] + getBendingFunction(1, u, tension) * ycr[3];
				path.lineTo(px, py);
			}
		}

		path.closePath();

		return path;

	}

	// Catmull-Rom spline function
	private static float getBendingFunction(int i, float u, float tension) {

		if (i < -2 || i > 1)
			throw new IllegalArgumentException("i cannot be " + i);
		if (u < 0 || u > 1)
			throw new IllegalArgumentException("u=" + u + " is outside [0,1]");

		switch (i) {
		case -2:
			return u * tension * (-1 + u * (2 - u));
		case -1:
			return 1 + u * u * ((tension - 3) + (2 - tension) * u);
		case 0:
			return u * (tension + u * ((3 - 2 * tension) + (tension - 2) * u));
		case 1:
			return tension * u * u * (-1 + u);
		}

		return 0;

	}

}
