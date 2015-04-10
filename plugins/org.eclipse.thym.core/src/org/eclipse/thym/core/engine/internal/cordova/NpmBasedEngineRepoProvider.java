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
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
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
	
	private InputStream getRemoteJSonStream(String url) throws IOException{
		try {
			// SSLSocketFactory to patch HTTPClient's that are earlier than 4.3.2
			// to enable SNI support.
			SSLSocketFactory factory = new SSLSocketFactory(SSLContext.getDefault()){
				@Override
				public Socket createSocket() throws IOException {
					return SocketFactory.getDefault().createSocket();
				}
				@Override
				public Socket createSocket(HttpParams params) throws IOException {
					return SocketFactory.getDefault().createSocket();
				}
			};
			DefaultHttpClient client = new DefaultHttpClient();
			HttpUtil.setupProxy(client);
			client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, factory));
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			return entity.getContent();
		} catch (NoSuchAlgorithmException e) {
			HybridCore.log(IStatus.ERROR, "Error creating the SSL Factory ", e);
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
	
	private List<DownloadableCordovaEngine> getPlatformEngines(String platformId) throws CoreException{
		try {
			InputStream stream = getRemoteJSonStream(NLS.bind(NPM_URL, platformId));
			if (stream != null) {
				return parseEngines(stream, platformId);
			}else{
				return null;
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind(
					"Could not retrieve and parse downloadable platform information for ({0})", platformId),e));
		}
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
