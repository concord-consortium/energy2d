/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.analytics.event;

/**
 * @author Charles Xie
 * 
 */
public interface MeasurementListener {

	public void measurementTaken(MeasurementEvent e);

}
