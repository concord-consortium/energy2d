package org.concord.energy2d.system;

import java.awt.Color;

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
	private float width = Float.NaN;
	private float height = Float.NaN;
	private boolean ruler;
	private boolean grid;
	private boolean isotherm;
	private boolean rainbow;
	private float timeStep = 1;

	XmlDecoder(System2D box) {
		this.box = box;
	}

	public void startDocument() {
	}

	public void endDocument() {
		if (!Float.isNaN(width)) {
			box.model.setLx(width);
			box.view.setArea(0, width, 0, box.model.getLy());
			width = Float.NaN;
		}
		if (!Float.isNaN(height)) {
			box.model.setLy(height);
			box.view.setArea(0, box.model.getLx(), 0, height);
			height = Float.NaN;
		}
		if (timeStep != 1) {
			box.model.setTimeStep(timeStep);
		}
		if (ruler) {
			box.view.setRulerOn(true);
		}
		if (grid) {
			box.view.setGridOn(true);
		}
		if (isotherm) {
			box.view.setIsothermOn(true);
		}
		if (rainbow) {
			box.view.setRainbowOn(true);
		}
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attrib) {

		String attribName, attribValue;

		if (qName == "rectangle") {
			if (attrib != null) {
				float x = 0;
				float y = 0;
				float w = 1;
				float h = 1;
				float thermal_conductivity = -1;
				float specific_heat = -1;
				boolean visible = true;
				boolean draggable = true;
				Color color = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "thermal_conductivity") {
						thermal_conductivity = Float.parseFloat(attribValue);
					} else if (attribName == "specific_heat") {
						specific_heat = Float.parseFloat(attribValue);
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
				p.setThermalConductivity(thermal_conductivity);
				p.setSpecificHeat(specific_heat);
				p.setDraggable(draggable);
				p.setVisible(visible);
				p.setColor(color);
			}
		}

	}

	public void endElement(String uri, String localName, String qName) {

		if (qName == "width") {
			width = Float.parseFloat(str);
		} else if (qName == "height") {
			height = Float.parseFloat(str);
		} else if (qName == "timestep") {
			timeStep = Float.parseFloat(str);
		} else if (qName == "ruler") {
			ruler = Boolean.parseBoolean(str);
		} else if (qName == "isotherm") {
			isotherm = Boolean.parseBoolean(str);
		} else if (qName == "grid") {
			grid = Boolean.parseBoolean(str);
		} else if (qName == "rainbow") {
			rainbow = Boolean.parseBoolean(str);
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
