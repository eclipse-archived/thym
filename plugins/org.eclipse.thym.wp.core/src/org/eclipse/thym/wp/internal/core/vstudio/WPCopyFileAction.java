/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.wp.internal.core.vstudio;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.thym.core.plugin.actions.CopyFileAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Copy file action implementation for Windows Phone 8 platform. As an
 * additional step to standard copy action it does add proper entry to .csproj
 * file.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WPCopyFileAction extends CopyFileAction {

	private static final String DOT_CS = ".cs"; //$NON-NLS-1$
	private static final String DOT_XAML = ".xaml"; //$NON-NLS-1$

	private static final String PAGE = "Page"; //$NON-NLS-1$
	private static final String DEPENDENT_UPON = "DependentUpon"; //$NON-NLS-1$
	private static final String CONTENT = "Content"; //$NON-NLS-1$
	private static final String COMPILE = "Compile"; //$NON-NLS-1$
	private static final String GENERATOR = "Generator"; //$NON-NLS-1$
	private static final String SUB_TYPE = "SubType"; //$NON-NLS-1$
	private static final String INCLUDE = "Include"; //$NON-NLS-1$
	private static final String ITEM_GROUP = "ItemGroup"; //$NON-NLS-1$

	private File rootFolder;
	private Document document;

	public WPCopyFileAction(File source, File target, File rootFolder) {
		super(source, target);
		this.rootFolder = rootFolder;
	}

	@Override
	public void install() throws CoreException {
		super.install();
		File csprojFile = WPProjectUtils.getCsrojFile(rootFolder);
		if (csprojFile != null) {
			setDocument(WPProjectUtils.readXML(csprojFile));
			Element root = getDocument().getDocumentElement();
			Element itemGroup = getItemGroup();
			String name = target.getName();
			if (name.endsWith(DOT_CS)) {
				itemGroup.appendChild(getCompileInclude());
				if (name.endsWith(DOT_XAML + DOT_CS)) {
					itemGroup.appendChild(getPageInclude());
				}
			} else if (!name.endsWith(DOT_XAML)) {
				itemGroup.appendChild(getContentInclude());
			}
			root.appendChild(itemGroup);
			WPProjectUtils.writeXML(csprojFile, getDocument());
		}
	}

	private Document getDocument() {
		return document;
	}

	private void setDocument(Document document) {
		this.document = document;
	}

	private Element getItemGroup() {
		return getDocument().createElement(ITEM_GROUP);
	}

	private Node getPageInclude() {
		Element pageInclude = getDocument().createElement(PAGE);
		String relativePath = getRelativePath(target);
		relativePath = relativePath.substring(0, relativePath.indexOf(DOT_CS));
		pageInclude.setAttribute(INCLUDE, relativePath);
		Element subType = getDocument().createElement(SUB_TYPE);
		subType.setTextContent("Designer"); //$NON-NLS-1$
		pageInclude.appendChild(subType);
		Element generator = getDocument().createElement(GENERATOR);
		generator.setTextContent("MSBuild:Compile"); //$NON-NLS-1$
		pageInclude.appendChild(generator);
		return pageInclude;
	}

	private Element getCompileInclude() {
		Element compileInclude = getDocument().createElement(COMPILE);
		String relativePath = getRelativePath(target);
		compileInclude.setAttribute(INCLUDE, relativePath);
		if (relativePath.endsWith(DOT_XAML + DOT_CS)) {
			compileInclude.appendChild(getDependetUpon(relativePath));
		}
		return compileInclude;
	}

	private Element getContentInclude() {
		Element contentInclude = getDocument().createElement(CONTENT);
		String relativePath = getRelativePath(target);
		contentInclude.setAttribute(INCLUDE, relativePath);
		return contentInclude;
	}

	private Element getDependetUpon(String fullPath) {
		Element dependUpon = getDocument().createElement(DEPENDENT_UPON);
		IPath path = new Path(fullPath);
		String fileName = path.lastSegment();
		fileName = fileName.substring(0, fileName.indexOf(DOT_CS));
		dependUpon.setTextContent(fileName);
		return dependUpon;
	}

	private String getRelativePath(File target) {
		IPath targetPath = new Path(target.getAbsolutePath());
		IPath rootPath = new Path(rootFolder.getAbsolutePath());
		targetPath = targetPath.makeRelativeTo(rootPath);
		return targetPath.toString().replace('/', '\\');
	}

}
