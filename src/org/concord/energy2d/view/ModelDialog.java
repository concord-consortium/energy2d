/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
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
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy2d.model.Boundary;
import org.concord.energy2d.model.DirichletHeatBoundary;
import org.concord.energy2d.model.HeatBoundary;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.NeumannHeatBoundary;
import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ModelDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final static DecimalFormat FORMAT = new DecimalFormat("####.########");

	private JTextField steplengthField;
	private JTextField bgTemperatureField;
	private JTextField conductivityField;
	private JTextField capacityField;
	private JTextField densityField;
	private JLabel viscosityLabel;
	private JTextField viscosityField;
	private JLabel buoyancyLabel;
	private JTextField buoyancyField;
	private JTextField wField, hField;
	private JLabel upperBoundaryLabel;
	private JLabel lowerBoundaryLabel;
	private JLabel leftBoundaryLabel;
	private JLabel rightBoundaryLabel;
	private JLabel upperBoundaryLabel2;
	private JLabel lowerBoundaryLabel2;
	private JLabel leftBoundaryLabel2;
	private JLabel rightBoundaryLabel2;
	private JTextField upperBoundaryField;
	private JTextField lowerBoundaryField;
	private JTextField leftBoundaryField;
	private JTextField rightBoundaryField;
	private JLabel solarPowerLabel;
	private JTextField solarPowerField;
	private JLabel raySpeedLabel;
	private JTextField raySpeedField;
	private JLabel rayNumberLabel;
	private JTextField rayNumberField;
	private JLabel emissionIntervalLabel;
	private JTextField emissionIntervalField;
	private JComboBox boundaryComboBox;
	private JLabel sunAngleLabel;
	private JSlider sunAngleSlider;
	private JCheckBox sunnyCheckBox;
	private JCheckBox convectiveCheckBox;
	private JLabel buoyancyApproximationLabel;
	private JComboBox buoyancyApproximationComboBox;
	private Window owner;
	private ActionListener okListener;

	ModelDialog(final View2D view, final Model2D model, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Model Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float bgTemperature = parse(bgTemperatureField.getText());
				if (Float.isNaN(bgTemperature))
					return;
				float conductivity = parse(conductivityField.getText());
				if (Float.isNaN(conductivity))
					return;
				float capacity = parse(capacityField.getText());
				if (Float.isNaN(capacity))
					return;
				float density = parse(densityField.getText());
				if (Float.isNaN(density))
					return;
				float viscosity = parse(viscosityField.getText());
				if (Float.isNaN(viscosity))
					return;
				float buoyancy = parse(buoyancyField.getText());
				if (Float.isNaN(buoyancy))
					return;
				float steplength = parse(steplengthField.getText());
				if (Float.isNaN(steplength))
					return;
				float width = parse(wField.getText());
				if (Float.isNaN(width))
					return;
				float height = parse(hField.getText());
				if (Float.isNaN(height))
					return;
				float valueAtLeft = parse(leftBoundaryField.getText());
				if (Float.isNaN(valueAtLeft))
					return;
				float valueAtRight = parse(rightBoundaryField.getText());
				if (Float.isNaN(valueAtRight))
					return;
				float valueAtUpper = parse(upperBoundaryField.getText());
				if (Float.isNaN(valueAtUpper))
					return;
				float valueAtLower = parse(lowerBoundaryField.getText());
				if (Float.isNaN(valueAtLower))
					return;
				float solarPower = parse(solarPowerField.getText());
				if (Float.isNaN(solarPower))
					return;
				float raySpeed = parse(raySpeedField.getText());
				if (Float.isNaN(raySpeed))
					return;
				float rayNumber = parse(rayNumberField.getText());
				if (Float.isNaN(rayNumber))
					return;
				float emissionInterval = parse(emissionIntervalField.getText());
				if (Float.isNaN(emissionInterval))
					return;

				if (steplength <= 0) {
					JOptionPane.showMessageDialog(ModelDialog.this,
							"Time step must be greater than zero!", "Time step error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				model.setTimeStep(steplength);
				model.setBackgroundTemperature(bgTemperature);
				model.setBackgroundConductivity(Math.max(conductivity, 0.000000001f));
				model.setBackgroundSpecificHeat(capacity);
				model.setBackgroundDensity(density);
				model.setBackgroundViscosity(viscosity);
				model.setThermalBuoyancy(buoyancy);
				model.setLx(width);
				model.setLy(height);
				model.setSolarPowerDensity(solarPower);
				model.setSolarRaySpeed(raySpeed);
				model.setSolarRayCount((int) rayNumber);
				model.setPhotonEmissionInterval((int) emissionInterval);
				model.setSunAngle((float) Math.toRadians(sunAngleSlider.getValue()));

				switch (boundaryComboBox.getSelectedIndex()) {
				case 0:
					DirichletHeatBoundary dhb = new DirichletHeatBoundary();
					dhb.setTemperatureAtBorder(Boundary.LEFT, valueAtLeft);
					dhb.setTemperatureAtBorder(Boundary.RIGHT, valueAtRight);
					dhb.setTemperatureAtBorder(Boundary.UPPER, valueAtUpper);
					dhb.setTemperatureAtBorder(Boundary.LOWER, valueAtLower);
					model.setHeatBoundary(dhb);
					break;
				case 1:
					NeumannHeatBoundary nhb = new NeumannHeatBoundary();
					nhb.setFluxAtBorder(Boundary.LEFT, valueAtLeft);
					nhb.setFluxAtBorder(Boundary.RIGHT, valueAtRight);
					nhb.setFluxAtBorder(Boundary.UPPER, valueAtUpper);
					nhb.setFluxAtBorder(Boundary.LOWER, valueAtLower);
					model.setHeatBoundary(nhb);
					break;
				}

				model.setSunny(sunnyCheckBox.isSelected());
				model.setConvective(convectiveCheckBox.isSelected());
				model.setBuoyancyApproximation((byte) buoyancyApproximationComboBox
						.getSelectedIndex());

				model.refreshMaterialPropertyArrays();

				view.repaint();

				if (!(e.getSource() instanceof JComboBox))
					ModelDialog.this.dispose();

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
				ModelDialog.this.dispose();
			}
		});
		buttonPanel.add(button);

		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);

		JPanel p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("General"));
		box.add(p);
		int count = 0;

		convectiveCheckBox = new JCheckBox("Convective");
		convectiveCheckBox.setSelected(model.isConvective());
		convectiveCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean b = convectiveCheckBox.isSelected();
				viscosityLabel.setEnabled(b);
				viscosityField.setEnabled(b);
				buoyancyLabel.setEnabled(b);
				buoyancyField.setEnabled(b);
				buoyancyApproximationLabel.setEnabled(b);
				buoyancyApproximationComboBox.setEnabled(b);
			}
		});
		p.add(convectiveCheckBox);

		sunnyCheckBox = new JCheckBox("Sunny");
		sunnyCheckBox.setSelected(model.isSunny());
		sunnyCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean b = sunnyCheckBox.isSelected();
				sunAngleSlider.setEnabled(b);
				emissionIntervalLabel.setEnabled(b);
				emissionIntervalField.setEnabled(b);
				rayNumberLabel.setEnabled(b);
				rayNumberField.setEnabled(b);
				raySpeedLabel.setEnabled(b);
				raySpeedField.setEnabled(b);
				solarPowerLabel.setEnabled(b);
				solarPowerField.setEnabled(b);
			}
		});
		p.add(sunnyCheckBox);

		// dummy
		JLabel label = new JLabel();
		p.add(label);

		label = new JLabel("Width");
		p.add(label);
		wField = new JTextField(FORMAT.format(model.getLx()), 8);
		wField.addActionListener(okListener);
		p.add(wField);
		label = new JLabel("<html><i>m");
		p.add(label);
		count++;

		label = new JLabel("Time steplength");
		p.add(label);
		steplengthField = new JTextField(FORMAT.format(model.getTimeStep()), 8);
		steplengthField.addActionListener(okListener);
		p.add(steplengthField);
		label = new JLabel("<html><i>s");
		p.add(label);

		label = new JLabel("Height");
		p.add(label);
		hField = new JTextField(FORMAT.format(model.getLy()), 8);
		hField.addActionListener(okListener);
		p.add(hField);
		label = new JLabel("<html><i>m");
		p.add(label);
		count++;

		MiscUtil.makeCompactGrid(p, count, 6, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("Fluid"));
		box.add(p);
		count = 0;

		label = new JLabel("Background temperature");
		p.add(label);
		bgTemperatureField = new JTextField(FORMAT.format(model.getBackgroundTemperature()), 16);
		bgTemperatureField.addActionListener(okListener);
		p.add(bgTemperatureField);
		label = new JLabel("<html><i>\u2103");
		p.add(label);
		count++;

		label = new JLabel("Conductivity");
		p.add(label);
		conductivityField = new JTextField(FORMAT.format(model.getBackgroundConductivity()), 16);
		conductivityField.addActionListener(okListener);
		p.add(conductivityField);
		label = new JLabel("<html><i>W/(m\u00b7\u2103)");
		p.add(label);
		count++;

		label = new JLabel("Specific heat");
		p.add(label);
		capacityField = new JTextField(FORMAT.format(model.getBackgroundSpecificHeat()), 16);
		capacityField.addActionListener(okListener);
		p.add(capacityField);
		label = new JLabel("<html><i>J/(kg\u00b7\u2103)");
		p.add(label);
		count++;

		label = new JLabel("Density");
		p.add(label);
		densityField = new JTextField(FORMAT.format(model.getBackgroundDensity()), 16);
		densityField.addActionListener(okListener);
		p.add(densityField);
		label = new JLabel("<html><i>kg/m<sup><font size=2>3</font></sup></html>");
		p.add(label);
		count++;

		viscosityLabel = new JLabel("Kinematic viscosity");
		viscosityLabel.setEnabled(model.isConvective());
		p.add(viscosityLabel);
		viscosityField = new JTextField(FORMAT.format(model.getBackgroundViscosity()), 16);
		viscosityField.setEnabled(model.isConvective());
		viscosityField.addActionListener(okListener);
		p.add(viscosityField);
		label = new JLabel("<html><i>m<sup><font size=2>2</font></sup>/s</html>");
		p.add(label);
		count++;

		buoyancyLabel = new JLabel("Thermal buoyancy");
		buoyancyLabel.setEnabled(model.isConvective());
		p.add(buoyancyLabel);
		buoyancyField = new JTextField(FORMAT.format(model.getThermalBuoyancy()), 16);
		buoyancyField.setEnabled(model.isConvective());
		buoyancyField.addActionListener(okListener);
		p.add(buoyancyField);
		label = new JLabel("<html><i>m/(s<sup><font size=2>2</font></sup>\u00b7\u2103)</html>)");
		p.add(label);
		count++;

		buoyancyApproximationLabel = new JLabel("Buoyancy approximation");
		buoyancyApproximationLabel.setEnabled(model.isConvective());
		p.add(buoyancyApproximationLabel);
		buoyancyApproximationComboBox = new JComboBox(new String[] { "All-cell average",
				"Column average" });
		buoyancyApproximationComboBox.setEnabled(model.isConvective());
		buoyancyApproximationComboBox.setSelectedIndex(model.getBuoyancyApproximation());
		p.add(buoyancyApproximationComboBox);
		label = new JLabel();
		p.add(label);
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("Radiation"));
		box.add(p);
		count = 0;

		solarPowerLabel = new JLabel("Solar power density");
		solarPowerLabel.setEnabled(model.isSunny());
		p.add(solarPowerLabel);
		solarPowerField = new JTextField(FORMAT.format(model.getSolarPowerDensity()), 16);
		solarPowerField.setEnabled(model.isSunny());
		solarPowerField.addActionListener(okListener);
		p.add(solarPowerField);
		label = new JLabel("<html><i>W/m<sup><font size=2>3</font></sup></html>)");
		p.add(label);
		count++;

		rayNumberLabel = new JLabel("Ray number");
		rayNumberLabel.setEnabled(model.isSunny());
		p.add(rayNumberLabel);
		rayNumberField = new JTextField(FORMAT.format(model.getSolarRayCount()), 16);
		rayNumberField.setEnabled(model.isSunny());
		rayNumberField.addActionListener(okListener);
		p.add(rayNumberField);
		label = new JLabel();
		p.add(label);
		count++;

		raySpeedLabel = new JLabel("Ray speed");
		raySpeedLabel.setEnabled(model.isSunny());
		p.add(raySpeedLabel);
		raySpeedField = new JTextField(FORMAT.format(model.getSolarRaySpeed()), 16);
		raySpeedField.setEnabled(model.isSunny());
		raySpeedField.addActionListener(okListener);
		p.add(raySpeedField);
		label = new JLabel("<html><i>m/s");
		p.add(label);
		count++;

		emissionIntervalLabel = new JLabel("Emission interval");
		emissionIntervalLabel.setEnabled(model.isSunny());
		p.add(emissionIntervalLabel);
		emissionIntervalField = new JTextField(FORMAT.format(model.getPhotonEmissionInterval()), 16);
		emissionIntervalField.setEnabled(model.isSunny());
		emissionIntervalField.addActionListener(okListener);
		p.add(emissionIntervalField);
		label = new JLabel();
		p.add(label);
		count++;

		sunAngleLabel = new JLabel("Sun angle");
		sunAngleLabel.setEnabled(model.isSunny());
		p.add(sunAngleLabel);
		sunAngleSlider = new JSlider(0, 180, (int) Math.toDegrees(model.getSunAngle()));
		sunAngleSlider.setEnabled(model.isSunny());
		sunAngleSlider.setPaintTicks(true);
		sunAngleSlider.setMajorTickSpacing(45);
		sunAngleSlider.setMinorTickSpacing(15);
		sunAngleSlider.setPaintLabels(true);
		Hashtable<Integer, JLabel> ht = new Hashtable<Integer, JLabel>();
		ht.put(0, new JLabel("0\u00b0"));
		ht.put(45, new JLabel("45\u00b0"));
		ht.put(90, new JLabel("90\u00b0"));
		ht.put(135, new JLabel("135\u00b0"));
		ht.put(180, new JLabel("180\u00b0"));
		sunAngleSlider.setLabelTable(ht);
		p.add(sunAngleSlider);
		label = new JLabel("Degree");
		p.add(label);
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		p.setBorder(BorderFactory.createTitledBorder("Boundary"));
		box.add(p);
		count = 0;

		label = new JLabel("Heat boundary condition");
		p.add(label);
		boundaryComboBox = new JComboBox(new String[] { "Dirichlet (constant temperature)",
				"Neumann (constant heat flux)" });
		if (model.getHeatBoundary() instanceof DirichletHeatBoundary) {
			boundaryComboBox.setSelectedIndex(0);
		} else if (model.getHeatBoundary() instanceof NeumannHeatBoundary) {
			boundaryComboBox.setSelectedIndex(1);
		}
		boundaryComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					switch (boundaryComboBox.getSelectedIndex()) {
					case 0:
						setHeatBoundaryFields(new DirichletHeatBoundary());
						break;
					case 1:
						setHeatBoundaryFields(new NeumannHeatBoundary());
						break;
					}
				}
			}
		});
		boundaryComboBox.addActionListener(okListener);
		p.add(boundaryComboBox);
		label = new JLabel();
		p.add(label);
		count++;

		leftBoundaryLabel = new JLabel();
		p.add(leftBoundaryLabel);
		leftBoundaryField = new JTextField();
		leftBoundaryField.addActionListener(okListener);
		p.add(leftBoundaryField);
		leftBoundaryLabel2 = new JLabel();
		p.add(leftBoundaryLabel2);
		count++;

		rightBoundaryLabel = new JLabel();
		p.add(rightBoundaryLabel);
		rightBoundaryField = new JTextField();
		rightBoundaryField.addActionListener(okListener);
		p.add(rightBoundaryField);
		rightBoundaryLabel2 = new JLabel();
		p.add(rightBoundaryLabel2);
		count++;

		upperBoundaryLabel = new JLabel();
		p.add(upperBoundaryLabel);
		upperBoundaryField = new JTextField();
		upperBoundaryField.addActionListener(okListener);
		p.add(upperBoundaryField);
		upperBoundaryLabel2 = new JLabel();
		p.add(upperBoundaryLabel2);
		count++;

		lowerBoundaryLabel = new JLabel();
		p.add(lowerBoundaryLabel);
		lowerBoundaryField = new JTextField();
		lowerBoundaryField.addActionListener(okListener);
		p.add(lowerBoundaryField);
		lowerBoundaryLabel2 = new JLabel();
		p.add(lowerBoundaryLabel2);
		count++;

		setHeatBoundaryFields(model.getHeatBoundary());
		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		pack();
		setLocationRelativeTo(view);

	}

	private void setHeatBoundaryFields(HeatBoundary heatBoundary) {
		if (heatBoundary instanceof DirichletHeatBoundary) {
			DirichletHeatBoundary b = (DirichletHeatBoundary) heatBoundary;
			leftBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.LEFT)));
			rightBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.RIGHT)));
			upperBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.UPPER)));
			lowerBoundaryField.setText(FORMAT.format(b.getTemperatureAtBorder(Boundary.LOWER)));
			leftBoundaryLabel.setText("Left boundary temperature");
			rightBoundaryLabel.setText("Right boundary temperature");
			upperBoundaryLabel.setText("Upper boundary temperature");
			lowerBoundaryLabel.setText("Lower boundary temperature");
			leftBoundaryLabel2.setText("<html><i>\u2103");
			rightBoundaryLabel2.setText("<html><i>\u2103");
			upperBoundaryLabel2.setText("<html><i>\u2103");
			lowerBoundaryLabel2.setText("<html><i>\u2103");
		} else if (heatBoundary instanceof NeumannHeatBoundary) {
			NeumannHeatBoundary b = (NeumannHeatBoundary) heatBoundary;
			leftBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.LEFT)));
			rightBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.RIGHT)));
			upperBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.UPPER)));
			lowerBoundaryField.setText(FORMAT.format(b.getFluxAtBorder(Boundary.LOWER)));
			leftBoundaryLabel.setText("Left boundary heat flux");
			rightBoundaryLabel.setText("Right boundary heat flux");
			upperBoundaryLabel.setText("Upper boundary heat flux");
			lowerBoundaryLabel.setText("Lower boundary heat flux");
			leftBoundaryLabel2.setText("<html><i>\u2103/m");
			rightBoundaryLabel2.setText("<html><i>\u2103/m");
			upperBoundaryLabel2.setText("<html><i>\u2103/m");
			lowerBoundaryLabel2.setText("<html><i>\u2103/m");
		}
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
