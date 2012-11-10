package de.kiwiwings.gccom.ListingParser.gpx;

import java.text.ParseException;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public abstract class TemplateToken {
	protected String tokenStr;
	protected TemplateToken(String tokenStr) {
		this.tokenStr = tokenStr;
	}
	public abstract String print(SpiderContext ctx, Map<String,String> entry) throws ParseException;
}
