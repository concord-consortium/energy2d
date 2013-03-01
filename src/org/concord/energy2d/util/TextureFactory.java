/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * @author Charles Xie
 * 
 */

public final class TextureFactory {

	private final static BasicStroke ultrathin = new BasicStroke(.5f);
	private final static BasicStroke thin = new BasicStroke(1);

	public final static byte SMALL = 101;
	public final static byte MEDIUM = 102;
	public final static byte LARGE = 103;
	public final static byte HUGE = 104;

	public final static byte POLKA = 1;
	public final static byte MOSIAC = 2;
	public final static byte POSITIVE = 3;
	public final static byte NEGATIVE = 4;
	public final static byte STARRY = 5;
	public final static byte CIRCULAR = 6;
	public final static byte HORIZONTAL_STRIPE = 7;
	public final static byte VERTICAL_STRIPE = 8;
	public final static byte DIAGONAL_UP_STRIPE = 9;
	public final static byte DIAGONAL_DOWN_STRIPE = 10;
	public final static byte GRID = 11;
	public final static byte HORIZONTAL_BRICK = 12;
	public final static byte DENSITY50 = 13;
	public final static byte DENSITY25 = 14;
	public final static byte DENSITY5 = 15;
	public final static byte DENSITY95 = 16;
	public final static byte CIRCLE_CONTACT = 17;
	public final static byte CIRCLE_SEPARATE = 18;
	public final static byte HORIZONTAL_LATTICE = 19;
	public final static byte TRIANGLE_HALF = 20;
	public final static byte DICE = 21;
	public final static byte DIAGONAL_CROSS = 22;

	final static ArrayList<TextureCode> textureList = new ArrayList<TextureCode>();

	static {
		textureList.add(new TextureCode(POLKA, SMALL));
		textureList.add(new TextureCode(POLKA, MEDIUM));
		textureList.add(new TextureCode(MOSIAC, SMALL));
		textureList.add(new TextureCode(MOSIAC, MEDIUM));
		textureList.add(new TextureCode(POSITIVE, MEDIUM));
		textureList.add(new TextureCode(NEGATIVE, MEDIUM));
		textureList.add(new TextureCode(STARRY, LARGE));
		textureList.add(new TextureCode(CIRCULAR, LARGE));
		textureList.add(new TextureCode(HORIZONTAL_STRIPE, SMALL));
		textureList.add(new TextureCode(HORIZONTAL_STRIPE, MEDIUM));
		textureList.add(new TextureCode(VERTICAL_STRIPE, SMALL));
		textureList.add(new TextureCode(VERTICAL_STRIPE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_UP_STRIPE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_UP_STRIPE, LARGE));
		textureList.add(new TextureCode(DIAGONAL_DOWN_STRIPE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_DOWN_STRIPE, LARGE));
		textureList.add(new TextureCode(GRID, SMALL));
		textureList.add(new TextureCode(GRID, MEDIUM));
		textureList.add(new TextureCode(HORIZONTAL_BRICK, MEDIUM));
		textureList.add(new TextureCode(HORIZONTAL_BRICK, LARGE));
		textureList.add(new TextureCode(DENSITY50, SMALL));
		textureList.add(new TextureCode(DENSITY25, SMALL));
		textureList.add(new TextureCode(DENSITY5, SMALL));
		textureList.add(new TextureCode(DENSITY95, SMALL));
		textureList.add(new TextureCode(DENSITY5, LARGE));
		textureList.add(new TextureCode(DENSITY95, LARGE));
		textureList.add(new TextureCode(CIRCLE_CONTACT, MEDIUM));
		textureList.add(new TextureCode(CIRCLE_CONTACT, LARGE));
		textureList.add(new TextureCode(CIRCLE_SEPARATE, MEDIUM));
		textureList.add(new TextureCode(CIRCLE_SEPARATE, LARGE));
		textureList.add(new TextureCode(HORIZONTAL_LATTICE, MEDIUM));
		textureList.add(new TextureCode(HORIZONTAL_LATTICE, LARGE));
		textureList.add(new TextureCode(DICE, MEDIUM));
		textureList.add(new TextureCode(DIAGONAL_CROSS, LARGE));
		textureList.add(new TextureCode(DIAGONAL_CROSS, MEDIUM));
		textureList.add(new TextureCode(TRIANGLE_HALF, MEDIUM));
	}

	private static Rectangle r = new Rectangle();

	public static TexturePaint createPattern(int type, int w, int h, Color c1, Color c2) {

		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setStroke(thin);
		switch (type) {
		case POLKA:
			int x = w / 4;
			int y = h / 4;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.fillOval(x, y, x + x, y + y);
			r.setBounds(x, y, w, h);
			return new TexturePaint(bi, r);
		case MOSIAC:
			x = w / 2;
			y = h / 2;
			g.setColor(c1);
			g.fillRect(0, 0, x, y);
			g.fillRect(x, y, x, y);
			g.setColor(c2);
			g.fillRect(x, 0, x, y);
			g.fillRect(0, y, x, y);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case POSITIVE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			x = w / 2 + 2;
			y = h / 2 + 2;
			g.drawLine(1, y, 3, y);
			g.drawLine(2, y - 1, 2, y + 1);
			g.drawLine(x, 1, x, 3);
			g.drawLine(x - 1, 2, x + 1, 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case NEGATIVE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			x = w / 2 + 2;
			y = h / 2 + 2;
			g.drawLine(1, y, 3, y);
			g.drawLine(x - 1, 2, x + 1, 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case CIRCULAR:
			for (int i = 0; i < w / 2; i++) {
				for (int j = 0; j < h / 2; j++) {
					g.setColor(c1);
					g.fillRect(2 * i, 2 * j, 1, 1);
					g.fillRect(2 * i + 1, 2 * j + 1, 1, 1);
					g.setColor(c2);
					g.fillRect(2 * i + 1, 2 * j, 1, 1);
					g.fillRect(2 * i, 2 * j + 1, 1, 1);
				}
			}
			g.setColor(c2);
			g.drawLine(1, h / 2, 3, h / 2);
			g.drawLine(2, h / 2 - 1, 2, h / 2 + 1);
			g.drawLine(w / 2, 1, w / 2, 3);
			g.drawLine(w / 2 - 1, 2, w / 2 + 1, 2);
			r.setBounds(0, 0, w - 2, h - 2);
			return new TexturePaint(bi, r);
		case DENSITY50:
			g.setColor(c1);
			g.fillRect(0, 0, w, h);
			for (int i = 0; i < w / 2; i++) {
				for (int j = 0; j < h / 2; j++) {
					g.setColor(c2);
					g.fillRect(2 * i + 1, 2 * j, 1, 1);
					g.fillRect(2 * i, 2 * j + 1, 1, 1);
				}
			}
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DENSITY25:
			g.setColor(c1);
			g.fillRect(0, 0, w, h);
			for (int i = 0; i < w / 2; i++) {
				for (int j = 0; j < h / 2; j++) {
					g.setColor(c2);
					g.fillRect(2 * i, 2 * j, 1, 1);
				}
			}
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DENSITY5:
			x = w / 2 + 1;
			y = h / 2 + 1;
			g.setColor(c1);
			g.fillRect(0, 0, w, h);
			g.setColor(c2);
			g.fillRect(1, y, 1, 1);
			g.fillRect(x, 1, 1, 1);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DENSITY95:
			x = w / 2 + 1;
			y = h / 2 + 1;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.fillRect(1, y, 1, 1);
			g.fillRect(x, 1, 1, 1);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case STARRY:
			g.setColor(c1);
			g.fillRect(0, 0, w, h);
			for (int i = 0; i < w / 2; i++) {
				for (int j = 0; j < h / 2; j++) {
					g.setColor(c2);
					g.fillRect(2 * i + 1, 2 * j, 1, 1);
					g.fillRect(2 * i, 2 * j + 1, 1, 1);
				}
			}
			g.setColor(c2);
			x = w / 2 + 2;
			y = h / 2 + 2;
			g.drawLine(1, y, 3, y);
			g.drawLine(2, y - 1, 2, y + 1);
			g.drawLine(x, 1, x, 3);
			g.drawLine(x - 1, 2, x + 1, 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case HORIZONTAL_STRIPE:
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, y, w, y);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case VERTICAL_STRIPE:
			x = w / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(x, 0, x, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DIAGONAL_UP_STRIPE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.setStroke(ultrathin);
			g.drawLine(0, h - 1, w - 1, 0);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DIAGONAL_DOWN_STRIPE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.setStroke(ultrathin);
			g.drawLine(0, 0, w, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case GRID:
			x = w / 2;
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, h / 2, w, h / 2);
			g.drawLine(w / 2, 0, w / 2, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case HORIZONTAL_BRICK:
			x = w / 2;
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, 0, w, 0);
			g.drawLine(0, y, w, y);
			g.drawLine(0, 0, 0, y);
			g.drawLine(x, y, x, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case CIRCLE_CONTACT:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawOval(0, 0, w, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case CIRCLE_SEPARATE:
			x = w / 4;
			y = h / 4;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawOval(0, 0, w, h);
			g.drawOval(x, y, x + x < w / 2 ? x + x + 2 : w / 2, y + y < h / 2 ? y + y + 2 : h / 2);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case HORIZONTAL_LATTICE:
			x = w / 2;
			y = h / 2;
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawOval(0, 0, x, y);
			g.drawLine(x, y / 2, w, y / 2);
			g.drawLine(x / 2, y, x / 2, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DICE:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawRect(0, 0, w, h);
			g.fillOval(w / 2 - 2, h / 2 - 2, 5, 5);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case TRIANGLE_HALF:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			Polygon triangle = new Polygon();
			triangle.addPoint(0, 0);
			triangle.addPoint(w, 0);
			triangle.addPoint(0, h);
			g.fillPolygon(triangle);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		case DIAGONAL_CROSS:
			g.setColor(c2);
			g.fillRect(0, 0, w, h);
			g.setColor(c1);
			g.drawLine(0, 0, w, h);
			g.drawLine(w, 0, 0, h);
			r.setBounds(0, 0, w, h);
			return new TexturePaint(bi, r);
		}
		return null;
	}

}
