package de.kiwiwings.gccom.ListingParser.plugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class GCVotePlugin implements SpiderPlugin {
	private final static String voteIds[] = {
		  "gcvote10", "gcvote15"
		, "gcvote20", "gcvote25"
		, "gcvote30", "gcvote35"
		, "gcvote40", "gcvote45"
		, "gcvote50" 
	};

	private final static int FETCH_SIZE = 250;
	
	public void execute(SpiderContext ctx) throws Exception {
		// http://gcvote.com/getVotes.php?waypoints=GC1ZFFH
		String updateDate = SpiderContext.fullDateFormat.format(new Date());
		Pattern votePat = Pattern.compile("\\((\\d).(\\d):(\\d+)\\)");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();

		int updateGCCount = FETCH_SIZE;
		
		List<Map<String,String>> dbCopy = new ArrayList<Map<String,String>>(ctx.getDatabase());
		Collections.sort(dbCopy, new DetailComparator(ctx, true, "gcvote_update"));
		
		while (updateGCCount > 0) {
			List<Map<String,String>> todoList = dbCopy.subList(0, Math.min(updateGCCount, FETCH_SIZE));
			
			StringBuffer wpList = new StringBuffer();
			for (Map<String,String> entry : todoList) {
				if (wpList.length()>0) wpList.append(",");
				String waypoint = entry.get("waypoint"); 
				wpList.append(waypoint);
				entry.put("gcvote_update", updateDate);
				ctx.putDatabaseEntry(entry);
			}

			todoList.clear();
			updateGCCount -= FETCH_SIZE;
			
			HttpRequestBase httpCall = new HttpGet("http://gcvote.com/getVotes.php?waypoints="+wpList);
			ctx.requestData(httpCall);

			InputStream content = ctx.getPageStream();
			Document currentDoc = docBuilder.parse(content);
			content.close();
		
			NodeList nl = currentDoc.getElementsByTagName("vote");
			for (int i=nl.getLength()-1; i>=0; i--) {
				Element elem = (Element)nl.item(i);
				Map<String,String> entryUpd = new HashMap<String,String>();
				entryUpd.put(ctx.getPrimaryKey(), elem.getAttribute("waypoint"));
				entryUpd.put("gcvote", elem.getAttribute("voteAvg"));
				entryUpd.put("gcvote_update", updateDate);
				for (String vi : voteIds) {
					entryUpd.put(vi, null);
				}
				
				Matcher m = votePat.matcher(elem.getAttribute("rawVotes"));
				while (m.find()) {
					entryUpd.put("gcvote"+m.group(1)+m.group(2), m.group(3));
				}

				ctx.putDatabaseEntry(entryUpd);
			}
		}
		
//		<votes userName='' currentVersion='2.4d' securityState='locked' loggedIn='false'>
//		<vote userName='' cacheId='f3e344e3-7945-452b-8a24-1271ed0e9dd4' voteMedian='5' voteAvg='4.625' voteCnt='16' voteUser='0' waypoint='GC1ZFFH' vote1='0' vote2='0' vote3='1' vote4='4' vote5='11' rawVotes='(3.0:1)(4.0:4)(5.0:11)'/>
//		<errorstring></errorstring>
//		</votes>
	}
}
