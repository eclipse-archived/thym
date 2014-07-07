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
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;

public class NativeProjectBuilder extends ExtensionPointProxy{
	
	public static final String EXTENSION_POINT_ID = "org.eclipse.thym.core.projectBuilder";
	public static final String ATTR_PLATFORM = "platform";
	public static final String ATTR_DELEGATE = "delegate";
	public static final String ATTR_ID="id";

	private String id;
	private String platform;
	private Expression expression;

	NativeProjectBuilder(IConfigurationElement element) {
		super(element);
		this.id = element.getAttribute(ATTR_ID);
		this.platform = element.getAttribute(PlatformSupport.ATTR_PLATFORM);
		configureEnablement(element.getChildren(ExpressionTagNames.ENABLEMENT));

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
	
	public AbstractNativeBinaryBuildDelegate createDelegate(IProject project, File destination) throws CoreException{
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensions(contributor);
		if(extensions == null )
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"Contributing platform is no longer available."));
		for (int i = 0; i < extensions.length; i++) {
			if(extensions[i].getExtensionPointUniqueIdentifier().equals(EXTENSION_POINT_ID)){
				IConfigurationElement[] configs = extensions[i].getConfigurationElements();
				for (int j = 0; j < configs.length; j++) {
					if(configs[j].getAttribute(ATTR_PLATFORM).equals(getPlatform())){
						AbstractNativeBinaryBuildDelegate delegate = (AbstractNativeBinaryBuildDelegate) configs[j].createExecutableExtension(ATTR_DELEGATE);
						delegate.init(project, destination);
						return delegate;
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"Contributing platform has changed"));
	}
}
