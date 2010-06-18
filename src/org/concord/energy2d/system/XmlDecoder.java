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

	}

	public void startElement(String uri, String localName, String qName,
			Attributes attrib) {

		String attribName, attribValue;

		float thermal_conductivity = Float.NaN;
		float specific_heat = Float.NaN;
		float density = Float.NaN;
		float temperature = Float.NaN;
		boolean visible = true;
		boolean draggable = true;
		Color color = Color.gray;

		if (qName == "rectangle") {
			if (attrib != null) {
				float x = 0;
				float y = 0;
				float w = 1;
				float h = 1;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "thermal_conductivity") {
						thermal_conductivity = Float.parseFloat(attribValue);
					} else if (attribName == "specific_heat") {
						specific_heat = Float.parseFloat(attribValue);
					} else if (attribName == "density") {
						density = Float.parseFloat(attribValue);
					} else if (attribName == "temperature") {
						temperature = Float.parseFloat(attribValue);
					} else if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					} else if (attribName == "visible") {
						visible = Boolean.parseBoolean(attribValue);
					} else if (attribName == "draggable") {
						draggable = Boolean.parseBoolean(attribValue);
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				Part p = box.model.addRectangularPart(x, y, w, h);
				if (!Float.isNaN(thermal_conductivity))
					p.setThermalConductivity(thermal_conductivity);
				if (!Float.isNaN(specific_heat))
					p.setSpecificHeat(specific_heat);
				if (!Float.isNaN(density))
					p.setDensity(density);
				if (!Float.isNaN(temperature))
					p.setTemperature(temperature);
				p.setDraggable(draggable);
				p.setVisible(visible);
				p.setColor(color);
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
		}

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
