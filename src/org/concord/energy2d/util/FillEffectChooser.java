/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 * @author Charles Xie
 * 
 */

public class FillEffectChooser extends JTabbedPane {

	private final static float[] dash = { 2.0f };
	private final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f);
	private static ResourceBundle bundle;
	private static boolean isUSLocale;

	private TexturePanel[] pp;
	private FillPattern fillPattern;

	/**
	 * Creates and returns a new dialog containing the specified FillEffectChooser pane along with "OK", "Cancel", and "Reset" buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is automatically hidden (but not disposed). If the "Reset" button is pressed, the chooser's selection will be reset to the option which was set the last time show was invoked on the dialog and the dialog will remain showing.
	 */
	public static JDialog createDialog(Component parent, String title, boolean modal, FillEffectChooser chooser, ActionListener okListener, ActionListener cancelListener) {

		final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(parent), title == null ? "Fill Effects" : title, modal);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		Container container = dialog.getContentPane();
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		container.add(panel, BorderLayout.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 6;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(chooser, c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel(), c);

		final ActionListener okListener1 = okListener;
		final FillEffectChooser chooser1 = chooser;

		JButton button = new JButton("OK");
		String s = getInternationalText("OKButton");
		if (s != null)
			button.setText(s);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (chooser1.getSelectedIndex()) {
				case 0:
					TexturePanel pp = chooser1.getSelectedTexturePanel();
					if (pp != null) {
						chooser1.fillPattern = new Texture(pp.getForeground().getRGB(), pp.getBackground().getRGB(), pp.getStyle(), pp.getCellWidth(), pp.getCellHeight());
					}
					break;
				}
				okListener1.actionPerformed(e);
				dialog.dispose();
			}
		});
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(button, c);

		button = new JButton("Cancel");
		s = getInternationalText("CancelButton");
		if (s != null)
			button.setText(s);
		if (cancelListener != null) {
			button.addActionListener(cancelListener);
		} else {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
		}
		c.gridy = 2;
		panel.add(button, c);

		dialog.pack();

		if (parent == null) {
			dialog.setLocation(200, 200);
		} else {
			dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(parent));
		}

		return dialog;

	}

	public FillEffectChooser() {

		super();

		if (bundle == null) {
			isUSLocale = Locale.getDefault().equals(Locale.US);
			try {
				bundle = ResourceBundle.getBundle("org.concord.energy2d.util.resources.FillEffectChooser", Locale.getDefault());
			} catch (MissingResourceException e) {
			}
		}

		setPreferredSize(new Dimension(300, 300));

		String s = getInternationalText("Texture");
		addTab(s == null ? "Texture" : s, createTexturePanel());

	}

	private static String getInternationalText(String name) {
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

	private TexturePanel getSelectedTexturePanel() {
		for (TexturePanel p : pp) {
			if (p.isSelected())
				return p;
		}
		return null;
	}

	private void setTexturePanelForeground(Color c) {
		for (TexturePanel p : pp)
			p.setForeground(c);
	}

	private void setTexturePanelBackground(Color c) {
		for (TexturePanel p : pp)
			p.setBackground(c);
	}

	private JPanel createTexturePanel() {

		JPanel p = new JPanel(new BorderLayout(10, 10));

		int size = TextureFactory.STYLE_ARRAY.length;
		int grid = (int) Math.sqrt(size + 0.0001);

		JPanel texturePanel = new JPanel(new GridLayout(grid, grid * grid < size ? grid + 1 : grid, 2, 2));
		p.add(texturePanel, BorderLayout.CENTER);

		pp = new TexturePanel[size];
		int cell = 10;
		for (int i = 0; i < pp.length; i++) {
			switch (TextureFactory.SIZE_ARRAY[i]) {
			case TextureFactory.SMALL:
				cell = 4;
				break;
			case TextureFactory.MEDIUM:
				cell = 10;
				break;
			case TextureFactory.LARGE:
				cell = 12;
				break;
			}
			pp[i] = new TexturePanel(TextureFactory.STYLE_ARRAY[i], cell, cell);
			texturePanel.add(pp[i]);
			final int ii = i;
			pp[i].addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					for (int k = 0; k < pp.length; k++) {
						pp[k].setSelected(k == ii);
						pp[k].repaint();
					}
				}
			});
		}
		pp[0].setSelected(true);

		JPanel p4 = new JPanel(new GridLayout(1, 2, 10, 10));
		p4.setBorder(new EmptyBorder(10, 10, 10, 10));
		p.add(p4, BorderLayout.SOUTH);

		JPanel p3 = new JPanel(new BorderLayout());
		p4.add(p3);

		String s = getInternationalText("ForegroundColor");
		JLabel label = new JLabel((s != null ? s : "Foreground Color") + " :");
		p3.add(label, BorderLayout.NORTH);
		ColorComboBox colorComboBox = new ColorComboBox(this);
		colorComboBox.setRenderer(new ComboBoxRenderer.ColorCell());
		colorComboBox.setToolTipText(s != null ? s : "Foreground color");
		colorComboBox.setSelectedIndex(0);
		colorComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final ColorComboBox cb = (ColorComboBox) e.getSource();
				int id = cb.getSelectedIndex();
				if (id >= ColorRectangle.COLORS.length + 1) {
					cb.updateColor(new Runnable() {
						public void run() {
							setTexturePanelForeground(cb.getMoreColor());
						}
					});
				} else if (id == ColorRectangle.COLORS.length) {
					setTexturePanelForeground(cb.getMoreColor());
				} else {
					setTexturePanelForeground(ColorRectangle.COLORS[id]);
				}
			}
		});
		p3.add(colorComboBox, BorderLayout.CENTER);
		label.setLabelFor(colorComboBox);

		p3 = new JPanel(new BorderLayout());
		p4.add(p3);

		s = getInternationalText("BackgroundColor");
		label = new JLabel((s != null ? s : "Background Color") + " :");
		p3.add(label, BorderLayout.NORTH);
		colorComboBox = new ColorComboBox(this);
		colorComboBox.setRenderer(new ComboBoxRenderer.ColorCell());
		colorComboBox.setToolTipText(s != null ? s : "Background color");
		colorComboBox.setSelectedIndex(ColorRectangle.COLORS.length);
		colorComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final ColorComboBox cb = (ColorComboBox) e.getSource();
				int id = cb.getSelectedIndex();
				if (id >= ColorRectangle.COLORS.length + 1) {
					cb.updateColor(new Runnable() {
						public void run() {
							setTexturePanelBackground(cb.getMoreColor());
						}
					});
				} else if (id == ColorRectangle.COLORS.length) {
					setTexturePanelBackground(cb.getMoreColor());
				} else {
					setTexturePanelBackground(ColorRectangle.COLORS[id]);
				}
			}
		});
		p3.add(colorComboBox, BorderLayout.CENTER);
		label.setLabelFor(colorComboBox);

		return p;

	}

	class TexturePanel extends JPanel {

		private boolean selected;
		private byte style = TextureFactory.POLKA;
		private int cellWidth = 12;
		private int cellHeight = 12;

		public TexturePanel(byte style, int cellWidth, int cellHeight) {
			setBackground(Color.white);
			setForeground(Color.black);
			this.style = style;
			this.cellWidth = cellWidth;
			this.cellHeight = cellHeight;
		}

		public void setStyle(byte i) {
			style = i;
		}

		public byte getStyle() {
			return style;
		}

		public void setCellWidth(int i) {
			cellWidth = i;
		}

		public int getCellWidth() {
			return cellWidth;
		}

		public void setCellHeight(int i) {
			cellHeight = i;
		}

		public int getCellHeight() {
			return cellHeight;
		}

		public void setSelected(boolean b) {
			selected = b;
		}

		public boolean isSelected() {
			return selected;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			int w = getWidth();
			int h = getHeight();
			g2d.setPaint(TextureFactory.createPattern(style, cellWidth, cellHeight, getForeground(), getBackground()));
			g2d.fillRect(0, 0, w, h);
			if (selected) {
				g2d.setStroke(dashed);
				g2d.setColor(Color.white);
				g2d.drawRect(2, 2, w - 5, h - 5);
			}
		}

	}

	public FillPattern getFillPattern() {
		return fillPattern;
	}

	public void setFillPattern(FillPattern fm) {
		fillPattern = fm;
	}

}
