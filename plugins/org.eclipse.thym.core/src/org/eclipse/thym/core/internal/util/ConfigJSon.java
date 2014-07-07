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
package org.eclipse.thym.core.internal.util;

import static org.eclipse.thym.core.platform.PlatformConstants.DIR_DOT_CORDOVA;
import static org.eclipse.thym.core.platform.PlatformConstants.FILE_JSON_CONFIG;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.platform.PlatformConstants;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class ConfigJSon {
	
	private String id;
	private String name;
	private Engine engine;
	
	public class Engine{
		private String id;
		private String ver;
		public Engine(){
		}
	}
	
	public ConfigJSon(){
	}
	

	public void persist(IProject project) throws CoreException{
		IFolder folder = project.getFolder(DIR_DOT_CORDOVA);
		if(folder != null && folder.exists()){
			Gson gson = new Gson();
			String json = gson.toJson(this);
			
			IFile file = folder.getFile(FILE_JSON_CONFIG);
			InputStream stream = null;
			try {
				stream = new ByteArrayInputStream(json.getBytes("utf-8"));
				if(file.exists()){
					file.setContents(stream, 0, new NullProgressMonitor());
				}else{
					file.create(stream,true,new NullProgressMonitor());
				}
			} catch (UnsupportedEncodingException e) {
				HybridCore.log(IStatus.ERROR, "Error while persisting config.json", e);
			}
			finally{
				if(stream !=null){
						try { stream.close();
						} catch (IOException e) {/*unhandled*/ }
				}
			}
		}
		else{
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, DIR_DOT_CORDOVA +" does not exist on project "+ project.getName()));
		}
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getEngineId(){
		if(engine == null )
			return null;
		return engine.id;
	}
	
	public String getEngineVersion(){
		if(engine == null)
			return null;
		return engine.ver;
	}

	public void setEngineInfo(HybridMobileEngine hybridMobileEngine) {
		if( hybridMobileEngine == null ){
			this.engine = null;
		}else{
			this.engine = new Engine();
			this.engine.id = hybridMobileEngine.getId();
			this.engine.ver = hybridMobileEngine.getVersion();
		}
	}
	
	public static ConfigJSon readConfigJson(IProject project) throws CoreException{
		IFolder folder = project.getProject().getFolder(PlatformConstants.DIR_DOT_CORDOVA);
		if(!folder.exists()){
			HybridCore.log(IStatus.WARNING, ".cordova folder is missing",null );
			return new ConfigJSon();
		}
		IFile file = folder.getFile(PlatformConstants.FILE_JSON_CONFIG);
		Gson gson = new Gson();
		JsonReader reader = new JsonReader( new InputStreamReader(file.getContents()));
		return  gson.fromJson(reader, ConfigJSon.class);
	}

}
