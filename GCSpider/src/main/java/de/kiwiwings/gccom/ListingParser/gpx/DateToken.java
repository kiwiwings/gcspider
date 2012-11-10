package de.kiwiwings.gccom.ListingParser.gpx;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class DateToken extends TemplateToken {
    static DateFormat dateIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

	public DateToken(String tokenStr) {
		super(tokenStr);
	}
	
	public String print(SpiderContext ctx, Map<String,String> entry) throws ParseException {
		String input = entry.get(tokenStr);
		return (input == null || "".equals(input)) ? "" : dateIso.format(SpiderContext.fullDateFormat.parse(input));
	}
}
