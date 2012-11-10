package de.kiwiwings.gccom.ListingParser.plugin;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class JTidyPlugin implements SpiderPlugin {
	private final Tidy tidy;
	private final DocumentBuilder docBuilder;
	
	public JTidyPlugin() throws Exception {
		tidy = new Tidy();
		tidy.setForceOutput(true);
		tidy.setXmlOut(true);
		tidy.setMakeClean(true);
		tidy.setIndentContent(false);
		tidy.setIndentAttributes(false);
		tidy.setWrapAttVals(false);
		tidy.setWraplen(800);
		tidy.setAltText("foobaa");
		tidy.setNumEntities(true);
		tidy.setDocType("omit");
		tidy.setQuoteMarks(true);
		tidy.setAsciiChars(false);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	public void execute(SpiderContext ctx) throws Exception {
		SpiderConfig myConfig = ctx.getConfig();
		
		Reader response;
		InputStream pageStream = ctx.getPageStream();
		if (myConfig.getDebug()) {
			final String responseRaw = myConfig.getResponseRawFile();
			FileOutputStream fos = new FileOutputStream(responseRaw);
			byte buf[] = new byte[1024];
			for (int readBytes; (readBytes = pageStream.read(buf)) != -1; fos.write(buf, 0, readBytes));
			fos.close();
			response = new InputStreamReader(new FileInputStream(responseRaw), ctx.getPageEncoding());
		} else {
			response = new InputStreamReader(pageStream, ctx.getPageEncoding());
		}
		
		tidy.setInputEncoding(ctx.getPageEncoding());
		tidy.setOutputEncoding(ctx.getPageEncoding());
		
		if (ctx.getConfig().getDebug()) {
			tidy.setErrout(new PrintWriter(new FileWriter(ctx.getConfig().getJTidyErrorFile())));	
		} else {
			tidy.setQuiet(true);
			tidy.setErrout(new PrintWriter(new Writer() {
				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {}
				
				@Override
				public void flush() throws IOException {}
				
				@Override
				public void close() throws IOException {}
			}));
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		tidy.parse(response, bos);
		response.close();
		
		String content= bos.toString(ctx.getPageEncoding());
		content=content.replaceAll("alt=\"([^\"]+)\" title=\"[^\"]+\" alt=\"[^\"]+\"", "alt=\"\\1\" title=\"\\1\""); // remove double alt attributes
		content=content.replaceAll("(\\r\\n){2,}", "\n"); // remove double line breaks
		content=content.replaceAll("<!\\[[^\\]]*\\]>", ""); // remove pseudo html if-then-else
		content=content.replaceAll("&#(1[5-9]|2[0-9]);", ""); // remove illegal character entities
		content=content.replaceAll("&&", "");
		content=content.replaceAll("(?s)<style.+?/style>", "");
		content=content.replaceAll(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
		content=content.replaceAll(">[ \t]+<","><"); // remove whitespace element content

		// language dependend conversions
		content=content.replaceAll("Versteckt am: :", "Hidden :");
		content=content.replaceAll("Event Date:", "Hidden :");
		content=content.replaceAll("An Event cache", "A cache");
		content=content.replaceAll("A Cache", "A cache");
		
		if (ctx.getConfig().getDebug()) {
			FileOutputStream fos = new FileOutputStream(ctx.getConfig().getResponseXmlFile());
			OutputStreamWriter osw = new OutputStreamWriter(fos, ctx.getPageEncoding());
			osw.write(content);
			osw.close();
		}
		
		Document currentDoc = docBuilder.parse(new InputSource(new StringReader(content)));
		
		ctx.setPageContent(currentDoc);
	}
}
