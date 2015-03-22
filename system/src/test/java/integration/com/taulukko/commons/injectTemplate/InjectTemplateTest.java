package integration.com.taulukko.commons.injectTemplate;

import java.io.File;
import java.io.IOException;

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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class InjectTemplateTest {

	private static Tomcat tomcat = null;
	private static Thread thread = null; 

	@BeforeClass
	public static void init() throws Exception {

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				String webappDirLocation = new File("").getAbsolutePath() +  "/src/main/resources/webapps";
				tomcat = new Tomcat();

				// The port that we should run on can be set into an environment
				// variable
				// Look for that variable and default to 8080 if it isn't there.
				String webPort = System.getenv("PORT");
				if (webPort == null || webPort.isEmpty()) {
					webPort = "8181";
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
		 * http://localhost:8181/
		 * http://localhost:8181/jsps/clean
		 * 
		 * You have 30 seconds to test until goal package run (required use goal package maven to run this test)
		 * To increment this time, increment the sleepTime
		 * 
		 * long sleepTime = 30000;
		 * sleep(sleepTime);
		 * 
		 */
		
		long sleepTime = 0*30000;
		sleep(sleepTime);
		
		tomcat.stop();
	}

	@Test
	public void defaultTemplateTest() throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8181/");
		HttpResponse httpresponse =  client.execute(httpPost);
		HttpEntity entity1 = httpresponse.getEntity();
		String content = EntityUtils.toString(entity1);
		Assert.assertTrue( content.contains("Template:: Test"));
		testOrder(content,"//head script","//page script");
		testOrder(content,"Template Content Head","Test Content Root");
		testOrder(content,"Test Content Root","Template Content Foot");
	}
	
	@Test
	public void cleanTemplateTest() throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8181/jsps/clean/");
		HttpResponse httpresponse =  client.execute(httpPost);
		HttpEntity entity1 = httpresponse.getEntity();
		String content = EntityUtils.toString(entity1);
		Assert.assertTrue( content.contains("Template:: Test Clean"));
		Assert.assertTrue( content.contains("//head script"));
		Assert.assertFalse( content.contains("//page script"));
		Assert.assertTrue( content.contains("Test Content Clean"));
		Assert.assertFalse( content.contains("Template Content Head"));
		Assert.assertFalse( content.contains("Template Content Foot"));
	}
	 
	
	public void testOrder(String content, String before, String after)
	{
		Assert.assertTrue( content.contains(before));
		Assert.assertTrue( content.contains(after));
		Assert.assertTrue( content.indexOf(after) > content.indexOf(before));
	}

	private static void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
