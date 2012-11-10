package de.kiwiwings.gccom.ListingParser.parser;

public interface CommonParser {
	CommonElement[] selectElements(String select) throws Exception;
	
	CommonElement selectElement(CommonElement base, String select) throws Exception;
}
