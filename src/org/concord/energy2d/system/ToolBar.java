/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.concord.energy2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class ToolBar extends JToolBar {

	private static final long serialVersionUID = 1L;

	ToolBar(final System2D box) {

		super(HORIZONTAL);
		setFloatable(false);

		ButtonGroup bg = new ButtonGroup();

		JToggleButton x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/select.gif")));
		x.setSelected(true);
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.SELECT_MODE);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/thermometer.gif")));
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.THERMOMETER_MODE);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/recttool.gif")));
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.RECTANGLE_MODE);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/ellipsetool.gif")));
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.ELLIPSE_MODE);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/triangletool.gif")));
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.POLYGON_MODE);
			}
		});
		add(x);
		bg.add(x);

	}

}
