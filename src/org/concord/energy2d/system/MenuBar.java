/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.util.FileChooser;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.util.ScreenshotSaver;

/**
 * @author Charles Xie
 * 
 */
class MenuBar extends JMenuBar {

	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");

	FileChooser e2dFileChooser;
	private FileChooser htmFileChooser;

	private FileFilter e2dFilter = new FileFilter() {

		public boolean accept(File file) {
			if (file == null)
				return false;
			if (file.isDirectory())
				return true;
			String filename = file.getName();
			int index = filename.lastIndexOf('.');
			if (index == -1)
				return false;
			String postfix = filename.substring(index + 1);
			if ("e2d".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Energy2D";
		}

	};

	private FileFilter htmFilter = new FileFilter() {

		public boolean accept(File file) {
			if (file == null)
				return false;
			if (file.isDirectory())
				return true;
			String filename = file.getName();
			int index = filename.lastIndexOf('.');
			if (index == -1)
				return false;
			String postfix = filename.substring(index + 1);
			if ("htm".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "HTML";
		}

	};

	private Action openAction;
	private Action saveAction;
	private Action saveAsAction;
	private Action saveAsAppletAction;
	private Action exitAction;
	private ScreenshotSaver screenshotSaver;
	private int fileMenuItemCount;
	private List<JComponent> recentFileMenuItems;

	MenuBar(final System2D box, final JFrame frame) {

		e2dFileChooser = new FileChooser();
		htmFileChooser = new FileChooser();
		recentFileMenuItems = new ArrayList<JComponent>();

		// file menu

		final JMenu fileMenu = new JMenu("File");
		fileMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				if (!recentFileMenuItems.isEmpty()) {
					for (JComponent x : recentFileMenuItems)
						fileMenu.remove(x);
				}
				String[] recentFiles = getRecentFiles();
				if (recentFiles != null) {
					int n = recentFiles.length;
					if (n > 0) {
						for (int i = 0; i < n; i++) {
							JMenuItem x = new JMenuItem((i + 1) + "  " + MiscUtil.getFileName(recentFiles[i]));
							final File rf = new File(recentFiles[i]);
							x.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									box.stop();
									if (!box.askSaveBeforeLoading())
										return;
									box.loadFile(rf);
									e2dFileChooser.rememberFile(rf.getPath());
								}
							});
							fileMenu.insert(x, fileMenuItemCount + i);
							recentFileMenuItems.add(x);
						}
						JSeparator s = new JSeparator();
						fileMenu.add(s, fileMenuItemCount + n);
						recentFileMenuItems.add(s);
					}
				}
			}
		});
		add(fileMenu);

		openAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				box.stop();
				if (!box.askSaveBeforeLoading())
					return;
				e2dFileChooser.setAcceptAllFileFilterUsed(false);
				e2dFileChooser.addChoosableFileFilter(e2dFilter);
				e2dFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				e2dFileChooser.setDialogTitle("Open");
				e2dFileChooser.setApproveButtonMnemonic('O');
				e2dFileChooser.setAccessory(null);
				if (e2dFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = e2dFileChooser.getSelectedFile();
					if (file.exists()) {
						box.loadFile(file);
					} else {
						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box), "File " + file + " was not found.", "File not found", JOptionPane.ERROR_MESSAGE);
					}
					e2dFileChooser.rememberFile(file.getPath());
				}
				e2dFileChooser.resetChoosableFileFilters();
			}
		};
		KeyStroke ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Open");
		box.view.getActionMap().put("Open", openAction);
		JMenuItem mi = new JMenuItem("Open...");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		saveAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (box.getCurrentFile() == null) {
					saveAs(box, frame);
				} else {
					save(box);
				}
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Save");
		box.view.getActionMap().put("Save", saveAction);
		mi = new JMenuItem("Save");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		saveAsAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				saveAs(box, frame);
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "SaveAs");
		box.view.getActionMap().put("SaveAs", saveAsAction);
		mi = new JMenuItem("Save As...");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		screenshotSaver = new ScreenshotSaver(box.view, true);
		fileMenu.add(screenshotSaver);
		fileMenuItemCount++;

		saveAsAppletAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (box.getCurrentFile() == null) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view), "Sorry, you have to save the current model as a local file in order to create an applet for it.", "Applet not allowed", JOptionPane.ERROR_MESSAGE);
					return;
				}
				saveAsApplet(box, frame);
			}
		};
		mi = new JMenuItem("Save As Applet...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAppletAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		fileMenu.addSeparator();
		fileMenuItemCount++;

		final Action propertyAction = box.view.getActionMap().get("Property");
		mi = new JMenuItem("Properties...");
		mi.setAccelerator((KeyStroke) propertyAction.getValue(Action.ACCELERATOR_KEY));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propertyAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		final Action taskAction = box.view.getActionMap().get("Task_Manager");
		mi = new JMenuItem("Task Manager...");
		mi.setAccelerator((KeyStroke) taskAction.getValue(Action.ACCELERATOR_KEY));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taskAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		if (!IS_MAC) {
			fileMenu.addSeparator();
			fileMenuItemCount++;
		}

		exitAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				switch (box.askSaveOption()) {
				case JOptionPane.YES_OPTION:
					Action a = null;
					if (box.getCurrentFile() != null) {
						a = box.view.getActionMap().get("Save");
					} else {
						a = box.view.getActionMap().get("SaveAs");
					}
					if (a != null)
						a.actionPerformed(null);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							System2D.savePreferences(box);
							System.exit(0);
						}
					});
					break;
				case JOptionPane.NO_OPTION:
					System2D.savePreferences(box);
					System.exit(0);
					break;
				case JOptionPane.CANCEL_OPTION:
					// do nothing
					break;
				}
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Quit");
		box.view.getActionMap().put("Quit", exitAction);
		if (!IS_MAC) {
			mi = new JMenuItem("Exit");
			mi.setAccelerator(ks);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exitAction.actionPerformed(e);
				}
			});
			fileMenu.add(mi);
		}

		// edit menu

		JMenu menu = new JMenu("Edit");
		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});
		add(menu);

		menu.add(box.view.getActionMap().get("Cut"));
		menu.add(box.view.getActionMap().get("Copy"));
		menu.add(box.view.getActionMap().get("Paste"));
		menu.addSeparator();

		mi = new JMenuItem("Clear All");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(box.view), "Are you sure you want to remove all objects?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					box.model.clear();
					box.model.refreshMaterialPropertyArrays();
					box.model.refreshPowerArray();
					box.model.refreshTemperatureBoundaryArray();
					box.view.clear();
					box.view.repaint();
				}
			}
		});
		menu.add(mi);

		menu.add(box.view.getActionMap().get("Insert Text Box"));

		// view menu

		final JCheckBoxMenuItem miSeeThrough = new JCheckBoxMenuItem("See-Through");
		final JCheckBoxMenuItem miIsotherm = new JCheckBoxMenuItem("Isotherm");
		final JCheckBoxMenuItem miVelocity = new JCheckBoxMenuItem("Velocity");
		final JCheckBoxMenuItem miStreamline = new JCheckBoxMenuItem("Streamlines");
		final JCheckBoxMenuItem miHeatFluxArrow = new JCheckBoxMenuItem("Heat Flux Arrows");
		final JCheckBoxMenuItem miHeatFluxLine = new JCheckBoxMenuItem("Heat Flux Lines");
		final JCheckBoxMenuItem miColorPalette = new JCheckBoxMenuItem("Color Palette");
		final JCheckBoxMenuItem miRuler = new JCheckBoxMenuItem("Ruler");
		final JCheckBoxMenuItem miGrid = new JCheckBoxMenuItem("Grid");

		menu = new JMenu("View");
		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				MiscUtil.setSelectedSilently(miSeeThrough, box.view.getSeeThrough());
				MiscUtil.setSelectedSilently(miIsotherm, box.view.isIsothermOn());
				MiscUtil.setSelectedSilently(miVelocity, box.view.isVelocityOn());
				MiscUtil.setSelectedSilently(miStreamline, box.view.isStreamlineOn());
				MiscUtil.setSelectedSilently(miHeatFluxArrow, box.view.isHeatFluxArrowsOn());
				MiscUtil.setSelectedSilently(miHeatFluxLine, box.view.isHeatFluxLinesOn());
				MiscUtil.setSelectedSilently(miColorPalette, box.view.isColorPaletteOn());
				MiscUtil.setSelectedSilently(miRuler, box.view.isRulerOn());
				MiscUtil.setSelectedSilently(miGrid, box.view.isGridOn());
			}
		});
		add(menu);

		miSeeThrough.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setSeeThrough(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miSeeThrough);

		miIsotherm.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setIsothermOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miIsotherm);

		miHeatFluxLine.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setHeatFluxLinesOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miHeatFluxLine);

		miHeatFluxArrow.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setHeatFluxArrowsOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miHeatFluxArrow);

		miVelocity.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setVelocityOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miVelocity);

		miStreamline.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setStreamlineOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miStreamline);

		miColorPalette.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setColorPaletteOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miColorPalette);

		miRuler.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setRulerOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miRuler);

		miGrid.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setGridOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miGrid);
		menu.addSeparator();

		mi = new JMenuItem("More...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.view.createDialog(box.view, false);
			}
		});
		menu.add(mi);

		// template menu

		menu = new JMenu("Templates");
		add(menu);

		JMenu subMenu = new JMenu("Heat and Temperature");
		menu.add(subMenu);

		LinkedHashMap<String, String> templates = new LinkedHashMap<String, String>();
		templates.put("Thermal Equilibrium Between Identical Objects", "models/identical-heat-capacity.e2d");
		templates.put("Thermal Equilibrium Between Objects with Different Specific Heats: Case 1", "models/different-specific-heat1.e2d");
		templates.put("Thermal Equilibrium Between Objects with Different Specific Heats: Case 2", "models/different-specific-heat2.e2d");
		templates.put("Thermal Equilibrium Between Objects with Different Densities: Case 1", "models/different-density1.e2d");
		templates.put("Thermal Equilibrium Between Objects with Different Densities: Case 2", "models/different-density2.e2d");
		templates.put("The Effect of Thermal Conductivity on Equilibration Speed", "models/different-conductivity.e2d");
		templates.put("The Zeroth Law of Thermodynamics", "models/zeroth.e2d");
		createMenu(box, subMenu, templates);

		subMenu = new JMenu("Conduction");
		menu.add(subMenu);

		templates.put("Comparing Thermal Conductivities", "models/conduction1.e2d");
		templates.put("Comparing Conduction Areas", "models/conduction2.e2d");
		templates.put("Comparing Temperature Differences", "models/conduction3.e2d");
		templates.put("Comparing Conducting Distances", "models/conduction4.e2d");
		templates.put("Comparing Specific Heats", "models/conduction5.e2d");
		templates.put("The Series Circuit Analogy", "models/series-circuit-analogy.e2d");
		templates.put("The Parallel Circuit Analogy", "models/parallel-circuit-analogy.e2d");
		templates.put("Thermal Bridge", "models/thermal-bridge.e2d");
		createMenu(box, subMenu, templates);

		subMenu = new JMenu("Convection");
		menu.add(subMenu);

		templates.put("Natural Convection", "models/natural-convection.e2d");
		templates.put("Natural Convection with Different Temperatures", "models/natural-convection-temperature.e2d");
		templates.put("Comparing Natural Convection and Conduction", "models/compare-convection-conduction.e2d");
		templates.put("Comparing Forced Convection and Conduction", "models/forced-convection.e2d");
		createMenu(box, subMenu, templates);

		subMenu = new JMenu("Radiation");
		menu.add(subMenu);

		mi = new JMenuItem("Radiation Heat and Temperature");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("models/temperature-radiation.e2d");
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Fluid Dynamics");
		menu.add(subMenu);

		templates.put("Bénard Cell", "models/benard-cell.e2d");
		templates.put("Lid-Driven Cavity", "models/lid-driven-cavity.e2d");
		templates.put("Smoke in Wind", "models/smoke-in-wind.e2d");
		templates.put("Laminar/Turbulent Flow", "models/laminar-turbulent.e2d");
		templates.put("Von Kármán Vortex Street", "models/vortex-street.e2d");
		createMenu(box, subMenu, templates);

		subMenu = new JMenu("Building Energy Analysis");
		menu.add(subMenu);

		templates.put("Internal Heating", "models/internal-heater.e2d");
		templates.put("Thermostats", "models/thermostat2.e2d");
		templates.put("Infiltration", "models/infiltration.e2d");
		templates.put("Wind Effect", "models/wind-effect.e2d");
		templates.put("Solar Heating: Gable Roof", "models/solar-heating-gable-roof.e2d");
		templates.put("Solar Heating: Skillion Roof", "models/solar-heating-skillion-roof.e2d");
		templates.put("Solar Heating: Two Story", "models/solar-heating-two-story.e2d");
		templates.put("Solar Heating: Convection", "models/solar-heating-convection.e2d");
		templates.put("Solar Heating: Thermostat", "models/thermostat1.e2d");
		createMenu(box, subMenu, templates);

		subMenu = new JMenu("Boundary Conditions");
		menu.add(subMenu);

		templates.put("Fixed Temperature Boundary", "models/fixed-temperature-boundary.e2d");
		templates.put("Fixed Heat Flux Boundary", "models/fixed-flux-boundary.e2d");
		createMenu(box, subMenu, templates);

		subMenu = new JMenu("Miscellaneous");
		menu.add(subMenu);

		templates.put("Ray Optics", "models/ray-optics.e2d");
		templates.put("Solar Cycles", "models/solar-cycles-shadow.e2d");
		createMenu(box, subMenu, templates);

		// help menu

		menu = new JMenu("Help");
		add(menu);

		final Action scriptAction = box.view.getActionMap().get("Script");
		mi = new JMenuItem("Script Console...");
		mi.setAccelerator((KeyStroke) scriptAction.getValue(Action.ACCELERATOR_KEY));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scriptAction.actionPerformed(e);
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Keyboard Shortcuts...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.showKeyboardShortcuts(frame);
			}
		});
		menu.add(mi);

		if (!System.getProperty("os.name").startsWith("Linux")) {
			mi = new JMenuItem("Online Manual...");
			mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, true));
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Helper.openBrowser("http://energy.concord.org/energy2d/manual/index.html");
				}
			});
			menu.add(mi);
			mi = new JMenuItem("Contact Us...");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Helper.openBrowser("http://energy.concord.org/energy2d/contact.html");
				}
			});
			menu.add(mi);
		}

		if (!System.getProperty("os.name").startsWith("Mac")) {
			menu.addSeparator();
			mi = new JMenuItem("About...");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Helper.showAbout(frame);
				}
			});
			menu.add(mi);
		}

	}

	private void createMenu(final System2D box, JMenu menu, LinkedHashMap<String, String> templates) {
		JMenuItem mi;
		for (Map.Entry<String, String> x : templates.entrySet()) {
			mi = new JMenuItem(x.getKey());
			final String val = x.getValue();
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					box.loadModel(val);
				}
			});
			menu.add(mi);
		}
		templates.clear();
	}

	void setLatestPath(String latestPath, String type) {
		if (latestPath != null) {
			if ("htm".equalsIgnoreCase(type)) {
				htmFileChooser.setCurrentDirectory(new File(latestPath));
			} else if ("png".equalsIgnoreCase(type)) {
				screenshotSaver.setCurrentDirectory(new File(latestPath));
			} else if ("e2d".equalsIgnoreCase(type)) {
				e2dFileChooser.setCurrentDirectory(new File(latestPath));
			}
		}
	}

	String getLatestPath(String type) {
		if ("htm".equalsIgnoreCase(type))
			return htmFileChooser.getLatestPath();
		if ("png".equalsIgnoreCase(type))
			return screenshotSaver.getLatestPath();
		return e2dFileChooser.getLatestPath();
	}

	void addRecentFile(String path) {
		if (path != null)
			e2dFileChooser.addRecentFile(path);
	}

	String[] getRecentFiles() {
		return e2dFileChooser.getRecentFiles();
	}

	private void save(System2D box) {
		try {
			box.saveState(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(box.getCurrentFile()), "UTF-8")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveAs(System2D box, JFrame frame) {
		e2dFileChooser.setAcceptAllFileFilterUsed(false);
		e2dFileChooser.addChoosableFileFilter(e2dFilter);
		e2dFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		e2dFileChooser.setDialogTitle("Save");
		e2dFileChooser.setApproveButtonMnemonic('S');
		if (e2dFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = e2dFileChooser.getSelectedFile();
			if (!file.toString().toLowerCase().endsWith(".e2d")) {
				file = new File(file.getParentFile(), MiscUtil.getFileName(file.toString()) + ".e2d");
			}
			boolean b = true;
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(frame, "File " + file.getName() + " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					b = false;
				}
			}
			if (b) {
				box.setCurrentFile(file);
				try {
					box.saveState(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(box.getCurrentFile()), "UTF-8")));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			e2dFileChooser.rememberFile(file.getPath());
		}
	}

	private void saveAsApplet(System2D box, JFrame frame) {
		htmFileChooser.setAcceptAllFileFilterUsed(false);
		htmFileChooser.addChoosableFileFilter(htmFilter);
		htmFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		htmFileChooser.setDialogTitle("Save As Applet");
		htmFileChooser.setApproveButtonMnemonic('S');
		if (htmFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = htmFileChooser.getSelectedFile();
			if (!file.toString().toLowerCase().endsWith(".htm")) {
				file = new File(file.getParentFile(), MiscUtil.getFileName(file.toString()) + ".htm");
			}
			boolean b = true;
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(frame, "File " + file.getName() + " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					b = false;
				}
			}
			if (b) {
				box.saveApplet(file);
			}
			htmFileChooser.rememberFile(file.getPath());
		}
	}

}
