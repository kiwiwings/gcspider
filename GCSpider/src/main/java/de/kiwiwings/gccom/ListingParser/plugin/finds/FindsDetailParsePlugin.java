package de.kiwiwings.gccom.ListingParser.plugin.finds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.CacheAttribute;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.parser.CommonParser;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;


public class FindsDetailParsePlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		CommonParser parser = ctx.getParser();
		if (!parser.hasDocument()) return;
		
		Map<String,String> entry = new HashMap<String,String>();

		List<CacheAttribute> cacheAttr = ctx.getCacheAttr();

		Map<String,String> attributes = new HashMap<String,String>();
		String values[] = parser.selectConfigStringList(ctx, "parse.detail.attributes");
		for (String v : values) {
			String pair[] = v.split("-");
			assert(pair != null && pair.length == 2);
			String name = pair[0];
			String value;
			if ("yes".equals(pair[1])) {
				value = "1";
			} else if ("no".equals(pair[1])) {
				value = "0";
			} else {
				continue;
			}
			
			for (CacheAttribute ca : cacheAttr) {
				if (name.equals(ca.getImageSuffix()) || ca.getColumn().endsWith(name)) {
					attributes.put(ca.getColumn(), value);
				}
			}
		}
		
		for (String column : ctx.getSchema().keySet()) {
			if (column.startsWith("attribute_")) {
				entry.put(column, attributes.get(column));
				continue;
			}
			
			String value = parser.selectConfigString(ctx, "parse.detail."+column); 
			if (value == null || "".equals(value)) continue;
			entry.put(column, value);
		}
		ctx.putDatabaseEntry(entry);
	}
}
