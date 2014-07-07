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
package org.eclipse.thym.core.config;

import org.w3c.dom.Node;
/**
 * Icon tag on a config.xml
 * 
 * @author Gorkem Ercan
 */
public class Icon extends ImageResourceBase {

	Icon(Node node) {
		super(node);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Icon))
			return false;
		return super.equals(obj);
	}

}
