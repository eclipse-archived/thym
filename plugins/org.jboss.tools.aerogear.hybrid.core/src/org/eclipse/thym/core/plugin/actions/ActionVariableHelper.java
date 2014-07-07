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
package org.eclipse.thym.core.plugin.actions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Preference;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;

public final class ActionVariableHelper {
	
	
	
	public static String replaceVariables(HybridProject project, String xml ) throws CoreException{
		WidgetModel model = WidgetModel.getModel(project);
		Widget widget = model.getWidgetForRead();
		xml = xml.replaceAll("\\$PACKAGE_NAME", widget.getId());
		List<Preference> preferences = widget.getPreferences();
		if (preferences != null) {
			for (Preference preference : preferences) {
				String preferenceKey = "\\$" + preference.getName();
				if (xml.contains(preferenceKey)) {
					xml = xml.replaceAll(preferenceKey, preference.getValue());
				}
			}
		}
		return xml;
	}

}
