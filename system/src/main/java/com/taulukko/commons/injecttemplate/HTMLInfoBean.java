package com.taulukko.commons.injecttemplate;

import java.util.List;

public class HTMLInfoBean {
	private String domain = null;
	private Integer port = null;
	private String protocol = null;
	private List<String> selectors  = null;
	
	public List<String> getSelectors() {
		return selectors;
	}
	public void setSelectors(List<String> selectors) {
		this.selectors = selectors;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	private String uri = null;
	private long lastUpdate  = 0;

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public long getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	 
}
