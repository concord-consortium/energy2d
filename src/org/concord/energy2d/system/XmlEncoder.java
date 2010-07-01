package org.concord.energy2d.system;

import java.util.List;

import org.concord.energy2d.model.Part;

/**
 * @author Charles Xie
 * 
 */
class XmlEncoder {

	private System2D box;

	XmlEncoder(System2D box) {
		this.box = box;
	}

	String encode() {

		StringBuffer sb = new StringBuffer(1000);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<state>\n");

		// view properties

		sb.append("<view>\n");
		if (box.view.isGridOn()) {
			sb.append("<grid>true</grid>\n");
		}
		if (box.view.isIsothermOn()) {
			sb.append("<isotherm>true</isotherm>\n");
		}
		if (box.view.isRainbowOn()) {
			sb.append("<rainbow>true</rainbow>\n");
		}
		if (box.view.getMinimumTemperature() != 0) {
			sb.append("<minimum_temperature>"
					+ box.view.getMinimumTemperature()
					+ "</minimum_temperature>\n");
		}
		if (box.view.getMaximumTemperature() != 40) {
			sb.append("<maximum_temperature>"
					+ box.view.getMaximumTemperature()
					+ "</maximum_temperature>\n");
		}
		if (box.view.isOutlineOn()) {
			sb.append("<outline>true</outline>\n");
		}
		if (box.view.isVelocityOn()) {
			sb.append("<velocity>true</velocity>\n");
		}
		if (box.view.isStreamlineOn()) {
			sb.append("<streamline>true</streamline>\n");
		}
		if (box.view.isGraphOn()) {
			sb.append("<graph>true</graph>\n");
		}
		if (!box.view.isClockOn()) {
			sb.append("<clock>false</clock>\n");
		}
		if (!box.view.isSmooth()) {
			sb.append("<smooth>false</smooth>\n");
		}
		sb.append("</view>\n");

		// model properties

		sb.append("<model>\n");

		if (box.model.getLx() != 10) {
			sb.append("<model_width>" + box.model.getLx() + "</model_width>\n");
		}
		if (box.model.getLy() != 10) {
			sb.append("<model_height>" + box.model.getLy()
					+ "</model_height>\n");
		}
		if (box.model.getTimeStep() != 1) {
			sb.append("<timestep>" + box.model.getTimeStep() + "</timestep>\n");
		}
		if (box.model.getMeasurementInterval() != 500) {
			sb.append("<measurement_interval>" + box.model.getTimeStep()
					+ "</measurement_interval>\n");
		}
		sb.append("<buoyancy_approximation>"
				+ box.model.getBuoyancyApproximation()
				+ "</buoyancy_approximation>\n");

		sb.append("<boundary>\n");
		sb.append(box.model.getHeatBoundary().toXml());
		sb.append("</boundary>\n");

		List<Part> parts = box.model.getParts();
		if (!parts.isEmpty()) {
			sb.append("<structure>\n");
			for (Part p : parts) {
				sb.append(p.toXml());
			}
			sb.append("</structure>\n");
		}

		sb.append("</model>\n");

		sb.append("</state>\n");

		return sb.toString();

	}

}
