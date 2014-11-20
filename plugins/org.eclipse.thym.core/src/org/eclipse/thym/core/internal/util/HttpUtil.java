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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;

public class HttpUtil {
	
	/**
	 * Set the proxy settings from ProxyService.
	 * This method sets a {@link HttpRoutePlanner} to the client
	 * 
	 * @param client
	 */
	public static void setupProxy(final DefaultHttpClient client ){
		client.setRoutePlanner(new HttpRoutePlanner() {
			
			@Override
			public HttpRoute determineRoute(HttpHost target, HttpRequest request,
					HttpContext context) throws HttpException {
				
				//use forced route if one exists
				HttpRoute route = ConnRouteParams.getForcedRoute(request.getParams());
			   	if (route != null)
				   return route;
			   
			   	// if layered, is it secure?
			   	final Scheme scheme = client.getConnectionManager().getSchemeRegistry().getScheme(target);
			   	final boolean secure = scheme.isLayered();
			   	

				final IProxyService proxy =  HybridCore.getDefault().getProxyService();
				HttpHost host =null;
				if (proxy != null ) {
					try {
						IProxyData[] proxyDatas = proxy.select(new URI(target.toURI()));
						for (IProxyData data : proxyDatas) {
							if (data.getType().equals(IProxyData.HTTP_PROXY_TYPE)) {
								host = new HttpHost(data.getHost(), data.getPort());
								break;
							}
						}
					} catch (URISyntaxException e) {
						HybridCore.log(IStatus.ERROR, "Incorrect URI", e);
					}
				}
				if(host == null ){
					return new HttpRoute(target, null, secure);
				}
				return new HttpRoute(target, null, host, secure);
			}
		});
		
	}

}
