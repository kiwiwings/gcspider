package de.kiwiwings.gccom.ListingParser.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.kiwiwings.gccom.ListingParser.postfix.CalcDate;
import de.kiwiwings.gccom.ListingParser.postfix.Find;
import de.kiwiwings.gccom.ListingParser.postfix.Noop;

public class ParseParams {
	public static interface ParseProc {
		String process(Object input) throws Exception;
	}

	public String select;
	public String attrib = "text()"; // text() or attribute name
	public String postfix = "noop";
	public ParseProc postproc;

	protected static Gson gson;
	
	public static ParseParams fromString(String paramString) {
		if (paramString.startsWith("{")) {
			return fromJsonString(paramString);
		} else {
			return fromPlainString(paramString);
		}		
	}

	protected static ParseParams fromPlainString(String selectString) {
		ParseParams pp = new ParseParams();
		pp.select = selectString;
		pp.postproc = new Noop();
		return pp;
	}
	
	
	protected static ParseParams fromJsonString(String jsonString) {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gson = gsonBuilder.create();
		}
		ParseParams pp = gson.fromJson(jsonString, ParseParams.class);
		
		assert(pp.postfix != null);
		if ("noop".equals(pp.postfix)) {
			pp.postproc = new Noop();
		} else if (pp.postfix.startsWith("find(")) {
			String pattern = pp.postfix.substring(5,pp.postfix.length()-1);
			pp.postproc = new Find(pattern); 
		} else if ("calcDate".equals(pp.postfix)) {
			pp.postproc = new CalcDate();
		} else {
			assert(false);
		}
		return pp;
	}
}
