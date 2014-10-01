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
package org.eclipse.thym.core.internal.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLContentDescriber;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConfigFileContentDescriber extends XMLContentDescriber implements IExecutableExtension{
	
	
	@Override
	public int describe(InputStream input, IContentDescription description)
			throws IOException {
		if(super.describe(input, description) == INVALID ){ //Not XML
			return INVALID;
		}
		try {
			input.reset();
			if(checkConfigFile(new InputSource(input))){
				return VALID;
			}
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Internal Error: XML parser configuration error during content description of config.xml files");
		} catch (SAXException e) {
			return INDETERMINATE;
		}
		return INDETERMINATE;
	}
	
	@Override
	public int describe(Reader input, IContentDescription description)
			throws IOException {
		if(super.describe(input, description) == INVALID ){
			return INVALID;
		}
		
		try {
			input.reset();
			if(checkConfigFile(new InputSource(input))){
				return VALID;
			}
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Internal Error: XML parser configuration error during content description of config.xml files");
		} catch (SAXException e) {
			return INDETERMINATE;
		}
		
		return INDETERMINATE;
	}
	
	private boolean checkConfigFile(InputSource source) throws ParserConfigurationException, SAXException, IOException{
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document configDocument = db.parse(source);
		Element root = configDocument.getDocumentElement();
		return root.getLocalName().equals("widget");
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

}
