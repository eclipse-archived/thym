/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.platform;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;

/**
 * Property tester checks if receiver is HybridProject with specified cordova platform
 * @author rawagner
 *
 */
public class PlatformPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(receiver instanceof IProject){
			IProject project = ((IProject)receiver);
			HybridProject hybridProject = HybridProject.getHybridProject(project);
			if(hybridProject != null){
				HybridMobileEngine[] engines = hybridProject.getActiveEngines();
				if(engines != null){
					for(HybridMobileEngine engine: engines){
						if(engine.getId().equals(expectedValue)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
