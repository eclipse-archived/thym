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
package org.eclipse.thym.ui;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
/**
 * Proxy object for the platformImages extension point.
 * 
 * @author Gorkem Ercan
 *
 */
public class PlatformImage {
	
	private static final String ATTR_ICON = "icon";
	public static final String ATTR_PLATFORM_SUPPORT="platformSupport";
	public static final String ATTR_PROJECT_BUILDER="projectBuilder";
	public static final String EXTENSION_POINT_ID= "org.eclipse.thym.ui.platformImages";
	public static final String IMAGE_REG_BASE= HybridUI.PLUGIN_ID + ".platformImage";

	private ImageDescriptor icon;
	private String projectGeneratorID;
	private String projectBuilderID;
	
	PlatformImage(IConfigurationElement configurationElement) {
		String iconPath = configurationElement.getAttribute(ATTR_ICON);
		icon= HybridUI.getImageDescriptor(configurationElement.getContributor().getName(), iconPath);
		projectGeneratorID = configurationElement.getAttribute(ATTR_PLATFORM_SUPPORT);
		projectBuilderID = configurationElement.getAttribute(ATTR_PROJECT_BUILDER);
		
	}

	public ImageDescriptor getIcon() {
		return icon;
	}

	public String getProjectGeneratorID() {
		return projectGeneratorID;
	}

	public String getProjectBuilderID() {
		return projectBuilderID;
	}
	/**
	 * Returns the {@link ImageDescriptor} for the attribute and platform id. This is not cached 
	 * and it is recommended to use {@link #getImageFor(String, String)} for cached and managed 
	 * image use.
	 * 
	 * @param attribute
	 * @param id
	 * @return
	 */
	public static ImageDescriptor getIconFor(String attribute, String id ){
		List<PlatformImage> images = HybridUI.getPlatformImages();
		for (PlatformImage platformImage : images) {			
			if(attribute.equals(ATTR_PLATFORM_SUPPORT) && id.equals(platformImage.getProjectGeneratorID())){
				return platformImage.getIcon();
			}
			if(attribute.equals(ATTR_PROJECT_BUILDER) && id.equals(platformImage.getProjectBuilderID())){
				return platformImage.getIcon();
			}
		}
		return null;
	}
	/**
	 * Returns the image for attribute and platform id. Image is cached by {@link JFaceResources}
	 * {@link ImageRegistry}
	 * 
	 * @param attribute
	 * @param id
	 * @return
	 */
	public static Image getImageFor(String attribute, String id){
		String key = IMAGE_REG_BASE +attribute+id;
		ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
		Image image = imageRegistry.get(key);
		if(image == null ){
			ImageDescriptor desc = getIconFor(attribute, id);
			if(desc == null ) return null;
			imageRegistry.put(key, getIconFor(attribute, id));
			image = imageRegistry.get(key);
		}
		return image;
	}

}
