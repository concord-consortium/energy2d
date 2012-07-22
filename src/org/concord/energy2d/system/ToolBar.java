/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.concord.energy2d.event.GraphEvent;
import org.concord.energy2d.event.GraphListener;
import org.concord.energy2d.event.IOEvent;
import org.concord.energy2d.event.IOListener;
import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.event.ManipulationListener;
import org.concord.energy2d.util.MiscUtil;
import org.concord.energy2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class ToolBar extends JToolBar implements GraphListener, IOListener, ManipulationListener {

	private JToggleButton graphButton;
	private JToggleButton selectButton;

	private System2D box;

	ToolBar(System2D s2d) {

		super(HORIZONTAL);
		setFloatable(false);

		box = s2d;

		box.view.addGraphListener(this);

		ButtonGroup bg = new ButtonGroup();

		selectButton = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/select.png")));
		selectButton.setToolTipText("Select and move an object");
		selectButton.setSelected(true);
		selectButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.SELECT_MODE);
			}
		});
		add(selectButton);
		bg.add(selectButton);

		JToggleButton x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/rectangle.png")));
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

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/ellipse.png")));
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

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/polygon.png")));
		x.setToolTipText("Draw a polygon");
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

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/heat.png")));
		x.setToolTipText("Click to heat, shift-click to cool");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.HEATING_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/thermometer.png")));
		x.setToolTipText("Add a thermometer");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.view.setActionMode(View2D.THERMOMETER_MODE);
				graphButton.setEnabled(true);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		graphButton = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/graph.png")));
		graphButton.setToolTipText("Show or hide graphs");
		graphButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JToggleButton src = (JToggleButton) e.getSource();
				box.view.setGraphOn(src.isSelected());
				box.view.repaint();
				box.view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		add(graphButton);
		addSeparator(new Dimension(10, 0));

		JComboBox cb = new JComboBox(new String[] { "Mouse: Nothing", "Mouse: Temperature", "Mouse: Energy" });
		cb.setToolTipText("Select a property the value of which at a mouse position will be shown when it moves");
		cb.setMaximumSize(new Dimension(150, 32));
		cb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					JComboBox src = (JComboBox) e.getSource();
					box.view.setMouseReadType((byte) src.getSelectedIndex());
				}
			}
		});
		add(cb);

	}

	public void graphClosed(GraphEvent e) {
		MiscUtil.setSelectedSilently(graphButton, false);
	}

	public void graphOpened(GraphEvent e) {
		MiscUtil.setSelectedSilently(graphButton, true);
	}

	public void manipulationOccured(ManipulationEvent e) {
		switch (e.getType()) {
		case ManipulationEvent.GRAPH:
			MiscUtil.setSelectedSilently(graphButton, !box.model.getThermometers().isEmpty());
			break;
		case ManipulationEvent.OBJECT_ADDED:
			selectButton.doClick();
			break;
		}
	}

	public void ioOccured(IOEvent e) {
		switch (e.getType()) {
		case IOEvent.FILE_INPUT:
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					selectButton.doClick();
					selectButton.requestFocusInWindow();
					MiscUtil.setSelectedSilently(graphButton, box.view.isGraphOn());
					graphButton.setEnabled(!box.model.getThermometers().isEmpty());
				}
			});
			break;
		case IOEvent.FILE_OUTPUT:
			break;
		}
	}

}
