package de.kiwiwings.gccom.ListingParser.plugin.hides;

import java.util.Iterator;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.SpiderContext.NavigationState;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class HidesDetailNavigationPlugin implements SpiderPlugin {
	Iterator<String> wpIter = null;
	
	public void execute(SpiderContext ctx) throws Exception {
		if (wpIter == null) {
			wpIter = ctx.getHidesTodos().keySet().iterator();
		}
		
		if (!wpIter.hasNext()) {
			ctx.setNavigationState(NavigationState.error);
			return;
		}
		
		String wpCode = wpIter.next();
		ctx.setNavigationState(wpIter.hasNext() ? NavigationState.next : NavigationState.last);
		
		HttpRequestBase httpCall = new HttpGet(ctx.getConfig().getDetailUrl()+wpCode);
		ctx.updateFormParams(httpCall);		
	}
}
