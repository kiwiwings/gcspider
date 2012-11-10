package de.kiwiwings.gccom.ListingParser.plugin;

import java.util.Locale;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.xalan.extensions.GCNamespaceContext;
import org.apache.xalan.extensions.GCXPathFunction;

import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class InitContextPlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(new GCNamespaceContext());
		xpath.setXPathFunctionResolver(new GCXPathFunction());
		ctx.setXpath(xpath);

		HttpClient hc = new DefaultHttpClient();
		ctx.setHttpClient(hc);

		HttpContext hctx = new BasicHttpContext();
		hctx.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
		ctx.setHttpContext(hctx);
		
		Locale.setDefault(Locale.US);
	}
}
