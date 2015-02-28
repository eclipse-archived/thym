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
package org.eclipse.thym.android.core.adt;


import static org.eclipse.thym.android.core.AndroidConstants.DIR_LIBS;
import static org.eclipse.thym.android.core.AndroidConstants.DIR_RES;
import static org.eclipse.thym.android.core.AndroidConstants.DIR_SRC;
import static org.eclipse.thym.android.core.AndroidConstants.DIR_VALUES;
import static org.eclipse.thym.android.core.AndroidConstants.DIR_XML;
import static org.eclipse.thym.android.core.AndroidConstants.FILE_JAR_CORDOVA;
import static org.eclipse.thym.android.core.AndroidConstants.FILE_XML_STRINGS;
import static org.eclipse.thym.core.internal.util.FileUtils.directoryCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.fileCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.templatedFileCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.toURL;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.android.core.AndroidCore;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Icon;
import org.eclipse.thym.core.config.ImageResourceBase;
import org.eclipse.thym.core.config.Splash;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.platform.AbstractProjectGeneratorDelegate;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class AndroidProjectGenerator extends AbstractProjectGeneratorDelegate{


	public AndroidProjectGenerator(){
		super();
	}
	
	public AndroidProjectGenerator(IProject project, File generationFolder,String platform) {
		init(project, generationFolder,platform);
	}

	@Override
	protected void generateNativeFiles(HybridMobileLibraryResolver resolver) throws CoreException {
		
		AndroidSDKManager sdkManager = AndroidSDKManager.getManager();
		
		HybridProject hybridProject = HybridProject.getHybridProject(getProject());
		if(hybridProject == null ){
			throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Not a hybrid mobile project, can not generate files"));
		}
		Widget widgetModel = WidgetModel.getModel(hybridProject).getWidgetForRead();
		
		// Create the basic android project
		String packageName = widgetModel.getId();
		String name = hybridProject.getBuildArtifactAppName();
		
		AndroidSDK target = AndroidProjectUtils.selectBestValidTarget(resolver);
		File destinationDir = getDestination();
		IPath destinationPath = new Path(destinationDir.toString());
		if(destinationDir.exists()){
			try {//Clean the android directory to avoid and "Error:" message 
				 // from the command line tools for using update. Otherwise all create
				// project calls will be recognized as failed.
				FileUtils.cleanDirectory(destinationDir);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.WARNING, AndroidCore.PLUGIN_ID,
						NLS.bind("Could not clean the android working directory at {0}",destinationDir), e));
			}
		}
		
		sdkManager.createProject(target, name, destinationDir,name, packageName, new NullProgressMonitor());
		
		try{
			IPath cordovaJarPath = destinationPath.append(DIR_LIBS).append(FILE_JAR_CORDOVA);
			//Move cordova library to /libs/cordova.jar
 			fileCopy(resolver.getTemplateFile(cordovaJarPath.makeRelativeTo(destinationPath)), toURL(cordovaJarPath.toFile()));
 			
 			// //res
 			IPath resPath = destinationPath.append(DIR_RES);
			directoryCopy(resolver.getTemplateFile(resPath.makeRelativeTo(destinationPath)),
					toURL(resPath.toFile()));
			
			IFile configFile = hybridProject.getConfigFile();
			IPath xmlPath = resPath.append(DIR_XML);
			File xmldir = xmlPath.toFile();
			if( !xmldir.exists() ){//only config.xml uses xml 
				xmldir.mkdirs();   //directory make sure it is created
			}
			fileCopy(toURL(configFile.getLocation().toFile()), 
					toURL(xmlPath.append(PlatformConstants.FILE_XML_CONFIG).toFile()));
			
			handleIcons(widgetModel, hybridProject);
			handleSplashScreens(widgetModel, hybridProject);
			updateAppName(hybridProject.getAppName());
			
			// Copy templated files 
			Map<String, String> values = new HashMap<String, String>();
			values.put("__ID__", packageName);
			values.put("__PACKAGE__", packageName);// yeap, cordova also uses two different names
			values.put("__ACTIVITY__", name);
			values.put("__APILEVEL__", target.getApiLevel());
			
			// /AndroidManifest.xml
			IPath andrManifestPath = destinationPath.append("AndroidManifest.xml");
			templatedFileCopy(resolver.getTemplateFile(andrManifestPath.makeRelativeTo(destinationPath)), 
					toURL(andrManifestPath.toFile()),
					values);
			// /src/${package_dirs}/Activity.java
			IPath activityPath = new Path(DIR_SRC).append(HybridMobileLibraryResolver.VAR_PACKAGE_NAME).append(HybridMobileLibraryResolver.VAR_APP_NAME+".java");
			IPath resolvedActivityPath = destinationPath.append(DIR_SRC).append(packageName.replace('.', '/')).append(name+".java");
			templatedFileCopy(resolver.getTemplateFile(activityPath), 
					toURL(resolvedActivityPath.toFile()),
					values);
			}
		catch(IOException e)
		{
			throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Error generating the native android project", e));
		}
	}
	
	private void handleSplashScreens(Widget widget, HybridProject project) throws CoreException{
		final List<Splash> splashes = widget.getSplashes();
		if(splashes == null || splashes.isEmpty()){
			return;
		}
		
		final File resDir = new File(getDestination(), DIR_RES);
		boolean templateCleaned = false;
		for (Splash splash : splashes) {
			IFile splashFile = null;
			String density = null;
			if(splash.isDefault()){
				splashFile = project.getProject().getFile(splash.getSrc());
			}else
			if(getTargetShortName().equals(splash.getPlatform()) && splash.getDensity() != null && !splash.getDensity().isEmpty()){
				splashFile = project.getProject().getFile(splash.getSrc());
				density = splash.getDensity();
			}
			if(splashFile != null ){// splash file found
				if(!templateCleaned) {
					deleteTemplateResources(resDir, "screen.png");
					templateCleaned = true;
				}
				if(!splashFile.exists()){
					AndroidCore.log(IStatus.ERROR, NLS.bind("Missing splash screen image {0}", splash.getSrc()), null);
					continue;
				}
				
				String filename= splashFile.getName().indexOf(".9.")> 0 ? "screen.9.png" : "screen.png";
				if(density != null && !density.isEmpty()){
					filename = "drawable-"+density+"/"+filename;
				}
				try{
					fileCopy(toURL(splashFile.getLocation().toFile()), toURL(new File(resDir, filename))); 	
				}catch(IOException e){
					throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Error while processing android splash screens ", e));
				}
			}
		}
	}

	private void handleIcons(Widget widgetModel, HybridProject project) throws CoreException{
		final List<Icon> icons = widgetModel.getIcons();
		if(icons == null || icons.isEmpty()){
			return; //Nothing to do; App uses the default icon from Cordova project template.
		}
		try{
			final File resFile = new File(getDestination(),DIR_RES);
			boolean templateCleaned =false;
			for (ImageResourceBase icon : icons) {
				IFile iconFile = null;
				String density = null;
				if(icon.isDefault()){
					iconFile = project.getProject().getFile(icon.getSrc());
				}else
				if(getTargetShortName().equals(icon.getPlatform())){
					iconFile = project.getProject().getFile(icon.getSrc());
					density = AndroidProjectUtils.getDensityForIcon(icon);
					if(density == null || density.isEmpty()){
						AndroidCore.log(IStatus.ERROR, NLS.bind("Can not determine density for icon {0}", icon.getSrc()), null);
						continue;
					}
				}
				if(iconFile != null){//found an icon let's process.
					if(!templateCleaned){
						deleteTemplateResources(resFile, "icon.png");
						templateCleaned = true;
					}
					if(!iconFile.exists()){
						AndroidCore.log(IStatus.ERROR, NLS.bind("Missing icon file {0}", icon.getSrc()), null);
						continue;
					}
					String drawableDir = density != null && !density.isEmpty() ? "drawable-"+density: "drawable";
					fileCopy(toURL(iconFile.getLocation().toFile()),toURL(new File(resFile,drawableDir+"/icon.png"))); 
				}
			}
		}
		catch(IOException e){
			throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Error whiile processing android icons",e));
		}
	}
	
	private void deleteTemplateResources(File directory, String name){
		Collection<File> files = FileUtils.listFiles(directory, FileFilterUtils.nameFileFilter(name), TrueFileFilter.INSTANCE);
		for (File file : files) {
			FileUtils.deleteQuietly(file);
		}
	}

	private void updateAppName( String appName ) throws CoreException{
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db;

	    try{
	    	db = dbf.newDocumentBuilder();
	    	IPath stringsPath = new Path(getDestination().toString()).append(DIR_RES).append(DIR_VALUES).append(FILE_XML_STRINGS);
	    	File strings = stringsPath.toFile();
	    	Document configDocument = db.parse( strings); 
	    	XPath xpath = XPathFactory.newInstance().newXPath();
	    	
	    	try {
	    		XPathExpression expr = xpath.compile("//string[@name=\"app_name\"]");
				Node node = (Node) expr.evaluate( configDocument, XPathConstants.NODE);
				node.setTextContent(appName);
			    configDocument.setXmlStandalone(true);
			    Source source = new DOMSource(configDocument);
			    StreamResult result = new StreamResult(strings);
			    // Write the DOM document to the file
			    TransformerFactory transformerFactory = TransformerFactory
				    .newInstance();
			    Transformer xformer = transformerFactory.newTransformer();
			    xformer.transform(source, result);
				
			} catch (XPathExpressionException e) {//We continue because this affects the displayed app name
				                                  // which is not a show stopper during development
				AndroidCore.log(IStatus.ERROR, "Error when updating the application name", e);
			} catch (TransformerConfigurationException e) {
				AndroidCore.log(IStatus.ERROR, "Error when updating the application name", e);
			} catch (TransformerException e) {
				AndroidCore.log(IStatus.ERROR, "Error when updating the application name", e);
			}
	    	
	    }
		catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parser error when parsing /res/values/strings.xml", e));
		} catch (SAXException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parsing error on /res/values/strings.xml", e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "IO error when parsing /res/values/strings.xml", e));
		} 
	}

	@Override
	protected void replaceCordovaPlatformFiles(HybridMobileLibraryResolver resolver) throws IOException {
		IPath cordovaJSPath = new Path(getPlatformWWWDirectory().toString()).append(PlatformConstants.FILE_JS_CORDOVA);
		fileCopy(resolver.getTemplateFile(HybridMobileLibraryResolver.PATH_CORDOVA_JS),
				toURL(cordovaJSPath.toFile()));
	}


	
	@Override
	protected File getPlatformWWWDirectory() {
		return AndroidProjectUtils.getPlatformWWWDirectory(getDestination());
	}

}
