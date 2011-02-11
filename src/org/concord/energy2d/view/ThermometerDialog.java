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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Thermometer;

/**
 * @author Charles Xie
 * 
 */
class ThermometerDialog extends JDialog {

	private Window owner;
	private JPanel p1, p2;

	ThermometerDialog(final View2D view, final Thermometer t, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Thermometer (#"
				+ view.model.getThermometers().indexOf(t) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});
		buttonPanel.add(button);

		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		box.add(p);

		JCheckBox checkBox = new JCheckBox("Thermostat");
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
		p.add(checkBox);

		p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		box.add(p1);

		JLabel label = new JLabel("Target temperature: ");
		p1.add(label);
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
		p1.add(textField);
		label = new JLabel("\u2103");
		p1.add(label);

		p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		box.add(p2);

		label = new JLabel("Controlled power source:");
		p2.add(label);

		JComboBox comboBox = new JComboBox();
		p2.add(comboBox);

		enableThermostatSettings(t.isThermostat());

		pack();
		setLocationRelativeTo(view);

	}

	private void enableThermostatSettings(boolean b) {
		int n = p1.getComponentCount();
		for (int i = 0; i < n; i++)
			p1.getComponent(i).setEnabled(b);
		n = p2.getComponentCount();
		for (int i = 0; i < n; i++)
			p2.getComponent(i).setEnabled(b);
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
