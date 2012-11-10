package de.kiwiwings.gccom.ListingParser.gpx;

import java.util.Map;

import de.kiwiwings.gccom.ListingParser.CacheAttribute;
import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class AttributeToken extends TemplateToken {
	public AttributeToken(String tokenStr) {
		super(tokenStr);
	}
	
	public String print(SpiderContext ctx, Map<String,String> entry) {
		StringBuffer attbuf = new StringBuffer();
		for (CacheAttribute ad : ctx.getCacheAttr()) {
			String state = entry.get(ad.getColumn());
			if (state == null || "".equals(state)) continue;
			String attStr = tokenStr;
			attStr = attStr.replaceFirst("\\$id\\$", Integer.toString(ad.getIndex()));
			attStr = attStr.replaceFirst("\\$state\\$", ("true".equalsIgnoreCase(state) ? "1" : "0"));
			attStr = attStr.replaceFirst("\\$text\\$", ad.getLabel());
			attbuf.append(attStr);
		}
		
		return attbuf.toString();
	}
}
