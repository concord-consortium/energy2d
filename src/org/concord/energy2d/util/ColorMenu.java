/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * @author Charles Xie
 * 
 */

public class ColorMenu extends JMenu {

	public final static String FILLING = "Filling";
	private static ResourceBundle bundle;
	private static boolean isUSLocale;

	protected JColorChooser colorChooser;
	protected TextureChooser textureChooser;

	private Component parent;
	private JMenuItem noFillMenuItem;
	private JMenuItem moreColorMenuItem;
	private JMenuItem hexColorMenuItem;
	private JMenuItem textureMenuItem;
	private ColorArrayPane cap;

	public ColorMenu(Component parent, String name, JColorChooser color) {
		this(parent, name, color, null);
	}

	public ColorMenu(Component parent, String name, JColorChooser color, TextureChooser texture) {

		super(name);

		if (bundle == null) {
			isUSLocale = Locale.getDefault().equals(Locale.US);
			try {
				bundle = ResourceBundle.getBundle("org.concord.energy2d.util.resources.ColorMenu", Locale.getDefault());
			} catch (MissingResourceException e) {
			}
		}

		this.parent = parent;
		colorChooser = color;
		textureChooser = texture;

		String s = getInternationalText("NoFill");
		noFillMenuItem = new JCheckBoxMenuItem(s != null ? s : "No Fill");
		add(noFillMenuItem);
		addSeparator();

		cap = new ColorArrayPane();
		cap.addColorArrayListener(new ColorArrayListener() {
			public void colorSelected(ColorArrayEvent e) {
				doSelection();
				ColorMenu.this.firePropertyChange(FILLING, null, new ColorFill(e.getSelectedColor()));
			}
		});
		add(cap);
		addSeparator();

		s = getInternationalText("MoreColors");
		moreColorMenuItem = new JMenuItem((s != null ? s : "More Colors") + "...");
		add(moreColorMenuItem);

		s = getInternationalText("HexColor");
		hexColorMenuItem = new JMenuItem((s != null ? s : "Hex Color") + "...");
		add(hexColorMenuItem);

		if (textureChooser != null) {
			s = getInternationalText("Texture");
			textureMenuItem = new JMenuItem((s != null ? s : "Texture") + "...");
			add(textureMenuItem);
		}

	}

	static String getInternationalText(String name) {
		if (bundle == null)
			return null;
		if (name == null)
			return null;
		if (isUSLocale)
			return null;
		String s = null;
		try {
			s = bundle.getString(name);
		} catch (MissingResourceException e) {
			s = null;
		}
		return s;
	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		super.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	public void setColorChooser(JColorChooser cc) {
		colorChooser = cc;
	}

	public JColorChooser getColorChooser() {
		return colorChooser;
	}

	public void setTextureChooser(TextureChooser fec) {
		textureChooser = fec;
	}

	public TextureChooser getTextureChooser() {
		return textureChooser;
	}

	public void addNoFillListener(ActionListener a) {
		noFillMenuItem.addActionListener(a);
	}

	public void removeNoFillListener(ActionListener a) {
		noFillMenuItem.removeActionListener(a);
	}

	public void setNoFillAction(Action a) {
		noFillMenuItem.setAction(a);
		String s = getInternationalText("NoFill");
		if (s != null)
			noFillMenuItem.setText(s);
	}

	public void addColorArrayListener(ActionListener a) {
		addActionListener(a);
	}

	public void removeColorArrayListener(ActionListener a) {
		removeActionListener(a);
	}

	public void setColorArrayAction(Action a) {
		setAction(a);
	}

	public Color getHexInputColor(Color oldColor) {
		String s = oldColor != null ? Integer.toHexString(oldColor.getRGB() & 0x00ffffff) : "";
		int m = 6 - s.length();
		if (m != 6 && m != 0) {
			for (int k = 0; k < m; k++)
				s = "0" + s;
		}
		String hex = JOptionPane.showInputDialog(parent, "Input a hex color number:", s);
		if (hex == null)
			return null;
		Color c = oldColor;
		try {
			c = MiscUtil.convertToColor(hex);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(ColorMenu.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return c;
	}

	public void addMoreColorListener(final ActionListener a) {
		moreColorMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = getInternationalText("MoreColors");
				JColorChooser.createDialog(parent, s != null ? s : "Background Color", true, colorChooser, a, null).setVisible(true);
			}
		});
	}

	public void addHexColorListener(ActionListener a) {
		hexColorMenuItem.addActionListener(a);
	}

	public void setMoreColorAction(final ActionListener a) {
		moreColorMenuItem.setAction(new AbstractAction("More Colors") {
			public void actionPerformed(ActionEvent e) {
				String s = getInternationalText("MoreColors");
				JColorChooser.createDialog(parent, s != null ? s : "Background Color", true, colorChooser, a, null).setVisible(true);
			}
		});
		String s = getInternationalText("MoreColors");
		if (s != null)
			moreColorMenuItem.setText(s);
	}

	public void addTextureListeners(final ActionListener ok, final ActionListener cancel) {
		if (textureMenuItem != null)
			textureMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String s = getInternationalText("Texture");
					TextureChooser.createDialog(parent, s != null ? s : "Texture", true, textureChooser, ok, cancel).setVisible(true);
				}
			});
	}

	public void setTextureActions(final ActionListener ok, final ActionListener cancel) {
		textureMenuItem.setAction(new AbstractAction("Texture") {
			public void actionPerformed(ActionEvent e) {
				String s = getInternationalText("Texture");
				TextureChooser.createDialog(parent, s != null ? s : "Texture", true, textureChooser, ok, cancel).setVisible(true);
			}
		});
		String s = getInternationalText("Texture");
		if (s != null)
			textureMenuItem.setText(s);
	}

	public void setColor(Color c) {
		cap.setSelectedColor(c);
	}

	public Color getColor() {
		return cap.getSelectedColor();
	}

	public void doSelection() {
		fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
	}

}
