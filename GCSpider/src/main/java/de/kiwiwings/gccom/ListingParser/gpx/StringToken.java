package de.kiwiwings.gccom.ListingParser.gpx;

import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class StringToken extends TemplateToken {
	public StringToken(String tokenStr) {
		super(tokenStr);
	}
	public String print(SpiderContext ctx, Map<String,String> entry) {
		String val = entry.get(tokenStr);
		val = val.replaceAll("&", "&amp;");
//		val = val.replaceAll("\"", "&quot;");
		val = val.replaceAll("<", "&lt;");
		return val;
	}
}