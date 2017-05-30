/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.platform;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


public interface PlatformConstants {
	
	public static final String DIR_WWW = "www";
	public static final String DIR_MERGES = "merges";
	public static final String DIR_PLUGINS = "plugins";
	public static final String DIR_DOT_CORDOVA = ".cordova";
	public static final String DIR_PLATFORMS = "platforms";
	public static final String DIR_NODE = "node_modules";
	
	public static final String FILE_JS_CORDOVA = "cordova.js";
	public static final String FILE_JS_CORDOVA_PLUGIN = "cordova_plugins.js";
	public static final String FILE_XML_CONFIG = "config.xml";
	public static final String FILE_JSON_CONFIG = "config.json";
	public static final String FILE_JSON_PLATFORMS = "platforms.json";
	/**
	 * plugin definition file for Cordova plugins
	 */
	public static final String FILE_XML_PLUGIN = "plugin.xml";
	
	public static final String HYBRID_UI_PLUGIN_ID = "org.eclipse.thym.ui";
	public static final String PREF_CUSTOM_LIB_LOCS = "custom_engine_loc";
	public static final String PREF_DEFAULT_ENGINE = "default_engine";
	public static final String PREF_SHRINKWRAP_PLUGIN_VERSIONS = "shrinkwrap_plugin_version";
	
	public static final IPath[] CONFIG_PATHS = {new Path(PlatformConstants.DIR_WWW).append(PlatformConstants.FILE_XML_CONFIG),
		new Path(PlatformConstants.FILE_XML_CONFIG) };
	public static final IPath PLATFORMS_JSON_PATH =
			new Path(PlatformConstants.DIR_PLATFORMS).append(FILE_JSON_PLATFORMS);
	
	public static final String[] DERIVED_SUBFOLDERS= {DIR_PLUGINS, DIR_PLATFORMS};
	public static final String[] DERIVED_FOLDERS= {DIR_NODE};

}
