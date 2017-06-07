/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *     Contributors:
 *          Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.engine;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.engine.internal.cordova.DownloadableCordovaEngine;

import com.github.zafarkhaja.semver.Version;

/**
 * A cordova platform engine.
 * 
 * @author Gorkem Ercan
 *
 */
public class HybridMobileEngine {

	private String name;
	private String spec;
	private HybridMobileLibraryResolver resolver;

	public HybridMobileEngine(String name, String spec, HybridMobileLibraryResolver resolver) {
		this.name = name;
		this.spec = spec;
		this.resolver = resolver;
	}

	/**
	 * User friendly name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public String getSpec() {
		return spec;
	}

	public boolean isValid() {
		if (spec == null) {
			return false;
		}
		// check local - needs a better check (is this dir actually a cordova platform?)
		File file = new File(spec);
		if (file.exists()) {
			return true;
		}

		// check downloadable
		Version version = null;
		try {
			version = Version.valueOf(spec);
		} catch (Exception e) {
			return false;
		}
		if (version != null) {
			try {
				List<DownloadableCordovaEngine> downloadableEngines = CordovaEngineProvider.getInstance()
						.getDownloadableEngines();
				for (DownloadableCordovaEngine dEngine : downloadableEngines) {
					if (dEngine.getPlatformId().equals(name) && dEngine.getVersion().equals(spec)) {
						return true;
					}
				}
				return false;
			} catch (CoreException e) {
				return false;
			}
		}
		// TODO git
		return false;
	}

	public HybridMobileLibraryResolver getResolver() {
		return resolver;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof HybridMobileEngine)) {
			return false;
		}
		HybridMobileEngine that = (HybridMobileEngine) obj;
		if (this.getName().equals(that.getName()) && this.getSpec().equals(that.getSpec())) {
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		if (this.getName() != null && this.getSpec() != null) {
			return this.getName().hashCode() + this.getSpec().hashCode();
		}
		return super.hashCode();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[ name: " + getName() + " spec: " + getSpec() + " ]";
	}

}