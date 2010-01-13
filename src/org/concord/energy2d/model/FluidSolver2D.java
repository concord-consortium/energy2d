/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.model;

import java.util.Arrays;

import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
abstract class FluidSolver2D {

	static byte relaxationSteps = 5;
	private float thermalBuoyancy = 0.00025f;
	private float gravity = 0;

	/*
	 * By default, air's kinematic viscosity = 1.568 x 10^-5 m^2/s at 27 C is
	 * used. It can be set to zero for inviscid fluid.
	 */
	float viscosity = 0.00001568f;

	int nx, ny, nx1, ny1, nx2, ny2;
	float[][] u0, v0;
	float timeStep = 1;
	float deltaX, deltaY;
	boolean[][] fluidity;
	Boundary boundary;
	float[][] t;
	float[][] uWind, vWind;

	private float i2dx, i2dy;
	float idxsq, idysq;

	FluidSolver2D(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
		nx1 = nx - 1;
		ny1 = ny - 1;
		nx2 = nx - 2;
		ny2 = ny - 2;
		u0 = new float[nx][ny];
		v0 = new float[nx][ny];
	}

	void reset() {
		for (int i = 0; i < nx; i++) {
			Arrays.fill(u0[i], 0);
			Arrays.fill(v0[i], 0);
		}
	}

	void setThermalBuoyancy(float thermalBuoyancy) {
		this.thermalBuoyancy = thermalBuoyancy;
	}

	float getThermalBuoyancy() {
		return thermalBuoyancy;
	}

	void setWindSpeed(float[][] uWind, float[][] vWind) {
		this.uWind = uWind;
		this.vWind = vWind;
	}

	void setViscosity(float viscosity) {
		this.viscosity = viscosity;
	}

	float getViscosity() {
		return viscosity;
	}

	void setTemperature(float[][] t) {
		this.t = t;
	}

	void setFluidity(boolean[][] fluidity) {
		this.fluidity = fluidity;
	}

	void setGridCellSize(float deltaX, float deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		i2dx = 0.5f / deltaX;
		i2dy = 0.5f / deltaY;
		idxsq = 1f / (deltaX * deltaX);
		idysq = 1f / (deltaY * deltaY);
	}

	void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}

	float getTimeStep() {
		return timeStep;
	}

	/* b=1 horizontal; b=2 vertical */
	void applyBoundary(int b, float[][] f) {
		boolean horizontal = b == 1;
		boolean vertical = b == 2;
		for (int i = 1; i < nx1; i++) {
			// upper side
			f[i][0] = vertical ? -f[i][1] : f[i][1];
			// lower side
			f[i][ny1] = vertical ? -f[i][ny2] : f[i][ny2];
		}
		for (int j = 1; j < ny1; j++) {
			// left side
			f[0][j] = horizontal ? -f[1][j] : f[1][j];
			// right side
			f[nx1][j] = horizontal ? -f[nx2][j] : f[nx2][j];
		}
		// upper-left corner
		f[0][0] = 0.5f * (f[1][0] + f[0][1]);
		// upper-right corner
		f[nx1][0] = 0.5f * (f[nx2][0] + f[nx1][1]);
		// lower-left corner
		f[0][ny1] = 0.5f * (f[1][ny1] + f[0][ny2]);
		// lower-right corner
		f[nx1][ny1] = 0.5f * (f[nx2][ny1] + f[nx1][ny2]);
	}

	private void setObstacleVelocity(float[][] u, float[][] v) {
		int count = 0;
		float uw, vw;
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (!fluidity[i][j]) {
					uw = uWind[i][j];
					vw = vWind[i][j];
					count = 0;
					if (fluidity[i - 1][j]) {
						count++;
						u[i][j] = uw - u[i - 1][j];
						v[i][j] = vw + v[i - 1][j];
					} else if (fluidity[i + 1][j]) {
						count++;
						u[i][j] = uw - u[i + 1][j];
						v[i][j] = vw + v[i + 1][j];
					}
					if (fluidity[i][j - 1]) {
						count++;
						u[i][j] = uw + u[i][j - 1];
						v[i][j] = vw - v[i][j - 1];
					} else if (fluidity[i][j + 1]) {
						count++;
						u[i][j] = uw + u[i][j + 1];
						v[i][j] = vw - v[i][j + 1];
					}
					if (count == 0) {
						u[i][j] = uw;
						v[i][j] = vw;
					}
				}
			}
		}
	}

	// ensure dx/dn = 0 at the boundary (the Neumann boundary condition)
	private void setObstacleBoundary(float[][] x) {
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (!fluidity[i][j]) {
					if (fluidity[i - 1][j]) {
						x[i][j] = x[i - 1][j];
					} else if (fluidity[i + 1][j]) {
						x[i][j] = x[i + 1][j];
					}
					if (fluidity[i][j - 1]) {
						x[i][j] = x[i][j - 1];
					} else if (fluidity[i][j + 1]) {
						x[i][j] = x[i][j + 1];
					}
				}
			}
		}
	}

	private float getMeanTemperature(int i, int j) {
		int lowerBound = 0;
		// search for the upper bound
		for (int k = j - 1; k > 0; k--) {
			if (!fluidity[i][k]) {
				lowerBound = k;
				break;
			}
		}
		int upperBound = ny;
		for (int k = j + 1; k < ny; k++) {
			if (!fluidity[i][k]) {
				upperBound = k;
				break;
			}
		}
		float t0 = 0;
		for (int k = lowerBound; k < upperBound; k++) {
			t0 += t[i][k];
		}
		return t0 / (upperBound - lowerBound);
	}

	private void applyBuoyancy(float[][] f) {
		float g = gravity * timeStep;
		float b = thermalBuoyancy * timeStep;
		float t0;
		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					t0 = getMeanTemperature(i, j);
					f[i][j] += (g - b) * t[i][j] + b * t0;
				}
			}
		}
	}

	abstract void diffuse(int b, float[][] f0, float[][] f);

	abstract void advect(int b, float[][] f0, float[][] f);

	// TODO: swap the two arrays instead of copying them every time?
	void solve(float[][] u, float[][] v) {
		if (thermalBuoyancy != 0) {
			applyBuoyancy(v);
		}
		setObstacleVelocity(u, v);
		if (viscosity > 0) { // inviscid case
			MiscUtil.copy(u0, u);
			MiscUtil.copy(v0, v);
			diffuse(1, u0, u);
			diffuse(2, v0, v);
			conserve(u, v, u0, v0);
			setObstacleVelocity(u, v);
		}
		MiscUtil.copy(u0, u);
		MiscUtil.copy(v0, v);
		advect(1, u0, u);
		advect(2, v0, v);
		conserve(u, v, u0, v0);
		setObstacleVelocity(u, v);
	}

	/*
	 * enforce the continuity condition div(V)=0 (velocity field must be
	 * divergence-free to conserve mass) using the relaxation method:
	 * http://en.wikipedia.org/wiki/Relaxation_method. This procedure solves the
	 * Poisson equation.
	 */
	void conserve(float[][] u, float[][] v, float[][] phi, float[][] div) {

		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					div[i][j] = (u[i + 1][j] - u[i - 1][j]) * i2dx
							+ (v[i][j + 1] - v[i][j - 1]) * i2dy;
					phi[i][j] = 0;
				}
			}
		}
		applyBoundary(0, div);
		applyBoundary(0, phi);
		setObstacleBoundary(div);
		setObstacleBoundary(phi);

		float s = 0.5f / (idxsq + idysq);

		for (int k = 0; k < relaxationSteps; k++) {
			for (int i = 1; i < nx1; i++) {
				for (int j = 1; j < ny1; j++) {
					if (fluidity[i][j]) {
						phi[i][j] = s
								* ((phi[i - 1][j] + phi[i + 1][j]) * idxsq
										+ (phi[i][j - 1] + phi[i][j + 1])
										* idysq - div[i][j]);
					}
				}
			}
		}

		for (int i = 1; i < nx1; i++) {
			for (int j = 1; j < ny1; j++) {
				if (fluidity[i][j]) {
					u[i][j] -= (phi[i + 1][j] - phi[i - 1][j]) * i2dx;
					v[i][j] -= (phi[i][j + 1] - phi[i][j - 1]) * i2dy;
				}
			}
		}
		applyBoundary(1, u);
		applyBoundary(2, v);

	}

}
