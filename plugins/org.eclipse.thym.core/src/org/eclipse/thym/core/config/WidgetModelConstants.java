/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *     Contributors:
 *          Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.config;
/**
 * Constants used by the Widget model
 * 
 * @author Gorkem Ercan
 */
interface WidgetModelConstants {
    
    static final String NS_W3C_WIDGET = "http://www.w3.org/ns/widgets"; 

    
    static final String WIDGET_TAG_AUTHOR = "author";
    static final String WIDGET_TAG_CONTENT = "content";
    static final String WIDGET_TAG_DESCRIPTION = "description";
    static final String WIDGET_TAG_NAME = "name";
    static final String WIDGET_TAG_PREFERENCE = "preference";
    static final String WIDGET_TAG_FEATURE = "feature";
    static final String WIDGET_TAG_ACCESS = "access";
    static final String WIDGET_TAG_PLUGIN = "plugin";
    static final String WIDGET_TAG_ICON = "icon";
    static final String WIDGET_TAG_SPLASH = "splash";
    static final String WIDGET_TAG_LICENSE = "license";
    static final String WIDGET_TAG_PLATFORM = "platform";
    static final String WIDGET_TAG_ENGINE = "engine";
    
    static final String WIDGET_ATTR_VERSION = "version";
    static final String WIDGET_ATTR_ID = "id";
    static final String WIDGET_ATTR_VIEWMODES = "viewmodes";
    static final String ACCESS_ATTR_BROWSER_ONLY = "browserOnly";
    static final String ACCESS_ATTR_SUBDOMAINS = "subdomains";
    static final String ACCESS_ATTR_ORIGIN = "origin";
    static final String AUTHOR_ATTR_EMAIL = "email";
    static final String AUTHOR_ATTR_HREF = "href";
    static final String CONTENT_ATTR_SRC = "src";
    static final String CONTENT_ATTR_TYPE = "type";
    static final String CONTENT_ATTR_ENCODING = "encoding";
    static final String FEATURE_ATTR_NAME = "name";
    static final String FEATURE_ATTR_REQUIRED = "required";
    static final String IMAGERESOURCE_ATTR_DENSITY = "density";
    static final String IMAGERESOURCE_ATTR_PLATFORM = "platform";
    static final String IMAGERESOURCE_ATTR_HEIGHT = "height";
    static final String IMAGERESOURCE_ATTR_WIDTH = "width";
    static final String IMAGERESOURCE_ATTR_SRC = "src";
    static final String LICENSE_ATTR_HREF = "href";
    static final String FEATURE_PARAM_TAG = "param";
    static final String PLUGIN_ATTR_NAME = "name";
    static final String PLUGIN_ATTR_SPEC = "spec";
    static final String PARAM_ATTR_NAME = "name";
    static final String PARAM_ATTR_VALUE = "value";
    static final String PREFERENCE_ATTR_VALUE = "value";
    static final String PREFERENCE_ATTR_NAME = "name";
    static final String PREFERENCE_ATTR_READONLY = "readonly";
    static final String NAME_ATTR_SHORT = "short";
    static final String PLATFORM_ATTR_NAME = "name";
    static final String ENGINE_ATTR_NAME = "name";
    /**
     * @deprecated use {@link #ENGINE_ATTR_SPEC}
     */
    static final String ENGINE_ATTR_VERSION = "version";
    static final String ENGINE_ATTR_SPEC = "spec";
}
