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
package org.eclipse.thym.ios.core.pbxproject;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import com.dd.plist.ASCIIPropertyListParser;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

public class PBXProject {
	private static final String KEY_LIBRARY_SEARCH_PATHS = "LIBRARY_SEARCH_PATHS";
	private final File file;
	private NSDictionary root;
	
	public PBXProject(File pbxfile){
		this.file = pbxfile;
	}

	public void addSourceFile(PBXFile file) throws PBXProjectException{
		addPluginFile(file);
		addToPbxBuildFileSection(file);        // PBXBuildFile
		addToBuildPhase("PBXSourcesBuildPhase",file);       // PBXSourcesBuildPhase
	}
	
	public void addPluginFile(PBXFile file) throws PBXProjectException{
		file.setPlugin(true);
		NSDictionary pluginsGroup= getGroupByName("Plugins");
		if(pluginsGroup.containsKey("path")){
			String path = file.getPath();
			int index = path.indexOf("Plugins/");
			if(index>-1){
				file.setPath(path.substring(index+"Plugins/".length()));
			}

		}
		this.addToPbxFileReferenceSection(file);    // PBXFileReference
		this.addToPbxGroup("Plugins",file);            // PBXGroup
	}
	
	public void addFramework(PBXFile file) throws PBXProjectException {
		file.setFramework(true);
		addToPbxBuildFileSection(file);        // PBXBuildFile
		addToPbxFileReferenceSection(file);    // PBXFileReference
		addToPbxGroup("Frameworks", file);         // PBXGroup
		addToBuildPhase("PBXFrameworksBuildPhase",file);    // PBXFrameworksBuildPhase
	}
	
	public void addHeaderFile(PBXFile file) throws PBXProjectException {
		this.addPluginFile(file);
	}
	
	public void addResourceFile(PBXFile file) throws PBXProjectException {
		if (file.isPlugin()) {
			this.addPluginFile(file);
		}
		if (!file.isPlugin()) {
			NSDictionary resGroup = getGroupByName("Resources");
			if(resGroup.containsKey("path")){
				String path = file.getPath();
				int index = path.indexOf("Resources/");
				if(index > -1){
					file.setPath(path.substring(index+"Resources/".length()));
				}
			}
		}
		addToPbxBuildFileSection(file); // PBXBuildFile
		addToBuildPhase("PBXResourcesBuildPhase",file); // PBXResourcesBuildPhase
		if(!file.isPlugin()){
			addToPbxFileReferenceSection(file); // PBXFileReference
			addToPbxGroup("Resources",file); // PBXGroup
		}
	}
	/**
	 * Adds the pbxfile to library search paths. The path of the 
	 * pbxfile must be project relative. 
	 * 
	 * @param pbxfile
	 * @throws PBXProjectException
	 */
	public void addToLibrarySearchPaths(PBXFile pbxfile) throws PBXProjectException {
		HashMap<String, NSObject> hashmap =  getObjects().getHashMap();	
		Collection<NSObject> values = hashmap.values();
		for (NSObject nsObject : values) {
			NSDictionary obj = (NSDictionary) nsObject;
			NSString isa = (NSString) obj.objectForKey("isa");
			if(isa != null && isa.getContent().equals("XCBuildConfiguration")){
				NSDictionary buildSettings = (NSDictionary) obj.objectForKey("buildSettings");
				NSArray arr  = null;
				if( buildSettings.containsKey(KEY_LIBRARY_SEARCH_PATHS)){
					arr = (NSArray) buildSettings.objectForKey(KEY_LIBRARY_SEARCH_PATHS);
				}
				if(arr == null){//new search path entry
					arr = new NSArray(NSObject.wrap("$(inherited)"), searchPathForFile(pbxfile));
				}else{//modify existing one
					Object[] current = arr.getArray();
					NSObject[] newArray = new NSObject[current.length + 1];
					System.arraycopy(current, 0, newArray, 0, current.length);
					newArray[newArray.length -1 ] = searchPathForFile(pbxfile);
				}
			buildSettings.put(KEY_LIBRARY_SEARCH_PATHS, arr);
			}
		}
	}
	
	private NSString searchPathForFile(PBXFile pbxfile) throws PBXProjectException {
		String filepath = FilenameUtils.getFullPathNoEndSeparator(pbxfile.getPath());
		if(filepath.equals(".")){
			filepath = "";
		}else{
			filepath = "/"+filepath;
		}
		NSDictionary group = getGroupByName("Plugins");
		
		if(pbxfile.isPlugin() && group.containsKey("path")){
			NSString groupPath = (NSString)group.objectForKey("path");
			return NSObject.wrap("$(SRCROOT)/" + groupPath.getContent().replace('"', ' ').trim());
	    }
		else{
			return NSObject.wrap("$(SRCROOT)/"+ getProductName() + filepath );
		}
	}

	public String getProductName() throws PBXProjectException {
		HashMap<String, NSObject> hashmap =  getObjects().getHashMap();	
		Collection<NSObject> values = hashmap.values();
		for (NSObject nsObject : values) {
			NSDictionary obj = (NSDictionary) nsObject;
			NSString isa = (NSString) obj.objectForKey("isa");
			if(isa != null && isa.getContent().equals("XCBuildConfiguration")){
				NSDictionary buildSettings = (NSDictionary) obj.objectForKey("buildSettings");
				if( buildSettings.containsKey("PRODUCT_NAME")){
					NSString name = (NSString) buildSettings.get("PRODUCT_NAME");
					return name.getContent().replace('"', ' ').trim();
				}
			}
		}
		return null;
	}

	public String getContent() throws PBXProjectException{
		return getRoot().toASCIIPropertyList();
	}
	
	public void persist() throws IOException, PBXProjectException{
		PropertyListParser.saveAsASCII(getRoot(), this.file);
	}

	private void addToBuildPhase(String phaseName, PBXFile pbxfile) throws PBXProjectException {
		NSDictionary phase = getPhaseByName(phaseName);
		NSArray files = (NSArray) phase.objectForKey("files");
		NSObject[] current = files.getArray();
		NSObject[] newArray = new NSObject[ current.length +1 ];
		System.arraycopy(current, 0, newArray, 0, current.length);
		newArray[newArray.length-1] = new NSString(pbxfile.getUuid());
		NSArray newNSArray = new NSArray(newArray);
		phase.remove("files");
		phase.put("files", newNSArray);
	}

	private void addToPbxBuildFileSection(PBXFile pbxfile) throws PBXProjectException {
		NSDictionary obj = new NSDictionary();
		obj.put("isa" , "PBXBuildFile");
		obj.put("fileRef", pbxfile.getFileRef());
		if (pbxfile.hasSettings()){
			NSDictionary settings = new NSDictionary();
			if(pbxfile.isWeak()){
				NSArray attribs = new NSArray(NSObject.wrap("Weak"));
				settings.put("ATTRIBUTES", attribs);
			}
			if(pbxfile.getCompilerFlags() != null ){
				settings.put("COMPILER_FLAGS", NSObject.wrap(pbxfile.getCompilerFlags()));
			}
			obj.put("settings", settings);
		}
		getObjects().put(pbxfile.getUuid(), obj);
	}


	private void addToPbxGroup(String groupName, PBXFile pbxfile) throws PBXProjectException {
		NSDictionary group = getGroupByName(groupName);
		NSArray children = (NSArray) group.objectForKey("children");
		NSObject[] childs = children.getArray();
		NSObject[] newChilds = new NSObject[childs.length +1];
		System.arraycopy(childs, 0, newChilds, 0, childs.length);
		newChilds[newChilds.length-1] = new NSString(pbxfile.getFileRef());
		NSArray newArray = new NSArray(newChilds);
		group.remove("children");
		group.put("children", newArray);
		
	}

	private void addToPbxFileReferenceSection(PBXFile pbxfile) throws PBXProjectException {
		NSDictionary obj = new NSDictionary();
		obj.put("isa", "PBXFileReference");
		obj.put("lastKnownFileType", pbxfile.getLastType());
		obj.put("path", pbxfile.getPath());
		obj.put("name", FilenameUtils.getName(pbxfile.getPath()));
		obj.put("sourceTree", pbxfile.getSourceTree());
		if(pbxfile.getEncoding() != null){
			obj.put("fileEncoding", pbxfile.getEncoding());
		}
		getObjects().put(pbxfile.getFileRef(), obj);
	}
	

	private NSDictionary getRoot() throws PBXProjectException{
		if(this.root == null ){
			try {
				root = (NSDictionary) ASCIIPropertyListParser.parse(file);
			} catch (Exception e) {
				throw new PBXProjectException(e);
			}
		}
		return root;
	}

	private NSDictionary getObjects() throws PBXProjectException {
		NSDictionary dict = (NSDictionary) getRoot();
		NSDictionary objects = (NSDictionary)dict.getHashMap().get("objects");
		return objects;
	}
	
	
	private NSDictionary getPhaseByName(String name) throws PBXProjectException{
		NSDictionary objects = getObjects();
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
	
	private NSDictionary getGroupByName(String name) throws PBXProjectException{
		NSDictionary objects = getObjects();
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
	
	public static String generateReference()
	  {
	    MessageDigest md = null;
	    SecureRandom prng = null;
	    try
	    {
	      md = MessageDigest.getInstance("SHA1");
	      prng = SecureRandom.getInstance("SHA1PRNG");
	    }
	    catch (NoSuchAlgorithmException e)
	    {
	    }

	    String randomNum = new Integer(prng.nextInt()).toString();
	    String ref = new String(Hex.encodeHex(md.digest(randomNum.getBytes())));
	    return ref.toUpperCase().substring(0, 24);
	  }

}
