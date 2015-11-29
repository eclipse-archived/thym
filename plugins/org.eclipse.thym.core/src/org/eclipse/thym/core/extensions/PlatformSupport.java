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
package org.eclipse.thym.core.extensions;

import java.io.File;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.platform.AbstractProjectGeneratorDelegate;
import org.osgi.framework.Bundle;

/**
 * Proxy object for the org.eclipse.thym.core.platformSupport extension point. 
 * 
 * @author Gorkem Ercan
 *
 */
public class PlatformSupport extends ExtensionPointProxy {
	public final static String ASSEMBLY_ROOT = "/proj_gen/";
	
	public static final String EXTENSION_POINT_ID = "org.eclipse.thym.core.platformSupport";
	public static final String ATTR_PLATFORM = "platform";
	public static final String ATTR_PROJECT_GENERATOR = "projectGenerator";
	public static final String ATTR_LIBRARY_RESOLVER = "libraryResolver";
	public static final String ATTR_ID="id";
	private static final String ATTR_PLATFORM_ID = "platformID";
	private String platform;
	private Expression expression;
	private String id;
	private String platformId;


	PlatformSupport(IConfigurationElement configurationElement ){
		super(configurationElement);
		this.id = configurationElement.getAttribute(ATTR_ID);
		this.platform = configurationElement.getAttribute(PlatformSupport.ATTR_PLATFORM);
		this.platformId=configurationElement.getAttribute(PlatformSupport.ATTR_PLATFORM_ID);
		configureEnablement(configurationElement.getChildren(ExpressionTagNames.ENABLEMENT));
	}

	private void configureEnablement(IConfigurationElement[] enablementNodes) {
		if(enablementNodes == null || enablementNodes.length < 1 ) return;
		IConfigurationElement node = enablementNodes[0];
		try {
			 expression = ExpressionConverter.getDefault().perform(node);
			
		} catch (CoreException e) {
			HybridCore.log(IStatus.ERROR, "Error while reading the enablement", e);
		}
	}

	/**
	 * Creates a project generator delegate for the given project and destination. 
	 * 
	 * @param project
	 * @param generationFolder
	 * @return
	 * @throws CoreException - if the extension point has changed and no longer valid
	 */
	public AbstractProjectGeneratorDelegate createDelegate(IProject project, File generationFolder) throws CoreException{
		if(generationFolder == null ){
			generationFolder = new File(getTempGenerationDirectory(),getPlatformId());
		}
		IExtension[] extensions = getContributorExtensions();
		for (int i = 0; i < extensions.length; i++) {
			if(extensions[i].getExtensionPointUniqueIdentifier().equals(EXTENSION_POINT_ID)){
				IConfigurationElement[] configs = extensions[i].getConfigurationElements();
				for (int j = 0; j < configs.length; j++) {
					if(configs[j].getAttribute(ATTR_PLATFORM_ID).equals(getPlatformId())){
						AbstractProjectGeneratorDelegate delegate = (AbstractProjectGeneratorDelegate) configs[j].createExecutableExtension(ATTR_PROJECT_GENERATOR);
						delegate.init(project, generationFolder, platformId);
						return delegate;
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"Contributing platform has changed"));
	}

	private IExtension[] getContributorExtensions() throws CoreException {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensions(contributor);
		if(extensions == null )
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"Contributing platform is no longer available."));
		return extensions;
	}

	public String getPlatform() {
		return platform;
	}
	
	public String getID(){
		return id;
	}

	public boolean isEnabled(IEvaluationContext context) throws CoreException{
		if(expression == null ) return true;
		if(context == null ){
			throw new IllegalArgumentException("Must have an evalutation context");
		}
		return (this.expression.evaluate(context) == EvaluationResult.TRUE);
	}

	public HybridMobileLibraryResolver getLibraryResolver() throws CoreException{
		IExtension[] extensions = getContributorExtensions();
		for (int i = 0; i < extensions.length; i++) {
			if(extensions[i].getExtensionPointUniqueIdentifier().equals(EXTENSION_POINT_ID)){
				IConfigurationElement[] configs = extensions[i].getConfigurationElements();
				for (int j = 0; j < configs.length; j++) {
					if(configs[j].getAttribute(ATTR_PLATFORM_ID).equals(getPlatformId())){
						HybridMobileLibraryResolver resolver = (HybridMobileLibraryResolver) configs[j].createExecutableExtension(ATTR_LIBRARY_RESOLVER);
						return resolver;
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"Contributing platform has changed"));
	}

	public String getPlatformId() {
		return platformId;
	}
	
	protected File getTempGenerationDirectory(){
		Bundle bundle = HybridCore.getContext().getBundle();
		return bundle.getDataFile(ASSEMBLY_ROOT);
	}
	
}
