package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermostat;

/**
 * @author Charles Xie
 * 
 */
class ThermostatDialog extends JDialog {

	private Window owner;
	private ActionListener okListener;
	private JTextField setpointField;
	private JTextField deadbandField;
	private JCheckBox enableCheckBox;

	ThermostatDialog(final View2D view, final Part powerSource, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Thermostat", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(powerSource, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (enableCheckBox.isSelected()) {
					float setpoint = parse(setpointField.getText());
					if (Float.isNaN(setpoint))
						return;
					float deadband = parse(deadbandField.getText());
					if (Float.isNaN(deadband))
						return;
					Thermostat t = view.model.addThermostat(null, powerSource);
					t.setSetPoint(setpoint);
					t.setDeadband(deadband);
				} else {
					view.model.removeThermostat(null, powerSource);
				}

				view.notifyManipulationListeners(powerSource, ManipulationEvent.PROPERTY_CHANGE);
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

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		p.setBorder(BorderFactory.createTitledBorder("Thermostat"));
		box.add(p);

		Thermostat thermostat = view.model.getThermostat(null, powerSource);

		enableCheckBox = new JCheckBox("Enabled", thermostat != null);
		p.add(enableCheckBox);

		p.add(new JLabel("Set point: "));
		setpointField = new JTextField(thermostat == null ? "20" : thermostat.getSetPoint() + "", 10);
		setpointField.addActionListener(okListener);
		p.add(setpointField);
		p.add(new JLabel("\u2103"));

		p.add(new JLabel("Deadband: "));
		deadbandField = new JTextField(thermostat == null ? "1" : thermostat.getDeadband() + "", 10);
		deadbandField.addActionListener(okListener);
		p.add(deadbandField);
		p.add(new JLabel("\u2103"));

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		float x = Float.NaN;
		try {
			x = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(owner, "Cannot parse " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

}
