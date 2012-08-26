package org.concord.energy2d.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.math.Ring2D;

/**
 * Units:
 * 
 * Temperature: centigrade; Length: meter; Time: second; Thermal diffusivity: m^2/s; Power: centigrade/second.
 * 
 * Using a 1D array and then a convenience function I(i, j) =i + j x ny to find t(i, j) is about 12% faster than using a 2D array directly (Java 6). Hence, using 1D array for 2D functions doesn't result in significant performance improvements.
 * 
 * @author Charles Xie
 * 
 */
public class Model2D {

	public final static byte BUOYANCY_AVERAGE_ALL = 0;
	public final static byte BUOYANCY_AVERAGE_COLUMN = 1;
	public final static byte GRAVITY_UNIFORM = 0;
	public final static byte GRAVITY_CENTRIC = 1;

	private int indexOfStep;

	private float backgroundConductivity = 10 * Constants.AIR_THERMAL_CONDUCTIVITY;
	private float backgroundSpecificHeat = Constants.AIR_SPECIFIC_HEAT;
	private float backgroundDensity = Constants.AIR_DENSITY;
	private float backgroundTemperature;
	private float maximumHeatCapacity = -1;

	// temperature array
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

	private List<Anemometer> anemometers;
	private List<Thermometer> thermometers;
	private List<Thermostat> thermostats;
	private List<Part> parts;
	private List<Photon> photons;
	private List<Cloud> clouds;
	private List<Tree> trees;

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

	// optimization flags
	private boolean hasPartPower;
	private boolean radiative;

	// condition flags
	private boolean convective = true;

	private List<PropertyChangeListener> propertyChangeListeners;
	private List<ManipulationListener> manipulationListeners;
	private Runnable tasks;

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
		anemometers = Collections.synchronizedList(new ArrayList<Anemometer>());
		thermometers = Collections.synchronizedList(new ArrayList<Thermometer>());
		thermostats = Collections.synchronizedList(new ArrayList<Thermostat>());
		photons = Collections.synchronizedList(new ArrayList<Photon>());
		clouds = Collections.synchronizedList(new ArrayList<Cloud>());
		trees = Collections.synchronizedList(new ArrayList<Tree>());

		propertyChangeListeners = new ArrayList<PropertyChangeListener>();
		manipulationListeners = new ArrayList<ManipulationListener>();

	}

	public int getNx() {
		return nx;
	}

	public int getNy() {
		return ny;
	}

	public void setTasks(Runnable r) {
		tasks = r;
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

	public void setGravityType(byte gravityType) {
		fluidSolver.setGravityType(gravityType);
	}

	public byte getGravityType() {
		return fluidSolver.getGravityType();
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

	/** synchronize the sun's angle with the clock, assuming sunrise at 6:00 and sunset at 18:00. */
	public void moveSun(float sunrise, float sunset) {
		float hour = getTime() / 3600f;
		int i = (int) hour;
		hour += (i % 24) - i;
		raySolver.setSunAngle((hour - sunrise) / (sunset - sunrise) * (float) Math.PI);
		refreshPowerArray();
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

	public void addCloud(Cloud c) {
		if (c != null)
			clouds.add(c);
	}

	public void removeCloud(Cloud c) {
		clouds.remove(c);
	}

	public List<Cloud> getClouds() {
		return clouds;
	}

	public void addTree(Tree t) {
		if (t != null)
			trees.add(t);
	}

	public void removeTree(Tree t) {
		trees.remove(t);
	}

	public List<Tree> getTrees() {
		return trees;
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

	public void translateAllBy(float dx, float dy) {
		for (Thermometer t : thermometers)
			t.translateBy(dx, dy);
		for (Anemometer a : anemometers)
			a.translateBy(dx, dy);
		for (Cloud c : clouds)
			c.translateBy(dx, dy);
		for (Tree t : trees)
			t.translateBy(dx, dy);
		for (Part p : parts)
			p.translateBy(dx, dy);
	}

	public boolean scaleAll(float scale) {
		Rectangle2D.Float bound = new Rectangle2D.Float(0, 0, lx, ly);
		boolean out = false;
		for (Thermometer t : thermometers) {
			t.setCenter(scale * t.getX(), ly - scale * (ly - t.getY()));
			if (!bound.intersects(t.getShape().getBounds2D()))
				out = true;
		}
		for (Anemometer a : anemometers) {
			a.setCenter(scale * a.getX(), ly - scale * (ly - a.getY()));
			if (!bound.intersects(a.getShape().getBounds2D()))
				out = true;
		}
		for (Cloud c : clouds) {
			c.setLocation(scale * c.getX(), ly - scale * (ly - c.getY()));
			c.setDimension(c.getWidth() * scale, c.getHeight() * scale);
			if (!bound.intersects(c.getShape().getBounds2D()))
				out = true;
		}
		for (Tree t : trees) {
			t.setLocation(scale * t.getX(), ly - scale * (ly - t.getY()));
			t.setDimension(t.getWidth() * scale, t.getHeight() * scale);
			if (!bound.intersects(t.getShape().getBounds2D()))
				out = true;
		}
		for (Part p : parts) {
			Shape s = p.getShape();
			if (s instanceof Rectangle2D.Float) {
				Rectangle2D.Float r = (Rectangle2D.Float) s;
				r.x = scale * r.x;
				r.y = ly - scale * (ly - r.y);
				r.width *= scale;
				r.height *= scale;
				if (!bound.intersects(r))
					out = true;
			} else if (s instanceof Ellipse2D.Float) {
				Ellipse2D.Float e = (Ellipse2D.Float) s;
				e.x = scale * e.x;
				e.y = ly - scale * (ly - e.y);
				e.width *= scale;
				e.height *= scale;
				if (!bound.intersects(e.getBounds2D()))
					out = true;
			} else if (s instanceof Area) {
				((Area) s).transform(AffineTransform.getScaleInstance(scale, scale));
				if (!bound.intersects(s.getBounds2D()))
					out = true;
			} else if (s instanceof Polygon2D) {
				Polygon2D g = (Polygon2D) s;
				int n = g.getVertexCount();
				for (int i = 0; i < n; i++) {
					Point2D.Float h = g.getVertex(i);
					h.x = scale * h.x;
					h.y = ly - scale * (ly - h.y);
				}
				if (!bound.intersects(g.getBounds2D()))
					out = true;
			}
		}
		return out;
	}

	public ThermalBoundary getThermalBoundary() {
		return heatSolver.getBoundary();
	}

	public void setThermalBoundary(ThermalBoundary b) {
		heatSolver.setBoundary(b);
	}

	public MassBoundary getMassBoundary() {
		return fluidSolver.getBoundary();
	}

	public void setMassBoundary(MassBoundary b) {
		fluidSolver.setBoundary(b);
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

	// thermostats

	/** only one thermostat is needed to connect a thermometer and a power source */
	public Thermostat addThermostat(Thermometer t, Part p) {
		Iterator<Thermostat> i = thermostats.iterator();
		while (i.hasNext()) {
			Thermostat x = i.next();
			if (x.getThermometer() == t && x.getPowerSource() == p)
				return x;
		}
		Thermostat x = new Thermostat(t, p);
		thermostats.add(x);
		return x;
	}

	public void removeThermostat(Thermometer t, Part p) {
		if (thermostats.isEmpty())
			return;
		for (Iterator<Thermostat> i = thermostats.iterator(); i.hasNext();) {
			Thermostat x = i.next();
			if (x.getThermometer() == t && x.getPowerSource() == p)
				i.remove();
		}
	}

	public boolean isConnected(Thermometer t, Part p) {
		Iterator<Thermostat> i = thermostats.iterator();
		while (i.hasNext()) {
			Thermostat x = i.next();
			if (x.getThermometer() == t && x.getPowerSource() == p)
				return true;
		}
		return false;
	}

	public Thermostat getThermostat(Object o) {
		Iterator<Thermostat> i = thermostats.iterator();
		while (i.hasNext()) {
			Thermostat x = i.next();
			if (x.getThermometer() == o || x.getPowerSource() == o)
				return x;
		}
		return null;
	}

	public List<Thermostat> getThermostats() {
		return thermostats;
	}

	// thermometers

	public void addThermometer(Thermometer t) {
		thermometers.add(t);
	}

	public void addThermometer(float x, float y) {
		thermometers.add(new Thermometer(x, y));
	}

	public void addThermometer(float x, float y, String uid, String label, byte stencil) {
		Thermometer t = new Thermometer(x, y);
		t.setUid(uid);
		t.setLabel(label);
		t.setStencil(stencil);
		thermometers.add(t);
	}

	public void removeThermometer(Thermometer t) {
		thermometers.remove(t);
		if (!thermostats.isEmpty()) {
			Iterator<Thermostat> i = thermostats.iterator();
			while (i.hasNext()) {
				Thermostat x = i.next();
				if (x.getThermometer() == t)
					i.remove();
			}
		}
	}

	public List<Thermometer> getThermometers() {
		return thermometers;
	}

	public Thermometer getThermometer(String uid) {
		if (uid == null)
			return null;
		synchronized (thermometers) {
			for (Thermometer t : thermometers) {
				if (uid.equals(t.getUid()))
					return t;
			}
		}
		return null;
	}

	public Thermometer getThermometer(int i) {
		if (i < 0 || i >= thermometers.size())
			return null;
		return thermometers.get(i);
	}

	// anemometers

	public void addAnemometer(Anemometer a) {
		anemometers.add(a);
	}

	public void addAnemometer(float x, float y) {
		anemometers.add(new Anemometer(x, y));
	}

	public void addAnemometer(float x, float y, String uid, String label, byte stencil) {
		Anemometer a = new Anemometer(x, y);
		a.setUid(uid);
		a.setLabel(label);
		a.setStencil(stencil);
		anemometers.add(a);
	}

	public void removeAnemometer(Anemometer a) {
		anemometers.remove(a);
	}

	public List<Anemometer> getAnemometers() {
		return anemometers;
	}

	public Anemometer getAnemometer(String uid) {
		if (uid == null)
			return null;
		synchronized (anemometers) {
			for (Anemometer a : anemometers) {
				if (uid.equals(a.getUid()))
					return a;
			}
		}
		return null;
	}

	public Anemometer getAnemometer(int i) {
		if (i < 0 || i >= anemometers.size())
			return null;
		return anemometers.get(i);
	}

	/** Since the sensor data are erased, the index of step (and hence the clock) is also reset. */
	public void clearSensorData() {
		indexOfStep = 0;
		if (thermometers != null && !thermometers.isEmpty()) {
			synchronized (thermometers) {
				for (Thermometer t : thermometers) {
					t.clear();
				}
			}
		}
		if (anemometers != null && !anemometers.isEmpty()) {
			synchronized (anemometers) {
				for (Anemometer a : anemometers) {
					a.clear();
				}
			}
		}
	}

	public Part addRectangularPart(float x, float y, float w, float h) {
		Part p = new Part(new Rectangle2D.Float(x, y, w, h));
		addPart(p);
		return p;
	}

	public Part addRectangularPart(float x, float y, float w, float h, float t) {
		Part p = addRectangularPart(x, y, w, h);
		p.setTemperature(t);
		return p;
	}

	public Part addEllipticalPart(float x, float y, float a, float b) {
		Part p = new Part(new Ellipse2D.Float(x - 0.5f * a, y - 0.5f * b, a, b));
		addPart(p);
		return p;
	}

	public Part addEllipticalPart(float x, float y, float a, float b, float t) {
		Part p = addEllipticalPart(x, y, a, b);
		p.setTemperature(t);
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

	public Part addPolygonPart(float[] x, float[] y, float t) {
		Part p = addPolygonPart(x, y);
		p.setTemperature(t);
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

	/** Every manipulable has a UID. To avoid confusion, two objects of different types cannot have the same UID. */
	public boolean isUidUsed(String uid) {
		if (uid == null || uid.trim().equals(""))
			throw new IllegalArgumentException("UID cannot be null or an empty string.");
		synchronized (parts) {
			for (Part p : parts) {
				if (uid.equals(p.getUid()))
					return true;
			}
		}
		synchronized (thermometers) {
			for (Thermometer t : thermometers) {
				if (uid.equals(t.getUid()))
					return true;
			}
		}
		synchronized (anemometers) {
			for (Anemometer a : anemometers) {
				if (uid.equals(a.getUid()))
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
		if (!thermostats.isEmpty()) {
			Iterator<Thermostat> i = thermostats.iterator();
			while (i.hasNext()) {
				Thermostat x = i.next();
				if (x.getPowerSource() == p)
					i.remove();
			}
		}
		checkPartPower();
		checkPartRadiation();
	}

	public float getMaximumHeatCapacity() {
		return maximumHeatCapacity;
	}

	/** the part on the top sets the properties of a cell */
	public void refreshMaterialPropertyArrays() {
		Part p = null;
		int count = parts.size();
		float x, y, windSpeed;
		boolean initial = indexOfStep == 0;
		maximumHeatCapacity = backgroundDensity * backgroundSpecificHeat;
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
					ListIterator<Part> li = parts.listIterator(count);
					while (li.hasPrevious()) {
						p = li.previous();
						if (p.getShape().contains(x, y)) {
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
		int count;
		for (int i = 0; i < nx; i++) {
			x = i * deltaX;
			for (int j = 0; j < ny; j++) {
				y = j * deltaY;
				q[i][j] = 0;
				if (hasPartPower) {
					count = 0;
					synchronized (parts) {
						for (Part p : parts) {
							if (p.getPower() != 0 && p.getPowerSwitch() && p.getShape().contains(x, y)) {
								q[i][j] += p.getPower();
								count++;
							}
						}
					}
					if (count > 0)
						q[i][j] /= count;
				}
			}
		}
	}

	public void refreshTemperatureBoundaryArray() {
		float x, y;
		int count;
		for (int i = 0; i < nx; i++) {
			x = i * deltaX;
			for (int j = 0; j < ny; j++) {
				y = j * deltaY;
				tb[i][j] = 0;
				count = 0;
				synchronized (parts) {
					for (Part p : parts) {
						if (p.getConstantTemperature() && p.getShape().contains(x, y)) {
							tb[i][j] += p.getTemperature();
							count++;
						}
					}
				}
				if (count > 0) {
					tb[i][j] /= count;
				} else {
					tb[i][j] = Float.NaN;
				}
			}
		}
	}

	/** get the total thermal energy of the system */
	public float getThermalEnergy() {
		float energy = 0;
		for (int i = 1; i < nx - 1; i++) { // excluding the border cells to ensure the conservation of energy
			for (int j = 1; j < ny - 1; j++) {
				energy += t[i][j] * density[i][j] * specificHeat[i][j];
			}
		}
		return energy * deltaX * deltaY;
	}

	/** get the total thermal energy stored in this part */
	public float getThermalEnergy(Part p) {
		if (p == null)
			return 0;
		float x, y;
		float energy = 0;
		for (int i = 0; i < nx; i++) {
			x = i * deltaX;
			for (int j = 0; j < ny; j++) {
				y = j * deltaY;
				if (p.getShape().contains(x, y)) { // no overlap of parts will be allowed
					energy += t[i][j] * density[i][j] * specificHeat[i][j];
				}
			}
		}
		return energy * deltaX * deltaY;
	}

	/** get the thermal energy stored in the cell at the given point. If the point is out of bound, return -1 (any impossible value to indicate error) */
	public float getThermalEnergyAt(float x, float y) {
		int i = Math.round(x / deltaX);
		if (i < 0 || i >= nx)
			return -1;
		int j = Math.round(y / deltaY);
		if (j < 0 || j >= ny)
			return -1;
		return t[i][j] * density[i][j] * specificHeat[i][j] * deltaX * deltaY;
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
		anemometers.clear();
		thermometers.clear();
		thermostats.clear();
		clouds.clear();
		trees.clear();
		maximumHeatCapacity = -1;
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
		if (parts == null || parts.isEmpty()) {
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					t[i][j] = backgroundTemperature;
				}
			}
		} else {
			float x, y;
			int count;
			for (int i = 0; i < nx; i++) {
				x = i * deltaX;
				for (int j = 0; j < ny; j++) {
					y = j * deltaY;
					count = 0;
					t[i][j] = 0;
					synchronized (parts) {
						for (Part p : parts) { // a cell gets the average temperature from the overlapping parts
							if (p.getShape().contains(x, y)) {
								count++;
								t[i][j] += p.getTemperature();
							}
						}
					}
					if (count > 0) {
						t[i][j] /= count;
					} else {
						t[i][j] = backgroundTemperature;
					}
				}
			}
		}
		clearSensorData();
	}

	public void run() {
		checkPartPower();
		checkPartRadiation();
		refreshPowerArray();
		if (!running) {
			running = true;
			while (running) {
				nextStep();
				if (tasks != null)
					tasks.run();
			}
			if (notifyReset) {
				indexOfStep = 0;
				reallyReset();
				notifyReset = false;
				// call view.repaint() to get rid of the residual pixels that are still calculated in nextStep()
				notifyManipulationListeners(ManipulationEvent.REPAINT);
			}
		}
	}

	public void stop() {
		running = false;
	}

	public boolean isRunning() {
		return running;
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
		for (Part p : parts)
			p.setPowerSwitch(true);
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
		if (radiative) {
			if (indexOfStep % photonEmissionInterval == 0) {
				refreshPowerArray();
				if (sunny)
					raySolver.sunShine(photons, parts);
				raySolver.radiate(this);
			}
			raySolver.solve(this);
		}
		if (convective)
			fluidSolver.solve(u, v);
		heatSolver.solve(convective, t);
		if (!clouds.isEmpty()) {
			synchronized (clouds) {
				for (Cloud c : clouds)
					c.move(heatSolver.getTimeStep(), lx);
			}
		}
		indexOfStep++;
	}

	public float getTime() {
		return indexOfStep * heatSolver.getTimeStep();
	}

	public int getIndexOfStep() {
		return indexOfStep;
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
		if (i < 0)
			i = 0;
		int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
		if (j < 0)
			j = 0;
		return t[i][j];
	}

	public void setTemperatureAt(float x, float y, float temperature) {
		int i = Math.min(t.length - 1, Math.round(x / deltaX));
		if (i < 0)
			return;
		int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
		if (j < 0)
			return;
		t[i][j] = temperature;
	}

	public void changeTemperatureAt(float x, float y, float increment) {
		int i = Math.min(t.length - 1, Math.round(x / deltaX));
		if (i < 0)
			return;
		int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
		if (j < 0)
			return;
		t[i][j] += increment;
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

	public float[] getHeatFluxAt(float x, float y) {
		int i = Math.min(t.length - 2, Math.round(x / deltaX));
		if (i < 1)
			i = 1;
		int j = Math.min(t[0].length - 2, Math.round(y / deltaY));
		if (j < 1)
			j = 1;
		float fx = -conductivity[i][j] * (t[i + 1][j] - t[i - 1][j]) / (2 * deltaX);
		float fy = -conductivity[i][j] * (t[i][j + 1] - t[i][j - 1]) / (2 * deltaY);
		return new float[] { fx, fy };
	}

	public float[][] getXVelocity() {
		return u;
	}

	public float[][] getYVelocity() {
		return v;
	}

	public float[] getVelocityAt(float x, float y) {
		int i = Math.min(t.length - 1, Math.round(x / deltaX));
		if (i < 0)
			i = 0;
		int j = Math.min(t[0].length - 1, Math.round(y / deltaY));
		if (j < 0)
			j = 0;
		return new float[] { u[i][j], v[i][j] };
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

	public void takeMeasurement() {
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

	// if controllers run every step, they could slow down significantly
	public void control() {
		boolean refresh = false;
		for (Thermostat x : thermostats) {
			if (x.onoff())
				refresh = true;
		}
		if (refresh)
			refreshPowerArray();
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
