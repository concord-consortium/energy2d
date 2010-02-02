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
	private int x = 50, y = 50;

	public Picture(ImageIcon image, int x, int y) {
		setImage(image);
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

	public void setImage(ImageIcon image) {
		this.image = image;
	}

	public ImageIcon getImage() {
		return image;
	}

}
