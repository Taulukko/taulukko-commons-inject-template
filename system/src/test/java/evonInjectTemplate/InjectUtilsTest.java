package evonInjectTemplate;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.evon.injectTemplate.config.InjectUtils;
import com.evon.injectTemplate.config.TemplateBean;
import com.evon.injectTemplate.config.TemplateConfig;
  

public class InjectUtilsTest {

	@Test
	public void isTemplateExcludeToThatURI()
	{
		Assert.assertEquals(false,InjectUtils.isTemplateExcludeToThatURI("/online.jsp", "/index_mini.jsp"));
	}
	

	@Test
	public void isTemplateExcludeToThatURITest() {
		String uri = "/localhost:8080/user/profile/6?teste=fixme.png";
		String exclude = "*.png";

		boolean ret = InjectUtils.isTemplateExcludeToThatURI(exclude, uri);
		Assert.assertFalse("[" + uri + "] cant shoud exclude .png", ret);

		uri = "/localhost:8080/user/profile.html?teste=fixme.png";

		ret = InjectUtils.isTemplateExcludeToThatURI(exclude, uri);
		Assert.assertFalse("[" + uri + "] cant shoud exclude .png", ret);

		uri = "/localhost:8080/user/profile.png?version=1";

		ret = InjectUtils.isTemplateExcludeToThatURI(exclude, uri);
		Assert.assertTrue("[" + uri + "] shoud exclude .png", ret);

	}

	@Test
	public void isTemplateIncludeToThatURITest() {

		String uri = "/localhost:8080/user/profile/6?teste=fixme.png";
		String include = "/**";
		boolean ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertTrue("[" + uri + "] shoud include " + include, ret);

		include = "/*";
		uri = "/teste.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertTrue("[" + uri + "] shoud include " + include, ret);

		include = "/jsp/*";
		uri = "/jsp/teste.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertTrue("[" + uri + "] shoud include " + include, ret);
		

		include = "/jsp/**";
		uri = "/jsp/teste/teste.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertTrue("[" + uri + "] shoud include " + include, ret);

		
		
		
		
		include = "/jsp/**";
		uri = "teste.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertFalse("[" + uri + "] cant shoud include " + include, ret);
		
		
		
		include = "/jsp/*";
		uri = "teste.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertFalse("[" + uri + "] cant shoud include " + include, ret);

		include = "jsps/*";
		uri = "pages/teste.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertFalse("[" + uri + "] cant shoud include " + include, ret);
		

		include = "jsp/**";
		uri = "pages/teste/teste.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertFalse("[" + uri + "] cant shoud include " + include, ret);

		include = "jsp/**";
		uri = "jsp.jpg?teste=fixme.png";
		ret = InjectUtils.isTemplateIncludeToThatURI(include, uri);
		Assert.assertFalse("[" + uri + "] cant shoud include " + include, ret);

	}
	
	@Test
	public void isTemplateTest() 
	{
		TemplateConfig.templates = new ArrayList<>();
		
		TemplateBean template = new TemplateBean();
		template.path = "/template/template.jsp";
		TemplateConfig.templates.add(template);
		
		boolean ret = InjectUtils.isTemplate("/template/template.jsp?version=3");
		Assert.assertTrue("Era esperado verdadeiro",ret);
		
		ret = InjectUtils.isTemplate("/jsp/template/template.jsp?version=3");
		Assert.assertFalse("Era esperado falso",ret);

	}
}
