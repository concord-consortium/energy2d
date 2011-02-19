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

	ThermometerDialog(final View2D view, final Thermometer t, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Thermometer (#"
				+ view.model.getThermometers().indexOf(t) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});
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

		// thermometer calibration

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Average over stencil"));
		box.add(p);

		ButtonGroup bg = new ButtonGroup();

		JRadioButton rb = new JRadioButton("One point");
		rb.setSelected(true);
		p.add(rb);
		bg.add(rb);

		rb = new JRadioButton("Five points");
		p.add(rb);
		bg.add(rb);

		rb = new JRadioButton("Nine points");
		p.add(rb);
		bg.add(rb);

		// thermostat properties

		thermostatPanel = new JPanel(new SpringLayout());
		thermostatPanel.setBorder(BorderFactory.createTitledBorder("Thermostat"));
		box.add(thermostatPanel);
		int count = 0;

		JCheckBox checkBox = new JCheckBox("Activate");
		checkBox.setSelected(t.isThermostat());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				boolean b = src.isSelected();
				t.setThermostat(b);
				enableThermostatSettings(b);
				view.repaint();
			}
		});
		thermostatPanel.add(checkBox);

		JLabel label = new JLabel("Target temperature: ");
		thermostatPanel.add(label);
		JTextField textField = new JTextField(t.getThermostatTemperature() + "", 10);
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField src = (JTextField) e.getSource();
				float x = parse(src.getText());
				if (Float.isNaN(x))
					return;
				t.setThermostatTemperature(x);
			}
		});
		thermostatPanel.add(textField);
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

		enableThermostatSettings(t.isThermostat());

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
			JOptionPane.showMessageDialog(owner, "Cannot parse: " + s, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

}
