package org.concord.energy2d.view;

/**
 * @author Charles Xie
 * 
 */
class DataRange {

	public float min = 0;
	public float max = 50;

	public DataRange(float min, float max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public String toString() {
		return min + " " + max;
	}

}
