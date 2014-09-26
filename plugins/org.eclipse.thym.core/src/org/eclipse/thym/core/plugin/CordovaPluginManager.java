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


import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getAssetNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getAttributeValue;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getConfigFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getDependencyNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getFrameworkNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getLibFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getPlatformNode;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getPreferencesNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getResourceFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.getSourceFileNodes;
import static org.eclipse.thym.core.plugin.CordovaPluginXMLHelper.stringifyNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.jgit.api.errors.CanceledException;
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
import org.eclipse.thym.core.plugin.RestorableCordovaPlugin.Type;
import org.eclipse.thym.core.plugin.actions.ActionVariableHelper;
import org.eclipse.thym.core.plugin.actions.ConfigXMLUpdateAction;
import org.eclipse.thym.core.plugin.actions.CopyFileAction;
import org.eclipse.thym.core.plugin.actions.DependencyInstallAction;
import org.eclipse.thym.core.plugin.actions.PluginInstallRecordAction;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
		if(monitor.isCanceled())
			return;
		
		Document doc = readPluginXML(directory);
		doInstallPlugin(directory,doc, overwrite, monitor);
		String id = CordovaPluginXMLHelper.getAttributeValue(doc.getDocumentElement(), "id");	
		JsonObject source = new JsonObject();
		source.addProperty("type", "local");
		source.addProperty("path", directory.toString());
		this.saveFetchMetadata(source,id,monitor );
		List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>(1);
		Map<String,String> params = new HashMap<String, String>();
		params.put("installPath", directory.toString());
		actions.add(getPluginInstallRecordAction(doc,params));
		runActions(actions,false,overwrite,monitor); 
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
	 *<li>if plugin.xml is missing</li>
	 *<li>if plug-ins directory is missing on the project</li>
	 *<li>if an error occurs during installation</li>
	 *</ul>
	 */
	public void installPlugin(CordovaRegistryPluginVersion plugin, FileOverwriteCallback overwrite, boolean isDependency, IProgressMonitor monitor ) throws CoreException{
		if(monitor == null )
			monitor = new NullProgressMonitor();
		if(monitor.isCanceled())
					return;
		
		CordovaPluginRegistryManager regMgr = new CordovaPluginRegistryManager(CordovaPluginRegistryManager.DEFAULT_REGISTRY_URL);
		File directory = regMgr.getInstallationDirectory(plugin,monitor);
		Document doc = readPluginXML(directory);
		doInstallPlugin(directory,doc,overwrite,monitor);
		
		String id = CordovaPluginXMLHelper.getAttributeValue(doc.getDocumentElement(), "id");	
		JsonObject source = new JsonObject();
		source.addProperty("type", "registry");
		source.addProperty("id", id);
		this.saveFetchMetadata(source,id,monitor );
		if(!isDependency){//update config.xml 
			List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>(1);
			actions.add(getPluginInstallRecordAction(doc, null));
			runActions(actions,false,overwrite,monitor); 
		}
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
		File tempRepoDirectory = new File(FileUtils.getTempDirectory(), "cordova_plugin_tmp_"+Long.toString(System.currentTimeMillis()));
		tempRepoDirectory.deleteOnExit();
		try {
			if(monitor.isCanceled())
				return;
			monitor.subTask("Clone plugin repository");
			String gitUrl = uri.getScheme()+":" + uri.getSchemeSpecificPart();
			Git git = Git.cloneRepository().setDirectory(tempRepoDirectory).setURI(gitUrl).call();
			File pluginDirectory = tempRepoDirectory;
			String fragment = uri.getFragment();
			String commit = null;
			String subdir = null;

			if(fragment != null ){
				int idx = fragment.indexOf(':');
				if(idx <0 ){
					idx = fragment.length();
				}
				commit = fragment.substring(0, idx);
				subdir = fragment.substring(Math.min(idx+1, fragment.length()));
				if(monitor.isCanceled()){
					throw new CanceledException("Plug-in installation cancelled");
				}
				if(commit != null && !commit.isEmpty()){
					git.checkout().setName(commit).call();
				}
				monitor.worked(1);
				
				if(subdir != null && !subdir.isEmpty()){
					pluginDirectory = new File(tempRepoDirectory, subdir);
					if(!pluginDirectory.isDirectory()){
						throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, 
								NLS.bind("{0} directory does not exist in this git repository", subdir ) ));
					}
				}
			}
			SubProgressMonitor sm = new SubProgressMonitor(monitor, 1);
			
			Document doc = readPluginXML(pluginDirectory);
			this.doInstallPlugin(pluginDirectory,doc,overwrite,sm);
			String id = CordovaPluginXMLHelper.getAttributeValue(doc.getDocumentElement(), "id");	
			JsonObject source = new JsonObject();
			source.addProperty("type", "git");
			source.addProperty("url", gitUrl);
			if(subdir != null && !subdir.isEmpty()){
				source.addProperty("subdir", subdir);
			}
			if(commit != null && !commit.isEmpty()){
				source.addProperty("ref", commit);
			}
			this.saveFetchMetadata(source,id,monitor );
			if(!isDependency){//update config.xml 
				List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>(1);
				Map<String,String> params = new HashMap<String, String>();
				params.put("url", uri.toString());
				actions.add(getPluginInstallRecordAction(doc,params));
				runActions(actions,false,overwrite,monitor); 
			}	
		} catch (GitAPIException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Error cloning the plugin repository", e));
		} finally{
			monitor.done();
		}
	}
	
	/**
	 * Fixes the installation of a plugin by running the first stage actions. 
	 * 
	 * @see {@link #collectInstallActions(Document, String, FileOverwriteCallback)}
	 * @param plugin
	 * @param overwrite
	 * @param monitor
	 * @throws CoreException
	 */
	public void fixInstalledPlugin(final CordovaPlugin plugin, final FileOverwriteCallback overwrite, IProgressMonitor monitor ) throws CoreException{
		File directory = getPluginHomeDirectory(plugin);
		Document doc = readPluginXML(directory);
		List<IPluginInstallationAction> actions = collectInstallActions(doc, plugin.getId(), overwrite);
		actions.add(getPluginInstallRecordAction(doc, null));
		runActions(actions,false,overwrite,monitor); 
		resetInstalledPlugins();
	}
	
	/**
	 * Does the actual copying of the plugin to plugins directory and runs the first stage 
	 * install actions.
	 * 
	 * @param directory
	 * @param doc
	 * @param overwrite
	 * @param monitor
	 * @throws CoreException
	 */
	private void doInstallPlugin(File directory,Document doc,
			FileOverwriteCallback overwrite, IProgressMonitor monitor)
			throws CoreException {
		
		String id = CordovaPluginXMLHelper.getAttributeValue(doc.getDocumentElement(), "id");
		if(isPluginInstalled(id)){
			HybridCore.log(IStatus.WARNING, "Cordova Plugin ("+id+") is already installed, skipping.",null);
		}
		
		IFolder plugins = this.project.getProject().getFolder(PlatformConstants.DIR_PLUGINS);
		if( !plugins.exists() ){
			plugins.create(true, true, monitor);
		}
		
		//collect first stage install actions
		List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>();
		File destination = new File(plugins.getLocation().toFile(), id);
		CopyFileAction copy = new CopyFileAction(directory, destination);
		actions.add(copy);
		actions.addAll(collectInstallActions( doc, id, overwrite));
		runActions(actions,false,overwrite,monitor); 
		resetInstalledPlugins();
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
	
	private void saveFetchMetadata(JsonObject source, String id, IProgressMonitor monitor )throws CoreException{
		IFolder plugins = getPluginsFolder();
		IFolder pluginHome = plugins.getFolder(id);
		IFile file = pluginHome.getFile(".fetch.json");

		JsonObject object = new JsonObject();
		object.add("source", source);
		Gson gson = new Gson();
		String jsonString = gson.toJson(object);
		InputStream stream = new ByteArrayInputStream(jsonString.getBytes());
		if(file.exists()){
			file.setContents(stream, IFile.FORCE, monitor);
		}else{
			file.create(stream, true, monitor);
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
		IResource pluginsDir = getPluginsFolder();
		List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>();
		File destination = new File(pluginsDir.getLocation().toFile(), id);
		CopyFileAction copy = new CopyFileAction(dir.getLocation().toFile(), destination);
		actions.add(copy);
		actions.addAll(collectInstallActions( doc, id, cb));
		actions.add(getPluginInstallRecordAction(doc, null));
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
		IFolder plugins = getPluginsFolder();
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
					rp.setType(Type.REGISTRY);
					String version = params.get("version");
					if (version != null){
						rp.setVersion(version);
					}
					String url = params.get("url");
					if(url != null ){
						rp.setUrl(url);
						rp.setType(Type.GIT);
					}
					String path = params.get("installPath");
					if(path != null ){
						rp.setPath(path);
						rp.setType(Type.LOCAL);
					}
					
					restorable.add(rp);
				}
			}
		}
	return restorable;
	}
	
	/**
	 * Collects all the actions for first stage install/uninstall
	 * First stage actions are.
	 * <ul>
	 * <li>dependency installations</li>
	 * <li>config.xml changes</li>
	 * <li>preferences with variables</li>
	 * </ul>
	 */
	private List<IPluginInstallationAction> collectInstallActions( Document doc, String id,  FileOverwriteCallback overwrite) {
		List<IPluginInstallationAction> actions = new ArrayList<IPluginInstallationAction>();
		//collect platform independent actions
		actions.addAll(collectDependencyActions(doc.getDocumentElement(), overwrite));
		actions.addAll(collectConfigXMLActions(doc.getDocumentElement()));
		actions.addAll(collectVariablePreferences(doc.getDocumentElement()));
		
		//platform actions
		List<PlatformSupport> platforms = HybridCore.getPlatformSupports();	
		for (PlatformSupport platformSupport : platforms) {
			Element platformNode = getPlatformNode(doc, platformSupport.getPlatformId());
			if(platformNode != null ){
				actions.addAll(collectDependencyActions(platformNode, overwrite));
				actions.addAll(collectConfigXMLActions(platformNode));
				actions.addAll(collectVariablePreferences(platformNode));
			}
		}
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
		IFolder plugins = getPluginsFolder();
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

	private IFolder getPluginsFolder() {
		IFolder plugins = this.project.getProject().getFolder(PlatformConstants.DIR_PLUGINS);
		return plugins;
	}
	
	private void runActions(final List<IPluginInstallationAction> actions, boolean runUnInstall, FileOverwriteCallback overwrite, IProgressMonitor monitor ) throws CoreException{
		PluginInstallActionsRunOperation op = new PluginInstallActionsRunOperation(actions, runUnInstall, overwrite,project.getProject());
		ResourcesPlugin.getWorkspace().run(op, monitor);
	}
	/*
	 * . collect common actions 
	 * . collect all js-module actions (for copying source files)
	 * . create cordova_plugin.js
	 * . collect all platform specific tags
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
		
		// Process jsmodules 
		allActions.addAll(collectCommonAndPlatformJSModuleActions(plugin, platform.getPlatformId(), actionFactory)); // add all js-module actions
		
		//collect common actions
		allActions.addAll(collectAssetActions(doc.getDocumentElement(), actionFactory));
		allActions.addAll(collectConfigFileActions(doc.getDocumentElement(), actionFactory));
		allActions.addAll(collectSourceFilesActions(doc.getDocumentElement(), actionFactory));
		allActions.addAll(collectResourceFileActions(doc.getDocumentElement(), actionFactory));
		allActions.addAll(collectHeaderFileActions(doc.getDocumentElement(), actionFactory));
		allActions.addAll(collectLibFileActions(doc.getDocumentElement(), actionFactory)) ;
		allActions.addAll(collectFrameworkActions(doc.getDocumentElement(), actionFactory ,plugin ));
		
		//collect platform actions
		Element node = getPlatformNode(doc, platform.getPlatformId());
		if( node != null ){
			allActions.addAll(collectAssetActions(node,actionFactory ));
			allActions.addAll(collectConfigFileActions(node, actionFactory));
			allActions.addAll(collectSourceFilesActions(node, actionFactory));
			allActions.addAll(collectResourceFileActions(node, actionFactory));
			allActions.addAll(collectHeaderFileActions(node, actionFactory));
			allActions.addAll(collectLibFileActions(node, actionFactory)) ;
			allActions.addAll(collectFrameworkActions(node, actionFactory,plugin ));
			
			//We do not need to create this file 
			//with every plugin. TODO: find a better place
			allActions.add(actionFactory.getCreatePluginJSAction(this.getCordovaPluginJSContent(platform.getPlatformId())));
		}
		runActions(allActions,false,overwrite,monitor);
	}
	
	private List<IPluginInstallationAction> collectCommonAndPlatformJSModuleActions(CordovaPlugin plugin,String platformId,AbstractPluginInstallationActionsFactory factory) {
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

	/**
	 * Collect the actions that are targeted for the config.xml file on the node.
	 * Collects actions from immediate children only.
	 * @param node
	 * @return
	 */
	private List<IPluginInstallationAction> collectConfigXMLActions(Element node ){
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> configFiles = getConfigFileNodes(node);
		for (Element current: configFiles) {
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
		return list;
	}
	/**
	 * Collects actions that require preference definitions on the config.xml 
	 * This method actually creates {@link ConfigXMLUpdateAction}s that will inject a 
	 * preference node to config.xml
	 * 
	 * Collects actions from immediate children only.
	 * 
	 * @param node
	 * @return
	 */
	private List<IPluginInstallationAction> collectVariablePreferences(Element node){
		List<Element> preferences = getPreferencesNodes(node);
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		for (Element current : preferences) {
			String name = getAttributeValue(current, "name");
			IPluginInstallationAction action = new ConfigXMLUpdateAction(
					this.project, "/widget",
					" <config-file target=\"res/xml/config.xml\" parent=\"/widget\">"
							+ "<preference name=\"" + name
							+ "\" value=\"PLEASE_DEFINE\"/>" + "</config-file>");
			list.add(action);
		}
		return list;
	}
	
	/**
	 * Collects dependency actions on the given node. 
	 * Collects actions from immediate children only.
	 * 
	 * @param node
	 * @return
	 */
	private List<IPluginInstallationAction> collectDependencyActions(Element node, FileOverwriteCallback overwrite){
		List<Element> dependencyNodes = getDependencyNodes(node);
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		for (Element dependencyNode: dependencyNodes) {
			String dependencyId = getAttributeValue(dependencyNode, "id");
			String url = getAttributeValue(dependencyNode, "url");
			String commit = getAttributeValue(dependencyNode, "commit");
			String subdir = getAttributeValue(dependencyNode, "subdir");
			URI uri = null;
			if(url != null && !url.isEmpty()){
				if(!url.endsWith(".git")){
					url= url+".git";
				}
				if(commit != null || subdir != null ){
					url = url+"#";
					if(commit!=null){
						url = url+commit;
					}
					if(subdir != null ){
						url=url+":"+subdir;
					}
				}
				uri = URI.create(url);
			}
			DependencyInstallAction action = new DependencyInstallAction(dependencyId, uri, this.project, overwrite);
			list.add(action);
		}
		return list;
	}
	
	
	private List<IPluginInstallationAction> collectFrameworkActions(Element node,
			AbstractPluginInstallationActionsFactory factory, CordovaPlugin plugin) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> frameworks = getFrameworkNodes(node);
		for (Element current : frameworks) {
			String src = getAttributeValue(current, "src");
			String weak = getAttributeValue(current, "weak");
			String custom = getAttributeValue(current, "custom");
			String type = getAttributeValue(current, "type");
			String parent = getAttributeValue(current, "parent");
			IPluginInstallationAction action = factory.getFrameworkAction(src,weak,plugin.getId(), custom,type,parent);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction> collectLibFileActions(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> libFiles = getLibFileNodes(node);
		for (Element current : libFiles) {
			String src = getAttributeValue(current, "src");
			String arch = getAttributeValue(current, "arch");
			IPluginInstallationAction action = factory.getLibFileAction(src,arch);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction>  collectConfigFileActions(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> configFiles = getConfigFileNodes(node);
		for (Element current: configFiles) {
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
	
	private List<IPluginInstallationAction> collectHeaderFileActions(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> headerFiles = CordovaPluginXMLHelper.getHeaderFileNodes(node);
		for (Element current : headerFiles) {
			String src = getAttributeValue(current, "src");
			String targetDir = getAttributeValue(current,"target-dir" );
			String id = CordovaPluginXMLHelper.getAttributeValue(node.getOwnerDocument().getDocumentElement(), "id");
			IPluginInstallationAction action = factory.getHeaderFileAction(src,targetDir,id);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction> collectResourceFileActions(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> resourceFiles = getResourceFileNodes(node);
		for (Element current : resourceFiles) {
			String src = getAttributeValue(current, "src");
			String target = getAttributeValue(current, "target");
			IPluginInstallationAction action = factory.getResourceFileAction(src, target);
			list.add(action);
		}
		return list;
	}

	private List<IPluginInstallationAction> collectSourceFilesActions(Element node,
			AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> sourceFiles = getSourceFileNodes(node);
		for (Element current : sourceFiles) {
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

	private List<IPluginInstallationAction> collectAssetActions(Element node, AbstractPluginInstallationActionsFactory factory) {
		ArrayList<IPluginInstallationAction> list = new ArrayList<IPluginInstallationAction>();
		List<Element> assets = getAssetNodes(node);
		for (Element current : assets) {
			String src = getAttributeValue(current, "src");
			String target = getAttributeValue(current, "target");
			IPluginInstallationAction action = factory.getAssetAction(src,target);
			list.add(action);
		}
		return list;
	}
	
	private PluginInstallRecordAction getPluginInstallRecordAction(Document pluginXml, Map<String,String> additionalParams) throws CoreException{
		Map<String,String> params = new HashMap<String, String>();
		if(additionalParams != null ){
			params.putAll(additionalParams);
		}
		String id = CordovaPluginXMLHelper.getAttributeValue(pluginXml.getDocumentElement(),"id");
		params.put("id", id);
		
		boolean saveVersion = Platform.getPreferencesService().getBoolean(PlatformConstants.HYBRID_UI_PLUGIN_ID, 
				PlatformConstants.PREF_SHRINKWRAP_PLUGIN_VERSIONS,false,null);
		String version = null;
		if(saveVersion){
			version = CordovaPluginXMLHelper.getAttributeValue(pluginXml.getDocumentElement(),"version");
			params.put("version", version);
		}
		Node n = CordovaPluginXMLHelper.getNameNode(pluginXml.getDocumentElement());
		if(n == null){
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,"plugin.xml is missing name"));
		}
		return new PluginInstallRecordAction(project, n.getTextContent().trim(), params);
	}
		
}
