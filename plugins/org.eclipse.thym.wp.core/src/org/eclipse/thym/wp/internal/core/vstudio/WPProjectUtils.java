/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.wp.internal.core.vstudio;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.wp.core.WPCore;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Project utilities for Windows Phone 8 project.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public final class WPProjectUtils {

	public static final String CSPROJ_EXTENSION = ".csproj"; //$NON-NLS-1$
	public static final String SLN_EXTENSION = ".sln"; //$NON-NLS-1$

	public static final String WP8 = "wp8"; //$NON-NLS-1$
	public static final String BIN = "Bin"; //$NON-NLS-1$
	public static final String DEBUG = "Debug"; //$NON-NLS-1$
	public static final String RELEASE = "Release"; //$NON-NLS-1$

	/**
	 * @param projectRoot
	 *            project's root folder
	 * @return www folder for specified project's root
	 */
	public static File getPlatformWWWDirectory(File projectRoot) {
		return new File(projectRoot, PlatformConstants.DIR_WWW);
	}

	/**
	 * Get .csproj file for a Windows Phone project.
	 * 
	 * @param projectRoot
	 *            Windows Phone project's root folder
	 * @return .csproj file or <code>null</code> if could not find it
	 */
	public static File getCsrojFile(File projectRoot) {
		File[] csprojFiles = projectRoot.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(WPProjectUtils.CSPROJ_EXTENSION);
			}
		});
		return csprojFiles != null && csprojFiles.length > 0 ? csprojFiles[0]
				: null;
	}

	/**
	 * Read specified XML file.
	 * 
	 * @param file
	 *            XML file
	 * @return {@link Document} instance
	 */
	public static Document readXML(File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(file.getAbsolutePath());
		} catch (ParserConfigurationException e) {
			WPCore.log(IStatus.ERROR, "error during XML file parsing", e); //$NON-NLS-1$
		} catch (SAXException e) {
			WPCore.log(IStatus.ERROR, "error during XML file parsing", e); //$NON-NLS-1$
		} catch (IOException e) {
			WPCore.log(IStatus.ERROR, "error during XML file parsing", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Write XML file to specified file.
	 * 
	 * @param file
	 * @param doc
	 *            {@link Document} instance
	 */
	public static void writeXML(File file, Document doc) {
		try {
			Result result = new StreamResult(file);
			Source source = new DOMSource(doc);
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			xformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
			xformer.transform(source, result);
		} catch (TransformerFactoryConfigurationError e) {
			WPCore.log(IStatus.ERROR, "error during XML file writing", e); //$NON-NLS-1$
		} catch (TransformerException e) {
			WPCore.log(IStatus.ERROR, "error during XML file writing", e); //$NON-NLS-1$
		}
	}

}
