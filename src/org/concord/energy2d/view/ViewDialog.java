/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ViewDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	ViewDialog(final View2D view, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "View Options", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViewDialog.this.dispose();
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

		JCheckBox checkBox = new JCheckBox("Isotherm");
		checkBox.setSelected(view.isIsothermOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setIsothermOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		checkBox = new JCheckBox("Velocity");
		checkBox.setSelected(view.isVelocityOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setIsothermOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		checkBox = new JCheckBox("Grid");
		checkBox.setSelected(view.isGridOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setGridOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		checkBox = new JCheckBox("Ruler");
		checkBox.setSelected(view.isRulerOn());
		checkBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				view.setRulerOn(src.isSelected());
				view.repaint();
			}
		});
		p.add(checkBox);
		count++;

		MiscUtil.makeCompactGrid(p, count, 1, 5, 5, 10, 2);

		pack();
		setLocationRelativeTo(view);

	}

}
