/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.util;

import static org.concord.energy2d.util.EscapeCharacters.*;

/**
 * restores entity references to characters.
 * 
 * @author Charles Xie
 * 
 */

public class XmlCharacterDecoder {

	final static String LESS_THAN = "<";
	final static String GREATER_THAN = ">";
	final static String AMPERSAND = "&";
	final static String APOSTROPHE = "\'";
	final static String QUOTATION = "\"";

	public XmlCharacterDecoder() {
	}

	public String decode(String text) {

		if (text == null)
			return null;

		return text.replaceAll(LESS_THAN_ER, LESS_THAN).replaceAll(GREATER_THAN_ER, GREATER_THAN).replaceAll(AMPERSAND_ER, AMPERSAND).replaceAll(APOSTROPHE_ER, APOSTROPHE).replaceAll(QUOTATION_ER, QUOTATION);

	}

}
