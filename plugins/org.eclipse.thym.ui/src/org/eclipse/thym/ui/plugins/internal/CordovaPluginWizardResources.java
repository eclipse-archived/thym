/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.plugins.internal;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class CordovaPluginWizardResources {
	
	private final ResourceManager resourceManager;
	private final FontDescriptor h2FontDescriptor;
	private final FontDescriptor subFontDescriptor;
	private final FontDescriptor italicFontDescriptor;
	private final FontDescriptor h2ItalicFontDescriptor;
	
	public CordovaPluginWizardResources(Display display) {
		this.resourceManager = new LocalResourceManager(JFaceResources.getResources(display));
		this.h2FontDescriptor = createFontDescriptor(SWT.BOLD, 1.15f);
		this.h2ItalicFontDescriptor = createFontDescriptor(SWT.BOLD | SWT.ITALIC, 1.15f);
		this.subFontDescriptor = createFontDescriptor(SWT.NONE, 0.75f);
		this.italicFontDescriptor = createFontDescriptor(SWT.ITALIC, 1);
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

	public Font getSmallHeaderFont() {
		return resourceManager.createFont(h2FontDescriptor);
	}
	
	public Font getSmallItalicHeaderFont() {
		return resourceManager.createFont(h2ItalicFontDescriptor);
	}
	
	public Font getSubTextFont(){
		return resourceManager.createFont(subFontDescriptor);
	}
	
	public Font getItalicFont(){
		return resourceManager.createFont(italicFontDescriptor);
	}
}
