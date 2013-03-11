/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import javax.swing.JDialog;

import org.concord.energy2d.model.Anemometer;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.HeatFluxSensor;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Tree;

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

	JDialog createModelDialog(Object o) {
		if (o instanceof Model2D)
			return new ModelDialog(view, (Model2D) o, modal);
		if (o instanceof Part)
			return new PartModelDialog(view, (Part) o, modal);
		if (o instanceof Cloud)
			return new CloudDialog(view, (Cloud) o, modal);
		if (o instanceof Tree)
			return new TreeDialog(view, (Tree) o, modal);
		if (o instanceof Thermometer)
			return new ThermometerDialog(view, (Thermometer) o, modal);
		if (o instanceof Anemometer)
			return new AnemometerDialog(view, (Anemometer) o, modal);
		if (o instanceof HeatFluxSensor)
			return new HeatFluxSensorDialog(view, (HeatFluxSensor) o, modal);
		if (o instanceof TextBox)
			return new TextBoxPanel((TextBox) o, view).createDialog(modal);
		return null;
	}

	JDialog createViewDialog(Object o) {
		if (o instanceof View2D)
			return new ViewDialog(view, modal);
		if (o instanceof Part)
			return new PartViewDialog(view, (Part) o, modal);
		if (o instanceof Cloud)
			return new CloudDialog(view, (Cloud) o, modal);
		if (o instanceof Tree)
			return new TreeDialog(view, (Tree) o, modal);
		if (o instanceof Thermometer)
			return new ThermometerDialog(view, (Thermometer) o, modal);
		if (o instanceof Anemometer)
			return new AnemometerDialog(view, (Anemometer) o, modal);
		if (o instanceof HeatFluxSensor)
			return new HeatFluxSensorDialog(view, (HeatFluxSensor) o, modal);
		if (o instanceof TextBox)
			return new TextBoxPanel((TextBox) o, view).createDialog(modal);
		return null;
	}

}
