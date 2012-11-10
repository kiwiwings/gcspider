package de.kiwiwings.gccom.ListingParser.plugin.finds;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;


public class FindsDetailParsePlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		Document doc = ctx.getPageContent();
		if (doc == null) return;
		
		SpiderConfig config = ctx.getConfig();
		Map<String,String> entry = new HashMap<String,String>();
		for (String column : ctx.getSchema().keySet()) {
			String xpathStr = (String)config.get("parse.detail."+column);
			if (xpathStr == null || "".equals(xpathStr)) continue;
			
			XPathExpression xpe = ctx.getXpathExpression(xpathStr);
			String value = (String)xpe.evaluate(doc, XPathConstants.STRING);
			
			entry.put(column, value);
		}
		ctx.putDatabaseEntry(entry);
	}
}
