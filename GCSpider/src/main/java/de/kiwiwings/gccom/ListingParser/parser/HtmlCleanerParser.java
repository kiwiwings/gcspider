package de.kiwiwings.gccom.ListingParser.parser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xalan.extensions.GCNamespaceContext;
import org.apache.xalan.extensions.GCXPathFunction;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlCleanerSerializer;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;
import de.kiwiwings.gccom.ListingParser.plugin.SpiderPlugin;

public class HtmlCleanerParser extends CommonParser implements SpiderPlugin {

	private HtmlCleaner cleaner;
	private Document currentDoc;
	private XPath xpath;
	
	private Map<String,XPathExpression> xpathCache = new HashMap<String,XPathExpression>();

	static class HtmlCleanerElement implements CommonElement {
		Element element;
		
		HtmlCleanerElement(Element element) {
			this.element = element;
		}
		
		public String getAttribute(String name) {
			return convertReservedEnt(element.getAttribute(name));
		}
		
		public String getText() {
			StringBuffer buf = new StringBuffer();
			NodeList nl = element.getChildNodes();
			for (int i=0; i<nl.getLength(); i++) {
				Node n = nl.item(i);
				switch (n.getNodeType()) {
					case Node.TEXT_NODE:
					case Node.CDATA_SECTION_NODE:
						buf.append(n.getNodeValue());
						break;
				}
			}
			return convertReservedEnt((buf.length()==0) ? null : buf.toString());
		}
		
		private String convertReservedEnt(String input) {
			if (input == null) return null;
			String output = input
				.replace("&#34;", "\"")
				.replace("&#38;", "&")
				.replace("&#39;", "'")
				.replace("&#60;", "<")
				.replace("&#62;", ">");
			return output;
		}
		
		public Element getTree() {
			return element;
		}
	}
	
	
	public HtmlCleanerParser() throws Exception {
		cleaner = new HtmlCleaner();
		
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		xpath.setNamespaceContext(new GCNamespaceContext());
		xpath.setXPathFunctionResolver(new GCXPathFunction());
	}	

	@Override
	public void execute(SpiderContext ctx) throws Exception {
		SpiderConfig myConfig = ctx.getConfig();
		
		InputStream response;
		InputStream pageStream = ctx.getPageStream();
		if (myConfig.getDebug()) {
			final String responseRaw = myConfig.getResponseRawFile();
			FileOutputStream fos = new FileOutputStream(responseRaw);
			byte buf[] = new byte[1024];
			for (int readBytes; (readBytes = pageStream.read(buf)) != -1; fos.write(buf, 0, readBytes));
			fos.close();
			pageStream.close();
			response = new FileInputStream(responseRaw);
		} else {
			response = pageStream;
		}
		
		
		CleanerProperties props = cleaner.getProperties();
		TagNode tagNode = cleaner.clean(response, ctx.getPageEncoding());
		response.close();
		
		if (ctx.getConfig().getDebug()) {
			new SimpleXmlSerializer(props).writeToFile(tagNode, ctx.getConfig().getResponseXmlFile(), ctx.getPageEncoding());
		}

		currentDoc = new HtmlCleanerSerializer(props).createDOM(tagNode);
	}

	@Override
	public CommonElement[] selectElements(String select) throws Exception {
		XPathExpression inputSel = getXpathExpression(select);

		NodeList ns = (NodeList)inputSel.evaluate(currentDoc, XPathConstants.NODESET);

		CommonElement clist[] = new CommonElement[ns.getLength()];
		for (int i=0; i<clist.length; i++) {
			clist[i] = new HtmlCleanerElement((Element)ns.item(i));
		}
		
		return clist;
	}

	@Override
	public CommonElement[] selectElements(CommonElement base, String select) throws Exception {
		Element baseEl = (base == null)
				? currentDoc.getDocumentElement()
				: ((HtmlCleanerElement)base).element; 
		
		XPathExpression inputSel = getXpathExpression(select);
		NodeList ns = (NodeList)inputSel.evaluate(baseEl, XPathConstants.NODESET);

		CommonElement clist[] = new CommonElement[ns.getLength()];
		for (int i=0; i<clist.length; i++) {
			clist[i] = new HtmlCleanerElement((Element)ns.item(i));
		}
		
		return clist;
	}
	
	@Override
	public CommonElement selectElement(CommonElement base, String select, int index)
			throws Exception {
		Element baseEl = (base == null)
			? currentDoc.getDocumentElement()
			: ((HtmlCleanerElement)base).element; 

		XPathExpression xpe = getXpathExpression(select);
		NodeList value = (NodeList)xpe.evaluate(baseEl, XPathConstants.NODESET);
		
		return (value.getLength() <= index)
			? null
			: new HtmlCleanerElement((Element)value.item(index));
	}


	XPathExpression getXpathExpression(String xpathstr) throws XPathExpressionException {
		XPathExpression xpe = xpathCache.get(xpathstr);
		if (xpe == null) {
			xpe = xpath.compile(xpathstr);
			xpathCache.put(xpathstr, xpe);
		}
		return xpe;
	}

	@Override
	public boolean hasDocument() {
		return currentDoc != null;
	}
}
