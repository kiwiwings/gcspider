package de.kiwiwings.gccom.ListingParser.plugin;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public interface SpiderPlugin {
	void execute(SpiderContext ctx) throws Exception;
}
