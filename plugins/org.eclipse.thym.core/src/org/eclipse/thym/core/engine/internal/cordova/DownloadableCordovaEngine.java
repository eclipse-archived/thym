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
package org.eclipse.thym.core.engine.internal.cordova;

import java.util.ArrayList;
import java.util.List;

public class DownloadableCordovaEngine {
	
	public static class LibraryDownloadInfo{
		private String platformId;
		private String downloadURL;
		private String version;
		
		public String getPlatformId() {
			return platformId;
		}
		public void setPlatformId(String platformId) {
			this.platformId = platformId;
		}
		public String getDownloadURL() {
			return downloadURL;
		}
		public void setDownloadURL(String downloadURI) {
			this.downloadURL = downloadURI;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LibraryDownloadInfo) {
				LibraryDownloadInfo info = (LibraryDownloadInfo) obj;
				return platformId.equals(info.getPlatformId())
						&& version.equals(info.getVersion());
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 31 + platformId.hashCode();
			return hash * 31 + version.hashCode();
		}
	}
	
	private String version;
	private List<LibraryDownloadInfo> libs;
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void addLibraryInfo(LibraryDownloadInfo info){
		if(this.libs == null ){
			this.libs = new ArrayList<DownloadableCordovaEngine.LibraryDownloadInfo>();
		}
		this.libs.add(info);
	}
	
	public LibraryDownloadInfo getPlatformLibraryInfo(String platformId){
		for (LibraryDownloadInfo libraryDownloadInfo : libs) {
			if(libraryDownloadInfo.getPlatformId().equals(platformId)){
				return libraryDownloadInfo;
			}
		}
		return null;
	}

	/**
	 * Merge two {@link DownloadableCordovaEngine} instances intro one. It is
	 * performed only if they have the same version.
	 * 
	 * @param newEngine
	 * @return <code>true</code> if libraries were merged; otherwise return
	 *         <code>false</code>
	 */
	protected boolean merge(DownloadableCordovaEngine newEngine) {
		if (this.version.endsWith(newEngine.getVersion())) {
			List<LibraryDownloadInfo> newLibs = newEngine.libs;
			for (LibraryDownloadInfo newLib : newLibs) {
				if (!libs.contains(newLib)) {
					libs.add(newLib);
				}
			}
			return true;
		}
		return false;
	}

}
