package de.kiwiwings.gccom.ListingParser.plugin.finds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.BitSet;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class FindsListNavigationPlugin implements SpiderPlugin {
	private BitSet todoList;
	private int currentListPage = -1;
	
	public void execute(SpiderContext ctx) throws Exception {
		// check for debugging
		if (ctx.getConfig().getDebugLastFetched()) {
			readFromDebugFile(ctx);
			int pageCount = ctx.getPageCount();
			ctx.setNavigationState((pageCount==-1)
				? SpiderContext.NavigationState.error
				: SpiderContext.NavigationState.last);
			return;
		}
		
		// check if list pages were configured
		SpiderConfig config = ctx.getConfig();
		String listPages = config.getListPages(); 
		if (listPages == null || "".equals(listPages)) {
			ctx.setNavigationState(SpiderContext.NavigationState.error);
			return;
		}
		
		int nextPage = getNextListPageIndex(ctx);
		switch (nextPage) {
		case -1:
			ctx.setNavigationState(SpiderContext.NavigationState.error);
			return;
		case 1:
			initFirstOverview(ctx);
			break;
		default:
			getNextPage(ctx, nextPage);
			break;
		}

		nextPage = getNextListPageIndex(ctx);
		ctx.setNavigationState(nextPage == -1
			? SpiderContext.NavigationState.last
			: SpiderContext.NavigationState.next );
	}
	
	protected static void readFromDebugFile(SpiderContext ctx) throws Exception {
		FileInputStream fis = new FileInputStream(ctx.getConfig().getResponseRawFile());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		byte buf[] = new byte[1024];
		for (int readBytes; (readBytes = fis.read(buf)) != -1; bos.write(buf, 0, readBytes));
		fis.close();
		
		// TODO: set page encoding and page index
		ctx.setPagestream(new ByteArrayInputStream(bos.toByteArray()));
		
		SpiderConfig config = ctx.getConfig();
		boolean debug = config.getDebug();
		try {
			config.setProperty("debug.active", "false");
			((SpiderPlugin)ctx.getParser()).execute(ctx);
		} finally {
			if (debug) config.setProperty("debug.active", "true");
		}
		
	}
	
	private void initFirstOverview(SpiderContext ctx) throws Exception {
		// go to first page
		HttpRequestBase httpCall = new HttpGet(ctx.getConfig().getListUrl());
		ctx.updateFormParams(httpCall);
		currentListPage = 1;
	}

	private void getNextPage(SpiderContext ctx, int nextPage) throws Exception {
		int pageCount = ctx.getPageCount();
		
		// need to compare 0-based, as e.g. cache10 is on same page as cache7,
		// but (10 mod 10) > (7 mod 10)
		int currentOverviewPage = currentListPage;
		
		if (currentOverviewPage == nextPage) {
			// do nothing ... actually an error :S
		} else if ((nextPage-1)/10 != (currentOverviewPage-1)/10) {
			// go to next 10 pages
			// previous: ctl00$ContentBody$pgrBottom$ctl05 page35 > previous = page21
			// next: ctl00$ContentBody$pgrBottom$ctl06     page15 > next = page21
			int nextListPage = (((currentOverviewPage-1)/10)+1)*10+1;
			System.out.println("Parsing page (+10): "+nextListPage+" of "+pageCount);
			getOverviewPage(ctx, "ctl00$ContentBody$pgrBottom$ctl06");
			currentListPage = nextListPage;
		} else {
			// go to page within the current 10 pages
			System.out.println("Parsing page: "+nextPage+" of "+pageCount);
			getOverviewPage(ctx, "ctl00$ContentBody$pgrBottom$lbGoToPage_"+nextPage);
			currentListPage = nextPage;
		}
	}
	
	private void getOverviewPage(SpiderContext ctx, String eventTarget)
	throws Exception {
		ctx.removeOtherFormParam(new String[]{"__VIEWSTATE","__VIEWSTATEFIELDCOUNT","__VIEWSTATE1"});
		ctx.replaceFormParam("__EVENTTARGET", eventTarget);
		ctx.replaceFormParam("__EVENTARGUMENT", "");
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(ctx.getFormParams(), "UTF-8");
		HttpRequestBase httpCall = new HttpPost(ctx.getConfig().getListUrl());
		((HttpPost)httpCall).setEntity(entity);
		ctx.updateFormParams(httpCall);
	}

	
	private int getNextListPageIndex(SpiderContext ctx) throws Exception {
		int currentPage = currentListPage; 
		if (currentPage == -1) {
			// always init first page
			return 1;
		}
		
		int pageCount = ctx.getPageCount();
		
		if (pageCount == -1) {
			return -1;
		}		

		if (todoList == null) {
			todoList = new BitSet();
			String testStr = ctx.getConfig().getListPages();
			String token[] = testStr.split(",");
			for (int i=0; i<token.length; i++) {
				String tok = token[i];
				if ("".equals(tok)) continue;
				if (tok.indexOf('-') > -1) {
					String rangeTok[] = tok.split("-");
					int start, stop;
					if (tok.startsWith("-")) {
						start = 1;
						stop = Integer.parseInt(rangeTok[1]);
					} else if (tok.endsWith("-")) {
						start = Integer.parseInt(rangeTok[0]);
						stop = pageCount;
					} else {
						start = Integer.parseInt(rangeTok[0]);
						stop = Integer.parseInt(rangeTok[1]);
					}
					for (int j=start;j<=stop;j++) {
						todoList.set(j);
					}
				} else {
					todoList.set(Integer.parseInt(tok));
				}
			}
		}
		
		int nextPage = todoList.nextSetBit(currentPage+1);
		return nextPage;
	}
}