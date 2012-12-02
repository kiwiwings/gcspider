package de.kiwiwings.gccom.ListingParser.postfix;

import java.net.URLDecoder;

import de.kiwiwings.gccom.ListingParser.json.ParseParams.ParseProc;

public class FindAndDecode extends Find implements ParseProc {
	public FindAndDecode(String pattern) {
		super(pattern);
	}
	
	@Override
	public String process(Object input) throws Exception {
		String result = super.process(input);
		return URLDecoder.decode(result, "UTF8");
	}

}
