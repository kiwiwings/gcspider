package de.kiwiwings.gccom.ListingParser.gpx;

import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class StaticToken extends TemplateToken {
	public StaticToken(String tokenStr) {
		super(tokenStr);
	}
	public String print(SpiderContext ctx, Map<String,String> entry) {
		return tokenStr;
	}
}
