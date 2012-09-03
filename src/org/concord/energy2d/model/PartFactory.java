package org.concord.energy2d.model;

/**
 * @author Charles Xie
 * 
 */
public class PartFactory {

	private Model2D model;

	public PartFactory(Model2D model) {
		this.model = model;
	}

	public void addParabola(float a) {
		int nx = model.getNx();
		float lx = model.getLx();
		float ly = model.getLy();
		float[] x = new float[nx * 2];
		float[] y = new float[nx * 2];
		for (int i = 0; i < nx; i++) {
			x[i] = lx / nx * i;
			y[i] = ly - a * (x[i] - lx * 0.5f) * (x[i] - lx * 0.5f);
			x[2 * nx - 1 - i] = x[i];
			y[2 * nx - 1 - i] = y[i] + ly * 0.01f;
		}
		model.addPolygonPart(x, y);
	}

}
