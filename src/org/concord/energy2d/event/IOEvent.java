/*
 *   Copyright (C) 2011  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.event;

import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */
public class IOEvent extends EventObject {

	public final static byte FILE_INPUT = 0;
	public final static byte FILE_OUTPUT = 1;

	private static final long serialVersionUID = 1L;

	private byte type = FILE_INPUT;

	public IOEvent(byte type, Object source) {
		super(source);
		this.type = type;
	}

	public byte getType() {
		return type;
	}

}
