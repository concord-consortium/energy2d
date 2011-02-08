/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.system;

import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * @author Charles Xie
 * 
 */
public class Helper {

	private static ScriptDialog scriptDialog;

	private Helper() {
	}

	public final static void showScriptDialog(System2D box) {
		if (scriptDialog != null && scriptDialog.isShowing()) {
			scriptDialog.toFront();
		} else {
			scriptDialog = new ScriptDialog(box);
			scriptDialog.pack();
			scriptDialog.setLocationRelativeTo(box);
			scriptDialog.setVisible(true);
		}
	}

	public final static void showKeyboardShortcuts(Frame frame) {
		String s = "<html><h2>Keyboard Shortcuts</h2><hr>";
		s += "<br><font face=Courier>'R'</font> &mdash; Run or pause the simulation.";
		s += "<br><font face=Courier>'T'</font> &mdash; Reset the simulation.";
		s += "<br><font face=Courier>'L'</font> &mdash; Reload the initial configurations.";
		s += "<br><font face=Courier>'G'</font> &mdash; Show or hide the graph.";
		s += "<br><font face=Courier>'S'</font> &mdash; Turn sunlight on or off.";
		s += "<br><font face=Courier>'Q'</font> &mdash; When sunlight is present, increase the sun angle (towards west).";
		s += "<br><font face=Courier>'W'</font> &mdash; When sunlight is present, decrease the sun angle (towards east).";
		s += "</html>";
		JOptionPane.showMessageDialog(frame, new JLabel(s));
	}

	public final static void showAbout(Frame frame) {
		String s = "<html><h2>Energy2D</h2>";
		s += "<h4><i>Interactive simulation of heat and mass flow</i></h4>";
		s += "http://energy.concord.org/energy2d/index.html<hr>";
		s += "<h4>Credit:</h4>This program is brought to you by Dr. Charles Xie (qxie@concord.org).<br>";
		s += "Funding of this project is provided by the National Science Foundation<br>";
		s += "under grant #0918449 to the Concord Consortium.";
		s += "<h4>License:</h4>GNU Lesser General Public License V3.0";
		s += "</html>";
		JOptionPane.showMessageDialog(frame, new JLabel(s));
	}

}
