package de.kiwiwings.gccom.ListingParser.postfix;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.kiwiwings.gccom.ListingParser.json.ParseParams.ParseProc;

public class BBCode implements ParseProc {

	private static Transformer myTransformer;
	private static final String bbcodemap[][] = {
		// <regex>, <replace>(, <repeat>)
		{ "&amp;", "&" },
		{ "&gt;", ">" },
		{ "\\r", "" },
		{ "( *\\n){2,}", "\n\n" },
		{ "\\[img\\].*?(?:signal/|icon_smile)([^.]*).gif\\[/img\\]", ":$1:" },
		{ ":_big:", "[:D]" },
		{ ":_cool:", "[8D]" },
		{ ":_blush:", "[:I]" },
		{ ":_tongue:", "[:P]" },
		{ ":_evil:", "[}:)]" },
		{ ":_wink:", "[;)]" },
		{ ":_clown:", "[:o)]" },
		{ ":_blackeye:", "[B)]" },
		{ ":_8ball:", "[8]" },
		{ ":_sad:", "[:(]" },
		{ ":_shy:", "[8)]" },
		{ ":_shock:", "[:O]" },
		{ ":_angry:", "[:(!]" },
		{ ":_dead:", "[xx(]" },
		{ ":_sleepy:", "[|)]" },
		{ ":_kisses:", "[:X]" },
		{ ":_approve:", "[^]" },
		{ ":_dissapprove:", "[V]" },
		{ ":_question:", "[?]" },
		{ ":mad:", ":angry:" },
		{ ":big_smile:", ":grin:" },
		{ ":shock:", ":shocked:" },
		{ ":ohh:", ":yikes:" },
		{ ":bad_boy_a:", ":bad:" },
		{ "::", "[:)]" },
	};

	private boolean rot13 = false;
	
	public BBCode(boolean rot13) {
		this.rot13 = rot13;
	}
	
	
	@Override
	public String process(Object input) throws Exception {
		assert(input instanceof Element);
		Element base = (Element)input;

		if (!base.hasChildNodes()) return "";
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().
			parse(new InputSource(new StringReader("<bbcode/>")));
		Element elem = doc.getDocumentElement();
		
		NodeList nodeList = base.getChildNodes();
		
		for (int i=0; i<nodeList.getLength(); i++) {
			elem.appendChild(doc.importNode(nodeList.item(i), true));
		}

		if (myTransformer == null) {
			TransformerFactory tfact = TransformerFactory.newInstance();
			File f = new File("src/main/resources/bbcode-conv.xslt");
			myTransformer = tfact.newTransformer(new StreamSource(f));
		}		
		
		StringWriter sw = new StringWriter();
		myTransformer.transform(new DOMSource(elem), new StreamResult(sw));

		String htmlstr = sw.toString();
		
		for (int i=0; i<bbcodemap.length; i++) {
			Pattern regex = Pattern.compile(bbcodemap[i][0]);
			String replace = bbcodemap[i][1];
			boolean repeat = (bbcodemap[i].length == 3 && Boolean.valueOf(bbcodemap[i][2]));
			boolean found = false;
			do {
				Matcher matcher = regex.matcher(htmlstr);
				if (found = matcher.find()) {
					htmlstr = matcher.replaceAll(replace);
				}
			}
			while (repeat && found);
		}
		
		htmlstr = htmlstr.trim();
		if (rot13) htmlstr = rot13(htmlstr); 
		return htmlstr;
	}

	String rot13(String encoded) {
		StringBuffer decoded = new StringBuffer();
		boolean decoding = true;
		for (int i=0; encoded != null && i<encoded.length(); i++) {
			char ch = encoded.charAt(i);
			if (('a'<=ch && ch<='m') || ('A'<=ch && ch<='M')) {
				if (decoding) ch += 13;
			} else if (('n'<=ch && ch<='z') || ('N'<=ch && ch<='Z')) {
				if (decoding) ch -= 13;
			} else if (ch == '[') {
				decoding = false;
			} else if (ch == ']') {
				decoding = true;
			}
			decoded.append(ch);
		}
		return decoded.toString();
	}
}
