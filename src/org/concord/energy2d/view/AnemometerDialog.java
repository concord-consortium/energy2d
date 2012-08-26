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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Anemometer;
import org.concord.energy2d.model.Sensor;

/**
 * @author Charles Xie
 * 
 */
class AnemometerDialog extends JDialog {

	private Window owner;
	private ActionListener okListener;
	private JTextField xField;
	private JTextField yField;
	private JTextField labelField;
	private JTextField uidField;
	private JRadioButton onePointButton;
	private JRadioButton fivePointsButton;
	private JRadioButton ninePointsButton;

	AnemometerDialog(final View2D view, final Anemometer anemometer, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Anemometer (#" + view.model.getAnemometers().indexOf(anemometer) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(anemometer, ManipulationEvent.PROPERTY_CHANGE);
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

				float x = parse(xField.getText());
				if (Float.isNaN(x))
					return;
				anemometer.setX(x);

				x = parse(yField.getText());
				if (Float.isNaN(x))
					return;
				anemometer.setY(view.model.getLy() - x);

				anemometer.setLabel(labelField.getText());
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(anemometer.getUid())) {
						if (view.model.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						anemometer.setUid(uid);
					}
				}

				if (onePointButton.isSelected())
					anemometer.setStencil(Sensor.ONE_POINT);
				else if (fivePointsButton.isSelected())
					anemometer.setStencil(Sensor.FIVE_POINT);
				else if (ninePointsButton.isSelected())
					anemometer.setStencil(Sensor.NINE_POINT);

				view.notifyManipulationListeners(anemometer, ManipulationEvent.PROPERTY_CHANGE);
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
		xField = new JTextField(anemometer.getX() + "", 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y:"));
		yField = new JTextField((view.model.getLy() - anemometer.getY()) + "", 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(anemometer.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(anemometer.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

		// anemometer calibration: how small it can be

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Sampled area (stencil)"));
		box.add(p);

		ButtonGroup bg = new ButtonGroup();

		onePointButton = new JRadioButton("One point");
		p.add(onePointButton);
		bg.add(onePointButton);

		fivePointsButton = new JRadioButton("Five points");
		p.add(fivePointsButton);
		bg.add(fivePointsButton);

		ninePointsButton = new JRadioButton("Nine points");
		p.add(ninePointsButton);
		bg.add(ninePointsButton);

		switch (anemometer.getStencil()) {
		case 5:
			fivePointsButton.setSelected(true);
			break;
		case 9:
			ninePointsButton.setSelected(true);
			break;
		default:
			onePointButton.setSelected(true);
		}

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
