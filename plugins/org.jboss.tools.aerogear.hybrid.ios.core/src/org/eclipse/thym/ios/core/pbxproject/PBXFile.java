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


public class PBXFile {
	
	private static final String M_EXTENSION = ".m";
	private static final String SOURCE_FILE = "sourcecode.c.objc";
	private static final String  H_EXTENSION = ".h";
	private static final String HEADER_FILE = "sourcecode.c.h";
	private static final String BUNDLE_EXTENSION =".bundle";
	private static final String BUNDLE = "\"wrapper.plug-in\"";
	private static final String XIB_EXTENSION = ".xib";
	private static final String XIB_FILE = "file.xib";
	private static final String DYLIB_EXTENSION = ".dylib"; 
	private static final String DYLIB = "\"compiled.mach-o.dylib\"";
	private static final String	FRAMEWORK_EXTENSION = ".framework";
	private static final String FRAMEWORK = "wrapper.framework";
	private static final String ARCHIVE_EXTENSION = ".a";
	private static final String ARCHIVE = "archive.ar";
			 
	
	private String path;
	private String lastType;
	private String sourceTree;
	private String encoding;
	private String compilerFlags;
	private String group;
	private String fileRef;
	private String uuid;
	private boolean weak;
	private boolean isFramework;
	private boolean isPlugin;
	
	
	public PBXFile(String path){
		this(path, guessLastType(path));
	}
	

	public PBXFile(String path, String lastType){
		this.lastType = lastType;
		this.path = path;
	}

	public String getPath() {
		if(getLastType().equals(FRAMEWORK)) {
		   return "System/Library/Frameworks/" + path;
		 } 
		if (getLastType().equals(DYLIB)) {
			 return "usr/lib/" + path;
		}
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


	public String getLastType() {
		return lastType;
	}


	public void setLastType(String lastType) {
		this.lastType = lastType;
	}


	public String getSourceTree() {
		if(sourceTree == null ){
			if(DYLIB.equals(getLastType()) || FRAMEWORK.equals(getLastType()) ){
				return "SDKROOT";
			}
			return "<group>";
		}
		return sourceTree;
	}


	public void setSourceTree(String sourceTree) {
		this.sourceTree = sourceTree;
	}


	public String getEncoding() {
		if(encoding == null && !BUNDLE.equals(getLastType())){
			return "4";
		}
		return encoding;
	}


	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}


	public String getCompilerFlags() {
		return compilerFlags;
	}


	public void setCompilerFlags(String compilerFlags) {
		this.compilerFlags = compilerFlags;
	}


	public boolean isWeak() {
		return weak;
	}


	public void setWeak(boolean weak) {
		this.weak = weak;
	}

	public String getGroup() {
		if (group == null) {
			if (SOURCE_FILE.equals(getLastType())) {
				return "Sources";
			} else if (DYLIB.equals(getLastType())
					|| ARCHIVE.equals(getLastType())) {
				return "Frameworks";
			} else {
				return "Resources";
			}
		}
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}

	public String getFileRef() {
		if(fileRef == null ){
			fileRef = PBXProject.generateReference();
		}
		return fileRef;
	}

	public void setFileRef(String fileRef) {
		this.fileRef = fileRef;
	}
	
	public String getUuid() {
		if(uuid == null ){
			uuid = PBXProject.generateReference();
		}
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public boolean isFramework() {
		return isFramework;
	}


	public void setFramework(boolean isFramework) {
		this.isFramework = isFramework;
	}


	public boolean isPlugin() {
		return isPlugin;
	}


	public void setPlugin(boolean isPlugin) {
		this.isPlugin = isPlugin;
	}


	public boolean hasSettings(){
		return isWeak() || getCompilerFlags() != null;
	}

	private static String guessLastType(String file) {
		if(file == null ) return null;
		if(file.endsWith(M_EXTENSION)){
			return SOURCE_FILE;
		}
		if(file.endsWith(H_EXTENSION)){
			return HEADER_FILE;
		}
		if(file.endsWith(BUNDLE_EXTENSION)){
			return BUNDLE;
		}
		if(file.endsWith(XIB_EXTENSION)){
			return XIB_FILE;
		}
		if(file.endsWith(DYLIB_EXTENSION)){
			return DYLIB;
		}
		if(file.endsWith(FRAMEWORK_EXTENSION)){
			return FRAMEWORK;
		}
		if(file.endsWith(ARCHIVE_EXTENSION)){
			return ARCHIVE;
		}
		return "unknown";
	}

}
