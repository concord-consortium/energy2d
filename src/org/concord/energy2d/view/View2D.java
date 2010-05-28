/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.model.Manipulable;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Photon;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.util.ContourMap;
import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
public class View2D extends JPanel implements PropertyChangeListener {

	public final static byte SELECT_MODE = 0;
	public final static byte RECTANGLE_MODE = 1;
	public final static byte ELLIPSE_MODE = 2;
	public final static byte POLYGON_MODE = 3;
	public final static byte THERMOMETER_MODE = 11;

	final static byte UPPER_LEFT = 0;
	final static byte LOWER_LEFT = 1;
	final static byte UPPER_RIGHT = 2;
	final static byte LOWER_RIGHT = 3;
	final static byte TOP = 4;
	final static byte BOTTOM = 5;
	final static byte LEFT = 6;
	final static byte RIGHT = 7;

	final static short[][] TEMPERATURE_COLOR_SCALE = { { 0, 0, 128 },
			{ 0, 128, 225 }, { 0, 225, 255 }, { 225, 175, 0 }, { 255, 0, 0 },
			{ 255, 255, 255 } };

	private static final long serialVersionUID = 1L;
	private final static int MINIMUM_MOUSE_DRAG_RESPONSE_INTERVAL = 20;
	private final static DecimalFormat TEMPERATURE_FORMAT = new DecimalFormat(
			"###.#");
	private Font smallFont = new Font(null, Font.PLAIN, 9);

	private RulerRenderer rulerRenderer;
	private GridRenderer gridRenderer;
	private Rainbow rainbow;
	private GraphRenderer graphRenderer;
	private ScalarDistributionRenderer temperatureRenderer;
	private VectorDistributionRenderer velocityRenderer;
	private boolean isothermOn;
	private boolean streamlineOn;
	private boolean showGraph;
	private boolean showRainbow;
	private boolean clockOn = true;

	private static Stroke thinStroke = new BasicStroke(1);
	private static Stroke moderateStroke = new BasicStroke(2);
	private static Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 1, new float[] { 2 }, 0);
	private static Color lightColor = new Color(255, 255, 255, 128);
	private float xmin, xmax, ymin, ymax;
	private int nx, ny;
	private float time;
	private JPopupMenu popupMenu;
	private Rectangle[] handle = new Rectangle[8];
	private boolean mouseBeingDragged;
	private MovingShape movingShape;
	private Point pressedPointRelative = new Point();
	private long mousePressedTime;
	private Point mouseReleasedPoint = new Point();
	private byte selectedSpot = -1;
	private Point anchorPoint = new Point();
	private AffineTransform scale, translate;
	private ContourMap isotherms;
	private ContourMap streamlines;
	private Polygon polygon;
	private float photonLength = 10;
	private byte actionMode = SELECT_MODE;
	private Point mousePressedPoint = new Point();
	private Rectangle rectangle = new Rectangle();
	private Ellipse2D.Float ellipse = new Ellipse2D.Float();

	Model2D model;
	private Manipulable selectedManipulable, copiedManipulable;
	private List<TextBox> textBoxes;
	private List<Picture> pictures;

	private JPopupMenu tipPopupMenu;
	private boolean runToggle;
	private DialogFactory dialogFactory;

	private List<ManipulationListener> manipulationListeners;

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
		createPopupMenu();
		dialogFactory = new DialogFactory(this);
		temperatureRenderer = new ScalarDistributionRenderer(
				TEMPERATURE_COLOR_SCALE);
		graphRenderer = new GraphRenderer(50, 50, 200, 200);
		rainbow = new Rainbow(TEMPERATURE_COLOR_SCALE);
		manipulationListeners = new ArrayList<ManipulationListener>();
	}

	public void setActionMode(byte mode) {
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

	public void addText(String text, int x, int y) {
		if (textBoxes == null)
			textBoxes = new ArrayList<TextBox>();
		textBoxes.add(new TextBox(text, x, y));
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

	void notifyManipulationListeners(Manipulable m, byte type) {
		if (manipulationListeners.isEmpty())
			return;
		ManipulationEvent e = new ManipulationEvent(this, m, type);
		for (ManipulationListener l : manipulationListeners) {
			l.manipulationOccured(e);
		}
	}

	public void setModel(Model2D model) {
		this.model = model;
		nx = model.getTemperature().length;
		ny = model.getTemperature()[0].length;
	}

	public void reset() {
		showGraph = false;
		runToggle = false;
		setTime(0);
	}

	public void setTime(float time) {
		this.time = time;
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

	public void setRainbowOn(boolean b) {
		showRainbow = b;
	}

	public boolean isRainbowOn() {
		return showRainbow;
	}

	public void setRainbowRectangle(int x, int y, int w, int h) {
		rainbow.setRect(x, y, w, h);
	}

	public void setGraphOn(boolean b) {
		showGraph = b;
	}

	public boolean isGraphOn() {
		return showGraph;
	}

	public void setVelocityOn(boolean b) {
		velocityRenderer = b ? new VectorDistributionRenderer(nx, ny) : null;
	}

	public boolean isVelocityOn() {
		return velocityRenderer != null;
	}

	public void setVelocitySpacing(int spacing) {
		if (velocityRenderer != null)
			velocityRenderer.setSpacing(spacing);
	}

	public void setStreamlineOn(boolean b) {
		streamlineOn = b;
		if (b) {
			if (streamlines == null)
				streamlines = new ContourMap();
			streamlines.setResolution(0.0001f);
		} else {
			streamlines = null;
		}
	}

	public boolean isStreamlineOn() {
		return streamlineOn;
	}

	public void setStreamlineResolution(float resolution) {
		if (streamlines != null)
			streamlines.setResolution(resolution);
	}

	public float getStreamlineResolution() {
		if (streamlines == null)
			return 5;
		return streamlines.getResolution();
	}

	public void setIsothermOn(boolean b) {
		isothermOn = b;
		if (b) {
			if (isotherms == null)
				isotherms = new ContourMap();
		} else {
			isotherms = null;
		}
	}

	public boolean isIsothermOn() {
		return isothermOn;
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
	}

	public float getMinimumTemperature() {
		return temperatureRenderer.getMinimum();
	}

	public void setMaximumTemperature(float max) {
		temperatureRenderer.setMaximum(max);
	}

	public float getMaximumTemperature() {
		return temperatureRenderer.getMaximum();
	}

	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	private void createPopupMenu() {

		if (popupMenu != null)
			return;

		popupMenu = new JPopupMenu();
		popupMenu.setInvoker(this);

		JMenuItem mi = new JMenuItem("Copy");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copiedManipulable = selectedManipulable;
			}
		});
		popupMenu.add(mi);

		mi = new JMenuItem("Cut");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedManipulable != null) {
					copiedManipulable = selectedManipulable;
					notifyManipulationListeners(selectedManipulable,
							ManipulationEvent.DELETE);
					setSelectedManipulable(null);
				}
			}
		});
		popupMenu.add(mi);

		mi = new JMenuItem("Paste");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (copiedManipulable instanceof Part) {
					Part p = (Part) copiedManipulable;
					model.addPart(p.duplicate(
							convertPixelToPointX(mouseReleasedPoint.x),
							convertPixelToPointY(mouseReleasedPoint.y)));
					model.refreshPowerArray();
					model.refreshTemperatureBoundaryArray();
					model.refreshMaterialPropertyArrays();
					model.setInitialTemperature();
				} else if (copiedManipulable instanceof Thermometer) {
					model.addThermometer(
							convertPixelToPointX(mouseReleasedPoint.x),
							convertPixelToPointY(mouseReleasedPoint.y));
				}
				repaint();
			}
		});
		popupMenu.add(mi);
		popupMenu.addSeparator();

		mi = new JMenuItem("Properties");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog d = dialogFactory
						.createDialog(selectedManipulable != null ? selectedManipulable
								: model);
				if (d != null)
					d.setVisible(true);
			}
		});
		popupMenu.add(mi);

		mi = new JMenuItem("View Options");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog d = dialogFactory.createDialog(View2D.this);
				if (d != null)
					d.setVisible(true);
			}
		});
		popupMenu.add(mi);

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
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		Stroke stroke = g2.getStroke();
		g.setColor(getBackground());
		g.fillRect(0, 0, w, h);
		drawTemperatureField(g2);
		drawParts(g2);
		if (isotherms != null) {
			g2.setStroke(thinStroke);
			isotherms.render(g2, getSize(), model.getTemperature());
		}
		if (streamlines != null) {
			g2.setStroke(thinStroke);
			streamlines.render(g2, getSize(), model.getStreamFunction());
		}
		if (selectedManipulable != null) {
			for (Rectangle r : handle) {
				if (r.x != 0 || r.y != 0) {
					g2.setColor(Color.yellow);
					g2.fill(r);
					g2.setColor(Color.black);
					g2.draw(r);
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
			g2.setColor(Color.white);
			rulerRenderer.render(this, g2);
		}
		if (showRainbow)
			rainbow.render(this, g2, temperatureRenderer.getMaximum(),
					temperatureRenderer.getMinimum());
		if (velocityRenderer != null)
			velocityRenderer.render(model.getXVelocity(), model.getYVelocity(),
					this, g2);
		drawThermometers(g2);
		drawPhotons(g2);
		drawTextBoxes(g2);
		drawPictures(g2);
		if (showGraph) {
			if (!model.getThermometers().isEmpty()) {
				graphRenderer.setDrawFrame(true);
				synchronized (model.getThermometers()) {
					for (Thermometer t : model.getThermometers()) {
						graphRenderer.render(this, g2, t.getData(),
								selectedManipulable == t);
					}
				}
			}
		}
		if (clockOn) {
			g2.setColor(Color.white);
			g2.drawString(MiscUtil.formatTime((int) time), w - 60, 16);
		}

		switch (actionMode) {
		case RECTANGLE_MODE:
			g2.setColor(Color.white);
			g2.setStroke(dashed);
			g2.draw(rectangle);
			break;
		case ELLIPSE_MODE:
			g2.setColor(Color.white);
			g2.setStroke(dashed);
			g2.draw(ellipse);
			break;
		}

		g2.setStroke(stroke);
		drawFrank(g2, getWidth() - 85, getHeight() - 16);

	}

	private void drawThermometers(Graphics2D g) {
		List<Thermometer> thermometers = model.getThermometers();
		if (thermometers.isEmpty())
			return;
		g.setStroke(thinStroke);
		Symbol s = Symbol.get(Symbol.THERMOMETER);
		Rectangle2D.Float r = (Rectangle2D.Float) thermometers.get(0)
				.getShape();
		s.setIconWidth((int) (r.width * getWidth() / (xmax - xmin)));
		s.setIconHeight((int) (r.height * getHeight() / (ymax - ymin)));
		float dx = s.getIconWidth() * 0.5f;
		float dy = s.getIconHeight() * 0.5f;
		float temp;
		String str;
		g.setFont(smallFont);
		int x, y;
		float rx, ry;
		int ix, iy;
		synchronized (thermometers) {
			for (Thermometer t : thermometers) {
				rx = (t.getX() - xmin) / (xmax - xmin);
				ry = (t.getY() - ymin) / (ymax - ymin);
				if (rx >= 0 && rx < 1 && ry >= 0 && ry < 1) {
					x = (int) (rx * getWidth() - dx);
					y = (int) (ry * getHeight() - dy);
					s.paintIcon(this, g, x, y);
					ix = (int) (nx * rx);
					iy = (int) (ny * ry);
					temp = model.getTemperature()[ix][iy];
					if (!Float.isNaN(temp)) {
						g.setColor(Color.white);
						str = TEMPERATURE_FORMAT.format(temp) + '\u2103';
						centerString(str, g, x + s.getIconWidth() / 2, y - 5);
					}
				}
			}
		}
	}

	private static void centerString(String s, Graphics2D g, int x, int y) {
		int stringWidth = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x - stringWidth / 2, y);
	}

	private Color getPartColor(Part p) {
		if (p.getPower() > 0)
			return new Color(0xFFFF00);
		if (p.getPower() < 0)
			return new Color(0xB0C4DE);
		if (!Float.isNaN(p.getTemperature()))
			return new Color(temperatureRenderer.getColor(p.getTemperature()));
		return p.getColor();
	}

	private void drawParts(Graphics2D g) {
		List<Part> parts = model.getParts();
		if (parts.isEmpty())
			return;
		Stroke oldStroke = g.getStroke();
		g.setStroke(moderateStroke);
		synchronized (parts) {
			for (Part p : parts) {
				Shape s = p.getShape();
				if (s instanceof Ellipse2D.Float) {
					Ellipse2D.Float e = (Ellipse2D.Float) s;
					int x = convertPointToPixelX(e.x);
					int y = convertPointToPixelY(e.y);
					int w = convertLengthToPixelX(e.width);
					int h = convertLengthToPixelY(e.height);
					if (p.isFilled()) {
						g.setColor(getPartColor(p));
						g.fillOval(x, y, w, h);
					}
					g.setColor(Color.black);
					g.drawOval(x - 1, y - 1, w + 2, h + 2);
				} else if (s instanceof Rectangle2D.Float) {
					Rectangle2D.Float r = (Rectangle2D.Float) s;
					int x = convertPointToPixelX(r.x);
					int y = convertPointToPixelY(r.y);
					int w = convertLengthToPixelX(r.width);
					int h = convertLengthToPixelY(r.height);
					if (p.isFilled()) {
						g.setColor(getPartColor(p));
						g.fillRect(x, y, w, h);
					}
					g.setColor(Color.black);
					g.drawRect(x - 1, y - 1, w + 2, h + 2);
				} else if (s instanceof Area) {
					if (scale == null)
						scale = new AffineTransform();
					scale.setToScale(getWidth() / (xmax - xmin), getHeight()
							/ (ymax - ymin));
					Area area = (Area) s;
					area.transform(scale);
					if (p.isFilled()) {
						g.setColor(getPartColor(p));
						g.fill(area);
					}
					g.setColor(Color.black);
					g.draw(area);
					scale.setToScale((xmax - xmin) / getWidth(), (ymax - ymin)
							/ getHeight());
					area.transform(scale);
				} else if (s instanceof Polygon2D) {
					Polygon2D q = (Polygon2D) s;
					int n = q.getVertexCount();
					if (polygon == null)
						polygon = new Polygon();
					else
						polygon.reset();
					int x, y;
					Point2D.Float v;
					for (int i = 0; i < n; i++) {
						v = q.getVertex(i);
						x = convertPointToPixelX(v.x);
						y = convertPointToPixelY(v.y);
						polygon.addPoint(x, y);
					}
					if (p.isFilled()) {
						g.setColor(getPartColor(p));
						g.fill(polygon);
					}
					g.setColor(Color.black);
					g.draw(polygon);
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
		for (TextBox x : textBoxes) {
			g.setFont(new Font(x.getName(), x.getStyle(), x.getSize()));
			g.setColor(x.getColor());
			g.drawString(x.getText(), x.getX(), x.getY());
		}
		g.setFont(oldFont);
		g.setColor(oldColor);
	}

	private void drawPictures(Graphics2D g) {
		if (pictures == null || pictures.isEmpty())
			return;
		for (Picture x : pictures) {
			x.getImage().paintIcon(this, g, x.getX(), x.getY());
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
				g.drawLine((int) (x - photonLength * p.getVx() * r),
						(int) (y - photonLength * p.getVy() * r), x, y);
			}
		}
	}

	private void drawTemperatureField(Graphics2D g) {
		temperatureRenderer.render(model.getTemperature(), this, g);
	}

	private void setAnchorPointForRectangularShape(byte i, float x, float y,
			float w, float h) {
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
			for (Part p : model.getParts()) {
				if (p.contains(rx, ry)) {
					setSelectedManipulable(p);
					return;
				}
			}
		}
	}

	private void setSelectedManipulable(Manipulable m) {
		if (selectedManipulable != null)
			selectedManipulable.setSelected(false);
		selectedManipulable = m;
		if (selectedManipulable != null) {
			selectedManipulable.setSelected(true);
			Shape shape = selectedManipulable.getShape();
			boolean b = false;
			if (shape instanceof Ellipse2D.Float) {
				Ellipse2D.Float e = (Ellipse2D.Float) shape;
				b = e.width < (xmax - xmin) / nx + 0.01f
						|| e.height < (ymax - ymin) / ny + 0.01f;
			}
			if (!b)
				HandleSetter.setRects(this, selectedManipulable, handle);
		}
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
			translate.setToTranslation(x - (float) rect.getX(), y
					- (float) rect.getY());
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
				translateManipulableBy(selectedManipulable, -.01f
						* (xmax - xmin), 0);
				break;
			case KeyEvent.VK_RIGHT:
				translateManipulableBy(selectedManipulable,
						.01f * (xmax - xmin), 0);
				break;
			case KeyEvent.VK_DOWN:
				translateManipulableBy(selectedManipulable, 0,
						.01f * (ymax - ymin));
				break;
			case KeyEvent.VK_UP:
				translateManipulableBy(selectedManipulable, 0, -.01f
						* (ymax - ymin));
				break;
			}
			setSelectedManipulable(selectedManipulable);
		}
		repaint();
		e.consume();
	}

	private void processKeyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_BACK_SPACE:
			if (selectedManipulable != null) {
				notifyManipulationListeners(selectedManipulable,
						ManipulationEvent.DELETE);
				setSelectedManipulable(null);
			}
			break;
		case KeyEvent.VK_R:
			notifyManipulationListeners(null,
					runToggle ? ManipulationEvent.STOP : ManipulationEvent.RUN);
			runToggle = !runToggle;
			break;
		case KeyEvent.VK_T:
			notifyManipulationListeners(null, ManipulationEvent.RESET);
			break;
		case KeyEvent.VK_S:
			notifyManipulationListeners(null, ManipulationEvent.SUN_SHINE);
			break;
		case KeyEvent.VK_Q:
			notifyManipulationListeners(null,
					ManipulationEvent.SUN_ANGLE_INCREASE);
			break;
		case KeyEvent.VK_W:
			notifyManipulationListeners(null,
					ManipulationEvent.SUN_ANGLE_DECREASE);
			break;
		case KeyEvent.VK_G:
			showGraph = !showGraph;
			break;
		}
		repaint();
		e.consume();
	}

	private void processMousePressed(MouseEvent e) {
		mousePressedTime = System.currentTimeMillis();
		requestFocusInWindow();
		if (showGraph) {
			e.consume();
			return;
		}
		int x = e.getX();
		int y = e.getY();
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
			mousePressedPoint.setLocation(x, y);
			break;
		}
		repaint();
		e.consume();
	}

	private void processMouseDragged(MouseEvent e) {
		if (MiscUtil.isRightClick(e))
			return;
		if (showGraph) {
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
					double a = s.getX(), b = s.getY(), c = s.getWidth(), d = s
							.getHeight();
					if (selectedSpot == -1) {
						// x+width/2+pressedPointRelative.x=mouse_x
						a = x - pressedPointRelative.x - c * 0.5;
						b = y - pressedPointRelative.y - d * 0.5;
						setCursor(Cursor
								.getPredefinedCursor(Cursor.MOVE_CURSOR));
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
							setCursor(Cursor
									.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
						setCursor(Cursor
								.getPredefinedCursor(Cursor.MOVE_CURSOR));
					} else {
						if (selectedManipulable instanceof Part) {
							s.xpoints[selectedSpot] = x;
							s.ypoints[selectedSpot] = y;
							setCursor(Cursor
									.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
						}
					}
				} else {
					showTip(
							"<html><font color=red>The selected object is not draggable!</font></html>",
							x, y, 500);
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
		}
		repaint();
		e.consume();
	}

	private void processMouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseReleasedPoint.setLocation(x, y);
		if (showGraph) {
			if (graphRenderer.buttonContains(GraphRenderer.CLOSE_BUTTON, x, y)) {
				showGraph = false;
			} else if (graphRenderer.buttonContains(
					GraphRenderer.X_EXPAND_BUTTON, x, y)) {
				graphRenderer.expandScopeX();
			} else if (graphRenderer.buttonContains(
					GraphRenderer.X_SHRINK_BUTTON, x, y)) {
				graphRenderer.shrinkScopeX();
			} else if (graphRenderer.buttonContains(
					GraphRenderer.Y_EXPAND_BUTTON, x, y)) {
				graphRenderer.expandScopeY();
			} else if (graphRenderer.buttonContains(
					GraphRenderer.Y_SHRINK_BUTTON, x, y)) {
				graphRenderer.shrinkScopeY();
			}
			repaint();
			e.consume();
			movingShape = null;
			mouseBeingDragged = false;
			return;
		}
		if (e.getClickCount() >= 2) {
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
			if (movingShape != null && mouseBeingDragged
					&& selectedManipulable != null) {
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
								float w2 = convertPixelToLengthX((int) r
										.getWidth());
								float h2 = convertPixelToLengthY((int) r
										.getHeight());
								resizeManipulableTo(selectedManipulable, x2,
										y2, w2, h2);
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
									p
											.setVertex(
													i,
													convertPixelToPointX(p0.xpoints[i]),
													convertPixelToPointY(p0.ypoints[i]));
								}
								setSelectedManipulable(selectedManipulable);
								notifyManipulationListeners(
										selectedManipulable,
										ManipulationEvent.RESIZE);
							}
						}
					}
				} else {
					showTip(
							"<html><font color=red>The selected object is not draggable!</font></html>",
							x, y, 500);
				}
			} else {
				selectManipulable(x, y);
			}
			break;
		case RECTANGLE_MODE:
			if (rectangle.width * rectangle.height > 9) {
				model.addRectangularPart(convertPixelToPointX(rectangle.x),
						convertPixelToPointY(rectangle.y),
						convertPixelToLengthX(rectangle.width),
						convertPixelToLengthY(rectangle.height));
				model.refreshPowerArray();
				model.refreshTemperatureBoundaryArray();
				model.refreshMaterialPropertyArrays();
				model.setInitialTemperature();
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
			}
			ellipse.setFrame(-1000, -1000, 0, 0);
			break;
		case THERMOMETER_MODE:
			model.addThermometer(convertPixelToPointX(x),
					convertPixelToPointY(y));
			break;
		}
		repaint();
		e.consume();
		movingShape = null;
		mouseBeingDragged = false;
	}

	private void processMouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (showGraph) {
			if (graphRenderer.buttonContains(GraphRenderer.CLOSE_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(
					GraphRenderer.X_EXPAND_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(
					GraphRenderer.X_SHRINK_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(
					GraphRenderer.Y_EXPAND_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (graphRenderer.buttonContains(
					GraphRenderer.Y_SHRINK_BUTTON, x, y)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			return;
		}
		switch (actionMode) {
		case SELECT_MODE:
			int iSpot = -1;
			if (selectedManipulable instanceof Part) {
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
							setCursor(Cursor
									.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
							break;
						case LOWER_LEFT:
							setCursor(Cursor
									.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
							break;
						case UPPER_RIGHT:
							setCursor(Cursor
									.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
							break;
						case LOWER_RIGHT:
							setCursor(Cursor
									.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
							break;
						case TOP:
							setCursor(Cursor
									.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
							break;
						case BOTTOM:
							setCursor(Cursor
									.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
							break;
						case LEFT:
							setCursor(Cursor
									.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
							break;
						case RIGHT:
							setCursor(Cursor
									.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
							break;
						}
					} else {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.HAND_CURSOR));
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
				if (!contained) {
					synchronized (model.getParts()) {
						for (Part p : model.getParts()) {
							if (p.contains(rx, ry)) {
								contained = true;
								break;
							}
						}
					}
				}
				setCursor(Cursor
						.getPredefinedCursor(contained ? Cursor.MOVE_CURSOR
								: Cursor.DEFAULT_CURSOR));
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
			graphRenderer.setScopeX(3600 * timeStep);
			photonLength = Math.max(5, timeStep * 0.1f);
		}
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
				movingShape = new MovingRoundRectangle(
						new RoundRectangle2D.Float(a, b, c, d, 0, 0));
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
				// if (anchor)
				// setAnchorPointForRectangularShape(selectedSpot, a, b, c, d);
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
			movingShape = new MovingRoundRectangle(new RoundRectangle2D.Float(
					a, b, c, d, 0, 0));
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

	private void drawFrank(Graphics g, int x, int y) {
		g.setFont(new Font("Arial", Font.BOLD, 16));
		g.setColor(Color.white);
		String s = "Energy2D";
		g.drawString(s, x + 1, y - 1);
		g.drawString(s, x + 1, y + 1);
		g.drawString(s, x - 1, y - 1);
		g.drawString(s, x - 1, y + 1);
		g.setColor(Color.black);
		g.drawString(s, x, y);
	}

}
