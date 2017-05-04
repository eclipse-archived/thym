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
package org.eclipse.thym.core.engine;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.platform.PlatformConstants;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public abstract class HybridMobileLibraryResolver {
	
	public static final IPath PATH_CORDOVA_JS = new Path(PlatformConstants.FILE_JS_CORDOVA);
	public static final String PLATFORM_JSON = "platform.json";
	
	public static final String VAR_PACKAGE_NAME = "$package";
	public static final String VAR_APP_NAME = "$appname";
	protected IPath libraryRoot;
	protected String version;
	
	/**
	 * 
	 * @param engine
	 */
	public void init(IPath libraryRoot){
		this.libraryRoot = libraryRoot;
		this.version = detectVersion();
	}
	
	/**
	 * Returns the URL of the file requested from engine. Destination 
	 * must be a relative path on the target platform's project structure.
	 * May return null if a corresponding file can not be found on the 
	 * engine. 
	 * 
	 * @param destination relative path on target structure
	 * @return URL to the corresponding file on the engine or null
	 */
	public abstract URL getTemplateFile(IPath destination);
	
	/**
	 * Checks if the underlying library compatible and 
	 * can support the platform.
	 * @return
	 */
	public abstract IStatus isLibraryConsistent();
	
	/**
	 * Pre-compiles the library so that it is ready to be used.
	 * @param monitor
	 * @throws CoreException
	 */
	public abstract void preCompile(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Returns true if this library needs to be precompiled before it can be used. 
	 * @return
	 */
	public abstract boolean needsPreCompilation();
	
	/**
	 * Detects the version of the engine from layout
	 * @return
	 */
	public abstract String detectVersion();
	
	/**
	 * Reads library name from package.json file 
	 * @return library name or null if name cannot be determined
	 * @throws FileNotFoundException if package.json file does not exist
	 */
	public String readLibraryName() {
		try{
			FileReader packageJson = new FileReader(libraryRoot.append(PLATFORM_JSON).toFile());
			JsonReader reader = new JsonReader(packageJson);
			JsonParser parser = new JsonParser();
			JsonObject root = parser.parse(reader).getAsJsonObject();
			JsonElement nameElement = root.get("name");
			if(nameElement != null){
				return nameElement.getAsString();
			}
			return null;
		} catch (Exception e) {
			HybridCore.log(IStatus.ERROR, "Error occured while reading "+PLATFORM_JSON, e);
			return null;
		}
	}
	
}
