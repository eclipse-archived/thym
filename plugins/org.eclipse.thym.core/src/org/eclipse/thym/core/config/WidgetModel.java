/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc. 
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.model.ModelLifecycleEvent;
import org.eclipse.wst.sse.core.internal.provisional.IModelLifecycleListener;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.cleanup.CleanupProcessorXML;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Manager for all access to the config.xml (Widget) model. 
 * Model is strictly tied with the {@link Document} that 
 * it was created for. Reading, persistence etc of the Document 
 * object used to create Widget model are responsibility of the 
 * caller.
 *  
 * @author Gorkem Ercan
 * @author Kaloyan Raev
 *
 */
@SuppressWarnings("restriction")
public class WidgetModel implements IModelLifecycleListener{
	
	private static Map<HybridProject, WidgetModel> widgetModels = new HashMap<HybridProject, WidgetModel>();
	public static final String[] ICON_EXTENSIONS = {"gif", "ico", "jpeg", "jpg", "png","svg" };
	
	private File configFile;
	private Widget editableWidget;
	private Widget readonlyWidget;
	private long readonlyTimestamp;

	public IStructuredModel underLyingModel;
	
	
	private WidgetModel(HybridProject project){
		this(getConfigXml(project));
	}
	
	private WidgetModel(File file){
		this.configFile = file; 
	}
	
	
	public static final WidgetModel getModel(HybridProject project){
		if( project == null ){
			throw new NullPointerException("Widget model can not be created because hybrid project is null");
		}
		if( !widgetModels.containsKey(project) ){
			synchronized (WidgetModel.class) {
				WidgetModel wm = new WidgetModel(project);
				widgetModels.put(project,wm);
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
	public static final Widget parseToWidget(File file) throws CoreException{
		if( file.isFile()){
			WidgetModel model = new WidgetModel(file);
			return model.getWidgetForRead();
		}else{
			throw new IllegalArgumentException(NLS.bind("File {0} does not exist ",	 file.toString()));
		}
	}
	
	
	public static final void shutdown(){
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
	 * 	<ul>
	 *   <li>if config.xml can not be parsed</li>
	 *   <li>its contents is not readable</li>
	 *   </ul>
	 *
	 */
	public Widget getWidgetForRead() throws CoreException{
		long enter = System.currentTimeMillis();
		if (!this.configFile.exists()) {
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
					throw new CoreException(new Status(IStatus.ERROR,
							HybridCore.PLUGIN_ID,
							"Parser error when parsing config.xml", e));
				} catch (SAXException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							HybridCore.PLUGIN_ID, "Failed to parse config.xml", e));
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							HybridCore.PLUGIN_ID,
							"IO error when parsing config.xml", e));
				}
			}
		}
		HybridCore.trace("Completed WidgetModel.getWidgetForRead it "+ Long.toString(System.currentTimeMillis() - enter)+ "ms");
		return readonlyWidget;
	}
	
	
	public Widget getWidgetForEdit() throws CoreException {
		long enter = System.currentTimeMillis();
		if (editableWidget == null){
			synchronized (this) {
				IFile configXml = configXMLtoIFile();
				if(configXml == null ){
					return null;
				}
				IModelManager manager = StructuredModelManager.getModelManager();
				try {
					underLyingModel = manager.getModelForEdit(configXml);
					if ((underLyingModel != null) && (underLyingModel instanceof IDOMModel)) {
						underLyingModel.addModelLifecycleListener(this);
						IDOMModel domModel = (IDOMModel) underLyingModel;
						editableWidget = load(domModel.getDocument());
					}
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							HybridCore.PLUGIN_ID,
							"Error creating widget model", e));
				}
			}
		}
		HybridCore.trace("Completed WidgetModel.getWidgetForEdit it "+ Long.toString(System.currentTimeMillis() - enter)+ "ms");
		return editableWidget;
	}

	protected IFile configXMLtoIFile() {
		IFile[] configFileCandidates = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(configFile.toURI());
		if(configFileCandidates == null || configFileCandidates.length == 0){
			return null;
		}
		// In case of linked resources, there is a chance that this will point to the wrong file.
		// This may happen only if two eclipse projects link the same config.xml. 
		return configFileCandidates[0];
	}

	/**
	 * Syncs the changes done to the config.xml directly to the model by reloading it. 
	 * This method needs to be called any time config.xml is modified without using the 
	 * {@link Widget} instance.
	 * 
	 * @throws CoreException
	 */
	public void resyncModel() throws CoreException{
		if (this.underLyingModel != null) {
			try {
				IFile configXml = configXMLtoIFile();
				if(configXml != null){
					configXml.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
					underLyingModel.getModelHandler().getModelLoader().load(configXml, underLyingModel);
				}
			} catch (IOException e) {
				HybridCore.log(IStatus.ERROR,
						"Error resyncing the editable model", e);
			}
		}
	}

	private static File getConfigXml(HybridProject project) {
		IFile configXml = project.getConfigFile();
		return configXml.getLocation().toFile();
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
					throw new CoreException(new Status(IStatus.ERROR,
							HybridCore.PLUGIN_ID,
							"Error saving changes to config.xml", e));
				}
			}
		}
	}
	
	/**
	 * Creates an {@link Author} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return new Author
	 */
	public Author createAuthor(Widget widget){
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_AUTHOR, Author.class);
	}
	/**
	 * Creates a {@link Content} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return  new Content
	 */
	public Content createContent(Widget widget){
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_CONTENT, Content.class);
	}
	/**
	 * Creates a {@link Preference} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return new Preference 
	 */
	public Preference createPreference(Widget widget){
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_PREFERENCE, Preference.class);
	}
	/**
	 * Creates a {@link Feature} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return new Feature
	 */
	public Feature createFeature(Widget widget){
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_FEATURE, Feature.class);
	}
	/**
	 * Creates a {@link Access} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return new Access 
	 */
	public Access createAccess(Widget widget){
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_ACCESS, Access.class);
	}
	
	/**
	 * Creates a {@link Plugin} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return new Splash 
	 */	
	public Icon createIcon(Widget widget){
		return createObject(widget,NS_W3C_WIDGET, WIDGET_TAG_ICON,Icon.class);
	}
	
	/**
	 * Creates a {@link Plugin} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return new Splash 
	 */
	public Splash createSplash(Widget widget){
		return createObject(widget, null, WIDGET_TAG_SPLASH, Splash.class);
	}
	
	/**
	 * Creates a {@link License} instance. This also creates the necessary 
	 * DOM elements on the {@link Document} associated with the widget.
	 * The new DOM elements are not inserted to the tree until 
	 * {@link Widget}'s proper set/add method is called
	 * 
	 * @param widget - parent widget
	 * @return new License
	 */
	public License createLicense(Widget widget){
		return createObject(widget, NS_W3C_WIDGET, WIDGET_TAG_LICENSE, License.class);
	}
	
	
	private <T extends AbstractConfigObject> T createObject(Widget widget, String namespace, String tag, Class<T> clazz ){
		if(widget != editableWidget){
			throw new IllegalArgumentException("Widget model is not editable");
		}
		Document doc = widget.itemNode.getOwnerDocument();
		if (doc == null )
			throw new IllegalStateException("Widget is not properly constructed");
		Element el = doc.createElementNS(namespace, tag);
		
		try {
			return clazz.getDeclaredConstructor(Node.class).newInstance(el);
		} catch (Exception e){
			HybridCore.log(IStatus.ERROR, "Error invoking the Node constructor for config model object", e);
		}
		return null;
	}

	@Override
	public void processPostModelEvent(ModelLifecycleEvent event) {
		if(event.getType() == ModelLifecycleEvent.MODEL_DIRTY_STATE && !underLyingModel.isDirty()){
			synchronized (this) {
				reloadEditableWidget();
				//release the readOnly model to be reloaded
				this.readonlyWidget = null;
				this.readonlyTimestamp = -1;
			}
		}
	}

	@Override
	public void processPreModelEvent(ModelLifecycleEvent event) {
	}
	
	public synchronized void dispose(){
		if(underLyingModel != null ){
			underLyingModel.releaseFromEdit();
			underLyingModel = null;
		}
		this.editableWidget = null;
		this.readonlyWidget = null;
	}
	
}
