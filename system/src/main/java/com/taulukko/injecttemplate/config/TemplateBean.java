package com.taulukko.injecttemplate.config;

public class TemplateBean {
	public String name = null;
	public String path = null;
	public FilterBean filter = null;
	public String cache = "OFF";//OFF, ON, SESSION
	public double cacheSizeMB = 10;
	public int refreshInSeconds = 300;
	public long lastUpdate = System.currentTimeMillis();
}
