package de.kiwiwings.gccom.ListingParser.plugin.hides;

import java.util.HashMap;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class HidesInitPlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		ctx.setDatatable("My Hides");
		ctx.setPrimaryKey("hides_logid");
		ctx.getDatabase().clear();
		ctx.getChangedIdx().clear();
		ctx.getPkIdx().clear();
		ctx.getSchema().clear();
		ctx.setHidesTodos(new HashMap<String,Map<String,String>>());
	}
}
