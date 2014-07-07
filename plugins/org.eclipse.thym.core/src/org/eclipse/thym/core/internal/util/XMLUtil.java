/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtil {
	
	
	/**
	 * Shorthand for namespace aware parsing of an XML file.
	 * 
	 * @param f
	 * @return
	 * @throws CoreException
	 */
	public static Document loadXML(File f )throws CoreException{
		return loadXML(f, true);
	}
	
	
	public static Document loadXML(File f , boolean isNamespaceAware )throws CoreException{
	    DocumentBuilder db;
		DocumentBuilderFactory dbf = getDocumentBuilderFactory(isNamespaceAware);
	
	    try{
	    	db = dbf.newDocumentBuilder();
	    	return db.parse(f); 
	    }
		catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parser configuration error", e));
		} catch (SAXException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind("Error when parsing file: {0}", f.toString()), e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind("IO error when parsing file: {0}",f.toString()), e));
		} 
	}


	private static DocumentBuilderFactory getDocumentBuilderFactory(
			boolean isNamespaceAware) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(isNamespaceAware);
		return dbf;
	}
	
	public static Document loadXML(InputStream source , boolean isNamespaceAware )throws CoreException{
	    DocumentBuilder db;
	    DocumentBuilderFactory dbf = getDocumentBuilderFactory(isNamespaceAware);
	
	    try{
	    	db = dbf.newDocumentBuilder();
	    	return db.parse(source); 
	    }
		catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parser error when parsing ", e));
		} catch (SAXException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parser error ", e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "IO error when parsing", e));
		} 
	}
	
	public static Document loadXML(String content) throws CoreException {
	    DocumentBuilderFactory dbf = getDocumentBuilderFactory(false);//snippets rarely have namespaces in place
	    dbf.setValidating(false);
	    DocumentBuilder db;
	
	    try{
	    	db = dbf.newDocumentBuilder();
	    	return db.parse(new InputSource(new StringReader(content))); 
	    }
		catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parser error when parsing ", e));
		} catch (SAXException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parsing error ", e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "IO error when parsing ", e));
		} 
		
	}
	
	public static void saveXML(File f, Document doc ) throws CoreException{
		try {
			Source source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);
			// Write the DOM document to the file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer xformer;
			xformer = transformerFactory.newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xformer.setOutputProperty(OutputKeys.METHOD, "xml");
			xformer.transform(source, result);

		} catch (TransformerException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Error writing XML to file "+f.toString(),e ));
		}
	}

}
