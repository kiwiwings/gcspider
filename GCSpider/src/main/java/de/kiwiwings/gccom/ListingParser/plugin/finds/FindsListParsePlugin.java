package de.kiwiwings.gccom.ListingParser.plugin.finds;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;


public class FindsListParsePlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		Document doc = ctx.getPageContent(); 
		if (doc == null || ctx.getPageCount() == -1) return;
		
		XPathExpression rowSel = ctx.getXpathExpression(ctx.getConfig().getProperty("parse.list.rowsel"));
		NodeList ns = (NodeList)rowSel.evaluate(doc, XPathConstants.NODESET);
		
		int maxItem = ns.getLength();
		for (int i=0; i<maxItem; i++) {
			Node line = ns.item(i);
			readOneListRecord(ctx, line);
		}
	}
	
	protected void readOneListRecord(SpiderContext ctx, Node line) throws Exception {
		SpiderConfig config = ctx.getConfig();
		Map<String,String> entry = new HashMap<String,String>();
		for (String column : ctx.getSchema().keySet()) {
			String xpathStr = (String)config.get("parse.list."+column);
			if (xpathStr == null || "".equals(xpathStr)) continue;
			
			XPathExpression xpe = ctx.getXpathExpression(xpathStr);
			try {
				String value = (String)xpe.evaluate(line, XPathConstants.STRING);
				entry.put(column, value);
			} catch (XPathExpressionException e) {
				if (config.getUser().equals(config.get("login.user"))) {
					throw e;
				} else {
					// ignore
					e.printStackTrace();
				}
			}
		}
		putDatabaseEntry(ctx, entry);
	}
	
	protected void putDatabaseEntry(SpiderContext ctx, Map<String,String> entry) throws Exception {
		ctx.putDatabaseEntry(entry);
	}
}
