/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Color;
import java.awt.Font;

/**
 * @author Charles Xie
 * 
 */
public class TextBox {

	private String str;
	private String name = "Arial";
	private int style = Font.PLAIN | Font.BOLD;
	private int size = 14;
	private Color color = Color.white;
	private float x, y;

	public TextBox(String str, float x, float y) {
		setString(str);
		setLocation(x, y);
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	public void setString(String str) {
		this.str = str;
	}

	public String getString() {
		return str;
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

	public String toXml() {
		String xml = "<text";
		xml += " string=\"" + str + "\"";
		xml += " name=\"" + name + "\"";
		xml += " size=\"" + size + "\"";
		xml += " style=\"" + style + "\"";
		xml += " color=\"" + Integer.toHexString(0x00ffffff & getColor().getRGB()) + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>\n";
		return xml;
	}

}
