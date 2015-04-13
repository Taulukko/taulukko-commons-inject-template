package com.taulukko.commons.injecttemplate;

import com.taulukko.commons.injecttemplate.config.TemplateBean;
import com.taulukko.commons.injecttemplate.config.TemplateConfig;

public class InjectTemplateManager {
	
	public static void refreshAllTemplates()
	{
		for(TemplateBean template: TemplateConfig.templates)
		{
			template.lastUpdate = 1;
		}
	}
	
	public static void refreshTemplateByName(String name)
	{
		for(TemplateBean template: TemplateConfig.templates)
		{
			if(template.name.equalsIgnoreCase(name))
			{				
				template.lastUpdate = 1;
			}
		}
		InjectTemplateFilter.htmlContents.clear();
	}
}