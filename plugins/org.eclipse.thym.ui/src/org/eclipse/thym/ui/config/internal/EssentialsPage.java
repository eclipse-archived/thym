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
package org.eclipse.thym.ui.config.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Author;
import org.eclipse.thym.core.config.Content;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.ui.plugins.internal.LaunchCordovaPluginWizardAction;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
/**
 * MultiEditor page to collect general information on the Cordova application.
 * 
 * @author Gorkem Ercan
 *
 */
public class EssentialsPage extends AbstactConfigEditorPage{
	private DataBindingContext m_bindingContext;
	
	private FormToolkit formToolkit;
	private Text txtIdtxt;
	private Text txtAuthorname;
	private Text txtDescription;
	private Text txtName;
	private Text txtEmail;
	private Text txtUrl;
	private Text txtVersion;
	private Text txtContentsource;
	

	//Author look alike class to be able to work the bindings with 
	//initial null values. 
	private class DummyAuthor{
		
		@SuppressWarnings("unused")
		public void setHref(String href) {
			Author a = createAuthor();
			a.setHref(href);

		}

		@SuppressWarnings("unused")
		public void setEmail(String email) {
			Author a = createAuthor();
			a.setEmail(email);
		}

		@SuppressWarnings("unused")
		public void setName(String name) {
			Author a =createAuthor();
			a.setName(name);
		}
		private Author createAuthor(){
			Author a = ((ConfigEditor)getEditor()).getWidgetModel().createAuthor(getWidget());
			getWidget().setAuthor(a);
			return a;
		}
		
		@SuppressWarnings("unused")
		public String getName(){ return null; }
		@SuppressWarnings("unused")
		public String getEmail(){ return null; }
		@SuppressWarnings("unused")
		public String getHref(){ return null;}
		
		@SuppressWarnings("unused")
		public void addPropertyChangeListener(PropertyChangeListener l){
			//no implementation
		}
		@SuppressWarnings("unused")		
		public void removePropertyChangeListener(PropertyChangeListener l){
			//no implementation
		}		
	}

	// Content look alike to cheat binding
	private class DummyContent{
		
		@SuppressWarnings("unused")
		public void setSrc(String href ){
			Content c = ((ConfigEditor)getEditor()).getWidgetModel().createContent(getWidget());
			getWidget().setContent(c);
			c.setSrc(href);
		}
		@SuppressWarnings("unused")
		public String getSrc(){return null;}
		
		@SuppressWarnings("unused")
		public void addPropertyChangeListener(PropertyChangeListener l){
			//no implementation
		}
		@SuppressWarnings("unused")		
		public void removePropertyChangeListener(PropertyChangeListener l){
			//no implementation
		}
		
	}
	
	public EssentialsPage(FormEditor editor) {
		super(editor, "essentials", "Overview");
		formToolkit = editor.getToolkit();
	}
	
	private Widget getWidget(){
		return ((ConfigEditor)getEditor()).getWidget();
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		final ScrolledForm form = managedForm.getForm();
		formToolkit.decorateFormHeading( form.getForm());
		managedForm.getForm().setText(getTitle());
		ColumnLayout columnLayout = new ColumnLayout();
		columnLayout.verticalSpacing = 10;
		columnLayout.horizontalSpacing = 10;
		columnLayout.maxNumColumns = 1;
		managedForm.getForm().getBody().setLayout(columnLayout);
		
		Section sctnNameAndDescription = managedForm.getToolkit().createSection(managedForm.getForm().getBody(), Section.TITLE_BAR);
		managedForm.getToolkit().paintBordersFor(sctnNameAndDescription);
		sctnNameAndDescription.setText("Name and Description");
		
		Composite composite_1 = managedForm.getToolkit().createComposite(sctnNameAndDescription, SWT.WRAP);
		managedForm.getToolkit().paintBordersFor(composite_1);
		sctnNameAndDescription.setClient(composite_1);
		composite_1.setLayout(new GridLayout(2, false));
		
		@SuppressWarnings("unused")
		Label lblId = managedForm.getToolkit().createLabel(composite_1, "ID:", SWT.NONE);
		
		txtIdtxt = managedForm.getToolkit().createText(composite_1, "New Text", SWT.NONE);
		txtIdtxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtIdtxt.setText("");
		
		Label lblName = managedForm.getToolkit().createLabel(composite_1, "Name:", SWT.NONE);
		lblName.setSize(39, 14);
		
		txtName = managedForm.getToolkit().createText(composite_1, "New Text", SWT.NONE);
		txtName.setText("");
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		@SuppressWarnings("unused")
		Label lblVersion = managedForm.getToolkit().createLabel(composite_1, "Version:", SWT.NONE);
		
		txtVersion = managedForm.getToolkit().createText(composite_1, "New Text", SWT.NONE);
		txtVersion.setText("");
		txtVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		@SuppressWarnings("unused")
		Label lblDescription = managedForm.getToolkit().createLabel(composite_1, "Description:", SWT.NONE);
		
		txtDescription = managedForm.getToolkit().createText(composite_1, "New Text", SWT.MULTI);
		txtDescription.setText("");
		GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtDescription.heightHint = 100;
		txtDescription.setLayoutData(gd_txtDescription);
		
		Label lblContentSource = new Label(composite_1, SWT.NONE);
		lblContentSource.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		managedForm.getToolkit().adapt(lblContentSource, true, true);
		lblContentSource.setText("Content Source:");
		
		txtContentsource = managedForm.getToolkit().createText(composite_1, "New Text", SWT.NONE);
		txtContentsource.setText("");
		txtContentsource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Section sctnAuthor = managedForm.getToolkit().createSection(managedForm.getForm().getBody(), Section.TITLE_BAR);
		managedForm.getToolkit().paintBordersFor(sctnAuthor);
		sctnAuthor.setText("Author");
		sctnAuthor.setExpanded(true);
		
		Composite composite = managedForm.getToolkit().createComposite(sctnAuthor, SWT.WRAP);
		managedForm.getToolkit().paintBordersFor(composite);
		sctnAuthor.setClient(composite);
		composite.setLayout(new GridLayout(2, false));
		
		Label lblName_1 = managedForm.getToolkit().createLabel(composite, "Name:", SWT.NONE);
		lblName_1.setBounds(0, 0, 59, 14);
		
		txtAuthorname = managedForm.getToolkit().createText(composite, "New Text", SWT.WRAP);
		txtAuthorname.setText("");
		txtAuthorname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		@SuppressWarnings("unused")
		Label lblEmail = managedForm.getToolkit().createLabel(composite, "Email:", SWT.NONE);
		
		txtEmail = managedForm.getToolkit().createText(composite, "New Text", SWT.NONE);
		txtEmail.setText("");
		txtEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		@SuppressWarnings("unused")
		Label lblUrl = managedForm.getToolkit().createLabel(composite, "URL:", SWT.NONE);
		
		txtUrl = managedForm.getToolkit().createText(composite, "New Text", SWT.NONE);
		txtUrl.setText("");
		txtUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		m_bindingContext = initDataBindings();
		bindAuthor(m_bindingContext); // binding seperately is necessary to be able to work with WindowBuilder
		bindContent(m_bindingContext);
		
	}

	private void bindAuthor(DataBindingContext bindingContext) {
		//
		final WritableValue value = new WritableValue();
		if (getWidget() != null) {
			Author author = getWidget().getAuthor();
			if (author == null) {
				value.setValue(new DummyAuthor());
			} else {
				value.setValue(author);
			}
			getWidget().addPropertyChangeListener("author",
					new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getNewValue() == null) {
								value.setValue(new DummyAuthor());
							} else {
								value.setValue(evt.getNewValue());
							}
						}
					});
		}
		//
		IObservableValue observeTextTxtAuthornameObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtAuthorname);
		IObservableValue authornameGetWidgetObserveValue = BeanProperties.value("name").observeDetail(value);
		UpdateValueStrategy strategy = new UpdateValueStrategy();
		strategy.setConverter(new StringToDisplayableStringConverter());
		bindingContext.bindValue(observeTextTxtAuthornameObserveWidget, authornameGetWidgetObserveValue, null,strategy);
		//
		IObservableValue observeTextTxtEmailObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtEmail);
		IObservableValue authoremailGetWidgetObserveValue = BeanProperties.value("email").observeDetail(value);
		bindingContext.bindValue(observeTextTxtEmailObserveWidget, authoremailGetWidgetObserveValue, null, null);
		//
		IObservableValue observeTextTxtUrlObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtUrl);
		IObservableValue authorhrefGetWidgetObserveValue = BeanProperties.value("href").observeDetail(value);
		bindingContext.bindValue(observeTextTxtUrlObserveWidget, authorhrefGetWidgetObserveValue, null, null);
	}

	private void bindContent(DataBindingContext bindingContext) {
		final WritableValue value = new WritableValue();
		if (getWidget() != null) {
			Content content= getWidget().getContent();
			if (content == null) {
				value.setValue(new DummyContent());
			} else {
				value.setValue(content);
			}
			getWidget().addPropertyChangeListener("content",
					new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getNewValue() == null) {
								value.setValue(new DummyContent());
							} else {
								value.setValue(evt.getNewValue());
							}
						}
					});
		}
		
		IObservableValue observeTextTxtContentsourceObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtContentsource);
		IObservableValue contentsrcGetWidgetObserveValue = BeanProperties.value("src").observeDetail(value);
		bindingContext.bindValue(observeTextTxtContentsourceObserveWidget, contentsrcGetWidgetObserveValue, new UpdateValueStrategy() {
			@Override
			protected IStatus doSet(IObservableValue observableValue,
					Object value) {
				if(value == null || value.toString().isEmpty()){
					getWidget().setContent(null);
					return Status.OK_STATUS; // Wste are done 
				}
				return super.doSet(observableValue, value);
			}
		}, null);
	}
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeTextTxtIdtxtObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtIdtxt);
		IObservableValue idGetWidgetObserveValue = BeanProperties.value("id").observe(getWidget());
		bindingContext.bindValue(observeTextTxtIdtxtObserveWidget, idGetWidgetObserveValue, null, null);
		//
		IObservableValue observeTextTxtNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtName);
		IObservableValue nameGetWidgetObserveValue = BeanProperties.value("name").observe(getWidget());
		UpdateValueStrategy strategy_3 = new UpdateValueStrategy();
		strategy_3.setConverter(new StringToDisplayableStringConverter());
		bindingContext.bindValue(observeTextTxtNameObserveWidget, nameGetWidgetObserveValue,null, strategy_3);
		//
		IObservableValue observeTextTxtDescriptionObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtDescription);
		IObservableValue descriptionGetWidgetObserveValue = BeanProperties.value("description").observe(getWidget());
		UpdateValueStrategy strategy_2 = new UpdateValueStrategy();
		strategy_2.setConverter(new StringToDisplayableStringConverter());
		bindingContext.bindValue(observeTextTxtDescriptionObserveWidget, descriptionGetWidgetObserveValue, null, strategy_2);
		//
		IObservableValue observeTextTxtVersionObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtVersion);
		IObservableValue versionGetWidgetObserveValue = BeanProperties.value("version").observe(getWidget());
		bindingContext.bindValue(observeTextTxtVersionObserveWidget, versionGetWidgetObserveValue, null, null);
		//
		return bindingContext;
	}
}
