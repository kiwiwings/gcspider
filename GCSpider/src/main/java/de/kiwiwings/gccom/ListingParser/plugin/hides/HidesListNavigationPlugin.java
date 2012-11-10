package de.kiwiwings.gccom.ListingParser.plugin.hides;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.SpiderContext.NavigationState;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class HidesListNavigationPlugin implements SpiderPlugin {
	private int currentListPage = -1;

	public void execute(SpiderContext ctx) throws Exception {
		switch (currentListPage) {
		case -1:
			initFirstOverview(ctx);
			break;
		default:
			currentListPage++;
			getOverviewPage(ctx, "ctl00$ContentBody$pgrBottom$lbGoToPage_"+currentListPage);
		}

		int pageCount = ctx.getPageCount();
		if (currentListPage == pageCount) {
			ctx.setNavigationState(NavigationState.last);
		} else if (currentListPage < pageCount) {
			ctx.setNavigationState(NavigationState.next);
		} else {
			ctx.setNavigationState(NavigationState.error);
		}
	}
	
	private void initFirstOverview(SpiderContext ctx) throws Exception {
		// go to first page
		HttpRequestBase httpCall = new HttpGet(ctx.getConfig().getHidesListUrl());
		ctx.updateFormParams(httpCall);
		currentListPage = 1;
	}

	private void getOverviewPage(SpiderContext ctx, String eventTarget)
	throws Exception {
		ctx.removeOtherFormParam(new String[]{"__VIEWSTATE","__VIEWSTATEFIELDCOUNT","__VIEWSTATE1"});
		ctx.replaceFormParam("__EVENTTARGET", eventTarget);
		ctx.replaceFormParam("__EVENTARGUMENT", "");
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(ctx.getFormParams(), "UTF-8");
		HttpRequestBase httpCall = new HttpPost(ctx.getConfig().getHidesListUrl());
		((HttpPost)httpCall).setEntity(entity);
		ctx.updateFormParams(httpCall);
	}
}
