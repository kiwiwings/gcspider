package de.kiwiwings.gccom.ListingParser.postfix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.kiwiwings.gccom.ListingParser.json.ParseParams.ParseProc;

public class Find implements ParseProc {
	Pattern p;

	public Find(String input) {
		p = Pattern.compile(input);
	}
	
	public String process(Object input) throws Exception {
		Matcher m = p.matcher(input.toString());
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			for (int i=1;i<=m.groupCount();i++) {
				sb.append(m.group(i));
			}
		}
		return sb.toString();
	}

}
