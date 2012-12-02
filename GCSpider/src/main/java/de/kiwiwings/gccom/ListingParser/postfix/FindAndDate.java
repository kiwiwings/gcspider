package de.kiwiwings.gccom.ListingParser.postfix;

import de.kiwiwings.gccom.ListingParser.json.ParseParams.ParseProc;

public class FindAndDate extends Find implements ParseProc {
	public FindAndDate(String pattern) {
		super(pattern);
	}
	
	@Override
	public String process(Object input) throws Exception {
		String result = super.process(input);
		return new CalcDate().process(result);
	}

}
