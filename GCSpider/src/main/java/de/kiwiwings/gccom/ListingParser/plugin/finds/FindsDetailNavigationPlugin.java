package de.kiwiwings.gccom.ListingParser.plugin.finds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.SpiderContext.NavigationState;
import de.kiwiwings.gccom.ListingParser.plugin.DetailComparator;
import de.kiwiwings.gccom.ListingParser.plugin.JTidyPlugin;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class FindsDetailNavigationPlugin implements SpiderPlugin {
	private List<Map<String,String>> todoList;
	
	public void execute(final SpiderContext ctx) throws Exception {
		SpiderConfig config = ctx.getConfig(); 
		
		// check for debugging
		if (doDebug(ctx)) return;
		
		String wpCode = getNextTodo(ctx);
		if (wpCode == null) return;
		
		HttpRequestBase httpCall = new HttpGet(config.getDetailUrl()+wpCode);
		ctx.updateFormParams(httpCall);
	}

	private boolean doDebug(SpiderContext ctx) throws Exception {
		if (!ctx.getConfig().getDebugLastFetched()) {
			return false;
		}
			
		FindsListNavigationPlugin.readFromDebugFile(ctx);
		// reverse logic to list-navigation
		if (ctx.getPageCount(true)==-1) {
			ctx.setNavigationState(NavigationState.last);
			new JTidyPlugin().execute(ctx);
		} else {
			ctx.setNavigationState(NavigationState.error);
		}

		return true;
	}
	
	private String getNextTodo(final SpiderContext ctx) {
		final int maxDetailsCount = ctx.getConfig().getMaxDetailsCount();
		final boolean onlyNew = ctx.getConfig().getOnlyNewDetails();
		
		if (maxDetailsCount <= 0
			|| (todoList != null && todoList.isEmpty())) {
			ctx.setNavigationState(NavigationState.error);
			return null;
		}

		if (todoList == null) {
			// init todo-list on first run
			List<Map<String,String>> dbCopy = new ArrayList<Map<String,String>>(ctx.getDatabase());
			Collections.sort(dbCopy, new DetailComparator(ctx, true, "lastupdate"));
			int copyCnt;
			if (onlyNew) {
				for (copyCnt=0; copyCnt<maxDetailsCount && copyCnt<dbCopy.size(); copyCnt++) {
					String lastUpdate = dbCopy.get(copyCnt).get("lastupdate");
					if (lastUpdate != null && !"".equals(lastUpdate)) break;
				}
			} else {
				copyCnt = Math.min(maxDetailsCount, dbCopy.size());
			}
			
			if (copyCnt == 0) {
				ctx.setNavigationState(NavigationState.error);
				return null;
			}
			
			todoList = dbCopy.subList(0, copyCnt);
		}
		
		String next = ctx.getPrimaryKeyValue(todoList.remove(0));
		ctx.setNavigationState(todoList.isEmpty() ? NavigationState.last : NavigationState.next);
		return next;
	}
}
