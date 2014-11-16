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

package com.evon.injectTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.fluent.Request;

import com.evon.htmlparser.HTMLParser;
import com.evon.htmlparser.IDocument;
import com.evon.htmlparser.IElement;
import com.evon.htmlparser.IElements;
import com.evon.injectTemplate.config.InjectUtils;
import com.evon.injectTemplate.config.TemplateBean;
import com.evon.injectTemplate.config.TemplateConfig;

public class InjectTemplateFilter implements Filter {
	private static Map<String, String> contentTypes = null;
	private static Map<String, HTMLInfoBean> templates = null;

	private static boolean templateLodaded = false;
	private static boolean templateLodadedStarted = false;
	static Map<String, HtmlContentBean> htmlContents = new ConcurrentHashMap<>();

	// remover o content do template pois n��o vai mais precisar
	// ver se resolve, se mesmo com cache n��o ocorre mais a repti����o de
	// google
	// analitics e tamb��m se para o problema dos erros
	// por ultimo, fazer um tuning pra ver se consegue melhorar o tempo mas s��
	// depois que corrigir os erros por timeout que estao dando agora

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
			System.out.println("cache:" + template.cache);
			System.out.println("filter:" + template.filter);
		}
	}

	private String getCookieHashs(HttpServletRequest httpRequest) {
		Cookie cookies[] = httpRequest.getCookies();
		if (cookies == null) {
			return "";
		}
		StringBuffer ret = new StringBuffer();
		for (Cookie cookie : cookies) {
			ret.append(cookie.getName().hashCode());
			ret.append(";");
			ret.append(cookie.getValue().hashCode());
			ret.append(";");
		}
		return ret.toString();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		StringWriter writer = new StringWriter();
		BufferedResponseWrapper wrapper = new BufferedResponseWrapper(
				(HttpServletResponse) response, writer);
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

		boolean requestedURIIsTheTemplate = false;

		requestedURIIsTheTemplate = InjectUtils.isTemplate(uri);

		boolean contentTypeIsText = !wrapper.isBinary() && contentType != null
				&& !contentType.equals("none");

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

		String key = null;

		if (template.cache.equals("SESSION")) {
			String cookiesNames = getCookieHashs(httpRequest);
			key = template.path + cookiesNames;
		} else if (template.cache.equals("ON"))

		{
			key = template.path;

		}

		if (!templates.containsKey("/" + template.path)) {
			throw new ServletException("Template [" + template.path
					+ "] not founded");
		}

		boolean needRefresh = template.refreshInSeconds > 0
				&& ((System.currentTimeMillis() - template.lastUpdate) / 1000) > template.refreshInSeconds;

		boolean replaceTemplate = template.cache.equals("OFF")
				|| !htmlContents.containsKey(key) || needRefresh;


				
/*	pra investigar erro de nulo, 	
		boolean notExist = key==null || !htmlContents.containsKey(key);
 * if(!notExist)
		{
			notExist=!notExist;
			//throw new ServletException("content not exist");
		}
	*/
		
		if (replaceTemplate) {
			if (needRefresh) {
				template.lastUpdate = System.currentTimeMillis();
			}

			try {
				loadContentTemplate(template, request.getServerName(),
						request.getServerPort(), request.getProtocol()
								.contains("HTTPS"), httpRequest);
			} catch (Exception e) {
				throw new ServletException(e.getMessage(), e);
			}
		}

		HTMLInfoBean templateHTML = templates.get("/" + template.path);

		
		String contentTemplate = htmlContents.get(key).getContent();
		IDocument docOut = HTMLParser.parse(out);

		for (String selector : templateHTML.getSelectors()) {
			IElements elements = docOut.select(selector);
			if (elements.size() == 0) {
				System.out.println("WARNING: Selector [" + selector
						+ "] in template [" + templateHTML.getUri()
						+ "] not founded in [" + httpRequest.getRequestURI()
						+ "]");
				continue;
			}
			if (elements.size() != 1) {
				System.out
						.println("WARNING: Selector get many elements. Choosed the first to URI: "
								+ templateHTML.getUri());
			}
			IElement element = elements.get(0);
			String innerHTML = element.html();

			contentTemplate = contentTemplate.replace("<INJECT selector='"
					+ selector + "'/>", innerHTML);
		}

		response.getWriter().print(contentTemplate);
	}

	private TemplateBean getTemplatePathByURI(String uri) {

		for (TemplateBean template : TemplateConfig.templates) {
			if (isTempltaToThatURI(template, uri)) {
				return template;
			}
		}

		return null;
	}

	/*
	 * @TODO: poderia ter um cache que guarda o resultado e tamb��m quantas
	 * vezes foi usado nos ultimos 5 min em 5 em 5 minutos (ou configuravel),
	 * poderia excluir os menos usados e manter um rank dos mais usados isso
	 * agilizaria e eviatria o for*for abaixo.
	 */
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

	private void loadTemplates(HttpServletRequest request)
			throws ServletException {

		if (templateLodaded) {
			return;
		}

		templateLodadedStarted = true;

		try {
			for (TemplateBean template : TemplateConfig.templates) {
				if (template.path.startsWith(";")) {
					template.path = template.path.substring(1);
				}

				boolean firstCharIsUnecessarySeparator = template.path
						.startsWith("/") || template.path.startsWith("\\");
				template.path = (firstCharIsUnecessarySeparator) ? template.path
						.substring(1) : template.path;

				loadHTMLInfo(template, request.getServerName(),
						request.getServerPort(), request.getProtocol()
								.contains("HTTPS"), request);

			}
		} catch (Exception e) {
			templateLodaded = false;
			templateLodadedStarted = false;

			throw new ServletException("Exception:" + e.getMessage(), e);
		}

		templateLodaded = true;
	}

	private void loadContentTemplate(TemplateBean template, String domain,
			Integer port, boolean https, HttpServletRequest httpRequest)
			throws Exception {
		HTMLInfoBean htmlInfo = templates.get("/" + template.path);

		String sport = (port == null) ? "" : ":" + String.valueOf(port);

		String url = htmlInfo.getProtocol() + "://" + domain + sport + "/"
				+ template.path;

		Request request = Request.Get(url);

		Enumeration<String> headerNames = httpRequest.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();

			Enumeration<String> headerValues = httpRequest.getHeaders(name);
			while (headerValues.hasMoreElements()) {
				String value = headerValues.nextElement();
				request = request.addHeader(name, value);
			}
		}

		String content = request.execute().returnContent().asString();

		Pattern pattern = Pattern.compile(
				"<INJECT[ ]{1,}selector=[\"'](.*?)[\"']/>",
				Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

		Matcher matcher = pattern.matcher(content);

		List<String> selectors = new ArrayList<String>();

		while (matcher.find()) {
			String tagInject = matcher.group(0);
			String selector = matcher.group(1);
			selectors.add(selector);
			content = content.replace(tagInject, "<INJECT selector='"
					+ selector + "'/>");
		}

		String key = null;

		if (template.cache.equals("SESSION")) {
			String cookiesNames = getCookieHashs(httpRequest);
			key = template.path + cookiesNames;
		} else {
			key = template.path;
		}

		HtmlContentBean contentBean = new HtmlContentBean();
		contentBean.setContent(content);
		contentBean.setLastAccess(System.currentTimeMillis());
		htmlContents.remove(key);
		htmlContents.put(key, contentBean);
		htmlInfo.setSelectors(selectors);
	}

	private void loadHTMLInfo(TemplateBean template, String domain,
			Integer port, boolean https, HttpServletRequest httpRequest)
			throws Exception {
		HTMLInfoBean htmlInfo = new HTMLInfoBean();
		htmlInfo.setUri(template.path);
		htmlInfo.setLastUpdate(System.currentTimeMillis());
		htmlInfo.setDomain(domain);
		htmlInfo.setPort(port);
		htmlInfo.setProtocol((https) ? "https" : "http");

		templates.put("/" + template.path, htmlInfo);
	}
}
