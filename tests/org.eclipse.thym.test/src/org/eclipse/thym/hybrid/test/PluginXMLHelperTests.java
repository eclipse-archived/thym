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
package org.eclipse.thym.hybrid.test;


import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.core.plugin.CordovaPluginXMLHelper;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PluginXMLHelperTests {
	
	private static final String TEST_PLUGIN_XML = "/cordova_plugin.xml";
	private Document document;
	
	@Before
	public void loadXML() throws ParserConfigurationException, SAXException, IOException{
		InputStream stream = PluginXMLHelperTests.class.getResourceAsStream(TEST_PLUGIN_XML);
		assertNotNull(stream);
	    DocumentBuilder db;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    db = dbf.newDocumentBuilder();
	    document = db.parse(stream); 
	}
	
	@Test
	public void testGetPlatformNode(){
		Element platformNode = getIOSNode();
		assertEquals("ios", platformNode.getAttribute("name"));
		platformNode = CordovaPluginXMLHelper.getPlatformNode(document, "invalid");
		assertNull(platformNode);
	}
	
	@Test
	public void testGetName(){
		Node node = CordovaPluginXMLHelper.getNameNode(document.getDocumentElement());
		assertNotNull(node);
	}
	
	@Test 
	public void testConfigFileForPlatform(){
		Element platformNode = getIOSNode();
		List<Element> nodes = CordovaPluginXMLHelper.getConfigFileNodes(platformNode);
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
	}

	
	@Test
	public void testConfigFileForAll(){
		List<Element> nodes = CordovaPluginXMLHelper.getConfigFileNodes(document.getDocumentElement());
		assertNotNull(nodes);
		assertTrue(nodes.isEmpty());
		
	}
	
	@Test
	public void testSourceFile(){
		Element platform = getIOSNode();
		List<Element> nodes = CordovaPluginXMLHelper.getSourceFileNodes(platform);
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
	}
	
	@Test
	public void testHeaderFile(){
		Element platform = getIOSNode();
		List<Element> nodes = CordovaPluginXMLHelper.getHeaderFileNodes(platform);
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
	}
	
	@Test
	public void testCreateCordovaPlugin() throws CoreException{
		InputStream stream = PluginXMLHelperTests.class.getResourceAsStream(TEST_PLUGIN_XML);
		assertNotNull(stream);
		CordovaPlugin plugin = CordovaPluginXMLHelper.createCordovaPlugin(stream);
		assertNotNull(plugin);
		assertEquals(1, plugin.getModules().size());
		assertEquals("org.apache.cordova.core.vibration", plugin.getId());
		assertEquals("0.1.0", plugin.getVersion());
		assertEquals("vibration", plugin.getName());
	}

	private Element getIOSNode() {
		Element platformNode = CordovaPluginXMLHelper.getPlatformNode(document, "ios");
		return platformNode;
	}
}
