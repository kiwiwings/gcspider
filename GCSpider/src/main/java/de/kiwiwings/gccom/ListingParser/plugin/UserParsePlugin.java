package de.kiwiwings.gccom.ListingParser.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.parser.CommonParser;


public class UserParsePlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		CommonParser parser = ctx.getParser();
		if (!parser.hasDocument()) return;
		
		final String key = "owner_guid";
		
		Map<String,String> entryTemplate = new HashMap<String,String>();
		for (String column : ctx.getSchema().keySet()) {
			String value = parser.selectConfigString(ctx, "parse.user."+column);
			if (value == null || "".equals(value)) continue;
			entryTemplate.put(column, value);
		}

		String pk = entryTemplate.get(key);
		if (pk == null) return;

		List<Map<String,String>> todoList = ctx.getUserMappingTodos();
		if (todoList == null) return;
		
		Iterator<Map<String,String>> iter = todoList.iterator();
		while (iter.hasNext()) {
			Map<String,String> entry = iter.next();
			if (pk.equals(entry.get(key))) {
				entry.putAll(entryTemplate);
				ctx.putDatabaseEntry(entry);
				iter.remove();
			}
		}
	}
}
