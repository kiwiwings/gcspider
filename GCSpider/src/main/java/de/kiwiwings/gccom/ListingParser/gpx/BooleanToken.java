package de.kiwiwings.gccom.ListingParser.gpx;

import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class BooleanToken extends TemplateToken {
	public BooleanToken(String tokenStr) {
		super(tokenStr);
	}
	
	public String print(SpiderContext ctx, Map<String,String> entry) {
		String input = entry.get(tokenStr);
		return ("true".equalsIgnoreCase(input) || "1".equals(input)) ? "True" : "False";
	}
}
