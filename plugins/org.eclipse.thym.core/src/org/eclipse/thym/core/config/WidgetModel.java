/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - [447351] synchronization between Overview and Source page
 *******************************************************************************/
package org.eclipse.thym.core.config;

import static org.eclipse.thym.core.config.WidgetModelConstants.NS_W3C_WIDGET;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_ACCESS;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_AUTHOR;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_CONTENT;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_FEATURE;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_ICON;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_LICENSE;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_PREFERENCE;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_SPLASH;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_ENGINE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.util.EngineUtils;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelStateListener;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.cleanup.CleanupProcessorXML;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Manager for all access to the config.xml (Widget) model. Model is strictly
 * tied with the {@link Document} that it was created for. Reading, persistence
 * etc of the Document object used to create Widget model are responsibility of
 * the caller.
 * 
 * @author Gorkem Ercan
 * @author Kaloyan Raev
 *
 */
@SuppressWarnings("restriction")
public class WidgetModel implements IModelStateListener {

	private static Map<HybridProject, WidgetModel> widgetModels = new HashMap<HybridProject, WidgetModel>();
	private static ResourceChangeListener resourceChangeListener;
	public static final String[] ICON_EXTENSIONS = { "gif", "ico", "jpeg", "jpg", "png", "svg" };

	private File configFile;
	private Widget editableWidget;
	private Widget readonlyWidget;
	private long readonlyTimestamp;
	private Widget lastWidget;

	public IStructuredModel underLyingModel;

	private WidgetModel(HybridProject project) {
		this(getConfigXml(project));
	}

	private WidgetModel(File file) {
		this.configFile = file;
		if (resourceChangeListener == null) {
			resourceChangeListener = new ResourceChangeListener();
		}
	}

	public static final WidgetModel getModel(HybridProject project) {
		if (project == null) {
			throw new NullPointerException("Widget model can not be created because hybrid project is null");
		}
		if (!widgetModels.containsKey(project)) {
			synchronized (WidgetModel.class) {
				WidgetModel wm = new WidgetModel(project);
				if (wm.configFile != null) {
					// Do not cache if config file is not present to allow it to correct itself.
					// This typically happens during project creation when widget model
					// is accessed before templates are copied.
					widgetModels.put(project, wm);
				}
				return wm;
			}
		}
		return widgetModels.get(project);
	}

	/**
	 * Parses a given config.xml file to WidgetModel.
	 * 
	 * @param file
	 * @return
	 * @throws CoreException
	 */
	public static final Widget parseToWidget(File file) throws CoreException {
		if (file.isFile()) {
			WidgetModel model = new WidgetModel(file);
			return model.getWidgetForRead();
		} else {
			throw new IllegalArgumentException(NLS.bind("File {0} does not exist ", file.toString()));
		}
	}

	public static final void shutdown() {
		Collection<WidgetModel> createdModels = widgetModels.values();
		for (WidgetModel widgetModel : createdModels) {
			widgetModel.dispose();
		}
	}

	/**
	 * Returns the {@link Widget} model for the config.xml
	 * 
	 * @return widget
	 * @throws CoreException
	 *             <ul>
	 *             <li>if config.xml can not be parsed</li>
	 *             <li>its contents is not readable</li>
	 *             </ul>
	 *
	 */
	public Widget getWidgetForRead() throws CoreException {
		long enter = System.currentTimeMillis();
		if (this.configFile == null || !this.configFile.exists()) {
			return null;
		}
		if (readonlyWidget == null || readonlyTimestamp != configFile.lastModified()) {
			synchronized (this) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				dbf.setValidating(false);
				DocumentBuilder db;
				try {
					db = dbf.newDocumentBuilder();
					Document configDocument = db.parse(configFile);
					readonlyWidget = load(configDocument);
					readonlyTimestamp = configFile.lastModified();

				} catch (ParserConfigurationException e) {
					throw new CoreException(
							new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Parser error when parsing config.xml", e));
				} catch (SAXException e) {
					throw new CoreException(
							new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Failed to parse config.xml", e));
				} catch (IOException e) {
					throw new CoreException(
							new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "IO error when parsing config.xml", e));
				}
			}
		}
		HybridCore.trace("Completed WidgetModel.getWidgetForRead it "
				+ Long.toString(System.currentTimeMillis() - enter) + "ms");
		return readonlyWidget;
	}

	public Widget getWidgetForEdit() throws CoreException {
		long enter = System.currentTimeMillis();
		if (editableWidget == null) {
			synchronized (this) {
				IFile configXml = configXMLtoIFile();
				if (configXml == null) {
					return null;
				}
				IModelManager manager = StructuredModelManager.getModelManager();
				try {
					underLyingModel = manager.getModelForEdit(configXml);
					if ((underLyingModel != null) && (underLyingModel instanceof IDOMModel)) {
						underLyingModel.addModelStateListener(this);
						IDOMModel domModel = (IDOMModel) underLyingModel;
						editableWidget = load(domModel.getDocument());
						lastWidget = load(domModel.getDocument());
					}
				} catch (IOException e) {
					throw new CoreException(
							new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Error creating widget model", e));
				}
			}
		}
		HybridCore.trace("Completed WidgetModel.getWidgetForEdit it "
				+ Long.toString(System.currentTimeMillis() - enter) + "ms");
		return editableWidget;
	}

	protected IFile configXMLtoIFile() {
		if (configFile == null) {
			return null;
		}
		final IFile[] configFileCandidates = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocationURI(configFile.toURI());
		if (configFileCandidates == null || configFileCandidates.length == 0) {
			return null;
		}
		// In case of linked resources, there is a chance that this will point to the
		// wrong file.
		// This may happen only if two eclipse projects link the same config.xml.
		return configFileCandidates[0];
	}

	private static File getConfigXml(HybridProject project) {
		IFile configXml = project.getConfigFile();
		if (configXml != null && configXml.exists()) {
			IPath location = configXml.getLocation();
			if (location != null) {
				return location.toFile();
			}
		}
		return null;
	}

	private Widget load(Document document) {
		Assert.isNotNull(document, "null document can not init widget");
		Element el = document.getDocumentElement();
		Assert.isNotNull(el, "null document root can not init widget");
		return new Widget(el);
	}

	public void reloadEditableWidget() {
		if (underLyingModel != null) {
			IDOMModel dom = (IDOMModel) underLyingModel;
			Document document = dom.getDocument();
			editableWidget.reload(document.getDocumentElement());
		}
	}

	public void save() throws CoreException {
		if (this.editableWidget != null && underLyingModel != null) {
			synchronized (this) {
				CleanupProcessorXML cp = new CleanupProcessorXML();
				try {
					cp.cleanupModel(underLyingModel);
					underLyingModel.save();
				} catch (IOException e) {
					throw new CoreException(
							new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Error saving changes to config.xml", e));
				}
			}
		}
	}

	/**
	 * Creates an {@link Author} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new Author
	 */
	public Author createAuthor(Widget widget) {
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_AUTHOR, Author.class);
	}

	/**
	 * Creates a {@link Content} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new Content
	 */
	public Content createContent(Widget widget) {
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_CONTENT, Content.class);
	}

	/**
	 * Creates a {@link Preference} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new Preference
	 */
	public Preference createPreference(Widget widget) {
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_PREFERENCE, Preference.class);
	}

	/**
	 * Creates a {@link Feature} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new Feature
	 */
	public Feature createFeature(Widget widget) {
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_FEATURE, Feature.class);
	}

	/**
	 * Creates a {@link Access} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new Access
	 */
	public Access createAccess(Widget widget) {
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_ACCESS, Access.class);
	}

	/**
	 * Creates a {@link Plugin} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new Splash
	 */
	public Icon createIcon(Widget widget) {
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_ICON, Icon.class);
	}

	/**
	 * Creates a {@link Plugin} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new Splash
	 */
	public Splash createSplash(Widget widget) {
		return createObject(widget, null, WIDGET_TAG_SPLASH, Splash.class);
	}

	/**
	 * Creates a {@link License} instance. This also creates the necessary DOM
	 * elements on the {@link Document} associated with the widget. The new DOM
	 * elements are not inserted to the tree until {@link Widget}'s proper set/add
	 * method is called
	 * 
	 * @param widget
	 *            - parent widget
	 * @return new License
	 */
	public License createLicense(Widget widget) {
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_LICENSE, License.class);
	}

	public Engine createEngine(Widget widget) {
		return createObject(widget, null, WIDGET_TAG_ENGINE, Engine.class);
	}

	private <T extends AbstractConfigObject> T createObject(Widget widget, String namespace, String tag,
			Class<T> clazz) {
		if (widget != editableWidget) {
			throw new IllegalArgumentException("Widget model is not editable");
		}
		Document doc = widget.itemNode.getOwnerDocument();
		if (doc == null)
			throw new IllegalStateException("Widget is not properly constructed");
		Element el = doc.createElementNS(namespace, tag);

		try {
			return clazz.getDeclaredConstructor(Node.class).newInstance(el);
		} catch (Exception e) {
			HybridCore.log(IStatus.ERROR, "Error invoking the Node constructor for config model object", e);
		}
		return null;
	}

	public synchronized void dispose() {
		if (underLyingModel != null) {
			underLyingModel.releaseFromEdit();
			underLyingModel = null;
		}
		this.editableWidget = null;
		this.readonlyWidget = null;
		this.lastWidget = null;
	}

	/**
	 * Returns hybrid project associated with this WigetModel
	 * 
	 * @return hybrid project
	 */
	public HybridProject getProject() {
		return HybridProject.getHybridProject(configXMLtoIFile().getProject());
	}

	@Override
	public void modelAboutToBeChanged(IStructuredModel model) {
	}

	@Override
	public void modelChanged(IStructuredModel model) {
	}

	@Override
	public void modelDirtyStateChanged(IStructuredModel model, boolean isDirty) {
		if (!isDirty) {
			synchronized (this) {
				final HybridProject project = getProject();

				final IDOMModel domModel = (IDOMModel) model;

				Job updatePlugins = new Job(
						"Synchronize project " + project.getProject().getName() + " with config.xml") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						if (project.getProject().exists()) {
							reloadEditableWidget();
							Widget newWidget = load(domModel.getDocument());
							syncEngines(project, newWidget, monitor);
							syncPlugins(project, newWidget, monitor);
						}
						return Status.OK_STATUS;
					}
				};

				updatePlugins.addJobChangeListener(new JobChangeAdapter() {

					@Override
					public void done(IJobChangeEvent event) {
						lastWidget.reload(domModel.getDocument().getDocumentElement());
						reloadEditableWidget();
						readonlyWidget = null;
						readonlyTimestamp = -1;
					}
				});
				ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(project.getProject());
				updatePlugins.setRule(rule);
				updatePlugins.schedule();
			}
		}
	}

	private void syncEngines(HybridProject project, Widget newWidget, IProgressMonitor monitor) {
		List<Engine> oldEngines = lastWidget.getEngines();
		List<Engine> newEngines = newWidget.getEngines();

		if (oldEngines == null) {
			oldEngines = new ArrayList<>();
		}
		if (newEngines == null) {
			newEngines = new ArrayList<>();
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor);

		List<Engine> toInstall = new ArrayList<>(newEngines);
		toInstall.removeAll(oldEngines);

		List<Engine> toUninstall = new ArrayList<>(oldEngines);
		toUninstall.removeAll(newEngines);

		List<Engine> enginesToUpdate = getEnginesToUpdate(oldEngines, newEngines);

		subMonitor.setWorkRemaining(toInstall.size() + toUninstall.size() + enginesToUpdate.size() * 2);
		for (Engine uninstall : toUninstall) {
			try {
				project.getEngineManager().removeEngine(uninstall.getName(), subMonitor.split(1), false);
			} catch (CoreException e) {
				HybridCore.log(Status.ERROR, "Unable to remove engine " + uninstall.getName(), e);
			}
		}
		for (Engine install : toInstall) {
			try {
				project.getEngineManager().addEngine(install.getName(), install.getSpec(), subMonitor.split(1), false);
			} catch (CoreException e) {
				HybridCore.log(Status.ERROR, "Unable to add engine " + install.getName(), e);
			}
		}

		// update versions
		for (Engine engine : enginesToUpdate) {
			int index = newEngines.indexOf(engine);
			if (index != -1) { // not really needed but just in case..
				try {
					// some engines do not support "in place" update. They require platform remove,
					// then platform add
					project.getEngineManager().removeEngine(newEngines.get(index).getName(), subMonitor.split(1),
							false);
					project.getEngineManager().addEngine(newEngines.get(index).getName(),
							newEngines.get(index).getSpec(), subMonitor.split(1), false);
				} catch (CoreException e) {
					HybridCore.log(Status.ERROR, "Unable to update engine " + engine.getName(), e);
				}
			}
		}
	}

	private void syncPlugins(HybridProject project, Widget newWidget, IProgressMonitor monitor) {
		List<Plugin> oldPlugins = lastWidget.getPlugins();
		List<Plugin> newPlugins = newWidget.getPlugins();

		if (oldPlugins == null) {
			oldPlugins = new ArrayList<>();
		}
		if (newPlugins == null) {
			newPlugins = new ArrayList<>();
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor);

		List<Plugin> toInstall = new ArrayList<>(newPlugins);
		toInstall.removeAll(oldPlugins);

		List<Plugin> toUninstall = new ArrayList<>(oldPlugins);
		toUninstall.removeAll(newPlugins);

		List<Plugin> pluginsToUpdate = getPluginsToUpdate(oldPlugins, newPlugins);

		subMonitor.setWorkRemaining(toInstall.size() + toUninstall.size() + pluginsToUpdate.size() * 2);
		for (Plugin uninstall : toUninstall) {
			try {
				project.getPluginManager().unInstallPlugin(uninstall, subMonitor.split(1), false);
			} catch (CoreException e) {
				HybridCore.log(Status.ERROR, "Unable to uninstall plugin " + uninstall.getName(), e);
			}
		}
		for (Plugin install : toInstall) {
			try {
				project.getPluginManager().installPlugin(install, subMonitor.split(1), false);
			} catch (CoreException e) {
				HybridCore.log(Status.ERROR, "Unable to install plugin " + install.getName(), e);
			}
		}

		// update versions
		for (Plugin plugin : pluginsToUpdate) {
			int index = newPlugins.indexOf(plugin);
			if (index != -1) { // not really needed but just in case..
				try {
					project.getPluginManager().unInstallPlugin(plugin, subMonitor.split(1), false);
					project.getPluginManager().installPlugin(newPlugins.get(index), subMonitor.split(1), false);
				} catch (CoreException e) {
					HybridCore.log(Status.ERROR, "Unable to update plugin " + plugin.getName(), e);
				}
			}
		}
	}

	private List<Plugin> getPluginsToUpdate(List<Plugin> oldPlugins, List<Plugin> newPlugins) {
		List<Plugin> pluginsToUpdate = new ArrayList<>();
		for (Plugin plugin : oldPlugins) {
			int index = newPlugins.indexOf(plugin);
			if (index != -1) {
				String newSpec = newPlugins.get(index).getSpec();
				if (!newSpec.equals(plugin.getSpec())) {
					pluginsToUpdate.add(plugin);
				}
			}
		}
		return pluginsToUpdate;

	}

	private List<Engine> getEnginesToUpdate(List<Engine> oldEngines, List<Engine> newEngines) {
		List<Engine> enginesToUpdate = new ArrayList<>();
		for (Engine engine : oldEngines) {
			int index = newEngines.indexOf(engine);
			if (index != -1) {
				String newSpec = newEngines.get(index).getSpec();
				if (!EngineUtils.getExactVersion(newSpec).equals(EngineUtils.getExactVersion(engine.getSpec()))) {
					enginesToUpdate.add(engine);
				}
			}
		}
		return enginesToUpdate;

	}

	@Override
	public void modelResourceDeleted(IStructuredModel model) {
		dispose();
	}

	@Override
	public void modelResourceMoved(IStructuredModel oldModel, IStructuredModel newModel) {

	}

	@Override
	public void modelAboutToBeReinitialized(IStructuredModel structuredModel) {

	}

	@Override
	public void modelReinitialized(IStructuredModel structuredModel) {
	}

	class ResourceChangeListener implements IResourceChangeListener {

		ResourceChangeListener() {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(this, IResourceChangeEvent.PRE_DELETE);
		}

		public void resourceChanged(IResourceChangeEvent event) {
			IResource resource = event.getResource();
			if (resource instanceof IProject) {
				HybridProject prj = HybridProject.getHybridProject(resource.getProject());
				if (prj != null) {
					WidgetModel model = widgetModels.remove(prj);
					if (model != null) {
						model.dispose();
					}
				}
			}
		}
	}

}
