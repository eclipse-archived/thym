/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.core.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.AbstractEngineRepoProvider;

/**
 * Proxy object for the org.eclipse.thym.core.cordovaEngineRepoProvider
 * extension point.
 * 
 * @author Wojciech Galanciak, 2014
 *
 */
public class CordovaEngineRepoProvider extends ExtensionPointProxy {

	public static final String EXTENSION_POINT_ID = "org.eclipse.thym.core.cordovaEngineRepoProvider";

	private static final String ATTR_ID = "id";
	private static final String PRODUCT_ID = "productId";
	private static final String PROVIDER_ID = "provider";

	private String id;
	private String productId;

	CordovaEngineRepoProvider(IConfigurationElement element) {
		super(element);
		this.id = element.getAttribute(ATTR_ID);
		this.productId = element.getAttribute(PRODUCT_ID);
	}

	/**
	 * @return provider's id
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * @return id of the product for which this repository provider replaces a default one.
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * Create {@link AbstractEngineRepoProvider} instance base on provider class
	 * specified in extension's configuration.
	 * 
	 * @return {@link AbstractEngineRepoProvider} instance
	 * @throws CoreException
	 */
	public AbstractEngineRepoProvider createProvider() throws CoreException {
		IExtension[] extensions = Platform.getExtensionRegistry()
				.getExtensions(contributor);
		if (extensions == null)
			throw new CoreException(new Status(IStatus.ERROR,
					HybridCore.PLUGIN_ID,
					"Contributing platform is no longer available."));
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].getExtensionPointUniqueIdentifier().equals(
					EXTENSION_POINT_ID)) {
				IConfigurationElement[] configs = extensions[i]
						.getConfigurationElements();
				for (int j = 0; j < configs.length; j++) {
					if (configs[j].getAttribute(ATTR_ID).equals(getID())) {
						AbstractEngineRepoProvider provider = (AbstractEngineRepoProvider) configs[j]
								.createExecutableExtension(PROVIDER_ID);
						return provider;
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,
				"Contributing platform has changed"));
	}

}