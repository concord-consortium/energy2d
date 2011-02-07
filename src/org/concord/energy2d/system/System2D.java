/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.system;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.event.VisualizationEvent;
import org.concord.energy2d.event.VisualizationListener;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.view.View2D;
import org.concord.modeler.MwService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Charles Xie
 * 
 */
public class System2D extends JApplet implements MwService, VisualizationListener,
		ManipulationListener {

	private static final long serialVersionUID = 1L;
	Model2D model;
	View2D view;
	private Scripter2D scripter;
	private ExecutorService threadService;

	private SAXParser saxParser;
	private DefaultHandler saxHandler;
	private XmlEncoder encoder;
	private File currentFile;
	private String currentModel;

	Runnable clickRun, clickStop, clickReset, clickReload;
	private JButton buttonRun, buttonStop, buttonReset, buttonReload;

	public System2D() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		model = new Model2D();
		model.addVisualizationListener(this);
		view = new View2D(this);
		view.addManipulationListener(this);
		view.setModel(model);
		view.setPreferredSize(new Dimension(400, 400));
		view.setBorder(BorderFactory.createEtchedBorder());
		view.setArea(0, model.getLx(), 0, model.getLy());
		model.addPropertyChangeListener(view);
		getContentPane().add(view, BorderLayout.CENTER);

		encoder = new XmlEncoder(this);
		saxHandler = new XmlDecoder(this);
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void init() {
		String s = null;
		try {
			s = getParameter("script");
		} catch (Exception e) {
			s = null;
		}
		if (s != null) {
			runNativeScript(s);
		}
		view.repaint();
	}

	void executeInThreadService(Runnable r) {
		if (threadService == null)
			threadService = Executors.newFixedThreadPool(1);
		threadService.execute(r);
	}

	public void run() {
		executeInThreadService(new Runnable() {
			public void run() {
				model.run();
			}
		});
	}

	public void runSteps(final int n) {
		executeInThreadService(new Runnable() {
			public void run() {
			}
		});
	}

	public void stop() {
		model.stop();
	}

	public void reset() {
		model.reset();
		view.reset();
		view.repaint();
	}

	public void initialize() {
		clear();
		init();
	}

	public void clear() {
		model.clear();
		view.clear();
		view.repaint();
	}

	public void destroy() {
		stop();
		try {
			if (threadService != null && !threadService.isShutdown()) {
				threadService.shutdownNow();
			}
		} catch (Throwable t) {
		}
	}

	public JPopupMenu getPopupMenu() {
		return view.getPopupMenu();
	}

	public Component getSnapshotComponent() {
		return view;
	}

	void loadStateApp(InputStream is) throws IOException {
		stop();
		reset();
		clear();
		loadState(is);
	}

	public void loadState(InputStream is) throws IOException {
		stop();
		if (is == null)
			return;
		try {
			saxParser.parse(new InputSource(is), saxHandler);
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (buttonStop != null)
					buttonStop.doClick();
			}
		});
	}

	public void saveState(OutputStream os) throws IOException {
		stop();
		if (os == null)
			return;
		try {
			os.write(encoder.encode().getBytes());
		} finally {
			os.close();
		}
	}

	void loadFile(File file) {
		currentFile = file;
		currentModel = null;
		try {
			loadStateApp(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void loadModel(String name) {
		currentModel = name;
		currentFile = null;
		try {
			loadStateApp(System2D.class.getResourceAsStream(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void reload() {
		if (currentFile == null) {
			if (currentModel != null)
				loadModel(currentModel);
		} else {
			loadFile(currentFile);
		}
	}

	void setCurrentModel(String name) {
		currentModel = name;
	}

	public String getCurrentModel() {
		return currentModel;
	}

	void setCurrentFile(File file) {
		currentFile = file;
	}

	File getCurrentFile() {
		return currentFile;
	}

	public boolean needExecutorService() {
		return true;
	}

	public String runNativeScript(String script) {
		if (script == null)
			return null;
		if (scripter == null)
			scripter = new Scripter2D(this);
		scripter.executeScript(script);
		return null;
	}

	public Scripter2D getScripter() {
		if (scripter == null)
			scripter = new Scripter2D(this);
		return scripter;
	}

	public void setEditable(boolean b) {
	}

	public void setExecutorService(ExecutorService service) {
		threadService = service;
	}

	public void visualizationRequested(VisualizationEvent e) {
		view.repaint();
		view.setTime(model.getTime());
	}

	public void manipulationOccured(ManipulationEvent e) {
		Object target = e.getTarget();
		switch (e.getType()) {
		case ManipulationEvent.DELETE:
			if (target instanceof Part)
				model.removePart((Part) target);
			else if (target instanceof Thermometer)
				model.getThermometers().remove((Thermometer) target);
			break;
		case ManipulationEvent.RUN:
			if (clickRun != null) {
				EventQueue.invokeLater(clickRun);
			} else {
				run();
			}
			break;
		case ManipulationEvent.STOP:
			if (clickStop != null) {
				EventQueue.invokeLater(clickStop);
			} else {
				stop();
			}
			break;
		case ManipulationEvent.RESET:
			if (clickReset != null) {
				EventQueue.invokeLater(clickReset);
			} else {
				reset();
			}
			break;
		case ManipulationEvent.RELOAD:
			if (clickReload != null) {
				EventQueue.invokeLater(clickReload);
			} else {
				reload();
			}
			break;
		case ManipulationEvent.SUN_SHINE:
			model.setSunny(!model.isSunny());
			model.refreshPowerArray();
			break;
		case ManipulationEvent.SUN_ANGLE_INCREASE:
			float a = model.getSunAngle() + (float) Math.PI / 18;
			model.setSunAngle(Math.min(a, (float) Math.PI));
			model.refreshPowerArray();
			break;
		case ManipulationEvent.SUN_ANGLE_DECREASE:
			a = model.getSunAngle() - (float) Math.PI / 18;
			model.setSunAngle(Math.max(a, 0));
			model.refreshPowerArray();
			break;
		}
		if (target instanceof Part) {
			Part p = (Part) target;
			model.refreshMaterialPropertyArrays();
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			if (p.getEmissivity() > 0)
				model.getPhotons().clear();
		}
		view.repaint();
	}

	private JPanel createButtonPanel() {
		JPanel p = new JPanel();
		buttonRun = new JButton("Run");
		buttonRun.setToolTipText("Run the simulation");
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
				buttonRun.setEnabled(false);
				buttonStop.setEnabled(true);
			}
		});
		p.add(buttonRun);
		buttonStop = new JButton("Stop");
		buttonStop.setEnabled(false);
		buttonStop.setToolTipText("Stop the simulation");
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
			}
		});
		p.add(buttonStop);
		buttonReset = new JButton("Reset");
		buttonReset.setToolTipText("Reset the simulation");
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
			}
		});
		p.add(buttonReset);
		buttonReload = new JButton("Reload");
		buttonReload.setToolTipText("Reload the initial configurations");
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
			}
		});
		p.add(buttonReload);
		clickRun = new Runnable() {
			public void run() {
				buttonRun.doClick();
			}
		};
		clickStop = new Runnable() {
			public void run() {
				buttonStop.doClick();
			}
		};
		clickReset = new Runnable() {
			public void run() {
				buttonReset.doClick();
			}
		};
		clickReload = new Runnable() {
			public void run() {
				buttonReload.doClick();
			}
		};
		return p;
	}

	public static void main(String[] args) {

		final System2D box = new System2D();
		box.view.setPreferredSize(new Dimension(600, 600));
		box.view.setFrankOn(false);

		final JFrame frame = new JFrame();
		frame.setIconImage(new ImageIcon(System2D.class.getResource("resources/frame.png"))
				.getImage());
		MenuBar menuBar = new MenuBar(box, frame);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(box.getContentPane());
		ToolBar toolBar = new ToolBar(box);
		menuBar.addIOListener(toolBar);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		frame.getContentPane().add(box.createButtonPanel(), BorderLayout.SOUTH);
		frame.setLocation(100, 100);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e) {
				File file = box.getCurrentFile();
				frame.setTitle(file != null ? "Energy2D V0.2: " + file : "Energy2D: V0.2");
			}

			public void windowLostFocus(WindowEvent e) {
			}
		});

	}

}
