/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.analytics.simulation2d.view;

import java.awt.Color;
import java.awt.Font;

/**
 * @author Charles Xie
 * 
 */
public class TextBox {

	private String text;
	private String name = "Arial";
	private int style = Font.PLAIN;
	private int size = 12;
	private Color color = Color.white;
	private int x = 50, y = 50;

	public TextBox(String text, int x, int y) {
		setText(text);
		setLocation(x, y);
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setLocation(int x, int y) {
		setX(x);
		setY(y);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

}
