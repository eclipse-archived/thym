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
package org.eclipse.thym.core.plugin.registry.repo;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CordovaRegistrySearchPlugin {

	@SerializedName("keywords")
	@Expose
	private List<String> keywords = null;
	@SerializedName("author")
	@Expose
	private List<String> author = null;
	@SerializedName("name")
	@Expose
	private List<String> name = null;
	@SerializedName("description")
	@Expose
	private List<String> description = null;

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getAuthor() {
		return author.get(0);
	}

	public void setAuthor(List<String> author) {
		this.author = author;
	}

	public String getName() {
		return name.get(0);
	}

	public void setName(List<String> name) {
		this.name = name;
	}

	public String getDescription() {
		return description.get(0);
	}

	public void setDescription(List<String> description) {
		this.description = description;
	}

}