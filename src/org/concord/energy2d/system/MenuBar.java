/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class MenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;
	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");

	private ScriptDialog scriptDialog;
	private JFileChooser fileChooser;
	private FileFilter filter = new FileFilter() {

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

	private Action openAction;
	private Action saveAction;
	private Action saveAsAction;
	private Action exitAction;
	private Action propertyAction;

	MenuBar(final System2D box, final JFrame frame) {

		fileChooser = new JFileChooser();

		// file menu

		JMenu menu = new JMenu("File");
		add(menu);

		openAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				fileChooser.setDialogTitle("Open");
				fileChooser.setApproveButtonMnemonic('O');
				fileChooser.setAccessory(null);
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists()) {
						box.setCurrentFile(file);
						try {
							box.loadStateApp(new FileInputStream(file));
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				fileChooser.resetChoosableFileFilters();
			}
		};
		KeyStroke ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_MASK)
				: KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Open");
		box.view.getActionMap().put("Open", openAction);
		JMenuItem mi = new JMenuItem("Open...");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction.actionPerformed(e);
			}
		});
		menu.add(mi);

		saveAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				if (box.getCurrentFile() == null) {
					saveAs(box, frame);
				} else {
					save(box);
				}
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK) : KeyStroke
				.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Save");
		box.view.getActionMap().put("Save", saveAction);
		mi = new JMenuItem("Save");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction.actionPerformed(e);
			}
		});
		menu.add(mi);

		saveAsAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				saveAs(box, frame);
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_MASK) : KeyStroke
				.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "SaveAs");
		box.view.getActionMap().put("SaveAs", saveAsAction);
		mi = new JMenuItem("Save As...");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAction.actionPerformed(e);
			}
		});
		menu.add(mi);

		menu.addSeparator();

		propertyAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				box.view.createDialog(box.model);
			}
		};
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK);
		box.view.getInputMap().put(ks, "Property");
		box.view.getActionMap().put("Property", propertyAction);
		mi = new JMenuItem("Properties...");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propertyAction.actionPerformed(e);
			}
		});
		menu.add(mi);

		menu.addSeparator();

		exitAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_MASK) : KeyStroke
				.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);
		box.view.getInputMap().put(ks, "Quit");
		box.view.getActionMap().put("Quit", exitAction);
		mi = new JMenuItem("Exit");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitAction.actionPerformed(e);
			}
		});
		menu.add(mi);

		// edit menu

		menu = new JMenu("Edit");
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

		// view menu

		final JCheckBoxMenuItem miIsotherm = new JCheckBoxMenuItem("Isotherm");
		final JCheckBoxMenuItem miVelocity = new JCheckBoxMenuItem("Velocity");
		final JCheckBoxMenuItem miRainbow = new JCheckBoxMenuItem("Rainbow");
		final JCheckBoxMenuItem miRuler = new JCheckBoxMenuItem("Ruler");
		final JCheckBoxMenuItem miGrid = new JCheckBoxMenuItem("Grid");

		menu = new JMenu("View");
		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				miIsotherm.setSelected(box.view.isIsothermOn());
				miVelocity.setSelected(box.view.isVelocityOn());
				miRainbow.setSelected(box.view.isRainbowOn());
				miRuler.setSelected(box.view.isRulerOn());
				miGrid.setSelected(box.view.isGridOn());
			}
		});
		add(menu);

		miIsotherm.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setIsothermOn(src.isSelected());
				box.view.repaint();
			}
		});
		menu.add(miIsotherm);

		miVelocity.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setVelocityOn(src.isSelected());
				box.view.repaint();
			}
		});
		menu.add(miVelocity);

		miRainbow.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setRainbowOn(src.isSelected());
				box.view.repaint();
			}
		});
		menu.add(miRainbow);

		miRuler.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setRulerOn(src.isSelected());
				box.view.repaint();
			}
		});
		menu.add(miRuler);

		miGrid.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view.setGridOn(src.isSelected());
				box.view.repaint();
			}
		});
		menu.add(miGrid);
		menu.addSeparator();

		mi = new JMenuItem("More...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.view.createDialog(box.view);
			}
		});
		menu.add(mi);

		// model menu

		menu = new JMenu("Models");
		add(menu);

		JMenu subMenu = new JMenu("Heat and Temperature");
		menu.add(subMenu);

		mi = new JMenuItem("Thermal Equilibrium Between Identical Objects");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/identical-heat-capacity.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem(
				"Thermal Equilibrium Between Objects with Different Specific Heats: Case 1");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/different-specific-heat1.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem(
				"Thermal Equilibrium Between Objects with Different Specific Heats: Case 2");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/different-specific-heat2.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Thermal Equilibrium Between Objects with Different Densities: Case 1");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/different-density1.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Thermal Equilibrium Between Objects with Different Densities: Case 2");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/different-density2.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("The Effect of Thermal Conductivity on Equilibration Speed");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/different-conductivity.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Heat Conduction");
		menu.add(subMenu);

		mi = new JMenuItem("Comparing Thermal Conductivities");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class.getResourceAsStream("models/conduction1.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Comparing Conduction Area");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class.getResourceAsStream("models/conduction2.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Comparing Temperature Difference");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class.getResourceAsStream("models/conduction3.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Comparing Heat Capacity");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class.getResourceAsStream("models/conduction4.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Heat Convection");
		menu.add(subMenu);

		mi = new JMenuItem("Natural Convection");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/natural-convection.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Forced Convection");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/forced-convection.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Fluid Dynamics");
		menu.add(subMenu);

		mi = new JMenuItem("Benard Cell");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class.getResourceAsStream("models/benard-cell.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Lid-Driven Cavity");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/lid-driven-cavity.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Smoke in Wind");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class.getResourceAsStream("models/smoke-in-wind.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Laminar/Turbulent Flow");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/laminar-turbulent.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Building Energy Flow");
		menu.add(subMenu);

		mi = new JMenuItem("Internal Heating");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/internal-heater.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Solar Heating: Gable Roof");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/solar-heating-gable-roof.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Solar Heating: Skillion Roof");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/solar-heating-skillion-roof.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Solar Heating: Two Story");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/solar-heating-two-story.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Solar Heating (Convection)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/solar-heating-convection.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Boundary Conditions");
		menu.add(subMenu);

		mi = new JMenuItem("Fixed Temperature Boundary");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/fixed-temperature-boundary.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Fixed Flux Boundary");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/fixed-flux-boundary.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Miscellaneous");
		menu.add(subMenu);

		mi = new JMenuItem("Ray Optics");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class.getResourceAsStream("models/ray-optics.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Projection Effect");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					box.loadStateApp(MenuBar.class
							.getResourceAsStream("models/projection-effect.e2d"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		subMenu.add(mi);

		// help menu

		menu = new JMenu("Help");
		add(menu);

		mi = new JMenuItem("Keyboard Shortcuts...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = "<html><h2>Keyboard Shortcuts</h2><hr>";
				s += "<ul>";
				s += "<li>R &mdash; Run or stop the graph.";
				s += "<li>G &mdash; Show or hide the graph.";
				s += "<li>S &mdash; Turn sunlight on or off.";
				s += "<li>Q &mdash; When sunlight is present, increase the sun angle (towards west).";
				s += "<li>W &mdash; When sunlight is present, decrease the sun angle (towards east).";
				s += "</ul>";
				s += "</html>";
				JOptionPane.showMessageDialog(frame, new JLabel(s));
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Script Console...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (scriptDialog != null && scriptDialog.isShowing()) {
					scriptDialog.toFront();
				} else {
					scriptDialog = new ScriptDialog(box);
					scriptDialog.pack();
					scriptDialog.setLocationRelativeTo(box);
					scriptDialog.setVisible(true);
				}
			}
		});
		menu.add(mi);

		mi = new JMenuItem("About...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = "<html><h2>Energy2D</h2>";
				s += "<h4><i>Interactive simulation of heat and mass flow<br>";
				s += "for studying energy science and technology</i></h4>";
				s += "http://energy.concord.org/energy2d.html<hr>";
				s += "<h4>Credit:</h4>This program is brought to you by:";
				s += "<ul><li>Dr. Charles Xie, Email: qxie@concord.org</ul>";
				s += "<p>This program is licensed under the GNU Lesser General Public License V3.0.<br>";
				s += "Funding of this project is provided by the National Science Foundation<br>";
				s += "under grant #0918449 to the Concord Consortium.";
				s += "</html>";
				JOptionPane.showMessageDialog(frame, new JLabel(s));
			}
		});
		menu.add(mi);

	}

	private void save(System2D box) {
		try {
			box.saveState(new FileOutputStream(box.getCurrentFile()));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void saveAs(System2D box, JFrame frame) {
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setDialogTitle("Save");
		fileChooser.setApproveButtonMnemonic('S');
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.toString().toLowerCase().endsWith(".e2d")) {
				file = new File(file.getParentFile(), MiscUtil.getFileName(file.toString())
						+ ".e2d");
			}
			boolean b = true;
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(frame, "File " + file.getName()
						+ " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					b = false;
				}
			}
			if (b) {
				box.setCurrentFile(file);
				try {
					box.saveState(new FileOutputStream(file));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}
