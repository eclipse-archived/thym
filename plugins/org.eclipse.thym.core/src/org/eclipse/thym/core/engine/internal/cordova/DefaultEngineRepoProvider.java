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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.AbstractEngineRepoProvider;

/**
 * Default implementation of {@link AbstractEngineRepoProvider} for
 * CordovaEngineRepoProvider extension point.
 * 
 * @author
 *
 */
public class DefaultEngineRepoProvider extends AbstractEngineRepoProvider {

	private static final String REPO_JSON_URL = "https://raw.githubusercontent.com/eclipse/thym/master/plugins/org.eclipse.thym.core/res/platforms.json";	
	
	@Override
	public List<DownloadableCordovaEngine> getEngines() throws CoreException {
		List<DownloadableCordovaEngine> downloadableCordovaEngines = new ArrayList<DownloadableCordovaEngine>();
		try {
			InputStream stream = getRemoteJSonStream(REPO_JSON_URL);
			if (stream == null) {
				URL url = FileLocator.find(HybridCore.getContext().getBundle(),
						new Path("/res/platforms.json"), null);
				if (url == null) {
					throw new CoreException(new Status(IStatus.ERROR,
							HybridCore.PLUGIN_ID,
							"Could not read downloadable engine list"));
				}
				stream = url.openStream();
			}
			downloadableCordovaEngines = getEnginesFromStream(stream);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					HybridCore.PLUGIN_ID,
					"Could not read downloadable engine list", e));
		}
		return downloadableCordovaEngines;
	}

}
