package de.kiwiwings.gccom.ListingParser.postfix;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.json.ParseParams.ParseProc;

public class CalcDate implements ParseProc {

	private static DateFormat parseMediumFormat =
		new SimpleDateFormat("dd MMM yy", Locale.US);
	private static DateFormat parseShortFormat =
		new SimpleDateFormat("MM/dd/yyyy", Locale.US);
	private static DateFormat parseLongFormat =
		new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US);
	private static DateFormat parseMediumFormat2 =
		new SimpleDateFormat("MMMM dd", Locale.US);
	private static DateFormat parseMediumFormat3 =
		new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
	
	public String process(Object input) throws ParseException {
		String datestr = input.toString();
		if (datestr != null) {
			datestr = datestr.replace("*", "");
			datestr = datestr.trim();
		}
		
		if (datestr == null || "".equals(datestr)) {
			System.out.println("calcDate received empty date string.");
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		if (datestr.contains("ago")) {
			int days = Integer.parseInt(datestr.substring(0, datestr.indexOf(' ')));
			cal.add(Calendar.DATE, -days);
			cal.add(Calendar.HOUR, -10);
		} else if ("Today".equals(datestr)) {
			cal.add(Calendar.HOUR, -10);
		} else if ("Yesterday".equals(datestr)) {
			cal.add(Calendar.DATE, -1);
			cal.add(Calendar.HOUR, -10);
		} else if (datestr.indexOf('/') != -1){
			cal.setTime(parseShortFormat.parse(datestr));
		} else if (datestr.startsWith("Monday")
			|| datestr.startsWith("Tuesday")
			|| datestr.startsWith("Wednesday")
			|| datestr.startsWith("Thursday")
			|| datestr.startsWith("Friday")
			|| datestr.startsWith("Saturday")
			|| datestr.startsWith("Sunday")) {
			cal.setTime(parseLongFormat.parse(datestr));
		} else if (datestr.startsWith("January")
			|| datestr.startsWith("February")
			|| datestr.startsWith("March")
			|| datestr.startsWith("April")
			|| datestr.startsWith("May")
			|| datestr.startsWith("June")
			|| datestr.startsWith("July")
			|| datestr.startsWith("August")
			|| datestr.startsWith("September")
			|| datestr.startsWith("October")
			|| datestr.startsWith("November")
			|| datestr.startsWith("December")
		) {
			if (datestr.indexOf(',') == -1) {
				int year = cal.get(Calendar.YEAR);
				cal.setTime(parseMediumFormat2.parse(datestr));
				cal.set(Calendar.YEAR, year);
			} else {
				cal.setTime(parseMediumFormat3.parse(datestr));
			}
		} else {
			cal.setTime(parseMediumFormat.parse(datestr));
		}

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		
		return SpiderContext.fullDateFormat.format(cal.getTime());
	}
}
