package org.apache.xalan.extensions;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.lib.ExsltBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class GCConversions extends ExsltBase {

	private static DateFormat parseMediumFormat =
		new SimpleDateFormat("dd MMM yy", Locale.US);
	private static DateFormat parseShortFormat =
		new SimpleDateFormat("MM/dd/yyyy", Locale.US);
	private static DateFormat parseLongFormat =
		new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US);
	private static DateFormat parseMediumFormat2 =
		new SimpleDateFormat("MMMM dd", Locale.US);
	private static DateFormat parseMediumFormat3 =
		new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
	
	public static String calcDate(String datestr) throws ParseException {
		if (datestr != null) {
			datestr = datestr.trim();
		}
		
		if (datestr == null || "".equals(datestr)) {
			System.out.println("calcDate received empty date string.");
			return null;
		}
		
		if (datestr.endsWith("*")) {
			datestr = datestr.substring(0, datestr.length()-1).trim();
		}
		
		Date d;
		if (datestr.contains("ago")) {
			int days = Integer.parseInt(datestr.substring(0, datestr.indexOf(' ')));
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -days);
			cal.add(Calendar.HOUR, -10);
			d = cal.getTime();
		} else if ("Today".equals(datestr)) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -10);
			d = cal.getTime();
		} else if ("Yesterday".equals(datestr)) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, -(10+24));
			d = cal.getTime();
		} else if (datestr.indexOf('/') != -1){
			d = parseShortFormat.parse(datestr);
		} else if (datestr.startsWith("Monday")
			|| datestr.startsWith("Tuesday")
			|| datestr.startsWith("Wednesday")
			|| datestr.startsWith("Thursday")
			|| datestr.startsWith("Friday")
			|| datestr.startsWith("Saturday")
			|| datestr.startsWith("Sunday")) {
			d = parseLongFormat.parse(datestr);
		} else if (datestr.startsWith("January")
				|| datestr.startsWith("February")
				|| datestr.startsWith("March")
				|| datestr.startsWith("April")
				|| datestr.startsWith("May")
				|| datestr.startsWith("June")
				|| datestr.startsWith("July")
				|| datestr.startsWith("August")
				|| datestr.startsWith("September")
				|| datestr.startsWith("October")
				|| datestr.startsWith("November")
				|| datestr.startsWith("December")
		) {
			if (datestr.indexOf(',') == -1) {
				d = parseMediumFormat2.parse(datestr);
				Calendar cal = Calendar.getInstance(Locale.US);
				int year = cal.get(Calendar.YEAR);
				cal.setTime(d);
				cal.set(Calendar.YEAR, year);
				d = cal.getTime();
			} else {
				d = parseMediumFormat3.parse(datestr);
			}
		} else {
			d = parseMediumFormat.parse(datestr);
		}
		
		return SpiderContext.fullDateFormat.format(d);
	}

	public static String decodeUriComponent(String uriComp) throws UnsupportedEncodingException {
		return URLDecoder.decode(uriComp, "UTF8");
	}
	
	private static Transformer myTransformer;
	
	static final String bbcodemap[][] = {
		// <regex>, <replace>(, <repeat>)
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
		{ "<table.*", "" },
//		{ "<small><a href=\"log.aspx.*</a></small>", "" },
//		{ "<span style=\"font-size: 70%\">(.*)</span>", "[size=1]$1[/size]" },
//		{ "<span style=\"color: (black|blue|red|purple|pink|orange|yellow|green|white)\">(?!.*?span style=\"color:)(.*?)(</span>|<span style=\"color: black\">)", "[$1]$2[/$1]", "true" },
////			{ "<span style=\"color: (black|blue|red|purple|pink|orange|yellow|green|white)\">(.*)(</span>|<span style=\"color: black\">)", "[$1]$2[/$1]" },
//		{ "<img.*src=\"([^\"]+)\"[^>]*>", "[img]$1[/img]" },
//		{ "<a href=\"([^\"]*)\" rel=\"nofollow\" target=\"_blank\">visit link</a>", "[url]$1[/url]" },
//		{ "<a href=\"([^\"]*)\" rel=\"nofollow\" target=\"_blank\">(.*)</a>", "[url=$1]$2[/url]" },
//		{ "<a[^>]*href=\"([^\"]+)\"[^>]*>(.*)</a>", "[url=$1]$2[/url]" },
//		{ "\\s*</p>\\s*", "\n" },
//		{ "\\s*<p>\\s*", "\n\n" },
//		{ "\\s*<br/?>\\s*", "\n" },
//		{ "\\[This entry.*\\]", "" },
		{ "&gt;", ">" },
		{ "&amp;", "&" },
//		{ "<(/?[ib])>", "[$1]" },
//		{ "&gt;", ">" },
		{ "[\n\r]*$", "" },
//			{ "::", "" },
		{ "(\\r?\\n){3,}", "\n\n" },
	};
	
	public static String html2bbcode(NodeList nodeList)
	throws TransformerException, IOException, SAXException, ParserConfigurationException {
		if (nodeList.getLength() == 0) return "";
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().
			parse(new InputSource(new StringReader("<bbcode/>")));
		Element elem = doc.getDocumentElement();
		
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
		return htmlstr.trim();
	}
	
	public static String rot13(String encoded) {
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
	
	
	public static String yesno(String yesno) {
		if (yesno == null || "".equals(yesno)) return "";
		if (yesno.indexOf("-yes.") > -1) return "1";
		if (yesno.indexOf("-no.") > -1) return "0";
		return "";
	}
	
	static String map[][] = {
		{ "â€\"", "–" },
		{ "Ã\"",  "Ä" },
		{ "Ã-",   "Ö" },
//		{ "â€",   "”" },
		{ "ö",   "ö" },
		{ "ä",   "ä" },
		{ "ü",   "ü" },
		{ "Ö",   "Ö" },
		{ "Ä",   "Ä" },
		{ "Ü",   "Ü" },
		{ "ß",   "ß" },
		{ "â'¬", "€" },
		{ "Â°",  "°" },
//		{ "°",   "°" },
		{ " ",   " " }, // geschützter Umbruch
		{ "ù",   "ù" },
		{ "Å ",  "Š" },
	};
	
	public static String fixUtf(String isoUtf) throws UnsupportedEncodingException {
		if (isoUtf == null) {
			return "";
		}
		
		if (isoUtf.startsWith("org.apache.xml.dtm.ref.DTMNodeList")) {
			return "";
		}

		for (int i=0; i<map.length; i++) {
			if (isoUtf.indexOf(map[i][0]) == -1) continue;
			isoUtf = isoUtf.replaceAll(map[i][0], new String(map[i][1].getBytes("UTF-8")));
		}
		
		byte buf[] = isoUtf.getBytes();
		return new String(buf, "UTF-8");
	}
	
	public static String find(String input, String regex) {
		if (input == null || "".equals(input)) return "";
		Matcher m = Pattern.compile(regex).matcher(input);
		if (!m.find()) return "";
		return (m.groupCount() > 0) ? m.group(1) : "";
	}
}
