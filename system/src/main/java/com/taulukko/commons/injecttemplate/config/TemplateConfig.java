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

package com.taulukko.commons.injecttemplate.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import com.taulukko.commons.injecttemplate.TemplateException;
import com.taulukko.commons.parsers.jsonParser.JSONParser;

public class TemplateConfig extends Thread {

	// //////////////////
	// SERVER//
	// //////////////////
	public static final String CONFIG_PATH = "/WEB-INF/inject.json";
	public static final String LINE_SEPARATOR = "#########################################################";

	private static String absoluteConfigPath = null;

	public static boolean loaded = false;

	public static boolean live = false;

	/**
	 * Description: Templates.
	 */
	public static List<TemplateBean> templates = new ArrayList<TemplateBean>();

	/**
	 * Description: Print more information. Default: false
	 */
	public static Boolean verbose = false;

	private TemplateConfig() {
	}

	public static void reload() throws TemplateException {
		if (absoluteConfigPath == null) {
			throw new TemplateException("Invalid state! Use load first!");
		}
		load(null);
	}

	public static void load(ServletContext servletContext)
			throws TemplateException {

		BufferedInputStream inStream = null;

		if (absoluteConfigPath == null) {
			absoluteConfigPath = servletContext.getRealPath(CONFIG_PATH);
		}

		try {
			inStream = new BufferedInputStream(new FileInputStream(
					absoluteConfigPath));
		} catch (FileNotFoundException fe) {
			throw new TemplateException(
					"Template properties file not founded in ("
							+ absoluteConfigPath + ")", fe);
		}

		StringBuffer stream = new StringBuffer();

		try {
			int avaiableBytes = inStream.available();
			while (avaiableBytes > 0) {
				byte buff[] = new byte[avaiableBytes];
				inStream.read(buff);
				stream.append(new String(buff));
				avaiableBytes = inStream.available();
			}
		} catch (IOException ioe) {
			throw new TemplateException(ioe.getMessage(), ioe);
		}

		try {
			inStream.close();
		} catch (IOException e) {
			throw new TemplateException(e.getMessage(), e);
		}

		String json = stream.toString();

		ConfigBean config = JSONParser.convert(json, ConfigBean.class);

		if (config.templates != null) {
			TemplateConfig.templates = config.templates;
		}

		if (config.verbose != null) {
			TemplateConfig.verbose = config.verbose;
		}
		if (TemplateConfig.verbose) {
			System.out.println("\n\n" + LINE_SEPARATOR + "\n[" + new Date()
					+ "] : Template properties loaded!");
			System.out.print("\nproperties = " + json);
			System.out.print("\n" + LINE_SEPARATOR + "\n\n");
		}
	}
}