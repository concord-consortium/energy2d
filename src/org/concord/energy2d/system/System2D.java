/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.system;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.concord.energy2d.event.IOEvent;
import org.concord.energy2d.event.IOListener;
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

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * @author Charles Xie
 * 
 */
public class System2D extends JApplet implements MwService, VisualizationListener, ManipulationListener {

	final static String BRAND_NAME = "Energy2D V0.5";

	Model2D model;
	View2D view;
	private Scripter2D scripter;
	private ExecutorService threadService;

	private SAXParser saxParser;
	private DefaultHandler saxHandler;
	private XmlEncoder encoder;
	private File currentFile;
	private URL currentURL;
	private String currentModel;
	private boolean saved = true;

	Runnable clickRun, clickStop, clickReset, clickReload;
	private JButton buttonRun, buttonStop, buttonReset, buttonReload;
	private List<IOListener> ioListeners;
	private JFrame owner;
	private static Preferences preferences;

	public System2D() {

		// Locale.setDefault(Locale.US); for applets, this is a security violation
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		model = new Model2D();
		model.addVisualizationListener(this);
		model.addManipulationListener(this);
		view = new View2D();
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

		createActions();

	}

	public View2D getView() {
		return view;
	}

	public Model2D getModel() {
		return model;
	}

	public void setOwner(JFrame owner) {
		this.owner = owner;
	}

	private void createActions() {

		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Helper.showScriptDialog(System2D.this);
			}
		};
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, true);
		a.putValue(Action.NAME, "Script");
		a.putValue(Action.ACCELERATOR_KEY, ks);
		view.getInputMap().put(ks, "Script");
		view.getActionMap().put("Script", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				view.createDialog(model, true);
			}
		};
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK);
		a.putValue(Action.NAME, "Property");
		a.putValue(Action.ACCELERATOR_KEY, ks);
		view.getInputMap().put(ks, "Property");
		view.getActionMap().put("Property", a);

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
		System.out.println(BRAND_NAME + " initialized.");
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
				view.toggleRun();
			}
		});
	}

	public void runSteps(final int n) {
		executeInThreadService(new Runnable() {
			public void run() {
				// TODO
			}
		});
	}

	public void stop() {
		model.stop();
		view.toggleRun();
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

	void saveApplet(File file) {
		new AppletConverter(this).write(file);
	}

	private void loadStateApp(Reader reader) throws IOException {
		stop();
		reset();
		clear();
		loadState(reader);
	}

	private void loadState(Reader reader) throws IOException {
		saved = true;
		stop();
		if (reader == null)
			return;
		try {
			saxParser.parse(new InputSource(reader), saxHandler);
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (buttonStop != null)
					buttonStop.doClick();
			}
		});
	}

	private void loadStateApp(InputStream is) throws IOException {
		stop();
		reset();
		clear();
		loadState(is);
	}

	public void loadState(InputStream is) throws IOException {
		saved = true;
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

	void saveState(Writer writer) throws IOException {
		if (clickStop != null) {
			EventQueue.invokeLater(clickStop);
		} else {
			stop();
		}
		if (writer == null)
			return;
		try {
			writer.write(encoder.encode().toCharArray());
		} finally {
			writer.close();
		}
		saved = true;
	}

	public void saveState(OutputStream os) throws IOException {
		if (clickStop != null) {
			EventQueue.invokeLater(clickStop);
		} else {
			stop();
		}
		if (os == null)
			return;
		try {
			os.write(encoder.encode().getBytes());
		} finally {
			os.close();
		}
		saved = true;
	}

	void loadFile(File file) {
		setReloadButtonEnabled(true);
		if (file == null)
			return;
		try {
			// loadStateApp(new FileInputStream(file));
			loadStateApp(new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")));
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), e.getLocalizedMessage(), "File error", JOptionPane.ERROR_MESSAGE);
		}
		notifyIOListeners(new IOEvent(IOEvent.FILE_INPUT, this));
		currentFile = file;
		currentModel = null;
		currentURL = null;
		setFrameTitle();
	}

	void loadModel(String name) {
		setReloadButtonEnabled(true);
		if (name == null)
			return;
		if (!askSaveBeforeLoading())
			return;
		try {
			loadStateApp(System2D.class.getResourceAsStream(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyIOListeners(new IOEvent(IOEvent.FILE_INPUT, this));
		currentModel = name;
		currentFile = null;
		currentURL = null;
		setFrameTitle();
	}

	void loadURL(URL url) throws IOException {
		setReloadButtonEnabled(true);
		if (url == null)
			return;
		if (!askSaveBeforeLoading())
			return;
		loadStateApp(url.openConnection().getInputStream());
		notifyIOListeners(new IOEvent(IOEvent.FILE_INPUT, this));
		currentURL = url;
		currentFile = null;
		currentModel = null;
		setFrameTitle();
	}

	public void reload() {
		if (currentFile != null) {
			loadFile(currentFile);
			return;
		}
		if (currentModel != null) {
			loadModel(currentModel);
			return;
		}
		if (currentURL != null) {
			try {
				loadURL(currentURL);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	private void setReloadButtonEnabled(final boolean b) {
		if (buttonReload == null)
			return;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				buttonReload.setEnabled(b);
			}
		});
	}

	int askSaveOption() {
		if (saved || owner == null || currentModel != null || currentURL != null)
			return JOptionPane.NO_OPTION;
		return JOptionPane.showConfirmDialog(owner, "Do you want to save the changes?", "Energy2D", JOptionPane.YES_NO_CANCEL_OPTION);
	}

	boolean askSaveBeforeLoading() {
		if (owner == null) // not an application
			return true;
		switch (askSaveOption()) {
		case JOptionPane.YES_OPTION:
			Action a = null;
			if (currentFile != null) {
				a = view.getActionMap().get("Save");
			} else {
				a = view.getActionMap().get("SaveAs");
			}
			if (a != null)
				a.actionPerformed(null);
			return true;
		case JOptionPane.NO_OPTION:
			return true;
		default:
			return false;
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
		setFrameTitle();
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
		saved = false;
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
		case ManipulationEvent.PROPERTY_CHANGE:
			saved = false;
			break;
		case ManipulationEvent.OBJECT_ADDED:
			saved = false;
			break;
		case ManipulationEvent.DELETE:
			if (target instanceof Part)
				model.removePart((Part) target);
			else if (target instanceof Thermometer)
				model.getThermometers().remove((Thermometer) target);
			saved = false;
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
		case ManipulationEvent.AUTO_STOP:
			if (clickStop != null) {
				EventQueue.invokeLater(clickStop);
			} else {
				stop();
			}
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view), "This simulation was designed to automatically stopped here.");
				}
			});
			break;
		case ManipulationEvent.SUN_SHINE:
			model.setSunny(!model.isSunny());
			model.refreshPowerArray();
			saved = false;
			break;
		case ManipulationEvent.SUN_ANGLE_INCREASE:
			float a = model.getSunAngle() + (float) Math.PI / 18;
			model.setSunAngle(Math.min(a, (float) Math.PI));
			model.refreshPowerArray();
			saved = false;
			break;
		case ManipulationEvent.SUN_ANGLE_DECREASE:
			a = model.getSunAngle() - (float) Math.PI / 18;
			model.setSunAngle(Math.max(a, 0));
			model.refreshPowerArray();
			saved = false;
			break;
		}
		if (target instanceof Part) {
			Part p = (Part) target;
			model.refreshMaterialPropertyArrays();
			model.refreshPowerArray();
			model.refreshTemperatureBoundaryArray();
			if (p.getEmissivity() > 0)
				model.getPhotons().clear();
			saved = false;
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
		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(20, 10));
		p.add(spacer);
		buttonReload = new JButton("Reload");
		buttonReload.setEnabled(false);
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

	void addIOListener(IOListener l) {
		if (ioListeners == null)
			ioListeners = new ArrayList<IOListener>();
		if (!ioListeners.contains(l))
			ioListeners.add(l);
	}

	void removeIOListener(IOListener l) {
		if (ioListeners == null)
			return;
		ioListeners.remove(l);
	}

	private void notifyIOListeners(IOEvent e) {
		setFrameTitle();
		if (ioListeners == null)
			return;
		for (IOListener x : ioListeners)
			x.ioOccured(e);
	}

	private void setFrameTitle() {
		if (owner == null)
			return;
		if (currentFile != null) {
			owner.setTitle(BRAND_NAME + ": " + currentFile);
		} else if (currentModel != null) {
			owner.setTitle(BRAND_NAME + ": " + currentModel);
		} else if (currentURL != null) {
			owner.setTitle(BRAND_NAME + ": " + currentURL);
		} else {
			owner.setTitle(BRAND_NAME);
		}
	}

	static void savePreferences(System2D box) {
		if (preferences == null || box.owner == null)
			return;
		MenuBar menuBar = (MenuBar) box.owner.getJMenuBar();
		preferences.put("Latest E2D Path", menuBar.getLatestPath("e2d"));
		preferences.put("Latest HTM Path", menuBar.getLatestPath("htm"));
		String[] recentFiles = menuBar.getRecentFiles();
		if (recentFiles != null) {
			int n = recentFiles.length;
			if (n > 0)
				for (int i = 0; i < n; i++)
					preferences.put("Recent File " + i, recentFiles[n - i - 1]);
		}
	}

	public static void main(String[] args) {

		Locale.setDefault(Locale.US);

		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", BRAND_NAME);
		}

		if (preferences == null)
			preferences = Preferences.userNodeForPackage(System2D.class);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) (screen.height * 0.6);

		final System2D box = new System2D();
		box.view.setPreferredSize(new Dimension(w, w));
		final JFrame frame = new JFrame();
		frame.setIconImage(new ImageIcon(System2D.class.getResource("resources/frame.png")).getImage());
		MenuBar menuBar = new MenuBar(box, frame);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setContentPane(box.getContentPane());
		ToolBar toolBar = new ToolBar(box);
		box.addIOListener(toolBar);
		box.view.addManipulationListener(toolBar);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		frame.getContentPane().add(box.createButtonPanel(), BorderLayout.SOUTH);
		frame.setLocation((screen.height - w) / 8, (screen.height - w) / 8);
		frame.setTitle(BRAND_NAME);
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Action a = box.view.getActionMap().get("Quit");
				if (a != null)
					a.actionPerformed(null);
			}
		});
		box.owner = frame;

		menuBar.setLatestPath(preferences.get("Latest E2D Path", null), "e2d");
		menuBar.setLatestPath(preferences.get("Latest HTM Path", null), "htm");
		menuBar.addRecentFile(preferences.get("Recent File 0", null));
		menuBar.addRecentFile(preferences.get("Recent File 1", null));
		menuBar.addRecentFile(preferences.get("Recent File 2", null));
		menuBar.addRecentFile(preferences.get("Recent File 3", null));

		if (System.getProperty("os.name").startsWith("Mac")) {
			Application app = new Application();
			app.setEnabledPreferencesMenu(true);
			app.addApplicationListener(new ApplicationAdapter() {
				public void handleQuit(ApplicationEvent e) {
					Action a = box.view.getActionMap().get("Quit");
					if (a != null)
						a.actionPerformed(null);
					// e.setHandled(true); //DO NOT CALL THIS!!!
				}

				public void handlePreferences(ApplicationEvent e) {
					e.setHandled(true);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
						}
					});
				}

				public void handleAbout(ApplicationEvent e) {
					Helper.showAbout(frame);
					e.setHandled(true);
				}
			});
		}

	}

}
