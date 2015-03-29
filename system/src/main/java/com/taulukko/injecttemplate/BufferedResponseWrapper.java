package com.taulukko.injecttemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class BufferedResponseWrapper extends HttpServletResponseWrapper {
	private final PrintWriter writer;

	private HttpServletResponse response;
	private boolean binary = false;
	private boolean changed = false;

	public BufferedResponseWrapper(HttpServletResponse response,
			StringWriter stringWriter) {
		super(response);
		this.writer = new PrintWriter(stringWriter);
		this.response = (HttpServletResponse) response;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		changed = true;
		binary = true;
		return response.getOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		changed = true;
		return writer;
	}

	public boolean isBinary() {
		return binary;
	}

	public boolean isChanged() {
		return changed;
	}

}