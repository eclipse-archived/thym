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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Plugin;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI.Command;
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
	 * 
	 * @param directory
	 * @param monitor
	 * @throws CoreException <ul>
	 *<li>if plugin.xml is missing</li>
	 *<li>if plug-ins directory is missing on the project</li>
	 *<li>if an error occurs during installation</li>
	 *</ul>
	 */
	public void installPlugin(File directory, IProgressMonitor monitor) 
			throws CoreException{
		// read plugin.xml to verify the plugin
		readPluginXML(directory);
		installPlugin(directory.toString(), monitor, true);
	}
	
	/**
	 * Installs a Cordova plug-in from registry.
	 * 
	 * @param plugin
	 * @param monitor
	 * @throws CoreException
	 *<ul>
	 *<li>if plug-ins directory is missing on the project</li>
	 *<li>if an error occurs during installation</li>
	 *</ul>
	 */
	public void installPlugin(RegistryPluginVersion plugin, IProgressMonitor monitor) throws CoreException{
		String pluginCoords = plugin.getName() + "@" + plugin.getVersionNumber();
		installPlugin(pluginCoords, monitor, true);
	}
	
	/**
	 * Installs a specified plugin.
	 * 
	 * @param plugin
	 * @param monitor
	 * @param save
	 * @throws CoreException
	 */
	public void installPlugin(Plugin plugin, IProgressMonitor monitor, boolean save) throws CoreException {
		if (isPluginInstalled(plugin.getName())){
			return;
		}
		String pluginVersion = plugin.getSpec().replaceAll("~", "");
		String pluginCoords = plugin.getName() + "@" + pluginVersion;
		installPlugin(pluginCoords, monitor, save);
	}

	/**
	 * Installs a Cordova plug-in from a git repository.
	 * 
	 * @param uri
	 * @param monitor 
	 * @throws CoreException
	 */
	public void installPlugin(URI uri, IProgressMonitor monitor) throws CoreException{
		installPlugin(uri.toString(), monitor, true);
	}
	
	private void installPlugin(String plugin, IProgressMonitor monitor, boolean save) throws CoreException{
		if(monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		subMonitor.setTaskName("Installing plugin: " + plugin);	
		String options ="";
		if(save){
			options = CordovaProjectCLI.OPTION_SAVE;
		}
		IStatus status = CordovaProjectCLI.newCLIforProject(project)
			.plugin(Command.ADD, subMonitor.split(90), plugin, options)
			.convertTo(PluginMessagesCLIResult.class)
			.asStatus();
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, subMonitor.split(10, SubMonitor.SUPPRESS_ALL_LABELS));
		if(!status.isOK()){
			throw new CoreException(status);
		}
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
	
	private void unInstallPlugin(String id, IProgressMonitor monitor, boolean save) throws CoreException{
		
		if(id == null || !isPluginInstalled(id))
			return;
		if(monitor == null ) 
			monitor = new NullProgressMonitor();
		IFolder dir =  getPluginHomeFolder(id);
		File pluginFile = new File(dir.getLocation().toFile(), PlatformConstants.FILE_XML_PLUGIN);
		if( !pluginFile.exists() ){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Not a valid plugin id , no plugin.xml exists"));
		}
		if(monitor.isCanceled()){
			return;
		}
		String options ="";
		if(save){
			options = CordovaProjectCLI.OPTION_SAVE;
		}
		IStatus status = CordovaProjectCLI.newCLIforProject(project)
			.plugin(Command.REMOVE, monitor, id, options)
			.convertTo(PluginMessagesCLIResult.class)
			.asStatus();
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		resetInstalledPlugins();
		if(!status.isOK()){
			throw new CoreException(status);
		}
	}
	
	/**
	 * Removes the plug-in with given id.
	 * 
	 * @param id
	 * @param monitor
	 * 
	 * @throws CoreException
	 */
	public void unInstallPlugin(String id, IProgressMonitor monitor) throws CoreException{
		unInstallPlugin(id, monitor, true);
	}
	
	/**
	 * Removes the given plug-in.
	 * 
	 * @param id
	 * @param monitor
	 * @param if save param should be added to cordova cli
	 * 
	 * @throws CoreException
	 */
	public void unInstallPlugin(Plugin plugin, IProgressMonitor monitor, boolean save) throws CoreException{
		unInstallPlugin(plugin.getName(), monitor, save);
	}

	
	private void resetInstalledPlugins() {
		installedPlugins.clear();
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
