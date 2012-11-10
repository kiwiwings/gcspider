package de.kiwiwings.gccom.ListingParser.plugin;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class DetailComparator implements Comparator<Map<String,String>> {
	final SpiderContext ctx;
	final boolean premiumMemberCompare;
	final String dateField;
	
	public DetailComparator(SpiderContext ctx, boolean premiumMemberCompare, String dateField) {
		this.ctx = ctx;
		this.premiumMemberCompare = premiumMemberCompare;
		this.dateField = dateField;
	}

	@Override
	public int compare(Map<String, String> o1, Map<String, String> o2) {
		try {
			Date date1 = getLastUpdate(o1);
			Date date2 = getLastUpdate(o2);
			int result = date1.compareTo(date2);
			if (result != 0) return result;
			
			String pk1 = ctx.getPrimaryKeyValue(o1);
			String pk2 = ctx.getPrimaryKeyValue(o2);
			result = pk1.compareTo(pk2);
			
			return result; 
		} catch (ParseException e) {
			throw new RuntimeException("Error while sorting entries.", e);
		}
	}

	protected void parseDate(Calendar cal, String dateStr) throws ParseException {
		if (dateStr != null && !"".equals(dateStr)) {
			cal.setTime(SpiderContext.fullDateFormat.parse(dateStr));
		} else {
			cal.set(2000, 0, 1, 0, 0, 0);
		}
	}

	/**
	 * if pm-user logged in, try to parse pm-only caches first (move to first)
	 * otherwise don't parse pm-only caches at all (move to last)
	 * 
	 * @param cal
	 * @param entry
	 */
	protected void parsePremium(Calendar cal, Map<String,String> entry) {
		if (!premiumMemberCompare) return;
		
		boolean isPmCache = "true".equalsIgnoreCase(entry.get("pmonly"));
		boolean isPmUser = ctx.getConfig().isPremiumMember(); 
		if (isPmCache) {
			cal.add(Calendar.YEAR, isPmUser ? -20 : 20);
		} else {
			cal.add(Calendar.YEAR, isPmUser ? 20 : 0);
		}
	}
	
	protected Date getLastUpdate(Map<String,String> entry) throws ParseException {
		String dateStr = entry.get(dateField);
		Calendar cal = Calendar.getInstance();
		parseDate(cal, dateStr);
		parsePremium(cal, entry);
		return cal.getTime();
	}
}
