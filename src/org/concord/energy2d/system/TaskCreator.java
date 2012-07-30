package org.concord.energy2d.system;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * @author Charles Xie
 * 
 */
class TaskCreator {

	private final static byte NAME_OK = 0;
	private final static byte NAME_ERROR = 1;
	private final static byte NAME_EXISTS = 2;

	private JPanel contentPane;
	private JTextArea scriptArea;
	private JTextField nameField, descriptionField;
	private JTextField intervalField, lifetimeField;
	private JCheckBox permanentCheckBox;
	private JLabel lifetimeLabel;
	private JSpinner prioritySpinner;
	private JDialog dialog;
	private TaskManager taskManager;
	private Task task;
	private JTable table;
	private int row;
	private Window owner;

	TaskCreator(TaskManager t, Window w) {

		taskManager = t;
		owner = w;

		contentPane = new JPanel(new BorderLayout(5, 5));

		scriptArea = new JTextArea();
		scriptArea.setBorder(BorderFactory.createTitledBorder("Scripts:"));
		JScrollPane scroller = new JScrollPane(scriptArea);
		scroller.setPreferredSize(new Dimension(600, 400));
		contentPane.add(scroller, BorderLayout.CENTER);

		JPanel topPanel = new JPanel(new BorderLayout());
		contentPane.add(topPanel, BorderLayout.NORTH);

		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		topPanel.add(p1, BorderLayout.CENTER);

		p1.add(new JLabel("Name: "));
		nameField = new JTextField("Untitled");
		nameField.setColumns(10);
		p1.add(nameField);

		p1.add(new JLabel("Description: "));
		descriptionField = new JTextField();
		descriptionField.setColumns(50);
		p1.add(descriptionField);

		final JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		topPanel.add(p2, BorderLayout.SOUTH);

		p2.add(new JLabel("Priority: "));
		prioritySpinner = new JSpinner(new SpinnerNumberModel(Thread.NORM_PRIORITY, 1, 5, 1));
		p2.add(prioritySpinner);

		p2.add(new JLabel("Interval: "));
		intervalField = new JTextField();
		p2.add(intervalField);

		permanentCheckBox = new JCheckBox("Permanent");
		permanentCheckBox.setSelected(true);
		permanentCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					p2.remove(lifetimeLabel);
					p2.remove(lifetimeField);
				} else {
					p2.add(lifetimeLabel);
					p2.add(lifetimeField);
					lifetimeField.setText("" + 100000);
				}
				p2.validate();
				p2.repaint();
			}
		});
		p2.add(permanentCheckBox);

		lifetimeLabel = new JLabel("Lifetime: ");
		lifetimeField = new JTextField();

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (ok()) {
				case NAME_OK:
					taskManager.notifyChange();
					dialog.dispose();
					break;
				case NAME_EXISTS:
					JOptionPane.showMessageDialog(dialog, "A task with the name \"" + nameField.getText() + "\" already exists.", "Duplicate Task Name", JOptionPane.ERROR_MESSAGE);
					break;
				case NAME_ERROR:
					JOptionPane.showMessageDialog(dialog, "A task name must contain at least four characters in [a-zA-Z_0-9] (no space allowed): \"" + nameField.getText() + "\".", "Task Name Error", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		});
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

	}

	private byte ok() {

		String name = nameField.getText();
		if (!name.matches("\\w{4,}")) {
			return NAME_ERROR;
		}
		if (task == null) {
			if (taskManager.getTaskByUid(name) != null)
				return NAME_EXISTS;
			Task t = new Task((int) parse(intervalField.getText())) {
				public void execute() {
					taskManager.runScript(getScript());
					if (taskManager.getIndexOfStep() >= getLifetime()) {
						setCompleted(true);
					}
				}
			};
			t.setSystemTask(false);
			t.setPriority((Integer) prioritySpinner.getValue());
			t.setLifetime(permanentCheckBox.isSelected() ? Task.PERMANENT : (int) parse(lifetimeField.getText()));
			t.setUid(name);
			t.setDescription(descriptionField.getText());
			t.setScript(scriptArea.getText());
			taskManager.add(t);
			taskManager.processPendingRequests();
		} else {
			task.setPriority((Integer) prioritySpinner.getValue());
			task.setInterval((int) parse(intervalField.getText()));
			task.setLifetime(permanentCheckBox.isSelected() ? Task.PERMANENT : (int) parse(lifetimeField.getText()));
			task.setDescription(descriptionField.getText());
			task.setScript(scriptArea.getText());
			if (!task.getUid().equals(name)) {
				task.setUid(name);
				table.setValueAt(name, row, 2);
			}
		}
		return NAME_OK;

	}

	void show(JTable table, Task l, int row) {
		this.table = table;
		this.row = row;
		if (dialog == null) {
			dialog = new JDialog(JOptionPane.getFrameForComponent(table), "Creating a Task", true);
			dialog.setContentPane(contentPane);
			dialog.pack();
			dialog.setLocationRelativeTo(table);
		}
		if (l != null) {
			dialog.setTitle("Edit a Task");
			nameField.setText(l.getUid());
			descriptionField.setText(l.getDescription());
			scriptArea.setText(l.getScript());
			scriptArea.setCaretPosition(0);
			prioritySpinner.setValue(l.getPriority());
			permanentCheckBox.setSelected(l.getLifetime() == Task.PERMANENT);
			lifetimeField.setText(Integer.toString(l.getLifetime()));
			intervalField.setText(Integer.toString(l.getInterval()));
		} else {
			dialog.setTitle("Create a Task");
			nameField.setText("Untitled");
			descriptionField.setText(null);
			scriptArea.setText(null);
			prioritySpinner.setValue(Thread.NORM_PRIORITY);
			lifetimeField.setText("" + Task.PERMANENT);
			intervalField.setText("" + 10);
			permanentCheckBox.setSelected(true);
		}
		task = l;
		dialog.setVisible(true);
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
