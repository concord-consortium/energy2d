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

		GeneralPath path = new GeneralPath();
		path.moveTo(poly.xpoints[n - 1], poly.ypoints[n - 1]);

		float u;
		float sx, sy;
		int index;
		float invStep = 1f / steps;
		float[] px = new float[4];
		float[] py = new float[4];

		for (int i = 0; i < n; i++) {

			for (int j = 0; j < 4; j++) { // Initialize points m-2, m-1, m, m+1
				index = (i + j - 2 + n) % n;
				px[j] = poly.xpoints[index];
				py[j] = poly.ypoints[index];
			}

			for (int k = 0; k < steps; k++) {
				u = k * invStep;
				sx = catmullrom(-2, u) * px[0] + catmullrom(-1, u) * px[1] + catmullrom(0, u) * px[2] + catmullrom(1, u) * px[3];
				sy = catmullrom(-2, u) * py[0] + catmullrom(-1, u) * py[1] + catmullrom(0, u) * py[2] + catmullrom(1, u) * py[3];
				path.lineTo(sx * 0.5f, sy * 0.5f);
			}
		}

		path.closePath();

		return path;

	}

	// Catmull-Rom spline function
	private static float catmullrom(int i, float u) {
		switch (i) {
		case -2:
			return u * (u * (2 - u) - 1);
		case -1:
			return u * u * (3 * u - 5) + 2;
		case 0:
			return u * ((4 - 3 * u) * u + 1);
		case 1:
			return u * u * (u - 1);
		}
		return 0;
	}

}
