/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.util.BackgroundComboBox;
import org.concord.energy2d.util.ColorFill;
import org.concord.energy2d.util.ColorMenu;
import org.concord.energy2d.util.ColoredLabel;
import org.concord.energy2d.util.FillEffectChooser;
import org.concord.energy2d.util.FillPattern;

/**
 * @author Charles Xie
 * 
 */
class PartViewDialog extends JDialog {

	private JColorChooser colorChooser;
	private FillEffectChooser fillEffectChooser;
	private ColoredLabel coloredLabel;
	private JCheckBox visibleCheckBox, draggableCheckBox, borderOnlyCheckBox;
	private BackgroundComboBox bgComboBox;
	private ActionListener okListener;

	PartViewDialog(final View2D view, final Part part, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Part (#" + view.model.getParts().indexOf(part) + ") View Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				part.setDraggable(draggableCheckBox.isSelected());
				part.setVisible(visibleCheckBox.isSelected());
				part.setFilled(!borderOnlyCheckBox.isSelected());
				FillPattern fillPattern = part.getFillPattern();
				if (fillPattern instanceof ColorFill) {
					part.setFillPattern(new ColorFill(coloredLabel.getBackground()));
				}

				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();

				PartViewDialog.this.dispose();

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
				PartViewDialog.this.dispose();
			}
		});
		buttonPanel.add(button);

		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(box, BorderLayout.CENTER);

		Box miscBox = Box.createVerticalBox();
		miscBox.setBorder(BorderFactory.createTitledBorder("Miscellaneous"));
		box.add(miscBox);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);
		draggableCheckBox = new JCheckBox("Draggable by user", part.isDraggable());
		p.add(draggableCheckBox);
		visibleCheckBox = new JCheckBox("Visible", part.isVisible());
		p.add(visibleCheckBox);
		borderOnlyCheckBox = new JCheckBox("Show border only", !part.isFilled());
		p.add(borderOnlyCheckBox);
		FillPattern fillPattern = part.getFillPattern();
		Color color = Color.gray;
		if (fillPattern instanceof ColorFill) {
			color = ((ColorFill) fillPattern).getColor();
		}
		coloredLabel = new ColoredLabel(color);
		p.add(coloredLabel);

		colorChooser = new JColorChooser();
		fillEffectChooser = new FillEffectChooser();

		bgComboBox = new BackgroundComboBox(this, colorChooser, fillEffectChooser);
		bgComboBox.setToolTipText("Background filling");
		bgComboBox.setFillPattern(part.getFillPattern());
		bgComboBox.getColorMenu().setNoFillAction(new AbstractAction("No Fill") {
			public void actionPerformed(ActionEvent e) {
				part.setFilled(true);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, null);
			}
		});
		bgComboBox.getColorMenu().setColorArrayAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				FillPattern fm = new ColorFill(bgComboBox.getColorMenu().getColor());
				if (fm.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fm);
				view.repaint();
			}
		});
		bgComboBox.getColorMenu().setMoreColorAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FillPattern fm = new ColorFill(bgComboBox.getColorMenu().getColorChooser().getColor());
				if (fm.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fm);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, fm);
			}
		});
		bgComboBox.getColorMenu().addHexColorListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = bgComboBox.getColorMenu().getHexInputColor(part.getFillPattern() instanceof ColorFill ? ((ColorFill) part.getFillPattern()).getColor() : null);
				if (c == null)
					return;
				FillPattern fm = new ColorFill(c);
				if (fm.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fm);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, fm);
			}
		});
		bgComboBox.getColorMenu().setFillEffectActions(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FillPattern fm = bgComboBox.getColorMenu().getFillEffectChooser().getFillPattern();
				if (fm.equals(part.getFillPattern()))
					return;
				part.setFillPattern(fm);
				view.repaint();
				bgComboBox.getColorMenu().firePropertyChange(ColorMenu.FILLING, null, fm);
			}
		}, null);
		p.add(bgComboBox);

		pack();
		setLocationRelativeTo(view);

	}

}
