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
package org.eclipse.thym.hybrid.test.ios.pbxproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.thym.hybrid.test.TestUtils;
import org.eclipse.thym.ios.core.pbxproject.PBXFile;
import org.eclipse.thym.ios.core.pbxproject.PBXProject;
import org.eclipse.thym.ios.core.pbxproject.PBXProjectException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dd.plist.ASCIIPropertyListParser;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;

public class PBXProjectTest {
	private static final String DEFAULT_GROUP = "<group>";
	private static File pbxFile;
	private static final String SOURCE_FILE = "sourcecode.c.objc";
	private static final String HEADER_FILE = "sourcecode.c.h";
	
	@BeforeClass
	public static void setFiles() throws IOException{
		pbxFile = TestUtils.createTempFile("plistTest.txt");
	}
	
	@Test
	public void testPBXFileDefaults(){
		PBXFile pbxFile = new PBXFile("/my/test/file.m");
		assertEquals("4", pbxFile.getEncoding());
		assertEquals(SOURCE_FILE, pbxFile.getLastType());
		assertEquals(DEFAULT_GROUP, pbxFile.getSourceTree());
		assertEquals("Sources", pbxFile.getGroup());
		assertNull(pbxFile.getCompilerFlags());
		assertNotNull(pbxFile.getFileRef());
	}
	
	@Test
	public void testAddPluginFile() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "my/files/abc.h";
		PBXFile file = new PBXFile(testPath);
		project.addPluginFile(file);
		
		
		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		NSDictionary fileRef = (NSDictionary) objects.objectForKey(file.getFileRef());
		assertNotNull(fileRef);
		NSString isa = (NSString)fileRef.get("isa");
		assertEquals("PBXFileReference",isa.getContent());
		NSString path = (NSString)fileRef.get("path");
		assertEquals(testPath, path.getContent());
		NSString lastType = (NSString)fileRef.get("lastKnownFileType");
		assertEquals(HEADER_FILE, lastType.getContent());
		NSString encoding = (NSString)fileRef.get("fileEncoding");
		assertEquals("4", encoding.getContent());
		NSString sourceTree = (NSString)fileRef.get("sourceTree");
		assertEquals(DEFAULT_GROUP, sourceTree.getContent());
		
		assertTrue("No entry found on the Plugins group",isFileEntryFoundOnPluginsGroup(file, objects));
		
	}

	private boolean isFileEntryFoundOnPluginsGroup(PBXFile file,
			NSDictionary objects) throws PBXProjectException {
		NSDictionary group = getGroupByName(objects, "Plugins");
		NSArray children = (NSArray) group.objectForKey("children");
		boolean groupFound = false;
		NSObject[] childs = children.getArray();
		for (int i = 0; i < childs.length; i++) {
			NSString str = (NSString)childs[i];
			if(str.getContent().equals(file.getFileRef())){
				groupFound = true;
				break;
			}
		}
		return groupFound;
	}
	
	@Test
	public void testAddSourceFile() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "my/files/abcd.m";
		PBXFile file = new PBXFile(testPath);
		project.addSourceFile(file);
		

		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		
		//Added the PBXBuildFile object correctly?
		NSDictionary buildFile = (NSDictionary)objects.objectForKey(file.getUuid());
		assertNotNull(buildFile);
		NSString isa = (NSString) buildFile.get("isa");
		assertEquals("PBXBuildFile",isa.getContent());
		NSString fileRef = (NSString) buildFile.get("fileRef");
		assertEquals(file.getFileRef(), fileRef.getContent());
		
		//Added the PBXFileReference object correctly?
		NSDictionary fileRefObj = (NSDictionary)objects.objectForKey(file.getFileRef());
		assertNotNull(fileRefObj);
		isa = (NSString)fileRefObj.get("isa");
		assertEquals("PBXFileReference", isa.getContent());
		NSString encoding = (NSString) fileRefObj.get("fileEncoding");
		assertEquals("4", encoding.getContent());
	    NSString lastKnownType = (NSString) fileRefObj.get("lastKnownFileType");
	    assertEquals("sourcecode.c.objc",lastKnownType.getContent());
	    NSString name = (NSString) fileRefObj.get("name");
	    assertEquals("abcd.m",name.getContent());
	    NSString path = (NSString) fileRefObj.get("path");
	    assertEquals(testPath, path.getContent());
	    NSString sourceTree = (NSString) fileRefObj.get("sourceTree");
	    assertEquals(DEFAULT_GROUP, sourceTree.getContent());
	    
	    //Added the PBXGroup entry correctly?
		assertTrue("No entry found on the Plugins group",isFileEntryFoundOnPluginsGroup(file, objects));
		
		NSDictionary phase = getPhase(objects, "PBXSourcesBuildPhase");
		NSArray files = (NSArray) phase.get("files");
		assertTrue(files.containsObject(new NSString(file.getUuid())));
		
	}
	
	@Test
	public void testAddFramework() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "libsqlite3.dylib";
		PBXFile file = new PBXFile(testPath);
		project.addFramework(file);
		
		
		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		
		//Added the PBXFileReference object correctly?
		NSDictionary fileRef = (NSDictionary) objects.objectForKey(file.getFileRef());
		assertNotNull(fileRef);
		NSString isa = (NSString)fileRef.get("isa");
		assertEquals("PBXFileReference",isa.getContent());
		NSString path = (NSString)fileRef.get("path");
		assertEquals("usr/lib/libsqlite3.dylib", path.getContent());
		NSString lastType = (NSString)fileRef.get("lastKnownFileType");
		assertEquals("\"compiled.mach-o.dylib\"", lastType.getContent());
		NSString sourceTree = (NSString)fileRef.get("sourceTree");
		assertEquals("SDKROOT", sourceTree.getContent());
		
		//Added the PBXBuildFile object correctly?
		NSDictionary buildFile = (NSDictionary)objects.objectForKey(file.getUuid());
		assertNotNull(buildFile);
		isa = (NSString) buildFile.get("isa");
		assertEquals("PBXBuildFile",isa.getContent());
		NSString fRef = (NSString) buildFile.get("fileRef");
		assertEquals(file.getFileRef(), fRef.getContent());
		assertFalse(buildFile.containsKey("settings"));

		
		//Added to the Frameworks PBXGroup
		NSDictionary group = getGroupByName(objects, "Frameworks");
		NSArray children = (NSArray) group.objectForKey("children");
		assertTrue(children.containsObject(new NSString(file.getFileRef())));

		
		//Added to the PBXFrameworksBuildPhase correctly?
		NSDictionary phase = getPhase(objects, "PBXFrameworksBuildPhase");
		NSArray files = (NSArray) phase.get("files");
		assertTrue(files.containsObject(new NSString(file.getUuid())));
	}
	
	@Test
	public void testAddFrameworkWithWeak() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "libsqlite3.dylib";
		PBXFile file = new PBXFile(testPath);
		file.setWeak(true);
		project.addFramework(file);
	
		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		
		NSDictionary buildFile = (NSDictionary)objects.objectForKey(file.getUuid());
		assertNotNull(buildFile);
		NSString isa = (NSString) buildFile.get("isa");
		assertEquals("PBXBuildFile",isa.getContent());
		NSString fRef = (NSString) buildFile.get("fileRef");
		assertEquals(file.getFileRef(), fRef.getContent());
		NSDictionary settings = (NSDictionary) buildFile.get("settings");
		NSArray attributes = (NSArray) settings.get("ATTRIBUTES");
		assertTrue(attributes.containsObject(NSObject.wrap("Weak")));
	}
	
	@Test
	public void testAddResource() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "assets.bundle";
		PBXFile file = new PBXFile(testPath);
		project.addResourceFile(file);
		
		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		
		//Added the PBXBuildFile object correctly?
		NSDictionary buildFile = (NSDictionary)objects.objectForKey(file.getUuid());
		assertNotNull(buildFile);
		NSString isa = (NSString) buildFile.get("isa");
		assertEquals("PBXBuildFile",isa.getContent());
		NSString fRef = (NSString) buildFile.get("fileRef");
		assertEquals(file.getFileRef(), fRef.getContent());
	
		//Added the PBXFileReference object correctly?
		NSDictionary fileRef = (NSDictionary) objects.objectForKey(file.getFileRef());
		assertNotNull(fileRef);
		isa = (NSString)fileRef.get("isa");
		assertEquals("PBXFileReference",isa.getContent());
		NSString path = (NSString)fileRef.get("path");
		assertEquals("assets.bundle", path.getContent());
		NSString lastType = (NSString)fileRef.get("lastKnownFileType");
		assertEquals("\"wrapper.plug-in\"", lastType.getContent());
		NSString sourceTree = (NSString)fileRef.get("sourceTree");
		assertEquals(DEFAULT_GROUP, sourceTree.getContent());
		NSString name = (NSString) fileRef.get("name");
		assertEquals("assets.bundle", name.getContent());
		assertFalse(fileRef.containsKey("fileEncoding"));
		
		//Added to the Resources PBXGroup group?
		NSDictionary group = getGroupByName(objects, "Resources");
		NSArray children = (NSArray) group.objectForKey("children");
		assertTrue(children.containsObject(new NSString(file.getFileRef())));
		
		//Added to the PBXSourcesBuildPhase
		NSDictionary phase = getPhase(objects, "PBXResourcesBuildPhase");
		NSArray files = (NSArray) phase.get("files");
		assertTrue(files.containsObject(new NSString(file.getUuid())));
	}
	
	@Test
	public void testAddResourceWithPlugin() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "assets.bundle";
		PBXFile file = new PBXFile(testPath);
		file.setPlugin(true);
		project.addResourceFile(file);
		
		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		
		//Added the PBXFileReference object correctly?
		NSDictionary fileRef = (NSDictionary) objects.objectForKey(file.getFileRef());
		assertNotNull(fileRef);
		NSString isa = (NSString)fileRef.get("isa");
		assertEquals("PBXFileReference",isa.getContent());
		NSString path = (NSString)fileRef.get("path");
		assertEquals("assets.bundle", path.getContent());
		NSString lastType = (NSString)fileRef.get("lastKnownFileType");
		assertEquals("\"wrapper.plug-in\"", lastType.getContent());
		NSString sourceTree = (NSString)fileRef.get("sourceTree");
		assertEquals(DEFAULT_GROUP, sourceTree.getContent());
		NSString name = (NSString) fileRef.get("name");
		assertEquals("assets.bundle", name.getContent());
		assertFalse(fileRef.containsKey("fileEncoding"));


		//Added to the Plugins PBXGroup group?
		NSDictionary group = getGroupByName(objects, "Plugins");
		NSArray children = (NSArray) group.objectForKey("children");
		assertTrue(children.containsObject(new NSString(file.getFileRef())));
		
		//Added to the PBXSourcesBuildPhase
		NSDictionary phase = getPhase(objects, "PBXResourcesBuildPhase");
		NSArray files = (NSArray) phase.get("files");
		assertTrue(files.containsObject(new NSString(file.getUuid())));


	}
	
	@Test
	public void testAddToLibrarySearchPaths() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "my/files/abcd.h";
		PBXFile file = new PBXFile(testPath);
		project.addToLibrarySearchPaths(file);
		
		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		HashMap<String, NSObject> hashmap =  objects.getHashMap();	
		Collection<NSObject> values = hashmap.values();
		for (NSObject nsObject : values) {
			NSDictionary obj = (NSDictionary) nsObject;
			NSString isa = (NSString) obj.objectForKey("isa");
			if(isa != null && isa.getContent().equals("XCBuildConfiguration")){
				NSDictionary buildSettings = (NSDictionary) obj.objectForKey("buildSettings");
				assertTrue(buildSettings.containsKey("LIBRARY_SEARCH_PATHS"));
				NSArray searchPaths = (NSArray) buildSettings.get("LIBRARY_SEARCH_PATHS"); 
				assertEquals("$(SRCROOT)/Test_Application/my/files", ((NSString)searchPaths.objectAtIndex(1)).getContent());
			}
		}

	}
	
	@Test
	public void testAddHeader() throws Exception{
		PBXProject project = new PBXProject(pbxFile);
		String testPath = "file.h";
		PBXFile file = new PBXFile(testPath);
		project.addHeaderFile(file);
		
		NSDictionary dict = (NSDictionary)ASCIIPropertyListParser.parse(project.getContent().getBytes());
		NSDictionary objects = (NSDictionary)dict.objectForKey("objects");
		
		//Added the PBXFileReference object correctly?
		NSDictionary fileRef = (NSDictionary) objects.objectForKey(file.getFileRef());
		assertNotNull(fileRef);
		NSString isa = (NSString)fileRef.get("isa");
		assertEquals("PBXFileReference",isa.getContent());
		NSString path = (NSString)fileRef.get("path");
		assertEquals(testPath, path.getContent());
		NSString lastType = (NSString)fileRef.get("lastKnownFileType");
		assertEquals("sourcecode.c.h", lastType.getContent());
		NSString sourceTree = (NSString)fileRef.get("sourceTree");
		assertEquals(DEFAULT_GROUP, sourceTree.getContent());
		NSString name = (NSString) fileRef.get("name");
		assertEquals("file.h", name.getContent());
		NSString encoding = (NSString) fileRef.get("fileEncoding");
		assertEquals("4", encoding.getContent());

		//Added to the Plugins PBXGroup group?
		NSDictionary group = getGroupByName(objects, "Plugins");
		NSArray children = (NSArray) group.objectForKey("children");
		assertTrue(children.containsObject(new NSString(file.getFileRef())));
		
	}
	
	private static NSDictionary getGroupByName(NSDictionary objects, String name) throws PBXProjectException{
		HashMap<String, NSObject> map = objects.getHashMap();
		Collection<NSObject> values = map.values();
		for (NSObject nsObject : values) {
			NSDictionary obj = (NSDictionary)nsObject;
			NSString isa = (NSString) obj.objectForKey("isa");
			NSString nameString = (NSString) obj.objectForKey("name");
			if(isa != null && isa.getContent().equals("PBXGroup") && nameString != null && name.equals(nameString.getContent())){
				return obj;
			}
		}
		return null;
	}
	
	private static NSDictionary getPhase(NSDictionary objects, String name) throws PBXProjectException{
		HashMap<String, NSObject> map = objects.getHashMap();
		Collection<NSObject> values = map.values();
		for (NSObject nsObject : values) {
			NSDictionary obj = (NSDictionary)nsObject;
			NSString isa = (NSString) obj.objectForKey("isa");
			if(isa != null && isa.getContent().equals(name)){
				return obj;
			}
		}
		return null;
	}	
	
	
}
