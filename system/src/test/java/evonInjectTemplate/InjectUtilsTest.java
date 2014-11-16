package evonInjectTemplate;

import org.junit.Assert;
import org.junit.Test;

import com.evon.injectTemplate.config.InjectUtils;
  

public class InjectUtilsTest {

	@Test
	public void isTemplateExcludeToThatURI()
	{
		Assert.assertEquals(false,InjectUtils.isTemplateExcludeToThatURI("/online.jsp", "/index_mini.jsp"));
	}
}
