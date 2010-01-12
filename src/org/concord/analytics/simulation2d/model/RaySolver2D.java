/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.analytics.simulation2d.model;

import java.util.Iterator;
import java.util.List;

/**
 * This solver models the ray optics of sunlight. Reflection, refraction, and
 * absorption are included.
 * 
 * @author Charles Xie
 * 
 */
class RaySolver2D {

	private float[][] q;
	private float deltaX, deltaY;
	private float lx, ly, sunAngle = (float) Math.PI * 0.5f;
	private int rayCount = 24;
	private float solarPowerDensity = 2000;
	private float rayPower = solarPowerDensity;

	/*
	 * the speed of the particle that carries light energy. Note that this is
	 * NOT the speed of light. This is just an artificial parameter.
	 */
	private float raySpeed = .1f;

	RaySolver2D(float lx, float ly) {
		this.lx = lx;
		this.ly = ly;
	}

	void setRaySpeed(float raySpeed) {
		this.raySpeed = raySpeed;
	}

	float getRaySpeed() {
		return raySpeed;
	}

	void setSolarPowerDensity(float solarPowerDensity) {
		this.solarPowerDensity = solarPowerDensity;
		rayPower = solarPowerDensity * 24 / rayCount;
	}

	float getSolarPowerDensity() {
		return solarPowerDensity;
	}

	void setSolarRayCount(int solarRayCount) {
		rayCount = solarRayCount;
		rayPower = solarPowerDensity * 24 / rayCount;
	}

	int getSolarRayCount() {
		return rayCount;
	}

	void setGridCellSize(float deltaX, float deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	void setPower(float[][] q) {
		this.q = q;
	}

	void radiate(Model2D model) {
		synchronized (model.getParts()) {
			for (Part p : model.getParts()) {
				if (p.getEmissivity() > 0)
					p.radiate(model);
			}
		}
	}

	void solve(Model2D model) {
		List<Photon> photons = model.getPhotons();
		// System.out.println(photons.size());
		if (photons.isEmpty())
			return;
		Photon p;
		float timeStep = model.getTimeStep();
		// Since a photon is emitted at a given interval, its energy
		// has to be divided evenly for internal power generation at
		// each second. The following factor takes this into account.
		float factor = 1.0f / (timeStep * model.getPhotonEmissionInterval());
		float idx = 1.0f / deltaX;
		float idy = 1.0f / deltaY;
		int i, j;
		int nx = q.length - 1;
		int ny = q[0].length - 1;
		boolean remove;
		synchronized (photons) {
			for (Iterator<Photon> it = photons.iterator(); it.hasNext();) {
				p = it.next();
				p.move(timeStep);
				if (model.getPartCount() > 0) {
					remove = false;
					synchronized (model.getParts()) {
						for (Part part : model.getParts()) {
							if (Math.abs(part.getReflection() - 1) < 0.001f) {
								if (part.reflect(p, timeStep))
									break;
							} else if (Math.abs(part.getAbsorption() - 1) < 0.001f) {
								if (part.absorb(p)) {
									i = Math.min(nx, (int) (p.getX() * idx));
									j = Math.min(ny, (int) (p.getY() * idy));
									q[i][j] = p.getEnergy() * factor;
									remove = true;
									break;
								}
							}
						}
					}
					if (remove)
						it.remove();
				}
			}
		}
		applyBoundary(photons);
	}

	void setSunAngle(float sunAngle) {
		this.sunAngle = (float) Math.PI - sunAngle;
	}

	float getSunAngle() {
		return (float) Math.PI - sunAngle;
	}

	void sunShine(List<Photon> photons, List<Part> parts) {
		if (sunAngle < 0)
			return;
		float s = (float) Math.abs(Math.sin(sunAngle));
		float c = (float) Math.abs(Math.cos(sunAngle));
		float spacing = s * ly < c * lx ? ly / c : lx / s;
		spacing /= rayCount;
		shootAtAngle(spacing / s, spacing / c, photons, parts);
	}

	private static boolean isContained(float x, float y, List<Part> parts) {
		synchronized (parts) {
			for (Part p : parts) {
				if (p.contains(x, y)) {
					return true;
				}
			}
		}
		return false;
	}

	private void shootAtAngle(float dx, float dy, List<Photon> photons,
			List<Part> parts) {
		int m = (int) (lx / dx);
		int n = (int) (ly / dy);
		float x, y;
		if (sunAngle >= 0 && sunAngle < 0.5f * Math.PI) {
			y = 0;
			for (int i = 1; i <= m; i++) {
				x = dx * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
			x = 0;
			for (int i = 0; i <= n; i++) {
				y = dy * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
		} else if (sunAngle < 0 && sunAngle >= -0.5f * Math.PI) {
			y = ly;
			for (int i = 1; i <= m; i++) {
				x = dx * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
			x = 0;
			for (int i = 0; i <= n; i++) {
				y = ly - dy * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
		} else if (sunAngle < Math.PI + 0.001 && sunAngle >= 0.5f * Math.PI) {
			y = 0;
			for (int i = 0; i <= m; i++) {
				x = lx - dx * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
			x = lx;
			for (int i = 1; i <= n; i++) {
				y = dy * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
		} else if (sunAngle >= -Math.PI && sunAngle < -0.5f * Math.PI) {
			y = ly;
			for (int i = 0; i <= m; i++) {
				x = lx - dx * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
			x = lx;
			for (int i = 1; i <= n; i++) {
				y = ly - dy * i;
				if (!isContained(x, y, parts))
					photons.add(new Photon(x, y, rayPower, sunAngle, raySpeed));
			}
		}
	}

	/* transparent boundary condition is assumed */
	void applyBoundary(List<Photon> photons) {
		synchronized (photons) {
			for (Iterator<Photon> it = photons.iterator(); it.hasNext();) {
				if (!it.next().isContained(0, lx, 0, ly)) {
					it.remove();
				}
			}
		}
	}

}
