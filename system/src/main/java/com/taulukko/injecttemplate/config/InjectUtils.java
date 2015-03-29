package com.taulukko.injecttemplate.config;

public class InjectUtils {

	public static boolean isTemplateExcludeToThatURI(String exclude, String uri) {

		boolean accepetSubfolders = exclude.endsWith("/**");

		if (accepetSubfolders) {
			String start = exclude.replace("/**", "/");
			if (uri.startsWith(start)) {
				return true;
			}
		}

		boolean sameFolder = exclude.split("[/]{1,1}").length == uri
				.split("[/]{1,1}").length;

		String start = exclude.replace("/*", "/");

		if (sameFolder && uri.startsWith(start)) {
			return true;
		}

		if (uri.indexOf("?") > 0) {
			uri = uri.substring(0, uri.indexOf("?"));
		}

		String parts[] = exclude.split("[.]{1,1}");

		if (parts.length < 2) {
			return false;
		}

		 
		boolean endsEqual = uri.endsWith(parts[1]);
		boolean startEqual = parts[0].endsWith("*") || uri.endsWith(exclude);
		return endsEqual && startEqual;
		 
	}

	public static boolean isTemplateIncludeToThatURI(String include, String uri) {
		boolean accepetSubfolders = include.endsWith("/**");

		if (accepetSubfolders) {
			String start = include.replace("/**", "/");
			if (uri.startsWith(start)) {
				return true;
			}
			return false;
		}

		boolean accepetAnyFiles = include.endsWith("/*");

		if (accepetAnyFiles) {
			String start = include.replace("/*", "/");
			if (!uri.startsWith(start)) {
				return false;
			}
			String right = uri.replace(start, "");
			boolean intoSubfolder = right.contains("/");
			if (!intoSubfolder) {
				return true;
			}
			return false;
		}

		boolean isExactURI = uri.startsWith(include);
		return isExactURI;

	}

	public static boolean isTemplate(String contextPath,String uri) {
		  
		if(contextPath.equals("/"))
		{
			contextPath = "";
		}
		
		uri = uri.substring(contextPath.length());
		
		for (TemplateBean template : TemplateConfig.templates) {
			if (uri.startsWith("/" + template.path)) {
				return true;
			}
		}
		return false;
	}

}
