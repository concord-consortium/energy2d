/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Color;
import java.awt.Font;

import org.concord.energy2d.util.XmlCharacterEncoder;

/**
 * @author Charles Xie
 * 
 */
public class TextBox {

	private float x, y;
	private String uid;
	private String str;
	private String face = "Arial";
	private int style = Font.PLAIN;
	private int size = 14;
	private Color color = Color.white;

	public TextBox() {
	}

	public TextBox(String str, float x, float y) {
		setString(str);
		setLocation(x, y);
	}

	public void set(TextBox t) {
		str = t.str;
		face = t.face;
		style = t.style;
		size = t.size;
		color = t.color;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
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

	public void setFace(String face) {
		this.face = face;
	}

	public String getFace() {
		return face;
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

	public void setFont(Font font) {
		face = font.getFamily();
		style = font.getStyle();
		size = font.getSize();
	}

	public Font getFont() {
		return new Font(face, style, size);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public String toXml() {
		String xml = "<text";
		if (uid != null)
			xml += " uid=\"" + uid + "\"";
		xml += " string=\"" + new XmlCharacterEncoder().encode(str) + "\"";
		xml += " face=\"" + face + "\"";
		xml += " size=\"" + size + "\"";
		xml += " style=\"" + style + "\"";
		xml += " color=\"" + Integer.toHexString(0x00ffffff & getColor().getRGB()) + "\"";
		xml += " x=\"" + getX() + "\"";
		xml += " y=\"" + getY() + "\"/>\n";
		return xml;
	}

}
