package de.kiwiwings.gccom.ListingParser;

import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext.NavigationState;
import de.kiwiwings.gccom.ListingParser.plugin.*;
import de.kiwiwings.gccom.ListingParser.plugin.finds.*;
import de.kiwiwings.gccom.ListingParser.plugin.hides.*;

public class GCSpider {
	public static void main(String[] args) throws Exception {
		SpiderContext ctx = new SpiderContext();

		new InitContextPlugin().execute(ctx);
		ConfigPlugin configPlg = new ConfigPlugin();
		// configPlg.setParserProps("listingparser.jsoup.properties");
		configPlg.execute(ctx);
		new LoginPlugin().execute(ctx);

		doFinds(ctx);
		doHides(ctx);
	}

	private static void doFinds(SpiderContext ctx) throws Exception {
		new FindsInitPlugin().execute(ctx);
		new LoadDatabasePlugin().execute(ctx);
		try {
			navigate(ctx, new FindsListNavigationPlugin(), new SpiderPlugin[]{new FindsListParsePlugin()});
			navigate(ctx, new FindsDetailNavigationPlugin(), new SpiderPlugin[]{new FindsDetailParsePlugin(),new FindsDetailLogsPlugin()});
			navigate(ctx, new UserNavigationPlugin(), new SpiderPlugin[]{new UserParsePlugin()});
			new GCVotePlugin().execute(ctx);
			// invalidateAll(ctx);
		} finally {
			new SaveDatabasePlugin().execute(ctx);
		}
		new ExportGPXPlugin().execute(ctx);
	}
	
	private static void doHides(SpiderContext ctx) throws Exception {
		new HidesInitPlugin().execute(ctx);
		new LoadDatabasePlugin().execute(ctx);
		SpiderConfig config = ctx.getConfig();
		if (config.getUser().equals(config.getProperty("login.user"))) {
			try {
				navigate(ctx, new HidesListNavigationPlugin(), new SpiderPlugin[]{new HidesListParsePlugin()});
				navigate(ctx, new HidesDetailNavigationPlugin(), new SpiderPlugin[]{new HidesDetailLogsPlugin()});
			} finally {
				new SaveDatabasePlugin().execute(ctx);
			}
		}
//		new CreateHidesStatsPlugin().execute(ctx);
		new CreateHidesChartPlugin().execute(ctx);
	}
	
	@SuppressWarnings("unused")
	private static void invalidateAll(SpiderContext ctx) {
		for (Map<String,String> entry : ctx.getDatabase()) {
			ctx.getChangedIdx().add(ctx.getPrimaryKeyValue(entry));
		}
	}
	
	public static void navigate(SpiderContext ctx, SpiderPlugin navigatePlugin, SpiderPlugin childrenPlugins[]) throws Exception {
		NavigationState navState;
		do {
			navigatePlugin.execute(ctx);
			navState = ctx.getNavigationState();
			if (navState == NavigationState.next || navState == NavigationState.last) {
				for (int i=0; i<childrenPlugins.length; i++) {
					childrenPlugins[i].execute(ctx);
				}
			}
		} while (navState == NavigationState.next);
	}
}
