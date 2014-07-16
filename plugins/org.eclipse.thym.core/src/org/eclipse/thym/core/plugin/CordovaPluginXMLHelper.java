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
package org.eclipse.thym.core.plugin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.internal.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * Helper for DOM parsing of the Cordova plugin.xml
 * 
 * @author Gorkem Ercan
 *
 */
public class CordovaPluginXMLHelper {
	
	public static final String PLGN_PROPERTY_INFO = "info";
	public static final String PLGN_PROPERTY_KEYWORDS = "keywords";
	public static final String PLGN_PROPERTY_LICENSE = "license";
	public static final String PLGN_PROPERTY_NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String PLGN_PROPERTY_AUTHOR = "author";
	public static final String PLGN_PROPERTY_VERSION = "version";
	public static final String PLGN_PROPERTY_PLATFORM = "platform";
	public static final String PLGN_PROPERTY_ID = "id";

	
	
	public static Element getPlatformNode(Document document, String platform){
		NodeList nodes = getNodes(document.getDocumentElement(), "platform");
		for(int i = 0; i< nodes.getLength(); i++){
			Node n = nodes.item(i);
			String platformName = getAttributeValue(n, "name");
			if(platformName != null && platformName.equals(platform)){
				return (Element) n;
			}
		}
		return null;
	}

	public static NodeList getSourceFileNodes(Element node){
		return getNodes(node, "source-file");
	}
	
	public static NodeList getResourceFileNodes(Element node){
		return getNodes(node, "resource-file");
	}
	
	public static NodeList getHeaderFileNodes(Element node){
		return getNodes(node, "header-file");
	}
	
	public static NodeList getAssets(Element node){
		return getNodes(node, "asset");
	}
	
	public static NodeList getConfigFileNodes(Element node) {
		return getNodes(node, "config-file");
	}	
	
	public static NodeList getPreferencesNodes(Element node) {
		return getNodes(node, "preference");
	}	
	
	public static NodeList getLibFileNodes(Element node) {
		return getNodes(node, "lib-file");
	}
	
	public static NodeList getFrameworks(Element node){
		return getNodes(node, "framework");
	}
	
	public static NodeList getDependencies(Element node){
		return getNodes(node, "dependency");
	}
	
	/**
	 * Returns the name node. May return null
	 * @param node
	 * @return name node or null
	 */
	public static Node getNameNode(Element node){
		NodeList list = getNodes(node, "name");
		if(list.getLength() == 1){
			return list.item(0);
		}
		return null;
	}
		
	private static NodeList getNodes(Element element, String nodeName ){
		return element.getElementsByTagName(nodeName);
	}
	
	public static String getAttributeValue(Node node, String attribute){
		Assert.isLegal(node !=null, "Null node value");
		NamedNodeMap map = node.getAttributes();
		Node attrib = map.getNamedItem(attribute);
		if(attrib == null ){
			return null;
		}
		return attrib.getNodeValue().trim();
	}
	
	public static String stringifyNode(Node node){
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource(node);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Result result = new StreamResult(out);
			transformer.transform(source, result);
			return out.toString();
			
		} catch (TransformerConfigurationException e) {
			return null;
		} catch (TransformerFactoryConfigurationError e) {
			return null;
		} catch (TransformerException e) {
			return null;
		}
	}

	public static CordovaPlugin createCordovaPlugin(InputStream contents) throws CoreException {
		Document doc = XMLUtil.loadXML(contents,false);
		CordovaPlugin plugin = new CordovaPlugin();
		Element rootNode = doc.getDocumentElement();
		plugin.setId(getAttributeValue(rootNode, PLGN_PROPERTY_ID));
		plugin.setVersion(getAttributeValue(rootNode, PLGN_PROPERTY_VERSION));
		plugin.setAuthor(getChildNodeValue(rootNode, PLGN_PROPERTY_AUTHOR));
		plugin.setDescription(getChildNodeValue(rootNode, DESCRIPTION));
		plugin.setName(getChildNodeValue(rootNode, PLGN_PROPERTY_NAME));
		plugin.setLicense(getChildNodeValue(rootNode, PLGN_PROPERTY_LICENSE));
		plugin.setKeywords(getChildNodeValue(rootNode, PLGN_PROPERTY_KEYWORDS));
		plugin.setInfo(getChildNodeValue(rootNode, PLGN_PROPERTY_INFO));
		//js-modules
		NodeList moduleNodes = getNodes(rootNode, "js-module");
		for (int i = 0; i < moduleNodes.getLength(); i++) {
			Node n = moduleNodes.item(i);

			PluginJavaScriptModule module = new PluginJavaScriptModule();
			if(n.getParentNode().getNodeName().equals("platform")){
				module.setPlatform(getAttributeValue(n.getParentNode(), PLGN_PROPERTY_NAME));
			}
			module.setName(plugin.getId()+"."+getAttributeValue(n, PLGN_PROPERTY_NAME));
			module.setSource(getAttributeValue(n, "src"));
			NodeList childNodes = n.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node item = childNodes.item(j);
				if(item.getNodeName().equals("runs")){
					module.setRuns(true);
				}else
				if(item.getNodeName().equals("merges")){
					module.addMerge(getAttributeValue(item,"target"));
				}else
				if(item.getNodeName().equals("clobbers")){
					module.addClobber(getAttributeValue(item,"target"));
				}	
			}
			plugin.addModule(module);
		}
		// engines
		NodeList engineNodes = getNodes(rootNode, "//:engine");
		for (int i = 0; i < engineNodes.getLength(); i++) {
			Node engineNode = engineNodes.item(i);

			String name = getAttributeValue(engineNode, PLGN_PROPERTY_NAME);
			String version = getAttributeValue(engineNode, PLGN_PROPERTY_VERSION);
			String platform = getAttributeValue(engineNode, PLGN_PROPERTY_PLATFORM);
			plugin.addSupportedEngine(name, version, platform);
		}
		return plugin;
	}
	
	private static String getChildNodeValue(Element node, String tagName){
		NodeList nodes = node.getElementsByTagName(tagName);
		if(nodes.getLength()>0){
			return nodes.item(0).getTextContent().trim();
		}
		return null;
	}
	
	
}
