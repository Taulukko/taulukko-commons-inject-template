/**
 * 
 *	Copyright 2013 Inject Template Evon (ITE)
 *  
 *	Inject Template Evon by Edson Vicente Carli Junior (evon) 
 *  is licensed under a Creative Commons Attribution 3.0 Unported License.
 *	Permissions beyond the scope of this license may be available at 
 *  http://inject.evon.com.br/permissions.
 * 
 */

package com.taulukko.commons.injecttemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taulukko.commons.injecttemplate.config.InjectUtils;
import com.taulukko.commons.injecttemplate.config.TemplateBean;
import com.taulukko.commons.injecttemplate.config.TemplateConfig;
import com.taulukko.commons.parsers.htmlparser.HTMLParser;
import com.taulukko.commons.parsers.htmlparser.IDocument;
import com.taulukko.commons.parsers.htmlparser.IElement;
import com.taulukko.commons.parsers.htmlparser.IElements;

public class InjectTemplateFilter implements Filter {

	private static Pattern pattern = Pattern.compile("<INJECT[ ]{1,}selector=[\"'](.*?)[\"']/>",
			Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
	
	private static Map<String, String> contentTypes = null;
	private static Map<String, HTMLInfoBean> templates = null;

	private static boolean templateLodaded = false;
	private static boolean templateLodadedStarted = false;

	@Override
	public void destroy() {
		contentTypes = null;
		templates = null;
		templateLodaded = false;
		templateLodadedStarted = false;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		try {
			contentTypes = new ConcurrentHashMap<>();
			templates = new ConcurrentHashMap<>();

			TemplateConfig.load(filterConfig.getServletContext());

			printConfig();
		} catch (TemplateException e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage(), e);
		}
	}

	private void printConfig() {

		if (!TemplateConfig.verbose) {
			return;
		}
		System.out.println("Inject Template Config Loaded:");
		System.out.println("verbose:true");
		for (TemplateBean template : TemplateConfig.templates) {
			System.out.println("name:" + template.name);
			System.out.println("path:" + template.path);
			System.out.println("filter:" + template.filter);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			StringWriter writer = new StringWriter();
			BufferedResponseWrapper wrapper = new BufferedResponseWrapper((HttpServletResponse) response, writer);
			chain.doFilter(request, wrapper);

			String contentType = null;
			String uri = httpRequest.getRequestURI();

			contentType = httpResponse.getContentType();

			if (contentTypes.containsKey(uri)) {
				contentType = contentTypes.get(uri);
			} else if (contentType != null) {
				contentTypes.put(uri, contentType);
			}
			contentType = (contentType == null) ? "none" : contentType;

			String out = writer.getBuffer().toString();

			writer.close();

			boolean requestedURIIsTheTemplate = InjectUtils.isTemplate(request.getServletContext().getContextPath(),
					uri);

			boolean contentTypeIsText = !wrapper.isBinary() && !contentType.equals("none");

			if (requestedURIIsTheTemplate || !contentTypeIsText) {
				if (contentTypeIsText) {
					response.getWriter().print(out);
				}
				return;
			}

			if (!templateLodaded && !templateLodadedStarted) {
				loadTemplates(httpRequest);
			}

			TemplateBean template = getTemplatePathByURI(uri);

			if (template == null) {
				response.getWriter().print(out);
				return;
			}

			if (!templates.containsKey("/" + template.path)) {
				throw new ServletException("Template [" + template.path + "] not founded");
			}

			String contentTemplate = loadContentTemplate(template, request.getServerName(),
					request.getServletContext().getContextPath(), request.getServerPort(),
					request.getProtocol().contains("HTTPS"), httpRequest, httpResponse);

			HTMLInfoBean templateHTML = templates.get("/" + template.path);

			IDocument docOut = HTMLParser.parse(out);

			for (String selector : templateHTML.getSelectors()) {
				contentTemplate = replaceSelector(selector, docOut, templateHTML, httpRequest, contentTemplate);
			}

			response.getWriter().print(contentTemplate);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private String replaceSelector(String selector, IDocument docOut, HTMLInfoBean templateHTML,
			HttpServletRequest httpRequest, String contentTemplate) {
		IElements elements = docOut.select(selector);
		if (elements.size() == 0) {
			System.out.println("WARNING: Selector [" + selector + "] in template [" + templateHTML.getUri()
					+ "] not founded in [" + httpRequest.getRequestURI() + "]");
			return contentTemplate;
		}
		if (elements.size() != 1) {
			System.out
					.println("WARNING: Selector get many elements. Choosed the first to URI: " + templateHTML.getUri());
		}
		IElement element = elements.get(0);
		String innerHTML = element.html();

		return contentTemplate.replace("<INJECT selector='" + selector + "'/>", innerHTML);

	}

	private TemplateBean getTemplatePathByURI(String uri) {

		for (TemplateBean template : TemplateConfig.templates) {
			if (isTempltaToThatURI(template, uri)) {
				return template;
			}
		}

		return null;
	}

	private boolean isTempltaToThatURI(TemplateBean template, String uri) {
		for (String include : template.filter.include) {
			if (InjectUtils.isTemplateIncludeToThatURI(include, uri)) {
				for (String exclude : template.filter.exclude) {
					if (InjectUtils.isTemplateExcludeToThatURI(exclude, uri)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private void loadTemplates(HttpServletRequest request) throws ServletException {

		if (templateLodaded) {
			return;
		}

		templateLodadedStarted = true;

		try {
			for (TemplateBean template : TemplateConfig.templates) {
				if (template.path.startsWith(";")) {
					template.path = template.path.substring(1);
				}

				boolean firstCharIsUnecessarySeparator = template.path.startsWith("/")
						|| template.path.startsWith("\\");
				template.path = (firstCharIsUnecessarySeparator) ? template.path.substring(1) : template.path;

				loadHTMLInfo(template, request.getServerName(), request.getServerPort(),
						request.getProtocol().contains("HTTPS"), request);

			}
		} catch (Exception e) {
			templateLodaded = false;
			templateLodadedStarted = false;

			throw new ServletException("Exception:" + e.getMessage(), e);
		}

		templateLodaded = true;
	}

	private String loadContentTemplate(TemplateBean template, String domain, String contextName, Integer port,
			boolean https, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
		HTMLInfoBean htmlInfo = templates.get("/" + template.path);

		String sport = (port == null) ? "" : ":" + String.valueOf(port);

		if (contextName.equals("/")) {
			contextName = "";
		}

		String url = htmlInfo.getProtocol() + "://" + domain + sport + contextName + "/" + template.path;

		String content;
		try {

			ServletContext context = httpRequest.getServletContext();

			String uri = (template.path.startsWith("/")) ? template.path : "/" + template.path;

			RequestDispatcher dispatcher = context.getRequestDispatcher(uri);

			StringWriter writer = new StringWriter();

			BufferedResponseWrapper responseWrapper = new BufferedResponseWrapper(httpResponse, writer);

			dispatcher.forward(httpRequest, responseWrapper);

			int status = responseWrapper.getStatus();
			if (status != 200) {
				throw new Exception("Template  [" + url + "] not loaded - HTML status code " + status + " must be 200");
			}

			content = writer.getBuffer().toString();

			writer.close();

		} catch (IOException | ServletException e) {
			if (e.getMessage() != null && e.getMessage().equals("Not Found")) {
				throw new IOException("Template [" + url + "] not found!", e);
			}
			throw e;
		}

		List<String> selectorsFixed = new ArrayList<>();

		content = fixSelectors(content, selectorsFixed);

		htmlInfo.setSelectors(selectorsFixed);
		
		return content;
	}

	private String fixSelectors(String content, List<String> selectors) {

		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			String tagInject = matcher.group(0);
			String selector = matcher.group(1);
			selectors.add(selector);
			content = content.replace(tagInject, "<INJECT selector='" + selector + "'/>");
		}
		return content;
	}

	private void loadHTMLInfo(TemplateBean template, String domain, Integer port, boolean https,
			HttpServletRequest httpRequest) throws Exception {
		HTMLInfoBean htmlInfo = new HTMLInfoBean();
		htmlInfo.setUri(template.path);
		htmlInfo.setLastUpdate(System.currentTimeMillis());
		htmlInfo.setDomain(domain);
		htmlInfo.setPort(port);
		htmlInfo.setProtocol((https) ? "https" : "http");

		templates.put("/" + template.path, htmlInfo);
	}
}
