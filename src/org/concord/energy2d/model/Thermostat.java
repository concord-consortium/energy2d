/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public class Thermostat implements Controller {

	private Thermometer thermometer;
	private Part powerSource;
	private float temperature = 20;
	private float delta = 1;

	public Thermostat(Thermometer thermometer, Part powerSource) {
		if (thermometer == null || powerSource == null)
			throw new IllegalArgumentException("A thermostat must connect a thermometer with a power source.");
		this.thermometer = thermometer;
		this.powerSource = powerSource;
	}

	public void control() {
		float power = powerSource.getPower();
		if (power == 0)
			return;
		float t = thermometer.getCurrentData();
		if (power > 0) { // if it is a heater
			if (t > temperature + delta) {
				powerSource.setPowerSwitch(false);
			} else if (t < temperature - delta) {
				powerSource.setPowerSwitch(true);
			}
		} else { // if it is a cooler
			if (t < temperature - delta) {
				powerSource.setPowerSwitch(false);
			} else if (t > temperature + delta) {
				powerSource.setPowerSwitch(true);
			}
		}
	}

	public void setDelta(float delta) {
		this.delta = delta;
	}

	public float getDelta() {
		return delta;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getTemperature() {
		return temperature;
	}

	public String toXml() {
		String xml = "<thermostat";
		xml += " temperature=\"" + temperature + "\"";
		xml += " delta=\"" + delta + "\"";
		xml += " thermometer=\"" + thermometer.getUid() + "\"";
		xml += " power_source=\"" + powerSource.getUid() + "\"/>";
		return xml;
	}

}
