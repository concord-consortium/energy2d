/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

/**
 * @author Charles Xie
 * 
 */
class MenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;
	private final static boolean IS_MAC = System.getProperty("os.name")
			.startsWith("Mac");

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
			if ("aps".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Energy2D";
		}

	};

	MenuBar(final System2D box, final JFrame frame) {

		fileChooser = new JFileChooser();

		// file menu

		JMenu menu = new JMenu("File");
		add(menu);

		JMenuItem mi = new JMenuItem("Open");
		mi.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.CTRL_MASK));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				fileChooser.setDialogTitle("Open");
				fileChooser.setApproveButtonMnemonic('O');
				fileChooser.setAccessory(null);
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists()) {
						try {
							box.loadState(new FileInputStream(file));
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				fileChooser.resetChoosableFileFilters();
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Save");
		mi.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.CTRL_MASK));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setDialogTitle("Save");
				fileChooser.setApproveButtonMnemonic('S');
				if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					boolean b = true;
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(frame, "File "
								+ file.getName() + " exists, overwrite?",
								"File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							b = false;
						}
					}
					if (b) {
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
		});
		menu.add(mi);

		mi = new JMenuItem("Save As");
		mi.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_A,
				KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_A,
				KeyEvent.CTRL_MASK));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Exit");
		mi.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				KeyEvent.CTRL_MASK));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		menu.add(mi);

		// help menu

		menu = new JMenu("Help");
		add(menu);

		mi = new JMenuItem("About");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JLabel label = new JLabel(
						"<html><h2>Energy3D</h2><hr><h3>Credit:</h3>This program is brought to you by:<ul><li>Dr. Charles Xie, Email: qxie@concord.org</ul><p>This program is licensed under the GNU Lesser General Public License V3.0.<br>Funding of this project is provided by the National Science Foundation<br>under grant #0918449 to the Concord Consortium. </html>");
				JOptionPane.showMessageDialog(frame, label);
			}
		});
		menu.add(mi);

	}

}
