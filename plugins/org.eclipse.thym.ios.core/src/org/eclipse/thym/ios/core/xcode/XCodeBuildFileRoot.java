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

import com.dd.plist.ASCIIPropertyListParser;
import com.dd.plist.NSDictionary;
/**
 * The root object on the XCode build file. XCode build file is a ASCII style plist file. 
 * At the root of the build file is a dictionary(NSDictionary) which holds the following values.
 * <table>
 * <tr>
 *    <td>KEY</td>
 *    <td>TYPE</td>
 *    <td>COMMENT</td>
 * </tr>
 * <tr>
 *    <td>archiveVersion</td>
 *    <td>NSNumeric</td>
 *    <td>Defaults to 1</td>
 * </tr>
 * <tr>
 *    <td>classes</td>
 *    <td>NSDictionary</td>
 *    <td>Empty</td>
 * </tr>
 * <tr>
 *    <td>objectVersion</td>
 *    <td>NSNumberic</td>
 *    <td>XCode compatibility version</td>
 * </tr>
 * <tr>
 *    <td>objects</td>
 *    <td>NSDictionary</td>
 *    <td>The structure of the project is represented by the
 *			children of this property.Keyed by unique uppercase hexadecimal characters</td>
 * </tr>
 * <tr>
 *    <td>rootObject</td>
 *    <td>NSString</td>
 *    <td>The object is a reference to a PBXProject element</td>
 * </tr>
 * </table>
 * @author Gorkem Ercan
 *
 */
public class XCodeBuildFileRoot {
	private NSDictionary dictionary;

	
	public XCodeBuildFileRoot(File file){
		try {
			NSDictionary root = (NSDictionary)ASCIIPropertyListParser.parse(file);
			this.dictionary = root;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	public String toASCII(){
		return dictionary.toASCIIPropertyList();
	}
	

	
}
