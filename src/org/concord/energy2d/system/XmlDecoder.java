package org.concord.energy2d.system;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.concord.energy2d.model.Boundary;
import org.concord.energy2d.model.Constants;
import org.concord.energy2d.model.DirichletHeatBoundary;
import org.concord.energy2d.model.HeatBoundary;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.NeumannHeatBoundary;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.util.ColorFill;
import org.concord.energy2d.util.Scripter;
import org.concord.energy2d.util.Texture;
import org.concord.energy2d.view.TextBox;
import org.concord.energy2d.view.View2D;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Charles Xie
 * 
 */
class XmlDecoder extends DefaultHandler {

	private System2D box;
	private String str;

	// model properties
	private float modelWidth = 10;
	private float modelHeight = 10;
	private float timeStep = 1;
	private int measurementInterval = 500;
	private int viewUpdateInterval = 100;
	private float stopTime = -1;
	private boolean sunny;
	private float sunAngle = (float) Math.PI * 0.5f;
	private float solarPowerDensity = 2000;
	private int solarRayCount = 24;
	private float solarRaySpeed = 0.1f;
	private int photonEmissionInterval = 20;
	private boolean convective = true;
	private float zHeatDiffusivity;
	private float backgroundConductivity = Constants.AIR_THERMAL_CONDUCTIVITY;
	private float backgroundDensity = Constants.AIR_DENSITY;
	private float backgroundSpecificHeat = Constants.AIR_SPECIFIC_HEAT;
	private float backgroundViscosity = Constants.AIR_VISCOSITY;
	private float backgroundTemperature;
	private float thermalBuoyancy;
	private byte buoyancyApproximation = Model2D.BUOYANCY_AVERAGE_COLUMN;

	// view properties
	private boolean ruler;
	private boolean grid;
	private boolean isotherm;
	private boolean streamline;
	private boolean colorPalette;
	private byte colorPaletteType = View2D.RAINBOW;
	private boolean brand = true;
	private byte heatMapType = View2D.HEATMAP_TEMPERATURE;
	private float colorPaletteX, colorPaletteY, colorPaletteW, colorPaletteH;
	private int gridSize = 10;
	private boolean velocity;
	private boolean heatFluxArrows;
	private boolean heatFluxLines;
	private boolean graphOn;
	private boolean clock = true;
	private boolean smooth = true;
	private float minimumTemperature;
	private float maximumTemperature = 40;
	private String graphXLabel, graphYLabel;

	// part properties
	private float partThermalConductivity = Float.NaN;
	private float partSpecificHeat = Float.NaN;
	private float partDensity = Float.NaN;
	private float partEmissivity = Float.NaN;
	private float partAbsorption = Float.NaN;
	private float partReflection = Float.NaN;
	private float partTransmission = Float.NaN;
	private float partTemperature = Float.NaN;
	private float partWindSpeed;
	private float partWindAngle;
	private boolean partConstantTemperature = false;
	private float partPower = Float.NaN;
	private boolean partVisible = true;
	private boolean partDraggable = true;
	private Color partColor = Color.gray;
	private byte partTextureStyle;
	private int partTextureWidth = 10;
	private int partTextureHeight = 10;
	private Color partTextureForeground = Color.black;
	private Color partTextureBackground = Color.white;
	private boolean partFilled = true;
	private String partUid;
	private String partLabel;
	private Part part;

	XmlDecoder(System2D box) {
		this.box = box;
	}

	public void startDocument() {
	}

	public void endDocument() {

		box.model.setLx(modelWidth);
		box.model.setLy(modelHeight);
		box.view.setArea(0, modelWidth, 0, modelHeight);
		box.model.setTimeStep(timeStep);
		box.model.setMeasurementInterval(measurementInterval);
		box.model.setViewUpdateInterval(viewUpdateInterval);
		box.model.setStopTime(stopTime);
		box.model.setSunny(sunny);
		box.model.setSunAngle(sunAngle);
		box.model.setSolarPowerDensity(solarPowerDensity);
		box.model.setSolarRayCount(solarRayCount);
		box.model.setSolarRaySpeed(solarRaySpeed);
		box.model.setPhotonEmissionInterval(photonEmissionInterval);
		box.model.setConvective(convective);
		box.model.setZHeatDiffusivity(zHeatDiffusivity);
		box.model.setBackgroundConductivity(backgroundConductivity);
		box.model.setBackgroundDensity(backgroundDensity);
		box.model.setBackgroundSpecificHeat(backgroundSpecificHeat);
		box.model.setBackgroundTemperature(backgroundTemperature);
		box.model.setBackgroundViscosity(backgroundViscosity);
		box.model.setThermalBuoyancy(thermalBuoyancy);
		box.model.setBuoyancyApproximation(buoyancyApproximation);

		box.view.setRulerOn(ruler);
		box.view.setGridOn(grid);
		box.view.setGridSize(gridSize);
		box.view.setIsothermOn(isotherm);
		box.view.setStreamlineOn(streamline);
		box.view.setVelocityOn(velocity);
		box.view.setHeatFluxArrowsOn(heatFluxArrows);
		box.view.setHeatFluxLinesOn(heatFluxLines);
		box.view.setColorPaletteOn(colorPalette);
		box.view.setColorPaletteType(colorPaletteType);
		box.view.setFrankOn(brand);
		box.view.setHeatMapType(heatMapType);
		float xColorPalette = colorPaletteX > 1 ? colorPaletteX / box.view.getWidth() : colorPaletteX;
		float yColorPalette = colorPaletteY > 1 ? colorPaletteY / box.view.getHeight() : colorPaletteY;
		float wColorPalette = colorPaletteW > 1 ? colorPaletteW / box.view.getWidth() : colorPaletteW;
		float hColorPalette = colorPaletteH > 1 ? colorPaletteH / box.view.getHeight() : colorPaletteH;
		box.view.setColorPaletteRectangle(xColorPalette, yColorPalette, wColorPalette, hColorPalette);
		box.view.setMinimumTemperature(minimumTemperature);
		box.view.setMaximumTemperature(maximumTemperature);
		box.view.setClockOn(clock);
		box.view.setSmooth(smooth);
		box.view.setGraphOn(graphOn);
		if (graphXLabel != null)
			box.view.setGraphXLabel(graphXLabel);
		if (graphYLabel != null)
			box.view.setGraphYLabel(graphXLabel);

		// since we don't know the width and height of the model
		// until now, we have to fix the locations and the sizes of
		// the thermometers, since they are relative to the size
		// of the model.
		List<Thermometer> thermometers = box.model.getThermometers();
		if (thermometers != null) {
			synchronized (thermometers) {
				for (Thermometer t : thermometers) {
					Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
					r.width = 0.025f * modelWidth;
					r.height = 0.05f * modelHeight;
					r.x = r.x - 0.5f * r.width;
					r.y = r.y - 0.5f * r.height;
				}
			}
		}

		box.model.refreshPowerArray();
		box.model.refreshTemperatureBoundaryArray();
		box.model.refreshMaterialPropertyArrays();
		box.model.setInitialTemperature();
		box.view.repaint();

		resetGlobalVariables();

	}

	public void startElement(String uri, String localName, String qName, Attributes attrib) {

		String attribName, attribValue;

		if (qName == "rectangle") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w) && !Float.isNaN(h))
					part = box.model.addRectangularPart(x, y, w, h);
			}
		} else if (qName == "ellipse") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, a = Float.NaN, b = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "a") {
						a = Float.parseFloat(attribValue);
					} else if (attribName == "b") {
						b = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(a) && !Float.isNaN(b))
					part = box.model.addEllipticalPart(x, y, a, b);
			}
		} else if (qName == "ring") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN, inner = Float.NaN, outer = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "inner") {
						inner = Float.parseFloat(attribValue);
					} else if (attribName == "outer") {
						outer = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(inner) && !Float.isNaN(outer))
					part = box.model.addRingPart(x, y, inner, outer);
			}
		} else if (qName == "polygon") {
			if (attrib != null) {
				int count = -1;
				String vertices = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "count") {
						count = Integer.parseInt(attribValue);
					} else if (attribName == "vertices") {
						vertices = attribValue;
					}
				}
				if (count > 0 && vertices != null) {
					float[] v = Scripter.parseArray(count * 2, vertices);
					float[] x = new float[count];
					float[] y = new float[count];
					for (int i = 0; i < count; i++) {
						x[i] = v[2 * i];
						y[i] = v[2 * i + 1];
					}
					part = box.model.addPolygonPart(x, y);
				}
			}
		} else if (qName == "temperature_at_border") {
			if (attrib != null) {
				float left = Float.NaN, right = Float.NaN, upper = Float.NaN, lower = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "left") {
						left = Float.parseFloat(attribValue);
					} else if (attribName == "right") {
						right = Float.parseFloat(attribValue);
					} else if (attribName == "upper") {
						upper = Float.parseFloat(attribValue);
					} else if (attribName == "lower") {
						lower = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(left) && !Float.isNaN(right) && !Float.isNaN(upper) && !Float.isNaN(lower)) {
					DirichletHeatBoundary b = null;
					HeatBoundary boundary = box.model.getHeatBoundary();
					if (boundary instanceof DirichletHeatBoundary) {
						b = (DirichletHeatBoundary) boundary;
					} else {
						b = new DirichletHeatBoundary();
						box.model.setHeatBoundary(b);
					}
					b.setTemperatureAtBorder(Boundary.UPPER, upper);
					b.setTemperatureAtBorder(Boundary.RIGHT, right);
					b.setTemperatureAtBorder(Boundary.LOWER, lower);
					b.setTemperatureAtBorder(Boundary.LEFT, left);
				}
			}
		} else if (qName == "flux_at_border") {
			if (attrib != null) {
				float left = Float.NaN, right = Float.NaN, upper = Float.NaN, lower = Float.NaN;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "left") {
						left = Float.parseFloat(attribValue);
					} else if (attribName == "right") {
						right = Float.parseFloat(attribValue);
					} else if (attribName == "upper") {
						upper = Float.parseFloat(attribValue);
					} else if (attribName == "lower") {
						lower = Float.parseFloat(attribValue);
					}
				}
				if (!Float.isNaN(left) && !Float.isNaN(right) && !Float.isNaN(upper) && !Float.isNaN(lower)) {
					NeumannHeatBoundary b = null;
					HeatBoundary boundary = box.model.getHeatBoundary();
					if (boundary instanceof NeumannHeatBoundary) {
						b = (NeumannHeatBoundary) boundary;
					} else {
						b = new NeumannHeatBoundary();
						box.model.setHeatBoundary(b);
					}
					b.setFluxAtBorder(Boundary.UPPER, upper);
					b.setFluxAtBorder(Boundary.RIGHT, right);
					b.setFluxAtBorder(Boundary.LOWER, lower);
					b.setFluxAtBorder(Boundary.LEFT, left);
				}
			}
		} else if (qName == "thermometer") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				String label = null;
				byte stencil = Thermometer.ONE_POINT;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "stencil") {
						stencil = Byte.parseByte(attribValue);
					} else if (attribName == "label") {
						label = attribValue;
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y))
					box.model.addThermometer(x, y, label, stencil);
			}
		} else if (qName == "text") {
			if (attrib != null) {
				float x = Float.NaN, y = Float.NaN;
				int size = 14, style = Font.PLAIN;
				String str = null, name = null;
				Color color = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "string") {
						str = attribValue;
					} else if (attribName == "size") {
						size = Integer.parseInt(attribValue);
					} else if (attribName == "name") {
						name = attribValue;
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				if (!Float.isNaN(x) && !Float.isNaN(y)) {
					TextBox t = box.view.addText(str, x, y);
					t.setSize(size);
					t.setStyle(style);
					t.setName(name);
					t.setColor(color);
					box.view.repaint();
				}
			}
		}

	}

	public void endElement(String uri, String localName, String qName) {

		if (qName == "model_width") {
			modelWidth = Float.parseFloat(str);
		} else if (qName == "model_height") {
			modelHeight = Float.parseFloat(str);
		} else if (qName == "timestep") {
			timeStep = Float.parseFloat(str);
		} else if (qName == "measurement_interval") {
			measurementInterval = Integer.parseInt(str);
		} else if (qName == "viewupdate_interval") {
			viewUpdateInterval = Integer.parseInt(str);
		} else if (qName == "stoptime") {
			stopTime = Float.parseFloat(str);
		} else if (qName == "sunny") {
			sunny = Boolean.parseBoolean(str);
		} else if (qName == "sun_angle") {
			sunAngle = Float.parseFloat(str);
		} else if (qName == "solar_power_density") {
			solarPowerDensity = Float.parseFloat(str);
		} else if (qName == "solar_ray_count") {
			solarRayCount = Integer.parseInt(str);
		} else if (qName == "solar_ray_speed") {
			solarRaySpeed = Float.parseFloat(str);
		} else if (qName == "photon_emission_interval") {
			photonEmissionInterval = Integer.parseInt(str);
		} else if (qName == "z_heat_diffusivity") {
			zHeatDiffusivity = Float.parseFloat(str);
		} else if (qName == "convective") {
			convective = Boolean.parseBoolean(str);
		} else if (qName == "background_conductivity") {
			backgroundConductivity = Float.parseFloat(str);
		} else if (qName == "background_density") {
			backgroundDensity = Float.parseFloat(str);
		} else if (qName == "background_specific_heat") {
			backgroundSpecificHeat = Float.parseFloat(str);
		} else if (qName == "background_temperature") {
			backgroundTemperature = Float.parseFloat(str);
		} else if (qName == "background_viscosity") {
			backgroundViscosity = Float.parseFloat(str);
		} else if (qName == "thermal_buoyancy") {
			thermalBuoyancy = Float.parseFloat(str);
		} else if (qName == "buoyancy_approximation") {
			buoyancyApproximation = Byte.parseByte(str);
		} else if (qName == "minimum_temperature") {
			minimumTemperature = Float.parseFloat(str);
		} else if (qName == "maximum_temperature") {
			maximumTemperature = Float.parseFloat(str);
		} else if (qName == "ruler") {
			ruler = Boolean.parseBoolean(str);
		} else if (qName == "isotherm") {
			isotherm = Boolean.parseBoolean(str);
		} else if (qName == "streamline") {
			streamline = Boolean.parseBoolean(str);
		} else if (qName == "velocity") {
			velocity = Boolean.parseBoolean(str);
		} else if (qName == "heat_flux_arrow") {
			heatFluxArrows = Boolean.parseBoolean(str);
		} else if (qName == "heat_flux_line") {
			heatFluxLines = Boolean.parseBoolean(str);
		} else if (qName == "grid") {
			grid = Boolean.parseBoolean(str);
		} else if (qName == "grid_size") {
			gridSize = Integer.parseInt(str);
		} else if (qName == "color_palette") {
			colorPalette = Boolean.parseBoolean(str);
		} else if (qName == "color_palette_type") {
			colorPaletteType = Byte.parseByte(str);
		} else if (qName == "brand") {
			brand = Boolean.parseBoolean(str);
		} else if (qName == "heat_map") {
			heatMapType = Byte.parseByte(str);
		} else if (qName == "color_palette_x") {
			colorPaletteX = Float.parseFloat(str);
		} else if (qName == "color_palette_y") {
			colorPaletteY = Float.parseFloat(str);
		} else if (qName == "color_palette_w") {
			colorPaletteW = Float.parseFloat(str);
		} else if (qName == "color_palette_h") {
			colorPaletteH = Float.parseFloat(str);
		} else if (qName == "clock") {
			clock = Boolean.parseBoolean(str);
		} else if (qName == "smooth") {
			smooth = Boolean.parseBoolean(str);
		} else if (qName == "graph") {
			graphOn = Boolean.parseBoolean(str);
		} else if (qName == "graph_xlabel") {
			graphXLabel = str;
		} else if (qName == "graph_ylabel") {
			graphYLabel = str;
		} else if (qName == "uid") {
			partUid = str;
		} else if (qName == "label") {
			partLabel = str;
		} else if (qName == "thermal_conductivity") {
			partThermalConductivity = Float.parseFloat(str);
		} else if (qName == "specific_heat") {
			partSpecificHeat = Float.parseFloat(str);
		} else if (qName == "density") {
			partDensity = Float.parseFloat(str);
		} else if (qName == "emissivity") {
			partEmissivity = Float.parseFloat(str);
		} else if (qName == "absorption") {
			partAbsorption = Float.parseFloat(str);
		} else if (qName == "reflection") {
			partReflection = Float.parseFloat(str);
		} else if (qName == "transmission") {
			partTransmission = Float.parseFloat(str);
		} else if (qName == "temperature") {
			partTemperature = Float.parseFloat(str);
		} else if (qName == "constant_temperature") {
			partConstantTemperature = Boolean.parseBoolean(str);
		} else if (qName == "power") {
			partPower = Float.parseFloat(str);
		} else if (qName == "wind_speed") {
			partWindSpeed = Float.parseFloat(str);
		} else if (qName == "wind_angle") {
			partWindAngle = Float.parseFloat(str);
		} else if (qName == "color") {
			partColor = new Color(Integer.parseInt(str, 16));
		} else if (qName == "texture_style") {
			partTextureStyle = Byte.parseByte(str);
		} else if (qName == "texture_width") {
			partTextureWidth = Integer.parseInt(str);
		} else if (qName == "texture_height") {
			partTextureHeight = Integer.parseInt(str);
		} else if (qName == "texture_fg") {
			partTextureForeground = new Color(Integer.parseInt(str, 16));
		} else if (qName == "texture_bg") {
			partTextureBackground = new Color(Integer.parseInt(str, 16));
		} else if (qName == "filled") {
			partFilled = Boolean.parseBoolean(str);
		} else if (qName == "visible") {
			partVisible = Boolean.parseBoolean(str);
		} else if (qName == "draggable") {
			partDraggable = Boolean.parseBoolean(str);
		} else if (qName == "boundary") {
			// nothing to do at this point
		} else if (qName == "part") {
			if (part != null) {
				if (!Float.isNaN(partThermalConductivity))
					part.setThermalConductivity(partThermalConductivity);
				if (!Float.isNaN(partSpecificHeat))
					part.setSpecificHeat(partSpecificHeat);
				if (!Float.isNaN(partDensity))
					part.setDensity(partDensity);
				if (!Float.isNaN(partTemperature))
					part.setTemperature(partTemperature);
				if (!Float.isNaN(partPower))
					part.setPower(partPower);
				if (!Float.isNaN(partEmissivity))
					part.setEmissivity(partEmissivity);
				if (!Float.isNaN(partAbsorption))
					part.setAbsorption(partAbsorption);
				if (!Float.isNaN(partReflection))
					part.setReflection(partReflection);
				if (!Float.isNaN(partTransmission))
					part.setTransmission(partTransmission);
				part.setWindAngle(partWindAngle);
				part.setWindSpeed(partWindSpeed);
				part.setConstantTemperature(partConstantTemperature);
				part.setDraggable(partDraggable);
				part.setVisible(partVisible);
				if (partColor != null)
					part.setFillPattern(new ColorFill(partColor));
				if (partTextureStyle != 0)
					part.setFillPattern(new Texture(partTextureForeground.getRGB(), partTextureBackground.getRGB(), partTextureStyle, partTextureWidth, partTextureHeight));
				part.setFilled(partFilled);
				part.setUid(partUid);
				part.setLabel(partLabel);
				resetPartVariables();
			}
		}

	}

	private void resetPartVariables() {
		partThermalConductivity = Float.NaN;
		partSpecificHeat = Float.NaN;
		partDensity = Float.NaN;
		partTemperature = Float.NaN;
		partConstantTemperature = false;
		partPower = Float.NaN;
		partEmissivity = Float.NaN;
		partAbsorption = Float.NaN;
		partReflection = Float.NaN;
		partTransmission = Float.NaN;
		partWindSpeed = 0;
		partWindAngle = 0;
		partVisible = true;
		partDraggable = true;
		partColor = Color.gray;
		partFilled = true;
		partTextureStyle = 0;
		partTextureWidth = 10;
		partTextureHeight = 10;
		partTextureForeground = Color.black;
		partTextureBackground = Color.white;
		partUid = null;
		partLabel = null;
	}

	private void resetGlobalVariables() {

		// model properties
		modelWidth = 10;
		modelHeight = 10;
		timeStep = 1;
		measurementInterval = 100;
		viewUpdateInterval = 20;
		stopTime = -1;
		sunny = false;
		sunAngle = (float) Math.PI * 0.5f;
		solarPowerDensity = 2000;
		solarRayCount = 24;
		solarRaySpeed = 0.1f;
		photonEmissionInterval = 20;
		zHeatDiffusivity = 0;
		convective = true;
		backgroundConductivity = Constants.AIR_THERMAL_CONDUCTIVITY;
		backgroundDensity = Constants.AIR_DENSITY;
		backgroundSpecificHeat = Constants.AIR_SPECIFIC_HEAT;
		backgroundViscosity = Constants.AIR_VISCOSITY;
		backgroundTemperature = 0;
		thermalBuoyancy = 0;
		buoyancyApproximation = Model2D.BUOYANCY_AVERAGE_COLUMN;

		// view properties
		ruler = false;
		grid = false;
		gridSize = 10;
		isotherm = false;
		streamline = false;
		colorPalette = false;
		velocity = false;
		heatFluxArrows = false;
		heatFluxLines = false;
		graphOn = false;
		clock = true;
		smooth = true;
		minimumTemperature = 0;
		maximumTemperature = 40;
		graphXLabel = null;
		graphYLabel = null;
		heatMapType = View2D.HEATMAP_TEMPERATURE;

	}

	public void characters(char[] ch, int start, int length) {
		str = new String(ch, start, length);
	}

	public void warning(SAXParseException e) {
		e.printStackTrace();
	}

	public void error(SAXParseException e) {
		e.printStackTrace();
	}

	public void fatalError(SAXParseException e) {
		e.printStackTrace();
	}

}
