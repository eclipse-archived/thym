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
package org.eclipse.thym.core.plugin.actions;
		

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.internal.util.XMLUtil;
import org.eclipse.thym.core.platform.IPluginInstallationAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLConfigFileAction implements IPluginInstallationAction {
	
	protected final File target;
	protected final String parent;
	protected final String xml;
	private XPathExpression xpathExpression;
	
	public XMLConfigFileAction(File target, String parent, String xml){
		this.target = target;
		this.parent = parent;
		this.xml = xml;
	}

	@Override
	public void install() throws CoreException {
		Document doc = XMLUtil.loadXML(target,false);
		Document newNode = XMLUtil.loadXML(xml);//config-file node
		Node node = getParentNode(doc.getDocumentElement());
		if(node == null ){
			handleParentNodeException();
			return;
		}
		NodeList childNodes = newNode.getDocumentElement().getChildNodes(); //append child nodes of config-file
		for(int i = 0; i < childNodes.getLength(); i++ ){
			Node importedNode = doc.importNode(childNodes.item(i), true);
			node.appendChild(importedNode);
		}
		XMLUtil.saveXML(target, doc);
	}
	
	@Override
	public void unInstall() throws CoreException {
		Document doc = XMLUtil.loadXML(target, false);//Namespaces cause the Node.isEqualNode to fail
		Document node = XMLUtil.loadXML(xml);         //because snippets usually can not be namespaces aware
		Node parentNode = getParentNode(doc.getDocumentElement());
		if(parentNode == null ){
			handleParentNodeException();
			return;
		}
		NodeList childNodes = node.getDocumentElement().getChildNodes(); 
		for(int i = 0; i < childNodes.getLength(); i++ ){
			Node importedNode = doc.importNode(childNodes.item(i),true);
			NodeList children = parentNode.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node item = children.item(j);
				if(item.isEqualNode(importedNode)){
					parentNode.removeChild(item);
					break;
				}
			}
			
		}
		XMLUtil.saveXML(target, doc);
	}

	private void handleParentNodeException() throws CoreException{
		//It is common that a parent node can not be found because 
		//plugins specify them on a platform specific way.
		HybridCore.log(IStatus.ERROR, 
				NLS.bind("Parent node could not be retrieved on {0} with expression {1}" ,
				new String[]{target.getName(),parent}),null);
	}

	private XPathExpression getXPathExpression() throws XPathExpressionException {
		if(xpathExpression == null ){
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpathExpression = xpath.compile(parent);
		}
		return xpathExpression;
	}
	
	protected Node getParentNode(Element root) throws CoreException {
		try {
			return (Node) getXPathExpression().evaluate(root,
					XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					HybridCore.PLUGIN_ID, NLS.bind("Parent node could not be retrieved on {0} with expression {1}" ,
							new String[]{target.getName(),parent}), e));
		}
	}

	@Override
	public String[] filesToOverwrite() {
		// nothing is overwritten but modified
		return new String[0];
	}
	

}
