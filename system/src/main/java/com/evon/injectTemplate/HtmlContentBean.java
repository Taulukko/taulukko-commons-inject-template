package com.evon.injectTemplate;

public class HtmlContentBean {
	
	private String content = null;
	private long lastAccess = 0;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getLastAccess() {
		return lastAccess;
	}
	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}

}
