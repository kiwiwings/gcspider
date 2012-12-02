package de.kiwiwings.gccom.ListingParser.plugin;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class ConfigPlugin implements SpiderPlugin {
	String parserProps = "listingparser.htmlcleaner.properties";
	
	public void execute(SpiderContext ctx) throws Exception {
		SpiderConfig config = new SpiderConfig(); 
		URL listProps = findResource("gcuser.properties");
		InputStream is = listProps.openStream(); 
		config.load(is);
		is.close();
		listProps = findResource(parserProps);
		is = listProps.openStream(); 
		config.load(is);
		is.close();

		Pattern pat = Pattern.compile("#([a-zA-Z_0-9.]+)#");
		
		for (Map.Entry<Object, Object> me : config.entrySet()) {
			String val = (String)me.getValue();
			if (val == null) continue;
			StringBuffer valbuf = new StringBuffer();
			Matcher m;
			for (int loops = 0; (m = pat.matcher(val)).find(); loops++) {
				m.reset();
				valbuf.setLength(0);
				while (m.find()) {
					String match = (String)config.get(m.group(1));
					match = match.replace("\\", "\\\\");
					m.appendReplacement(valbuf, match);
				}
				m.appendTail(valbuf);
				val = valbuf.toString();
				if (++loops > 5) {
					throw new RuntimeException("possible configuration loop");
				}
			}
			
			me.setValue(val);
		}
		ctx.setConfig(config);
	}		

	public static URL findResource(String resourceName) throws MalformedURLException {
		File dirs[] = { 
			new File("."), new File("src/main/resources"), new File("src/test/resources")
		};
		
		for (File dir : dirs) {
			File f = new File(dir, resourceName);
			if (f.exists()) {
				return f.toURI().toURL();
			}
		}
		

		ClassLoader cloaders[] = {
			  Thread.currentThread().getContextClassLoader()
			, ConfigPlugin.class.getClassLoader()
		};
		
		URL resUrl = null;
		for (ClassLoader cl : cloaders) {
			resUrl = cl.getResource(resourceName);
			if (resUrl != null) break;
		}
		
		return resUrl;
	}

	public void setParserProps(String parserProps) {
		this.parserProps = parserProps;
	}
}
