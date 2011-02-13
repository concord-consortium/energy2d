/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.RectangularShape;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.util.ColoredLabel;
import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class PartDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.######");

	private JTextField thermalConductivityField;
	private JTextField specificHeatField;
	private JTextField densityField;
	private JLabel powerLabel;
	private JTextField powerField;
	private JLabel temperatureLabel;
	private JTextField temperatureField;
	private JTextField windSpeedField;
	private JTextField windAngleField;
	private JTextField absorptionField;
	private JTextField reflectionField;
	private JTextField transmissionField;
	private JTextField xField, yField, wField, hField;
	private JTextField uidField;
	private JTextField labelField;
	private ColoredLabel coloredLabel;
	private JCheckBox visibleCheckBox, draggableCheckBox, borderOnlyCheckBox;
	private JRadioButton notHeatSourceRadioButton, powerRadioButton,
			constantTemperatureRadioButton;
	private Window owner;
	private ActionListener okListener;

	PartDialog(final View2D view, final Part part, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Part (#"
				+ view.model.getParts().indexOf(part) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float absorption = parse(absorptionField.getText());
				if (Float.isNaN(absorption))
					return;
				float reflection = parse(reflectionField.getText());
				if (Float.isNaN(reflection))
					return;
				float transmission = parse(transmissionField.getText());
				if (Float.isNaN(transmission))
					return;
				float conductivity = parse(thermalConductivityField.getText());
				if (Float.isNaN(conductivity))
					return;
				float capacity = parse(specificHeatField.getText());
				if (Float.isNaN(capacity))
					return;
				float density = parse(densityField.getText());
				if (Float.isNaN(density))
					return;
				float windSpeed = parse(windSpeedField.getText());
				if (Float.isNaN(windSpeed))
					return;
				float windAngle = parse(windAngleField.getText());
				if (Float.isNaN(windAngle))
					return;
				float xcenter = parse(xField.getText());
				if (Float.isNaN(xcenter))
					return;
				float ycenter = parse(yField.getText());
				if (Float.isNaN(ycenter))
					return;
				float width = Float.NaN;
				if (wField != null) {
					width = parse(wField.getText());
					if (Float.isNaN(width))
						return;
				}
				float height = Float.NaN;
				if (hField != null) {
					height = parse(hField.getText());
					if (Float.isNaN(height))
						return;
				}
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(part.getUid())) {
						if (view.model.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner,
									"UID: " + uid + " has been taken.", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}

				if (notHeatSourceRadioButton.isSelected()
						|| constantTemperatureRadioButton.isSelected()) {
					float temperature = parse(temperatureField.getText());
					if (Float.isNaN(temperature))
						return;
					part.setTemperature(temperature);
				} else if (powerRadioButton.isSelected()) {
					float power = parse(powerField.getText());
					if (Float.isNaN(power))
						return;
					part.setPower(power);
				}
				part.setConstantTemperature(constantTemperatureRadioButton.isSelected());

				Shape shape = part.getShape();
				if (shape instanceof RectangularShape) {
					if (!Float.isNaN(width) && !Float.isNaN(height)) {
						view.resizeManipulableTo(part, xcenter - 0.5f * width, ycenter - 0.5f
								* height, width, height);
					}
				}

				part.setWindAngle((float) Math.toRadians(windAngle));
				part.setWindSpeed(windSpeed);
				part.setThermalConductivity(Math.max(conductivity, 0.000000001f));
				part.setSpecificHeat(capacity);
				part.setDensity(density);
				part.setAbsorption(absorption);
				part.setReflection(reflection);
				part.setTransmission(transmission);
				part.setDraggable(draggableCheckBox.isSelected());
				part.setVisible(visibleCheckBox.isSelected());
				part.setFilled(!borderOnlyCheckBox.isSelected());
				part.setColor(coloredLabel.getBackground());
				part.setLabel(labelField.getText());
				part.setUid(uid);

				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();

				PartDialog.this.dispose();

			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PartDialog.this.dispose();
			}
		});
		buttonPanel.add(button);

		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);

		JPanel p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("Geometry"));
		box.add(p);
		int count = 0;

		JLabel label = new JLabel("Center x");
		p.add(label);
		xField = new JTextField(FORMAT.format(part.getCenter().x));
		xField.addActionListener(okListener);
		p.add(xField);
		label = new JLabel("<html><i>m");
		p.add(label);

		label = new JLabel("Center y");
		p.add(label);
		yField = new JTextField(FORMAT.format(part.getCenter().y));
		yField.addActionListener(okListener);
		p.add(yField);
		label = new JLabel("<html><i>m");
		p.add(label);
		count++;

		if (part.getShape() instanceof RectangularShape) {

			label = new JLabel("Width");
			p.add(label);
			wField = new JTextField(FORMAT.format(part.getShape().getBounds2D().getWidth()));
			wField.addActionListener(okListener);
			p.add(wField);
			label = new JLabel("<html><i>m");
			p.add(label);

			label = new JLabel("Height");
			p.add(label);
			hField = new JTextField(FORMAT.format(part.getShape().getBounds2D().getHeight()));
			hField.addActionListener(okListener);
			p.add(hField);
			label = new JLabel("<html><i>m");
			p.add(label);
			count++;

		}

		MiscUtil.makeCompactGrid(p, count, 6, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("Thermal properties"));
		box.add(p);
		count = 0;

		label = new JLabel("Thermal conductivity");
		p.add(label);
		thermalConductivityField = new JTextField(FORMAT.format(part.getThermalConductivity()), 8);
		thermalConductivityField.addActionListener(okListener);
		p.add(thermalConductivityField);
		label = new JLabel("<html><i>W/(m\u00b7\u2103)");
		p.add(label);
		count++;

		label = new JLabel("Specific heat");
		p.add(label);
		specificHeatField = new JTextField(FORMAT.format(part.getSpecificHeat()), 8);
		specificHeatField.addActionListener(okListener);
		p.add(specificHeatField);
		label = new JLabel("<html><i>J/(kg\u00b7\u2103)");
		p.add(label);
		count++;

		label = new JLabel("Density");
		p.add(label);
		densityField = new JTextField(FORMAT.format(part.getDensity()), 8);
		densityField.addActionListener(okListener);
		p.add(densityField);
		label = new JLabel("<html><i>kg/m<sup><font size=2>3</font></sup></html>");
		p.add(label);
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("Source properties"));
		box.add(p);
		count = 0;

		ButtonGroup bg = new ButtonGroup();
		notHeatSourceRadioButton = new JRadioButton("Not a heat source");
		notHeatSourceRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(true);
					temperatureField.setEnabled(true);
					powerLabel.setEnabled(false);
					powerField.setEnabled(false);
				}
			}
		});
		p.add(notHeatSourceRadioButton);
		bg.add(notHeatSourceRadioButton);

		constantTemperatureRadioButton = new JRadioButton("Constant temperature");
		constantTemperatureRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(true);
					temperatureField.setEnabled(true);
					powerLabel.setEnabled(false);
					powerField.setEnabled(false);
				}
			}
		});
		p.add(constantTemperatureRadioButton);
		bg.add(constantTemperatureRadioButton);

		powerRadioButton = new JRadioButton("Constant power");
		powerRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(false);
					temperatureField.setEnabled(false);
					powerLabel.setEnabled(true);
					powerField.setEnabled(true);
				}
			}
		});
		p.add(powerRadioButton);
		bg.add(powerRadioButton);
		count++;

		powerLabel = new JLabel("Power density");
		p.add(powerLabel);
		powerField = new JTextField(FORMAT.format(part.getPower()), 16);
		powerField.addActionListener(okListener);
		p.add(powerField);
		label = new JLabel("<html><i>W/m<sup><font size=2>3</font></sup></html>");
		p.add(label);
		count++;

		temperatureLabel = new JLabel("Temperature");
		p.add(temperatureLabel);
		temperatureField = new JTextField(FORMAT.format(part.getTemperature()), 16);
		temperatureField.addActionListener(okListener);
		p.add(temperatureField);
		label = new JLabel("<html><i>\u2103");
		p.add(label);
		count++;

		label = new JLabel("Wind speed");
		p.add(label);
		windSpeedField = new JTextField(FORMAT.format(part.getWindSpeed()), 8);
		windSpeedField.addActionListener(okListener);
		p.add(windSpeedField);
		label = new JLabel("<html><i>m/s");
		p.add(label);
		count++;

		label = new JLabel("Wind angle");
		p.add(label);
		windAngleField = new JTextField(FORMAT.format(Math.toDegrees(part.getWindAngle())), 8);
		windAngleField.addActionListener(okListener);
		p.add(windAngleField);
		label = new JLabel("Degrees");
		p.add(label);
		count++;

		if (part.getPower() != 0) {
			powerRadioButton.setSelected(true);
		} else if (part.getConstantTemperature()) {
			constantTemperatureRadioButton.setSelected(true);
		} else {
			notHeatSourceRadioButton.setSelected(true);
		}

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("Optical properties"));
		box.add(p);
		count = 0;

		label = new JLabel("Absorption");
		p.add(label);
		absorptionField = new JTextField(FORMAT.format(part.getAbsorption()), 8);
		absorptionField.addActionListener(okListener);
		p.add(absorptionField);

		label = new JLabel("Reflection");
		p.add(label);
		reflectionField = new JTextField(FORMAT.format(part.getReflection()), 8);
		reflectionField.addActionListener(okListener);
		p.add(reflectionField);
		count++;

		label = new JLabel("Transmission");
		p.add(label);
		transmissionField = new JTextField(FORMAT.format(part.getTransmission()), 16);
		transmissionField.addActionListener(okListener);
		p.add(transmissionField);
		p.add(new JLabel());
		p.add(new JLabel());
		count++;

		MiscUtil.makeCompactGrid(p, count, 4, 5, 5, 10, 2);

		Box miscBox = Box.createVerticalBox();
		miscBox.setBorder(BorderFactory.createTitledBorder("Miscellaneous"));
		box.add(miscBox);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);
		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(part.getUid(), 20);
		uidField.addActionListener(okListener);
		p.add(uidField);
		p.add(new JLabel("Label:"));
		labelField = new JTextField(part.getLabel(), 20);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);
		draggableCheckBox = new JCheckBox("Draggable by user", part.isDraggable());
		p.add(draggableCheckBox);
		visibleCheckBox = new JCheckBox("Visible", part.isVisible());
		p.add(visibleCheckBox);
		borderOnlyCheckBox = new JCheckBox("Show border only", !part.isFilled());
		p.add(borderOnlyCheckBox);
		coloredLabel = new ColoredLabel(part.getColor());
		p.add(coloredLabel);

		pack();
		setLocationRelativeTo(view);

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
