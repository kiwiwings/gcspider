package de.kiwiwings.gccom.ListingParser;

import java.util.Properties;

public class SpiderConfig extends Properties {
	
	private static final long serialVersionUID = 634922876878398243L;

	public String getUser() {
		return (String)get("parse.user");
	}
	
	public boolean getDebugLastFetched() {
		return Boolean.parseBoolean((String)get("debug.lastFetched"));
	}
	
	public int getMaxDetailsCount() {
		return Integer.parseInt((String)get("detail.pages"));
	}
	
	public boolean getOnlyNewDetails() {
		return "1".equals(get("detail.onlynew"));
	}
	
	public String getResponseXmlFile() {
		return (String)get("debug.response.xml");
	}

	public String getResponseRawFile() {
		return (String)get("debug.response.raw");
	}

	public boolean getDebug() {
		return Boolean.valueOf((String)get("debug.active"));
	}
	
	public String getExcelFile() {
		return (String)get("output.file.excel");
	}

	public String getGpxFile() {
		return (String)get("output.gpx.file");
	}

	public String getGpxHeader() {
		return (String)get("output.gpx.header");
	}

	public String getGpxHeaderTime() {
		return (String)get("output.gpx.headertime");
	}
	
	public String getGpxFooter() {
		return (String)get("output.gpx.footer");
	}

	public String getListPages() {
		return (String)get("list.pages");
	}
	
	
	public boolean isPremiumMember() {
		return Boolean.valueOf((String)get("login.ispremium"));
	}
	
	public String getWaypointTemplate() {
		return (String)get("output.gpx.entry");
	}

	public String getAttributeTemplate() {
		return (String)get("output.gpx.attribute");
	}

	public String getUserAgent() {
		return (String)get("http.useragent");
	}
	
	public String getLoginUrl() {
		return (String)get("http.url.login");
	}
	
	public String getListUrl() {
		return (String)get("http.url.list");
	}

	public String getDetailUrl() {
		return (String)get("http.url.detail");
	}

	public String getHidesListUrl() {
		return (String)get("http.url.hides");
	}
	
	public int getSleepTime() {
		return Integer.parseInt((String)get("parse.sleep"));
	}
}
