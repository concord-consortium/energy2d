/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

import java.util.Arrays;

/**
 * @author Charles Xie
 * 
 */
abstract class HeatSolver2D {

	int nx, ny, nx1, ny1, nx2, ny2;
	ThermalBoundary boundary;
	float[][] conductivity;
	float[][] specificHeat;
	float[][] density;
	float[][] q;
	float[][] u, v;
	float[][] tb;
	float[][] t0; // array that stores the previous temperature results
	boolean[][] fluidity;
	float deltaX, deltaY;
	float timeStep = 0.5f;
	float backgroundTemperature;
	float zHeatDiffusivity;

	HeatSolver2D(int nx, int ny) {
		this.nx = nx;
		this.ny = ny;
		nx1 = nx - 1;
		ny1 = ny - 1;
		nx2 = nx - 2;
		ny2 = ny - 2;
		t0 = new float[nx][ny];
		boundary = new DirichletThermalBoundary();
	}

	void reset() {
		for (int i = 0; i < nx; i++) {
			Arrays.fill(t0[i], 0);
		}
	}

	void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}

	float getTimeStep() {
		return timeStep;
	}

	void setFluidity(boolean[][] fluidity) {
		this.fluidity = fluidity;
	}

	void setGridCellSize(float deltaX, float deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	void setBoundary(ThermalBoundary boundary) {
		this.boundary = boundary;
	}

	ThermalBoundary getBoundary() {
		return boundary;
	}

	void setVelocity(float[][] u, float[][] v) {
		this.u = u;
		this.v = v;
	}

	void setConductivity(float[][] conductivity) {
		this.conductivity = conductivity;
	}

	void setSpecificHeat(float[][] specificHeat) {
		this.specificHeat = specificHeat;
	}

	void setDensity(float[][] density) {
		this.density = density;
	}

	void setPower(float[][] q) {
		this.q = q;
	}

	void setTemperatureBoundary(float[][] tb) {
		this.tb = tb;
	}

	abstract void solve(boolean convective, float[][] t);

	void applyBoundary(float[][] t) {

		if (boundary instanceof DirichletThermalBoundary) {
			DirichletThermalBoundary b = (DirichletThermalBoundary) boundary;
			float tUpper = b.getTemperatureAtBorder(Boundary.UPPER);
			float tLower = b.getTemperatureAtBorder(Boundary.LOWER);
			float tLeft = b.getTemperatureAtBorder(Boundary.LEFT);
			float tRight = b.getTemperatureAtBorder(Boundary.RIGHT);
			for (int i = 0; i < nx; i++) {
				t[i][0] = tUpper;
				t[i][ny1] = tLower;
			}
			for (int j = 0; j < ny; j++) {
				t[0][j] = tLeft;
				t[nx1][j] = tRight;
			}
		} else if (boundary instanceof ComplexDirichletThermalBoundary) {
			ComplexDirichletThermalBoundary b = (ComplexDirichletThermalBoundary) boundary;
			float[] tUpper = b.getTemperaturesAtBorder(Boundary.UPPER);
			float[] tLower = b.getTemperaturesAtBorder(Boundary.LOWER);
			float[] tLeft = b.getTemperaturesAtBorder(Boundary.LEFT);
			float[] tRight = b.getTemperaturesAtBorder(Boundary.RIGHT);
			for (int i = 0; i < nx; i++) {
				t[i][0] = tUpper[i];
				t[i][ny1] = tLower[i];
			}
			for (int j = 0; j < ny; j++) {
				t[0][j] = tLeft[j];
				t[nx1][j] = tRight[j];
			}
		} else if (boundary instanceof NeumannThermalBoundary) {
			NeumannThermalBoundary b = (NeumannThermalBoundary) boundary;
			float fN = b.getFluxAtBorder(Boundary.UPPER);
			float fS = b.getFluxAtBorder(Boundary.LOWER);
			float fW = b.getFluxAtBorder(Boundary.LEFT);
			float fE = b.getFluxAtBorder(Boundary.RIGHT);
			for (int i = 0; i < nx; i++) {
				t[i][0] = t[i][1] + fN * deltaY / conductivity[i][0];
				t[i][ny1] = t[i][ny2] - fS * deltaY / conductivity[i][ny1];
			}
			for (int j = 0; j < ny; j++) {
				t[0][j] = t[1][j] - fW * deltaX / conductivity[0][j];
				t[nx1][j] = t[nx2][j] + fE * deltaX / conductivity[nx1][j];
			}
		}

	}

}
