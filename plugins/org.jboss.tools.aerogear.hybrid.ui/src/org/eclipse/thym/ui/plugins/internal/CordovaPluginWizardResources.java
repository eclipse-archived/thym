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

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class CordovaPluginWizardResources {
	private static final String COLOR_DARK_GRAY = "dark_gray";
	private static final String COLOR_DARK_LIST_BACKGROUND = "dark_list_background";
	
	private final ResourceManager resourceManager;
	private final FontDescriptor h1FontDescriptor;
	private final FontDescriptor h2FontDescriptor;
	private final FontDescriptor subFontDescriptor;
	private Color colorDisabled;
	private Color darkListBackground;
	
	public CordovaPluginWizardResources(Display display) {
		this.resourceManager = new LocalResourceManager(JFaceResources.getResources(display));
		this.h1FontDescriptor = createFontDescriptor(SWT.BOLD, 1.25f);
		this.h2FontDescriptor = createFontDescriptor(SWT.BOLD, 1.15f);
		this.subFontDescriptor = createFontDescriptor(SWT.NONE, 0.75f);
	}

	private FontDescriptor createFontDescriptor(int style, float heightMultiplier) {
		Font baseFont = JFaceResources.getDialogFont();
		FontData[] fontData = baseFont.getFontData();
		FontData[] newFontData = new FontData[fontData.length];
		for (int i = 0; i < newFontData.length; i++) {
			newFontData[i] = new FontData(fontData[i].getName(), (int) (fontData[i].getHeight() * heightMultiplier), fontData[i].getStyle() | style);
		}
		return FontDescriptor.createFrom(newFontData);
	}

	public Font getHeaderFont() {
		return resourceManager.createFont(h1FontDescriptor);
	}

	public Font getSmallHeaderFont() {
		return resourceManager.createFont(h2FontDescriptor);
	}
	
	public Font getSubTextFont(){
		return resourceManager.createFont(subFontDescriptor);
	}
	
	public Color getDisabledColor(){
		if(colorDisabled == null){
			ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
			if (!colorRegistry.hasValueFor(COLOR_DARK_GRAY)) {
				colorRegistry.put(COLOR_DARK_GRAY, new RGB(0x69, 0x69, 0x69));
			}
			colorDisabled = colorRegistry.get(COLOR_DARK_GRAY);
		}
		return colorDisabled;
	}

	public Color getDarkListBackGroundColor() {
		if(darkListBackground == null ){
			Color lightColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
			int shift = -10;
			// Determine a dark color by shifting the list color
			ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
			if(!colorRegistry.hasValueFor(COLOR_DARK_LIST_BACKGROUND)){
				RGB darkRGB = new RGB(Math.max(0, lightColor.getRed() + shift), Math.max(0, lightColor.getGreen() + shift), Math.max(0, lightColor.getBlue() + shift));
				JFaceResources.getColorRegistry().put(COLOR_DARK_LIST_BACKGROUND, darkRGB);
			}
			darkListBackground = colorRegistry.get(COLOR_DARK_LIST_BACKGROUND);
		}
		return darkListBackground;
	}

	
}
