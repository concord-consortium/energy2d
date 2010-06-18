/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.concord.energy2d.math.Polygon2D;

/**
 * Default properties set to be that of polystyrene. See
 * http://en.wikipedia.org/wiki/Polystyrene
 * 
 * @author Charles Xie
 * 
 */
public class Part extends Manipulable {

	// Stefan's constant unit J/(s*m^2*K^-4)
	private final static float STEFAN_CONSTANT = 0.0000000567f;

	/*
	 * constant power input/output: positive = source, negative = sink, zero =
	 * off. Unit: W/m^3
	 */
	private float power;

	/*
	 * a fixed or initial temperature for this part. When a temperature value is
	 * imposed, this part becomes an internal boundary. Set it to Float.NaN to
	 * disable this and allow heat to flow through this part.
	 */
	private float temperature = java.lang.Float.NaN;

	/*
	 * when this flag is true, temperature is maintained at the set value.
	 * Otherwise, it will be just the initial value that defines the heat energy
	 * this part initially possesses.
	 */
	private boolean constantTemperature = true;

	/*
	 * the thermal conductivity: Fourier's Law, the flow of heat energy
	 * 
	 * q = - k dT/dx
	 * 
	 * Unit: W/(mK). The default value is water's.
	 */
	private float thermalConductivity = 0.08f;

	/*
	 * the specific heat capacity: J/(kgK).
	 */
	private float specificHeat = 1300f;

	// density kg/m^3. The default value is foam's.
	private float density = 25f;

	private float absorption = 1;

	private float transmission;

	private float reflection;

	private float emissivity;

	private float windSpeed;
	private float windAngle;

	private float unitSurfaceArea = 100;

	private static int polygonize = 50;
	private static float radiatorSpacing = .5f;
	private static float MINIMUM_RADIATING_TEMPERATUE = 20;
	private final static float SIN30 = (float) Math.sin(Math.PI / 6);
	private final static float COS30 = (float) Math.cos(Math.PI / 6);
	private final static float SIN60 = (float) Math.sin(Math.PI / 3);
	private final static float COS60 = (float) Math.cos(Math.PI / 3);

	public Part(Shape shape) {
		super(shape);
	}

	public Part duplicate(float x, float y) {
		Shape s = getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			s = new Rectangle2D.Float(x - 0.5f * r.width, y - 0.5f * r.height,
					r.width, r.height);
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) s;
			s = new Ellipse2D.Float(x - 0.5f * e.width, y - 0.5f * e.height,
					e.width, e.height);
		} else if (s instanceof Area) {
			s = new Area(s);
			Rectangle2D r = s.getBounds2D();
			float dx = x - (float) r.getCenterX();
			float dy = y - (float) r.getCenterY();
			((Area) s).transform(AffineTransform.getTranslateInstance(dx, dy));
		} else if (s instanceof Polygon2D) {
			s = ((Polygon2D) s).duplicate();
			Rectangle2D r = s.getBounds2D();
			float dx = x - (float) r.getCenterX();
			float dy = y - (float) r.getCenterY();
			((Polygon2D) s).translateBy(dx, dy);
		}
		Part p = new Part(s);
		p.setFilled(isFilled());
		p.setColor(getColor());
		p.power = power;
		p.temperature = temperature;
		p.constantTemperature = constantTemperature;
		p.thermalConductivity = thermalConductivity;
		p.specificHeat = specificHeat;
		p.density = density;
		p.absorption = absorption;
		p.reflection = reflection;
		p.transmission = transmission;
		p.emissivity = emissivity;
		p.windAngle = windAngle;
		p.windSpeed = windSpeed;
		return p;
	}

	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}

	public float getWindSpeed() {
		return windSpeed;
	}

	public void setWindAngle(float windAngle) {
		this.windAngle = windAngle;
	}

	public float getWindAngle() {
		return windAngle;
	}

	public void setEmissivity(float emissivity) {
		this.emissivity = emissivity;
	}

	public float getEmissivity() {
		return emissivity;
	}

	public void setTransmission(float transmission) {
		this.transmission = transmission;
	}

	public float getTransmission() {
		return transmission;
	}

	public void setAbsorption(float absorption) {
		this.absorption = absorption;
	}

	public float getAbsorption() {
		return absorption;
	}

	public void setReflection(float reflection) {
		this.reflection = reflection;
	}

	public float getReflection() {
		return reflection;
	}

	public void setConstantTemperature(boolean b) {
		constantTemperature = b;
	}

	public boolean getConstantTemperature() {
		return constantTemperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
		if (this.temperature <= -273)
			this.temperature = java.lang.Float.NaN;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setPower(float power) {
		this.power = power;
	}

	public float getPower() {
		return power;
	}

	public void setThermalConductivity(float thermalConductivity) {
		this.thermalConductivity = thermalConductivity;
	}

	public float getThermalConductivity() {
		return thermalConductivity;
	}

	public void setSpecificHeat(float specificHeat) {
		this.specificHeat = specificHeat;
	}

	public float getSpecificHeat() {
		return specificHeat;
	}

	public void setDensity(float density) {
		this.density = density;
	}

	public float getDensity() {
		return density;
	}

	boolean absorb(Photon p) {
		return getShape().contains(p.getX(), p.getY());
	}

	void radiate(Model2D model) {

		if (emissivity == 0)
			return;

		Shape shape = getShape();
		Line2D.Float line = new Line2D.Float();

		if (shape instanceof Rectangle2D.Float) {
			// must follow the clockwise direction in setting lines
			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			// north
			line.setLine(r.x, r.y, r.x + r.width, r.y);
			radiate(model, line);
			// east
			line.setLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
			radiate(model, line);
			// south
			line.setLine(r.x + r.width, r.y + r.height, r.x, r.y + r.height);
			radiate(model, line);
			// west
			line.setLine(r.x, r.y + r.height, r.x, r.y);
			radiate(model, line);
		}

		else if (shape instanceof Polygon2D) {
			Polygon2D r = (Polygon2D) shape;
			int n = r.getVertexCount();
			// must follow the clockwise direction in setting lines
			Point2D.Float v1, v2;
			for (int i = 0; i < n - 1; i++) {
				v1 = r.getVertex(i);
				v2 = r.getVertex(i + 1);
				line.setLine(v1, v2);
				radiate(model, line);
			}
			v1 = r.getVertex(n - 1);
			v2 = r.getVertex(0);
			line.setLine(v1, v2);
			radiate(model, line);
		}

		else if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) shape;
			float a = e.width * 0.5f;
			float b = e.height * 0.5f;
			float x = e.x + a;
			float y = e.y + b;
			float[] vx = new float[polygonize];
			float[] vy = new float[polygonize];
			float theta;
			float delta = (float) (2 * Math.PI / polygonize);
			for (int i = 0; i < polygonize; i++) {
				theta = delta * i;
				vx[i] = (float) (x + a * Math.cos(theta));
				vy[i] = (float) (y + b * Math.sin(theta));
			}
			for (int i = 0; i < polygonize - 1; i++) {
				line.setLine(vx[i], vy[i], vx[i + 1], vy[i + 1]);
				radiate(model, line);
			}
			line.setLine(vx[polygonize - 1], vy[polygonize - 1], vx[0], vy[0]);
			radiate(model, line);
		}

	}

	private float getIrradiance(float temperature) {
		if (emissivity == 0)
			return 0;
		float t2 = 273 + temperature;
		t2 *= t2;
		return emissivity * STEFAN_CONSTANT * unitSurfaceArea * t2 * t2;
	}

	private void radiate(Model2D model, Line2D.Float line) {
		if (emissivity == 0)
			return;
		float length = (float) Math.hypot(line.x1 - line.x2, line.y1 - line.y2);
		float cos = (line.x2 - line.x1) / length;
		float sin = (line.y2 - line.y1) / length;
		int n = Math.max(1, Math.round(length / radiatorSpacing));
		float x, y;
		Photon p;
		float d;
		float vx = model.getRaySpeed() * sin;
		float vy = -model.getRaySpeed() * cos;
		if (n == 1) {
			d = 0.5f * length;
			x = line.x1 + d * cos;
			y = line.y1 + d * sin;
			d = model.getAverageTemperatureAt(x, y);
			if (d > MINIMUM_RADIATING_TEMPERATUE) {
				d = model.getTemperatureAt(x, y);
				p = new Photon(x, y, getIrradiance(d), model.getRaySpeed());
				p.setVx(vx);
				p.setVy(vy);
				model.addPhoton(p);
				model.setTemperatureAt(x, y, d - p.getEnergy()
						/ getSpecificHeat());
			}
		} else {
			float[] vxi = new float[4], vyi = new float[4];
			vxi[0] = vx * COS30 - vy * SIN30;
			vyi[0] = vx * SIN30 + vy * COS30;
			vxi[1] = vy * SIN30 + vx * COS30;
			vyi[1] = vy * COS30 - vx * SIN30;
			vxi[2] = vx * COS60 - vy * SIN60;
			vyi[2] = vx * SIN60 + vy * COS60;
			vxi[3] = vy * SIN60 + vx * COS60;
			vyi[3] = vy * COS60 - vx * SIN60;
			int nray = 1 + vxi.length;
			float ir;
			for (int i = 0; i < n; i++) {
				d = (i + 0.5f) * radiatorSpacing;
				x = line.x1 + d * cos;
				y = line.y1 + d * sin;
				d = model.getAverageTemperatureAt(x, y);
				ir = getIrradiance(d) / nray;
				if (d > MINIMUM_RADIATING_TEMPERATUE) {
					p = new Photon(x, y, ir, model.getRaySpeed());
					p.setVx(vx);
					p.setVy(vy);
					model.addPhoton(p);
					for (int k = 0; k < nray - 1; k++) {
						p = new Photon(x, y, ir, model.getRaySpeed());
						p.setVx(vxi[k]);
						p.setVy(vyi[k]);
						model.addPhoton(p);
					}
					model.changeAverageTemperatureAt(x, y, -ir * nray
							/ getSpecificHeat());
				}
			}
		}
	}

	boolean reflect(Photon p, float timeStep) {

		Shape shape = getShape();

		if (shape instanceof Rectangle2D.Float) {

			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			float x0 = r.x;
			float y0 = r.y;
			float x1 = r.x + r.width;
			float y1 = r.y + r.height;
			if (p.getX() < x1 && p.getX() > x0 && p.getY() < y1
					&& p.getY() > y0) {
				float dx = p.getVx() * timeStep;
				if (p.getX() - dx < x0) {
					p.setVx(-Math.abs(p.getVx()));
				} else if (p.getX() - dx > x1) {
					p.setVx(Math.abs(p.getVx()));
				}
				float dy = p.getVy() * timeStep;
				if (p.getY() - dy < y0) {
					p.setVy(-Math.abs(p.getVy()));
				} else if (p.getY() - dy > y1) {
					p.setVy(Math.abs(p.getVy()));
				}
				return true;
			}

		} else if (shape instanceof Polygon2D) {

			Polygon2D r = (Polygon2D) shape;
			if (r.contains(p.getX(), p.getY())) {
				reflect(r, p, timeStep);
				return true;
			}

		} else if (shape instanceof Ellipse2D.Float) {

			Ellipse2D.Float e = (Ellipse2D.Float) shape;
			if (e.contains(p.getX(), p.getY())) {
				reflect(e, p, timeStep);
				return true;
			}

		}

		return false;

	}

	private static void reflect(Ellipse2D.Float e, Photon p, float timeStep) {
		float a = e.width * 0.5f;
		float b = e.height * 0.5f;
		float x = e.x + a;
		float y = e.y + b;
		float[] vx = new float[polygonize];
		float[] vy = new float[polygonize];
		float theta;
		float delta = (float) (2 * Math.PI / polygonize);
		for (int i = 0; i < polygonize; i++) {
			theta = delta * i;
			vx[i] = (float) (x + a * Math.cos(theta));
			vy[i] = (float) (y + b * Math.sin(theta));
		}
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < polygonize - 1; i++) {
			line.setLine(vx[i], vy[i], vx[i + 1], vy[i + 1]);
			if (reflectFromLine(p, line, timeStep))
				return;
		}
		line.setLine(vx[polygonize - 1], vy[polygonize - 1], vx[0], vy[0]);
		reflectFromLine(p, line, timeStep);
	}

	private static void reflect(Polygon2D r, Photon p, float timeStep) {
		int n = r.getVertexCount();
		Point2D.Float v1, v2;
		Line2D.Float line = new Line2D.Float();
		for (int i = 0; i < n - 1; i++) {
			v1 = r.getVertex(i);
			v2 = r.getVertex(i + 1);
			line.setLine(v1, v2);
			if (reflectFromLine(p, line, timeStep))
				return;
		}
		v1 = r.getVertex(n - 1);
		v2 = r.getVertex(0);
		line.setLine(v1, v2);
		reflectFromLine(p, line, timeStep);
	}

	private static boolean reflectFromLine(Photon p, Line2D.Float line,
			float timeStep) {
		float x1 = p.getX();
		float y1 = p.getY();
		float x2 = p.getX() - p.getVx() * timeStep;
		float y2 = p.getY() - p.getVy() * timeStep;
		if (line.intersectsLine(x1, y1, x2, y2)) {
			x1 = line.x1;
			y1 = line.y1;
			x2 = line.x2;
			y2 = line.y2;
			float r12 = 1.0f / (float) Math.hypot(x1 - x2, y1 - y2);
			float sin = (y2 - y1) * r12;
			float cos = (x2 - x1) * r12;
			// velocity component parallel to the line
			float u = p.getVx() * cos + p.getVy() * sin;
			// velocity component perpendicular to the line
			float w = p.getVy() * cos - p.getVx() * sin;
			p.setVx(u * cos + w * sin);
			p.setVy(u * sin - w * cos);
			return true;
		}
		return false;
	}

	public String toXml() {
		String xml = "<";
		if (getShape() instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) getShape();
			xml += "rectangle";
			xml += " x=\"" + r.x + "\"";
			xml += " y=\"" + r.y + "\"";
			xml += " width=\"" + r.width + "\"";
			xml += " height=\"" + r.height + "\"";
		}
		xml += " thermal_conductivity=\"" + thermalConductivity + "\"";
		xml += " specific_heat=\"" + specificHeat + "\"";
		xml += " density=\"" + density + "\"";
		if (!java.lang.Float.isNaN(temperature))
			xml += " temperature=\"" + temperature + "\"";
		if (!constantTemperature)
			xml += " constant_temperature=\"false\"";
		if (power != 0)
			xml += " power=\"" + power + "\"";
		if (!getColor().equals(Color.gray))
			xml += " color=\""
					+ Integer.toHexString(0x00ffffff & getColor().getRGB())
					+ "\"";
		if (!isVisible())
			xml += " visible=\"false\"";
		if (!isDraggable())
			xml += " draggable=\"false\"";
		xml += "/>\n";
		return xml;
	}

	@Override
	public String toString() {
		return toXml();
	}

}
