/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ThermometerDialog extends JDialog {

	private Window owner;
	private JPanel thermostatPanel;
	private ActionListener okListener;
	private JTextField xField;
	private JTextField yField;
	private JTextField labelField;
	private JTextField thermostatField;
	private JRadioButton onePointButton;
	private JRadioButton fivePointsButton;
	private JRadioButton ninePointsButton;
	private JCheckBox thermostatCheckBox;

	ThermometerDialog(final View2D view, final Thermometer thermometer, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Thermometer (#" + view.model.getThermometers().indexOf(thermometer) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float x = parse(xField.getText());
				if (Float.isNaN(x))
					return;
				thermometer.setX(x);

				x = parse(yField.getText());
				if (Float.isNaN(x))
					return;
				thermometer.setY(view.model.getLy() - x);

				thermometer.setLabel(labelField.getText());

				if (onePointButton.isSelected())
					thermometer.setStencil(Thermometer.ONE_POINT);
				else if (fivePointsButton.isSelected())
					thermometer.setStencil(Thermometer.FIVE_POINT);
				else if (ninePointsButton.isSelected())
					thermometer.setStencil(Thermometer.NINE_POINT);

				if (thermostatCheckBox.isSelected()) {
					thermometer.setThermostat(true);
					x = parse(thermostatField.getText());
					if (Float.isNaN(x))
						return;
					thermometer.setThermostatTemperature(x);
				} else {
					thermometer.setThermostat(false);
				}

				view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		};

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(button);

		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);

		// general properties

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("General properties"));
		box.add(p);

		p.add(new JLabel("X:"));
		xField = new JTextField(thermometer.getX() + "", 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y:"));
		yField = new JTextField((view.model.getLy() - thermometer.getY()) + "", 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(thermometer.getLabel(), 20);
		labelField.addActionListener(okListener);
		p.add(labelField);

		// thermometer calibration

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Sampled area (stencil)"));
		box.add(p);

		ButtonGroup bg = new ButtonGroup();

		onePointButton = new JRadioButton("One point");
		onePointButton.setSelected(true);
		p.add(onePointButton);
		bg.add(onePointButton);

		fivePointsButton = new JRadioButton("Five points");
		p.add(fivePointsButton);
		bg.add(fivePointsButton);

		ninePointsButton = new JRadioButton("Nine points");
		p.add(ninePointsButton);
		bg.add(ninePointsButton);

		// thermostat properties

		thermostatPanel = new JPanel(new SpringLayout());
		thermostatPanel.setBorder(BorderFactory.createTitledBorder("Thermostat"));
		box.add(thermostatPanel);
		int count = 0;

		thermostatCheckBox = new JCheckBox("Activate");
		thermostatCheckBox.setSelected(thermometer.isThermostat());
		thermostatCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				enableThermostatSettings(src.isSelected());
			}
		});
		thermostatPanel.add(thermostatCheckBox);

		JLabel label = new JLabel("Target temperature: ");
		thermostatPanel.add(label);
		thermostatField = new JTextField(thermometer.getThermostatTemperature() + "", 10);
		thermostatField.addActionListener(okListener);
		thermostatPanel.add(thermostatField);
		label = new JLabel("\u2103");
		thermostatPanel.add(label);
		count++;

		label = new JLabel("Controlled power source:");
		thermostatPanel.add(label);

		JComboBox comboBox = new JComboBox();
		thermostatPanel.add(comboBox);

		thermostatPanel.add(new JPanel());
		thermostatPanel.add(new JPanel());
		count++;

		MiscUtil.makeCompactGrid(thermostatPanel, count, 4, 2, 5, 10, 2);

		enableThermostatSettings(thermometer.isThermostat());

		pack();
		setLocationRelativeTo(view);

	}

	private void enableThermostatSettings(boolean b) {
		int n = thermostatPanel.getComponentCount();
		for (int i = 1; i < n; i++)
			thermostatPanel.getComponent(i).setEnabled(b);
	}

	private float parse(String s) {
		float x = Float.NaN;
		try {
			x = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(owner, "Cannot parse: " + s, "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

}
