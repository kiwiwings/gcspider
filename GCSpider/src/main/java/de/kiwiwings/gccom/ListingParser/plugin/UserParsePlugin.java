package de.kiwiwings.gccom.ListingParser.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;

import de.kiwiwings.gccom.ListingParser.SpiderContext;


public class UserParsePlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		Document doc = ctx.getPageContent();
		if (doc == null) return;
		
		final String key = "owner_guid";
		
		Map<String,String> entryTemplate = new HashMap<String,String>();
		for (String column : ctx.getSchema().keySet()) {
			String xpathStr = (String)ctx.getConfig().get("parse.user."+column);
			if (xpathStr == null || "".equals(xpathStr)) continue;
			
			XPathExpression xpe = ctx.getXpathExpression(xpathStr);
			String value = (String)xpe.evaluate(doc, XPathConstants.STRING);
			
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
