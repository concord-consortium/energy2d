/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.analytics.simulation2d.view;

import javax.swing.JDialog;

import org.concord.analytics.simulation2d.model.Model2D;
import org.concord.analytics.simulation2d.model.Part;

/**
 * @author Charles Xie
 * 
 */
final class DialogFactory {

	private View2D view;
	private boolean modal = true;

	DialogFactory(View2D view) {
		this.view = view;
	}

	void setModal(boolean modal) {
		this.modal = modal;
	}

	JDialog createDialog(Object o) {
		if (o instanceof Model2D)
			return new ModelDialog(view, (Model2D) o, modal);
		if (o instanceof Part)
			return new PartDialog(view, (Part) o, modal);
		return null;
	}

}
