/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

/**
 * @author Charles Xie
 * 
 */

public abstract class ComboBoxRenderer {

	public static class IconRenderer extends JRadioButton implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setBackground(isSelected ? SystemColor.textHighlight : Color.white);
			setForeground(isSelected ? SystemColor.textHighlightText : Color.black);
			setHorizontalAlignment(CENTER);
			if (value instanceof Icon) {
				setIcon((Icon) value);
				if (value instanceof ImageIcon) {
					setToolTipText(((ImageIcon) value).getDescription());
				}
			} else {
				setText(value == null ? "Unknown" : value.toString());
			}
			return this;
		}

	}

	public static class ColorCell extends ColorRectangle implements ListCellRenderer {

		public ColorCell() {
			super();
		}

		public ColorCell(Color moreColor) {
			this();
			if (!isDefaultColor(moreColor))
				setMoreColor(moreColor);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setBackground(isSelected ? SystemColor.textHighlight : Color.white);
			setForeground(isSelected ? SystemColor.textHighlightText : Color.black);
			setColorID((Integer) value);
			return this;
		}

	}

}