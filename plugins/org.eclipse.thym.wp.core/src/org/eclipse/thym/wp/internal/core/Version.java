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
package org.eclipse.thym.wp.internal.core;

/**
 * Represents comparable <code>major.minor.build</code> version number.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class Version implements Comparable<Version> {

	public static final Version UNKNOWN = new Version(-1, -1, -1, null);
	private int major;
	private int minor;
	private int build;
	private String fullVersion;

	private Version(int major, int minor, int build, String fullVersion) {
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.fullVersion = fullVersion;
	}

	public String getName() {
		return major + "." + minor + "." + build; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getBuild() {
		return build;
	}

	public String toString() {
		return fullVersion;
	}

	public int compareTo(Version v) {
		if (getMajor() < v.getMajor()) {
			return -1;
		}
		if (getMajor() > v.getMajor()) {
			return 1;
		}
		if (getMinor() != -1 && v.getMinor() != -1) {
			if (getMinor() < v.getMinor()) {
				return -1;
			}
			if (getMinor() > v.getMinor()) {
				return 1;
			}
		}
		if (getBuild() != -1 && v.getBuild() != -1) {
			if (getBuild() < v.getBuild()) {
				return -1;
			}
			if (getBuild() > v.getBuild()) {
				return 1;
			}
		}
		return 0;
	}

	public static Version byName(String name) {
		if (name == null) {
			return UNKNOWN;
		}
		return parse(name);
	}

	private static Version parse(final String name) {
		String toParse = name.trim();
		if (name.equals("*")) { //$NON-NLS-1$
			return UNKNOWN;
		}
		// e.g. v2.0.0
		if (toParse.startsWith("v") || toParse.startsWith("V")) { //$NON-NLS-1$ //$NON-NLS-2$
			toParse = toParse.substring(1);
		}
		String[] segments = toParse.split("\\."); //$NON-NLS-1$
		int[] result = new int[4];
		for (int i = 0; i < result.length; i++) {
			if (segments.length > i) {
				if (segments[i].equalsIgnoreCase("x")) { //$NON-NLS-1$
					result[i] = 9999999;
				} else if (segments[i].equalsIgnoreCase("*")) { //$NON-NLS-1$
					result[i] = -1;
				} else {
					try {
						result[i] = Integer.valueOf(segments[i]);
					} catch (NumberFormatException e) {
						result[i] = -1;
					}
				}
			} else {
				result[i] = 0;
			}
		}
		return new Version(result[0], result[1], result[2], name);
	}

}