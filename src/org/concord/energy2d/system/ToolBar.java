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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.concord.energy2d.event.GraphEvent;
import org.concord.energy2d.event.GraphListener;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class ToolBar extends JToolBar implements GraphListener {

	private JToggleButton graphButton;

	private static final long serialVersionUID = 1L;

	ToolBar(final System2D box) {

		super(HORIZONTAL);
		setFloatable(false);

		box.view.addGraphListener(this);

		ButtonGroup bg = new ButtonGroup();

		JToggleButton x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/select.png")));
		x.setToolTipText("Select and move an object");
		x.setSelected(true);
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.SELECT_MODE);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/rectangle.png")));
		x.setToolTipText("Draw a rectangle");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.RECTANGLE_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/ellipse.png")));
		x.setToolTipText("Draw an ellipse");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.ELLIPSE_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/polygon.png")));
		x.setToolTipText("Draw an arbitray shape");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.POLYGON_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/thermometer.png")));
		x.setToolTipText("Add a thermometer");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.THERMOMETER_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		graphButton = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/graph.png")));
		graphButton.setToolTipText("Show or hide graphs");
		graphButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JToggleButton src = (JToggleButton) e.getSource();
				box.view.setGraphOn(src.isSelected());
				box.view.repaint();
			}
		});
		add(graphButton);

		x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/grid.png")));
		x.setToolTipText("Show or hide grid lines");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JToggleButton src = (JToggleButton) e.getSource();
				box.view.setGridOn(src.isSelected());
				box.view.repaint();
			}
		});
		add(x);

	}

	public void graphClosed(GraphEvent e) {
		MiscUtil.setSelectedSilently(graphButton, false);
	}

	public void graphOpened(GraphEvent e) {
		MiscUtil.setSelectedSilently(graphButton, true);
	}

}
