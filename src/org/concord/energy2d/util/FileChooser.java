/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

/**
 * This is a file chooser that is able to remember its last-visited path and
 * files (4).
 * 
 * @author Charles Xie
 */

public class FileChooser extends JFileChooser {

	private final static String LATEST_PATH = "Latest Path";
	private final static String[] RECENT_FILES = new String[] { "Recent File 1", "Recent File 2",
			"Recent File 3", "Recent File 4" };
	private final static File DUMMY_FILE = new File("");

	private Map<String, String> historyMap;
	private List<String> recentFiles;
	private String lastVisitedPath;

	public FileChooser() {
		super();
		init();
	}

	public FileChooser(File currentDirectory) {
		super(currentDirectory);
		init();
	}

	public FileChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
		init();
	}

	/**
	 * when reusing this file chooser to open other type of file, the input text
	 * field needs to be cleared to avoid confusion.
	 */
	public void handleFileTypeSwitching(File file) {
		if (file != null) {
			FileFilter ff = getFileFilter();
			if (ff == null || !ff.accept(file)) {
				clearTextField();
			}
		} else {
			clearTextField();
		}
	}

	public void clearTextField() {
		setSelectedFile(DUMMY_FILE);
	}

	/** save the last visited path to the hard drive */
	public void rememberPath(String path) {
		lastVisitedPath = path;
		historyMap.put(LATEST_PATH, path);
	}

	public String getLastVisitedPath() {
		if (historyMap == null)
			return lastVisitedPath;
		return historyMap.get(LATEST_PATH);
	}

	public void rememberFile(String fileName, final JMenu recentFilesMenu) {
		if (historyMap == null)
			return;
		if (fileName == null)
			return;
		int max = RECENT_FILES.length;
		if (recentFiles.contains(fileName)) {
			recentFiles.remove(fileName);
		} else {
			if (recentFiles.size() >= max)
				recentFiles.remove(0);
		}
		recentFiles.add(fileName);
		final int n = recentFiles.size();
		for (int i = 0; i < max; i++) {
			if (i < n)
				historyMap.put(RECENT_FILES[max - 1 - i], recentFiles.get(i));
		}
		if (recentFilesMenu != null)
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JMenuItem mi;
					for (int i = 0; i < n; i++) {
						mi = recentFilesMenu.getItem(i);
						mi.setText(recentFiles.get(n - 1 - i));
					}
				}
			});
	}

	public String[] getRecentFiles() {
		int n = recentFiles.size();
		if (n == 0)
			return new String[] {};
		String[] s = new String[n];
		for (int i = 0; i < n; i++) {
			s[n - 1 - i] = recentFiles.get(i);
		}
		return s;
	}

	private void init() {
		setMultiSelectionEnabled(false);
		setFileHidingEnabled(true);
		recentFiles = new ArrayList<String>();
		historyMap = new HashMap<String, String>();
		addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String s = e.getPropertyName();
				if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(s)) {
					if (getDialogType() == OPEN_DIALOG) {
						clearTextField();
					}
				}
			}
		});
	}

}
