/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.analytics.event;

import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */
public class MeasurementEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public MeasurementEvent(Object source) {
		super(source);
	}

}
