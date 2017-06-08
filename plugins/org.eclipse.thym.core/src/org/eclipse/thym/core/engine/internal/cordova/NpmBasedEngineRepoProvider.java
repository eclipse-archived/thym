/*******************************************************************************
 * Copyright (c) 2015, 2017 Red Hat, Inc. 
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.AbstractEngineRepoProvider;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.internal.util.HttpUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class NpmBasedEngineRepoProvider extends AbstractEngineRepoProvider{

	private static final String NPM_URL ="https://registry.npmjs.org/cordova-{0}";
	
	@Override
	public List<DownloadableCordovaEngine> getEngines() throws CoreException {
		List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
		List<DownloadableCordovaEngine> allEngines = new ArrayList<DownloadableCordovaEngine>();
		for (PlatformSupport support : platforms) {
			allEngines.addAll(getEngines(support.getPlatformId()));
		}
		return allEngines;
	}
	
	@Override
	public List<DownloadableCordovaEngine> getEngines(String engineId) throws CoreException {
		List<DownloadableCordovaEngine> platEngines = getPlatformEngines(engineId);
		if(platEngines == null){
			return new ArrayList<>();
		}
		return platEngines;
	}
	
	
	private List<DownloadableCordovaEngine> getPlatformEngines(String platformId) throws CoreException{
		try {
			InputStream stream = HttpUtil.getHttpStream(NLS.bind(NPM_URL, platformId));
			return parseEngines(stream, platformId);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind(
					"Could not retrieve and parse downloadable platform information for ({0})", platformId),e));
		}
	}
	
	private List<DownloadableCordovaEngine> parseEngines(InputStream stream, String platformId) throws IOException, CoreException{
		List<DownloadableCordovaEngine> engines = new ArrayList<DownloadableCordovaEngine>();
		JsonReader reader = null;
		try {
			reader = new JsonReader(new InputStreamReader(stream));
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(reader);
			if(!jsonElement.isJsonObject()){
				throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, 
						"cannot parse object bacuse it is not a Json object"));
			}
			final JsonObject root = jsonElement.getAsJsonObject();
			final JsonElement element = root.get("versions");
			final JsonObject topVersions = element.getAsJsonObject();
			final Set<Entry<String, JsonElement>> versions =  topVersions.entrySet();
			for (Iterator<Entry<String, JsonElement>> iterator = versions.iterator(); iterator.hasNext();) {
				Entry<String, JsonElement> entry = iterator.next();
				JsonObject v = entry.getValue().getAsJsonObject();
				DownloadableCordovaEngine engine = new DownloadableCordovaEngine();
				engine.setVersion(v.get("version").getAsString());
				engine.setPlatformId(platformId);
				JsonObject dist = v.get("dist").getAsJsonObject();
				engine.setDownloadURL(dist.get("tarball").getAsString());
				engines.add(engine);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return engines;
	}
}