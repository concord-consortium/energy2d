/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class AppletConverter {

	private final static String LINE_SEPARATOR = System.getProperty("line.separator");

	private System2D s2d;

	AppletConverter(System2D s2d) {
		this.s2d = s2d;
	}

	void write(final File file) {

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (out == null)
			return;

		String s = "<html>";
		s += LINE_SEPARATOR;

		s += "  <head>";
		s += LINE_SEPARATOR;
		s += "    <title>" + System2D.BRAND_NAME + "</title>";
		s += LINE_SEPARATOR;
		s += "    <script type=\"text/javascript\" src=\"library.js\"></script>";
		s += LINE_SEPARATOR;
		s += "  </head>";
		s += LINE_SEPARATOR;

		s += "  <body>";
		s += LINE_SEPARATOR;

		s += "    <p><font color=\"red\">If nothing shows up below, try the following: ";
		s += "<ol><li>Download <a href=\"http://energy.concord.org/energy2d/energy2d-applet.jar\">energy2d-applet.jar</a> ";
		s += "and <a href=\"http://energy.concord.org/energy2d/library.js\">library.js</a> ";
		s += "to where this HTML file is located; ";
		s += "<li>Make sure " + s2d.getCurrentFile() + " is copied or moved to where this HTML file is located; ";
		s += "<li><b>Restart the browser</b> and reload this page.";
		s += "</ol><p>This line of message should be removed if the applet works.</font></p>";
		s += LINE_SEPARATOR;

		s += "    <center>";
		s += LINE_SEPARATOR;

		s += "      <applet id=\"applet1\" code=\"org.concord.energy2d.system.System2D\" archive=\"energy2d-applet.jar\" width=\"500\" height=\"500\">";
		s += LINE_SEPARATOR;
		s += "        <param name=\"script\" value=\"load " + MiscUtil.getFileName(s2d.getCurrentFile().toString()) + "\"/>";
		s += LINE_SEPARATOR;
		s += "      </applet>";
		s += LINE_SEPARATOR;

		s += "      <br>";
		s += LINE_SEPARATOR;
		s += "      <form autocomplete=\"off\">";
		s += LINE_SEPARATOR;
		s += "      <input type=\"button\" value=\"Run\" onclick=\"run('applet1')\">";
		s += LINE_SEPARATOR;
		s += "      <input type=\"button\" value=\"Stop\" onclick=\"stop('applet1')\">";
		s += LINE_SEPARATOR;
		s += "      <input type=\"button\" value=\"Reset\" onclick=\"jsReset('applet1')\">";
		s += LINE_SEPARATOR;
		s += "      <input type=\"button\" value=\"Reload\" onclick=\"jsReload('applet1')\">";
		s += LINE_SEPARATOR;
		s += "      </form>";
		s += LINE_SEPARATOR;

		s += "      <br><br>";
		s += LINE_SEPARATOR;

		s += "      <p><b>System requirements:</b> You must have Java Version 5 or higher. <a href=\"http://java.com\">Download Java now</a>.";
		s += LINE_SEPARATOR;

		s += "    </center>";
		s += LINE_SEPARATOR;

		s += "  </body>";
		s += LINE_SEPARATOR;

		s += "</html>";

		try {
			out.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
		}

	}

}
