package de.kiwiwings.gccom.ListingParser.parser;

import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JSoupParser implements CommonParser {
	Document doc;

	static class JSoupElement implements CommonElement {
		Element element;
		
		JSoupElement(Element element) {
			this.element = element;
		}
		
		public String getAttribute(String name) {
			return element.attr(name);
		}
		
		public String getText() {
			return element.ownText();
		}
		
	}
	
	public JSoupParser(File input) throws Exception {
		doc = Jsoup.parse(input, "ISO-8859-1");
	}
	
	public CommonElement[] selectElements(String select) throws Exception {
		Elements elist = doc.select(select);
		
		CommonElement[] clist = new CommonElement[elist.size()];
		
		for (int i=0; i<clist.length; i++) {
			clist[i] = new JSoupElement(elist.get(i));
		}
		
		return clist;
	}
	
	public CommonElement selectElement(CommonElement base, String select) throws Exception {
		Elements elist = ((JSoupElement)base).element.select(select);

		if (elist.isEmpty()) return null;
		
		return new JSoupElement(elist.get(0));
	}
}
