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
package org.eclipse.thym.core.config;

import static org.eclipse.thym.core.config.WidgetModelConstants.NAME_ATTR_SHORT;
import static org.eclipse.thym.core.config.WidgetModelConstants.NS_W3C_WIDGET;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_ATTR_ID;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_ATTR_VERSION;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_ATTR_VIEWMODES;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_ACCESS;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_AUTHOR;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_CONTENT;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_DESCRIPTION;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_FEATURE;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_ICON;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_LICENSE;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_NAME;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_PREFERENCE;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_SPLASH;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_ENGINE;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * The root object for the config.xml. 
 * 
 * @author Gorkem Ercan
 *
 */
public class Widget extends AbstractConfigObject {


	
	private Property<String> id = new Property<String>(WIDGET_ATTR_ID);
	private Property<String> version = new Property<String>(WIDGET_ATTR_VERSION);
	private Property<String> name = new Property<String>(WIDGET_TAG_NAME);
	private Property<String> shortname = new Property<String>("shortname");
	private Property<String> description= new Property<String>(WIDGET_TAG_DESCRIPTION);
	private Property<String> viewmodes = new Property<String>(WIDGET_ATTR_VIEWMODES);
	private Property<Author> author = new Property<Author>(WIDGET_TAG_AUTHOR);
	private Property<Content> content = new Property<Content>(WIDGET_TAG_CONTENT);
	private Property<License> license = new Property<License>("license");
	

	private Property<List<Preference>> preferences = new Property<List<Preference>>("preferences");
	private Property<List<Access>> accesses = new Property<List<Access>>("accesses");
	private Property<List<Feature>> features = new Property<List<Feature>>("features");
	private Property<List<Icon>> icons = new Property<List<Icon>>("icons");
	private Property<List<Splash>> splashes = new Property<List<Splash>>("splashes");
	private Property<List<Engine>> engines = new Property<List<Engine>>("engines");
	
	/**
	 * Creates a new instance from its xml representation.
	 * @param node
	 */
	Widget (Node node) {
		super();
		init(node);
	}
	/**
	 * Reloads the xml dom. This causes {@link PropertyChangeListener}s to be triggered 
	 * for any changed content.
	 * 
	 * @param node
	 */
	void reload(Node node){
		init(node);
	}

	private void init(Node node) {
		itemNode = (Element)node;
		id.setValue(getNodeAttribute(node, null, WIDGET_ATTR_ID));
		version.setValue(getNodeAttribute(node, null, WIDGET_ATTR_VERSION));
		
		
		loadName(node);
		loadDescription(node);
		loadItem(WIDGET_TAG_AUTHOR,null,node, author,Author.class);
		loadItem(WIDGET_TAG_CONTENT, null, node, content, Content.class);
		loadItem(WIDGET_TAG_LICENSE,null,node,license,License.class );
		loadListItem(WIDGET_TAG_PREFERENCE,null,node, preferences, Preference.class);
		loadListItem(WIDGET_TAG_ACCESS, null, node, accesses, Access.class);
		loadListItem(WIDGET_TAG_FEATURE, null,node,features, Feature.class);
		loadListItem(WIDGET_TAG_ICON, null, node, icons, Icon.class);
		loadListItem(WIDGET_TAG_SPLASH, null, node, splashes, Splash.class);
		loadListItem(WIDGET_TAG_ENGINE, null, node, engines, Engine.class);
	}


	
	private <T extends AbstractConfigObject> void loadItem(String tagName, String namespace, Node node, Property<T> prop, Class<T> clazz){
		Element el = (Element)node;
		NodeList nodes = el.getElementsByTagNameNS(namespace, tagName);
		if(nodes.getLength()>0){
			try{
				prop.setValue(clazz.getDeclaredConstructor(Node.class).newInstance(nodes.item(0)));
			}catch(Exception e){
				HybridCore.log(IStatus.ERROR, "Error invoking the Node constructor for config model object", e);
			}
		}else{
			prop.setValue(null);
		}
		
	}

	private void loadDescription(Node node) {
		description.setValue(getTextContentForTag(node, WIDGET_TAG_DESCRIPTION));
	}

	private void loadName(Node node) {
		name.setValue(getTextContentForTag(node, WIDGET_TAG_NAME));
		NodeList nodes = itemNode.getElementsByTagNameNS(NS_W3C_WIDGET, WIDGET_TAG_NAME);
		if(nodes.getLength() >0 ){
			shortname.setValue(getNodeAttribute(nodes.item(0),null,NAME_ATTR_SHORT));	
		}else{
			shortname.setValue(null);
		}
	}
	
	private <T extends AbstractConfigObject> void loadListItem(String tagName, String namespaceURI, Node node, Property<List<T>> props, Class<T> clazz){
		ArrayList<T> items = null; 
		Element el = (Element)node;
		
		NodeList nodes = null;
		if(namespaceURI != null ){
			nodes = el.getElementsByTagNameNS(namespaceURI,tagName);
		}else{
			nodes = el.getElementsByTagName(tagName);
		}
		
		if (nodes.getLength() > 0) {
			items = new ArrayList<T>();
			for (int i = 0; i < nodes.getLength(); i++) {
				try {
					T item = clazz.getDeclaredConstructor(Node.class)
							.newInstance(nodes.item(i));
					items.add(item);
				} catch (Exception e) {
					HybridCore.log(IStatus.ERROR,
									"Error invoking the Node constructor for config model object",
									e);
				}
			}
		}
		props.setValue(items);
	}

	public String getId() {
		return id.getValue();
	}

	public String getVersion() {
		return version.getValue();
	}
	

	public String getName() {
		return name.getValue();
	}
	
	public String getShortname() {
		return shortname.getValue();
	}

	public String getDescription() {
		return description.getValue();
	}
	
	public String getViewmodes() {
		return viewmodes.getValue();
	}

	public Author getAuthor() {
		return author.getValue();
	}
	
	public Content getContent() {
		return content.getValue();
	}
	
	public License getLicense(){
		return license.getValue();
	}
	
	public List<Preference> getPreferences() {
		return preferences.getValue();
	}
	
	public List<Access> getAccesses() {
		return accesses.getValue();
	}
	
	public List<Feature> getFeatures() {
		return features.getValue();
	}
	
	public List<Icon> getIcons() {
		return icons.getValue();
	}
	
	public List<Splash> getSplashes() {
		return splashes.getValue();
	}
	
	public List<Engine> getEngines(){
		return engines.getValue();
	}
	
	public void setId(String id) {
		this.id.setValue(id);
		setAttributeValue(itemNode, null, WIDGET_ATTR_ID, id);
	}
	

	public void setVersion(String version) {
		this.version.setValue(version);
		setAttributeValue(itemNode, null, WIDGET_ATTR_VERSION, version);
	}
	
	public void setName(String name) {
		this.name.setValue(name);
		setTextContentValueForTag(itemNode, null, WIDGET_TAG_NAME, name);
		
	}
	
	public void setShortname(String shortname) {		
		this.shortname.setValue(shortname);
		NodeList nodes = itemNode.getElementsByTagNameNS(null, WIDGET_TAG_NAME);
		if(nodes.getLength() >0 ){
			setAttributeValue((Element)nodes.item(0), null, "short", shortname);	
		}
	}

	public void setDescription(String description) {
		this.description.setValue(description);
		setTextContentValueForTag(itemNode, NS_W3C_WIDGET, WIDGET_TAG_DESCRIPTION, description);
	}

	public void setViewmodes(String viewmodes) {
		this.viewmodes.setValue(viewmodes);
		setAttributeValue(itemNode, null, WIDGET_ATTR_VIEWMODES, viewmodes);
	}
	
	public void setAuthor(Author author) {
		setTagObject(author, this.author);
		this.author.setValue( author);
	}
	
	public void setContent(Content content) {
		setTagObject(content, this.content);
		this.content.setValue(content);
	}
	
	public void setLicense( License license ){
		setTagObject(license, this.license);
		this.license.setValue(license);
	}
	
	public void addPreference(Preference preference){
		addItem(preference, preferences);
	}
	
	public void addAccess(Access access ){
		addItem(access, accesses);
	}
	
	public void addFeature( Feature feature ){
		addItem(feature,features);
	}
	
	public void addIcon( Icon icon ){
		addItem(icon,icons);
	}
	
	public void addSplash(Splash splash ){
		addItem(splash,splashes);
	}
	
	public void addEngine(Engine engine){
		addItem(engine, engines);
	}
	
	
	public void removePreference( Preference preference){
		removeItem(preference, this.preferences);
	}
	
	public void removeAccess( Access access ){
		removeItem(access, this.accesses);
	}
	
	public void removeFeature( Feature feature ){
		removeItem(feature, this.features);
	}
	
	public void removeIcon( Icon icon ){
		removeItem(icon, this.icons);
	}
	
	public void removeSplash( Splash splash ){
		removeItem(splash, this.splashes);
	}
	
	public void removeEngine(Engine engine){
		removeItem(engine, this.engines);
	}
	
	private <T extends AbstractConfigObject > void removeItem(T object, Property<List<T>> property){
		if(object == null )
			return;
		List<T> oldList = property.getValue();
		if (oldList.contains(object)){
			Node removed = this.itemNode.removeChild(object.itemNode);	
			if(removed != null){
				List<T> list = new ArrayList<T>(oldList);
				list.remove(object);
				property.setValue(list);	
			}
			
		}
	}
	
	private <T extends AbstractConfigObject> void addItem(T object, Property<List<T>> property){
		if (object == null )
			return;
		List<T> objects = new ArrayList<T>();
		if(property.getValue() == null || property.getValue().isEmpty()){
			Node newNode = itemNode.appendChild(object.itemNode);
			if (newNode != null ){
				objects.add(object);
			}
		}else{
			objects.addAll(property.getValue());
			Node ref = objects.get(objects.size()-1).itemNode.getNextSibling();
			
			Node newNode = itemNode.insertBefore(object.itemNode, ref);
			if(newNode != null ){
				objects.add(object);
			}
		}
		property.setValue(objects);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Widget) ) 
			return false;
		if(obj == this )
			return true;

		Widget that = (Widget) obj;	
		return equalField(that.getId(), this.getId()) && 
				equalField(that.getVersion(), this.getVersion());
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if(getId() != null )
			hash *= getId().hashCode();
		if(getVersion() != null )
			hash *= getVersion().hashCode();
		return hash;
	}
	
	private <T extends AbstractConfigObject > void setTagObject(T object, Property<T> prop){
		if(object == null ){
			this.itemNode.removeChild(prop.getValue().itemNode);
		}else if (prop.getValue() == null ){
			this.itemNode.appendChild(object.itemNode);
		}else{
			this.itemNode.replaceChild(object.itemNode, prop.getValue().itemNode);
		}
	}
	
}
