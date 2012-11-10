package de.kiwiwings.gccom.ListingParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.kiwiwings.gccom.ListingParser.json.ParseParams;
import de.kiwiwings.gccom.ListingParser.parser.CommonElement;
import de.kiwiwings.gccom.ListingParser.parser.CommonParser;
import de.kiwiwings.gccom.ListingParser.parser.JSoupParser;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class ParseCompare implements SpiderPlugin {

	List<Map<String,String>> dataSet = new ArrayList<Map<String,String>>();
	CommonParser cp;
	SpiderContext ctx;
	
	public void execute(SpiderContext ctx) throws Exception {
		this.ctx = ctx;
		
		dataSet.clear();
		
		SpiderConfig config = ctx.getConfig();
		
//		Document doc = ctx.getPageContent(); 
//		if (doc == null || ctx.getPageCount() == -1) return;

		File input = new File("src/test/htmlparse-compare/response.raw");
		cp = new JSoupParser(input);

		String entry = ctx.getConfig().getProperty("parse.list.rowsel");
		ParseParams pp = ParseParams.fromString(entry);
		
		CommonElement elems[] = cp.selectElements(pp.select);
		for (int i=0; i<elems.length; i++) {
			dataSet.add(new HashMap<String,String>());
		}

		for (String column : ctx.getSchema().keySet()) {
			String xpathStr = (String)config.get("parse.list."+column);
			if (xpathStr == null || "".equals(xpathStr)) continue;

			pp = ParseParams.fromString(xpathStr);
			
			for (int i=0; i<elems.length; i++) {
				String value = readOneListRecord(elems[i], pp);
				dataSet.get(i).put(column, value);
			}
		}

		for (Map<String,String> dataEntry : dataSet) {
			putDatabaseEntry(ctx, dataEntry);
		}
	}
	
	protected String readOneListRecord(CommonElement line, ParseParams pp) throws Exception {
		
		CommonElement elem = cp.selectElement(line, pp.select);
		
		StringBuffer sb = new StringBuffer();
		String result = "";
		if ("exist()".equals(pp.attrib)) {
			result = (elem == null ? "false" : "true");
		} else {
			assert(pp.attrib != null);
			if ("text()".equals(pp.attrib)) {
				result = elem.getText();
			} else {
				result = elem.getAttribute(pp.attrib);
			}
			result = pp.postproc.process(sb);
		}

		return result;
	}

	protected void putDatabaseEntry(SpiderContext ctx, Map<String,String> entry) throws Exception {
		ctx.putDatabaseEntry(entry);
	}

	public void test() throws Exception {
		File input = new File("src/test/htmlparse-compare/response.raw");
		cp = new JSoupParser(input);

		String json =
			"{" +
			"select:\"img[alt=Premium Member Only Cache]\"" +
			",attrib:\"exist()\"" +
//			",postfix:\"exist\"" +
			"}";

		ParseParams pp = ParseParams.fromString(json);
		
		CommonElement eList[] = cp.selectElements("tr.TertiaryRow");
		for (CommonElement e : eList) {
			String result = readOneListRecord(e, pp);
			System.out.println(result);
		}
	}
	

	public static void main(String[] args) throws Exception {
		ParseCompare pc = new ParseCompare();
		pc.test();
	}
}
