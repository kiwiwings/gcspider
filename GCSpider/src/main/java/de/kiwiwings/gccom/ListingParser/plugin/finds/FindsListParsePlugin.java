package de.kiwiwings.gccom.ListingParser.plugin.finds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.parser.CommonElement;
import de.kiwiwings.gccom.ListingParser.parser.CommonParser;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;


public class FindsListParsePlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		CommonParser parser = ctx.getParser();
		if (!parser.hasDocument() || ctx.getPageCount() == -1) return;

		SpiderConfig config = ctx.getConfig();
		
		CommonElement clist[] = parser.selectElements(config.getProperty(getRowSelEntry()));
		
		List<Map<String,String>> dataSet = new ArrayList<Map<String,String>>();
		for (int i=0; i<clist.length; i++) {
			dataSet.add(new HashMap<String,String>());
		}
		
		for (String column : ctx.getSchema().keySet()) {
			String configEntry = (String)config.get("parse.list."+column);
			if (configEntry == null || "".equals(configEntry)) continue;

			for (int i=0; i<clist.length; i++) {
				String value = parser.selectConfigString(ctx, clist[i], "parse.list."+column);
				dataSet.get(i).put(column, value);
			}
		}
		
		for (Map<String,String> dataEntry : dataSet) {
			putDatabaseEntry(ctx, dataEntry);
		}
	}

	protected String getRowSelEntry() {
		return "parse.list.rowsel";
	}
	
	protected void putDatabaseEntry(SpiderContext ctx, Map<String,String> entry) throws Exception {
		ctx.putDatabaseEntry(entry);
	}
}
