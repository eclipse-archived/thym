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
package org.eclipse.thym.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Provides utilities for checking against Hybrid mobile project conventions
 * such as the naming syntax
 * 
 * @author Gorkem Ercan
 *
 */
public class HybridProjectConventions {
		
	/**
	 * Validates if a project name is valid.
	 * 
	 * @param name
	 * @return
	 */
	public static IStatus validateProjectName(String name ){
		if(name == null || name.trim().isEmpty() )
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Project name must be specified");
		Pattern pattern  = Pattern.compile("[_a-zA-z][_a-zA-Z0-9]*");
		if(!pattern.matcher(name).matches()){
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind("{0} is not a valid application name", name));
		}
		return Status.OK_STATUS;

	}

	/**
	 * Validates if an application id is valid for use.
	 * 
	 * @param id
	 * @return
	 */
	public static IStatus validateProjectID(String id ){
		if(id == null || id.trim().isEmpty() )
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Application ID must be specified");

		Pattern pattern  = Pattern.compile("([a-zA-Z][a-zA-Z\\d_]*[\\.])+[a-zA-Z_][a-zA-Z\\d_]*");
		if( !pattern.matcher(id).matches()){
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind("{0} is not a valid application id", id));
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Validates if the application name is suitable for use on app stores.
	 * @param name
	 * @return
	 */
	public static IStatus validateApplicationName(String name ){
		if( name == null || name.trim().isEmpty() )
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Application name must be specified");
		
		if( name.length() >=25 ){
			// iTunes Store recommendation which is valid for other stores as well.
			// http://developer.apple.com/library/mac/#documentation/LanguagesUtilities/Conceptual/iTunesConnect_Guide/18_BestPractices/BestPractices.html
			return new Status(IStatus.WARNING, HybridCore.PLUGIN_ID,
					"Application names are recommended to have fewer than 25 characters for best presentation.");
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Generates a project ID given an Eclipse project name. 
	 * This method returns null of it can not determine the project ID
	 * @param projectName
	 * @return a project id or null
	 */
	public static String generateProjectID(String projectName) {
		if (projectName == null || projectName.isEmpty())
			return "";
		List<String> tokens = tokenizeProjectName(projectName);
		if(tokens.size() < 2 ){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (String string : tokens) {
			if (sb.length() > 0) {
				sb.append(".");
			}
			sb.append(string.toLowerCase());
		}
		return sb.toString();

	}
	/**
	 * Generates a cordova application name from a given Eclipse project name
	 * 
	 * @param projectName
	 * @return
	 */
	public static String generateApplicationName(String projectName) {
		if (projectName == null || projectName.isEmpty())
			return "";
		List<String> tokens = tokenizeProjectName(projectName);
		StringBuilder sb = new StringBuilder();
		for (String string : tokens) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(string);
		}
		return sb.toString();
	}
	/**
	 * Tokenize a project id. It considers all character type changes as 
	 * a new token.
	 * @param projectName
	 * @return
	 */
	private static List<String> tokenizeProjectName(String projectName) {
		char[] c = projectName.toCharArray();	
        List<String> list = new ArrayList<String>();
        int tokenStart = 0;
        int currentType = Character.getType(c[tokenStart]);
        for (int pos = tokenStart + 1; pos < c.length; pos++) {
               int type = Character.getType(c[pos]);
               if ((type == currentType && c[pos] != '_') || type == Character.DECIMAL_DIGIT_NUMBER){
                    continue;
                }
                if (type == Character.LOWERCASE_LETTER && currentType == Character.UPPERCASE_LETTER) {//Adds all upper case
                    int newTokenStart = pos - 1;
                    if (newTokenStart != tokenStart) {
                        list.add(new String(c, tokenStart, newTokenStart - tokenStart));
                         tokenStart = newTokenStart;
                    }
                } else {
                	list.add(new String(c, tokenStart, pos - tokenStart));
                    if(!Character.isJavaIdentifierStart(c[pos]) && !Character.isJavaIdentifierPart(c[pos]) || c[pos] == '_'){
                    	tokenStart = Math.min(pos+1, c.length-1);
                    	type = Character.getType(c[tokenStart]);
                    }else{
                    	tokenStart = pos;
                    }
                }
                currentType = type;
            }
            list.add(new String(c, tokenStart, c.length - tokenStart));
		return list;
	}
	
}
