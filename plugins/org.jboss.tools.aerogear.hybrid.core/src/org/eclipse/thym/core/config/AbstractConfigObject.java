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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The root object for the all config.xml model objects.
 * 
 * @author Gorkem Ercan
 * 
 */
public class AbstractConfigObject {
	
	public class Property<T>{
		public final String name;
	    private T value;
	    public Property(String propertyName){
	    	name = propertyName;
	    }
	    
	    public T getValue() { 
	    	return value; 
	    }

	    public void setValue(T value) {
	        T old = this.value;
	        this.value = value;
	        if(propertySupport != null)
	            propertySupport.firePropertyChange(name, old, this.value);
	    }
	}
	
	
	protected PropertyChangeSupport propertySupport;
	Element itemNode;
	
	protected AbstractConfigObject(){
		propertySupport = new PropertyChangeSupport(this);
	}

	/**
	 * Returns the value of the attribute on node
	 * @param node 
	 * @param namespace
	 * @param name of the attribute
	 * @return value of the attribute or null
	 * @throws IllegalArgumentException- if node is null
	 */
	protected String getNodeAttribute(Node node, String namespace,String name) {
		if(node == null )
			throw new IllegalArgumentException("Node is null");
		
		NamedNodeMap nodeMap = node.getAttributes();
		if (nodeMap == null) {
			return null;
		}
		Node attribute = nodeMap.getNamedItemNS(namespace, name);
		if (attribute != null) {
			return attribute.getNodeValue();
		}
		return null;
	}
	
	protected String getTextContentForTag(Node node, String name){
		if(node == null )
			throw new IllegalArgumentException("Node is null" );
		Element el = (Element)node;
		NodeList nodes = el.getElementsByTagName(name);
		if(nodes.getLength()>0){
			return nodes.item(0).getTextContent();
		}
		return null;
	}

	protected boolean equalField(Object one, Object two) {
		if(one == null && two == null )
			return true;
		if( one != null && two != null )
			return one.equals(two);
		return false;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener){
		propertySupport.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener){
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener){
		propertySupport.removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener){
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}
	
	/**
	 * Set the text content value
	 * 
	 * @param element
	 * @param value
	 * @throws IllegalArgumentException if element is null
	 */
	protected void setTextContentValue(Element element, String value ){
		if(element == null )
			throw new IllegalArgumentException("Element is null");
		//We are going through all this trouble instead of using DOM 3 
		//APIs to set textContent because the underlying implementation 
		//through Structured Source Editor does not support it. 
		Node child = element.getFirstChild();
		while(child != null ){
			if(child.getNodeType() == Node.TEXT_NODE){
				element.removeChild(child);
			}
			child = child.getNextSibling();
		}
		Node textNode = element.getOwnerDocument().createTextNode(value);
		element.appendChild(textNode);
	}
	
	/**
	 * Sets the text content for a child of element. If tagName child can not 
	 * be found it creates one 
	 * 
	 * @param element
	 * @param namespace
	 * @param tagName
	 * @param value
	 * @throws IllegalArgumentException if element is null
	 */
	protected void setTextContentValueForTag(Element element, String namespace,
			String tagName, String value) {
		if ( element == null )
			throw new IllegalArgumentException("Element is null");
		
		NodeList nodes = null; 
		if (namespace == null ){
			nodes = element.getElementsByTagName(tagName);
		}else{
			nodes = element.getElementsByTagNameNS(namespace, tagName);
		}
		
		Node target = null;
		if (nodes.getLength() < 1) {
			target = element.getOwnerDocument().createElementNS(namespace,
					tagName);
			element.appendChild(target);
		} else {
			target = nodes.item(0);
		}
		Node firstChild = target.getFirstChild();
		if (firstChild != null) {
			firstChild.setNodeValue(value);
		} else {
			target.appendChild(element.getOwnerDocument().createTextNode(value));
		}

	}

	/**
	 * Sets the value of the attribute Namespace must be null if no namespace is desired. 
	 * @see Element#setAttributeNS(String, String, String)
	 * 
	 * @param element
	 * @param namespace
	 * @param attributeName
	 * @param value
	 * @throws IllegalArgumentException if element is null
	 */
	protected void setAttributeValue(Element element, String namespace, 
			String attributeName,
			String value) {
		if (element == null)
			throw new IllegalArgumentException("null Element");

		element.setAttributeNS(namespace, attributeName, value);
	}
	
}
