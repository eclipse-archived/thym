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
package org.eclipse.thym.core.plugin;


import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getAssets;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getAttributeValue;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getConfigFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getDependencies;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getFrameworks;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getLibFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getPlatformNode;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getPreferencesNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getResourceFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getSourceFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.stringifyNode;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Feature;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.internal.util.XMLUtil;
import org.eclipse.thym.core.platform.AbstractPluginInstallationActionsFactory;
import org.eclipse.thym.core.platform.IPluginInstallationAction;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.actions.ActionVariableHelper;
import org.eclipse.thym.core.plugin.actions.ConfigXMLUpdateAction;
import org.eclipse.thym.core.plugin.actions.CopyFileAction;
import org.eclipse.thym.core.plugin.actions.DependencyInstallAction;
import org.eclipse.thym.core.plugin.actions.PluginInstallRecordAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
		File pluginFile = new File(directory, PlatformConstants.FILE_XML_PLUGIN);
		Assert.isTrue(pluginFile.exists());
		if(monitor.isCanceled())
			return;
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
		
		String id = CordovaPluginXMLHelper.getAttributeValue(doc.getDocumentElement(), "id");
		if(isPluginInstalled(id)){
			HybridCore.log(IStatus.WARNING, "Cordova Plugin ("+id+") is already installed, skipping.",null);
		}
		if( !pluginFile.exists() ){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Not a valid plugin directory, no plugin.xml exists"));
		}
		IFolder plugins = this.project.getProject().getFolder(PlatformConstants.DIR_PLUGINS);
		if( plugins == null || !plugins.exists() ){
			plugins.create(true, true, monitor);
		}
		
		
		List<IPluginInstallationAction> actions = collectInstallActions(
				directory, doc, id, plugins, overwrite);
		actions.add(getPluginInstallRecordAction(doc));
		runActions(actions,false,overwrite,monitor); 
		resetInstalledPlugins();
	}

	/**
	 * Installs a Cordova plug-in from a git repository. 
	 * This method delegates to {@link #installPlugin(File)} after cloning the
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
	 * @param commit 
	 * @param subdir
	 * @param overwrite
	 * @param monitor 
	 * @throws CoreException
	 */
	public void installPlugin(URI uri, String commit, String subdir,FileOverwriteCallback overwrite,IProgressMonitor monitor) throws CoreException{
		File tempRepoDirectory = new File(FileUtils.getTempDirectory(), "cordova_plugin_tmp_"+Long.toString(System.currentTimeMillis()));
		tempRepoDirectory.deleteOnExit();
		try {
			if(monitor.isCanceled())
				return;
			monitor.subTask("Clone plugin repository");
			Git git = Git.cloneRepository().setDirectory(tempRepoDirectory).setURI(uri.toString()).call();
			if(commit != null && !monitor.isCanceled()){
				git.checkout().setName(commit).call();
			}
			monitor.worked(1);
			SubProgressMonitor sm = new SubProgressMonitor(monitor, 1);
			sm.setTaskName("Installing to "+this.project.getProject().getName());
			File pluginDirectory = tempRepoDirectory;
			if(subdir != null ){
				pluginDirectory = new File(tempRepoDirectory, subdir);
				if(!pluginDirectory.isDirectory()){
					throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, subdir + " does not exist in this repo"));
				}
			}
			this.installPlugin(pluginDirectory,overwrite,sm);
		} catch (GitAPIException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Error cloning the plugin repository", e));
		} finally{
			monitor.done();
		}
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
		IResource dir = this.project.getProject().findMember("/"+PlatformConstants.DIR_PLUGINS+"/"+id);
		if(dir == null || !dir.exists() ){//No plugins folder abort
			return;
		}
		File pluginFile = new File(dir.getLocation().toFile(), PlatformConstants.FILE_XML_PLUGIN);
		if( !pluginFile.exists() ){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Not a valid plugin id , no plugin.xml exists"));
		}
		Document doc = XMLUtil.loadXML(pluginFile, false); 
		
		FileOverwriteCallback cb = new FileOverwriteCallback() {
			@Override
			public boolean isOverwiteAllowed(String[] files) {
				return true;
			}
		};
		IResource pluginsDir = this.project.getProject().findMember("/"+PlatformConstants.DIR_PLUGINS);
		List<IPluginInstallationAction> actions = collectInstallActions(
				dir.getLocation().toFile(),             // TODO: replace with values from .fetch.json
				doc, id, pluginsDir,cb);                           
		runActions(actions,true,cb, monitor);
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
		List<CordovaPlugin> plugins  = getInstalledPlugins();
		PlatformSupport platformSupport = HybridCore.getPlatformSupport(platform);
		for (CordovaPlugin cordovaPlugin : plugins) {
 			completePluginInstallationToPlatform(cordovaPlugin, platformSupport, platformProjectLocation, overwrite, monitor);
		}
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
	 * 
	 * @param pluginId
	 * @return true if the plug-in is installed
	 */
	public boolean isPluginInstalled(String pluginId){
		if(pluginId == null ) return false;
		IFolder plugins = this.project.getProject().getFolder(PlatformConstants.DIR_PLUGINS);
		IPath pluginIDPath = new Path(pluginId);
		pluginIDPath.append(PlatformConstants.FILE_XML_PLUGIN);
		boolean result = plugins.exists(pluginIDPath);
		return result;
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
	
	/**
	 * Returns the list of plugin ids that are listed on config.xml and are not already installed.
	 * 
	 * @param monitor
	 * @return plugin ids
	 * @throws CoreException
	 */
	public List<RestorableCordovaPlugin> getRestorablePlugins(IProgressMonitor monitor) throws CoreException{
		if(monitor == null ){
			monitor = new NullProgressMonitor();
		}
		Widget widget  = WidgetModel.getModel(this.project).getWidgetForRead();
		if(widget == null ){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Unable to read config.xml"));
		}
		List<Feature> features = widget.getFeatures();
		List<RestorableCordovaPlugin> restorable = new ArrayList<RestorableCordovaPlugin>();
		if (features != null) {
			for (Feature feature : features) {
				Map<String, String> params = feature.getParams();
				String id = params.get("id");
				if (id != null && !isPluginInstalled(id)) {
					RestorableCordovaPlugin rp = new RestorableCordovaPlugin();
					rp.setId(id);
					String version = params.get("version");
					if (version != null){
						rp.setVersion(version);
					}
					restorable.add(rp);
				}
			}
		}
	return restorable;
	}
	
	/*
	 * Collects all the actions for first stage install/uninstall
	 */
	private List<IPluginInstallationAction> collectInstallActions(
			File directory, Document doc, String id, IResource dir, FileOverwriteCallback overwrite) {
		List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>();
		NodeList dependencyNodes = getDependencies(doc.getDocumentElement());
		for (int i = 0; i < dependencyNodes.getLength(); i++) {
			Node dependencyNode = dependencyNodes.item(i);
			String dependencyId = getAttributeValue(dependencyNode, "id");
			String url = getAttributeValue(dependencyNode, "url");
			String commit = getAttributeValue(dependencyNode, "commit");
			String subdir = getAttributeValue(dependencyNode, "subdir");
			URI uri = null;
			if(url != null && !url.isEmpty()){
				if(!url.endsWith(".git")){
					url= url+".git";
				}
				uri = URI.create(url);
			}
			DependencyInstallAction action = new DependencyInstallAction(dependencyId, uri, commit, subdir, this.project, overwrite);
			actions.add(action);
		}
		File destination = new File(dir.getLocation().toFile(), id);
		
		CopyFileAction copy = new CopyFileAction(directory, destination);
		actions.add(copy);
		actions.addAll(collectAllConfigXMLActionsForSupporredPlatforms(doc));
		actions.addAll(collectVariablePreferencesForSupportedPlatforms(doc));
		return actions;
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

	private File getPluginHomeDirectory(CordovaPlugin plugin) throws CoreException{
		IProject prj = this.project.getProject();
		IFolder plugins = prj.getFolder(PlatformConstants.DIR_PLUGINS);
		if(plugins.exists()){
			IFolder pluginHome = plugins.getFolder(plugin.getId());
			if(pluginHome.exists() && pluginHome.getLocation() != null ){
				File f = pluginHome.getLocation().toFile();
				if(f.exists())
					return f;
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Plugin folder does not exist"));
	}
	
	private void runActions(final List<IPluginInstallationAction> actions, boolean runUnInstall, FileOverwriteCallback overwrite, IProgressMonitor monitor ) throws CoreException{
		PluginInstallActionsRunOperation op = new PluginInstallActionsRunOperation(actions, runUnInstall, overwrite,project.getProject());
		ResourcesPlugin.getWorkspace().run(op, monitor);
	}
	/*
	 * 1. collect common asset tags 
	 * 2. collect config tags except config.xml (which are handled during installation)
	 * 3. collect all js-module actions (for copying source files)
	 * 3. create cordova_plugin.js
	 * 4. collect all platform specific tags
	 * 	
	 */
	private void completePluginInstallationToPlatform(CordovaPlugin plugin, 
			PlatformSupport platform, 
			File platformProject, FileOverwriteCallback overwrite,
			IProgressMonitor monitor) throws CoreException{
		if(platform == null ) return;
			
		File pluginHome = getPluginHomeDirectory(plugin);
		File pluginFile = new File(pluginHome, PlatformConstants.FILE_XML_PLUGIN);
		Document doc = XMLUtil.loadXML(pluginFile, false); 
		//TODO: check  supported engines
		ArrayList<IPluginInstallationAction> allActions = new ArrayList<IPluginInstallationAction>();
		AbstractPluginInstallationActionsFactory actionFactory = platform.getPluginInstallationActionsFactory(this.project.getProject(), 
				pluginHome, platformProject);
		
		// Process jsmodules even if there is no platform node. 
		// See JBIDE-16544 
		allActions.addAll(getCommonAndPlatformJSModuleActions(plugin, platform.getPlatformId(), actionFactory)); // add all js-module actions
		
		Element node = getPlatformNode(doc, platform.getPlatformId());
		if( node != null ){
			allActions.addAll(getAssetActionsForPlatform(doc.getDocumentElement(),actionFactory ));// add common assets
			allActions.addAll(getConfigFileActionsForPlatform(doc.getDocumentElement(), actionFactory)); // common config changes
			//We do not need to create this file 
			//with every plugin. TODO: find a better place
			allActions.add(actionFactory.getCreatePluginJSAction(this.getCordovaPluginJSContent(platform.getPlatformId())));
			allActions.addAll(collectActionsForPlatform(node, actionFactory));
		}
		runActions(allActions,false,overwrite,monitor);
	}
	
	private List<IPluginInstallationAction> getCommonAndPlatformJSModuleActions(CordovaPlugin plugin,String platformId,AbstractPluginInstallationActionsFactory factory) {
		List<PluginJavaScriptModule> modules =  plugin.getModules(); 
		List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>();
		for (PluginJavaScriptModule scriptModule : modules) {
			if(scriptModule.getPlatform() == null || scriptModule.getPlatform().equals(platformId)){
				IPluginInstallationAction action = factory.getJSModuleAction(scriptModule.getSource(), 
						plugin.getId(), scriptModule.getName());
				actions.add(action);
			}
		}
		return actions;
	}

	private List<IPluginInstallationAction> collectAllConfigXMLActionsForSupporredPlatforms(Document doc){
		List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> nodes = new ArrayList<Element>();
		nodes.add(doc.getDocumentElement());
		for (PlatformSupport platform : platforms) {
			Element platformNode = getPlatformNode(doc, platform.getPlatformId());
			if(platformNode != null)
				nodes.add(platformNode);
		}
		for(Element node: nodes){
			NodeList configFiles = getConfigFileNodes(node);
			for (int i = 0; i < configFiles.getLength(); i++) {
				Node current = configFiles.item(i);
				String target = getAttributeValue(current, "target");
				if(!target.endsWith(PlatformConstants.FILE_XML_CONFIG)){
					continue;
				}
				String parent = getAttributeValue(current, "parent");
				String resolvedValue = stringifyNode(current);
				try{
					resolvedValue = ActionVariableHelper.replaceVariables(this.project, resolvedValue);
				}
				catch(CoreException ex){
					HybridCore.log(IStatus.ERROR, "Error while resolving variables", ex);
				}
				IPluginInstallationAction action = new ConfigXMLUpdateAction(this.project, parent, resolvedValue);
				list.add(action);
			}
		}
		return list;
	}
	
	private List<IPluginInstallationAction> collectVariablePreferencesForSupportedPlatforms(Document doc){
		List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> nodes = new ArrayList<Element>();
		nodes.add(doc.getDocumentElement());
		for (PlatformSupport platform : platforms) {
			Element platformNode = getPlatformNode(doc, platform.getPlatformId());
			if(platformNode != null)
				nodes.add(platformNode);
		}
		for(Element node: nodes){
			NodeList preferences = getPreferencesNodes(node);
			for( int i = 0; i < preferences.getLength(); i++){
				Node current = preferences.item(i);
				String name = getAttributeValue(current, "name");
				IPluginInstallationAction action  = new ConfigXMLUpdateAction(this.project, "/widget", 
						" <config-file target=\"res/xml/config.xml\" parent=\"/widget\">"
						+ "<preference name=\""+name+"\" value=\"PLEASE_DEFINE\"/>"
								+ "</config-file>");
				list.add(action);
			}
		}
		return list;

	}
	
	
	private List<IPluginInstallationAction> collectActionsForPlatform(Element node, AbstractPluginInstallationActionsFactory factory) throws CoreException{

		ArrayList<IPluginInstallationAction> actionsList = new ArrayList<IPluginInstallationAction>(); 
		actionsList.addAll(getSourceFilesActionsForPlatform(node, factory));
		actionsList.addAll(getResourceFileActionsForPlatform(node, factory));
		actionsList.addAll(getHeaderFileActionsForPlatform(node, factory));
		actionsList.addAll(getAssetActionsForPlatform(node, factory));
		actionsList.addAll(getConfigFileActionsForPlatform(node, factory));
		actionsList.addAll(getLibFileActionsForPlatform(node, factory)) ;
		actionsList.addAll(getFrameworkActionsForPlatfrom(node, factory ));
		return actionsList;
	}

	private List<IPluginInstallationAction> getFrameworkActionsForPlatfrom(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		NodeList frameworks = getFrameworks(node);
		for( int i =0; i< frameworks.getLength(); i++){
			Node current = frameworks.item(i);
			String src = getAttributeValue(current, "src");
			String weak = getAttributeValue(current, "weak");
			IPluginInstallationAction action = factory.getFrameworkAction(src,weak);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction> getLibFileActionsForPlatform(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		NodeList libFiles = getLibFileNodes(node);
		for(int i = 0; i<libFiles.getLength(); i++){
			Node current = libFiles.item(i);
			String src = getAttributeValue(current, "src");
			String arch = getAttributeValue(current, "arch");
			IPluginInstallationAction action = factory.getLibFileAction(src,arch);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction>  getConfigFileActionsForPlatform(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		NodeList configFiles = getConfigFileNodes(node);
		for (int i = 0; i < configFiles.getLength(); i++) {
			Node current = configFiles.item(i);
			String target = getAttributeValue(current, "target");
			if(target.endsWith(PlatformConstants.FILE_XML_CONFIG)){//config.xmls are handled on #collectAllConfigXMLActions
				continue;
			}
			String parent = getAttributeValue(current, "parent");
			String resolvedValue = stringifyNode(current);
			try{
				resolvedValue = ActionVariableHelper.replaceVariables(this.project, resolvedValue);
			}catch(CoreException e){
				HybridCore.log(IStatus.ERROR, "Error while resolving the variables", e);
			}
			IPluginInstallationAction action = factory.getConfigFileAction(target,parent, resolvedValue);
			list.add(action);
		}
		return list;
	}
	
	private List<IPluginInstallationAction> getHeaderFileActionsForPlatform(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		NodeList headerFiles = CordovaPluginXMLHelper.getHeaderFileNodes(node);
		for (int i = 0; i < headerFiles.getLength(); i++) {
			Node current = headerFiles.item(i);
			String src = getAttributeValue(current, "src");
			String targetDir = getAttributeValue(current,"target-dir" );
			String id = CordovaPluginXMLHelper.getAttributeValue(node.getOwnerDocument().getDocumentElement(), "id");
			IPluginInstallationAction action = factory.getHeaderFileAction(src,targetDir,id);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction> getResourceFileActionsForPlatform(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		NodeList resourceFiles = getResourceFileNodes(node);
		for (int i = 0; i < resourceFiles.getLength(); i++) {
			String src = getAttributeValue(resourceFiles.item(i), "src");
			IPluginInstallationAction action = factory.getResourceFileAction(src);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction> getSourceFilesActionsForPlatform(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		NodeList sourceFiles = getSourceFileNodes(node);
		for (int i = 0; i < sourceFiles.getLength(); i++) {
			Node current = sourceFiles.item(i);
			String src = getAttributeValue(current, "src");
			String targetDir = getAttributeValue(current,"target-dir" );
			String framework = getAttributeValue(current,"framework" );
			String compilerFlags = getAttributeValue(current, "compiler-flags");
			String id = CordovaPluginXMLHelper.getAttributeValue(node.getOwnerDocument().getDocumentElement(), "id");
			IPluginInstallationAction action = factory.getSourceFileAction(src, targetDir, framework,id, compilerFlags);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction> getAssetActionsForPlatform(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		NodeList assets = getAssets(node);
		for (int i = 0; i < assets.getLength(); i++) {
			Node current = assets.item(i);
			String src = getAttributeValue(current, "src");
			String target = getAttributeValue(current, "target");
			IPluginInstallationAction action = factory.getAssetAction(src,target);
			list.add(action);
		}
		return list;
	}
	
	private PluginInstallRecordAction getPluginInstallRecordAction(Document pluginXml) throws CoreException{
		String id = CordovaPluginXMLHelper.getAttributeValue(pluginXml.getDocumentElement(),"id");
		boolean saveVersion = Platform.getPreferencesService().getBoolean(PlatformConstants.HYBRID_UI_PLUGIN_ID, 
				PlatformConstants.PREF_SHRINKWRAP_PLUGIN_VERSIONS,false,null);
		String version = null;
		if(saveVersion){
			version = CordovaPluginXMLHelper.getAttributeValue(pluginXml.getDocumentElement(),"version");
		}
		Node n = CordovaPluginXMLHelper.getNameNode(pluginXml.getDocumentElement());
		if(n == null){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"plugin.xml is missing name"));
		}
		return new PluginInstallRecordAction(project, n.getTextContent().trim(), id, version);
	}
		
}
