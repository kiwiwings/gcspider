package de.kiwiwings.gccom.ListingParser.plugin.hides;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.finds.FindsListParsePlugin;

public class HidesListParsePlugin extends FindsListParsePlugin {
	Map<String,Date> lastFoundList = new HashMap<String,Date>();
	public void execute(SpiderContext ctx) throws Exception {
		// determine last saved found date 
		for (Map<String,String> entry : ctx.getDatabase()) {
			String wpcode = entry.get("hides_waypoint");
			String foundDateStr = entry.get("hides_founddate");
			if (wpcode == null || foundDateStr == null) continue;
			Date foundDate = SpiderContext.fullDateFormat.parse(foundDateStr);
			Date lastEntry = lastFoundList.get(wpcode);
			if (lastEntry == null || lastEntry.before(foundDate)) {
				lastFoundList.put(wpcode, foundDate);
			}
		}
		
		super.execute(ctx);
	}
	
	protected void putDatabaseEntry(SpiderContext ctx, Map<String,String> entry) throws Exception {
		String wpcode = entry.get("hides_waypoint");
		String lastFoundParsedStr = entry.get("hides_founddate");
		if (lastFoundParsedStr == null || "".equals(lastFoundParsedStr)) return;
		Date lastFoundParsed = SpiderContext.fullDateFormat.parse(lastFoundParsedStr);
		Date lastFoundDB = lastFoundList.get(wpcode);
		if (lastFoundDB == null || lastFoundParsed.after(lastFoundDB)) {
			ctx.getHidesTodos().put(wpcode, entry);
		}
	}

	protected String getRowSelEntry() {
		return "parse.list.hides_rowsel";
	}
}