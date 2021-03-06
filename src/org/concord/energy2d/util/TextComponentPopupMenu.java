/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

/**
 * @author Charles Xie
 * 
 */

public class TextComponentPopupMenu extends JPopupMenu {

	protected Map<Object, Action> actions;
	protected JTextComponent text;
	protected JMenuItem miCut, miPaste, miCopy, miSelectAll;

	public TextComponentPopupMenu(JTextComponent t) {

		text = t;

		actions = new HashMap<Object, Action>();
		for (Action act : text.getActions())
			actions.put(act.getValue(Action.NAME), act);

		miCopy = new JMenuItem(actions.get(DefaultEditorKit.copyAction));
		miCopy.setText("Copy");
		miCopy.setAccelerator(System.getProperty("os.name").startsWith("Mac") ? KeyStroke
				.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK) : KeyStroke.getKeyStroke(
				KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		add(miCopy);

		miCut = new JMenuItem(actions.get(DefaultEditorKit.cutAction));
		miCut.setText("Cut");
		miCut.setAccelerator(System.getProperty("os.name").startsWith("Mac") ? KeyStroke
				.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_MASK) : KeyStroke.getKeyStroke(
				KeyEvent.VK_X, KeyEvent.CTRL_MASK));
		add(miCut);

		miPaste = new JMenuItem(actions.get(DefaultEditorKit.pasteAction));
		miPaste.setText("Paste");
		miPaste.setAccelerator(System.getProperty("os.name").startsWith("Mac") ? KeyStroke
				.getKeyStroke(KeyEvent.VK_V, KeyEvent.ALT_MASK) : KeyStroke.getKeyStroke(
				KeyEvent.VK_V, KeyEvent.CTRL_MASK));
		add(miPaste);

		miSelectAll = new JMenuItem(actions.get(DefaultEditorKit.selectAllAction));
		miSelectAll.setText("Select All");
		miSelectAll.setAccelerator(System.getProperty("os.name").startsWith("Mac") ? KeyStroke
				.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK) : KeyStroke.getKeyStroke(
				KeyEvent.VK_A, KeyEvent.CTRL_MASK));
		add(miSelectAll);

	}

	public void show(Component invoker, int x, int y) {
		super.show(invoker, x, y);
		miCut.setEnabled(text.getSelectedText() != null && text.isEditable());
		miCopy.setEnabled(text.getSelectedText() != null);
		miPaste.setEnabled(text.isEditable());
		miSelectAll.setEnabled(text.getText() != null);
	}

	public JTextComponent getTextComponent() {
		return text;
	}

	/** set a customized action for pasting */
	public void setPasteAction(ActionListener listener) {
		// somehow removing action listener also removes text and icon so we
		// must save them and restore later
		String text = miPaste.getText();
		Icon icon = miPaste.getIcon();
		ActionListener[] al = miPaste.getActionListeners();
		if (al != null) {
			for (ActionListener x : al)
				miPaste.removeActionListener(x);
		}
		if (listener != null)
			miPaste.addActionListener(listener);
		miPaste.setText(text);
		miPaste.setIcon(icon);
	}

}
