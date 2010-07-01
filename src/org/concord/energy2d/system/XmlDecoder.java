package org.concord.energy2d.system;

import java.awt.Color;

import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
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
	private float minimumTemperature;
	private float maximumTemperature = 40;
	private int measurementInterval = 500;
	private boolean ruler;
	private boolean grid;
	private boolean isotherm;
	private boolean streamline;
	private boolean outline;
	private boolean rainbow;
	private boolean velocity;
	private boolean clock = true;
	private boolean smooth = true;
	private byte buoyancyApproximation = Model2D.BUOYANCY_AVERAGE_COLUMN;

	// part properties
	private float partThermalConductivity = Float.NaN;
	private float partSpecificHeat = Float.NaN;
	private float partDensity = Float.NaN;
	private float partTemperature = Float.NaN;
	private boolean partConstantTemperature = true;
	private float partPower = Float.NaN;
	private boolean partVisible = true;
	private boolean partDraggable = true;
	private Color partColor = Color.gray;
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
		box.model.setBuoyancyApproximation(buoyancyApproximation);

		box.view.setRulerOn(ruler);
		box.view.setOutlineOn(outline);
		box.view.setGridOn(grid);
		box.view.setIsothermOn(isotherm);
		box.view.setStreamlineOn(streamline);
		box.view.setVelocityOn(velocity);
		box.view.setRainbowOn(rainbow);
		box.view.setMinimumTemperature(minimumTemperature);
		box.view.setMaximumTemperature(maximumTemperature);
		box.view.setClockOn(clock);
		box.view.setSmooth(smooth);

		box.model.refreshPowerArray();
		box.model.refreshTemperatureBoundaryArray();
		box.model.refreshMaterialPropertyArrays();
		box.model.setInitialTemperature();
		box.view.repaint();

	}

	public void startElement(String uri, String localName, String qName,
			Attributes attrib) {

		String attribName, attribValue;

		float x = Float.NaN, y = Float.NaN, w = Float.NaN, h = Float.NaN;

		if (qName == "rectangle") {
			if (attrib != null) {
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
					if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w)
							&& !Float.isNaN(h))
						part = box.model.addRectangularPart(x, y, w, h);
				}
			}
		} else if (qName == "ellipse") {
			if (attrib != null) {
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
					if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w)
							&& !Float.isNaN(h))
						part = box.model.addEllipticalPart(x, y, w, h);
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
		} else if (qName == "grid") {
			grid = Boolean.parseBoolean(str);
		} else if (qName == "rainbow") {
			rainbow = Boolean.parseBoolean(str);
		} else if (qName == "clock") {
			clock = Boolean.parseBoolean(str);
		} else if (qName == "outline") {
			outline = Boolean.parseBoolean(str);
		} else if (qName == "smooth") {
			smooth = Boolean.parseBoolean(str);
		} else if (qName == "thermal_conductivity") {
			partThermalConductivity = Float.parseFloat(str);
		} else if (qName == "specific_heat") {
			partSpecificHeat = Float.parseFloat(str);
		} else if (qName == "density") {
			partDensity = Float.parseFloat(str);
		} else if (qName == "temperature") {
			partTemperature = Float.parseFloat(str);
		} else if (qName == "constant_temperature") {
			partConstantTemperature = Boolean.parseBoolean(str);
		} else if (qName == "power") {
			partPower = Float.parseFloat(str);
		} else if (qName == "color") {
			partColor = new Color(Integer.parseInt(str, 16));
		} else if (qName == "visible") {
			partVisible = Boolean.parseBoolean(str);
		} else if (qName == "draggable") {
			partDraggable = Boolean.parseBoolean(str);
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
				part.setConstantTemperature(partConstantTemperature);
				part.setDraggable(partDraggable);
				part.setVisible(partVisible);
				part.setColor(partColor);
				resetPartVariables();
			}
		}

	}

	private void resetPartVariables() {
		partThermalConductivity = Float.NaN;
		partSpecificHeat = Float.NaN;
		partDensity = Float.NaN;
		partTemperature = Float.NaN;
		partConstantTemperature = true;
		partPower = Float.NaN;
		partVisible = true;
		partDraggable = true;
		partColor = Color.gray;
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
