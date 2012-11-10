package de.kiwiwings.gccom.ListingParser.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.SpiderContext.NavigationState;

public class UserNavigationPlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		if (ctx.getConfig().getDebugLastFetched()) {
			// TODO: not implemented
			ctx.setNavigationState(NavigationState.error);
			return;
		}		
		
		List<Map<String,String>> todoList = ctx.getUserMappingTodos();
		if (todoList == null) {
			todoList = new ArrayList<Map<String,String>>();
			ctx.setUserMappingTodos(todoList);
			Map<String,String> guid2Id = new HashMap<String,String>();
			
			// determine mapping
			for (Map<String,String> entry : ctx.getDatabase()) {
				String guid = entry.get("owner_guid");
				String forumId = entry.get("ownerid");
				if (isFilled(guid) && isFilled(forumId)) {
					guid2Id.put(guid,forumId);
				}
			}
			
			// fill known ids and todo list
			for (Map<String,String> entry : ctx.getDatabase()) {
				String guid = entry.get("owner_guid");
				String forumId = entry.get("ownerid");
				if (!isFilled(guid) || isFilled(forumId)) continue;
				forumId = guid2Id.get(guid);
				if (isFilled(forumId)) {
					entry.put("ownerid", forumId);
					ctx.putDatabaseEntry(entry);
				} else {
					todoList.add(entry);
				}
			}
		}

		if (todoList.isEmpty()) {
			ctx.setNavigationState(NavigationState.error);
			return;
		}
		
		Set<String> distinctIds = new HashSet<String>();
		for (Map<String,String> entry : todoList) {
			distinctIds.add(entry.get("owner_guid"));
		}
		
		ctx.setNavigationState(distinctIds.size() > 1 ? NavigationState.next : NavigationState.last);
		
		String guid = distinctIds.iterator().next();
		HttpRequestBase httpCall = new HttpGet("http://www.geocaching.com/profile/?guid="+guid);
		ctx.updateFormParams(httpCall);
	}
	
	private static boolean isFilled(String str) {
		return (str != null && str.length()>0);
	}
}
