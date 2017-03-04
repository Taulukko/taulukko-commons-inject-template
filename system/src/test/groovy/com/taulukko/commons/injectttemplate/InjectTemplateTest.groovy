package com.taulukko.commons.injectttemplate;

import org.junit.Test
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.taulukko.commons.injecttemplate.config.ConfigBean
import com.taulukko.commons.parsers.jsonParser.JSONParser

@RunWith(JUnit4.class)
class InjectTemplateTest {

	@Test
	void jsonToClass () {
		String json = """{
  "verbose": true,
  "templates": [
    {
      "name": "Clean",
      "cache": "SESSION",
      "cacheSizeMB": 10,
      "path": "/jsps/template/clean.jsp",
      "refreshInSeconds": 600,
      "filter": {
        "include": [
          "/jsps/clean/**",
          "/jsps/clean2/**"
        ],
        "exclude": [
          "*.js",
          "*.css",
          "*.zip",
          "*.dwr",
          "*.ttf",
          "*.wav",
          "*.ogg",
          "*.mp3",
          "*.png",
          "*.gif",
          "*.bmp",
          "*.jpg"
        ]
      }
    },
    {
      "name": "Default",
      "cache": "SESSION",
      "cacheSizeMB": 10,
      "refreshInSeconds": 300,
      "path": "/jsps/template/default.jsp",
      "filter": {
        "include": [
          "/**"
        ],
        "exclude": [
          "*.js",
          "*.css",
          "*.zip",
          "*.dwr",
          "*.ttf",
          "*.wav",
          "*.ogg",
          "*.mp3",
          "*.png",
          "*.gif",
          "*.bmp",
          "*.jpg",
          "/translate/**",
          "/online.jsp"
        ]
      }
    }
  ]
}""";


		ConfigBean config = JSONParser.convert(json, ConfigBean.class);
		assert config != null;
	}
}
