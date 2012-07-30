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
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.concord.energy2d.event.ScriptEvent;
import org.concord.energy2d.event.ScriptListener;
import org.concord.energy2d.model.Boundary;
import org.concord.energy2d.model.DirichletThermalBoundary;
import org.concord.energy2d.model.MassBoundary;
import org.concord.energy2d.model.SimpleMassBoundary;
import org.concord.energy2d.model.ThermalBoundary;
import org.concord.energy2d.model.NeumannThermalBoundary;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.util.ColorFill;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.util.Scripter;
import org.concord.energy2d.view.Picture;
import org.concord.energy2d.view.TextBox;
import org.concord.energy2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class Scripter2D extends Scripter {

	private final static Pattern RUNSTEPS = compile("(^(?i)runsteps\\b){1}");
	private final static Pattern PART = compile("(^(?i)part\\b){1}");
	private final static Pattern THERMOMETER = compile("(^(?i)thermometer\\b){1}");
	private final static Pattern BOUNDARY = compile("(^(?i)boundary\\b){1}");
	private final static Pattern PART_FIELD = compile("^%?((?i)part){1}(\\[){1}" + REGEX_WHITESPACE + "*\\w+" + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern SENSOR_FIELD = compile("^%?((?i)sensor){1}(\\[){1}" + REGEX_WHITESPACE + "*\\w+" + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern IMAGE_FIELD = compile("^%?((?i)image){1}(\\[){1}" + REGEX_WHITESPACE + "*" + REGEX_NONNEGATIVE_DECIMAL + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern TEXT_FIELD = compile("^%?((?i)text){1}(\\[){1}" + REGEX_WHITESPACE + "*" + REGEX_NONNEGATIVE_DECIMAL + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern BOUNDARY_FIELD = compile("^%?((?i)boundary){1}(\\[){1}" + REGEX_WHITESPACE + "*\\w+" + REGEX_WHITESPACE + "*(\\]){1}\\.");

	private System2D s2d;
	private List<ScriptListener> listenerList;
	private boolean arrayUpdateRequested, temperatureInitializationRequested;

	Scripter2D(System2D s2d) {
		this.s2d = s2d;
	}

	void addScriptListener(ScriptListener listener) {
		if (listenerList == null)
			listenerList = new CopyOnWriteArrayList<ScriptListener>();
		if (!listenerList.contains(listener))
			listenerList.add(listener);
	}

	void removeScriptListener(ScriptListener listener) {
		if (listenerList == null)
			return;
		listenerList.remove(listener);
	}

	void removeAllScriptListeners() {
		if (listenerList == null)
			return;
		listenerList.clear();
	}

	private void notifyScriptListener(ScriptEvent e) {
		if (listenerList == null)
			return;
		synchronized (listenerList) {
			for (ScriptListener l : listenerList) {
				l.outputScriptResult(e);
			}
		}
	}

	private void showException(String command, Exception e) {
		e.printStackTrace();
		out(ScriptEvent.FAILED, "Error in \'" + command + "\':" + e.getMessage());
	}

	private void showError(String command, String message) {
		out(ScriptEvent.FAILED, "Error in \'" + command + "\':" + message);
	}

	private void out(byte status, String description) {
		if (status == ScriptEvent.FAILED) {
			notifyScriptListener(new ScriptEvent(s2d, status, "Aborted: " + description));
		} else {
			notifyScriptListener(new ScriptEvent(s2d, status, description));
		}
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

		matcher = RELOAD.matcher(ci);
		if (matcher.find()) {
			if (s2d.clickReload != null) {
				EventQueue.invokeLater(s2d.clickReload);
			} else {
				s2d.reload();
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

		matcher = INIT.matcher(ci);
		if (matcher.find()) {
			s2d.initialize();
			return;
		}

		matcher = LOAD.matcher(ci);
		if (matcher.find()) {
			URL codeBase = s2d.getCodeBase();
			if (codeBase != null) {
				String s = ci.substring(matcher.end()).trim();
				try {
					s2d.loadURL(new URL(codeBase, s));
				} catch (IOException e) {
					showException(ci, e);
				}
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
				boolean success = false;
				if (s.startsWith("(")) {
					final int j = s.indexOf(")");
					if (j != -1) {
						final float[] z = parseArray(2, s.substring(1, j));
						if (z != null) {
							final String s2 = s;
							final Runnable r = new Runnable() {
								public void run() {
									s2d.view.addText(s2.substring(j + 1).trim(), z[0], z[1]);
								}
							};
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									EventQueue.invokeLater(r);
								}
							});
							success = true;
						}
					}
				}
				if (!success)
					out(ScriptEvent.FAILED, "Error in \'" + ci + "\'");
			} else if (s.toLowerCase().startsWith("image")) {
				s = s.substring(5).trim();
				boolean success = false;
				if (s.startsWith("(")) {
					int j = s.indexOf(")");
					if (j != -1) {
						final float[] z = parseArray(2, s.substring(1, j));
						if (z != null) {
							String filename = s.substring(j + 1);
							URL url = null;
							try {
								url = new URL(s2d.getCodeBase(), filename);
							} catch (Exception e) {
								showException(ci, e);
								return;
							}
							if (url != null) {
								final ImageIcon image = new ImageIcon(url);
								final Runnable r = new Runnable() {
									public void run() {
										s2d.view.addPicture(image, s2d.view.convertPointToPixelX(z[0]), s2d.view.convertPointToPixelY(z[1]));
									}
								};
								EventQueue.invokeLater(new Runnable() {
									public void run() {
										EventQueue.invokeLater(r);
									}
								});
								success = true;
							}
						}
					}
				}
				if (!success)
					out(ScriptEvent.FAILED, "Error in \'" + ci + "\'");
			} else {
				out(ScriptEvent.FAILED, "Unrecognized command \'" + ci + "\'");
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
					showException(ci, e);
					return;
				}
			}
			out(ScriptEvent.FAILED, "Error in \'" + ci + "\'");
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
						float y = convertVerticalCoordinate(Float.parseFloat(t[1]));
						float w = Float.parseFloat(t[2]);
						float h = Float.parseFloat(t[3]);
						s2d.model.addRectangularPart(x, y, w, h);
					} catch (NumberFormatException e) {
						showException(ci, e);
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
						float y = convertVerticalCoordinate(Float.parseFloat(t[1]));
						float a = Float.parseFloat(t[2]);
						float b = Float.parseFloat(t[3]);
						s2d.model.addEllipticalPart(x, y, a, b);
					} catch (NumberFormatException e) {
						showException(ci, e);
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
						float ycenter = convertVerticalCoordinate(Float.parseFloat(t[1]));
						float inner = Float.parseFloat(t[2]);
						float outer = Float.parseFloat(t[3]);
						s2d.model.addRingPart(xcenter, ycenter, inner, outer);
					} catch (NumberFormatException e) {
						showException(ci, e);
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
							y[i] = convertVerticalCoordinate(Float.parseFloat(t[2 * i + 1]));
						} catch (NumberFormatException e) {
							showException(ci, e);
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
					DirichletThermalBoundary b = null;
					ThermalBoundary boundary = s2d.model.getThermalBoundary();
					if (boundary instanceof DirichletThermalBoundary) {
						b = (DirichletThermalBoundary) boundary;
					} else {
						b = new DirichletThermalBoundary();
						s2d.model.setThermalBoundary(b);
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
						showException(ci, e);
						return;
					}
				}
			} else if (s.startsWith("flux")) {
				s = s.substring(4).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 4) {
					NeumannThermalBoundary b = null;
					ThermalBoundary boundary = s2d.model.getThermalBoundary();
					if (boundary instanceof NeumannThermalBoundary) {
						b = (NeumannThermalBoundary) boundary;
					} else {
						b = new NeumannThermalBoundary();
						s2d.model.setThermalBoundary(b);
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
						showException(ci, e);
						return;
					}
				}
			}
		}

		matcher = SET.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			String[] t = s.split(REGEX_WHITESPACE + "+");
			if (t.length >= 2) {
				if (t[0].equalsIgnoreCase("sunny")) {
					s2d.model.setSunny("true".equalsIgnoreCase(t[1]));
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("action")) {
					byte mode = 0;
					if ("selection".equalsIgnoreCase(t[1])) {
						mode = View2D.SELECT_MODE;
					} else if ("rectangle".equalsIgnoreCase(t[1])) {
						mode = View2D.RECTANGLE_MODE;
					} else if ("ellipse".equalsIgnoreCase(t[1])) {
						mode = View2D.ELLIPSE_MODE;
					} else if ("polygon".equalsIgnoreCase(t[1])) {
						mode = View2D.POLYGON_MODE;
					} else if ("heating".equalsIgnoreCase(t[1])) {
						mode = View2D.HEATING_MODE;
					} else if ("thermometer".equalsIgnoreCase(t[1])) {
						mode = View2D.THERMOMETER_MODE;
					}
					s2d.view.setActionMode(mode);
				} else if (t[0].equalsIgnoreCase("temperature_increment")) {
					float temperatureIncrement = 0;
					try {
						temperatureIncrement = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.view.setTemperatureIncrement(temperatureIncrement);
				} else if (t[0].equalsIgnoreCase("sun_angle")) {
					float angle = 0;
					try {
						angle = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setSunAngle((float) (angle / 180.0 * Math.PI));
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("solar_power")) {
					float solarPower = 0;
					try {
						solarPower = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setSolarPowerDensity(solarPower);
				} else if (t[0].equalsIgnoreCase("emission_interval")) {
					int emissionInterval = 20;
					try {
						emissionInterval = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setPhotonEmissionInterval(emissionInterval);
				} else if (t[0].equalsIgnoreCase("ray_speed")) {
					float raySpeed = 0.1f;
					try {
						raySpeed = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setSolarRaySpeed(raySpeed);
				} else if (t[0].equalsIgnoreCase("ray_count")) {
					int rayCount = 0;
					try {
						rayCount = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
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
						showException(ci, e);
						return;
					}
					s2d.model.setThermalBuoyancy(thermalBuoyancy);
				} else if (t[0].equalsIgnoreCase("buoyancy_approximation")) {
					int buoyancyApproximation = 0;
					try {
						buoyancyApproximation = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setBuoyancyApproximation((byte) buoyancyApproximation);
				} else if (t[0].equalsIgnoreCase("velocity")) {
					s2d.view.setVelocityOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("heat_flux_arrow")) {
					s2d.view.setHeatFluxArrowsOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("heat_flux_line")) {
					s2d.view.setHeatFluxLinesOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("clock")) {
					s2d.view.setClockOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("ruler")) {
					s2d.view.setRulerOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("graph")) {
					s2d.view.setGraphOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("graph_xlabel")) {
					s2d.view.setGraphXLabel(t[1]);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("graph_ylabel")) {
					s2d.view.setGraphYLabel(t[1]);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("grid")) {
					s2d.view.setGridOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("grid_size")) {
					int gridSize = 0;
					try {
						gridSize = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.view.setGridSize(gridSize);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("color_palette")) {
					s2d.view.setColorPaletteOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("color_palette_type")) {
					if ("RAINBOW".equalsIgnoreCase(t[1])) {
						s2d.view.setColorPaletteType(View2D.RAINBOW);
						s2d.view.repaint();
					} else if ("IRON".equalsIgnoreCase(t[1])) {
						s2d.view.setColorPaletteType(View2D.IRON);
						s2d.view.repaint();
					} else if ("GRAY".equalsIgnoreCase(t[1])) {
						s2d.view.setColorPaletteType(View2D.GRAY);
						s2d.view.repaint();
					} else
						showError(ci, "color palette type not supported");
				} else if (t[0].equalsIgnoreCase("seethrough")) {
					s2d.view.setSeeThrough("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("isotherm")) {
					s2d.view.setIsothermOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("streamline")) {
					s2d.view.setStreamlineOn("true".equalsIgnoreCase(t[1]));
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("color_palette_rectangle")) {
					if (t.length > 4) {
						float x = 0, y = 0, w = 0, h = 0;
						try {
							x = Float.parseFloat(t[1]);
							y = Float.parseFloat(t[2]);
							w = Float.parseFloat(t[3]);
							h = Float.parseFloat(t[4]);
						} catch (NumberFormatException e) {
							showException(ci, e);
							return;
						}
						final float x1 = x;
						final float y1 = y;
						final float w1 = w;
						final float h1 = h;
						final Runnable r = new Runnable() {
							public void run() {
								float x2 = s2d.view.convertPointToPixelX(x1) / s2d.view.getWidth();
								float y2 = s2d.view.convertPointToPixelY(y1) / s2d.view.getHeight();
								float w2 = s2d.view.convertLengthToPixelX(w1) / s2d.view.getWidth();
								float h2 = s2d.view.convertLengthToPixelY(h1) / s2d.view.getHeight();
								s2d.view.setColorPaletteRectangle(x2, y2, w2, h2);
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
						showException(ci, e);
						return;
					}
					s2d.view.setMinimumTemperature(min);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("maximum_temperature")) {
					float max = 0;
					try {
						max = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.view.setMaximumTemperature(max);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("heat_map")) {
					if ("NONE".equalsIgnoreCase(t[1]))
						s2d.view.setHeatMapType(View2D.HEATMAP_NONE);
					else if ("TEMPERATURE".equalsIgnoreCase(t[1]))
						s2d.view.setHeatMapType(View2D.HEATMAP_TEMPERATURE);
					else if ("THERMAL_ENERGY".equalsIgnoreCase(t[1]))
						s2d.view.setHeatMapType(View2D.HEATMAP_THERMAL_ENERGY);
					else
						showError(ci, "heat map type not supported");
				} else if (t[0].equalsIgnoreCase("mouse_read")) {
					if ("NOTHING".equalsIgnoreCase(t[1]))
						s2d.view.setMouseReadType(View2D.MOUSE_READ_NOTHING);
					else if ("TEMPERATURE".equalsIgnoreCase(t[1]))
						s2d.view.setMouseReadType(View2D.MOUSE_READ_TEMPERATURE);
					else if ("THERMAL_ENERGY".equalsIgnoreCase(t[1]))
						s2d.view.setMouseReadType(View2D.MOUSE_READ_THERMAL_ENERGY);
					else if ("VELOCITY".equalsIgnoreCase(t[1]))
						s2d.view.setMouseReadType(View2D.MOUSE_READ_VELOCITY);
					else
						showError(ci, "mouse read type not supported");
				} else if (t[0].equalsIgnoreCase("isotherm_resolution")) {
					float resolution = 0;
					try {
						resolution = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.view.setIsothermResolution(resolution);
					s2d.view.repaint();
				} else if (t[0].equalsIgnoreCase("timestep")) {
					float timestep = 0;
					try {
						timestep = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setTimeStep(timestep);
				} else if (t[0].equalsIgnoreCase("viewupdate")) {
					int viewUpdateInterval = 0;
					try {
						viewUpdateInterval = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.repaint.setInterval(viewUpdateInterval);
				} else if (t[0].equalsIgnoreCase("measurement_interval")) {
					int measurementInterval = 0;
					try {
						measurementInterval = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.measure.setInterval(measurementInterval);
				} else if (t[0].equalsIgnoreCase("control_interval")) {
					int controlInterval = 0;
					try {
						controlInterval = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.control.setInterval(controlInterval);
				} else if (t[0].equalsIgnoreCase("stoptime")) {
					float stopTime = -1;
					try {
						stopTime = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.autopause.setInterval(stopTime > 0 ? Math.round(stopTime / s2d.model.getTimeStep()) : -1);
					s2d.autopause.setEnabled(s2d.autopause.getInterval() > 0);
				} else if (t[0].equalsIgnoreCase("width")) {
					float width = 0;
					try {
						width = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
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
						showException(ci, e);
						return;
					}
					s2d.model.setLy(height);
					s2d.view.setArea(0, s2d.model.getLx(), 0, height);
					temperatureInitializationRequested = true;
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("z_heat_diffusivity")) {
					float zHeatDiffusivity = 0;
					try {
						zHeatDiffusivity = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setZHeatDiffusivity(zHeatDiffusivity);
				} else if (t[0].equalsIgnoreCase("background_viscosity")) {
					float viscosity = 0;
					try {
						viscosity = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setBackgroundViscosity(viscosity);
				} else if (t[0].equalsIgnoreCase("background_conductivity")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setBackgroundConductivity(x);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_capacity")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setBackgroundSpecificHeat(x);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_specific_heat")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setBackgroundSpecificHeat(x);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_density")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
						return;
					}
					s2d.model.setBackgroundDensity(x);
					arrayUpdateRequested = true;
				} else if (t[0].equalsIgnoreCase("background_temperature")) {
					float x = 0;
					try {
						x = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						showException(ci, e);
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
						if (i < 0)
							return;
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setPartField(s1, s2, s3);
						return;
					}
					// sensor field
					matcher = SENSOR_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0)
							return;
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setSensorField(s1, s2, s3);
						return;
					}
					// boundary field
					matcher = BOUNDARY_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0)
							return;
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setBoundaryField(s1, s2, s3);
						return;
					}
					// text field
					matcher = TEXT_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0)
							return;
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
						if (i < 0)
							return;
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

		out(ScriptEvent.HARMLESS, "Command not recognized");

	}

	private void setPartField(String str1, String str2, String str3) {
		Part part = null;
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		String s = str1.substring(lb + 1, rb).trim();
		float z = Float.NaN;
		try {
			z = Float.parseFloat(s);
		} catch (Exception e) {
			z = Float.NaN;
		}
		part = Float.isNaN(z) ? s2d.model.getPart(s) : s2d.model.getPart((int) Math.round(z));
		if (part == null) {
			showError(str1, "Part " + s + " not found");
			return;
		}
		s = str2.toLowerCase().intern();
		if (str3.startsWith("#")) {
			try {
				z = Integer.parseInt(str3.substring(1), 16);
			} catch (Exception e) {
				showException(str3, e);
				return;
			}
		} else if (str3.startsWith("0X") || str3.startsWith("0x")) {
			try {
				z = Integer.parseInt(str3.substring(2), 16);
			} catch (Exception e) {
				showException(str3, e);
				return;
			}
		} else if (str3.equalsIgnoreCase("true")) {
			z = 1;
		} else if (str3.equalsIgnoreCase("false")) {
			z = 0;
		} else {
			if (s == "label") {
				part.setLabel(str3);
				return;
			}
			if (s == "uid") {
				part.setUid(str3);
				return;
			}
			try {
				z = Float.parseFloat(str3);
			} catch (Exception e) {
				showException(str3, e);
				return;
			}
		}
		if (s == "conductivity") {
			part.setThermalConductivity(z);
			arrayUpdateRequested = true;
		} else if (s == "thermal_conductivity") {
			part.setThermalConductivity(z);
			arrayUpdateRequested = true;
		} else if (s == "capacity") {
			part.setSpecificHeat(z);
			arrayUpdateRequested = true;
		} else if (s == "specific_heat") {
			part.setSpecificHeat(z);
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
			part.setFillPattern(new ColorFill(new Color((int) z)));
		} else if (s == "filled") {
			part.setFilled(z > 0);
		} else if (s == "draggable") {
			part.setDraggable(z > 0);
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

	private void setBoundaryField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		String side = str1.substring(lb + 1, rb);
		if (!side.equalsIgnoreCase("LEFT") && !side.equalsIgnoreCase("RIGHT") && !side.equalsIgnoreCase("LOWER") && !side.equalsIgnoreCase("UPPER")) {
			showError(str1 + str2 + str3, "Side parameter of boundary not recognized: must be LEFT, RIGHT, UPPER, or LOWER.");
		}
		float z = 0;
		try {
			z = Float.parseFloat(str3);
		} catch (Exception e) {
			showException(str3, e);
			return;
		}
		String s = str2.toLowerCase().intern();
		if (s == "temperature") {
			ThermalBoundary b = s2d.model.getThermalBoundary();
			if (b instanceof DirichletThermalBoundary) {
				DirichletThermalBoundary db = (DirichletThermalBoundary) b;
				if (side.equalsIgnoreCase("LEFT")) {
					db.setTemperatureAtBorder(Boundary.LEFT, z);
				} else if (side.equalsIgnoreCase("RIGHT")) {
					db.setTemperatureAtBorder(Boundary.RIGHT, z);
				} else if (side.equalsIgnoreCase("LOWER")) {
					db.setTemperatureAtBorder(Boundary.LOWER, z);
				} else if (side.equalsIgnoreCase("UPPER")) {
					db.setTemperatureAtBorder(Boundary.UPPER, z);
				}
			}
		} else if (s == "flux") {
			ThermalBoundary b = s2d.model.getThermalBoundary();
			if (b instanceof NeumannThermalBoundary) {
				NeumannThermalBoundary db = (NeumannThermalBoundary) b;
				if (side.equalsIgnoreCase("LEFT")) {
					db.setFluxAtBorder(Boundary.LEFT, z);
				} else if (side.equalsIgnoreCase("RIGHT")) {
					db.setFluxAtBorder(Boundary.RIGHT, z);
				} else if (side.equalsIgnoreCase("LOWER")) {
					db.setFluxAtBorder(Boundary.LOWER, z);
				} else if (side.equalsIgnoreCase("UPPER")) {
					db.setFluxAtBorder(Boundary.UPPER, z);
				}
			}
		} else if (s == "mass_flow_type") {
			MassBoundary b = s2d.model.getMassBoundary();
			if (b instanceof SimpleMassBoundary) {
				byte z2 = (byte) z;
				if (z2 == MassBoundary.REFLECTIVE || z2 == MassBoundary.THROUGH) {
					SimpleMassBoundary db = (SimpleMassBoundary) b;
					if (side.equalsIgnoreCase("LEFT")) {
						db.setFlowTypeAtBorder(Boundary.LEFT, z2);
					} else if (side.equalsIgnoreCase("RIGHT")) {
						db.setFlowTypeAtBorder(Boundary.RIGHT, z2);
					} else if (side.equalsIgnoreCase("LOWER")) {
						db.setFlowTypeAtBorder(Boundary.LOWER, z2);
					} else if (side.equalsIgnoreCase("UPPER")) {
						db.setFlowTypeAtBorder(Boundary.UPPER, z2);
					}
				} else {
					showError(str1 + str2 + str3, "Property value not recognized.");
				}
			}
		}
	}

	private void setSensorField(String str1, String str2, String str3) {
		Thermometer sensor = null;
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		String s = str1.substring(lb + 1, rb).trim();
		float z = Float.NaN;
		try {
			z = Float.parseFloat(s);
		} catch (Exception e) {
			z = Float.NaN;
		}
		sensor = Float.isNaN(z) ? s2d.model.getThermometer(s) : s2d.model.getThermometer((int) Math.round(z));
		if (sensor == null) {
			showError(str1, "Sensor " + s + " not found");
			return;
		}
		s = str2.toLowerCase().intern();
		if (s == "label") {
			sensor.setLabel(str3);
			return;
		}
		if (s == "uid") {
			sensor.setUid(str3);
			return;
		}
		try {
			z = Float.parseFloat(str3);
		} catch (Exception e) {
			showException(str3, e);
			return;
		}
		if (s == "x") {
			sensor.setX(z);
		} else if (s == "y") {
			sensor.setY(convertVerticalCoordinate(z));
		}
	}

	private void setTextField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			showException(str1, e);
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
		} else if (s == "text") {
			text.setString(str3);
			s2d.view.repaint();
		} else {
			if (str3.startsWith("#")) {
				try {
					z = Integer.parseInt(str3.substring(1), 16);
				} catch (Exception e) {
					showException(str3, e);
					return;
				}
			} else if (str3.startsWith("0X") || str3.startsWith("0x")) {
				try {
					z = Integer.parseInt(str3.substring(2), 16);
				} catch (Exception e) {
					showException(str3, e);
					return;
				}
			} else {
				try {
					z = Float.parseFloat(str3);
				} catch (Exception e) {
					showException(str3, e);
					return;
				}
			}
			if (s == "color") {
				text.setColor(new Color((int) z));
				s2d.view.repaint();
			} else if (s == "x") {
				final float z2 = z;
				final Runnable r = new Runnable() {
					public void run() {
						text.setX(z2);
						s2d.view.repaint();
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
						text.setY(z2);
						s2d.view.repaint();
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
			showException(str1, e);
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= s2d.view.getPictureCount()) {
			return;
		}
		try {
			z = Float.parseFloat(str3);
		} catch (Exception e) {
			showException(str3, e);
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
					picture.setX(z2);
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
					picture.setY(z2);
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
	 * in the rendering system, the origin is at the upper-left corner. In the user's coordinate system, the origin needs to be changed to the lower-left corner.
	 */
	private float convertVerticalCoordinate(float y) {
		return s2d.model.getLy() - y;
	}

}
