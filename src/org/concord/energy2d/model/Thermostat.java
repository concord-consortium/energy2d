/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.model;

/**
 * A thermostat switches heating or cooling devices on or off to maintain the temperature at a setpoint.
 * 
 * @author Charles Xie
 * 
 */
public class Thermostat implements Controller {

	private Thermometer thermometer;
	private Part powerSource;
	private float setpoint = 20;
	private float deadband = 1;

	public Thermostat(Thermometer thermometer, Part powerSource) {
		if (thermometer == null || powerSource == null)
			throw new IllegalArgumentException("A thermostat must connect a thermometer with a power source.");
		this.thermometer = thermometer;
		this.powerSource = powerSource;
	}

	/** implements a bang-bang (on-off) controller */
	public boolean onoff() {
		float power = powerSource.getPower();
		if (power == 0)
			return false;
		boolean refresh = false;
		float t = thermometer.getCurrentData();
		if (power > 0) { // if it is a heater
			if (t > setpoint + deadband) {
				powerSource.setPowerSwitch(false);
				refresh = true;
			} else if (t < setpoint - deadband) {
				powerSource.setPowerSwitch(true);
				refresh = true;
			}
		} else { // if it is a cooler
			if (t < setpoint - deadband) {
				powerSource.setPowerSwitch(false);
				refresh = true;
			} else if (t > setpoint + deadband) {
				powerSource.setPowerSwitch(true);
				refresh = true;
			}
		}
		return refresh;
	}

	public Thermometer getThermometer() {
		return thermometer;
	}

	public Part getPowerSource() {
		return powerSource;
	}

	public void setDeadband(float deadband) {
		this.deadband = deadband;
	}

	public float getDeadband() {
		return deadband;
	}

	public void setSetPoint(float setpoint) {
		this.setpoint = setpoint;
	}

	public float getSetPoint() {
		return setpoint;
	}

	public String toXml() {
		String xml = "<thermostat";
		xml += " set_point=\"" + setpoint + "\"";
		xml += " deadband=\"" + deadband + "\"";
		xml += " thermometer=\"" + thermometer.getUid() + "\"";
		xml += " power_source=\"" + powerSource.getUid() + "\"/>";
		return xml;
	}

}
