package de.kiwiwings.gccom.ListingParser.postfix;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;

import de.kiwiwings.gccom.ListingParser.json.ParseParams.ParseProc;

public class Html2BBCode implements ParseProc {

	static final String bbcodemap[][] = {
		// <regex>, <replace>(, <repeat>)
		{ "&gt;", ">" },
		{ "&amp;", "&" },
		{ "[\n\r]*$", "" },
		{ "(?s)(\\r?\\n){2,}", "\n\n" },
		{ "(?s).*<body>(.*)</body>.*", "$1" },
		{ "(?m)^[ \\t]+","" },
		{ "(?m)[ \\t]+$","" },
	};
	
	
	@Override
	public String process(Object input) throws Exception {
		Source src;
		// if input is of type string, we assume xml wellformness
		// i.e. the string comes from the log elements which are in xhtml format
		if (input instanceof String) {
			src = new StreamSource(new StringReader("<bbcode>"+input+"</bbcode>"));
		} else if (input instanceof Element) {
			src = new DOMSource((Element)input);
		} else {
			return null;
		}

		URL xslt = Thread.currentThread().getContextClassLoader().getResource("bbcode-conv.xslt");
		TransformerFactory tfact = TransformerFactory.newInstance();
		Transformer myTransformer = tfact.newTransformer(new StreamSource(xslt.toExternalForm()));
		myTransformer.setOutputProperty("{http://xml.apache.org/xalan}line-separator", "\n");
		
		StringWriter sw = new StringWriter();
		myTransformer.transform(src, new StreamResult(sw));
	
		String htmlstr = sw.toString();
		
/*
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
*/
		htmlstr = htmlstr.trim();
		
		return htmlstr;
	}
}
