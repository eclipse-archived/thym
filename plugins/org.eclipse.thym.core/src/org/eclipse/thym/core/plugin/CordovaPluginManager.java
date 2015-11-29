/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.plugin;


import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.cordova.CordovaCLI.Command;
import org.eclipse.thym.core.internal.util.XMLUtil;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryMapper;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin.RegistryPluginVersion;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


/**
 * Manages the Cordova plug-ins for a project. 
 * 
 * @author Gorkem Ercan
 *
 */
public class CordovaPluginManager {
	
	private final HybridProject project;
	private List<CordovaPlugin> installedPlugins = new ArrayList<CordovaPlugin>();
	
	public CordovaPluginManager(HybridProject project){
		this.project = project;
	}
	
	/**
	 * Installs a Cordova plug-in to {@link HybridProject} from directory.
	 * A plug-ins installation is a two step process. This method triggers the 
	 * first step where Cordova Plug-ins is installed to HybridProject. 
	 * 
	 * @see #completePluginInstallationsForPlatform(File, String)
	 * @param directory
	 * @param overwrite
	 * @param monitor
	 * @throws CoreException <ul>
	 *<li>if plugin.xml is missing</li>
	 *<li>if plug-ins directory is missing on the project</li>
	 *<li>if an error occurs during installation</li>
	 *</ul>
	 */
	public void installPlugin(File directory, FileOverwriteCallback overwrite, IProgressMonitor monitor) throws CoreException{
		if(monitor == null )
			monitor = new NullProgressMonitor();
		if(monitor.isCanceled()) return;
		// read plugin.xml to verify the plugin
		readPluginXML(directory);
		CordovaCLI.newCLIforProject(project).plugin(Command.ADD, monitor, directory.toString(), CordovaCLI.OPTION_SAVE);
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}
	
	/**
	 * Installs a Cordova plug-in from registry. This method 
	 * delegates to {@link #doInstallPlugin()} after downloading the plugin from registry. 
	 * 
	 * @param plugin
	 * @param overwrite
	 * @param isDependency 
	 * @param monitor
	 * @throws CoreException
	 *<ul>
	 *<li>if plug-ins directory is missing on the project</li>
	 *<li>if an error occurs during installation</li>
	 *</ul>
	 */
	public void installPlugin(RegistryPluginVersion plugin, FileOverwriteCallback overwrite, boolean isDependency, IProgressMonitor monitor ) throws CoreException{
		if(monitor == null )
			monitor = new NullProgressMonitor();
		if(monitor.isCanceled()) return;
		String pluginCoords = plugin.getName() + "@" + plugin.getVersionNumber();
		CordovaCLI.newCLIforProject(this.project).plugin(Command.ADD, monitor, pluginCoords, CordovaCLI.OPTION_SAVE );
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}



	/**
	 * Installs a Cordova plug-in from a git repository. 
	 * This method delegates to {@link #doInstallPlugin(File)} after cloning the
	 * repository to a temporary location to complete the installation of the 
	 * plug-in. 
	 * <br/>
	 * If commit is not null the cloned repository will be checked out to 
	 * commit. 
	 * <br/>
	 * If subdir is not null it is assumed that the subdir path exists and installation 
	 * will be done from that location. 
	 * 
	 * @param uri
	 * @param overwrite
	 * @param isDependency 
	 * @param monitor 
	 * @param commit 
	 * @param subdir
	 * @throws CoreException
	 */
	public void installPlugin(URI uri, FileOverwriteCallback overwrite, boolean isDependency, IProgressMonitor monitor) throws CoreException{
		if(monitor == null )
			monitor = new NullProgressMonitor();
		if(monitor.isCanceled()) return;	
		CordovaCLI.newCLIforProject(project).plugin(Command.ADD, monitor, uri.toString(), CordovaCLI.OPTION_SAVE);
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}
	
	private Document readPluginXML(File directory) throws CoreException {
		File pluginFile = new File(directory, PlatformConstants.FILE_XML_PLUGIN);
		if(!pluginFile.exists()){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind("plugin.xml can not be located at {0}", pluginFile.toString())));
		}
		Document doc = null;
		try{
			doc = XMLUtil.loadXML(pluginFile, false); 
		}catch(CoreException e ){
			//Convert the SAXParseException exceptions to HybridMobileStatus because
			//it may indicate a broken plugin.xml or an platform not supported 
			// see https://issues.jboss.org/browse/JBIDE-15768
			if(e.getCause() != null && e.getCause() instanceof SAXParseException){
				HybridMobileStatus hms = new HybridMobileStatus(IStatus.ERROR, HybridCore.PLUGIN_ID, HybridMobileStatus.STATUS_CODE_CONFIG_PARSE_ERROR,
						e.getStatus().getMessage(), e.getCause());
				e = new CoreException(hms);
			}
			throw e;
		}
		return doc;
	}
	
	/**
	 * Removes the plug-in with given id
	 * @param id
	 * @param overwrite
	 * @param monitor
	 * 
	 * @throws CoreException
	 */
	public void unInstallPlugin(String id, IProgressMonitor monitor) throws CoreException{
		
		if(id == null || !isPluginInstalled(id))
			return;
		if(monitor == null ) 
			monitor = new NullProgressMonitor();
		IFolder dir =  getPluginHomeFolder(id);
		File pluginFile = new File(dir.getLocation().toFile(), PlatformConstants.FILE_XML_PLUGIN);
		if( !pluginFile.exists() ){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Not a valid plugin id , no plugin.xml exists"));
		}
		if(monitor.isCanceled()) return;
		CordovaCLI.newCLIforProject(project).plugin(Command.REMOVE, monitor, id, CordovaCLI.OPTION_SAVE);
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		resetInstalledPlugins();
	}

	
	private void resetInstalledPlugins() {
		installedPlugins.clear();
	}

	/**
	 * Completes the installation of all the installed plug-ins in this HybridProject 
	 * to the given platform project location. 
	 * This installation involves modifying of necessary files and 
	 * copying/generation of the others.
	 * 
	 * @param platformProjectLocation
	 * @param platform
	 * @param overwrite
	 * @param monitor
	 * 
	 * @throws CoreException
	 */
	public void completePluginInstallationsForPlatform(File platformProjectLocation, String platform, FileOverwriteCallback overwrite, IProgressMonitor monitor) throws CoreException{
		if(monitor.isCanceled()) return;
		CordovaCLI.newCLIforProject(project).prepare(monitor, platform);
	}
	
	/**
	 * <p>
	 * Return unmodifiable list of currently installed plug-ins.
	 * </p>
	 * <p>
	 * This is a cached call so subsequent calls will perform better.
	 * However, it is cached per {@link CordovaPluginManager} instance
	 * which is also a single instance per {@link HybridProject} however 
	 * HybridProject instances are created on demand and the client should 
	 * handle the optimal caching.
	 * </p>
	 * @return list of installedPlugins
	 * @throws CoreException
	 */
	public List<CordovaPlugin> getInstalledPlugins() throws CoreException{
		updatePluginList();
		return Collections.unmodifiableList(installedPlugins);
	}
	
	/**
	 * Checks if the given plug-in with pluginId is installed for the project.
	 * Also uses {@link CordovaPluginRegistryMapper} to check alternate IDs 
	 * for plugins.
	 * 
	 * @param pluginId
	 * @return true if the plug-in is installed
	 */
	public boolean isPluginInstalled(String pluginId){
		if(pluginId == null ) return false;
		try{
			IFolder pluginHome = getPluginHomeFolder(pluginId);
			if(pluginHome !=  null ){
				IFile pluginxml = pluginHome.getFile(PlatformConstants.FILE_XML_PLUGIN);
				return pluginxml.getLocation() != null && pluginHome.getLocation().toFile().exists();
			}
		}catch(CoreException e){
			//ignore to return false
		}
		return false;
	}
	
	/**
	 * Constructs the contents for the cordova_plugin.js from the list of 
	 * installed plugins. 
	 * 
	 * @return 
	 * @throws CoreException
	 */
	public String getCordovaPluginJSContent(String platformId) throws CoreException{
		JsonArray moduleObjects = new JsonArray();
		
		List<CordovaPlugin> plugins =  getInstalledPlugins();
		for (CordovaPlugin cordovaPlugin : plugins) {
			List<PluginJavaScriptModule> modules = cordovaPlugin.getModules();
			for (PluginJavaScriptModule pluginJavaScriptModule : modules) {
				if( platformId == null || pluginJavaScriptModule.getPlatform() == null ||
						pluginJavaScriptModule.getPlatform().equals(platformId))
				{

					JsonObject obj = new JsonObject();
					obj.addProperty("file", (new Path("plugins")).append(cordovaPlugin.getId()).append(pluginJavaScriptModule.getSource()).toString());
					obj.addProperty("id", pluginJavaScriptModule.getName());
					if(pluginJavaScriptModule.isRuns()) {
						obj.addProperty("runs", true);
					}
					if( pluginJavaScriptModule.getClobbers() != null ){
						List<String> clobbers = pluginJavaScriptModule.getClobbers();
						JsonArray clobbersArray = new JsonArray();
						for (String string : clobbers) {
							clobbersArray.add(new JsonPrimitive(string));
						}
						obj.add("clobbers", clobbersArray);
					}
					if( pluginJavaScriptModule.getMerges() != null ){
						List<String> merges = pluginJavaScriptModule.getMerges();
						JsonArray mergesArray = new JsonArray();
						for (String string : merges) {
							mergesArray.add(new JsonPrimitive(string));
						}
						obj.add("merges", mergesArray);
					}
					moduleObjects.add(obj);
				}
			}
		}
		StringBuilder finalContents = new StringBuilder();
		finalContents.append("cordova.define('cordova/plugin_list', function(require, exports, module) {\n");
		Gson gson = new Gson();
	    finalContents.append("module.exports = ").append(gson.toJson(moduleObjects)).append("\n});");
	    
		return finalContents.toString();
	}
	
	private void updatePluginList() throws CoreException {
		long start = System.currentTimeMillis();
		if(installedPlugins == null || installedPlugins.isEmpty()) {
			HybridCore.trace("Really updating the installed plugin list");
			IResourceVisitor visitor = new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					if(resource.getType() == IResource.FOLDER){
						IFolder folder = (IFolder) resource.getAdapter(IFolder.class);
						IFile file = folder.getFile(PlatformConstants.FILE_XML_PLUGIN);
						if(file.exists()){
							addInstalledPlugin(file);
						}
					}
					return resource.getName().equals(PlatformConstants.DIR_PLUGINS);
				}
			};
			IFolder plugins = this.project.getProject().getFolder(PlatformConstants.DIR_PLUGINS);
			if(plugins != null && plugins.exists()){
				synchronized (installedPlugins) {
					plugins.accept(visitor,IResource.DEPTH_ONE,false);
				}
			}
		}
		HybridCore.trace(NLS.bind("Updated plugin list in {0} ms", (System.currentTimeMillis() - start)));
	}
	
	private void addInstalledPlugin(IFile pluginxml) throws CoreException{
		CordovaPlugin plugin = CordovaPluginXMLHelper.createCordovaPlugin(pluginxml.getContents());
		plugin.setFolder((IFolder)pluginxml.getParent().getAdapter(IFolder.class));
		int index = installedPlugins.indexOf(plugin);
		if(index>-1){
			installedPlugins.set(index, plugin);
		}else{
			installedPlugins.add(plugin);
		}
	}

	/**
	 * Returns the folder that the plugin with id is installed under the plugins folder.
	 * It also uses {@link CordovaPluginRegistryMapper} to check for alternate ids.
	 * 
	 * @param id
	 * @return null or a folder
	 * throws CoreException - if <i>plugins</i> folder does not exist
	 */
	private IFolder getPluginHomeFolder(String id) throws CoreException{
		if(id == null ) return null;
		IFolder plugins = getPluginsFolder();
		if(!plugins.exists()){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Plugin folder does not exist"));
		}
		IFolder pluginHome = plugins.getFolder(id);
		IPath location = pluginHome.getLocation();
		if(pluginHome.exists() &&  location != null && location.toFile().isDirectory()){
			return pluginHome;
		}
		// try the alternate ID 
		String alternateId = CordovaPluginRegistryMapper.alternateID(id);
		if(alternateId != null ){
			 pluginHome = plugins.getFolder(alternateId);
			 location = pluginHome.getLocation();
			 if(pluginHome.exists() &&  location != null && location.toFile().isDirectory()){
				 return pluginHome;
			 }
		}
		return null;
	}
	
	private IFolder getPluginsFolder() {
		IFolder plugins = this.project.getProject().getFolder(PlatformConstants.DIR_PLUGINS);
		return plugins;
	}
		
}
