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
package org.eclipse.thym.ui.wizard.export;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.thym.ui.PlatformImage;
import org.eclipse.thym.core.extensions.NativeProjectBuilder;

public class NativeProjectBuilderLabelProvider extends BaseLabelProvider
		implements ILabelProvider {
	private HashMap<String, Image> imageCache = new HashMap<String, Image>();
	@Override
	public Image getImage(Object element) {
		
		NativeProjectBuilder builder = (NativeProjectBuilder)element;
		Image img = imageCache.get(builder.getID());
		if(img != null ){
			return img;
		}
		ImageDescriptor imgDesc =PlatformImage.getIconFor(PlatformImage.ATTR_PROJECT_BUILDER, builder.getID());
		if(imgDesc != null){
			img= imgDesc.createImage();
			imageCache.put(builder.getID(), img);
		}
		return img;
	}

	@Override
	public String getText(Object element) {
		NativeProjectBuilder builder = (NativeProjectBuilder) element;
		return builder.getPlatform();
	}

}
