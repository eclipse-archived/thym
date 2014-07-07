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
package org.eclipse.thym.ui.internal.projectGenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
/**
 * IStructuredContentProvider implementation for the {@link PlatformSupport} extension point. 
 * If a List of ProjectGenerators are passed to a ContentViewer this ProjectGenerator will use the 
 * input otherwise it will query the  {@link PlatformSupport#EXTENSION_POINT_ID} for a list.
 * 
 * @author Gorkem Ercan
 *
 */
public class ProjectGeneratorContentProvider implements IStructuredContentProvider {

	private List<PlatformSupport> platforms;


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(platforms == null ){
			platforms = HybridCore.getPlatformSupports();
		}
		ArrayList<PlatformSupport> elements = new ArrayList<PlatformSupport>();
		IEvaluationService service = (IEvaluationService)PlatformUI.getWorkbench().getService(IEvaluationService.class);
		for (PlatformSupport generator : platforms) {
			try {
				if(generator.isEnabled(service.getCurrentState())){
					elements.add(generator);
				}
				
			} catch (CoreException e) {
				HybridUI.log(IStatus.ERROR, "Error filtering objects", e);
			}
		}
		return elements.toArray();
	}

	@Override
	public void dispose() {
		
	}
}
