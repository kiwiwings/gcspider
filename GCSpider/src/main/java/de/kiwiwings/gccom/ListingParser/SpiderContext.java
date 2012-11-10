package de.kiwiwings.gccom.ListingParser;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.kiwiwings.gccom.ListingParser.plugin.JTidyPlugin;

public class SpiderContext {
	public static final DateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	XPath xpath;
	Map<String,XPathExpression> xpathCache = new HashMap<String,XPathExpression>();
	SpiderConfig config;
	String datatable = null;
	String primaryKey = null;
	List<Map<String,String>> database = new ArrayList<Map<String,String>>();
	// changed entries
	Set<String> changedIdx = new HashSet<String>();
	// primary key (i.e. waypoint) index
	Map<String,Integer> pkIdx = new HashMap<String,Integer>();
	// record attribute to excel column mapping
	Map<String,Integer> schema = new HashMap<String,Integer>();
	// cache attribute extra infos, column name to info mapping
	List<CacheAttribute> cacheAttr;
	// http input stream
	InputStream pageStream;
	// http input stream encoding
	String pageEncoding = "UTF-8";
	Document pageContent;
	List<NameValuePair> formParams = new ArrayList<NameValuePair>();
	HttpClient httpClient;
	HttpContext httpContext;
	int pageCount = -1;
	// Result for user guid to forum id mapping
	List<Map<String,String>> userMappingTodos;
	// Result for hides waypoint todo (GCCode, Cache-Name)
	Map<String,Map<String,String>> hidesTodos;
	JTidyPlugin jTidy;
	
	public SpiderContext() throws Exception {
		jTidy = new JTidyPlugin();
	}
	
	public static enum NavigationState { error, last, next, skip };
	NavigationState navigationState = NavigationState.next;
	
	/**
	 * Adds or updates a database entry
	 * @param entry
	 * @return true, if entry was updated, false otherwise
	 * @throws Exception
	 */
	public boolean putDatabaseEntry(Map<String,String> entry) throws Exception {
		String pk = entry.get(primaryKey);
		if (pk == null || "".equals(pk)) {
			throw new Exception("no primary key defined");
		}
		boolean found;
		if (pkIdx.containsKey(pk)) {
			found = true;
			Map<String,String> oldEntry = database.get(pkIdx.get(pk));
			if (oldEntry != entry) {
				// check for instance equality
				oldEntry.putAll(entry);
			}
		} else {
			found = false;
			int newIdx = database.size();
			database.add(entry);
			pkIdx.put(pk, newIdx);
		}
		changedIdx.add(pk);
		return found;
	}

	public XPathExpression getXpathExpression(String xpathstr) throws XPathExpressionException {
		XPathExpression xpe = xpathCache.get(xpathstr);
		if (xpe == null) {
			xpe = xpath.compile(xpathstr);
			xpathCache.put(xpathstr, xpe);
		}
		return xpe;
	}

	public void replaceFormParam(String name, String value) {
		Iterator<NameValuePair> iter = formParams.iterator();
		while (iter.hasNext()) {
			if (iter.next().getName().equals(name)) iter.remove();
		}
		formParams.add(new BasicNameValuePair(name, value));
	}

	public void removeOtherFormParam(String names[]) {
		Iterator<NameValuePair> iter = formParams.iterator();
outer:	while (iter.hasNext()) {
			String name = iter.next().getName();
			for (int i=0; i<names.length; i++) {
				if (names[i].equals(name)) continue outer;
			}
			iter.remove();
		}
	}	
	
	public void requestData(HttpRequestBase method)
	throws Exception {
		SpiderConfig myConfig = getConfig();

		try { Thread.sleep(myConfig.getSleepTime()*1000); }
		catch (InterruptedException e) {}
		
		
		System.out.println(method.getMethod()+": "+method.getURI());
		if (method instanceof HttpPost) {
			HttpEntity ent = ((HttpPost)method).getEntity();
			if (ent instanceof UrlEncodedFormEntity) {
				System.out.print("Data: ");
				((UrlEncodedFormEntity)ent).writeTo(System.out);
				System.out.println();
			}
		}
		
		method.setHeader("User-Agent", myConfig.getUserAgent());

		HttpResponse resp = getHttpClient().execute(method, getHttpContext());
		HttpEntity ent1 = resp.getEntity();
		
		setPagestream(ent1.getContent());

		Header encodingHdr = ent1.getContentEncoding();
		Header contentType = ent1.getContentType();
		String encoding = "ISO-8859-1";
		if (encodingHdr != null && encodingHdr.getValue() != null) {
			encoding = encodingHdr.getValue();
		} else if (contentType != null && contentType.getValue() != null && contentType.getValue().indexOf("charset=")>0) {
			String ct = contentType.getValue();
			encoding = ct.substring(ct.indexOf("charset=")+8);
		} else {
			System.out.println("can't determine encoding.");
		}
		setPageEncoding(encoding);
	}
	
	public void updateFormParams(HttpRequestBase method)
	throws Exception {
		requestData(method);
		
		jTidy.execute(this);
		
		XPathExpression inputSel = getXpathExpression("//input");
		
		NodeList ns = (NodeList)inputSel.evaluate(getPageContent(), XPathConstants.NODESET);

		formParams.clear();
		for (int i=0; i<ns.getLength(); i++) {
			Element line = (Element)ns.item(i);
			String name = line.getAttribute("name");
			String value = line.getAttribute("value");
			if (name == null || "".equals(name) || value == null) continue;
			formParams.add(new BasicNameValuePair(name, value));
		}
	}

	public int getPageCount(boolean forceParsing) throws Exception {
		if (pageCount != -1 && !forceParsing) return pageCount;
		
		if (pageContent == null) return -1;
		
		XPathExpression inputSel = getXpathExpression((String)config.get("parse.list.pagecount"));
		Double pageCountD = (Double)inputSel.evaluate(pageContent, XPathConstants.NUMBER);
		if (pageCountD == null || pageCountD.isNaN()) return -1;
		return pageCountD.intValue();
	}
	
	public int getPageCount() throws Exception {
		return getPageCount(false);
	}	
	
	public String getPrimaryKeyValue(Map<String,String> entry) {
		String pk = entry.get(getPrimaryKey());
		return (pk == null) ? "" : pk;
	}
	
	public XPath getXpath() {
		return xpath;
	}
	public void setXpath(XPath xpath) {
		this.xpath = xpath;
	}
	public SpiderConfig getConfig() {
		return config;
	}
	public void setConfig(SpiderConfig config) {
		this.config = config;
	}
	public String getDatatable() {
		return datatable;
	}
	public void setDatatable(String datatable) {
		this.datatable = datatable;
	}
	public Map<String, Integer> getSchema() {
		return schema;
	}
	public Map<String, Integer> getPkIdx() {
		return pkIdx;
	}
	public String getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public InputStream getPageStream() {
		return pageStream;
	}

	public void setPagestream(InputStream pageStream) {
		this.pageStream = pageStream;
	}

	public String getPageEncoding() {
		return pageEncoding;
	}

	public void setPageEncoding(String pageEncoding) {
		this.pageEncoding = pageEncoding;
	}

	public Document getPageContent() {
		return pageContent;
	}

	public void setPageContent(Document pageContent) {
		this.pageContent = pageContent;
	}

	public List<NameValuePair> getFormParams() {
		return formParams;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}

	public NavigationState getNavigationState() {
		return navigationState;
	}

	public void setNavigationState(NavigationState listPageState) {
		this.navigationState = listPageState;
	}

	public List<Map<String, String>> getDatabase() {
		return database;
	}

	public Set<String> getChangedIdx() {
		return changedIdx;
	}

	public void setCacheAttr(List<CacheAttribute> cacheAttr) {
		this.cacheAttr = cacheAttr;
	}

	public List<CacheAttribute> getCacheAttr() {
		return cacheAttr;
	}

	public List<Map<String, String>> getUserMappingTodos() {
		return userMappingTodos;
	}

	public void setUserMappingTodos(List<Map<String, String>> userMappingTodos) {
		this.userMappingTodos = userMappingTodos;
	}

	public Map<String,Map<String,String>> getHidesTodos() {
		return hidesTodos;
	}

	public void setHidesTodos(Map<String,Map<String,String>> hidesTodos) {
		this.hidesTodos = hidesTodos;
	}
	
}
