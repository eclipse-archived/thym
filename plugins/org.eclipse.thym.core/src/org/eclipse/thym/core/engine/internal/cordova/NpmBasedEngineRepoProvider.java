/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. 
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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
	
	protected InputStream getRemoteJSonStream(String url){
		try {
			IProxyData[] proxies =  HttpUtil.getEclipseProxyData(new URI(url));
			HttpHost proxyHost = null;
			for (IProxyData data : proxies) {
				if(data.getType().equals(IProxyData.HTTP_PROXY_TYPE)){
					proxyHost = new HttpHost(data.getHost(),data.getPort());
				}
			}
			return Request.Get(url).viaProxy(proxyHost).execute().returnContent().asStream();
		} catch (IOException | URISyntaxException e) {
			HybridCore.log(IStatus.ERROR, "Unable to retrieve downloadable platform information", e);
		}
		return null;
	}
	
	@Override
	public List<DownloadableCordovaEngine> getEngines() throws CoreException {
		List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
		List<DownloadableCordovaEngine> allEngines = new ArrayList<DownloadableCordovaEngine>();
		for (PlatformSupport support : platforms) {
			List<DownloadableCordovaEngine> platEngines = getPlatformEngines(support.getPlatformId());
			if(platEngines != null && !platEngines.isEmpty()){
				allEngines.addAll(platEngines);
			}
		}
		return allEngines;
	}
	
	private List<DownloadableCordovaEngine> getPlatformEngines(String platformId){
		InputStream stream = getRemoteJSonStream(NLS.bind(NPM_URL, platformId));
		try {
			if (stream != null && stream.available() >0) {
				return parseEngines(stream, platformId);
			}
		} catch (IOException e) {
			HybridCore.log(IStatus.WARNING, NLS.bind(
					"Could not retrieve the downloadable platform information for ({0})", platformId), e);
		}
		return null;
	}
	
	private List<DownloadableCordovaEngine> parseEngines(InputStream stream, String platformId) throws IOException{
		List<DownloadableCordovaEngine> engines = new ArrayList<DownloadableCordovaEngine>();
		JsonReader reader = null;
		try {
			reader = new JsonReader(new InputStreamReader(stream));
			JsonParser parser = new JsonParser();
			final JsonObject root = (JsonObject) parser.parse(reader);
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
