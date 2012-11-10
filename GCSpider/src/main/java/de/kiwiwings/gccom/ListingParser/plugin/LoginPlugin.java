package de.kiwiwings.gccom.ListingParser.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class LoginPlugin implements SpiderPlugin {
	CookieStore cookieStore;
	Gson gson;
	URL sessionProps;
	
	public void execute(SpiderContext ctx) throws Exception {
		SpiderConfig config = ctx.getConfig(); 
		if (config.getDebugLastFetched()) return;

		gson = new GsonBuilder().create();
		cookieStore = (CookieStore)ctx.getHttpContext().getAttribute("http.cookie-store");
		sessionProps = ConfigPlugin.findResource((String)config.get("login.cookies"));
		
		if (!initCookies(ctx)) {
			doLogin(ctx);
			updateCookies(ctx);
		}
		
	}
	
	private boolean initCookies(SpiderContext ctx) throws Exception {
		if (sessionProps == null) return false;
			
		Properties props = new Properties();
		InputStream is = sessionProps.openStream();
		props.load(is);
		is.close();
		
		for (Object propObj : props.values()) {
			String json = propObj.toString();
			BasicClientCookie cookie = gson.fromJson(json, BasicClientCookie.class);
			Date expiryDate = cookie.getExpiryDate();
			if (expiryDate != null && new Date().before(expiryDate)) {
				cookieStore.addCookie(cookie);
			}
		}
		
		return (cookieStore.getCookies().size() > 0);
	}

	private void updateCookies(SpiderContext ctx) throws Exception {
		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies.isEmpty()) return;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 30);
		
		Properties props = new Properties();
		int cookieId = 0;
		for (Cookie c : cookies) {
			Date expiryDate = c.getExpiryDate();
			if (expiryDate == null) {
				// let session scoped cookies live a bit longer
				((BasicClientCookie)c).setExpiryDate(cal.getTime());
			}
			props.setProperty("cookie"+(cookieId++), gson.toJson(c));
		}

		FileOutputStream fos = new FileOutputStream(new File(sessionProps.toURI()));
		props.store(fos, "File is automatically maintained ... don't edit it!");
		fos.close();
	}
	
	private void doLogin(SpiderContext ctx) throws Exception {
		SpiderConfig config = ctx.getConfig();
		
		HttpRequestBase httpCall = new HttpGet(config.getLoginUrl());
		ctx.updateFormParams(httpCall);

		String user = config.getProperty("login.user");
		String pass = config.getProperty("login.pass");
		assert(user != null && pass != null);
		
		ctx.replaceFormParam("ctl00$ContentBody$tbUsername", user);
		ctx.replaceFormParam("ctl00$ContentBody$tbPassword", pass);
		ctx.replaceFormParam("ctl00$ContentBody$cbRememberMe", "on");
		ctx.replaceFormParam("ctl00$ContentBody$btnSignIn", "Login");
//		ctx.replaceFormParam("__EVENTTARGET", "");
//		ctx.replaceFormParam("__EVENTARGUMENT", "");

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(ctx.getFormParams(), "UTF-8");
		httpCall = new HttpPost(config.getLoginUrl());
		((HttpPost)httpCall).setEntity(entity);
		ctx.updateFormParams(httpCall);
	}
	
}
