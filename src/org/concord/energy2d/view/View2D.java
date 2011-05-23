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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Photon;
import org.concord.energy2d.model.Thermometer;
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

	public final static byte PIXEL_NONE = 0;
	public final static byte PIXEL_TEMPERATURE = 1;
	public final static byte PIXEL_THERMAL_ENERGY = 2;

	final static byte UPPER_LEFT = 0;
	final static byte LOWER_LEFT = 1;
	final static byte UPPER_RIGHT = 2;
	final static byte LOWER_RIGHT = 3;
	final static byte TOP = 4;
	final static byte BOTTOM = 5;
	final static byte LEFT = 6;
	final static byte RIGHT = 7;

	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");

	final static short[][] TEMPERATURE_COLOR_SCALE = { { 0, 0, 128 }, { 0, 128, 225 }, { 0, 225, 255 }, { 225, 175, 0 }, { 255, 0, 0 }, { 255, 255, 255 } };

	private final static int MINIMUM_MOUSE_DRAG_RESPONSE_INTERVAL = 20;
	private final static DecimalFormat TEMPERATURE_FORMAT = new DecimalFormat("###.#");
	private Font smallFont = new Font(null, Font.PLAIN, 10);
	private Font labelFont = new Font("Arial", Font.PLAIN | Font.BOLD, 12);

	private RulerRenderer rulerRenderer;
	private GridRenderer gridRenderer;
	private Rainbow rainbow;
	private GraphRenderer graphRenderer;
	private ScalarDistributionRenderer temperatureRenderer, thermalEnergyRenderer;
	private VectorDistributionRenderer vectorFieldRenderer;
	private boolean showIsotherm;
	private boolean showStreamLines;
	private boolean showVelocity;
	private boolean showHeatFluxArrows, showHeatFluxLines;
	private boolean showGraph;
	private boolean showRainbow;
	private boolean clockOn = true;
	private boolean frankOn = true;
	private byte pixelAttribute = PIXEL_TEMPERATURE;
	private float[][] distribution;

	private static Stroke thinStroke = new BasicStroke(1);
	private static Stroke moderateStroke = new BasicStroke(2);
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
	private AffineTransform scale, translate;
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
	private Color textColor = Color.white;

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
		createActions();
		createPopupMenu();
		dialogFactory = new DialogFactory(this);
		temperatureRenderer = new ScalarDistributionRenderer(TEMPERATURE_COLOR_SCALE);
		thermalEnergyRenderer = new ScalarDistributionRenderer(TEMPERATURE_COLOR_SCALE);
		graphRenderer = new GraphRenderer(50, 50, 200, 200);
		rainbow = new Rainbow(TEMPERATURE_COLOR_SCALE);
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

	}

	public void setPixelAttribute(byte pixelAttribute) {
		this.pixelAttribute = pixelAttribute;
		switch (pixelAttribute) {
		case PIXEL_NONE:
			lightColor = new Color(0, 0, 0, 128);
			textColor = Color.black;
			break;
		case PIXEL_TEMPERATURE:
			lightColor = new Color(255, 255, 255, 128);
			textColor = Color.white;
			break;
		case PIXEL_THERMAL_ENERGY:
			lightColor = new Color(255, 255, 255, 128);
			textColor = Color.white;
			break;
		}
	}

	public byte getPixelAttribute() {
		return pixelAttribute;
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
		}
		repaint();
	}

	public byte getActionMode() {
		return actionMode;
	}

	public void clear() {
		if (textBoxes != null)
			textBoxes.clear();
		if (pictures != null)
			pictures.clear();
	}

	public TextBox addText(String text, float x, float y) {
		if (textBoxes == null)
			textBoxes = new ArrayList<TextBox>();
		TextBox t = new TextBox(text, x, y);
		textBoxes.add(t);
		repaint();
		return t;
	}

	public int getTextBoxCount() {
		if (textBoxes == null)
			return 0;
		return textBoxes.size();
	}

	public TextBox getTextBox(int i) {
		if (textBoxes == null)
			return null;
		if (i < 0 || i >= textBoxes.size())
			return null;
		return textBoxes.get(i);
	}

	public void addPicture(ImageIcon image, int x, int y) {
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

	private void notifyGraphListeners(byte eventType) {
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
		gridRenderer = b ? new GridRenderer(nx, ny) : null;
	}

	public boolean isGridOn() {
		return gridRenderer != null;
	}

	public void setGridSize(int gridSize) {
		if (gridRenderer != null)
			gridRenderer.setGridSize(gridSize);
	}

	public int getGridSize() {
		if (gridRenderer == null)
			return 10;
		return gridRenderer.getGridSize();
	}

	public void setRainbowOn(boolean b) {
		showRainbow = b;
	}

	public boolean isRainbowOn() {
		return showRainbow;
	}

	/** relative to the width and height of the view */
	public void setRainbowRectangle(float rx, float ry, float rw, float rh) {
		rainbow.setRect(rx, ry, rw, rh);
	}

	/** relative to the width and height of the view */
	public Rectangle2D.Float getRainbowRectangle() {
		return rainbow.getRect();
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

	public void setVelocityOn(boolean b) {
		showVelocity = b;
		if (b && vectorFieldRenderer == null)
			vectorFieldRenderer = new VectorDistributionRenderer(nx, ny);
	}

	public boolean isVelocityOn() {
		return showVelocity;
	}

	public void setHeatFluxArrowsOn(boolean b) {
		showHeatFluxArrows = b;
		if (b && vectorFieldRenderer == null)
			vectorFieldRenderer = new VectorDistributionRenderer(nx, ny);
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
			vectorFieldRenderer = new VectorDistributionRenderer(nx, ny);
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

	public void setOutlineOn(boolean b) {
		List<Part> parts = model.getParts();
		synchronized (parts) {
			for (Part p : parts) {
				p.setFilled(!b);
			}
		}
	}

	public boolean isOutlineOn() {
		if (model.getPartCount() == 0)
			return false;
		return !model.getPart(0).isFilled();
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

	public void setMinimumTemperature(float min) {
		temperatureRenderer.setMinimum(min);
		thermalEnergyRenderer.setMinimum(min);
	}

	public float getMinimumTemperature() {
		return temperatureRenderer.getMinimum();
	}

	public void setMaximumTemperature(float max) {
		temperatureRenderer.setMaximum(max);
		thermalEnergyRenderer.setMaximum(max);
	}

	public float getMaximumTemperature() {
		return temperatureRenderer.getMaximum();
	}

	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	private void cut() {
		if (selectedManipulable != null) {
			copiedManipulable = selectedManipulable;
			notifyManipulationListeners(selectedManipulable, ManipulationEvent.DELETE);
			setSelectedManipulable(null);
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
		}
		notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
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
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	@Override
	public void update(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		Stroke stroke = g2.getStroke();
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		switch (pixelAttribute) {
		case PIXEL_TEMPERATURE:
			drawTemperatureField(g2);
			break;
		case PIXEL_THERMAL_ENERGY:
			drawThermalEnergyField(g2);
			break;
		}
		drawParts(g2);
		if (isotherms != null) {
			g2.setStroke(thinStroke);
			isotherms.render(g2, getSize(), model.getTemperature());
		}
		if (showStreamLines && streamlines != null) {
			g2.setStroke(thinStroke);
			streamlines.render(g2, getSize(), model.getXVelocity(), model.getYVelocity());
		}
		if (showHeatFluxLines && heatFluxLines != null) {
			g2.setStroke(thinStroke);
			heatFluxLines.render(g2, getSize(), model.getTemperature(), -1);
		}
		if (selectedManipulable != null) {
			if (selectedManipulable instanceof Thermometer) {
				Thermometer t = (Thermometer) selectedManipulable;
				Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
				int wt = convertLengthToPixelX(r.width);
				int ht = convertLengthToPixelY(r.height);
				int xt = convertPointToPixelX(t.getX()) - wt / 2;
				int yt = convertPointToPixelY(t.getY()) - ht / 2;
				g2.setColor(Color.yellow);
				g2.fillRect(xt - 3, yt - 3, wt + 5, ht + 5);
			} else {
				for (Rectangle r : handle) {
					if (r.x != 0 || r.y != 0) {
						g2.setColor(Color.yellow);
						g2.fill(r);
						g2.setColor(Color.black);
						g2.draw(r);
					}
				}
			}
		}
		if (mouseBeingDragged) {
			if (movingShape != null) {
				g2.setColor(Color.white);
				g2.setStroke(dashed);
				movingShape.render(g2);
			}
		}
		if (gridRenderer != null)
			gridRenderer.render(this, g2);
		if (rulerRenderer != null) {
			g2.setColor(textColor);
			rulerRenderer.render(this, g2);
		}
		if (showRainbow && pixelAttribute != PIXEL_NONE) {
			g2.setStroke(thinStroke);
			switch (pixelAttribute) {
			case PIXEL_TEMPERATURE:
				rainbow.render(this, g2, temperatureRenderer.getMaximum(), temperatureRenderer.getMinimum());
				break;
			case PIXEL_THERMAL_ENERGY:
				rainbow.render(this, g2, thermalEnergyRenderer.getMaximum(), thermalEnergyRenderer.getMinimum());
				break;
			}
		}
		if (showVelocity)
			vectorFieldRenderer.renderVectors(model.getXVelocity(), model.getYVelocity(), this, g2);
		if (showHeatFluxArrows)
			vectorFieldRenderer.renderHeatFlux(model.getTemperature(), model.getConductivity(), this, g2);
		drawThermometers(g2);
		drawPhotons(g2);
		drawTextBoxes(g2);
		drawPictures(g2);
		if (showGraph && !model.getThermometers().isEmpty()) {
			graphRenderer.setDrawFrame(true);
			synchronized (model.getThermometers()) {
				for (Thermometer t : model.getThermometers()) {
					graphRenderer.render(this, g2, t.getData(), t.getLabel(), selectedManipulable == t);
				}
			}
		}
		if (clockOn) {
			g2.setFont(smallFont);
			g2.setColor(textColor);
			g2.drawString(MiscUtil.formatTime((int) time), w - 68, 16);
		}

		g2.setStroke(dashed);
		switch (actionMode) {
		case RECTANGLE_MODE:
			g2.setColor(TRANSLUCENT_GRAY);
			g2.fill(rectangle);
			g2.setColor(Color.white);
			g2.draw(rectangle);
			break;
		case ELLIPSE_MODE:
			g2.setColor(TRANSLUCENT_GRAY);
			g2.fill(ellipse);
			g2.setColor(Color.white);
			g2.draw(ellipse);
			break;
		case POLYGON_MODE:
			g2.setColor(TRANSLUCENT_GRAY);
			g2.fill(polygon);
			g2.setColor(Color.white);
			g2.draw(polygon);
			if (mouseMovedPoint.x >= 0 && mouseMovedPoint.y >= 0 && mouseReleasedPoint.x >= 0 && mouseReleasedPoint.y >= 0) {
				g2.setColor(Color.green);
				g2.drawLine(mouseMovedPoint.x, mouseMovedPoint.y, mouseReleasedPoint.x, mouseReleasedPoint.y);
				int np = polygon.npoints;
				if (np > 0) {
					g2.drawLine(mouseMovedPoint.x, mouseMovedPoint.y, polygon.xpoints[0], polygon.ypoints[0]);
				}
			}
			break;
		}

		g2.setStroke(stroke);
		if (frankOn) {
			int dy = rulerRenderer != null ? 30 : 15;
			drawFrank(g2, getWidth() - 84, getHeight() - dy);
		}

		if (errorMessage != null) {
			g.setColor(Color.red);
			g.setFont(new Font("Arial", Font.BOLD, 30));
			FontMetrics fm = g.getFontMetrics();
			g.drawString(errorMessage, w / 2 - fm.stringWidth(errorMessage) / 2, h / 2);
			notifyManipulationListeners(null, ManipulationEvent.STOP);
		}

	}

	void setErrorMessage(String message) {
		this.errorMessage = message;
	}

	private void drawThermometers(Graphics2D g) {
		List<Thermometer> thermometers = model.getThermometers();
		if (thermometers.isEmpty())
			return;
		g.setStroke(thinStroke);
		Symbol s = Symbol.get(Symbol.THERMOMETER);
		float w = Thermometer.RELATIVE_WIDTH * model.getLx();
		float h = Thermometer.RELATIVE_HEIGHT * model.getLy();
		s.setIconWidth((int) (w * getWidth() / (xmax - xmin)));
		s.setIconHeight((int) (h * getHeight() / (ymax - ymin)));
		float lx = s.getIconWidth();
		float ly = s.getIconHeight();
		float temp;
		String str;
		g.setFont(smallFont);
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
					x = (int) (rx * getWidth() - lx * 0.5);
					y = (int) (ry * getHeight() - ly * 0.5);
					s.paintIcon(this, g, x, y);
					ix = Math.round(nx * rx);
					iy = Math.round(ny * ry);
					temp = model.getTemperature()[ix][iy];
					if (!Float.isNaN(temp)) {
						g.setColor(textColor);
						str = TEMPERATURE_FORMAT.format(temp) + '\u2103';
						centerString(str, g, x + s.getIconWidth() / 2, y - 5);
						if (t.getLabel() != null) {
							centerString(t.getLabel(), g, x + s.getIconWidth() / 2, y + s.getIconHeight() + 10);
						}
					}
				}
			}
		}
	}

	private static void centerString(String s, Graphics2D g, int x, int y) {
		int stringWidth = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x - stringWidth / 2, y);
	}

	private Color getPartColor(Part p, Color proposedColor) {
		if (p.getPower() > 0)
			return new Color(0xFFFF00);
		if (p.getPower() < 0)
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
						}
					} else if (fillPattern instanceof Texture) {
						setPaint(g, (Texture) fillPattern, p.isFilled());
						g.fillOval(x, y, w, h);
					}
					g.setColor(Color.black);
					g.drawOval(x - 1, y - 1, w + 2, h + 2);
					String label = p.getLabel();
					if (label != null) {
						String partLabel = p.getLabel(label);
						if (partLabel != null)
							label = partLabel;
						g.setColor(Color.white);
						g.setFont(labelFont);
						FontMetrics fm = g.getFontMetrics();
						int labelWidth = fm.stringWidth(label);
						float x0 = x + 0.5f * w;
						float y0 = y + 0.5f * h;
						if (w < h * 0.25f) {
							g.rotate(Math.PI * 0.5, x0, y0);
							g.drawString(label, x0 - labelWidth / 2, y0 + fm.getHeight() / 4);
							g.rotate(-Math.PI * 0.5, x0, y0);
						} else {
							g.drawString(label, x0 - labelWidth / 2, y0 + fm.getHeight() / 4);
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
						}
					} else if (fp instanceof Texture) {
						setPaint(g, (Texture) fp, p.isFilled());
						g.fillRect(x, y, w, h);
					}
					g.setColor(Color.black);
					g.drawRect(x - 1, y - 1, w + 2, h + 2);
					String label = p.getLabel();
					if (label != null) {
						String partLabel = p.getLabel(label);
						if (partLabel != null)
							label = partLabel;
						g.setColor(Color.white);
						g.setFont(labelFont);
						FontMetrics fm = g.getFontMetrics();
						int labelWidth = fm.stringWidth(label);
						float x0 = x + 0.5f * w;
						float y0 = y + 0.5f * h;
						if (w < h * 0.25f) {
							g.rotate(Math.PI * 0.5, x0, y0);
							g.drawString(label, x0 - labelWidth / 2, y0 + fm.getHeight() / 4);
							g.rotate(-Math.PI * 0.5, x0, y0);
						} else {
							g.drawString(label, x0 - labelWidth / 2, y0 + fm.getHeight() / 4);
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
						}
					} else if (fp instanceof Texture) {
						setPaint(g, (Texture) fp, p.isFilled());
						g.fill(multigon);
					}
					g.setColor(Color.black);
					g.draw(multigon);
					String label = p.getLabel();
					if (label != null) {
						String partLabel = p.getLabel(label);
						if (partLabel != null)
							label = partLabel;
						g.setColor(Color.white);
						g.setFont(labelFont);
						FontMetrics fm = g.getFontMetrics();
						int labelWidth = fm.stringWidth(label);
						cx /= multigon.npoints;
						cy /= multigon.npoints;
						g.drawString(label, cx - labelWidth / 2, cy + fm.getHeight() / 4);
					}
				}
			}
		}
		g.setStroke(oldStroke);
	}

	private void drawTextBoxes(Graphics2D g) {
		if (textBoxes == null || textBoxes.isEmpty())
			return;
		Font oldFont = g.getFont();
		Color oldColor = g.getColor();
		String s = null;
		for (TextBox x : textBoxes) {
			g.setFont(new Font(x.getName(), x.getStyle(), x.getSize()));
			g.setColor(x.getColor());
			s = x.getString();
			s = s.replaceAll("%Prandtl", formatter.format(model.getPrandtlNumber()));
			g.drawString(s, convertPointToPixelX(x.getX()), getHeight() - convertPointToPixelY(x.getY()));
		}
		g.setFont(oldFont);
		g.setColor(oldColor);
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
		if (!model.getThermometers().isEmpty()) {
			// always prefer to select a thermometer
			synchronized (model.getThermometers()) {
				for (Thermometer t : model.getThermometers()) {
					if (t.contains(rx, ry)) {
						setSelectedManipulable(t);
						return;
					}
				}
			}
		}
		synchronized (model.getParts()) {
			int n = model.getParts().size();
			if (n > 0) { // later-added has higher priority
				for (int i = n - 1; i >= 0; i--) {
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
			}
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float r = (Ellipse2D.Float) s;
			r.x += dx;
			r.y += dy;
		} else if (s instanceof Area) {
			if (translate == null)
				translate = new AffineTransform();
			translate.setToTranslation(dx, dy);
			((Area) s).transform(translate);
		} else if (s instanceof Polygon2D) {
			Polygon2D p = (Polygon2D) s;
			p.translateBy(dx, dy);
		}
		notifyManipulationListeners(m, ManipulationEvent.TRANSLATE);
	}

	private void translateManipulableTo(Manipulable m, float x, float y) {
		Shape s = m.getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.x = x - r.width / 2;
			r.y = y - r.height / 2;
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float r = (Ellipse2D.Float) s;
			r.x = x - r.width / 2;
			r.y = y - r.height / 2;
		} else if (s instanceof Area) {
			Rectangle2D rect = ((Area) s).getBounds2D();
			if (translate == null)
				translate = new AffineTransform();
			translate.setToTranslation(x - (float) rect.getX(), y - (float) rect.getY());
			((Area) s).transform(translate);
		} else if (s instanceof Polygon2D) {
			Shape[] shape = movingShape.getShapes();
			if (shape[0] instanceof Polygon) {
				Polygon polygon = (Polygon) shape[0];
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

	void resizeManipulableTo(Manipulable m, float x, float y, float w, float h) {
		Shape s = m.getShape();
		if (s instanceof Rectangle2D.Float) {
			Rectangle2D.Float r = (Rectangle2D.Float) s;
			r.setFrame(x, y, w, h);
		} else if (s instanceof Ellipse2D.Float) {
			Ellipse2D.Float r = (Ellipse2D.Float) s;
			r.setFrame(x, y, w, h);
		} else if (s instanceof Area) {
		}
		notifyManipulationListeners(m, ManipulationEvent.RESIZE);
	}

	private void processKeyPressed(KeyEvent e) {
		if (selectedManipulable != null) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				translateManipulableBy(selectedManipulable, -.01f * (xmax - xmin), 0);
				break;
			case KeyEvent.VK_RIGHT:
				translateManipulableBy(selectedManipulable, .01f * (xmax - xmin), 0);
				break;
			case KeyEvent.VK_DOWN:
				translateManipulableBy(selectedManipulable, 0, .01f * (ymax - ymin));
				break;
			case KeyEvent.VK_UP:
				translateManipulableBy(selectedManipulable, 0, -.01f * (ymax - ymin));
				break;
			}
			setSelectedManipulable(selectedManipulable);
		}
		repaint();
		// e.consume();//don't call, or this stops key binding
	}

	private void processKeyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_BACK_SPACE:
			// this is different than cut() in that it doesn't
			// create a copy for pasting
			if (selectedManipulable != null) {
				notifyManipulationListeners(selectedManipulable, ManipulationEvent.DELETE);
				setSelectedManipulable(null);
			}
			break;
		case KeyEvent.VK_R:
			notifyManipulationListeners(null, runToggle ? ManipulationEvent.STOP : ManipulationEvent.RUN);
			runToggle = !runToggle;
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
			selectManipulable(x, y);
			if (selectedManipulable != null) {
				Point2D.Float center = selectedManipulable.getCenter();
				pressedPointRelative.x = x - convertPointToPixelX(center.x);
				pressedPointRelative.y = y - convertPointToPixelY(center.y);
				setMovingShape(false);
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
				Shape[] shape = movingShape.getShapes();
				if (shape[0] instanceof RectangularShape) {
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
					RectangularShape s = (RectangularShape) shape[0];
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
				} else if (shape[0] instanceof Polygon) {
					Polygon s = (Polygon) shape[0];
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
		case POLYGON_MODE:
			if (e.getClickCount() < 2)
				addPolygonPoint(x, y);
			break;
		}
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
			} else if (graphRenderer.buttonContains(GraphRenderer.X_EXPAND_BUTTON, x, y)) {
				graphRenderer.expandScopeX();
			} else if (graphRenderer.buttonContains(GraphRenderer.X_SHRINK_BUTTON, x, y)) {
				graphRenderer.shrinkScopeX();
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_EXPAND_BUTTON, x, y)) {
				graphRenderer.expandScopeY();
			} else if (graphRenderer.buttonContains(GraphRenderer.Y_SHRINK_BUTTON, x, y)) {
				graphRenderer.shrinkScopeY();
			}
			repaint();
			e.consume();
			movingShape = null;
			mouseBeingDragged = false;
			return;
		}
		if (e.getClickCount() >= 2) {
			switch (actionMode) {
			case POLYGON_MODE:
				resetMousePoints();
				int n = polygon.npoints;
				if (n > 0) {
					float[] px = new float[n];
					float[] py = new float[n];
					for (int i = 0; i < n; i++) {
						px[i] = convertPixelToPointX(polygon.xpoints[i]);
						py[i] = convertPixelToPointY(polygon.ypoints[i]);
					}
					model.addPolygonPart(px, py);
					model.refreshPowerArray();
					model.refreshTemperatureBoundaryArray();
					model.refreshMaterialPropertyArrays();
					model.setInitialTemperature();
					notifyManipulationListeners(model.getPart(model.getPartCount() - 1), ManipulationEvent.OBJECT_ADDED);
					polygon.reset();
				}
				break;
			}
			repaint();
			e.consume();
			return;
		}
		switch (actionMode) {
		case SELECT_MODE:
			if (MiscUtil.isRightClick(e)) {
				selectManipulable(x, y);
				repaint();
				createPopupMenu();
				popupMenu.show(this, x, y);
				return;
			}
			if (movingShape != null && mouseBeingDragged && selectedManipulable != null) {
				if (selectedManipulable.isDraggable()) {
					Shape[] shape = movingShape.getShapes();
					if (shape[0] instanceof RectangularShape) {
						if (selectedSpot == -1) {
							float x2 = convertPixelToPointX((int) (x - pressedPointRelative.x));
							float y2 = convertPixelToPointY((int) (y - pressedPointRelative.y));
							translateManipulableTo(selectedManipulable, x2, y2);
							setSelectedManipulable(selectedManipulable);
						} else {
							if (selectedManipulable instanceof Part) {
								RectangularShape r = (RectangularShape) shape[0];
								float x2 = convertPixelToPointX((int) r.getX());
								float y2 = convertPixelToPointY((int) r.getY());
								float w2 = convertPixelToLengthX((int) r.getWidth());
								float h2 = convertPixelToLengthY((int) r.getHeight());
								resizeManipulableTo(selectedManipulable, x2, y2, w2, h2);
								setSelectedManipulable(selectedManipulable);
							}
						}
					} else if (shape[0] instanceof Polygon) {
						if (selectedSpot == -1) {
							float x2 = convertPixelToPointX((int) (x - pressedPointRelative.x));
							float y2 = convertPixelToPointY((int) (y - pressedPointRelative.y));
							translateManipulableTo(selectedManipulable, x2, y2);
							setSelectedManipulable(selectedManipulable);
						} else {
							Shape s = selectedManipulable.getShape();
							if (s instanceof Polygon2D) {
								Polygon2D p = (Polygon2D) s;
								Polygon p0 = (Polygon) shape[0];
								int n = p0.npoints;
								for (int i = 0; i < n; i++) {
									p.setVertex(i, convertPixelToPointX(p0.xpoints[i]), convertPixelToPointY(p0.ypoints[i]));
								}
								setSelectedManipulable(selectedManipulable);
								notifyManipulationListeners(selectedManipulable, ManipulationEvent.RESIZE);
							}
						}
					}
				} else {
					showTip("<html><font color=red>The selected object is not draggable!</font></html>", x, y, 500);
				}
			} else {
				selectManipulable(x, y);
			}
			break;
		case RECTANGLE_MODE:
			if (rectangle.width * rectangle.height > 9) {
				model.addRectangularPart(convertPixelToPointX(rectangle.x), convertPixelToPointY(rectangle.y), convertPixelToLengthX(rectangle.width), convertPixelToLengthY(rectangle.height));
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
				model.addEllipticalPart(ex + 0.5f * ew, ey + 0.5f * eh, ew, eh);
				model.refreshPowerArray();
				model.refreshTemperatureBoundaryArray();
				model.refreshMaterialPropertyArrays();
				model.setInitialTemperature();
				notifyManipulationListeners(model.getPart(model.getPartCount() - 1), ManipulationEvent.OBJECT_ADDED);
			}
			ellipse.setFrame(-1000, -1000, 0, 0);
			break;
		case POLYGON_MODE:
			mouseReleasedPoint.setLocation(x, y);
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

	private void addThermometer(float x, float y) {
		Thermometer t = new Thermometer(x, y);
		Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
		r.width = Thermometer.RELATIVE_WIDTH * model.getLx();
		r.height = Thermometer.RELATIVE_HEIGHT * model.getLy();
		t.setCenter(x, y);
		model.addThermometer(t);
	}

	private void processMouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
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
			if (!showGraph && selectedManipulable instanceof Part) {
				for (int i = 0; i < handle.length; i++) {
					if (handle[i].x < -10 || handle[i].y < -10)
						continue;
					if (handle[i].contains(x, y)) {
						iSpot = i;
						break;
					}
				}
				if (iSpot >= 0) {
					if (selectedManipulable.getShape() instanceof RectangularShape) {
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
				synchronized (model.getThermometers()) {
					for (Thermometer t : model.getThermometers()) {
						if (t.contains(rx, ry)) {
							contained = true;
							break;
						}
					}
				}
				if (!contained && !showGraph) {
					synchronized (model.getParts()) {
						for (Part p : model.getParts()) {
							if (p.contains(rx, ry)) {
								contained = true;
								break;
							}
						}
					}
				}
				setCursor(Cursor.getPredefinedCursor(contained ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
			}
			break;
		case POLYGON_MODE:
			if (!showGraph) {
				mouseMovedPoint.setLocation(x, y);
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
			graphRenderer.setScopeX(7200 * timeStep);
			photonLength = Math.max(5, timeStep * 0.1f);
		}
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
		} else if (selectedManipulable instanceof Thermometer) {
			Thermometer t = (Thermometer) selectedManipulable;
			Rectangle2D.Float r = (Rectangle2D.Float) t.getShape();
			int a = convertPointToPixelX(r.x);
			int b = convertPointToPixelY(r.y);
			int c = convertLengthToPixelX(r.width);
			int d = convertLengthToPixelY(r.height);
			if (anchor)
				setAnchorPointForRectangularShape(selectedSpot, a, b, c, d);
			movingShape = new MovingRoundRectangle(new RoundRectangle2D.Float(a, b, c, d, 0, 0));
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
		g.setColor(pixelAttribute != PIXEL_NONE ? Color.lightGray : Color.black);
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
