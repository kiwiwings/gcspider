package de.kiwiwings.gccom.ListingParser.plugin.hides;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.json.Shape;
import de.kiwiwings.gccom.ListingParser.json.ShapeContainer;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class CreateHidesChartPlugin implements SpiderPlugin {
	
	static class UseMapEntry {
		String name;
		String url;
		String title;
	}
	
	// http://code.google.com/apis/chart/image/docs/data_formats.html#simple
	private static final String simpleEncoding =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final String extendedEncoding =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.";
	
	private static final int minimumFinds = 6;

	
	
	public void execute(SpiderContext ctx) throws Exception {
		Map<String,Map<String,Map<String,String>>> ownermap =
			new HashMap<String,Map<String,Map<String,String>>>();

		Map<Date,String> datehidden_map = new TreeMap<Date,String>();
		Map<String,String> cachename_map = new HashMap<String,String>();
		Map<String,Map<String,String>> ftf_map = new HashMap<String,Map<String,String>>();
		
		for (Map<String,String> entry : ctx.getDatabase()) {
			String userid = entry.get("hides_userid");
			String waypoint = entry.get("hides_waypoint");
			Map<String,Map<String,String>> foundlist = ownermap.get(userid);
			if (foundlist == null) {
				foundlist = new HashMap<String,Map<String,String>>();
				ownermap.put(userid, foundlist);
			}
			foundlist.put(waypoint,entry);

			// fill datehidden of cache
			if (!datehidden_map.containsValue(waypoint)) {
				Date dateHidden = SpiderContext.fullDateFormat.parse(entry.get("hides_datehidden"));
				datehidden_map.put(dateHidden, waypoint);
			}

			// fill cachename
			cachename_map.put(waypoint, entry.get("hides_name"));
			
			// fill ftf
			if (!ftf_map.containsKey(waypoint)) {
				ftf_map.put(waypoint, entry);
			} else {
				Map<String,String> oldFtf = ftf_map.get(waypoint);
				Date oldDate = SpiderContext.fullDateFormat.parse(oldFtf.get("hides_founddate"));
				Date newDate = SpiderContext.fullDateFormat.parse(entry.get("hides_founddate"));
				long oldLogId = Long.parseLong(oldFtf.get("hides_logid"));
				long newLogId = Long.parseLong(entry.get("hides_logid"));
				int comp1 = newDate.compareTo(oldDate);
				if (comp1 == -1 || (comp1 == 0 && newLogId < oldLogId)) {
					ftf_map.put(waypoint, entry);
				}
			}
		}

		List<Map<String,Map<String,String>>> ownerlist =
			new ArrayList<Map<String,Map<String,String>>>(); 
		ownerlist.addAll(ownermap.values());
		Collections.sort(ownerlist, new Comparator<Map<String,Map<String,String>>>(){
			public int compare(Map<String,Map<String, String>> o1, Map<String,Map<String, String>> o2) {
				// reverse sort, biggest first
				return o2.size()-o1.size();
			}
		});

		DateFormat simpleDate = new SimpleDateFormat("dd.MM.yy");
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(3);
		
		Map<String,UseMapEntry> imageMapEntry = new HashMap<String,UseMapEntry>(); 
		
		StringBuffer axisY = new StringBuffer();
		int maxFinder = 0;
		for (Map<String,Map<String,String>> entry : ownerlist) {
			if (entry.size() < minimumFinds) break;
			if (axisY.length()>0) axisY.append(",");
			axisY.append(maxFinder+1);
			maxFinder++;
			
		}
		
		StringBuffer axisX1 = new StringBuffer();
		StringBuffer axisX2 = new StringBuffer();
		StringBuffer cache_names1 = new StringBuffer();
		StringBuffer cache_names2 = new StringBuffer();
		int i=1;
		for (String wpcode : datehidden_map.values()) {
			UseMapEntry ume = new UseMapEntry();
			ume.title = cachename_map.get(wpcode);
			ume.url = "http://coord.info/"+wpcode;
			
			StringBuffer axis, cache_names;
			if (i % 2 == 1) {
				axis = axisX1;
				cache_names = cache_names1;
				ume.name = "axis0_"+((i-1)/2);
			} else {
				axis = axisX2;
				cache_names = cache_names2;
				ume.name = "axis2_"+((i-1)/2);
			}
			imageMapEntry.put(ume.name, ume);
			if (cache_names.length()>0) cache_names.append("|");
			cache_names.append(wpcode.substring(2));
			if (axis.length()>0) axis.append(",");
			axis.append(i);
			i++;
		}
		
		final int yscale = 1;
		final int xscale = 1;
		StringBuffer user_names = new StringBuffer();
		int maxX = 0, maxY = 0, logpos = 0;
		i=0;
		List<Integer> xvals = new ArrayList<Integer>();
		List<Integer> yvals = new ArrayList<Integer>();
		for (Map<String,Map<String,String>> entry : ownerlist) {
			if (entry.size() < minimumFinds) break;
			Map<String,String> firstEntry = entry.values().iterator().next();
			String name = firstEntry.get("hides_username")+" ("+entry.size()+") ";
			user_names.insert(0, "|"+encode(name));

			UseMapEntry ume = new UseMapEntry();
			ume.title = name;
			ume.url = "http://coord.info/PR"+CreateHidesStatsPlugin.shorter(firstEntry.get("hides_userid"));
			ume.name = "axis1_"+(maxFinder-i-1);
			imageMapEntry.put(ume.name, ume);
			
			
			int ypos = (maxFinder-1)*yscale-i*yscale+1;
			int xpos = 0;
			for (String wpcode : datehidden_map.values()) {
				xpos += xscale;
				Map<String,String> log = entry.get(wpcode);
				Map<String,String> ftflog = ftf_map.get(wpcode);
				if (log == null) continue;
				xvals.add(xpos);
				yvals.add(ypos);
//				size.append((log == ftflog) ? 100 : 80);
				maxX = Math.max(maxX, xpos);
				maxY = Math.max(maxY, ypos);
				ume = new UseMapEntry();

				Date date = SpiderContext.fullDateFormat.parse(log.get("hides_founddate"));
				ume.title = simpleDate.format(date);
				ume.url = "http://coord.info/GL"+CreateHidesStatsPlugin.shorter(log.get("hides_logid"));
				ume.name = "circle"+logpos;
				imageMapEntry.put(ume.name, ume);
				logpos++;
			}
			i++;
		}

		// http://code.google.com/apis/chart/image/docs/gallery/scatter_charts.html#axis_labels
		String chart = "https://chart.googleapis.com/chart?"
			+"cht=s"
			+"&chxt=t,y,t"
			+"&chs=600x500"
			+"&chf=bg,s,000000FF"
			+"&chm=H,D6A4FB,0,-1,1,-1|V,D6A4FB,0,-1,1,-1"
//			+"&chg="+nf.format(100d/maxX)+","+nf.format(100d/maxY)
			+"&chxs=0,FFFFFF|1,FFFFFF|2,FFFFFF"
			+"&chxr=0,0,"+maxX+"|1,0,"+maxY+"|2,0,"+maxX
//			+"&chds=0,"+maxX+",0,"+maxY
			+"&chxl=0:|"+cache_names1+"|1:"+user_names+"|2:|"+cache_names2+"|"
			+"&chxp=0,"+axisX1+"|1,"+axisY+"|2,"+axisX2
//			+"&chd=s:"+simpleEncode(xvals,maxX)
//			+","+simpleEncode(yvals,maxY)
			+"&chd=e:"+extendedEncode(xvals,maxX)
			+","+extendedEncode(yvals,maxY)
		;
		chart = chart.replace("|", "%7C");
		System.out.println(chart);

		HttpRequestBase httpCall = new HttpGet(chart+"&chof=json");
		ctx.requestData(httpCall);
		
		Gson gson = new GsonBuilder().create();
		InputStreamReader isr = new InputStreamReader(ctx.getPageStream(), ctx.getPageEncoding());
//		FileReader fr = new FileReader("src/test/resources/chartmap.json");
		ShapeContainer sc = gson.fromJson(isr, ShapeContainer.class);
		isr.close();
		
		StringBuffer usemap = new StringBuffer("<map name=\"topfinder\">\n");
		for (Shape shape : sc.chartshape) {
			UseMapEntry ume = imageMapEntry.get(shape.name);
			if (ume == null) continue;
			
			usemap.append("<area shape=\""+shape.type+"\" coords=\"");
			for (int c : shape.coords) {
				usemap.append(c+",");
			}
			usemap.deleteCharAt(usemap.length()-1);
			usemap.append("\" href=\""+ume.url+"\" title=\""+ume.title.replaceAll("\"(.*)\"", "&#8220;$1&#8221;")+"\"/>\n");
		}
		usemap.append("</map>");
		
		FileWriter fw = new FileWriter("src/test/resources/hides_chart.html");
		fw.write("<div>");
		fw.write(usemap.toString());
		fw.write("<img src=\""+chart+"\" usemap=\"#topfinder\" style=\"border: none\"/>");
		fw.write("</div>");
		fw.close();
		
		
	}

	static String extendedEncode(List<Integer> arrVals, int maxVal) {
		final int EXTENDED_MAP_LENGTH = extendedEncoding.length();
		StringBuffer chartData = new StringBuffer();

		for (int i = 0, len = arrVals.size(); i < len; i++) {
			int numericVal = arrVals.get(i);

			// Scale the value to maxVal.
			int scaledVal = (int) Math.floor(EXTENDED_MAP_LENGTH
					* EXTENDED_MAP_LENGTH * numericVal / maxVal);

			if (scaledVal > (EXTENDED_MAP_LENGTH * EXTENDED_MAP_LENGTH) - 1) {
				chartData.append("..");
			} else if (scaledVal < 0) {
				chartData.append("__");
			} else {
				// Calculate first and second digits and add them to the output.
				int quotient = (int) Math.floor(scaledVal / EXTENDED_MAP_LENGTH);
				int remainder = scaledVal - EXTENDED_MAP_LENGTH * quotient;
				chartData.append(extendedEncoding.charAt(quotient));
				chartData.append(extendedEncoding.charAt(remainder));
			}
		}

		return chartData.toString();
	}	

	// This function scales the submitted values so that
	// maxVal becomes the highest value.
	static String simpleEncode(List<Integer> valueArray, int maxValue) {
		StringBuffer chartData = new StringBuffer();
		for (int i = 0; i < valueArray.size(); i++) {
			int currentValue = valueArray.get(i);
			if (currentValue >= 0) {
				int scaledVal = Math.round((simpleEncoding.length() - 1) * currentValue / maxValue);
				chartData.append(simpleEncoding.charAt(scaledVal));
			} else {
				chartData.append("_");
			}
		}
		return chartData.toString();
	}
	
	
	final static String[] hex = {
		"%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
		"%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
		"%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
		"%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
		"%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
		"%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
		"%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
		"%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
		"%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
		"%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
		"%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
		"%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
		"%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
		"%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
		"%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
		"%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
		"%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
		"%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
		"%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
		"%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
		"%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
		"%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
		"%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
		"%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
		"%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
		"%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
		"%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
		"%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
		"%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
		"%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
		"%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
		"%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
	};

  /**
   * Encode a string to the "x-www-form-urlencoded" form, enhanced
   * with the UTF-8-in-URL proposal. This is what happens:
   *
   * <ul>
   * <li><p>The ASCII characters 'a' through 'z', 'A' through 'Z',
   *        and '0' through '9' remain the same.
   *
   * <li><p>The unreserved characters - _ . ! ~ * ' ( ) remain the same.
   *
   * <li><p>The space character ' ' is converted into a plus sign '+'.
   *
   * <li><p>All other ASCII characters are converted into the
   *        3-character string "%xy", where xy is
   *        the two-digit hexadecimal representation of the character
   *        code
   *
   * <li><p>All non-ASCII characters are encoded in two steps: first
   *        to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
   *        secondly each of these bytes is encoded as "%xx".
   * </ul>
   *
   * @param s The string to be encoded
   * @return The encoded string
   */
  private static String encode(String s)
  {
    StringBuffer sbuf = new StringBuffer();
    int len = s.length();
    for (int i = 0; i < len; i++) {
      int ch = s.charAt(i);
      if ('A' <= ch && ch <= 'Z') {		// 'A'..'Z'
        sbuf.append((char)ch);
      } else if ('a' <= ch && ch <= 'z') {	// 'a'..'z'
	       sbuf.append((char)ch);
      } else if ('0' <= ch && ch <= '9') {	// '0'..'9'
	       sbuf.append((char)ch);
      } else if (ch == ' ') {			// space
	       sbuf.append('+');
      } else if (ch == '-' || ch == '_'		// unreserved
          || ch == '.' || ch == '!'
          || ch == '~' || ch == '*'
          || ch == '\'' || ch == '('
          || ch == ')') {
        sbuf.append((char)ch);
      } else if (ch <= 0x007f) {		// other ASCII
	       sbuf.append(hex[ch]);
      } else if (ch <= 0x07FF) {		// non-ASCII <= 0x7FF
	       sbuf.append(hex[0xc0 | (ch >> 6)]);
	       sbuf.append(hex[0x80 | (ch & 0x3F)]);
      } else {					// 0x7FF < ch <= 0xFFFF
	       sbuf.append(hex[0xe0 | (ch >> 12)]);
	       sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
	       sbuf.append(hex[0x80 | (ch & 0x3F)]);
      }
    }
    return sbuf.toString();
  }
}
