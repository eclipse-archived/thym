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
package org.eclipse.thym.ios.core.xcode;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.plugin.actions.XMLConfigFileAction;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * Inserts the given xml under the parent key
 * 
 * @author Gorkem Ercan
 *
 */
public class PlistConfigFileAction extends XMLConfigFileAction {

	public PlistConfigFileAction(File target, String parent, String xml) {
		super(target, parent, xml);
	}
	
	@Override
	protected Node getParentNode(Element root) throws CoreException {
		NodeList keys = root.getElementsByTagName("key");
		for (int i = 0; i < keys.getLength(); i++) {
			if(keys.item(i).getTextContent().equals(parent)){
				return keys.item(i);
			}
		}
		Element newKey = root.getOwnerDocument().createElement("key");
		newKey.setTextContent(parent);
		root.appendChild(newKey);
		return newKey;
	}

}
