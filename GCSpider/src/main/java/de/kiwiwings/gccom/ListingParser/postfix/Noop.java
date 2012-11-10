package de.kiwiwings.gccom.ListingParser.postfix;

import de.kiwiwings.gccom.ListingParser.json.ParseParams.ParseProc;

public class Noop implements ParseProc {
	public void init(Object input) {}

	public String process(Object input) { 
		return input.toString(); 
	}
}
