package com.taulukko.commons.injecttemplate;

public class TemplateException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7684912558198518434L;

	public TemplateException(String message, Throwable t) {
		super(message, t);
	}

	public TemplateException(String message) {
		super(message);
	}
}
