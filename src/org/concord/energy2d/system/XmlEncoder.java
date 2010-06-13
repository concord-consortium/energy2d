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
		if (box.view.isGridOn()) {
			sb.append("<grid>true</grid>\n");
		}
		sb.append("</state>");
		return sb.toString();
	}

}
