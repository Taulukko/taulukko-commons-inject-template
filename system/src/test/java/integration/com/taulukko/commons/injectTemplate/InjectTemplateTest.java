package integration.com.taulukko.commons.injectTemplate;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.evon.injectTemplate.config.TemplateBean;
import com.evon.injectTemplate.config.TemplateConfig;

public class InjectTemplateTest {

	private static final Integer TOMCAT_PORT = 8182;
	private static final String URL_BASE = "http://localhost:" + TOMCAT_PORT
			+ "/";
	private static Tomcat tomcat = null;
	private static Thread thread = null;

	@BeforeClass
	public static void init() throws Exception {

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				String webappDirLocation = new File("").getAbsolutePath()
						+ "/src/main/resources/webapp";
				tomcat = new Tomcat();

				// The port that we should run on can be set into an environment
				// variable
				// Look for that variable and default to 8080 if it isn't there.
				String webPort = System.getenv("PORT");
				if (webPort == null || webPort.isEmpty()) {
					webPort = TOMCAT_PORT.toString();
				}

				tomcat.setPort(Integer.valueOf(webPort));

				try {
					Thread.sleep(1000);
					tomcat.addWebapp("/",
							new File(webappDirLocation).getAbsolutePath());

					System.out.println("configuring app with basedir: "
							+ new File(webappDirLocation).getAbsolutePath());

					tomcat.start();
					tomcat.getServer().await();
				} catch (ServletException | LifecycleException
						| InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();

		while (tomcat == null) {
			Thread.sleep(100);
		}

		String state = tomcat.getServer().getStateName();

		while (!state.equals("STARTED")) {
			Thread.sleep(100);
			state = tomcat.getServer().getStateName();
		}
	}

	@AfterClass
	public static void end() throws Exception {

		/***
		 * Uncomment to manually test urls:
		 * 
		 * URL_BASE or URL_BASE/jsps/clean
		 * 
		 * You have 30 seconds to test until goal package run (required use goal
		 * package maven to run this test) To increment this time, increment the
		 * sleepTime
		 * 
		 * long sleepTime = 30000; sleep(sleepTime);
		 * 
		 */

		long sleepTime = 0 * 30000;
		sleep(sleepTime);

		tomcat.stop();
	}

	@Test
	public void defaultTemplateTest() throws ClientProtocolException,
			IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(URL_BASE);
		HttpResponse httpresponse = client.execute(httpPost);
		HttpEntity entity1 = httpresponse.getEntity();
		String content = EntityUtils.toString(entity1);
		Assert.assertTrue(content.contains("Template:: Test"));
		testOrder(content, "//head script", "//page script");
		testOrder(content, "Template Content Head", "Test Content Root");
		testOrder(content, "Test Content Root", "Template Content Foot");
	}

	@Test
	public void cleanTemplateTest() throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(URL_BASE + "jsps/clean/");
		HttpResponse httpresponse = client.execute(httpPost);
		HttpEntity entity1 = httpresponse.getEntity();
		String content = EntityUtils.toString(entity1);
		Assert.assertTrue(content.contains("Template:: Test Clean"));
		Assert.assertTrue(content.contains("//head script"));
		Assert.assertFalse(content.contains("//page script"));
		Assert.assertTrue(content.contains("Test Content Clean"));
		Assert.assertFalse(content.contains("Template Content Head"));
		Assert.assertFalse(content.contains("Template Content Foot"));
	}

	private String getValue(String param, String content) {

		Pattern pattern = Pattern.compile("<P>" + param + ":(.*?)</P>",
				Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

		Matcher matcher = pattern.matcher(content);

		if (matcher.find()) {
			//group 0 is the all
			return matcher.group(1);
		}
		return null;
	}

	private static void changeCacheToON() {
		for (TemplateBean template : TemplateConfig.templates) {
			template.cache = "ON";
		}
	}

	private static void changeCacheToOFF() {
		for (TemplateBean template : TemplateConfig.templates) {
			template.cache = "OFF";
		}
	}

	private static void changeCacheToSession() {
		for (TemplateBean template : TemplateConfig.templates) {
			template.cache = "SESSION";
		}
	}

	@After
	public void changeCacheToDefault() {
		changeCacheToSession();
	}

	@Test
	public void cacheBySession() throws ClientProtocolException, IOException {

		changeCacheToSession();

		// First time, put in cache
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(URL_BASE + "sessionTest.jsp");
		HttpResponse httpresponse = client.execute(httpPost);
		HttpEntity entity1 = httpresponse.getEntity();
		String content = EntityUtils.toString(entity1);

		String before1 = getValue("Before", content);
		String after1 = getValue("After", content);
		String sessionid1 = getValue("Sessionid", content);
		String random1 = getValue("Random", content);

		// request again, no change session
		httpPost = new HttpPost(URL_BASE + "sessionTest.jsp");
		httpresponse = client.execute(httpPost);
		entity1 = httpresponse.getEntity();
		content = EntityUtils.toString(entity1);

		String before2 = getValue("Before", content);
		String after2 = getValue("After", content);
		String sessionid2 = getValue("Sessionid", content);
		String random2 = getValue("Random", content);

		Assert.assertEquals(before1, before2);
		Assert.assertEquals(after1, after2);
		Assert.assertEquals(sessionid1, sessionid2);
		Assert.assertEquals(after1, sessionid1);
		Assert.assertEquals(before1, after1);
		Assert.assertEquals(random1, random2);

		// reset cache

		httpPost = new HttpPost(URL_BASE + "sessionResetTest.jsp");
		httpresponse = client.execute(httpPost);
		entity1 = httpresponse.getEntity();
		content = EntityUtils.toString(entity1);

		before1 = getValue("Before", content);
		after1 = getValue("After", content);
		sessionid1 = getValue("Sessionid", content);
		random1 = getValue("Random", content);

		// request again
		httpPost = new HttpPost(URL_BASE + "sessionResetTest.jsp");
		httpresponse = client.execute(httpPost);
		entity1 = httpresponse.getEntity();
		content = EntityUtils.toString(entity1);

		before2 = getValue("Before", content);
		after2 = getValue("After", content);
		sessionid2 = getValue("Sessionid", content);
		random2 = getValue("Random", content);

		Assert.assertNotEquals(before1, before2);
		Assert.assertNotEquals(after1, after2);
		Assert.assertNotEquals(sessionid1, sessionid2);
		Assert.assertEquals(after1, sessionid1);
		Assert.assertEquals(after2, sessionid2);
		Assert.assertNotEquals(before1, after1);
		Assert.assertNotEquals(before2, after2);
		Assert.assertNotEquals(random1, random2);
	}

	@Test
	public void cahceON() throws ClientProtocolException, IOException {

		changeCacheToON();

		// First time, put in cache
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(URL_BASE + "sessionResetTest.jsp");
		HttpResponse httpresponse = client.execute(httpPost);
		HttpEntity entity1 = httpresponse.getEntity();
		String content = EntityUtils.toString(entity1);

		String before1 = getValue("Before", content);
		String after1 = getValue("After", content);
		String sessionid1 = getValue("Sessionid", content);
		String random1 = getValue("Random", content);

		// request again

		httpresponse = client.execute(httpPost);
		entity1 = httpresponse.getEntity();
		content = EntityUtils.toString(entity1);

		String before2 = getValue("Before", content);
		String after2 = getValue("After", content);
		String sessionid2 = getValue("Sessionid", content);
		String random2 = getValue("Random", content);

		Assert.assertNotEquals(before1, before2);
		Assert.assertNotEquals(after1, after2);
		Assert.assertEquals(sessionid1, sessionid2);
		Assert.assertEquals(random1, random2);

	}

	@Test
	public void cahceOFF() throws ClientProtocolException, IOException {

		changeCacheToOFF();

		// First time, put in cache
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(URL_BASE + "sessionResetTest.jsp");
		HttpResponse httpresponse = client.execute(httpPost);
		HttpEntity entity1 = httpresponse.getEntity();
		String content = EntityUtils.toString(entity1);

		String before1 = getValue("Before", content);
		String after1 = getValue("After", content);
		String sessionid1 = getValue("Sessionid", content);
		String random1 = getValue("Random", content);

		// request again

		httpresponse = client.execute(httpPost);
		entity1 = httpresponse.getEntity();
		content = EntityUtils.toString(entity1);

		String before2 = getValue("Before", content);
		String after2 = getValue("After", content);
		String sessionid2 = getValue("Sessionid", content);
		String random2 = getValue("Random", content);

		Assert.assertNotEquals(before1, before2);
		Assert.assertNotEquals(after1, after2);
		Assert.assertNotEquals(random1, random2);
		Assert.assertNotEquals(sessionid1, sessionid2);

	}

	public void testOrder(String content, String before, String after) {
		Assert.assertTrue(content.contains(before));
		Assert.assertTrue(content.contains(after));
		Assert.assertTrue(content.indexOf(after) > content.indexOf(before));
	}

	private static void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
