package de.kiwiwings.gccom.ListingParser.plugin.hides;

import java.io.FileWriter;
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

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class CreateHidesStatsPlugin implements SpiderPlugin {
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
		
		int degree=50, movx=0, movy=23;
		double rad=Math.PI*degree/180;
		String cos = nf.format(Math.cos(rad));
		String sin = nf.format(Math.sin(rad));
//		String box_matrix = "matrix("+cos+",-"+sin+","+sin+","+cos+","+movx+","+movy+")";
//		String box_rotate_style = 
//			"-webkit-transform: "+box_matrix+"; "+
//			"-moz-transform: "+box_matrix+"; "+
//			"-ms-transform: "+box_matrix+"; "+
//			"-o-transform: "+box_matrix+"; "+
//			"transform: "+box_matrix+"; ";

		String msbox_matrix = "M11="+cos+",M21=-"+sin+",M12="+sin+",M22="+cos+",Dy=60";
		System.out.println(msbox_matrix);
		String box_rotate_style =
			"PoSition:relative; top: -5px; width: 100px; height: 80px; "
			+"-moz-transform: rotate(-"+degree+"deg); " /* FF3.5+ */
			+"-o-transform: rotate(-"+degree+"deg); " /* Opera 10.5 */
			+"-webkit-transform: rotate(-"+degree+"deg); " /* Saf3.1+, Chrome */
			+"FiLter: progid:DXImageTransform.Microsoft.Matrix("+msbox_matrix+"); " /* IE6,IE7 */
			+"-Ms-FiLter: 'progid:DXImageTransform.Microsoft.Matrix("+msbox_matrix+")'"  /* IE8 */
			;
		
		
		StringBuffer line = new StringBuffer();
//		line.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//		line.append("<html><head><base href=\"http://www.geocaching.com\"/></head><body>");
		line.append("<table style=\"margin-left: auto; margin-right: auto\">\n");
		line.append("<tr style=\"vertical-align: bottom\">\n");
		line.append("<th style=\"text-align: left\">Finder</th>\n");
		for (String wpcode : datehidden_map.values()) {
			line.append("<td>");
			line.append("<div style=\"PoSition:rElative; width: 30px; height: 80px; \">");
			line.append("<div style=\""+box_rotate_style+"\">");
			line.append("<a href=\"http://coord.info/"+wpcode+"\"");
			line.append(" title=\""+cachename_map.get(wpcode).replace("\"","&#8221;")+"\">"+wpcode+"</a>");
			line.append("</div>");
			line.append("</div>");
			line.append("</td>\n");
		}
		line.append("</tr>\n");
		
		final int minimumFinds = 5;
		for (Map<String,Map<String,String>> entry : ownerlist) {
			if (entry.size() < minimumFinds) break;
			Map<String,String> firstEntry = entry.values().iterator().next();
			line.append("<tr><td>");
			line.append("<a href=\"http://coord.info/PR"+shorter(firstEntry.get("hides_userid"))+"\">");
			line.append(firstEntry.get("hides_username"));
			line.append("</a> ("+entry.size()+")</td>");
			
			for (String wpcode : datehidden_map.values()) {
				Map<String,String> log = entry.get(wpcode);
				Map<String,String> ftflog = ftf_map.get(wpcode);
				if (log == null) {
					line.append("<td/>");
				} else {
					Date founddate = SpiderContext.fullDateFormat.parse(log.get("hides_founddate"));
					line.append("<th><a");
					line.append(" href=\"http://coord.info/GL"+shorter(log.get("hides_logid"))+"\"");
					if (log == ftflog) {
						line.append(" title=\"FTF - "+simpleDate.format(founddate)+"\">");
						line.append(":bad:");
					} else {
						line.append(" title=\""+simpleDate.format(founddate)+"\">");
						line.append("[:D]");
					}
					line.append("</a></th>");
				}
			}
			
			line.append("</tr>\n");
		}
		
		line.append("</table>");
//		line.append("</body></html>");
		
		FileWriter fw = new FileWriter("src/test/resources/hides_stats.html");
		fw.write(line.toString());
		fw.close();
	}

	protected static final String java_digits = "0123456789ABCDEFGHIJKLMNOPQRSTU".toLowerCase();
	protected static final String gc_digits =   "0123456789ABCDEFGHJKMNPQRTVWXYZ".toLowerCase();
	
	protected static String shorter(String inputStr) {
		// http://www.markwell.us/geofaq.htm#Hexadec
		long input = Long.parseLong(inputStr);
		StringBuffer sb = new StringBuffer();
		if (input <= 0xFFFFl) {
			sb.append(Long.toHexString(input));
		} else {
			input = (16*31*31*31) + (input - 0xFFFF - 1);
			sb.append(Long.toString(input, gc_digits.length()));
			for (int i=0; i<sb.length(); i++) {
				char ch = sb.charAt(i);
				int pos = java_digits.indexOf(ch);
				ch = gc_digits.charAt(pos);
				sb.setCharAt(i, ch);
			}
		}
		
		return sb.toString().toUpperCase();
	}
}
