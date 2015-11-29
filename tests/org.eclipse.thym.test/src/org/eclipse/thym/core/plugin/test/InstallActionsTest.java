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
package org.eclipse.thym.core.plugin.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.hybrid.test.TestProject;
import org.eclipse.thym.hybrid.test.TestUtils;
import org.eclipse.thym.ios.core.xcode.PlistConfigFileAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

public class InstallActionsTest {
	
	private TestProject project;
	
	@Before
	public void setUpTestProject(){
		project = new TestProject();
	}
	
	@After
	public void cleanProject() throws CoreException, IOException{
		if(this.project != null ){
			this.project.delete();
			this.project = null;
		}
	}
	
	@Test
	@SuppressWarnings("restriction")
	public void testPlistAction_mergeDictionary() throws Exception{
		File target = TestUtils.createTempFile("test-Info.plist");
		String xml = "<config-file target=\"*-Info.plist\" parent=\"CFBundleIcons\">"+
		"<dict> <key>UINewsstandIcon</key> <dict>" + 
	       "<key>CFBundleIconFiles</key>" +
	        "<array><string>Newsstand-Cover-Icon.png</string>" +
	        "<string>Newsstand-Cover-Icon@2x.png</string></array>"+
	        "<key>UINewsstandBindingType</key>"+
	        "<string>UINewsstandBindingTypeMagazine</string>"+
	        "<key>UINewsstandBindingEdge</key>"+
	        "<string>UINewsstandBindingEdgeLeft</string>" + 
	        "</dict> </dict> </config-file>";
		PlistConfigFileAction action = new PlistConfigFileAction(target, "CFBundleIcons", xml);
		action.install();
		NSDictionary dict = (NSDictionary)PropertyListParser.parse(target);
		NSObject object = dict.get("CFBundleIcons");
		assertNotNull(object);
		assertTrue(object instanceof NSDictionary);
		NSDictionary dictionary = (NSDictionary)object;
		assertTrue(dictionary.containsKey("UINewsstandIcon"));
		assertTrue(dictionary.containsKey("CFBundlePrimaryIcon"));
		
		// Reverse
		action.unInstall();
		NSDictionary revDict = (NSDictionary)PropertyListParser.parse(target);
		NSObject revObject = revDict.get("CFBundleIcons");
		assertNotNull(object);
		assertTrue(object instanceof NSDictionary);
		NSDictionary revDictionary = (NSDictionary)revObject;
		assertFalse(revDictionary.containsKey("UINewsstandIcon"));
		assertTrue(revDictionary.containsKey("CFBundlePrimaryIcon"));

	}
	
	@Test
	@SuppressWarnings("restriction")
	public void testPlistAction_mergeArrays() throws Exception{
		File target = TestUtils.createTempFile("test-Info.plist");
		String xml = "<config-file target=\"*-Info.plist\" parent=\"UISupportedInterfaceOrientations\">"+
				"<array> <string>MyPluginValue</string> </array>" + 
	        "</config-file>";
		PlistConfigFileAction action = new PlistConfigFileAction(target, "UISupportedInterfaceOrientations", xml);
		action.install();
		NSDictionary dict = (NSDictionary)PropertyListParser.parse(target);
		NSObject object = dict.get("UISupportedInterfaceOrientations");
		assertNotNull(object);
		assertTrue(object instanceof NSArray);
		NSArray array = (NSArray)object;
		assertTrue(array.count() == 2);
		assertTrue(array.containsObject(NSObject.wrap("UIInterfaceOrientationPortrait")));
		assertTrue(array.containsObject(NSObject.wrap("MyPluginValue")));
		
		//Test uninstall 
		action.unInstall();
		NSDictionary reverseDict = 	 (NSDictionary)PropertyListParser.parse(target);
		NSObject revObject = reverseDict.get("UISupportedInterfaceOrientations");
		assertNotNull(revObject);
		assertTrue(revObject instanceof NSArray);
		NSArray revArray = (NSArray)revObject;
		assertTrue(revArray.count() == 1);
		assertTrue(revArray.containsObject(NSObject.wrap("UIInterfaceOrientationPortrait")));
		assertFalse(revArray.containsObject(NSObject.wrap("MyPluginValue")));
	
		
	}
	
	@Test
	@SuppressWarnings("restriction")
	public void testPlistAction_replaceValue() throws Exception{
		File target = TestUtils.createTempFile("test-Info.plist");
		String xml = "<config-file target=\"*-Info.plist\" parent=\"CFBundleDevelopmentRegion\">"+
		"<string>Klingon</string>"+
	        "</config-file>";
		PlistConfigFileAction action = new PlistConfigFileAction(target, "CFBundleDevelopmentRegion", xml);
		action.install();
		NSDictionary dict = (NSDictionary)PropertyListParser.parse(target);
		NSObject object = dict.get("CFBundleDevelopmentRegion");
		assertNotNull(object);
		assertTrue(object instanceof NSString);
		NSString string = (NSString) object;
		assertTrue(string.getContent().equals("Klingon"));
		
		//Reverse 
		action.unInstall();
		NSDictionary revDict = (NSDictionary)PropertyListParser.parse(target);
		NSObject revObject = revDict.get("CFBundleDevelopmentRegion");
		assertNull(revObject);

		
	}

}
