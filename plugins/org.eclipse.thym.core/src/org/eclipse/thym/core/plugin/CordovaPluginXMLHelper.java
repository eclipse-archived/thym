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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.internal.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
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
		List<Element> nodes = getImmediateNodes(document.getDocumentElement(), "platform");
		for (Element n : nodes) {
			String platformName = getAttributeValue(n, "name");
			if(platformName != null && platformName.equalsIgnoreCase(platform)){
				return (Element) n;
			}
		}
		return null;
	}

	public static List<Element> getSourceFileNodes(Element node){
		return getImmediateNodes(node, "source-file");
	}
	
	public static List<Element> getResourceFileNodes(Element node){
		return getImmediateNodes(node, "resource-file");
	}
	
	public static List<Element> getHeaderFileNodes(Element node){
		return getImmediateNodes(node, "header-file");
	}
	
	public static List<Element> getAssetNodes(Element node){
		return getImmediateNodes(node, "asset");
	}
	
	public static List<Element> getConfigFileNodes(Element node) {
		return getImmediateNodes(node, "config-file");
	}	
	 
	public static List<Element> getPreferencesNodes(Element node) {
		return getImmediateNodes(node, "preference");
	}	
	
	public static List<Element> getLibFileNodes(Element node) {
		return getImmediateNodes(node, "lib-file");
	}
	
	public static List<Element> getFrameworkNodes(Element node){
		return getImmediateNodes(node, "framework");
	}
	
	public static List<Element> getDependencyNodes(Element node){
		return getImmediateNodes(node, "dependency");
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
	
	private static List<Element> getImmediateNodes(Element element, String nodeName){
		List<Element> nodeList = new ArrayList<Element>();
	    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
	      if (child.getNodeType() == Node.ELEMENT_NODE && 
	    		  nodeName.equals(child.getNodeName())) {
	        nodeList.add((Element) child);
	      }
	    }
	    return nodeList;
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
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
			LSSerializer writer = impl.createLSSerializer();
			writer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
			String str = writer.writeToString(node);
			return str;
		} catch (Exception e) {
			HybridCore.log(IStatus.ERROR, "Error resolving node for injection", e);
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
		NodeList engineNodes = getNodes(rootNode, "engine");
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
