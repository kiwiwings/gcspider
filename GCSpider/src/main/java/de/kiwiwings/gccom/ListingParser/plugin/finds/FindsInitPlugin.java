package de.kiwiwings.gccom.ListingParser.plugin.finds;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class FindsInitPlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		ctx.setDatatable("My Finds");
		ctx.setPrimaryKey("waypoint");
		ctx.getDatabase().clear();
		ctx.getChangedIdx().clear();
		ctx.getPkIdx().clear();
		ctx.getSchema().clear();
	}
}
