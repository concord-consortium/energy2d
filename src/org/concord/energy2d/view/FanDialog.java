package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Fan;

/**
 * @author Charles Xie
 * 
 */
class FanDialog extends JDialog {

	private Window owner;
	private ActionListener okListener;
	private JTextField xField;
	private JTextField yField;
	private JTextField wField;
	private JTextField hField;
	private JTextField speedField;
	private JTextField labelField;
	private JTextField uidField;

	FanDialog(final View2D view, final Fan fan, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Fan (#" + view.model.getFans().indexOf(fan) + ") Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(fan, ManipulationEvent.PROPERTY_CHANGE);
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

				Rectangle2D.Float r = (Rectangle2D.Float) fan.getShape();

				float a = parse(xField.getText());
				if (Float.isNaN(a))
					return;
				r.x = a;

				a = parse(yField.getText());
				if (Float.isNaN(a))
					return;
				r.y = view.model.getLy() - a;

				a = parse(wField.getText());
				if (Float.isNaN(a))
					return;
				r.width = a;

				a = parse(hField.getText());
				if (Float.isNaN(a))
					return;
				r.height = a;

				a = parse(speedField.getText());
				if (Float.isNaN(a))
					return;
				fan.setSpeed(a);

				fan.setLabel(labelField.getText());
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(fan.getUid())) {
						if (view.model.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						fan.setUid(uid);
					}
				}

				view.notifyManipulationListeners(fan, ManipulationEvent.PROPERTY_CHANGE);
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

		// dimension

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Dimension"));
		box.add(p);

		Rectangle2D.Float r = (Rectangle2D.Float) fan.getShape();

		p.add(new JLabel("X:"));
		xField = new JTextField(r.x + "", 10);
		xField.addActionListener(okListener);
		p.add(xField);

		p.add(new JLabel("Y:"));
		yField = new JTextField((view.model.getLy() - r.y) + "", 10);
		yField.addActionListener(okListener);
		p.add(yField);

		p.add(new JLabel("Width:"));
		wField = new JTextField(r.width + "", 10);
		wField.addActionListener(okListener);
		p.add(wField);

		p.add(new JLabel("Height:"));
		hField = new JTextField(r.height + "", 10);
		hField.addActionListener(okListener);
		p.add(hField);

		// other properties

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Other properties"));
		box.add(p);

		p.add(new JLabel("Speed:"));
		speedField = new JTextField(fan.getSpeed() + "", 10);
		speedField.addActionListener(okListener);
		p.add(speedField);

		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(fan.getUid(), 10);
		uidField.addActionListener(okListener);
		p.add(uidField);

		p.add(new JLabel("Label:"));
		labelField = new JTextField(fan.getLabel(), 10);
		labelField.addActionListener(okListener);
		p.add(labelField);

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
