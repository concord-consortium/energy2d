/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import javax.swing.ImageIcon;

/**
 * @author Charles Xie
 * 
 */
public class Picture {

	private ImageIcon image;
	private float x, y;

	public Picture(ImageIcon image, float x, float y) {
		setImage(image);
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

	public void setImage(ImageIcon image) {
		this.image = image;
	}

	public ImageIcon getImage() {
		return image;
	}

}
