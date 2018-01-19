package com.taulukko.commons.injectttemplate;

import static org.junit.Assert.assertNotNull;
 
import org.junit.Test;

import com.taulukko.commons.injecttemplate.config.ConfigBean;
import com.taulukko.commons.parsers.jsonParser.JSONParser;
 
 
public class InjectTemplateTest {

	@Test
	public void jsonToClass () {
		String json="{\n \"verbose\": true,\n \"templates\": [\n {\n \"name\": \"Clean\",\n \"cache\": \"SESSION\",\n \"cacheSizeMB\": 10,\n \"path\": \"/jsps/template/clean.jsp\",\n \"refreshInSeconds\": 600,\n \"filter\": {\n \"include\": [\n \"/jsps/clean/**\",\n \"/jsps/clean2/**\"\n ],\n \"exclude\": [\n \"*.js\",\n \"*.css\",\n \"*.zip\",\n \"*.dwr\",\n \"*.ttf\",\n \"*.wav\",\n \"*.ogg\",\n \"*.mp3\",\n \"*.png\",\n \"*.gif\",\n \"*.bmp\",\n \"*.jpg\"\n ]\n }\n },\n {\n \"name\": \"Default\",\n \"cache\": \"SESSION\",\n \"cacheSizeMB\": 10,\n \"refreshInSeconds\": 300,\n \"path\": \"/jsps/template/default.jsp\",\n \"filter\": {\n \"include\": [\n \"/**\"\n ],\n \"exclude\": [\n \"*.js\",\n \"*.css\",\n \"*.zip\",\n \"*.dwr\",\n \"*.ttf\",\n \"*.wav\",\n \"*.ogg\",\n \"*.mp3\",\n \"*.png\",\n \"*.gif\",\n \"*.bmp\",\n \"*.jpg\",\n \"/translate/**\",\n \"/online.jsp\"\n ]\n }\n }\n ]\n}";

		ConfigBean config = JSONParser.convert(json, ConfigBean.class);
		assertNotNull(   config);
	}
}
