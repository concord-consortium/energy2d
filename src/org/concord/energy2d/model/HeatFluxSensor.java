package org.concord.energy2d.model;

import java.awt.geom.Rectangle2D;

/**
 * @author Charles Xie
 * 
 */
public class HeatFluxSensor extends Sensor {

	public final static float RELATIVE_WIDTH = 0.036f;
	public final static float RELATIVE_HEIGHT = 0.012f;

	private float angle;

	public HeatFluxSensor(float x, float y) {
		super(new Rectangle2D.Float());
		setCenter(x, y);
	}

	public HeatFluxSensor(float x, float y, String label) {
		this(x, y);
		setLabel(label);
	}

	public HeatFluxSensor duplicate(float x, float y) {
		return new HeatFluxSensor(x, y);
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	@Override
	public String toXml() {
		String xml = "<heat_flux_sensor";
		String uid = getUid();
		if (uid != null && !uid.trim().equals(""))
			xml += " uid=\"" + uid + "\"";
		String label = getLabel();
		if (label != null && !label.trim().equals(""))
			xml += " label=\"" + label + "\"";
		xml += " angle=\"" + angle + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>";
		return xml;
	}

}
