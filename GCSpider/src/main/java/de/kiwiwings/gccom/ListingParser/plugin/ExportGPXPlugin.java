package de.kiwiwings.gccom.ListingParser.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.gpx.*;

public class ExportGPXPlugin implements SpiderPlugin {
	TemplateToken tokens[];
	double maxLat = Double.NEGATIVE_INFINITY, maxLon = Double.NEGATIVE_INFINITY;
	double minLat = Double.POSITIVE_INFINITY, minLon = Double.POSITIVE_INFINITY;
	
	public void execute(SpiderContext ctx) throws Exception {
		parseWaypointTemplate(ctx);
		determineMaxValues(ctx);

		SpiderConfig config = ctx.getConfig();
		File gpxF = new File(config.getGpxFile());
		FileOutputStream fos = new FileOutputStream(gpxF);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
		BufferedWriter bw = new BufferedWriter(osw);

		writeHeader(ctx, bw);
		
		for (Map<String,String> entry : ctx.getDatabase()) {
			writeEntry(ctx, bw, entry);
		}
		
		writeFooter(ctx, bw);
		
		bw.close();
	}
	
	private void writeEntry(SpiderContext ctx, Writer out, Map<String,String> entry) throws Exception {
		for (TemplateToken t : tokens) {
			out.write(t.print(ctx, entry));
		}
	}

	private void writeFooter(SpiderContext ctx, Writer out) throws Exception {
		out.write(ctx.getConfig().getGpxFooter());		
	}	
	
	private void writeHeader(SpiderContext ctx, Writer out) throws Exception {
		SpiderConfig config = ctx.getConfig();
		String header = config.getGpxHeader();
		header = header.replaceFirst("\\$minlat\\$", Double.toString(minLat));
		header = header.replaceFirst("\\$maxlat\\$", Double.toString(maxLat));
		header = header.replaceFirst("\\$minlon\\$", Double.toString(minLon));
		header = header.replaceFirst("\\$maxlon\\$", Double.toString(maxLon));
		String headerTime = config.getGpxHeaderTime();
		header = header.replaceFirst("\\$time\\$", new SimpleDateFormat(headerTime).format(new Date()));
		out.write(header);
	}
	
	private void determineMaxValues(SpiderContext ctx) {
		for (Map<String,String> entry : ctx.getDatabase()) {
			String latStr = entry.get("latitude");
			String lonStr = entry.get("longitude");
			if (latStr == null || "".equals(latStr) || lonStr == null || "".equals(lonStr)) continue;
			double lat = Double.parseDouble(latStr);
			double lon = Double.parseDouble(lonStr);
			maxLat = Math.max(maxLat, lat);
			minLat = Math.min(minLat, lat);
			maxLon = Math.max(maxLon, lon);
			minLon = Math.min(minLon, lon);
		}
	}
	
	
	private void parseWaypointTemplate(SpiderContext ctx) {
		SpiderConfig config = ctx.getConfig();
		String templateString = config.getWaypointTemplate();
		
		List<TemplateToken> tokenList = new ArrayList<TemplateToken>();
		String token[] = templateString.split("\\$");
		boolean isToken = true;
		for (int i=0; i<token.length; i++) {
			if (isToken) {
				isToken = false;
			} else {
				if (i>0 && token[i-1].endsWith("\\")) {
					token[i] = token[i-1]+token[i];
				} else {
					isToken = true;
				}
			}
			
			if (!isToken) {
				tokenList.add(new StaticToken(token[i]));
			} else if (token[i].contains(":neg_boolean")) {
				tokenList.add(new NegativeBooleanToken(token[i].substring(0,token[i].indexOf(':'))));
			} else if (token[i].contains(":boolean")) {
				tokenList.add(new BooleanToken(token[i].substring(0,token[i].indexOf(':'))));
			} else if (token[i].contains(":date")) {
				tokenList.add(new DateToken(token[i].substring(0,token[i].indexOf(':'))));
			} else if ("attributes".equals(token[i])) {
				tokenList.add(new AttributeToken(config.getAttributeTemplate()));
			} else {
				tokenList.add(new StringToken(token[i]));
			}
		}
		tokens = tokenList.toArray(new TemplateToken[0]);
	}
}
