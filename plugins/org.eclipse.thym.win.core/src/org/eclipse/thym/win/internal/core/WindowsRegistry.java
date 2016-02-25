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
package org.eclipse.thym.win.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.eclipse.thym.win.core.WPCore;

/**
 * Utility class for accessing Windows registry.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
@SuppressWarnings("restriction")
public class WindowsRegistry {

	private static class ValueParser implements IStreamListener {

		private static final String REG_SZ = "REG_SZ"; //$NON-NLS-1$

		private StringBuffer buffer = new StringBuffer();
		private String key;

		public ValueParser(String key) {
			this.key = key;
		}

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
		}

		/**
		 * @return string value of specified key or <code>null</code> if key
		 *         could not be found
		 */
		public String getValue() {
			if (buffer == null || buffer.length() < 1) {
				return null;
			}
			StringReader reader = new StringReader(buffer.toString());
			BufferedReader read = new BufferedReader(reader);
			String line = null;
			try {
				while ((line = read.readLine()) != null) {
					line = line.trim();
					if (line.startsWith(key) && line.indexOf(REG_SZ) != -1) {
						String[] segments = line.split(REG_SZ);
						return segments[1].trim();
					}
				}
			} catch (IOException e) {
				WPCore.log(IStatus.ERROR,
						"Error parsing the String value from Windows Registry", //$NON-NLS-1$
						e);
			}
			return null;
		}

	}

	private static class ChildrenParser implements IStreamListener {

		private StringBuffer buffer = new StringBuffer();

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
		}

		/**
		 * @return array of key's children names
		 */
		public String[] getChildren() {
			if (buffer == null || buffer.length() < 1) {
				return null;
			}
			StringReader reader = new StringReader(buffer.toString());
			BufferedReader read = new BufferedReader(reader);
			List<String> children = new ArrayList<String>();
			String line = null;
			try {
				while ((line = read.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0) {
						IPath path = new Path(line.trim());
						children.add(path.lastSegment());
					}
				}
			} catch (IOException e) {
				WPCore.log(IStatus.ERROR,
						"Error parsing the String value from Windows Registry", //$NON-NLS-1$
						e);
			}
			return children.toArray(new String[children.size()]);
		}

	}

	/**
	 * Get string value the key for specified location in Windows Registry.
	 * 
	 * @param location
	 *            path in the registry
	 * @param key
	 *            registry key
	 * @return registry value or <code>null</code> if key could not be found
	 * @throws CoreException
	 */
	public static String readRegistry(String location, String key)
			throws CoreException {
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		ValueParser parser = new ValueParser(key);
		processUtility.execSync("reg query " + addQuotes(location) + " /v " //$NON-NLS-1$ //$NON-NLS-2$
				+ addQuotes(key), null, parser, parser,
				new NullProgressMonitor(), null, null);
		return parser.getValue();
	}

	/**
	 * Get list of children names of specified location in Windows Registry.
	 * 
	 * @param location
	 *            path in the registry
	 * @return
	 * @throws CoreException
	 */
	public static String[] getChildren(String location) throws CoreException {
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		ChildrenParser parser = new ChildrenParser();
		processUtility.execSync("reg query " + addQuotes(location), null, //$NON-NLS-1$
				parser, parser, new NullProgressMonitor(), null, null);
		return parser.getChildren();
	}

	private static String addQuotes(String input) {
		return "\"" + input + "\""; //$NON-NLS-1$//$NON-NLS-2$
	}

}
