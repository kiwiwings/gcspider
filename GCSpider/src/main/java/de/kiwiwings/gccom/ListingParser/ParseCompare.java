package de.kiwiwings.gccom.ListingParser;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.kiwiwings.gccom.ListingParser.parser.CommonElement;
import de.kiwiwings.gccom.ListingParser.parser.JSoupParser;
import de.kiwiwings.gccom.ListingParser.postfix.Html2BBCode;

public class ParseCompare {

	public static void main3(String[] args) throws Exception {
		Html2BBCode bbcode = new Html2BBCode();
		String str = "<font size=\"6\">Cacherteam weckt Dose aus dem Dornröschenschlaf </font><br /><br /><br /><br /><i>(GC)</i> - <b>Wie uns aus zuverlässiger Quelle gemeldet wird, wurde am heutigen Mittwoch die Dose nach über einen Jahr wieder geöffnet. Das Team - bestehend aus IngRu, L_X_B, zeras und grmblfx - wagte sich erfolgreich an die zehnte Inn's Bruck.</b><br /><br /><br />\"Aufgefallen ist uns die Herausforderung, da die Dose seit über einem Jahr nicht mehr gelogged wurde - Grund genug hier mal nach dem Rechten zu sehen\" so ein Mitglied des Teams.  <br /><br />Die Idee war alle möglichen Zugänge (T5, Otto-Normal-D5, Sissy-Zugang) zu knacken. <br /><br />Wer Rudolf von IngRu kennt, der weiss dass er sich sofort an die Lösung des D5 Teils gewagt hat. Für die Lösung brauchte er mehrere Stunden (oder Tage???) - auch die Anzahl seiner grauen Haare hat dabei merklich zugenommen. Aber letztendlich hatte er die diversen (fiesen) Rätsel doch geknackt. <b>Respekt.</b> \"Das waren mal wirklich harte D5 Rätsel\" so der überglückliche King of Mysteries, nachdem er die Lösung hatte. <br /><br />Die anderen bevorzugten dann doch die T5 Variante (Muskelkraft statt Hirnschmalz war angesagt) - so eine schöne Brücke hat man ja schliesslich auch nicht alle Tage. Was folgte war eine Materialschlacht sonders gleichen. Ein Kletterausrüster hätte wahrscheinlich seine wahre Freude daran gehabt. Geklettert wurde mit starrem und umlaufenden Seil - das Ergebnis war jedes Mal das gleiche: Sie waren \"drin\". Die Koordinaten konnten gut gefunden werden - natürlich wurde auch der Rest der Bruecke untersucht.  <br /><br />Natürlich haben die T5er noch den Sissy Eingang geöffnet - damit waren (fast) alle vom Owner beschriebenen Zugänge geprüft und für gut befunden.<br /><br />Vielen Dank für dieses aussergewöhnliche Ereignis - sowohl ans Team als auch an den Owner. War super klasse!<br />Grüsse,<br /><b><font color=\"green\">g<i>r</i>m<i>b</i>l<i>f</i>x</font></b><br /><br />in: Natürlich einen <b>dicken</b> verdienten Favoriten Punkt.<br /><br />Bilder folgen in Kürze - eh klar...";
		str = bbcode.process(str);
		System.out.println(str);
	}
	
	public static void main(String[] args) throws Exception {
		File input = new File("src/test/debug/response - detail2.raw");
		Document doc = Jsoup.parse(input, "UTF-8", "");

		JSoupParser jsp = new JSoupParser();
		jsp.setDocument(doc);
		
		CommonElement ce = jsp.selectElement(null, "#ctl00_ContentBody_ShortDescription", 0);
		
		org.w3c.dom.Element el = ce.getTree();
		
		Html2BBCode bb = new Html2BBCode();
		String str = bb.process(el);
		
		System.out.println(str);
	}
	
	public static void main2(String[] args) throws Exception {
		File input = new File("src/test/debug/response - detail2.raw");
		Document doc = Jsoup.parse(input, "UTF-8", "");

		Elements eList = doc.select("#ctl00_ContentBody_ShortDescription");
		assert(eList.size() != 1);
		org.jsoup.nodes.Element el = eList.get(0);
		
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document dom = db.getDOMImplementation().createDocument(null, "bbcode", null);
		org.w3c.dom.Element root = dom.getDocumentElement();
		
		copy(el,root);
		
		Properties prop = OutputPropertiesFactory.getDefaultMethodProperties(org.apache.xml.serializer.Method.XML);
		prop.setProperty("omit-xml-declaration", "yes");
		Serializer ser = SerializerFactory.getSerializer(prop);
		StringWriter sw = new StringWriter(); 
		ser.setWriter(sw);
		ser.asDOMSerializer().serialize(root);
		String str = sw.toString();
		System.out.println(str);
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
