package de.kiwiwings.gccom.ListingParser.parser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class JSoupParser extends CommonParser implements SpiderPlugin {
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
			if ("script".equals(element.nodeName())) {
				return element.data();
			} else {
				return element.ownText();
			}
		}
		
		public org.w3c.dom.Element getTree() throws Exception {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document dom = db.getDOMImplementation().createDocument(null, "bbcode", null);
			org.w3c.dom.Element root = dom.getDocumentElement();
			
			copy(element,root);
			
			return root;
		}
	}

	@Override
	public void execute(SpiderContext ctx) throws Exception {
		SpiderConfig config = ctx.getConfig();
		InputStream pageStream = ctx.getPageStream();
		if (config.getDebug()) {
			final String responseRaw = config.getResponseRawFile();
			FileOutputStream fos = new FileOutputStream(responseRaw);
			byte buf[] = new byte[1024];
			for (int readBytes; (readBytes = pageStream.read(buf)) != -1; fos.write(buf, 0, readBytes));
			fos.close();
			pageStream = new FileInputStream(responseRaw);
		}

		doc = Jsoup.parse(pageStream, ctx.getPageEncoding(), "");
	}
	
	public CommonElement[] selectElements(String select) throws Exception {
		return selectElements(null, select);
	}

	public CommonElement[] selectElements(CommonElement base, String select) throws Exception {
		Element el = (base == null) ? doc : ((JSoupElement)base).element;
		Elements elist = el.select(select);
		
		CommonElement[] clist = new CommonElement[elist.size()];
		
		for (int i=0; i<clist.length; i++) {
			clist[i] = new JSoupElement(elist.get(i));
		}
		
		return clist;
	}
	
	public CommonElement selectElement(CommonElement base, String select, int index) throws Exception {
		Element el = (base == null) ? doc : ((JSoupElement)base).element;
		Elements elist = el.select(select);

		if (elist.isEmpty()) return null;
		
		return new JSoupElement(elist.get(index));
	}

	@Override
	public boolean hasDocument() {
		return doc != null;
	}

	public void setDocument(Document doc) {
		this.doc = doc;
	}
	
	static void copy(org.jsoup.nodes.Element fromParent, org.w3c.dom.Element toParent) {
		for (org.jsoup.nodes.Attribute fromAttr : fromParent.attributes()) {
			toParent.setAttribute(fromAttr.getKey(), fromAttr.getValue());
		}

		org.w3c.dom.Document toDoc = toParent.getOwnerDocument();
		
		for (org.jsoup.nodes.Node fromChild : fromParent.childNodes()) {
			if (fromChild instanceof org.jsoup.nodes.Comment) {
				org.jsoup.nodes.Comment fromComm = (org.jsoup.nodes.Comment)fromChild;
				org.w3c.dom.Comment toComm = toDoc.createComment(fromComm.getData());
				toParent.appendChild(toComm);
			} else if (fromChild instanceof org.jsoup.nodes.DataNode) {
				org.jsoup.nodes.DataNode fromData = (org.jsoup.nodes.DataNode)fromChild;
				org.w3c.dom.CDATASection toData = toDoc.createCDATASection(fromData.getWholeData());
				toParent.appendChild(toData);
			} else if (fromChild instanceof org.jsoup.nodes.Element) {
				org.jsoup.nodes.Element fromElem = (org.jsoup.nodes.Element)fromChild;
				org.w3c.dom.Element toElem = toDoc.createElement(fromElem.nodeName());
				copy(fromElem,toElem);
				toParent.appendChild(toElem);
			} else if (fromChild instanceof org.jsoup.nodes.TextNode) {
				org.jsoup.nodes.TextNode fromText = (org.jsoup.nodes.TextNode)fromChild;
				String text = fromText.getWholeText();
				text = text.replace("\r", "");
				text = text.trim();
				org.w3c.dom.Text toText = toDoc.createTextNode(text);
				toParent.appendChild(toText);
			} else if (fromChild instanceof org.jsoup.nodes.XmlDeclaration) {
				org.jsoup.nodes.XmlDeclaration fromXmlDec = (org.jsoup.nodes.XmlDeclaration)fromChild;
				String wholeDec = fromXmlDec.getWholeDeclaration().trim();
				String name="", value="";
				if (wholeDec.contains(" ")) {
					int idx = wholeDec.indexOf(' ');
					name = wholeDec.substring(0,idx);
					value = wholeDec.substring(idx+1);
				} else {
					name = wholeDec;
				}
				org.w3c.dom.ProcessingInstruction toXmlPI = toDoc.createProcessingInstruction(name, value);
				toParent.appendChild(toXmlPI);
			}
		}
	}
}
