package com.evon.injectTemplate;

import com.evon.injectTemplate.config.TemplateBean;
import com.evon.injectTemplate.config.TemplateConfig;

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
