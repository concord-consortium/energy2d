package org.concord.energy2d.system;

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
		sb.append("<minimum_temperature>" + box.view.getMinimumTemperature()
				+ "</minimum_temperature>\n");
		sb.append("<maximum_temperature>" + box.view.getMaximumTemperature()
				+ "</maximum_temperature>\n");
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
			sb.append("<model_width>" + box.model.getLx() + "</model_width>");
		}
		if (box.model.getLy() != 10) {
			sb.append("<model_height>" + box.model.getLy() + "</model_height>");
		}
		if (box.model.getTimeStep() != 1) {
			sb.append("<timestep>" + box.model.getTimeStep() + "</timestep>");
		}
		sb.append("</model>\n");

		sb.append("</state>\n");

		return sb.toString();

	}

}
