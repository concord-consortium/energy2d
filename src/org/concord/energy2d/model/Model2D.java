/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.event.VisualizationEvent;
import org.concord.energy2d.event.VisualizationListener;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.math.Ring2D;

/**
 * Units:
 * 
 * Temperature: centigrade; Length: meter; Time: second; Thermal diffusivity: m^2/s; Power: centigrade/second
 * 
 * @author Charles Xie
 * 
 */
public class Model2D {

	public final static byte BUOYANCY_AVERAGE_ALL = 0;
	public final static byte BUOYANCY_AVERAGE_COLUMN = 1;

	private int indexOfStep;
	private float stopTime = -1;

	private float backgroundConductivity = 10 * Constants.AIR_THERMAL_CONDUCTIVITY;
	private float backgroundSpecificHeat = Constants.AIR_SPECIFIC_HEAT;
	private float backgroundDensity = Constants.AIR_DENSITY;
	private float backgroundTemperature;

	/*
	 * temperature array. On Java 6, using a 1D array and then a convenience function I(i, j) =i + j x ny to find t(i, j) is about 12% faster than using a 2D array directly. Hence, using 1D array for 2D functions doesn't result in significant performance improvements (the JRE probably have already optimized this for us).
	 */
	private float[][] t;

	// velocity x-component array (m/s)
	private float[][] u;

	// velocity y-component array (m/s)
	private float[][] v;

	// internal temperature boundary array
	private float[][] tb;

	// internal heat generation array
	private float[][] q;

	// wind speed
	private float[][] uWind, vWind;

	// conductivity array
	private float[][] conductivity;

	// specific heat array
	private float[][] specificHeat;

	// density array
	private float[][] density;

	// fluid cell array
	private boolean[][] fluidity;

	private float maximumHeatCapacity = -1, minimumHeatCapacity = Float.MAX_VALUE;

	private List<Thermometer> thermometers;

	private List<Part> parts;
	private List<Photon> photons;

	private RaySolver2D raySolver;
	private FluidSolver2D fluidSolver;
	private HeatSolver2D heatSolver;

	private boolean sunny;
	private int photonEmissionInterval = 20;

	private int nx = 100;
	private int ny = 100;

	// length in x direction (unit: meter)
	private float lx = 10;

	// length in y direction (unit: meter)
	private float ly = 10;

	private float deltaX = lx / nx;
	private float deltaY = ly / ny;

	private boolean running;
	private boolean notifyReset;
	private int viewUpdateInterval = 20;
	private int measurementInterval = 100;

	// optimization flags
	private boolean hasPartPower;
	private boolean radiative;

	// condition flags
	private boolean convective = true;

	private List<VisualizationListener> visualizationListeners;
	private List<PropertyChangeListener> propertyChangeListeners;
	private List<ManipulationListener> manipulationListeners;

	public Model2D() {

		t = new float[nx][ny];
		u = new float[nx][ny];
		v = new float[nx][ny];
		q = new float[nx][ny];
		tb = new float[nx][ny];
		uWind = new float[nx][ny];
		vWind = new float[nx][ny];
		conductivity = new float[nx][ny];
		specificHeat = new float[nx][ny];
		density = new float[nx][ny];
		fluidity = new boolean[nx][ny];

		init();

		heatSolver = new HeatSolver2DImpl(nx, ny);
		heatSolver.setSpecificHeat(specificHeat);
		heatSolver.setConductivity(conductivity);
		heatSolver.setDensity(density);
		heatSolver.setPower(q);
		heatSolver.setVelocity(u, v);
		heatSolver.setTemperatureBoundary(tb);
		heatSolver.setFluidity(fluidity);

		fluidSolver = new FluidSolver2DImpl(nx, ny);
		fluidSolver.setFluidity(fluidity);
		fluidSolver.setTemperature(t);
		fluidSolver.setWindSpeed(uWind, vWind);

		raySolver = new RaySolver2D(lx, ly);
		raySolver.setPower(q);

		setGridCellSize();

		parts = Collections.synchronizedList(new ArrayList<Part>());
		thermometers = Collections.synchronizedList(new ArrayList<Thermometer>());
		photons = Collections.synchronizedList(new ArrayList<Photon>());

		visualizationListeners = new ArrayList<VisualizationListener>();
		propertyChangeListeners = new ArrayList<PropertyChangeListener>();
		manipulationListeners = new ArrayList<ManipulationListener>();

	}

	public void setStopTime(float stopTime) {
		this.stopTime = stopTime;
	}

	public float getStopTime() {
		return stopTime;
	}

	public void setConvective(boolean convective) {
		this.convective = convective;
	}

	public boolean isConvective() {
		return convective;
	}

	/**
	 * Imagine that the 2D plane is thermally coupled with a thin layer that has the background temperature
	 */
	public void setZHeatDiffusivity(float zHeatDiffusivity) {
		heatSolver.zHeatDiffusivity = zHeatDiffusivity;
	}

	public float getZHeatDiffusivity() {
		return heatSolver.zHeatDiffusivity;
	}

	public void setThermalBuoyancy(float thermalBuoyancy) {
		fluidSolver.setThermalBuoyancy(thermalBuoyancy);
	}

	public float getThermalBuoyancy() {
		return fluidSolver.getThermalBuoyancy();
	}

	public void setBuoyancyApproximation(byte buoyancyApproximation) {
		fluidSolver.setBuoyancyApproximation(buoyancyApproximation);
	}

	public byte getBuoyancyApproximation() {
		return fluidSolver.getBuoyancyApproximation();
	}

	public void setBackgroundViscosity(float viscosity) {
		fluidSolver.setBackgroundViscosity(viscosity);
	}

	public float getBackgroundViscosity() {
		return fluidSolver.getViscosity();
	}

	public void setSunny(boolean sunny) {
		this.sunny = sunny;
		if (sunny) {
			radiative = true;
		} else {
			photons.clear();
		}
	}

	public boolean isSunny() {
		return sunny;
	}

	public void setSunAngle(float sunAngle) {
		if (Math.abs(sunAngle - raySolver.getSunAngle()) < 0.001f)
			return;
		photons.clear();
		raySolver.setSunAngle(sunAngle);
	}

	public float getSunAngle() {
		return raySolver.getSunAngle();
	}

	public void setSolarPowerDensity(float solarPowerDensity) {
		raySolver.setSolarPowerDensity(solarPowerDensity);
	}

	public float getSolarPowerDensity() {
		return raySolver.getSolarPowerDensity();
	}

	public void setSolarRayCount(int solarRayCount) {
		if (solarRayCount == raySolver.getSolarRayCount())
			return;
		photons.clear();
		raySolver.setSolarRayCount(solarRayCount);
	}

	public int getSolarRayCount() {
		return raySolver.getSolarRayCount();
	}

	public void setSolarRaySpeed(float raySpeed) {
		raySolver.setSolarRaySpeed(raySpeed);
	}

	public float getSolarRaySpeed() {
		return raySolver.getSolarRaySpeed();
	}

	public void setPhotonEmissionInterval(int photonEmissionInterval) {
		this.photonEmissionInterval = photonEmissionInterval;
	}

	public int getPhotonEmissionInterval() {
		return photonEmissionInterval;
	}

	public void addPhoton(Photon p) {
		if (p != null)
			photons.add(p);
	}

	public void removePhoton(Photon p) {
		photons.remove(p);
	}

	public List<Photon> getPhotons() {
		return photons;
	}

	private void setGridCellSize() {
		heatSolver.setGridCellSize(deltaX, deltaY);
		fluidSolver.setGridCellSize(deltaX, deltaY);
		raySolver.setGridCellSize(deltaX, deltaY);
	}

	public void setLx(float lx) {
		this.lx = lx;
		deltaX = lx / nx;
		setGridCellSize();
		raySolver.setLx(lx);
	}

	public float getLx() {
		return lx;
	}

	public void setLy(float ly) {
		this.ly = ly;
		deltaY = ly / ny;
		setGridCellSize();
		raySolver.setLy(ly);
	}

	public float getLy() {
		return ly;
	}

	public HeatBoundary getHeatBoundary() {
		return heatSolver.getBoundary();
	}

	public void setHeatBoundary(HeatBoundary b) {
		heatSolver.setBoundary(b);
	}

	public void setBackgroundTemperature(float backgroundTemperature) {
		this.backgroundTemperature = backgroundTemperature;
		heatSolver.backgroundTemperature = backgroundTemperature;
	}

	public float getBackgroundTemperature() {
		return backgroundTemperature;
	}

	public void setBackgroundConductivity(float backgroundConductivity) {
		this.backgroundConductivity = backgroundConductivity;
	}

	public float getBackgroundConductivity() {
		return backgroundConductivity;
	}

	public void setBackgroundSpecificHeat(float backgroundSpecificHeat) {
		this.backgroundSpecificHeat = backgroundSpecificHeat;
	}

	public float getBackgroundSpecificHeat() {
		return backgroundSpecificHeat;
	}

	public void setBackgroundDensity(float backgroundDensity) {
		this.backgroundDensity = backgroundDensity;
	}

	public float getBackgroundDensity() {
		return backgroundDensity;
	}

	/** return the Prandtl Number of the background fluid */
	public float getPrandtlNumber() {
		return getBackgroundViscosity() * backgroundDensity * backgroundSpecificHeat / backgroundConductivity;
	}

	public void addThermometer(Thermometer t) {
		thermometers.add(t);
	}

	public void addThermometer(float x, float y) {
		thermometers.add(new Thermometer(x, y));
	}

	public void addThermometer(float x, float y, String label, byte stencil) {
		Thermometer t = new Thermometer(x, y);
		t.setLabel(label);
		t.setStencil(stencil);
		thermometers.add(t);
	}

	public List<Thermometer> getThermometers() {
		return thermometers;
	}

	public Part addRectangularPart(float x, float y, float w, float h) {
		Part p = new Part(new Rectangle2D.Float(x, y, w, h));
		addPart(p);
		return p;
	}

	public Part addEllipticalPart(float x, float y, float a, float b) {
		Part p = new Part(new Ellipse2D.Float(x - 0.5f * a, y - 0.5f * b, a, b));
		addPart(p);
		return p;
	}

	public Part addRingPart(float x, float y, float inner, float outer) {
		Part p = new Part(new Ring2D(x, y, inner, outer));
		addPart(p);
		return p;
	}

	public Part addPolygonPart(float[] x, float[] y) {
		Part p = new Part(new Polygon2D(x, y));
		addPart(p);
		return p;
	}

	public List<Part> getParts() {
		return parts;
	}

	public Part getPart(String uid) {
		if (uid == null)
			return null;
		synchronized (parts) {
			for (Part p : parts) {
				if (uid.equals(p.getUid()))
					return p;
			}
		}
		return null;
	}

	public boolean isUidUsed(String uid) {
		if (uid == null)
			throw new IllegalArgumentException("UID cannot be null.");
		synchronized (parts) {
			for (Part p : parts) {
				if (uid.equals(p.getUid()))
					return true;
			}
		}
		return false;
	}

	public Part getPart(int i) {
		if (i < 0 || i >= parts.size())
			return null;
		return parts.get(i);
	}

	public int getPartCount() {
		return parts.size();
	}

	public void addPart(Part p) {
		if (!parts.contains(p)) {
			parts.add(p);
			if (p.getPower() != 0)
				hasPartPower = true;
			if (p.getEmissivity() > 0)
				radiative = true;
		}
	}

	public void removePart(Part p) {
		parts.remove(p);
		checkPartPower();
		checkPartRadiation();
	}

	public float getMaximumHeatCapacity() {
		return maximumHeatCapacity;
	}

	public float getMinimumHeatCapacity() {
		return minimumHeatCapacity;
	}

	public void refreshMaterialPropertyArrays() {
		float x, y, windSpeed;
		boolean initial = indexOfStep == 0;
		maximumHeatCapacity = minimumHeatCapacity = backgroundDensity * backgroundSpecificHeat;
		float heatCapacity = 0;
		for (int i = 0; i < nx; i++) {
			x = i * deltaX;
			for (int j = 0; j < ny; j++) {
				y = j * deltaY;
				conductivity[i][j] = backgroundConductivity;
				specificHeat[i][j] = backgroundSpecificHeat;
				density[i][j] = backgroundDensity;
				fluidity[i][j] = true;
				uWind[i][j] = vWind[i][j] = 0;
				synchronized (parts) {
					for (Part p : parts) {
						if (p.getShape().contains(x, y)) {
							// no overlap of parts will be allowed
							conductivity[i][j] = p.getThermalConductivity();
							specificHeat[i][j] = p.getSpecificHeat();
							density[i][j] = p.getDensity();
							if (!initial && p.getConstantTemperature())
								t[i][j] = p.getTemperature();
							fluidity[i][j] = false;
							if ((windSpeed = p.getWindSpeed()) != 0) {
								uWind[i][j] = (float) (windSpeed * Math.cos(p.getWindAngle()));
								vWind[i][j] = (float) (windSpeed * Math.sin(p.getWindAngle()));
							}
							break;
						}
					}
				}
				heatCapacity = specificHeat[i][j] * density[i][j];
				if (maximumHeatCapacity < heatCapacity)
					maximumHeatCapacity = heatCapacity;
				if (minimumHeatCapacity > heatCapacity)
					minimumHeatCapacity = heatCapacity;
			}
		}
		if (initial) {
			setInitialTemperature();
			setInitialVelocity();
		}
	}

	public void refreshPowerArray() {
		checkPartPower();
		float x, y;
		for (int i = 0; i < nx; i++) {
			x = i * deltaX;
			for (int j = 0; j < ny; j++) {
				y = j * deltaY;
				q[i][j] = 0;
				if (hasPartPower) {
					synchronized (parts) {
						for (Part p : parts) {
							if (p.getPower() != 0 && p.getShape().contains(x, y)) {
								// no overlap of parts will be allowed
								q[i][j] = p.getPower();
								break;
							}
						}
					}
				}
			}
		}
	}

	public void refreshTemperatureBoundaryArray() {
		float x, y;
		for (int i = 0; i < nx; i++) {
			x = i * deltaX;
			for (int j = 0; j < ny; j++) {
				y = j * deltaY;
				tb[i][j] = Float.NaN;
				synchronized (parts) {
					for (Part p : parts) {
						if (p.getConstantTemperature() && p.getShape().contains(x, y)) {
							tb[i][j] = p.getTemperature();
							break;
						}
					}
				}
			}
		}
	}

	/** get the total thermal energy stored in this part */
	public float getThermalEnergy(Part p) {
		float x, y;
		float energy = 0;
		for (int i = 0; i < nx; i++) {
			x = i * deltaX;
			for (int j = 0; j < ny; j++) {
				y = j * deltaY;
				if (p.getShape().contains(x, y)) {
					// no overlap of parts will be allowed
					energy += t[i][j] * density[i][j] * specificHeat[i][j];
				}
			}
		}
		return energy * deltaX * deltaY;
	}

	private void init() {
		for (int i = 0; i < nx; i++) {
			Arrays.fill(conductivity[i], backgroundConductivity);
			Arrays.fill(specificHeat[i], backgroundSpecificHeat);
			Arrays.fill(density[i], backgroundDensity);
		}
		setInitialTemperature();
	}

	public void clear() {
		parts.clear();
		photons.clear();
		thermometers.clear();
		maximumHeatCapacity = -1;
		minimumHeatCapacity = Float.MAX_VALUE;
	}

	private void setInitialVelocity() {
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				if (fluidity[i][j]) {
					u[i][j] = v[i][j] = 0;
				} else {
					u[i][j] = uWind[i][j];
					v[i][j] = vWind[i][j];
				}
			}
		}
	}

	public void setInitialTemperature() {
		if (parts == null) {
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					t[i][j] = backgroundTemperature;
				}
			}
		} else {
			float x, y;
			boolean found = false;
			for (int i = 0; i < nx; i++) {
				x = i * deltaX;
				for (int j = 0; j < ny; j++) {
					y = j * deltaY;
					found = false;
					synchronized (parts) {
						for (Part p : parts) {
							if (p.getShape().contains(x, y)) {
								// no overlap of parts will be allowed
								t[i][j] = p.getTemperature();
								found = true;
								break;
							}
						}
					}
					if (!found)
						t[i][j] = backgroundTemperature;
				}
			}
		}
		if (thermometers != null && !thermometers.isEmpty()) {
			synchronized (thermometers) {
				for (Thermometer t : thermometers) {
					t.clear();
				}
			}
		}
	}

	public void run() {
		checkPartPower();
		checkPartRadiation();
		refreshPowerArray();
		if (!running) {
			running = true;
			while (running)
				nextStep();
			if (notifyReset) {
				indexOfStep = 0;
				reallyReset();
				notifyVisualizationListeners();
				notifyReset = false;
			}
		}
	}

	public void stop() {
		running = false;
	}

	public void reset() {
		if (running) {
			stop();
			notifyReset = true;
		} else {
			reallyReset();
		}
		running = false;
		indexOfStep = 0;
	}

	private void reallyReset() {
		setInitialTemperature();
		setInitialVelocity();
		photons.clear();
		heatSolver.reset();
		fluidSolver.reset();
	}

	private void checkPartPower() {
		hasPartPower = false;
		synchronized (parts) {
			for (Part p : parts) {
				if (p.getPower() != 0) {
					hasPartPower = true;
					break;
				}
			}
		}
	}

	private void checkPartRadiation() {
		radiative = sunny;
		if (!radiative) {
			synchronized (parts) {
				for (Part p : parts) {
					if (p.getEmissivity() > 0) {
						radiative = true;
						break;
					}
				}
			}
		}
	}

	private void nextStep() {
		if (stopTime > 0) {
			if (indexOfStep > 0) {
				if (indexOfStep % Math.round(stopTime / getTimeStep()) == 0) {
					stop();
					notifyManipulationListeners(ManipulationEvent.AUTO_STOP);
				}
			}
		}
		if (radiative) {
			if (indexOfStep % photonEmissionInterval == 0) {
				refreshPowerArray();
				if (sunny)
					raySolver.sunShine(photons, parts);
				raySolver.radiate(this);
			}
			raySolver.solve(this);
		}
		if (convective) {
			fluidSolver.solve(u, v);
		}
		heatSolver.solve(convective, t);
		if (indexOfStep % measurementInterval == 0) {
			takeMeasurement();
		}
		if (indexOfStep % viewUpdateInterval == 0) {
			notifyVisualizationListeners();
		}
		indexOfStep++;
	}

	public void setViewUpdateInterval(int viewUpdateInterval) {
		this.viewUpdateInterval = viewUpdateInterval;
	}

	public int getViewUpdateInterval() {
		return viewUpdateInterval;
	}

	public void setMeasurementInterval(int measurementInterval) {
		this.measurementInterval = measurementInterval;
	}

	public int getMeasurementInterval() {
		return measurementInterval;
	}

	public float getTime() {
		return indexOfStep * heatSolver.getTimeStep();
	}

	public void setTimeStep(float timeStep) {
		notifyPropertyChangeListeners("Time step", getTimeStep(), timeStep);
		heatSolver.setTimeStep(timeStep);
		fluidSolver.setTimeStep(timeStep);
	}

	public float getTimeStep() {
		return heatSolver.getTimeStep();
	}

	public void setTemperature(float[][] t) {
		this.t = t;
	}

	public float getTemperatureAt(float x, float y) {
		int i = Math.min(t.length - 1, Math.round(x / deltaX));
		int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
		return t[i][j];
	}

	public void setTemperatureAt(float x, float y, float temperature) {
		int i = Math.min(t.length - 1, Math.round(x / deltaX));
		int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
		t[i][j] = temperature;
	}

	float getAverageTemperatureAt(float x, float y) {
		float temp = 0;
		int i0 = Math.round(x / deltaX);
		int j0 = Math.round(y / deltaY);
		int i = Math.min(t.length - 1, i0);
		int j = Math.min(t[0].length - 1, j0);
		if (i < 0)
			i = 0;
		if (j < 0)
			j = 0;
		temp += t[i][j];
		i = Math.min(t.length - 1, i0 + 1);
		j = Math.min(t[0].length - 1, j0);
		if (i < 0)
			i = 0;
		if (j < 0)
			j = 0;
		temp += t[i][j];
		i = Math.min(t.length - 1, i0 - 1);
		j = Math.min(t[0].length - 1, j0);
		if (i < 0)
			i = 0;
		if (j < 0)
			j = 0;
		temp += t[i][j];
		i = Math.min(t.length - 1, i0);
		j = Math.min(t[0].length - 1, j0 + 1);
		if (i < 0)
			i = 0;
		if (j < 0)
			j = 0;
		temp += t[i][j];
		i = Math.min(t.length - 1, i0);
		j = Math.min(t[0].length - 1, j0 - 1);
		if (i < 0)
			i = 0;
		if (j < 0)
			j = 0;
		temp += t[i][j];
		return temp * 0.2f;
	}

	void changeAverageTemperatureAt(float x, float y, float increment) {
		increment *= 0.2f;
		int i0 = Math.round(x / deltaX);
		int j0 = Math.round(y / deltaY);
		int i = Math.min(t.length - 1, i0);
		int j = Math.min(t[0].length - 1, j0);
		if (i >= 0 && j >= 0)
			t[i][j] += increment;
		i = Math.min(t.length - 1, i0 + 1);
		j = Math.min(t[0].length - 1, j0);
		if (i >= 0 && j >= 0)
			t[i][j] += increment;
		i = Math.min(t.length - 1, i0 - 1);
		j = Math.min(t[0].length - 1, j0);
		if (i >= 0 && j >= 0)
			t[i][j] += increment;
		i = Math.min(t.length - 1, i0);
		j = Math.min(t[0].length - 1, j0 + 1);
		if (i >= 0 && j >= 0)
			t[i][j] += increment;
		i = Math.min(t.length - 1, i0);
		j = Math.min(t[0].length - 1, j0 - 1);
		if (i >= 0 && j >= 0)
			t[i][j] += increment;
	}

	public float[][] getTemperature() {
		return t;
	}

	public float[][] getXVelocity() {
		return u;
	}

	public float[][] getYVelocity() {
		return v;
	}

	public float[][] getStreamFunction() {
		return fluidSolver.getStreamFunction(u, v);
	}

	public float[][] getSpecificHeat() {
		return specificHeat;
	}

	public float[][] getDensity() {
		return density;
	}

	public float[][] getConductivity() {
		return conductivity;
	}

	private void takeMeasurement() {
		if (!thermometers.isEmpty()) {
			int ix, iy;
			synchronized (thermometers) {
				for (Thermometer m : thermometers) {
					ix = Math.round(m.getX() / deltaX);
					iy = Math.round(m.getY() / deltaY);
					if (ix >= 0 && ix < nx && iy >= 0 && iy < ny) {
						switch (m.getStencil()) {
						case Thermometer.ONE_POINT:
							m.addData(getTime(), t[ix][iy]);
							break;
						case Thermometer.FIVE_POINT:
							float temp = t[ix][iy];
							int count = 1;
							if (ix > 0) {
								temp += t[ix - 1][iy];
								count++;
							}
							if (ix < nx - 1) {
								temp += t[ix + 1][iy];
								count++;
							}
							if (iy > 0) {
								temp += t[ix][iy - 1];
								count++;
							}
							if (iy < ny - 1) {
								temp += t[ix][iy + 1];
								count++;
							}
							m.addData(getTime(), temp / count);
							break;
						case Thermometer.NINE_POINT:
							temp = t[ix][iy];
							count = 1;
							if (ix > 0) {
								temp += t[ix - 1][iy];
								count++;
							}
							if (ix < nx - 1) {
								temp += t[ix + 1][iy];
								count++;
							}
							if (iy > 0) {
								temp += t[ix][iy - 1];
								count++;
							}
							if (iy < ny - 1) {
								temp += t[ix][iy + 1];
								count++;
							}
							if (ix > 0 && iy > 0) {
								temp += t[ix - 1][iy - 1];
								count++;
							}
							if (ix > 0 && iy < ny - 1) {
								temp += t[ix - 1][iy + 1];
								count++;
							}
							if (ix < nx - 1 && iy > 0) {
								temp += t[ix + 1][iy - 1];
								count++;
							}
							if (ix < nx - 1 && iy < ny - 1) {
								temp += t[ix + 1][iy + 1];
								count++;
							}
							m.addData(getTime(), temp / count);
							break;
						}
					}
				}
			}
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!propertyChangeListeners.contains(listener))
			propertyChangeListeners.add(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listener != null)
			propertyChangeListeners.remove(listener);
	}

	private void notifyPropertyChangeListeners(String propertyName, Object oldValue, Object newValue) {
		if (propertyChangeListeners.isEmpty())
			return;
		PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		for (PropertyChangeListener x : propertyChangeListeners)
			x.propertyChange(e);
	}

	public void addVisualizationListener(VisualizationListener listener) {
		if (!visualizationListeners.contains(listener))
			visualizationListeners.add(listener);
	}

	public void removeVisualizationListener(VisualizationListener listener) {
		if (listener != null)
			visualizationListeners.remove(listener);
	}

	private void notifyVisualizationListeners() {
		if (visualizationListeners.isEmpty())
			return;
		VisualizationEvent e = new VisualizationEvent(this);
		for (VisualizationListener x : visualizationListeners)
			x.visualizationRequested(e);
	}

	public void addManipulationListener(ManipulationListener listener) {
		if (!manipulationListeners.contains(listener))
			manipulationListeners.add(listener);
	}

	public void removeManipulationListener(ManipulationListener listener) {
		if (listener != null)
			manipulationListeners.remove(listener);
	}

	private void notifyManipulationListeners(byte type) {
		if (manipulationListeners.isEmpty())
			return;
		ManipulationEvent e = new ManipulationEvent(this, type);
		for (ManipulationListener x : manipulationListeners)
			x.manipulationOccured(e);
	}

}
