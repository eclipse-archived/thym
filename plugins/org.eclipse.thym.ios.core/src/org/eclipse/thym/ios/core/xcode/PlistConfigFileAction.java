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
import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.internal.util.XMLUtil;
import org.eclipse.thym.core.platform.IPluginInstallationAction;
import org.eclipse.thym.core.plugin.CordovaPluginXMLHelper;
import org.eclipse.thym.ios.core.IOSCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
/**
 * Inserts the given xml under the parent key
 * 
 * @author Gorkem Ercan
 *
 */
public class PlistConfigFileAction implements IPluginInstallationAction {
	private final File target;
	private final String key;
	private final String xml;

	public PlistConfigFileAction(File target, String parent, String xml) {
		this.target = target;
		this.key = parent;
		this.xml = xml;
	}

	@Override
	public String[] filesToOverwrite() {
		return new String[0];
	}

	@Override
	public void install() throws CoreException {
		try {
			NSObject valueObject = getValueObject();
			NSDictionary dict = (NSDictionary)PropertyListParser.parse(target);
			
			if(dict.containsKey(key)){
				NSObject existingObj = dict.get(key);
				if(existingObj instanceof NSArray && valueObject instanceof NSArray){//Merge existing arrays
					NSArray existingArray = (NSArray) existingObj;
					NSArray valueArray = (NSArray) valueObject;
					valueObject = concatArrays(existingArray, valueArray);
				}
			}
			
			dict.put(key, valueObject);
			PropertyListParser.saveAsXML(dict, target);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "Error updating plist file", e));
		}
		
	}

	@Override
	public void unInstall() throws CoreException {
		try {
			NSDictionary dict = (NSDictionary) PropertyListParser.parse(target);

			if (dict.containsKey(key)) {
				NSObject valueObject = getValueObject();
				NSObject existingObj = dict.get(key);
				if (existingObj instanceof NSArray && valueObject instanceof NSArray) {// remove values

					NSArray existingArray = (NSArray) existingObj;
					NSArray valueArray = (NSArray) valueObject;
					for (int i = 0; i < valueArray.count(); i++) {
						int removeIndex = existingArray.indexOfObject(valueArray.objectAtIndex(i));
						if (removeIndex > 0) {
							existingArray.remove(removeIndex);
						}
					}
					if(existingArray.count() == 0){
						dict.remove(key);
					}
				}else{
					dict.remove(key);
				}
				
				PropertyListParser.saveAsXML(dict, target);
			}
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					IOSCore.PLUGIN_ID, "Error updating plist file", e));
		}

	}
	
	private NSObject getValueObject() throws Exception{
		Document newNode = XMLUtil.loadXML(xml);//config-file node
		NodeList childNodes = newNode.getDocumentElement().getChildNodes(); //append child nodes of config-file
		//Let's get the child node that will be inserted
		Element insertNode = null;
		for(int i = 0; i < childNodes.getLength(); i++ ){
			if(childNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
				insertNode = (Element) childNodes.item(i);
			}
		}
		if(insertNode == null ){
			throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "Error updating plist file"));
		}
		// We add plist tags so that we can parse these as plist objects. 
		String value = "<plist>"+CordovaPluginXMLHelper.stringifyNode(insertNode)+"</plist>";
		return PropertyListParser.parse(value.getBytes());

	}
	
	private NSArray concatArrays(NSArray array1, NSArray array2){
		ArrayList<NSObject> concatList = new ArrayList<NSObject>();
		NSObject[] theArray1 = array1.getArray();
		concatList.addAll(Arrays.asList(theArray1));
		NSObject[] theArray2 = array2.getArray();
		for (NSObject nsObject : theArray2) {
			if( !concatList.contains(nsObject)){
				concatList.add(nsObject);
			}
		}
		NSObject[] theConcatArray = concatList.toArray(new NSObject[concatList.size()]);
		return new NSArray(theConcatArray);
	}
	
}
