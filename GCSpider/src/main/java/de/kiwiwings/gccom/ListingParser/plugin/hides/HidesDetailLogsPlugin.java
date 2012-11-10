package de.kiwiwings.gccom.ListingParser.plugin.hides;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.SpiderContext.NavigationState;
import de.kiwiwings.gccom.ListingParser.json.Log;
import de.kiwiwings.gccom.ListingParser.plugin.finds.FindsDetailLogsPlugin;

public class HidesDetailLogsPlugin extends FindsDetailLogsPlugin {
	private String datehidden;
	private long lastAlreadyFoundLogEntryId;
	
	public HidesDetailLogsPlugin() throws Exception {
		super();
	}

	public void execute(SpiderContext ctx) throws Exception {
		lastAlreadyFoundLogEntryId = -1;
		super.execute(ctx);
	}
	
	protected void updateDetailData(SpiderContext ctx) throws Exception {
		super.updateDetailData(ctx);
		
		XPathExpression xpe = ctx.getXpathExpression((String)ctx.getConfig().get("parse.detail.hides_datehidden"));
		datehidden = (String)xpe.evaluate(ctx.getPageContent(), XPathConstants.STRING);
	}

	protected NavigationState processState(SpiderContext ctx, Log logEntry) {
		if (!"Found it".equals(logEntry.logType)) {
			return NavigationState.skip;
		} else if (logEntry.logId < lastAlreadyFoundLogEntryId) {
			return NavigationState.last;
		} else {
			return NavigationState.next;
		}
	}
	
	protected void putDatabaseEntry(SpiderContext ctx, Log logEntry)
	throws Exception {
		Map<String,String> cacheEntry = ctx.getHidesTodos().get(waypoint);
		String attrList[][] = {
			  { "hides_logid", logEntry.logId.toString() }
			, { "hides_waypoint", waypoint }
			, { "hides_name", cacheEntry.get("hides_name") }
			, { "hides_datehidden", datehidden }
			, { "hides_logtext", logEntry.logText }
			, { "hides_user_guid", logEntry.accountGuid }
			, { "hides_username", logEntry.userName }
			, { "hides_userid", logEntry.accountId.toString() }
			, { "hides_founddate", SpiderContext.fullDateFormat.format(logEntry.visited) }
			, { "hides_lastupdate", SpiderContext.fullDateFormat.format(new Date()) }
		};
		
		Map<String,String> entry = new HashMap<String,String>();
		for (String attr[] : attrList) {
			entry.put(attr[0], attr[1]);
		}
		
		if (ctx.putDatabaseEntry(entry)) {
			lastAlreadyFoundLogEntryId = Math.max(lastAlreadyFoundLogEntryId,logEntry.logId);
		}
	}
}
