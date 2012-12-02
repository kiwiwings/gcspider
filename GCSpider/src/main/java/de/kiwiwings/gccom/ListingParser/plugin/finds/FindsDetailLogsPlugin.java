package de.kiwiwings.gccom.ListingParser.plugin.finds;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.SpiderContext.NavigationState;
import de.kiwiwings.gccom.ListingParser.json.Log;
import de.kiwiwings.gccom.ListingParser.json.LogContainer;
import de.kiwiwings.gccom.ListingParser.parser.CommonParser;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;
import de.kiwiwings.gccom.ListingParser.postfix.Html2BBCode;

public class FindsDetailLogsPlugin implements SpiderPlugin {
	public static final int FETCH_SIZE = 25;
	protected String waypoint;
	protected int logCount;
	protected String userToken;
	private Gson gson;
	
	public FindsDetailLogsPlugin() throws Exception {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			public Date deserialize(JsonElement json, Type typeOfT,JsonDeserializationContext context)
			throws JsonParseException {
				try {
					return df.parse(json.getAsString());
				} catch (ParseException e) {
					throw new JsonParseException(e);
				}
			}
		});
		
		gson = gsonBuilder.create();
	}
	
	public void execute(SpiderContext ctx) throws Exception {
		if (ctx.getConfig().getDebugLastFetched()) return;
		
		updateDetailData(ctx);
		if (waypoint == null || logCount == 0) return;
		
		boolean processed = false;
FETCH:	for (int idx=0; (idx*FETCH_SIZE)<logCount; idx++) {
			// http://www.geocaching.com/seek/geocache.logbook?tkn=HIBNOFPFMBDN55JBBSHBYIODMH7NFMH37DGVCQCARZPNVYUBR6GYTCXIYK3ABMJMJQZNLLUBA6FNBA6PQCLZVMXEDCFFQ4CIO6LCTPQKAMGJRJYPUQJN72M4ASEWCE6DTWGC2PSBQQB62TYDCLEXBOJ4SY&idx=2&num=25&decrypt=false
			String url = "http://www.geocaching.com/seek/geocache.logbook"
				+"?tkn="+userToken
				+"&idx="+(idx+1)
				+"&num="+FETCH_SIZE
				+"&decrypt=false"; 
			HttpRequestBase httpCall = new HttpGet(url);
			ctx.requestData(httpCall);
			
			Reader r = new InputStreamReader(ctx.getPageStream(), ctx.getPageEncoding());
			LogContainer lc = gson.fromJson(r, LogContainer.class);
			r.close();
			
			for (Log l : lc.data) {
				switch (processState(ctx, l)) {
				case next: processed = true; processEntry(ctx, l); break;
				case skip: break;
				case last: processed = true; processEntry(ctx, l); break FETCH;
				case error: break FETCH;
				}
			}
		}
		
		if (!processed) {
			System.out.println("no log details found.");
			return;
		}
	}

	protected NavigationState processState(SpiderContext ctx, Log logEntry) {
		String user = ctx.getConfig().getProperty("parse.user");
		return (user.equals(logEntry.userName) && !"Write note".equals(logEntry.logType))
			? NavigationState.last : NavigationState.skip;
	}
	
	private void processEntry(SpiderContext ctx, Log logEntry) throws Exception {
		String s = new Html2BBCode().process(logEntry.logText);
		logEntry.logText = s;
		putDatabaseEntry(ctx, logEntry);
	}
	
	protected void updateDetailData(SpiderContext ctx) throws Exception {
		CommonParser parser = ctx.getParser(); 
		userToken = parser.selectConfigString(ctx, "parse.detail.usertoken");
		waypoint = parser.selectConfigString(ctx, "parse.detail.waypoint");
		String logCountStr = parser.selectConfigString(ctx, "parse.detail.loggedvisits");
		
		try {
			logCount = Integer.parseInt(logCountStr);
		} catch (NumberFormatException e) {
			logCount = 0; // i.e. no founds yet
			System.out.println("no founds for "+waypoint+" yet.");
		}
	}
	
	protected void putDatabaseEntry(SpiderContext ctx, Log logEntry)
	throws Exception {
		String attrList[][] = {
			  { "waypoint", waypoint }
			, { "lastupdate", SpiderContext.fullDateFormat.format(new Date()) }
			, { "logtext", logEntry.logText }
			, { "logid", logEntry.logId.toString() }
		};
		
		Map<String,String> entry = new HashMap<String,String>();
		for (String attr[] : attrList) {
			entry.put(attr[0], attr[1]);
		}
		
		ctx.putDatabaseEntry(entry);
	}
}
