/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.system;

import static java.util.regex.Pattern.compile;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.concord.energy2d.model.Boundary;
import org.concord.energy2d.model.DirichletHeatBoundary;
import org.concord.energy2d.model.HeatBoundary;
import org.concord.energy2d.model.NeumannHeatBoundary;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.util.Scripter;
import org.concord.energy2d.view.Picture;
import org.concord.energy2d.view.TextBox;

/**
 * @author Charles Xie
 * 
 */
class Scripter2D extends Scripter {

	private final static Pattern RUNSTEPS = compile("(^(?i)runsteps\\b){1}");
	private final static Pattern PART = compile("(^(?i)part\\b){1}");
	private final static Pattern THERMOMETER = compile("(^(?i)thermometer\\b){1}");
	private final static Pattern BOUNDARY = compile("(^(?i)boundary\\b){1}");
	private final static Pattern PART_FIELD = compile("^%?((?i)part){1}(\\[){1}"
			+ REGEX_WHITESPACE
			+ "*"
			+ REGEX_NONNEGATIVE_DECIMAL
			+ REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern IMAGE_FIELD = compile("^%?((?i)image){1}(\\[){1}"
			+ REGEX_WHITESPACE
			+ "*"
			+ REGEX_NONNEGATIVE_DECIMAL
			+ REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern TEXT_FIELD = compile("^%?((?i)text){1}(\\[){1}"
			+ REGEX_WHITESPACE
			+ "*"
			+ REGEX_NONNEGATIVE_DECIMAL
			+ REGEX_WHITESPACE + "*(\\]){1}\\.");

	private System2D s2d;
	private boolean arrayUpdateRequested, temperatureInitializationRequested;

	Scripter2D(System2D s2d) {
		this.s2d = s2d;
	}

	public void executeScript(String script) {
		super.executeScript(script);
		if (arrayUpdateRequested) {
			s2d.model.refreshPowerArray();
			s2d.model.refreshTemperatureBoundaryArray();
			s2d.model.refreshMaterialPropertyArrays();
			arrayUpdateRequested = false;
		}
		if (temperatureInitializationRequested) {
			s2d.model.setInitialTemperature();
			temperatureInitializationRequested = false;
		}
		s2d.view.repaint();
	}

	protected void evalCommand(String ci) {

		Matcher matcher = RESET.matcher(ci);
		if (matcher.find()) {
			if (s2d.clickReset != null) {
				EventQueue.invokeLater(s2d.clickReset);
			} else {
				s2d.reset();
			}
			return;
		}

		matcher = RUN.matcher(ci);
		if (matcher.find()) {
			if (s2d.clickRun != null) {
				EventQueue.invokeLater(s2d.clickRun);
			} else {
				s2d.run();
			}
			return;
		}

		matcher = RUNSTEPS.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			int nsteps = 10;
			try {
				nsteps = (int) Float.parseFloat(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
			s2d.runSteps(nsteps);
			return;
		}

		matcher = STOP.matcher(ci);
		if (matcher.find()) {
			if (s2d.clickStop != null) {
				EventQueue.invokeLater(s2d.clickStop);
			} else {
				s2d.stop();
			}
			return;
		}

		matcher = DELAY.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			try {
				Thread.sleep((int) (Float.valueOf(s).floatValue() * 1000));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		matcher = ADD.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.toLowerCase().startsWith("text")) {
				s = s.substring(4).trim();
				int i = s.indexOf("(");
				final int j = s.indexOf(")");
				if (i != -1 && j != -1) {
					final float[] z = parseArray(2, s.substring(i + 1, j));
					if (z != null) {
						final String s2 = s;
						final Runnable r = new Runnable() {
							public void run() {
								s2d.view.addText(s2.substring(j + 1), s2d.view
										.convertPointToPixelX(z[0]), s2d.view
										.convertPointToPixelY(z[1]));
							}
						};
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								EventQueue.invokeLater(r);
							}
						});
					}
				}
			} else if (s.toLowerCase().startsWith("image")) {
				s = s.substring(5).trim();
				int i = s.indexOf("(");
				int j = s.indexOf(")");
				if (i != -1 && j != -1) {
					final float[] z = parseArray(2, s.substring(i + 1, j));
					if (z != null) {
						String filename = s.substring(j + 1);
						URL url = null;
						try {
							url = new URL(s2d.getCodeBase(), filename);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
						if (url != null) {
							final ImageIcon image = new ImageIcon(url);
							final Runnable r = new Runnable() {
								public void run() {
									s2d.view
											.addPicture(
													image,
													s2d.view
															.convertPointToPixelX(z[0]),
													s2d.view
															.convertPointToPixelY(z[1]));
								}
							};
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									EventQueue.invokeLater(r);
								}
							});
						}
					}
				}
			}
			return;
		}

		matcher = REMOVE.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.equalsIgnoreCase("all")) {
				s2d.clear();
			} else {

			}
			return;
		}

		matcher = THERMOMETER.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			s = s.substring(1, s.length() - 1);
			String[] t = s.split(REGEX_SEPARATOR + "+");
			if (t.length == 2) {
				try {
					float x = Float.parseFloat(t[0]);
					float y = convertVerticalCoordinate(Float.parseFloat(t[1]));
					s2d.model.addThermometer(x, y);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return;
				}
			}
		}

		matcher = PART.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.toLowerCase().startsWith("rectangle")) {
				s = s.substring(9).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 4) {
					try {
						float x = Float.parseFloat(t[0]);
						float y = convertVerticalCoordinate(Float
								.parseFloat(t[1]));
						float w = Float.parseFloat(t[2]);
						float h = Float.parseFloat(t[3]);
						s2d.model.addRectangularPart(x, y, w, h);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("ellipse")) {
				s = s.substring(7).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 4) {
					try {
						float x = Float.parseFloat(t[0]);
						float y = convertVerticalCoordinate(Float
								.parseFloat(t[1]));
						float a = Float.parseFloat(t[2]);
						float b = Float.parseFloat(t[3]);
						s2d.model.addEllipticalPart(x, y, a, b);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("ring")) {
				s = s.substring(4).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 4) {
					try {
						float xcenter = Float.parseFloat(t[0]);
						float ycenter = convertVerticalCoordinate(Float
								.parseFloat(t[1]));
						float inner = Float.parseFloat(t[2]);
						float outer = Float.parseFloat(t[3]);
						s2d.model.addRingPart(xcenter, ycenter, inner, outer);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("polygon")) {
				s = s.substring(7).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				int n = t.length;
				if (n >= 6) {
					float[] x = new float[n / 2];
					float[] y = new float[n / 2];
					for (int i = 0; i < x.length; i++) {
						try {
							x[i] = Float.parseFloat(t[2 * i]);
							y[i] = convertVerticalCoordinate(Float
									.parseFloat(t[2 * i + 1]));
						} catch (NumberFormatException e) {
							e.printStackTrace();
							return;
						}
					}
					s2d.model.addPolygonPart(x, y);
				}
			}
			arrayUpdateRequested = true;
		}

		matcher = BOUNDARY.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim().toLowerCase();
			if (s.startsWith("temperature")) {
				s = s.substring(11).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 4) {
					DirichletHeatBoundary b = null;
					HeatBoundary boundary = s2d.model.getHeatBoundary();
					if (boundary instanceof DirichletHeatBoundary) {
						b = (DirichletHeatBoundary) boundary;
					} else {
						b = new DirichletHeatBoundary();
						s2d.model.setHeatBoundary(b);
					}
					try {
						float tN = Float.parseFloat(t[0]);
						float tE = Float.parseFloat(t[1]);
						float tS = Float.parseFloat(t[2]);
						float tW = Float.parseFloat(t[3]);
						b.setTemperatureAtBorder(Boundary.UPPER, tN);
						b.setTemperatureAtBorder(Boundary.RIGHT, tE);
						b.setTemperatureAtBorder(Boundary.LOWER, tS);
						b.setTemperatureAtBorder(Boundary.LEFT, tW);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.startsWith("flux")) {
				s = s.substring(4).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 4) {
					NeumannHeatBoundary b = null;
					HeatBoundary boundary = s2d.model.getHeatBoundary();
					if (boundary instanceof NeumannHeatBoundary) {
						b = (NeumannHeatBoundary) boundary;
					} else {
						b = new NeumannHeatBoundary();
						s2d.model.setHeatBoundary(b);
					}
					try {
						float fN = Float.parseFloat(t[0]);
						float fE = Float.parseFloat(t[1]);
						float fS = Float.parseFloat(t[2]);
						float fW = Float.parseFloat(t[3]);
						b.setFluxAtBorder(Boundary.UPPER, fN);
						b.setFluxAtBorder(Boundary.RIGHT, fE);
						b.setFluxAtBorder(Boundary.LOWER, fS);
						b.setFluxAtBorder(Boundary.LEFT, fW);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}

		matcher = SET.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			String[] t = s.split(REGEX_WHITESPACE);
			if (t.length >= 2) {
				if (t[0].equalsIgnoreCase("sunny")) {
					s2d.model.setSunny("true".equalsIgnoreCase(t[1]));
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("sun_angle")) {
					float angle = 0;
					try {
						angle = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setSunAngle((float) (angle / 180.0 * Math.PI));
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("solar_power")) {
					float solarPower = 0;
					try {
						solarPower = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setSolarPowerDensity(solarPower);
				} else if (t[0].equalsIgnoreCase("emission_interval")) {
					int emissionInterval = 20;
					try {
						emissionInterval = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setPhotonEmissionInterval(emissionInterval);
				} else if (t[0].equalsIgnoreCase("ray_speed")) {
					float raySpeed = 0.1f;
					try {
						raySpeed = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setRaySpeed(raySpeed);
				} else if (t[0].equalsIgnoreCase("ray_count")) {
					int rayCount = 0;
					try {
						rayCount = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setSolarRayCount(rayCount);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("convective")) {
					s2d.model.setConvective("true".equalsIgnoreCase(t[1]));
				} else if (t[0].equalsIgnoreCase("thermal_buoyancy")) {
					float thermalBuoyancy = 0;
					try {
						thermalBuoyancy = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setThermalBuoyancy(thermalBuoyancy);
				} else if (t[0].equalsIgnoreCase("buoyancy_approximation")) {
					int buoyancyApproximation = 0;
					try {
						buoyancyApproximation = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model
							.setBuoyancyApproximation((byte) buoyancyApproximation);
				} else if (t[0].equalsIgnoreCase("viscosity")) {
					float viscosity = 0;
					try {
						viscosity = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setViscosity(viscosity);
				} else if (t[0].equalsIgnoreCase("velocity")) {
					s2d.view.setVelocityOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("clock")) {
					s2d.view.setClockOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("ruler")) {
					s2d.view.setRulerOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("grid")) {
					s2d.view.setGridOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("rainbow")) {
					s2d.view.setRainbowOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("outline")) {
					s2d.view.setOutlineOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("isotherm")) {
					s2d.view.setIsothermOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("rainbow_rectangle")) {
					if (t.length > 4) {
						float x = 0, y = 0, w = 0, h = 0;
						try {
							x = Float.parseFloat(t[1]);
							y = Float.parseFloat(t[2]);
							w = Float.parseFloat(t[3]);
							h = Float.parseFloat(t[4]);
						} catch (NumberFormatException e) {
							return;
						}
						final float x1 = x;
						final float y1 = y;
						final float w1 = w;
						final float h1 = h;
						final Runnable r = new Runnable() {
							public void run() {
								int x2 = s2d.view.convertPointToPixelX(x1);
								int y2 = s2d.view.convertPointToPixelY(y1);
								int w2 = s2d.view.convertLengthToPixelX(w1);
								int h2 = s2d.view.convertLengthToPixelY(h1);
								s2d.view.setRainbowRectangle(x2, y2, w2, h2);
								s2d.view.repaint();
							}
						};
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								EventQueue.invokeLater(r);
							}
						});
					}
				} else if (t[0].equalsIgnoreCase("minimum_temperature")) {
					float min = 0;
					try {
						min = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.view.setMinimumTemperature(min);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("maximum_temperature")) {
					float max = 0;
					try {
						max = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.view.setMaximumTemperature(max);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("contour_resolution")) {
					float resolution = 0;
					try {
						resolution = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.view.setContourResolution(resolution);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("timestep")) {
					float timestep = 0;
					try {
						timestep = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setTimeStep(timestep);
				} else if (t[0].equalsIgnoreCase("viewupdate")) {
					int viewUpdateInterval = 0;
					try {
						viewUpdateInterval = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setViewUpdateInterval(viewUpdateInterval);
				} else if (t[0].equalsIgnoreCase("measurement_interval")) {
					int measurementInterval = 0;
					try {
						measurementInterval = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setMeasurementInterval(measurementInterval);
				} else if (t[0].equalsIgnoreCase("width")) {
					float width = 0;
					try {
						width = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setLx(width);
					s2d.view.setArea(0, width, 0, s2d.model.getLy());
					temperatureInitializationRequested = true;
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("height")) {
					float height = 0;
					try {
						height = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setLy(height);
					s2d.view.setArea(0, s2d.model.getLx(), 0, height);
					temperatureInitializationRequested = true;
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_conductivity")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setBackgroundConductivity(x);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_capacity")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setBackgroundCapacity(x);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_density")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setBackgroundDensity(x);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_temperature")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					s2d.model.setBackgroundTemperature(x);
					temperatureInitializationRequested = true;
				} else if (t[0].equalsIgnoreCase("bgcolor")) {
					final Color c = MiscUtil.parseRGBColor(t[1]);
					if (c != null) {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								s2d.view.setBackground(c);
							}
						});
					}
				} else {
					// part field
					matcher = PART_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setPartField(s1, s2, s3);
						return;
					}
					// text field
					matcher = TEXT_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setTextField(s1, s2, s3);
						s2d.view.repaint();
						return;
					}
					// image field
					matcher = IMAGE_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setImageField(s1, s2, s3);
						s2d.view.repaint();
						return;
					}
				}
			}
			return;
		}

	}

	private void setPartField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= s2d.model.getPartCount()) {
			return;
		}
		if (str3.startsWith("#")) {
			try {
				z = Integer.parseInt(str3.substring(1), 16);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else if (str3.startsWith("0X") || str3.startsWith("0x")) {
			try {
				z = Integer.parseInt(str3.substring(2), 16);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else {
			try {
				z = Float.parseFloat(str3);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		String s = str2.toLowerCase().intern();
		Part part = s2d.model.getPart(i);
		if (part == null)
			return;
		if (s == "conductivity") {
			part.setConductivity(z);
			arrayUpdateRequested = true;
		} else if (s == "capacity") {
			part.setCapacity(z);
			arrayUpdateRequested = true;
		} else if (s == "density") {
			part.setDensity(z);
			arrayUpdateRequested = true;
		} else if (s == "power") {
			part.setPower(z);
			arrayUpdateRequested = true;
		} else if (s == "wind_speed") {
			part.setWindSpeed(z);
			arrayUpdateRequested = true;
		} else if (s == "wind_angle") {
			part.setWindAngle((float) Math.toRadians(z));
			arrayUpdateRequested = true;
		} else if (s == "temperature") {
			part.setTemperature(z);
			arrayUpdateRequested = true;
		} else if (s == "color") {
			part.setColor(new Color((int) z));
		} else if (s == "filled") {
			part.setFilled(z > 0);
		} else if (s == "transmission") {
			part.setTransmission(z);
			arrayUpdateRequested = true;
		} else if (s == "absorption") {
			part.setAbsorption(z);
			arrayUpdateRequested = true;
		} else if (s == "reflection") {
			part.setReflection(z);
			arrayUpdateRequested = true;
		} else if (s == "emissivity") {
			part.setEmissivity(z);
			arrayUpdateRequested = true;
		} else if (s == "constant_temperature") {
			part.setConstantTemperature(z > 0);
			arrayUpdateRequested = true;
		}
		Shape shape = part.getShape();
		if (shape instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			if (s == "x") {
				r.x = z;
				arrayUpdateRequested = true;
			} else if (s == "y") {
				r.y = convertVerticalCoordinate(z);
				arrayUpdateRequested = true;
			} else if (s == "width") {
				r.width = z;
				arrayUpdateRequested = true;
			} else if (s == "height") {
				r.height = z;
				arrayUpdateRequested = true;
			}
		} else if (shape instanceof Ellipse2D.Float) {
			Ellipse2D.Float e = (Ellipse2D.Float) shape;
			if (s == "x") {
				e.x = z;
				arrayUpdateRequested = true;
			} else if (s == "y") {
				e.y = convertVerticalCoordinate(z);
				arrayUpdateRequested = true;
			} else if (s == "width") {
				e.width = z;
				arrayUpdateRequested = true;
			} else if (s == "height") {
				e.height = z;
				arrayUpdateRequested = true;
			}
		} else if (shape instanceof Area) {
		}
	}

	private void setTextField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= s2d.view.getTextBoxCount()) {
			return;
		}
		final TextBox text = s2d.view.getTextBox(i);
		if (text == null)
			return;
		String s = str2.toLowerCase().intern();
		if (s == "name") {
			text.setName(str3);
		} else {
			if (str3.startsWith("#")) {
				try {
					z = Integer.parseInt(str3.substring(1), 16);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			} else if (str3.startsWith("0X") || str3.startsWith("0x")) {
				try {
					z = Integer.parseInt(str3.substring(2), 16);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			} else {
				try {
					z = Float.parseFloat(str3);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			if (s == "color") {
				text.setColor(new Color((int) z));
			} else if (s == "x") {
				final float z2 = z;
				final Runnable r = new Runnable() {
					public void run() {
						text.setX(s2d.view.convertPointToPixelX(z2));
					}
				};
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						EventQueue.invokeLater(r);
					}
				});
			} else if (s == "y") {
				final float z2 = z;
				final Runnable r = new Runnable() {
					public void run() {
						text.setY(s2d.view.convertPointToPixelY(z2));
					}
				};
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						EventQueue.invokeLater(r);
					}
				});
			}
		}
	}

	private void setImageField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= s2d.view.getPictureCount()) {
			return;
		}
		try {
			z = Float.parseFloat(str3);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String s = str2.toLowerCase().intern();
		final Picture picture = s2d.view.getPicture(i);
		if (picture == null)
			return;
		if (s == "x") {
			final float z2 = z;
			final Runnable r = new Runnable() {
				public void run() {
					picture.setX(s2d.view.convertPointToPixelX(z2));
				}
			};
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					EventQueue.invokeLater(r);
				}
			});
		} else if (s == "y") {
			final float z2 = z;
			final Runnable r = new Runnable() {
				public void run() {
					picture.setY(s2d.view.convertPointToPixelY(z2));
				}
			};
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					EventQueue.invokeLater(r);
				}
			});
		}
	}

	/*
	 * in the rendering system, the origin is at the upper-left corner. In the
	 * user's coordinate system, the origin needs to be changed to the
	 * lower-left corner.
	 */
	private float convertVerticalCoordinate(float y) {
		return s2d.model.getLy() - y;
	}

}
