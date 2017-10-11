/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.plugin.registry.plugin;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class PluginVersionDeserializer implements JsonDeserializer<List<CordovaRegistryPluginVersion>> {

	@Override
	public List<CordovaRegistryPluginVersion> deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {

		List<CordovaRegistryPluginVersion> result = new ArrayList<>();
		Set<Entry<String, JsonElement>> entrySet = ((JsonObject) json).entrySet();
		for (Entry<String, JsonElement> element : entrySet) {
			CordovaRegistryPluginVersion version = context.deserialize(element.getValue(),
					CordovaRegistryPluginVersion.class);
			result.add(version);
		}
		return result;
	}

}
