/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.concord.energy2d.event.GraphEvent;
import org.concord.energy2d.event.GraphListener;
import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Photon;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Thermostat;
import org.concord.energy2d.system.Helper;
import org.concord.energy2d.util.ColorFill;
import org.concord.energy2d.util.ContourMap;
import org.concord.energy2d.util.FieldLines;
import org.concord.energy2d.util.FillPattern;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.util.Texture;
import org.concord.energy2d.util.TextureFactory;

/**
 * @author Charles Xie
 * 
 */
public class View2D extends JPanel implements PropertyChangeListener {

	public final static String DEFAULT_YLABEL = "Temperature (" + '\u2103' + ")";
	public final static String DEFAULT_XLABEL = "Time (hr)";

	public final static byte SELECT_MODE = 0;
	public final static byte RECTANGLE_MODE = 1;
	public final static byte ELLIPSE_MODE = 2;
	public final static byte POLYGON_MODE = 3;
	public final static byte THERMOMETER_MODE = 11;
	public final static byte HEATING_MODE = 21;

	public final static byte HEATMAP_NONE = 0;
	public final static byte HEATMAP_TEMPERATURE = 1;
	public final static byte HEATMAP_THERMAL_ENERGY = 2;
	public final static byte MOUSE_READ_DEFAULT = 0;
	public final static byte MOUSE_READ_TEMPERATURE = 1;
	public final static byte MOUSE_READ_THERMAL_ENERGY = 2;
	public final static byte MOUSE_READ_VELOCITY = 3;

	public final static byte RAINBOW = 0;
	public final static byte IRON = 1;
	public final static byte GRAY = 2;

	final static byte UPPER_LEFT = 0;
	final static byte LOWER_LEFT = 1;
	final static byte UPPER_RIGHT = 2;
	final static byte LOWER_RIGHT = 3;
	final static byte TOP = 4;
	final static byte BOTTOM = 5;
	final static byte LEFT = 6;
	final static byte RIGHT = 7;

	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");

	private final static int MINIMUM_MOUSE_DRAG_RESPONSE_INTERVAL = 20;
	private final static DecimalFormat TEMPERATURE_FORMAT = new DecimalFormat("###.#");
	private final static DecimalFormat VELOCITY_FORMAT = new DecimalFormat("#.####");
	private Font smallFont = new Font(null, Font.PLAIN, 10);
	private Font sensorReadingFont = new Font(null, Font.PLAIN, 10);
	private Font labelFont = new Font("Arial", Font.PLAIN | Font.BOLD, 14);

	private BufferedImage bimg;
	private RulerRenderer rulerRenderer;
	private GridRenderer gridRenderer;
	private ColorPalette colorPalette;
	private GraphRenderer graphRenderer;
	private ScalarDistributionRenderer temperatureRenderer, thermalEnergyRenderer;
	private VectorDistributionRenderer vectorFieldRenderer;
	private ThermostatRenderer thermostatRenderer;
	private boolean showIsotherm;
	private boolean showStreamLines;
	private boolean showVelocity;
	private boolean showHeatFluxArrows, showHeatFluxLines;
	private boolean showGraph;
	private boolean showColorPalette;
	private boolean showGrid;
	private boolean clockOn = true;
	private boolean frankOn = true;
	private byte heatMapType = HEATMAP_TEMPERATURE;
	private byte mouseReadType = MOUSE_READ_DEFAULT;
	private byte colorPaletteType = RAINBOW;
	private float[][] distribution;

	private static Stroke thinStroke = new BasicStroke(1);
	private static Stroke moderateStroke = new BasicStroke(2);
	private static Stroke thickStroke = new BasicStroke(4);
	private static Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 2 }, 0);
	private final static Color TRANSLUCENT_GRAY = new Color(128, 128, 128, 128);
	private float xmin, xmax, ymin, ymax;
	private int nx, ny;
	private float time;
	private JPopupMenu popupMenu;
	private Rectangle[] handle = new Rectangle[8];
	private boolean mouseBeingDragged;
	private MovingShape movingShape;
	private Point pressedPointRelative = new Point();
	private long mousePressedTime;
	private byte selectedSpot = -1;
	private Point anchorPoint = new Point();
	private AffineTransform scale;
	private ContourMap isotherms;
	private FieldLines streamlines;
	private FieldLines heatFluxLines;
	private Polygon multigon;
	private float photonLength = 10;
	private byte actionMode = SELECT_MODE;
	private Rectangle rectangle = new Rectangle();
	private Ellipse2D.Float ellipse = new Ellipse2D.Float();
	private Polygon polygon = new Polygon();
	private Point mousePressedPoint = new Point(-1, -1);
	private Point mouseReleasedPoint = new Point(-1, -1);
	private Point mouseMovedPoint = new Point(-1, -1);
	private String errorMessage;
	private DecimalFormat formatter = new DecimalFormat("#####.#####");
	private Color lightColor = new Color(255, 255, 255, 128);
	private Symbol moon, sun;

	Model2D model;
	private Manipulable selectedManipulable, copiedManipulable;
	private List<TextBox> textBoxes;
	private List<Picture> pictures;

	private JPopupMenu tipPopupMenu;
	private boolean runToggle;
	private DialogFactory dialogFactory;

	private List<ManipulationListener> manipulationListeners;
	private List<GraphListener> graphListeners;

	private Action copyAction;
	private Action cutAction;
	private Action pasteAction;
	private volatile boolean runHeatingThread;
	private volatile boolean cooling;
	private volatile float heatingX, heatingY;
	private float temperatureIncrement = 10;

	public View2D() {
		super();
		for (int i = 0; i < handle.length; i++)
			handle[i] = new Rectangle(0, 0, 6, 6);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				processKeyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				processKeyReleased(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				processMousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				processMouseReleased(e);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				processMouseMoved(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				processMouseDragged(e);
			}
		});
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				processComponentResized(e);
			}
		});
		textBoxes = Collections.synchronizedList(new ArrayList<TextBox>());
		createActions();
		createPopupMenu();
		setColorPaletteType(colorPaletteType);
		dialogFactory = new DialogFactory(this);
		graphRenderer = new GraphRenderer(50, 50, 200, 200);
		thermostatRenderer = new ThermostatRenderer();
		manipulationListeners = new ArrayList<ManipulationListener>();
		graphListeners = new ArrayList<GraphListener>();
	}

	private void createActions() {

		cutAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				cut();
			}
		};
		KeyStroke ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK);
		cutAction.putValue(Action.NAME, "Cut");
		cutAction.putValue(Action.ACCELERATOR_KEY, ks);
		getInputMap().put(ks, "Cut");
		getActionMap().put("Cut", cutAction);

		copyAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
		copyAction.putValue(Action.NAME, "Copy");
		copyAction.putValue(Action.ACCELERATOR_KEY, ks);
		getInputMap().put(ks, "Copy");
		getActionMap().put("Copy", copyAction);

		pasteAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
		pasteAction.putValue(Action.NAME, "Paste");
		pasteAction.putValue(Action.ACCELERATOR_KEY, ks);
		getInputMap().put(ks, "Paste");
		getActionMap().put("Paste", pasteAction);

		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				TextBox t = new TextBox(new Rectangle2D.Float());
				t.setX(mouseReleasedPoint.x > 0 ? convertPixelToPointX(mouseReleasedPoint.x) : model.getLx() * 0.1f);
				t.setY(mouseReleasedPoint.y > 0 ? model.getLy() - convertPixelToPointY(mouseReleasedPoint.y) : model.getLy() * 0.9f);
				addTextBox(t);
				TextBoxPanel tbp = new TextBoxPanel(t, View2D.this);
				tbp.createDialog(true).setVisible(true);
				if (tbp.isCancelled() || t.getLabel() == null || t.getLabel().trim().equals("")) {
					removeTextBox(t);
				} else {
					setSelectedManipulable(t);
				}
			}
		};
		a.putValue(Action.NAME, "Text Box");
		a.putValue(Action.SHORT_DESCRIPTION, "Insert a text box where the mouse last clicked");
		getActionMap().put("Insert Text Box", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				float x = mouseReleasedPoint.x > 0 ? convertPixelToPointX(mouseReleasedPoint.x) : model.getLx() * 0.05f;
				float y = mouseReleasedPoint.y > 0 ? convertPixelToPointY(mouseReleasedPoint.y) : model.getLy() * 0.025f;
				setSelectedManipulable(addCloud(x, y, model.getLx() * 0.3f, model.getLx() * 0.1f, 0));
				notifyManipulationListeners(null, ManipulationEvent.OBJECT_ADDED);
				repaint();
			}
		};
		a.putValue(Action.NAME, "Cloud");
		a.putValue(Action.SHORT_DESCRIPTION, "Insert a cloud where the mouse last clicked");
		getActionMap().put("Insert Cloud", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setSelectedManipulable(addThermometer(mouseReleasedPoint.x > 0 ? convertPixelToPointX(mouseReleasedPoint.x) : model.getLx() * 0.5f, mouseReleasedPoint.y > 0 ? convertPixelToPointY(mouseReleasedPoint.y) : model.getLy() * 0.5f));
				notifyManipulationListeners(null, ManipulationEvent.SENSOR_ADDED);
				repaint();
			}
		};
		a.putValue(Action.NAME, "Thermometer");
		a.putValue(Action.SHORT_DESCRIPTION, "Insert a thermometer where the mouse last clicked");
		getActionMap().put("Insert Thermometer", a);

	}

	public void setMouseReadType(byte mouseReadType) {
		this.mouseReadType = mouseReadType;
	}

	public byte getMouseReadType() {
		return mouseReadType;
	}

	public void setHeatMapType(byte heatMapType) {
		this.heatMapType = heatMapType;
		switch (heatMapType) {
		case HEATMAP_NONE:
			lightColor = new Color(0, 0, 0, 128);
			break;
		case HEATMAP_TEMPERATURE:
			lightColor = new Color(255, 255, 255, 128);
			break;
		case HEATMAP_THERMAL_ENERGY:
			lightColor = new Color(255, 255, 255, 128);
			break;
		}
	}

	public byte getHeatMapType() {
		return heatMapType;
	}

	public void setColorPaletteType(byte colorPaletteType) {
		this.colorPaletteType = colorPaletteType;
		temperatureRenderer = new ScalarDistributionRenderer(ColorPalette.getRgbArray(colorPaletteType), temperatureRenderer == null ? 0 : temperatureRenderer.getMinimum(), temperatureRenderer == null ? 40 : temperatureRenderer.getMaximum());
		thermalEnergyRenderer = new ScalarDistributionRenderer(ColorPalette.getRgbArray(colorPaletteType), thermalEnergyRenderer == null ? 0 : thermalEnergyRenderer.getMinimum(), thermalEnergyRenderer == null ? 40 : thermalEnergyRenderer.getMaximum());
		colorPalette = new ColorPalette(ColorPalette.getRgbArray(colorPaletteType));
	}

	public byte getColorPaletteType() {
		return colorPaletteType;
	}

	public void setActionMode(byte mode) {
		resetMousePoints();
		setSelectedManipulable(null);
		actionMode = mode;
		switch (mode) {
		case SELECT_MODE:
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case RECTANGLE_MODE:
		case ELLIPSE_MODE:
		case POLYGON_MODE:
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			break;
		case THERMOMETER_MODE:
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			break;
		case HEATING_MODE:
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			// in case the engine hasn't been initialized, call the following to set the stage
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			break;
		}
		repaint();
	}

	public byte getActionMode() {
		return actionMode;
	}

	public void setTemperatureIncrement(float temperatureIncrement) {
		this.temperatureIncrement = temperatureIncrement;
	}

	public float getTemperatureIncrement() {
		return temperatureIncrement;
	}

	public void clear() {
		textBoxes.clear();
		if (pictures != null)
			pictures.clear();
	}

	public void addTextBox(TextBox t) {
		textBoxes.add(t);
		repaint();
	}

	public void removeTextBox(TextBox t) {
		textBoxes.remove(t);
		repaint();
	}

	public TextBox addText(String text, float x, float y) {
		TextBox t = new TextBox(new Rectangle2D.Float(), text, x, y);
		addTextBox(t);
		return t;
	}

	public int getTextBoxCount() {
		return textBoxes.size();
	}

	public TextBox getTextBox(int i) {
		if (i < 0 || i >= textBoxes.size())
			return null;
		return textBoxes.get(i);
	}

	public TextBox getTextBoxByUid(String uid) {
		if (uid == null)
			return null;
		for (TextBox t : textBoxes) {
			if (uid.equals(t.getUid()))
				return t;
		}
		return null;
	}

	public void addPicture(Icon image, int x, int y) {
		if (pictures == null)
			pictures = new ArrayList<Picture>();
		pictures.add(new Picture(image, x, y));
	}

	public int getPictureCount() {
		if (pictures == null)
			return 0;
		return pictures.size();
	}

	public Picture getPicture(int i) {
		if (pictures == null)
			return null;
		if (i < 0 || i >= pictures.size())
			return null;
		return pictures.get(i);
	}

	public Cloud addCloud(float x, float y, float w, float h, float speed) {
		Cloud c = new Cloud(new Rectangle2D.Float(0, 0, w, h));
		c.setX(x);
		c.setY(y);
		c.setSpeed(speed);
		model.addCloud(c);
		return c;
	}

	public void removeCloud(Cloud c) {
		model.removeCloud(c);
		repaint();
	}

	public void addManipulationListener(ManipulationListener l) {
		if (!manipulationListeners.contains(l))
			manipulationListeners.add(l);
	}

	public void removeManipulationListener(ManipulationListener l) {
		manipulationListeners.remove(l);
	}

	public void notifyManipulationListeners(Manipulable m, byte type) {
		if (manipulationListeners.isEmpty())
			return;
		ManipulationEvent e = new ManipulationEvent(this, m, type);
		for (ManipulationListener l : manipulationListeners) {
			l.manipulationOccured(e);
		}
	}

	public void addGraphListener(GraphListener l) {
		if (!graphListeners.contains(l))
			graphListeners.add(l);
	}

	public void removeGraphListener(GraphListener l) {
		graphListeners.remove(l);
	}

	public void notifyGraphListeners(byte eventType) {
		if (graphListeners.isEmpty())
			return;
		GraphEvent e = new GraphEvent(this);
		for (GraphListener l : graphListeners) {
			switch (eventType) {
			case GraphEvent.GRAPH_CLOSED:
				l.graphClosed(e);
				break;
			case GraphEvent.GRAPH_OPENED:
				l.graphOpened(e);
				break;
			}
		}
	}

	public void setModel(Model2D model) {
		this.model = model;
		nx = model.getTemperature().length;
		ny = model.getTemperature()[0].length;
	}

	public void reset() {
		runToggle = false;
		setSelectedManipulable(null);
		setTime(0);
		if (graphRenderer != null) {
			graphRenderer.setYmin(getMinimumTemperature());
			graphRenderer.setYmax(getMaximumTemperature());
		}
	}

	public void toggleRun() {
		runToggle = !runToggle;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public void setFrankOn(boolean b) {
		frankOn = b;
	}

	public boolean isFrankOn() {
		return frankOn;
	}

	public void setRulerOn(boolean b) {
		rulerRenderer = b ? new RulerRenderer() : null;
		if (b)
			rulerRenderer.setSize(xmin, xmax, ymin, ymax);
	}

	public boolean isRulerOn() {
		return rulerRenderer != null;
	}

	public void setGridOn(boolean b) {
		showGrid = b;
		if (b && gridRenderer == null)
			gridRenderer = new GridRenderer(nx, ny);
	}

	public boolean isGridOn() {
		return showGrid;
	}

	public void setGridSize(int gridSize) {
		if (gridRenderer == null)
			gridRenderer = new GridRenderer(nx, ny);
		gridRenderer.setGridSize(gridSize);
	}

	public int getGridSize() {
		if (gridRenderer == null)
			return 10;
		return gridRenderer.getGridSize();
	}

	public void setColorPaletteOn(boolean b) {
		showColorPalette = b;
	}

	public boolean isColorPaletteOn() {
		return showColorPalette;
	}

	/** relative to the width and height of the view */
	public void setColorPaletteRectangle(float rx, float ry, float rw, float rh) {
		colorPalette.setRect(rx, ry, rw, rh);
	}

	/** relative to the width and height of the view */
	public Rectangle2D.Float getColorPaletteRectangle() {
		return colorPalette.getRect();
	}

	public Color getTemperatureColor(float value) {
		return new Color(temperatureRenderer.getColor(value));
	}

	public void setGraphOn(boolean b) {
		if (b && model.getThermometers().isEmpty()) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(View2D.this, "No graph can be shown because there is no thermometer.");
				}
			});
			notifyManipulationListeners(null, ManipulationEvent.GRAPH);
			return;
		}
		showGraph = b;
	}

	public boolean isGraphOn() {
		return showGraph;
	}

	public void setGraphXLabel(String xLabel) {
		graphRenderer.setLabelX(xLabel);
	}

	public String getGraphXLabel() {
		return graphRenderer.getLabelX();
	}

	public void setGraphYLabel(String yLabel) {
		graphRenderer.setLabelY(yLabel);
	}

	public String getGraphYLabel() {
		return graphRenderer.getLabelY();
	}

	public void setVectorStroke(Stroke s) {
		if (vectorFieldRenderer == null)
			vectorFieldRenderer = new VectorDistributionRenderer(this, nx, ny);
		vectorFieldRenderer.setStroke(s);
	}

	public void setVelocityOn(boolean b) {
		showVelocity = b;
		if (b && vectorFieldRenderer == null)
			vectorFieldRenderer = new VectorDistributionRenderer(this, nx, ny);
	}

	public boolean isVelocityOn() {
		return showVelocity;
	}

	public void setHeatFluxArrowsOn(boolean b) {
		showHeatFluxArrows = b;
		if (b && vectorFieldRenderer == null)
			vectorFieldRenderer = new VectorDistributionRenderer(this, nx, ny);
	}

	public boolean isHeatFluxArrowsOn() {
		return showHeatFluxArrows;
	}

	public void setHeatFluxLinesOn(boolean b) {
		showHeatFluxLines = b;
		if (b && heatFluxLines == null)
			heatFluxLines = new FieldLines();
	}

	public boolean isHeatFluxLinesOn() {
		return showHeatFluxLines;
	}

	public void setVectorFieldSpacing(int spacing) {
		if (vectorFieldRenderer == null)
			vectorFieldRenderer = new VectorDistributionRenderer(this, nx, ny);
		vectorFieldRenderer.setSpacing(spacing);
	}

	public int getVectorFieldSpacing() {
		if (vectorFieldRenderer == null)
			return 5;
		return vectorFieldRenderer.getSpacing();
	}

	public void setStreamlineOn(boolean b) {
		showStreamLines = b;
		if (b && streamlines == null) {
			streamlines = new FieldLines();
			streamlines.setColor(Color.white);
		}
	}

	public FieldLines getStreamlines() {
		if (streamlines == null)
			streamlines = new FieldLines();
		return streamlines;
	}

	public boolean isStreamlineOn() {
		return showStreamLines;
	}

	public void setIsothermOn(boolean b) {
		showIsotherm = b;
		if (b) {
			if (isotherms == null)
				isotherms = new ContourMap();
		} else {
			isotherms = null;
		}
	}

	public boolean isIsothermOn() {
		return showIsotherm;
	}

	public void setIsothermResolution(float resolution) {
		if (isotherms != null)
			isotherms.setResolution(resolution);
	}

	public float getIsothermResolution() {
		if (isotherms == null)
			return 5;
		return isotherms.getResolution();
	}

	public void setSeeThrough(boolean b) {
		List<Part> parts = model.getParts();
		synchronized (parts) {
			for (Part p : parts) {
				p.setFilled(!b);
			}
		}
	}

	public boolean getSeeThrough() {
		if (model.getPartCount() == 0)
			return false;
		List<Part> parts = model.getParts();
		synchronized (parts) {
			for (Part p : parts) {
				if (p.isFilled())
					return false;
			}
		}
		return true;
	}

	public void setClockOn(boolean b) {
		clockOn = b;
	}

	public boolean isClockOn() {
		return clockOn;
	}

	public void setSmooth(boolean smooth) {
		temperatureRenderer.setSmooth(smooth);
	}

	public boolean isSmooth() {
		return temperatureRenderer.isSmooth();
	}

	public void setLabelFont(Font font) {
		labelFont = font;
	}

	public void setSensorReadingFont(Font font) {
		sensorReadingFont = font;
	}

	public void setMinimumTemperature(float min) {
		temperatureRenderer.setMinimum(min);
		thermalEnergyRenderer.setMinimum(min);
		graphRenderer.setYmin(min);
	}

	public float getMinimumTemperature() {
		return temperatureRenderer.getMinimum();
	}

	public void setMaximumTemperature(float max) {
		temperatureRenderer.setMaximum(max);
		thermalEnergyRenderer.setMaximum(max);
		graphRenderer.setYmax(max);
	}

	public float getMaximumTemperature() {
		return temperatureRenderer.getMaximum();
	}

	public float getBackgroundTemperature() {
		return model.getBackgroundTemperature();
	}

	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	private void cut() {
		if (selectedManipulable != null) {
			copiedManipulable = selectedManipulable;
			if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected object?", "Delete Object", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				notifyManipulationListeners(selectedManipulable, ManipulationEvent.DELETE);
				setSelectedManipulable(null);
			}
		}
	}

	private void copy() {
		copiedManipulable = selectedManipulable;
	}

	private void paste() {
		if (copiedManipulable instanceof Part) {
			Part p = (Part) copiedManipulable;
			model.addPart(p.duplicate(convertPixelToPointX(mouseReleasedPoint.x), convertPixelToPointY(mouseReleasedPoint.y)));
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			model.refreshMaterialPropertyArrays();
			model.setInitialTemperature();
		} else if (copiedManipulable instanceof Thermometer) {
			addThermometer(convertPixelToPointX(mouseReleasedPoint.x), convertPixelToPointY(mouseReleasedPoint.y));
		} else if (copiedManipulable instanceof TextBox) {
			addTextBox((TextBox) copiedManipulable.duplicate(convertPixelToPointX(mouseReleasedPoint.x), model.getLy() - convertPixelToPointY(mouseReleasedPoint.y)));
		} else if (copiedManipulable instanceof Cloud) {
			model.addCloud((Cloud) copiedManipulable.duplicate(convertPixelToPointX(mouseReleasedPoint.x), convertPixelToPointY(mouseReleasedPoint.y)));
		}
		notifyManipulationListeners(copiedManipulable, ManipulationEvent.PROPERTY_CHANGE);
		repaint();
	}

	private void createPopupMenu() {

		if (popupMenu != null)
			return;

		popupMenu = new JPopupMenu();
		popupMenu.setInvoker(this);

		popupMenu.add(copyAction);
		popupMenu.add(cutAction);
		popupMenu.add(pasteAction);
		popupMenu.addSeparator();

		JMenuItem mi = new JMenuItem("Properties...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createDialog(selectedManipulable != null ? selectedManipulable : model, true);
			}
		});
		popupMenu.add(mi);

		mi = new JMenuItem("View Options...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createDialog(selectedManipulable != null ? selectedManipulable : View2D.this, false);
			}
		});
		popupMenu.add(mi);

		mi = new JMenuItem("Task Manager...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Action a = getActionMap().get("Task_Manager");
				if (a != null)
					a.actionPerformed(e);
			}
		});
		popupMenu.add(mi);
		popupMenu.addSeparator();

		JMenu subMenu = new JMenu("Help");
		popupMenu.add(subMenu);

		mi = new JMenuItem("Script Console...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Action a = getActionMap().get("Script");
				if (a != null)
					a.actionPerformed(e);
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Keyboard Shortcuts...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.showKeyboardShortcuts(JOptionPane.getFrameForComponent(View2D.this));
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("About...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.showAbout(JOptionPane.getFrameForComponent(View2D.this));
			}
		});
		subMenu.add(mi);

	}

	public void createDialog(Object o, boolean forModel) {
		JDialog d = forModel ? dialogFactory.createModelDialog(o) : dialogFactory.createViewDialog(o);
		if (d != null)
			d.setVisible(true);
	}

	public void setArea(float xmin, float xmax, float ymin, float ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		if (rulerRenderer != null)
			rulerRenderer.setSize(xmin, xmax, ymin, ymax);
	}

	private Graphics2D createGraphics2D() {
		int w = getWidth();
		int h = getHeight();
		Graphics2D g;
		if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
			bimg = (BufferedImage) createImage(w, h);
			// The following code doesn't seem to make any difference compared with the above line
			// GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			// GraphicsDevice gd = ge.getDefaultScreenDevice();
			// GraphicsConfiguration gc = gd.getDefaultConfiguration();
			// bimg = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
		}
		g = bimg.createGraphics();
		g.setBackground(getBackground());
		g.clearRect(0, 0, w, h);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		return g;
	}

	/* Need to use this old double-buffering technique in order to avoid flickering when run as an applet on the Mac */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = createGraphics2D();
		draw(g2);
		g2.dispose();
		if (bimg != null)
			g.drawImage(bimg, 0, 0, this);
	}

	private void draw(Graphics2D g) {
		int w = getWidth();
		int h = getHeight();
		Stroke stroke = g.getStroke();
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		switch (heatMapType) {
		case HEATMAP_TEMPERATURE:
			drawTemperatureField(g);
			break;
		case HEATMAP_THERMAL_ENERGY:
			drawThermalEnergyField(g);
			break;
		}
		drawParts(g);
		drawClouds(g);
		drawTextBoxes(g);
		drawPictures(g);
		if (isotherms != null) {
			g.setStroke(thinStroke);
			isotherms.render(g, getSize(), model.getTemperature());
		}
		if (showStreamLines && streamlines != null) {
			g.setStroke(thinStroke);
			streamlines.render(g, getSize(), model.getXVelocity(), model.getYVelocity());
		}
		if (showHeatFluxLines && heatFluxLines != null) {
			g.setStroke(thinStroke);
			heatFluxLines.render(g, getSize(), model.getTemperature(), -1);
		}
		if (selectedManipulable != null) {
			if (selectedManipulable instanceof Thermometer) {
				Thermometer t = (Thermometer) selectedManipulable;
				Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
				int wt = convertLengthToPixelX(r.width);
				int ht = convertLengthToPixelY(r.height);
				int xt = convertPointToPixelX(t.getX()) - wt / 2;
				int yt = convertPointToPixelY(t.getY()) - ht / 2;
				g.setColor(Color.yellow);
				g.fillRect(xt - 3, yt - 3, wt + 5, ht + 5);
			} else if (selectedManipulable instanceof TextBox) { // textboxes are not resizable
			} else {
				g.setStroke(thinStroke);
				for (Rectangle r : handle) {
					if (r.x != 0 || r.y != 0) {
						g.setColor(Color.yellow);
						g.fill(r);
						g.setColor(Color.black);
						g.draw(r);
					}
				}
			}
		}
		if (mouseBeingDragged) {
			if (movingShape != null) {
				g.setColor(Color.white);
				g.setStroke(dashed);
				movingShape.render(g);
			}
		}
		if (showGrid && gridRenderer != null)
			gridRenderer.render(this, g);
		if (rulerRenderer != null) {
			rulerRenderer.render(this, g);
		}
		if (showColorPalette && heatMapType != HEATMAP_NONE) {
			g.setStroke(thinStroke);
			switch (heatMapType) {
			case HEATMAP_TEMPERATURE:
				colorPalette.render(this, g, temperatureRenderer.getMaximum(), temperatureRenderer.getMinimum());
				break;
			case HEATMAP_THERMAL_ENERGY:
				colorPalette.render(this, g, thermalEnergyRenderer.getMaximum(), thermalEnergyRenderer.getMinimum());
				break;
			}
		}
		if (showVelocity)
			vectorFieldRenderer.renderVectors(model.getXVelocity(), model.getYVelocity(), this, g);
		if (showHeatFluxArrows)
			vectorFieldRenderer.renderHeatFlux(model.getTemperature(), model.getConductivity(), this, g);
		drawPhotons(g);
		showSunOrMoon(g);
		drawThermometers(g);
		if (showGraph && !model.getThermometers().isEmpty()) {
			graphRenderer.setDrawFrame(true);
			if (model.getTime() > graphRenderer.getXmax())
				graphRenderer.doubleXmax();
			synchronized (model.getThermometers()) {
				for (Thermometer t : model.getThermometers()) {
					if (t.getCurrentData() > graphRenderer.getYmax() + 2) { // allow 2 degrees of overshot above max
						graphRenderer.increaseYmax();
					} else if (t.getCurrentData() < graphRenderer.getYmin() - 2) { // allow 2 degrees of overshot below min
						graphRenderer.decreaseYmin();
					}
					graphRenderer.render(this, g, t.getData(), t.getLabel(), selectedManipulable == t);
				}
			}
		}
		if (clockOn) {
			g.setFont(smallFont);
			g.setColor(getContrastColor(w - 68, 16));
			g.drawString(MiscUtil.formatTime((int) time), w - 68, 16);
		}

		g.setStroke(dashed);
		switch (actionMode) {
		case RECTANGLE_MODE:
			g.setColor(TRANSLUCENT_GRAY);
			g.fill(rectangle);
			g.setColor(Color.white);
			g.draw(rectangle);
			break;
		case ELLIPSE_MODE:
			g.setColor(TRANSLUCENT_GRAY);
			g.fill(ellipse);
			g.setColor(Color.white);
			g.draw(ellipse);
			break;
		case POLYGON_MODE:
			g.setColor(TRANSLUCENT_GRAY);
			g.fill(polygon);
			g.setColor(Color.white);
			g.draw(polygon);
			if (mouseMovedPoint.x >= 0 && mouseMovedPoint.y >= 0 && mouseReleasedPoint.x >= 0 && mouseReleasedPoint.y >= 0) {
				g.setColor(Color.green);
				g.drawLine(mouseMovedPoint.x, mouseMovedPoint.y, mouseReleasedPoint.x, mouseReleasedPoint.y);
				int np = polygon.npoints;
				if (np > 1) { // draw a dotted line to show what will be a complete polygon if double-clicking
					double dx = polygon.xpoints[0] - mouseMovedPoint.x;
					double dy = polygon.ypoints[0] - mouseMovedPoint.y;
					double distance = Math.hypot(dx, dy);
					int n = (int) Math.round(distance * 0.1);
					dx = dx / n;
					dy = dy / n;
					for (int i = 0; i < n + 1; i++)
						g.fillOval((int) (mouseMovedPoint.x + dx * i), (int) (mouseMovedPoint.y + dy * i), 2, 2);
				}
			}
			break;
		}

		if (!model.getThermostats().isEmpty()) {
			for (Thermostat t : model.getThermostats()) {
				thermostatRenderer.render(t, this, g);
			}
		}

		g.setStroke(stroke);
		if (frankOn) {
			int dy = rulerRenderer != null ? 30 : 15;
			drawFrank(g, getWidth() - 84, getHeight() - dy);
		}

		if (actionMode == SELECT_MODE) { // draw field reader last
			switch (mouseReadType) {
			case MOUSE_READ_TEMPERATURE:
				float pointValue = model.getTemperatureAt(convertPixelToPointX(mouseMovedPoint.x), convertPixelToPointY(mouseMovedPoint.y));
				drawMouseReadString(g, TEMPERATURE_FORMAT.format(pointValue) + " " + '\u2103');
				break;
			case MOUSE_READ_THERMAL_ENERGY:
				pointValue = model.getThermalEnergyAt(convertPixelToPointX(mouseMovedPoint.x), convertPixelToPointY(mouseMovedPoint.y));
				drawMouseReadString(g, TEMPERATURE_FORMAT.format(pointValue) + " J");
				break;
			case MOUSE_READ_VELOCITY:
				float[] velocity = model.getVelocityAt(convertPixelToPointX(mouseMovedPoint.x), convertPixelToPointY(mouseMovedPoint.y));
				drawMouseReadString(g, "(" + VELOCITY_FORMAT.format(velocity[0]) + ", " + VELOCITY_FORMAT.format(-velocity[1]) + ") m/s");
				break;
			case MOUSE_READ_DEFAULT:
				if (showGraph) {
					String dataInfo = getGraphDataAt(mouseMovedPoint.x, mouseMovedPoint.y);
					if (dataInfo != null)
						drawMouseReadString(g, dataInfo);
				}
				break;
			}
		}

		if (errorMessage != null) {
			g.setColor(Color.red);
			g.setFont(new Font("Arial", Font.BOLD, 30));
			FontMetrics fm = g.getFontMetrics();
			g.drawString(errorMessage, w / 2 - fm.stringWidth(errorMessage) / 2, h / 2);
			notifyManipulationListeners(null, ManipulationEvent.STOP);
		}

	}

	private void showSunOrMoon(Graphics g) {
		if (model.isSunny()) {
			if (model.getSunAngle() <= 0 || model.getSunAngle() > Math.PI) {
				if (moon == null)
					moon = Symbol.get("Moon");
				moon.paintIcon(this, g, getWidth() - moon.getIconWidth() * 2, moon.getIconHeight() + 10);
			} else {
				if (sun == null)
					sun = Symbol.get("Sun");
				sun.paintIcon(this, g, getWidth() - sun.getIconWidth() * 2, sun.getIconHeight() + 10);
			}
		}
	}

	private void drawClouds(Graphics2D g) {
		if (model.getClouds().isEmpty())
			return;
		g.setStroke(thickStroke);
		Rectangle2D.Float r = new Rectangle2D.Float();
		boolean daytime = model.isSunny() && model.getSunAngle() > 0 && model.getSunAngle() < Math.PI;
		synchronized (model.getClouds()) {
			for (Cloud c : model.getClouds()) {
				r.x = convertPointToPixelX(c.getX());
				r.y = convertPointToPixelY(c.getY());
				r.width = convertLengthToPixelX(c.getWidth());
				r.height = convertLengthToPixelY(c.getHeight());
				Area a = Cloud.getShape(r);
				g.setColor(daytime ? c.getColor() : c.getColor().darker());
				g.fill(a);
				g.setColor(selectedManipulable == c ? Color.yellow : Color.gray);
				g.draw(a);
				if (model.isRunning() && c.getSpeed() != 0 && c == selectedManipulable) {
					HandleSetter.setRects(this, selectedManipulable, handle);
				}
				if (c.getLabel() != null) {
					g.setFont(labelFont);
					g.setColor(getContrastColor((int) r.getCenterX(), (int) r.getCenterY()));
					String label = c.getLabel();
					FontMetrics fm = g.getFontMetrics();
					g.drawString(label, (int) r.getCenterX() - fm.stringWidth(label) / 2, (int) r.getCenterY() + fm.getHeight());
				}
			}
		}
	}

	private void drawMouseReadString(Graphics2D g, String s) {
		g.setFont(sensorReadingFont);
		FontMetrics fm = g.getFontMetrics();
		int stringWidth = fm.stringWidth(s);
		g.setStroke(thinStroke);
		int x2 = mouseMovedPoint.x;
		boolean nearRightBorder = x2 > getWidth() - 50;
		x2 += nearRightBorder ? -30 : 20;
		g.setColor(Color.black);
		g.fillRoundRect(x2 - 5, mouseMovedPoint.y - 14, stringWidth + 10, 20, 8, 8);
		g.drawLine(nearRightBorder ? x2 + stringWidth + 5 : x2 - 5, mouseMovedPoint.y - 5, mouseMovedPoint.x, mouseMovedPoint.y);
		g.fillOval(mouseMovedPoint.x - 2, mouseMovedPoint.y - 2, 4, 4);
		g.setColor(Color.white);
		g.drawString(s, x2, mouseMovedPoint.y);
	}

	void setErrorMessage(String message) {
		this.errorMessage = message;
	}

	Color getContrastColor(int x, int y) {
		switch (heatMapType) {
		case HEATMAP_TEMPERATURE:
			return new Color(~temperatureRenderer.getRGB(x, y));
		case HEATMAP_THERMAL_ENERGY:
			return new Color(~thermalEnergyRenderer.getRGB(x, y));
		default:
			return Color.black;
		}
	}

	private void drawThermometers(Graphics2D g) {
		List<Thermometer> thermometers = model.getThermometers();
		if (thermometers.isEmpty())
			return;
		g.setStroke(thinStroke);
		Symbol.Thermometer s = (Symbol.Thermometer) Symbol.get("Thermometer");
		float w = Thermometer.RELATIVE_WIDTH * model.getLx();
		float h = Thermometer.RELATIVE_HEIGHT * model.getLy();
		s.setIconWidth((int) (w * getWidth() / (xmax - xmin)));
		s.setIconHeight((int) (h * getHeight() / (ymax - ymin)));
		float lx = s.getIconWidth();
		float ly = s.getIconHeight();
		float temp;
		String str;
		g.setFont(sensorReadingFont);
		int x, y;
		float rx, ry;
		int ix, iy;
		synchronized (thermometers) {
			for (Thermometer t : thermometers) {
				Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
				r.width = w;
				r.height = h;
				rx = (t.getX() - xmin) / (xmax - xmin);
				ry = (t.getY() - ymin) / (ymax - ymin);
				if (rx >= 0 && rx < 1 && ry >= 0 && ry < 1) {
					x = (int) (rx * getWidth() - lx * 0.5f);
					y = (int) (ry * getHeight() - ly * 0.5f);
					ix = Math.round(nx * rx);
					iy = Math.round(ny * ry);
					temp = model.getTemperature()[ix][iy];
					if (!Float.isNaN(temp)) {
						str = TEMPERATURE_FORMAT.format(temp) + '\u2103';
						centerString(str, g, x + s.getIconWidth() / 2, y - 5, true);
						if (t.getLabel() != null)
							centerString(t.getLabel(), g, x + s.getIconWidth() / 2, y + s.getIconHeight() + 12, false);
						s.setValue(Math.round((temp - getMinimumTemperature()) / (getMaximumTemperature() - getMinimumTemperature()) * (s.getIconHeight() - 10)));
					}
					s.paintIcon(this, g, x, y);
				}
			}
		}
	}

	private static void centerString(String s, Graphics2D g, int x, int y, boolean box) {
		FontMetrics fm = g.getFontMetrics();
		int stringWidth = fm.stringWidth(s);
		int x2 = x - stringWidth / 2;
		if (box) {
			g.setColor(Color.gray);
			g.fillRoundRect(x2 - 5, y - fm.getAscent(), stringWidth + 10, fm.getHeight(), 8, 8);
		}
		g.setColor(Color.white);
		g.drawString(s, x2, y);
	}

	private Color getPartColor(Part p, Color proposedColor) {
		if (p.getPower() > 0 && p.getPowerSwitch())
			return new Color(0xFFFF00);
		if (p.getPower() < 0 && p.getPowerSwitch())
			return new Color(0xB0C4DE);
		if (p.getConstantTemperature())
			return new Color(temperatureRenderer.getColor(p.getTemperature()));
		return proposedColor;
	}

	private void setPaint(Graphics2D g, Texture texture, boolean filled) {
		Color bg = new Color(((filled ? texture.getAlpha() : 0) << 24) | (0x00ffffff & texture.getBackground()), true);
		Color fg = new Color((texture.getAlpha() << 24) | (0x00ffffff & texture.getForeground()), true);
		g.setPaint(TextureFactory.createPattern(texture.getStyle(), texture.getCellWidth(), texture.getCellHeight(), fg, bg));
	}

	private void drawParts(Graphics2D g) {
		List<Part> parts = model.getParts();
		if (parts.isEmpty())
			return;
		Stroke oldStroke = g.getStroke();
		g.setStroke(moderateStroke);
		synchronized (parts) {
			for (Part p : parts) {
				if (!p.isVisible())
					continue;
				Shape s = p.getShape();
				if (s instanceof Ellipse2D.Float) {
					Ellipse2D.Float e = (Ellipse2D.Float) s;
					int x = convertPointToPixelX(e.x);
					int y = convertPointToPixelY(e.y);
					int w = convertLengthToPixelX(e.width);
					int h = convertLengthToPixelY(e.height);
					FillPattern fillPattern = p.getFillPattern();
					if (fillPattern instanceof ColorFill) {
						if (p.isFilled()) {
							g.setColor(getPartColor(p, ((ColorFill) fillPattern).getColor()));
							g.fillOval(x, y, w, h);
						} else {
							drawStatus(g, p, x + w / 2, y + h / 2);
						}
					} else if (fillPattern instanceof Texture) {
						setPaint(g, (Texture) fillPattern, p.isFilled());
						g.fillOval(x, y, w, h);
					}
					g.setColor(Color.black);
					g.drawOval(x - 1, y - 1, w + 2, h + 2);
					String label = p.getLabel();
					if (label != null) {
						String partLabel = p.getLabel(label, model);
						if (partLabel != null)
							label = partLabel;
						g.setFont(labelFont);
						FontMetrics fm = g.getFontMetrics();
						int labelWidth = fm.stringWidth(label);
						float x0 = x + 0.5f * w;
						float y0 = y + 0.5f * h;
						float x1 = x0 - labelWidth / 2;
						float y1 = y0 + fm.getHeight() / 4;
						g.setColor(getContrastColor(Math.round(x1), Math.round(y1)));
						if (w < h * 0.25f) {
							g.rotate(Math.PI * 0.5, x0, y0);
							g.drawString(label, x1, y1);
							g.rotate(-Math.PI * 0.5, x0, y0);
						} else {
							g.drawString(label, x1, y1);
						}
					}
				} else if (s instanceof Rectangle2D.Float) {
					Rectangle2D.Float r = (Rectangle2D.Float) s;
					int x = convertPointToPixelX(r.x);
					int y = convertPointToPixelY(r.y);
					int w = convertLengthToPixelX(r.width);
					int h = convertLengthToPixelY(r.height);
					FillPattern fp = p.getFillPattern();
					if (fp instanceof ColorFill) {
						if (p.isFilled()) {
							g.setColor(getPartColor(p, ((ColorFill) fp).getColor()));
							g.fillRect(x, y, w, h);
						} else {
							drawStatus(g, p, x + w / 2, y + h / 2);
						}
					} else if (fp instanceof Texture) {
						setPaint(g, (Texture) fp, p.isFilled());
						g.fillRect(x, y, w, h);
					}
					g.setColor(Color.black);
					g.drawRect(x - 1, y - 1, w + 2, h + 2);
					String label = p.getLabel();
					if (label != null) {
						String partLabel = p.getLabel(label, model);
						if (partLabel != null)
							label = partLabel;
						g.setFont(labelFont);
						FontMetrics fm = g.getFontMetrics();
						int labelWidth = fm.stringWidth(label);
						float x0 = x + 0.5f * w;
						float y0 = y + 0.5f * h;
						float x1 = x0 - labelWidth / 2;
						float y1 = y0 + fm.getHeight() / 4;
						g.setColor(getContrastColor(Math.round(x1), Math.round(y1)));
						if (w < h * 0.25f) {
							g.rotate(Math.PI * 0.5, x0, y0);
							g.drawString(label, x1, y1);
							g.rotate(-Math.PI * 0.5, x0, y0);
						} else {
							g.drawString(label, x1, y1);
						}
					}
				} else if (s instanceof Area) {
					if (scale == null)
						scale = new AffineTransform();
					scale.setToScale(getWidth() / (xmax - xmin), getHeight() / (ymax - ymin));
					Area area = (Area) s;
					area.transform(scale);
					FillPattern fillPattern = p.getFillPattern();
					if (fillPattern instanceof ColorFill) {
						if (p.isFilled()) {
							g.setColor(getPartColor(p, ((ColorFill) fillPattern).getColor()));
							g.fill(area);
						} else {
							Rectangle bounds = area.getBounds();
							drawStatus(g, p, (int) bounds.getCenterX(), (int) bounds.getCenterY());
						}
					} else if (fillPattern instanceof Texture) {
						setPaint(g, (Texture) fillPattern, p.isFilled());
						g.fill(area);
					}
					g.setColor(Color.black);
					g.draw(area);
					scale.setToScale((xmax - xmin) / getWidth(), (ymax - ymin) / getHeight());
					area.transform(scale);
				} else if (s instanceof Polygon2D) {
					Polygon2D q = (Polygon2D) s;
					int n = q.getVertexCount();
					if (multigon == null)
						multigon = new Polygon();
					else
						multigon.reset();
					int x, y;
					Point2D.Float v;
					int cx = 0, cy = 0;
					for (int i = 0; i < n; i++) {
						v = q.getVertex(i);
						x = convertPointToPixelX(v.x);
						y = convertPointToPixelY(v.y);
						multigon.addPoint(x, y);
						cx += x;
						cy += y;
					}
					FillPattern fp = p.getFillPattern();
					if (fp instanceof ColorFill) {
						if (p.isFilled()) {
							g.setColor(getPartColor(p, ((ColorFill) fp).getColor()));
							g.fill(multigon);
						} else {
							drawStatus(g, p, cx / multigon.npoints, cy / multigon.npoints);
						}
					} else if (fp instanceof Texture) {
						setPaint(g, (Texture) fp, p.isFilled());
						g.fill(multigon);
					}
					g.setColor(Color.black);
					g.draw(multigon);
					String label = p.getLabel();
					if (label != null) {
						String partLabel = p.getLabel(label, model);
						if (partLabel != null)
							label = partLabel;
						g.setFont(labelFont);
						FontMetrics fm = g.getFontMetrics();
						int labelWidth = fm.stringWidth(label);
						cx /= multigon.npoints;
						cy /= multigon.npoints;
						float x1 = cx - labelWidth / 2;
						float y1 = cy + fm.getHeight() / 4;
						g.setColor(getContrastColor(Math.round(x1), Math.round(y1)));
						g.drawString(label, x1, y1);
					}
				}
			}
		}
		g.setStroke(oldStroke);
	}

	private void drawStatus(Graphics2D g, Part p, int x, int y) {
		if (p.getPower() != 0) {
			String onoff = p.getPowerSwitch() ? "On" : "Off";
			g.setColor(getContrastColor(x, y));
			g.setFont(smallFont);
			FontMetrics fm = g.getFontMetrics();
			g.drawString(onoff, x - fm.stringWidth(onoff) / 2, y - 5);
		}
	}

	private void drawTextBoxes(Graphics2D g) {
		if (textBoxes.isEmpty())
			return;
		Font oldFont = g.getFont();
		Color oldColor = g.getColor();
		String s = null;
		for (TextBox x : textBoxes) {
			if (!x.isVisible())
				continue;
			g.setFont(new Font(x.getFace(), x.getStyle(), x.getSize()));
			g.setColor(x.getColor());
			s = x.getLabel();
			if (s != null) {
				s = s.replaceAll("%Prandtl", formatter.format(model.getPrandtlNumber()));
				s = s.replaceAll("%thermal_energy", "" + Math.round(model.getThermalEnergy()));
				drawStringWithLineBreaks(g, s, x);
			}
		}
		g.setFont(oldFont);
		g.setColor(oldColor);
	}

	private void drawStringWithLineBreaks(Graphics2D g, String text, TextBox t) {
		int x = convertPointToPixelX(t.getX());
		int y = getHeight() - convertPointToPixelY(t.getY());
		FontMetrics fm = g.getFontMetrics();
		int stringHeight = fm.getHeight();
		int stringWidth = 0;
		int w = 0;
		int h = 0;
		for (String line : text.split("\n")) {
			h += stringHeight;
			g.drawString(line, x, y + h);
			stringWidth = fm.stringWidth(line);
			if (stringWidth > w)
				w = stringWidth;
		}
		Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
		r.x = x - 8;
		r.y = y - 2;
		r.width = w + 16;
		r.height = h + 10;
		if (t.hasBorder()) {
			g.setStroke(moderateStroke);
			g.drawRoundRect((int) r.x, (int) r.y, (int) r.width, (int) r.height, 10, 10);
		}
		if (t.isSelected()) {
			g.setStroke(dashed);
			g.drawRoundRect((int) (r.x - 5), (int) (r.y - 5), (int) (r.width + 10), (int) (r.height + 10), 15, 15);
		}
		r.x = convertPixelToPointX((int) r.x);
		r.y = convertPixelToPointX((int) r.y);
		r.width = convertPixelToPointX((int) r.width);
		r.height = convertPixelToPointX((int) r.height);
	}

	private void drawPictures(Graphics2D g) {
		if (pictures == null || pictures.isEmpty())
			return;
		for (Picture x : pictures) {
			x.getImage().paintIcon(this, g, convertPointToPixelX(x.getX()), getHeight() - convertPointToPixelY(x.getY()));
		}
	}

	private void drawPhotons(Graphics2D g) {
		if (model.getPhotons().isEmpty())
			return;
		int x, y;
		g.setColor(lightColor);
		g.setStroke(thinStroke);
		double r;
		synchronized (model.getPhotons()) {
			for (Photon p : model.getPhotons()) {
				x = convertPointToPixelX(p.getX());
				y = convertPointToPixelY(p.getY());
				r = 1.0 / Math.hypot(p.getVx(), p.getVy());
				g.drawLine((int) (x - photonLength * p.getVx() * r), (int) (y - photonLength * p.getVy() * r), x, y);
			}
		}
	}

	private void drawTemperatureField(Graphics2D g) {
		temperatureRenderer.render(this, g, model.getTemperature());
	}

	private void drawThermalEnergyField(Graphics2D g) {
		float[][] density = model.getDensity();
		float[][] specificHeat = model.getSpecificHeat();
		float[][] temperature = model.getTemperature();
		int nx = temperature.length;
		int ny = temperature[0].length;
		if (distribution == null)
			distribution = new float[nx][ny];
		float factor = 1f / model.getMaximumHeatCapacity();
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				distribution[i][j] = factor * density[i][j] * specificHeat[i][j] * temperature[i][j];
			}
		}
		thermalEnergyRenderer.render(this, g, distribution);
	}

	private void setAnchorPointForRectangularShape(byte i, float x, float y, float w, float h) {
		switch (i) {
		case UPPER_LEFT:
			anchorPoint.setLocation(x + w, y + h);
			break;
		case UPPER_RIGHT:
			anchorPoint.setLocation(x, y + h);
			break;
		case LOWER_RIGHT:
			anchorPoint.setLocation(x, y);
			break;
		case LOWER_LEFT:
			anchorPoint.setLocation(x + w, y);
			break;
		case TOP:
			anchorPoint.setLocation(x, y + h);
			break;
		case RIGHT:
			anchorPoint.setLocation(x, y);
			break;
		case BOTTOM:
			anchorPoint.setLocation(x, y);
			break;
		case LEFT:
			anchorPoint.setLocation(x + w, y);
			break;
		}
	}

	private void selectManipulable(int x, int y) {
		setSelectedManipulable(null);
		float rx = convertPixelToPointX(x);
		float ry = convertPixelToPointY(y);
		if (!model.getThermometers().isEmpty()) { // always prefer to select a thermometer
			synchronized (model.getThermometers()) {
				for (Thermometer t : model.getThermometers()) {
					if (t.contains(rx, ry)) {
						setSelectedManipulable(t);
						return;
					}
				}
			}
		}
		if (!model.getClouds().isEmpty()) {
			synchronized (model.getClouds()) {
				for (Cloud c : model.getClouds()) {
					if (c.contains(rx, ry)) {
						setSelectedManipulable(c);
						return;
					}
				}
			}
		}
		if (!textBoxes.isEmpty()) {
			synchronized (textBoxes) {
				for (TextBox t : textBoxes) {
					if (t.contains(rx, ry)) {
						setSelectedManipulable(t);
						return;
					}
				}
			}
		}
		int n = model.getParts().size();
		if (n > 0) {
			synchronized (model.getParts()) {
				for (int i = n - 1; i >= 0; i--) { // later-added has higher priority
					Part p = model.getPart(i);
					if (p.contains(rx, ry)) {
						setSelectedManipulable(p);
						return;
					}
				}
			}
		}
	}

	public void setSelectedManipulable(Manipulable m) {
		if (selectedManipulable != null)
			selectedManipulable.setSelected(false);
		selectedManipulable = m;
		if (selectedManipulable != null) {
			selectedManipulable.setSelected(true);
			Shape shape = selectedManipulable.getShape();
			boolean b = false;
			if (shape instanceof Ellipse2D.Float) {
				Ellipse2D.Float e = (Ellipse2D.Float) shape;
				b = e.width < (xmax - xmin) / nx + 0.01f || e.height < (ymax - ymin) / ny + 0.01f;
			}
			if (!b)
				HandleSetter.setRects(this, selectedManipulable, handle);
		}
	}

	public Manipulable getSelectedManipulable() {
		return selectedManipulable;
	}

	public Manipulable getBufferedManipulable() {
		return copiedManipulable;
	}

	private void translateManipulableBy(Manipulable m, float dx, float dy) {
		Shape s = m.getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x += dx;
			r.y += dy;
			if (m instanceof Thermometer) {
				if (r.x + r.width / 2 < xmin + dx)
					r.x = xmin + dx - r.width / 2;
				else if (r.x + r.width / 2 > xmax - dx)
					r.x = xmax - dx - r.width / 2;
				if (r.y + r.height / 2 < ymin + dy)
					r.y = ymin + dy - r.height / 2;
				else if (r.y + r.height / 2 > ymax - dy)
					r.y = ymax - dy - r.height / 2;
			} else if (m instanceof TextBox) {
				((TextBox) m).translateBy(dx, -dy);
			}
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float r = (Ellipse2D.Float) s;
			r.x += dx;
			r.y += dy;
		} else if (s instanceof Area) {
			if (m instanceof Cloud) {
				((Cloud) m).translateBy(dx, dy);
			}
		} else if (s instanceof Polygon2D) {
			((Polygon2D) s).translateBy(dx, dy);
		}
		notifyManipulationListeners(m, ManipulationEvent.TRANSLATE);
	}

	private void translateManipulableTo(Manipulable m, float x, float y) {
		Shape s = m.getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x = x - r.width / 2;
			r.y = y - r.height / 2;
			if (m instanceof TextBox)
				((TextBox) m).setLocation(r.x + convertPixelToPointX(8), model.getLy() - r.y - convertPixelToPointY(2));
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float r = (Ellipse2D.Float) s;
			r.x = x - r.width / 2;
			r.y = y - r.height / 2;
		} else if (s instanceof Area) {
			if (m instanceof Cloud)
				((Cloud) m).setLocation((float) (x - s.getBounds2D().getCenterX()), (float) (y - s.getBounds2D().getCenterY()));
		} else if (s instanceof Polygon2D) {
			Shape shape = movingShape.getShape();
			if (shape instanceof Polygon) {
				Polygon polygon = (Polygon) shape;
				float xc = 0, yc = 0;
				for (int i = 0; i < polygon.npoints; i++) {
					xc += polygon.xpoints[i];
					yc += polygon.ypoints[i];
				}
				xc = convertPixelToPointX((int) (xc / polygon.npoints));
				yc = convertPixelToPointY((int) (yc / polygon.npoints));
				Polygon2D p = (Polygon2D) s;
				Point2D.Float center = p.getCenter();
				p.translateBy((float) (xc - center.x), (float) (yc - center.y));
			}
		}
		notifyManipulationListeners(m, ManipulationEvent.TRANSLATE);
	}

	// (x0, y0) is the coordinate of the upper-left corner of an Area (if shape is an Area)
	void resizeManipulableTo(Manipulable m, float x, float y, float w, float h, float x0, float y0) {
		Shape s = m.getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.setFrame(x, y, w, h);
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float r = (Ellipse2D.Float) s;
			r.setFrame(x, y, w, h);
		} else if (s instanceof Area) {
			if (m instanceof Cloud) {
				Cloud c = (Cloud) m;
				c.setDimension(w, h);
				c.setX(x0 + x);
				c.setY(y0 + y);
			}
		}
		notifyManipulationListeners(m, ManipulationEvent.RESIZE);
	}

	private void processKeyPressed(KeyEvent e) {
		if (runHeatingThread) {
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				cooling = true;
			}
		}
		if (selectedManipulable != null && selectedManipulable.isDraggable()) {
			boolean keyDown = IS_MAC ? e.isMetaDown() : e.isControlDown();
			float delta = keyDown ? 1 : 5;
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				translateManipulableBy(selectedManipulable, -delta * (xmax - xmin) / getWidth(), 0);
				break;
			case KeyEvent.VK_RIGHT:
				translateManipulableBy(selectedManipulable, delta * (xmax - xmin) / getWidth(), 0);
				break;
			case KeyEvent.VK_DOWN:
				translateManipulableBy(selectedManipulable, 0, delta * (ymax - ymin) / getHeight());
				break;
			case KeyEvent.VK_UP:
				translateManipulableBy(selectedManipulable, 0, -delta * (ymax - ymin) / getHeight());
				break;
			}
			setSelectedManipulable(selectedManipulable);
		}
		repaint();
		// e.consume();//don't call, or this stops key binding
	}

	private void processKeyReleased(KeyEvent e) {
		if (runHeatingThread) {
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				cooling = false;
			}
		}
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_BACK_SPACE:
			// this is different than cut() in that it doesn't create a copy for pasting
			if (selectedManipulable != null) {
				if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected object?", "Delete Object", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					notifyManipulationListeners(selectedManipulable, ManipulationEvent.DELETE);
					setSelectedManipulable(null);
				}
			} else {
				if (showGraph) {
					if (JOptionPane.showConfirmDialog(this, "Are you sure you want to erase the graph?", "Erase Graph", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						eraseGraph();
					}
				}
			}
			break;
		case KeyEvent.VK_R:
			notifyManipulationListeners(null, runToggle ? ManipulationEvent.STOP : ManipulationEvent.RUN);
			break;
		case KeyEvent.VK_T:
			notifyManipulationListeners(null, ManipulationEvent.RESET);
			break;
		case KeyEvent.VK_L:
			notifyManipulationListeners(null, ManipulationEvent.RELOAD);
			break;
		case KeyEvent.VK_S: // avoid conflict with the save keystroke
			if (!e.isControlDown() && !e.isMetaDown() && !e.isAltDown())
				notifyManipulationListeners(null, ManipulationEvent.SUN_SHINE);
			break;
		case KeyEvent.VK_Q:
			notifyManipulationListeners(null, ManipulationEvent.SUN_ANGLE_INCREASE);
			break;
		case KeyEvent.VK_W:
			notifyManipulationListeners(null, ManipulationEvent.SUN_ANGLE_DECREASE);
			break;
		case KeyEvent.VK_G:
			showGraph = !showGraph;
			notifyGraphListeners(showGraph ? GraphEvent.GRAPH_OPENED : GraphEvent.GRAPH_CLOSED);
			break;
		case KeyEvent.VK_ESCAPE: // allow the app to shut down when in full screen mode
			Object r = getClientProperty("close_full_screen");
			if (r instanceof Runnable)
				((Runnable) r).run();
			break;
		}
		repaint();
		// e.consume();//don't call, or this stops key binding
	}

	private void processMousePressed(MouseEvent e) {
		mousePressedTime = System.currentTimeMillis();
		requestFocusInWindow();
		int x = e.getX();
		int y = e.getY();
		if (showGraph) {
			boolean inGraph = false;
			if (graphRenderer.buttonContains(GraphRenderer.CLOSE_BUTTON, x, y)) {
				inGraph = true;
			} else if (graphRenderer.buttonContains(GraphRenderer.X_EXPAND_BUTTON, x, y)) {
				inGraph = true;
			} else if (graphRenderer.buttonContains(GraphRenderer.X_SHRINK_BUTTON, x, y)) {
				inGraph = true;
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_EXPAND_BUTTON, x, y)) {
				inGraph = true;
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_SHRINK_BUTTON, x, y)) {
				inGraph = true;
			}
			if (inGraph) {
				e.consume();
				return;
			}
		}
		switch (actionMode) {
		case SELECT_MODE:
			if (selectedManipulable != null) {
				selectedSpot = -1;
				for (byte i = 0; i < handle.length; i++) {
					if (handle[i].x < -10 || handle[i].y < -10)
						continue;
					if (handle[i].contains(x, y)) {
						selectedSpot = i;
						break;
					}
				}
				if (selectedSpot != -1) {
					setMovingShape(true);
					return;
				}
			}
			if (!MiscUtil.isRightClick(e)) {
				selectManipulable(x, y);
				if (selectedManipulable != null) {
					Point2D.Float center = selectedManipulable.getCenter();
					pressedPointRelative.x = x - convertPointToPixelX(center.x);
					pressedPointRelative.y = y - convertPointToPixelY(center.y);
					setMovingShape(false);
				}
			}
			break;
		case RECTANGLE_MODE:
		case ELLIPSE_MODE:
			if (showGraph) {
				e.consume();
				return;
			}
			mousePressedPoint.setLocation(x, y);
			break;
		case POLYGON_MODE:
			if (showGraph) {
				e.consume();
				return;
			}
			if (e.getClickCount() < 2)
				addPolygonPoint(x, y);
			break;
		case HEATING_MODE:
			runHeatingThread = true;
			heatingX = convertPixelToPointX(x);
			heatingY = convertPixelToPointY(y);
			new Thread(new Runnable() {
				public void run() {
					while (runHeatingThread) {
						float t = model.getTemperatureAt(heatingX, heatingY);
						if (cooling) {
							if (t > -100)
								model.changeTemperatureAt(heatingX, heatingY, -temperatureIncrement);
						} else {
							if (t < 100)
								model.changeTemperatureAt(heatingX, heatingY, temperatureIncrement);
						}
					}
				}
			}).start();
			break;
		}
		repaint();
		e.consume();
	}

	private void processMouseDragged(MouseEvent e) {
		if (MiscUtil.isRightClick(e))
			return;
		if (showGraph && !(selectedManipulable instanceof Thermometer)) {
			e.consume();
			return;
		}
		mouseBeingDragged = true;
		if (System.currentTimeMillis() - mousePressedTime < MINIMUM_MOUSE_DRAG_RESPONSE_INTERVAL)
			return;
		mousePressedTime = System.currentTimeMillis();
		int x = e.getX();
		int y = e.getY();
		switch (actionMode) {
		case SELECT_MODE:
			if (movingShape != null && selectedManipulable != null) {
				Shape shape = movingShape.getShape();
				if (shape instanceof RectangularShape) {
					if (selectedManipulable instanceof Thermometer) {
						if (x < 8)
							x = 8;
						else if (x > getWidth() - 8)
							x = getWidth() - 8;
						if (y < 8)
							y = 8;
						else if (y > getHeight() - 8)
							y = getHeight() - 8;
					}
					RectangularShape s = (RectangularShape) shape;
					double a = s.getX(), b = s.getY(), c = s.getWidth(), d = s.getHeight();
					if (selectedSpot == -1) {
						// x+width/2+pressedPointRelative.x=mouse_x
						a = x - pressedPointRelative.x - c * 0.5;
						b = y - pressedPointRelative.y - d * 0.5;
						setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					} else {
						if (selectedManipulable instanceof Part) {
							switch (selectedSpot) {
							case LOWER_LEFT:
							case LOWER_RIGHT:
							case UPPER_LEFT:
							case UPPER_RIGHT:
								a = Math.min(x, anchorPoint.x);
								b = Math.min(y, anchorPoint.y);
								c = Math.abs(x - anchorPoint.x);
								d = Math.abs(y - anchorPoint.y);
								break;
							case TOP:
							case BOTTOM:
								b = Math.min(y, anchorPoint.y);
								d = Math.abs(y - anchorPoint.y);
								break;
							case LEFT:
							case RIGHT:
								a = Math.min(x, anchorPoint.x);
								c = Math.abs(x - anchorPoint.x);
								break;
							}
							setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
						}
					}
					s.setFrame(a, b, c, d);
				} else if (shape instanceof Polygon) {
					Polygon s = (Polygon) shape;
					if (selectedSpot == -1) {
						float xc = 0, yc = 0;
						for (int i = 0; i < s.npoints; i++) {
							xc += s.xpoints[i];
							yc += s.ypoints[i];
						}
						xc /= s.npoints;
						yc /= s.npoints;
						xc = x - pressedPointRelative.x - xc;
						yc = y - pressedPointRelative.y - yc;
						s.translate((int) xc, (int) yc);
						setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					} else {
						if (selectedManipulable instanceof Part) {
							int k = s.npoints < handle.length ? selectedSpot : (int) ((float) selectedSpot * (float) s.npoints / (float) handle.length);
							s.xpoints[k] = x;
							s.ypoints[k] = y;
							setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
						}
					}
				} else if (shape instanceof Area) {
					if (selectedManipulable instanceof Cloud && movingShape instanceof MovingCloud) {
						MovingCloud mc = (MovingCloud) movingShape;
						Rectangle r = mc.getShape().getBounds();
						if (selectedSpot == -1) {
							int xc = (int) (x - pressedPointRelative.x - r.getCenterX());
							int yc = (int) (y - pressedPointRelative.y - r.getCenterY());
							mc.setLocation(xc, yc);
						} else {
							double a = r.getX() + mc.getX(), b = r.getY() + mc.getY(), c = r.getWidth(), d = r.getHeight();
							switch (selectedSpot) {
							case LOWER_LEFT:
							case LOWER_RIGHT:
							case UPPER_LEFT:
							case UPPER_RIGHT:
								a = Math.min(x, anchorPoint.x);
								b = Math.min(y, anchorPoint.y);
								c = Math.abs(x - anchorPoint.x);
								d = Math.abs(y - anchorPoint.y);
								break;
							case TOP:
							case BOTTOM:
								b = Math.min(y, anchorPoint.y);
								d = Math.abs(y - anchorPoint.y);
								break;
							case LEFT:
							case RIGHT:
								a = Math.min(x, anchorPoint.x);
								c = Math.abs(x - anchorPoint.x);
								break;
							}
							movingShape = new MovingCloud(new Rectangle2D.Float((float) a, (float) b, (float) c, (float) d));
							setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
						}
					}
				} else {
					showTip("<html><font color=red>The selected object is not draggable!</font></html>", x, y, 500);
				}
			}
			break;
		case RECTANGLE_MODE:
			if (x > mousePressedPoint.x) {
				rectangle.width = x - mousePressedPoint.x;
				rectangle.x = mousePressedPoint.x;
			} else {
				rectangle.width = mousePressedPoint.x - x;
				rectangle.x = mousePressedPoint.x - rectangle.width;
			}
			if (y > mousePressedPoint.y) {
				rectangle.height = y - mousePressedPoint.y;
				rectangle.y = mousePressedPoint.y;
			} else {
				rectangle.height = mousePressedPoint.y - y;
				rectangle.y = mousePressedPoint.y - rectangle.height;
			}
			break;
		case ELLIPSE_MODE:
			if (x > mousePressedPoint.x) {
				ellipse.width = x - mousePressedPoint.x;
				ellipse.x = mousePressedPoint.x;
			} else {
				ellipse.width = mousePressedPoint.x - x;
				ellipse.x = mousePressedPoint.x - ellipse.width;
			}
			if (y > mousePressedPoint.y) {
				ellipse.height = y - mousePressedPoint.y;
				ellipse.y = mousePressedPoint.y;
			} else {
				ellipse.height = mousePressedPoint.y - y;
				ellipse.y = mousePressedPoint.y - ellipse.height;
			}
			break;
		case HEATING_MODE:
			heatingX = convertPixelToPointX(x);
			heatingY = convertPixelToPointY(y);
			break;
		}
		if (!model.isRunning())
			repaint();
		e.consume();
	}

	private void processMouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseReleasedPoint.setLocation(x, y);
		if (showGraph && !(selectedManipulable instanceof Thermometer)) {
			if (graphRenderer.buttonContains(GraphRenderer.CLOSE_BUTTON, x, y)) {
				showGraph = false;
				notifyGraphListeners(GraphEvent.GRAPH_CLOSED);
				notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			} else if (graphRenderer.buttonContains(GraphRenderer.X_EXPAND_BUTTON, x, y)) {
				graphRenderer.doubleXmax();
			} else if (graphRenderer.buttonContains(GraphRenderer.X_SHRINK_BUTTON, x, y)) {
				graphRenderer.halfXmax();
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_EXPAND_BUTTON, x, y)) {
				graphRenderer.increaseYmax();
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_SHRINK_BUTTON, x, y)) {
				graphRenderer.decreaseYmax();
			}
			repaint();
			e.consume();
			movingShape = null;
			mouseBeingDragged = false;
			if (graphRenderer.windowContains(x, y))
				return;
		}
		switch (actionMode) {
		case SELECT_MODE:
			if (MiscUtil.isRightClick(e)) {
				selectManipulable(x, y);
				createPopupMenu();
				popupMenu.show(this, x, y);
				repaint();
				return;
			} else {
				if (movingShape != null && mouseBeingDragged && selectedManipulable != null) {
					if (selectedManipulable.isDraggable()) {
						Shape shape = movingShape.getShape();
						if (shape instanceof RectangularShape) {
							if (selectedSpot == -1) {
								float x2 = convertPixelToPointX((int) (x - pressedPointRelative.x));
								float y2 = convertPixelToPointY((int) (y - pressedPointRelative.y));
								translateManipulableTo(selectedManipulable, x2, y2);
								setSelectedManipulable(selectedManipulable);
							} else {
								if (selectedManipulable instanceof Part) {
									RectangularShape r = (RectangularShape) shape;
									float x2 = convertPixelToPointX((int) r.getX());
									float y2 = convertPixelToPointY((int) r.getY());
									float w2 = convertPixelToLengthX((int) r.getWidth());
									float h2 = convertPixelToLengthY((int) r.getHeight());
									resizeManipulableTo(selectedManipulable, x2, y2, w2, h2, 0, 0);
									setSelectedManipulable(selectedManipulable);
								}
							}
						} else if (shape instanceof Polygon) {
							if (selectedSpot == -1) {
								float x2 = convertPixelToPointX((int) (x - pressedPointRelative.x));
								float y2 = convertPixelToPointY((int) (y - pressedPointRelative.y));
								translateManipulableTo(selectedManipulable, x2, y2);
								setSelectedManipulable(selectedManipulable);
							} else {
								Shape s = selectedManipulable.getShape();
								if (s instanceof Polygon2D) {
									Polygon2D p = (Polygon2D) s;
									Polygon p0 = (Polygon) shape;
									int n = p0.npoints;
									for (int i = 0; i < n; i++) {
										p.setVertex(i, convertPixelToPointX(p0.xpoints[i]), convertPixelToPointY(p0.ypoints[i]));
									}
									setSelectedManipulable(selectedManipulable);
									notifyManipulationListeners(selectedManipulable, ManipulationEvent.RESIZE);
								}
							}
						} else if (shape instanceof Area) {
							if (selectedSpot == -1) {
								float x2 = convertPixelToPointX((int) (x - pressedPointRelative.x));
								float y2 = convertPixelToPointY((int) (y - pressedPointRelative.y));
								translateManipulableTo(selectedManipulable, x2, y2);
								setSelectedManipulable(selectedManipulable);
							} else {
								if (selectedManipulable instanceof Cloud && movingShape instanceof MovingCloud) {
									Rectangle2D r = shape.getBounds2D();
									float x2 = convertPixelToPointX((int) r.getX());
									float y2 = convertPixelToPointY((int) r.getY());
									float w2 = convertPixelToLengthX((int) r.getWidth());
									float h2 = convertPixelToLengthY((int) r.getHeight());
									Point p = ((MovingCloud) movingShape).getLocation();
									resizeManipulableTo(selectedManipulable, x2, y2, w2, h2, convertPixelToPointX(p.x), convertPixelToPointY(p.y));
									setSelectedManipulable(selectedManipulable);
								}
							}
						}
					} else {
						showTip("<html><font color=red>The selected object is not draggable!</font></html>", x, y, 500);
					}
				}
			}
			break;
		case RECTANGLE_MODE:
			if (rectangle.width * rectangle.height > 9) {
				model.addRectangularPart(convertPixelToPointX(rectangle.x), convertPixelToPointY(rectangle.y), convertPixelToLengthX(rectangle.width), convertPixelToLengthY(rectangle.height), model.getBackgroundTemperature() + 20);
				model.refreshPowerArray();
				model.refreshTemperatureBoundaryArray();
				model.refreshMaterialPropertyArrays();
				model.setInitialTemperature();
				notifyManipulationListeners(model.getPart(model.getPartCount() - 1), ManipulationEvent.OBJECT_ADDED);
			}
			rectangle.setRect(-1000, -1000, 0, 0);
			break;
		case ELLIPSE_MODE:
			if (ellipse.width * ellipse.height > 9) {
				float ex = convertPixelToPointX((int) ellipse.x);
				float ey = convertPixelToPointY((int) ellipse.y);
				float ew = convertPixelToLengthX((int) ellipse.width);
				float eh = convertPixelToLengthY((int) ellipse.height);
				model.addEllipticalPart(ex + 0.5f * ew, ey + 0.5f * eh, ew, eh, model.getBackgroundTemperature() + 20);
				model.refreshPowerArray();
				model.refreshTemperatureBoundaryArray();
				model.refreshMaterialPropertyArrays();
				model.setInitialTemperature();
				notifyManipulationListeners(model.getPart(model.getPartCount() - 1), ManipulationEvent.OBJECT_ADDED);
			}
			ellipse.setFrame(-1000, -1000, 0, 0);
			break;
		case POLYGON_MODE:
			if (e.getClickCount() >= 2) {
				resetMousePoints();
				int n = polygon.npoints;
				if (n > 0) {
					float[] px = new float[n];
					float[] py = new float[n];
					for (int i = 0; i < n; i++) {
						px[i] = convertPixelToPointX(polygon.xpoints[i]);
						py[i] = convertPixelToPointY(polygon.ypoints[i]);
					}
					model.addPolygonPart(px, py, model.getBackgroundTemperature() + 20);
					model.refreshPowerArray();
					model.refreshTemperatureBoundaryArray();
					model.refreshMaterialPropertyArrays();
					model.setInitialTemperature();
					notifyManipulationListeners(model.getPart(model.getPartCount() - 1), ManipulationEvent.OBJECT_ADDED);
					polygon.reset();
				}
			}
			break;
		case HEATING_MODE:
			runHeatingThread = false;
			cooling = false;
			break;
		case THERMOMETER_MODE:
			addThermometer(convertPixelToPointX(x), convertPixelToPointY(y));
			notifyManipulationListeners(model.getThermometers().get(model.getThermometers().size() - 1), ManipulationEvent.OBJECT_ADDED);
			break;
		}
		repaint();
		e.consume();
		movingShape = null;
		mouseBeingDragged = false;
	}

	public void addThermometer(float x, float y, String label) {
		Thermometer t = addThermometer(x, y);
		t.setLabel(label);
	}

	private Thermometer addThermometer(float x, float y) {
		Thermometer t = new Thermometer(x, y);
		Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
		r.width = Thermometer.RELATIVE_WIDTH * model.getLx();
		r.height = Thermometer.RELATIVE_HEIGHT * model.getLy();
		t.setCenter(x, y);
		model.addThermometer(t);
		return t;
	}

	private String getGraphDataAt(int x, int y) {
		synchronized (model.getThermometers()) {
			for (Thermometer t : model.getThermometers()) {
				float[] data = graphRenderer.getData(t.getData(), x, y);
				if (data != null) {
					String s = "(" + TEMPERATURE_FORMAT.format(data[0] / 60) + " min, " + TEMPERATURE_FORMAT.format(data[1]) + " " + '\u2103' + ")";
					if (t.getLabel() == null)
						return s;
					return t.getLabel() + ": " + s;
				}
			}
		}
		return null;
	}

	private void processMouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseMovedPoint.setLocation(x, y);
		if (showGraph) {
			if (graphRenderer.buttonContains(GraphRenderer.CLOSE_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(GraphRenderer.X_EXPAND_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(GraphRenderer.X_SHRINK_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_EXPAND_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_SHRINK_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
		switch (actionMode) {
		case SELECT_MODE:
			int iSpot = -1;
			if (!showGraph && (selectedManipulable instanceof Part) || (selectedManipulable instanceof Cloud)) {
				for (int i = 0; i < handle.length; i++) {
					if (handle[i].x < -10 || handle[i].y < -10)
						continue;
					if (handle[i].contains(x, y)) {
						iSpot = i;
						break;
					}
				}
				if (iSpot >= 0) {
					if (selectedManipulable.getShape() instanceof RectangularShape || selectedManipulable.getShape() instanceof Area) {
						switch (iSpot) {
						case UPPER_LEFT:
							setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
							break;
						case LOWER_LEFT:
							setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
							break;
						case UPPER_RIGHT:
							setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
							break;
						case LOWER_RIGHT:
							setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
							break;
						case TOP:
							setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
							break;
						case BOTTOM:
							setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
							break;
						case LEFT:
							setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
							break;
						case RIGHT:
							setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
							break;
						}
					} else {
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				}
			}
			if (iSpot == -1) {
				float rx = convertPixelToPointX(x);
				float ry = convertPixelToPointY(y);
				boolean contained = false;
				// prioritize sensor selection
				synchronized (model.getThermometers()) {
					for (Thermometer t : model.getThermometers()) {
						if (t.contains(rx, ry)) {
							contained = true;
							break;
						}
					}
				}
				if (!contained && !showGraph) {
					boolean draggable = false;
					synchronized (model.getParts()) {
						for (Part p : model.getParts()) {
							if (p.contains(rx, ry)) {
								contained = true;
								draggable = p.isDraggable();
								break;
							}
						}
					}
					if (!draggable)
						contained = false;
					if (!contained) {
						synchronized (model.getClouds()) {
							for (Cloud c : model.getClouds()) {
								if (c.contains(rx, ry)) {
									contained = true;
									draggable = c.isDraggable();
									break;
								}
							}
						}
						if (!draggable)
							contained = false;
					}
				}
				setCursor(Cursor.getPredefinedCursor(contained ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
			}
			if (!model.isRunning())
				repaint();
			break;
		case POLYGON_MODE:
			if (!showGraph) {
				mouseMovedPoint.setLocation(x, y);
				if (!model.isRunning())
					repaint();
			}
			break;
		}
		e.consume();
	}

	private void processComponentResized(ComponentEvent e) {
		graphRenderer.setFrame(50, 50, getWidth() - 100, getHeight() - 100);
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("Time step")) {
			float timeStep = (Float) e.getNewValue();
			graphRenderer.setXmax(7200 * timeStep);
			photonLength = Math.max(5, timeStep * 0.1f);
		}
	}

	private void eraseGraph() {
		model.clearSensorData();
		repaint();
	}

	private void addPolygonPoint(int x, int y) {
		int n = polygon.npoints;
		if (n > 0) {
			int dx = x - polygon.xpoints[n - 1];
			int dy = y - polygon.ypoints[n - 1];
			if (dx * dx + dy * dy > 9)
				polygon.addPoint(x, y);
		} else {
			polygon.addPoint(x, y);
		}
	}

	private void resetMousePoints() {
		mousePressedPoint.setLocation(-1, -1);
		mouseReleasedPoint.setLocation(-1, -1);
		mouseMovedPoint.setLocation(-1, -1);
	}

	private void setMovingShape(boolean anchor) {
		if (selectedManipulable instanceof Part) {
			Part p = (Part) selectedManipulable;
			Shape shape = p.getShape();
			if (shape instanceof Rectangle2D.Float) {
				Rectangle2D.Float r = (Rectangle2D.Float) shape;
				int a = convertPointToPixelX(r.x);
				int b = convertPointToPixelY(r.y);
				int c = convertLengthToPixelX(r.width);
				int d = convertLengthToPixelY(r.height);
				if (anchor)
					setAnchorPointForRectangularShape(selectedSpot, a, b, c, d);
				movingShape = new MovingRoundRectangle(new RoundRectangle2D.Float(a, b, c, d, 0, 0));
			} else if (shape instanceof Ellipse2D.Float) {
				Ellipse2D.Float e = (Ellipse2D.Float) shape;
				int a = convertPointToPixelX(e.x);
				int b = convertPointToPixelY(e.y);
				int c = convertLengthToPixelX(e.width);
				int d = convertLengthToPixelY(e.height);
				if (anchor)
					setAnchorPointForRectangularShape(selectedSpot, a, b, c, d);
				movingShape = new MovingEllipse(new Ellipse2D.Float(a, b, c, d));
			} else if (shape instanceof Polygon2D) {
				Polygon2D q = (Polygon2D) shape;
				int n = q.getVertexCount();
				int[] x = new int[n];
				int[] y = new int[n];
				Point2D.Float point;
				for (int i = 0; i < n; i++) {
					point = q.getVertex(i);
					x[i] = convertPointToPixelX(point.x);
					y[i] = convertPointToPixelY(point.y);
				}
				movingShape = new MovingPolygon(new Polygon(x, y, n));
			}
		} else if (selectedManipulable instanceof Thermometer || selectedManipulable instanceof TextBox) {
			Rectangle2D.Float r = (Rectangle2D.Float) selectedManipulable.getShape();
			int a = convertPointToPixelX(r.x);
			int b = convertPointToPixelY(r.y);
			int c = convertLengthToPixelX(r.width);
			int d = convertLengthToPixelY(r.height);
			if (anchor)
				setAnchorPointForRectangularShape(selectedSpot, a, b, c, d);
			movingShape = new MovingRoundRectangle(new RoundRectangle2D.Float(a, b, c, d, 0, 0));
		} else if (selectedManipulable instanceof Cloud) {
			Cloud cloud = (Cloud) selectedManipulable;
			Rectangle2D.Float r = new Rectangle2D.Float();
			int x = convertPointToPixelX(cloud.getX());
			int y = convertPointToPixelY(cloud.getY());
			r.width = convertLengthToPixelX(cloud.getWidth());
			r.height = convertLengthToPixelY(cloud.getHeight());
			if (anchor)
				setAnchorPointForRectangularShape(selectedSpot, x, y, r.width, r.height);
			movingShape = new MovingCloud(r);
			((MovingCloud) movingShape).setLocation(x, y);
		}
	}

	private float convertPixelToPointX(int x) {
		return xmin + (xmax - xmin) * (float) x / (float) getWidth();
	}

	private float convertPixelToPointY(int y) {
		return ymin + (ymax - ymin) * (float) y / (float) getHeight();
	}

	private float convertPixelToLengthX(int l) {
		return (xmax - xmin) * (float) l / (float) getWidth();
	}

	private float convertPixelToLengthY(int l) {
		return (ymax - ymin) * (float) l / (float) getHeight();
	}

	public int convertPointToPixelX(float x) {
		int w = getWidth();
		if (w == 0)
			w = getPreferredSize().width;
		return Math.round((x - xmin) / (xmax - xmin) * w);
	}

	public int convertPointToPixelY(float y) {
		int h = getHeight();
		if (h == 0)
			h = getPreferredSize().height;
		return Math.round((y - ymin) / (ymax - ymin) * h);
	}

	public int convertLengthToPixelX(float l) {
		int w = getWidth();
		if (w == 0)
			w = getPreferredSize().width;
		return Math.round(l / (xmax - xmin) * w);
	}

	public int convertLengthToPixelY(float l) {
		int h = getHeight();
		if (h == 0)
			h = getPreferredSize().height;
		return Math.round(l / (ymax - ymin) * h);
	}

	private void showTip(String msg, int x, int y, int time) {
		if (tipPopupMenu == null) {
			tipPopupMenu = new JPopupMenu("Tip");
			tipPopupMenu.setBorder(BorderFactory.createLineBorder(Color.black));
			tipPopupMenu.setBackground(SystemColor.info);
			JLabel l = new JLabel(msg);
			l.setFont(new Font(null, Font.PLAIN, 10));
			l.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			tipPopupMenu.add(l);
		} else {
			((JLabel) tipPopupMenu.getComponent(0)).setText(msg);
		}
		tipPopupMenu.show(this, x, y);
		if (time > 0) {
			Timer timer = new Timer(time, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tipPopupMenu.setVisible(false);
				}
			});
			timer.setRepeats(false);
			timer.setInitialDelay(time);
			timer.start();
		}
	}

	private void drawFrank(Graphics2D g, int x, int y) {
		String s = "Energy2D";
		g.setFont(new Font("Arial", Font.BOLD, 14));
		int w = g.getFontMetrics().stringWidth(s);
		g.setColor(Color.gray);
		g.fillRoundRect(x - 6, y - 15, w + 10, 20, 16, 16);
		g.setStroke(moderateStroke);
		g.setColor(heatMapType != HEATMAP_NONE ? Color.lightGray : Color.black);
		g.drawRoundRect(x - 6, y - 15, w + 10, 20, 16, 16);
		g.setColor(Color.black);
		g.drawString(s, x + 1, y - 1);
		g.drawString(s, x + 1, y + 1);
		g.drawString(s, x - 1, y - 1);
		g.drawString(s, x - 1, y + 1);
		g.setColor(Color.lightGray);
		g.drawString(s, x, y);
	}

}
