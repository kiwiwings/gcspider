package de.kiwiwings.gccom.ListingParser.parser;

import org.w3c.dom.Element;

public interface CommonElement {
	String getAttribute(String name);
	String getText();
	Element getTree() throws Exception;
}
