/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.thym.core.HybridCore;

/**
 * Util class for all outgoing http connections
 *
 */
public class HttpUtil {
	
	/**
	 * Returns input stream from defined url. Connection uses cache and eclipse proxy, if defined
	 * @param url
	 * @return url input stream
	 * @throws IOException 
	 */
	public static InputStream getHttpStream(String url) throws IOException{
		URI target = URI.create(url);
		CloseableHttpClient client = getHttpClient(target);
		HttpGet get = new HttpGet(target);
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		StatusLine line = response.getStatusLine();
		if(line.getStatusCode() != 200){
			throw new IOException("HTTP response status is "+line.getStatusCode());
		}
		return entity.getContent();
	}
	
	@SuppressWarnings("restriction")
	private static CloseableHttpClient getHttpClient(URI url){
		CacheConfig cacheConfig = CacheConfig.custom()
        	.setMaxCacheEntries(1000)
        	.setMaxObjectSize(120*1024).setHeuristicCachingEnabled(true)
        	.setHeuristicDefaultLifetime(TimeUnit.HOURS.toSeconds(12))
        	.build();
		
		CachingHttpClientBuilder builder = CachingHttpClients.custom()
				.setCacheConfig(cacheConfig)
				.setHttpCacheStorage(new BundleHttpCacheStorage(HybridCore.getContext().getBundle()));
		
		builder = setupProxy(builder, url);
		return builder.build();
	}
	
	@SuppressWarnings("restriction")
	private static CachingHttpClientBuilder setupProxy(CachingHttpClientBuilder builder, URI url){
		final IProxyService proxyService = HybridCore.getDefault().getProxyService();
		if(proxyService != null ){
			IProxyData[] proxies = proxyService.select(url);
			if(proxies != null && proxies.length > 0){
				IProxyData proxy = proxies[0];
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				if(proxy.isRequiresAuthentication()){
					credsProvider.setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()), 
							new UsernamePasswordCredentials(proxy.getUserId(), proxy.getPassword()));
				}
				builder.setDefaultCredentialsProvider(credsProvider);
				builder.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
			}
		}
		return builder;
		
	}

}
