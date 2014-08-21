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
package org.eclipse.thym.ui.plugins.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public abstract class BaseCordovaPluginItem<T> extends ControlListItem<T>{

	protected final CordovaPluginWizardResources resources;

	public BaseCordovaPluginItem(Composite parent, T element, CordovaPluginWizardResources resources) {
		super(parent, SWT.NULL, element);
		this.resources = resources;
	}
	
	@Override
	public void setSelected(boolean select) {
		// avoid selection since CHECK is used instead
		// this also avoids redundant calls to updateColors()
	}
	
	@Override
	public void updateColors(int index) {
		if (index % 2 == 0) {
			setBackground(resources.getDarkListBackGroundColor());
		} else {
			setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		}
	}
}
