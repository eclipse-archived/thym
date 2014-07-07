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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.thym.core.HybridCore;

public class HybridMobileEngine{
	
	private String id;
	private String name;
	private String version;
	private ArrayList<PlatformLibrary> platforms = new ArrayList<PlatformLibrary>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public void addPlatformLib(PlatformLibrary platform) {
		if(!platforms.contains(platform)){
			platforms.add(platform);
		}
	}
	
	public List<PlatformLibrary> getPlatformLibs(){
		return Collections.unmodifiableList(platforms);
	}
	
	public PlatformLibrary getPlatformLib(String id){
		List<PlatformLibrary> pls = getPlatformLibs();
		for (PlatformLibrary thePlatform : pls) {
			if(thePlatform.getPlatformId().equals(id)){
				return thePlatform;
			}
		}
		return null;
	}
	
	/**
	 * Checks if the underlying library compatible and 
	 * support the platforms of this engine.
	 * 
	 * @return status of the library
	 */
	public IStatus isLibraryConsistent(){
		List<PlatformLibrary> pls = getPlatformLibs();
		MultiStatus status = new MultiStatus(HybridCore.PLUGIN_ID, 0, "The library can not support this application",null);
		for (PlatformLibrary thePlatform : pls) {
			status.add(thePlatform.getPlatformLibraryResolver().isLibraryConsistent());
		}
		return status;
	}
	
	/**
	 * Pre-compiles the libraries used by this engine.
	 * @param monitor
	 * @throws CoreException
	 */
	public void preCompile(IProgressMonitor monitor) throws CoreException{
		List<PlatformLibrary> pls = getPlatformLibs();
		for (PlatformLibrary thePlatform : pls) {
			HybridMobileLibraryResolver resolver = thePlatform.getPlatformLibraryResolver();
			if(resolver.needsPreCompilation())
			{
				resolver.preCompile(monitor);
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof HybridMobileEngine) ){
			return false;
		}
		HybridMobileEngine that = (HybridMobileEngine) obj;
		if(this.getId().equals(that.getId()) 
				&& this.getVersion().equals(that.getVersion())){
			return true;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		if(this.getId() != null && this.getVersion() != null ){
			return this.getId().hashCode()+this.getVersion().hashCode();
		}
		return super.hashCode();
	}
	
}