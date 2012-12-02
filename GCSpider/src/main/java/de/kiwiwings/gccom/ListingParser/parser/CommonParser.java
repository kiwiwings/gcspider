package de.kiwiwings.gccom.ListingParser.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.json.ParseParams;

public abstract class CommonParser {
	Map<String,ParseParams> paramsCache = new HashMap<String,ParseParams>();
	
	public abstract CommonElement[] selectElements(String select) throws Exception;
	
	public abstract CommonElement[] selectElements(CommonElement base, String select) throws Exception;
	
	public abstract CommonElement selectElement(CommonElement base, String select, int index) throws Exception;
	
	public abstract boolean hasDocument();
	
	public String selectConfigString(SpiderContext ctx, String configEntry) throws Exception {
		return selectConfigString(ctx, null, configEntry);
	}
	
	public String selectConfigString(SpiderContext ctx, CommonElement base, String configEntry) throws Exception {
		String entry = ctx.getConfig().getProperty(configEntry);
		if (entry == null || "".equals(entry)) return null;

		ParseParams pp = paramsCache.get(configEntry);
		if (pp == null) {
			pp = ParseParams.fromString(entry);
			paramsCache.put(configEntry, pp);
		}

		CommonElement elem = selectElement(base, pp.select, pp.index);
		assert(pp.attrib != null);
		
		String result;
		if ("exist()".equals(pp.attrib)) {
			result = (elem == null ? "false" : "true");
		} else if (elem == null) {
			result = "";
		} else if ("tree()".equals(pp.attrib)) {
			Element el = elem.getTree();
			result = pp.postproc.process(el);
		} else {
			if ("text()".equals(pp.attrib)) {
				result = elem.getText();
			} else {
				result = elem.getAttribute(pp.attrib);
			}
			result = pp.postproc.process(result);
		}

		return result;
	}

	public String[] selectConfigStringList(SpiderContext ctx, String configEntry) throws Exception {
		return selectConfigStringList(ctx, null, configEntry);
	}
		
	public String[] selectConfigStringList(SpiderContext ctx, CommonElement base, String configEntry) throws Exception {
		String entry = ctx.getConfig().getProperty(configEntry);
		if (configEntry == null || "".equals(configEntry)) return null;

		ParseParams pp = paramsCache.get(configEntry);
		if (pp == null) {
			pp = ParseParams.fromString(entry);
			paramsCache.put(configEntry, pp);
		}

		CommonElement elem[] = selectElements(base, pp.select);
		
		List<String> result = new ArrayList<String>();
		if ("exist()".equals(pp.attrib)) {
			result.add((elem == null || elem.length == 0) ? "false" : "true");
		} else {
			assert(pp.attrib != null);
			for (CommonElement ce : elem) {
				String resStr = null;
				if ("text()".equals(pp.attrib)) {
					resStr = ce.getText();
				} else {
					resStr = ce.getAttribute(pp.attrib);
				}
				if (resStr == null) continue;
				resStr = pp.postproc.process(resStr);
				if (resStr == null) continue;
				result.add(resStr);
			}
		}

		return result.toArray(new String[0]);
	}
}
