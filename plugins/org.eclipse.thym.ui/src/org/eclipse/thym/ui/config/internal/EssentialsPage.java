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
package org.eclipse.thym.ui.config.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Author;
import org.eclipse.thym.core.config.Content;
import org.eclipse.thym.core.config.Engine;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.engine.AvailableCordovaEnginesSection;
import org.eclipse.thym.ui.plugins.internal.CordovaPluginSelectionPage;
import org.eclipse.thym.ui.plugins.internal.LaunchCordovaPluginWizardAction;
import org.eclipse.thym.ui.wizard.export.NativeArtifactExportAction;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
/**
 * MultiEditor page to collect general information on the Cordova application.
 * 
 * @author Gorkem Ercan
 *
 */
public class EssentialsPage extends AbstactConfigEditorPage implements IHyperlinkListener{
	private static final String PLUGINS_SECTION_CONTENT = "<form><p>Add Cordova plug-ins to extend your applications functionality</p>"
			+ "<li style=\"image\"  value=\"plugin\" bindent=\"5\">Search and install from a <a href=\"plugin.registry\">registry</a></li>"
			+ "<li style=\"image\"  value=\"plugin\" bindent=\"5\">Use a <a href=\"plugin.git\">git</a> URL to pull and install from a repo</li>"
			+ "<li style=\"image\"  value=\"plugin\" bindent=\"5\">Install from a <a href=\"plugin.folder\">directory</a></li>"
			+ "</form>";

	private static final String EXPORT_SECTION_CONTENT = "<form><p>Options available to export this application to supported platforms:</p>"
			+ "<li style=\"image\" value=\"export\" bindent=\"5\">Export <a href=\"export.app\">Mobile application(s)</a> to distribute</li>"
			+ "</form>";

	private DataBindingContext m_bindingContext;
	
	private AvailableCordovaEnginesSection engineSection;
	private FormToolkit formToolkit;
	private Text txtIdtxt;
	private Text txtAuthorname;
	private Text txtDescription;
	private Text txtName;
	private Text txtEmail;
	private Text txtUrl;
	private Text txtVersion;
	private Text txtContentsource;
	
	private PropertyChangeListener engineListener;
	private PropertyChangeListener contentListener;
	private PropertyChangeListener authorListener;

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
	
	private WidgetModel getWidgetModel() {
		return ((ConfigEditor)getEditor()).getWidgetModel();
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
		Composite body = managedForm.getForm().getBody();
		body.setLayout(FormUtils.createFormTableWrapLayout(2));
		
		Composite left = formToolkit.createComposite(body);
		left.setLayout(FormUtils.createFormPaneTableWrapLayout(1));
		left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		
		Composite right = formToolkit.createComposite(body);
		right.setLayout(FormUtils.createFormPaneTableWrapLayout(1));
		right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		
		createNameDescriptionSection(left);
		createAuthorSection(left);
		createExportSection(left);
		
		createPluginsSection(right);
		createEngineSection(right);
		
		m_bindingContext = initDataBindings();
		bindAuthor(m_bindingContext); // binding separately is necessary to be able to work with WindowBuilder
		bindContent(m_bindingContext);
		
	}

	private void createPluginsSection(Composite parent){
		Section sctnPlugins = createSection(parent, "Plug-ins");
		sctnPlugins.setLayout(FormUtils.createClearTableWrapLayout(false, 1));
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		sctnPlugins.setLayoutData(data);
		
		FormText text = formToolkit.createFormText(sctnPlugins, true);
		ImageDescriptor idesc = HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID,"/icons/etool16/cordovaplug_wiz.png");
		text.setImage("plugin", idesc.createImage());

		text.setText(PLUGINS_SECTION_CONTENT, true, false);
		
		sctnPlugins.setClient(text);
		text.addHyperlinkListener(this);
		
	}
	
	private void createExportSection(Composite parent) {
		Section sctnExport = createSection(parent, "Export");
		sctnExport.setLayout(FormUtils.createClearTableWrapLayout(false, 1));
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		sctnExport.setLayoutData(data);
		FormText text = formToolkit.createFormText(sctnExport, true);
		ImageDescriptor idesc = HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, "/icons/etool16/export_wiz.png");
		text.setImage("export", idesc.createImage());
		text.setText(EXPORT_SECTION_CONTENT, true, false);
		
		sctnExport.setClient(text);
		text.addHyperlinkListener(this);
	}

	private void createAuthorSection(Composite parent) {
		Section sctnAuthor = createSection(parent, "Author");
		sctnAuthor.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		
		Composite composite = formToolkit.createComposite(sctnAuthor, SWT.WRAP);
		formToolkit.paintBordersFor(composite);
		sctnAuthor.setClient(composite);
		composite.setLayout(FormUtils.createSectionClientGridLayout(false, 2));
		
		createFormFieldLabel(composite, "Name:");
	
		txtAuthorname = formToolkit.createText(composite, "", SWT.WRAP);
		txtAuthorname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		createFormFieldLabel(composite, "Email:");
		
		txtEmail = formToolkit.createText(composite, "", SWT.NONE);
		txtEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		createFormFieldLabel(composite, "URL:");
		
		txtUrl = formToolkit.createText(composite, "", SWT.NONE);
		txtUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	private Label createFormFieldLabel(final Composite composite, final String labelText) {
		Label label = formToolkit.createLabel(composite, labelText, SWT.NONE);
		PixelConverter converter = new PixelConverter(label);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		widthHint = Math.max(widthHint, label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		GridDataFactory.swtDefaults().hint(widthHint, SWT.DEFAULT).applyTo(label);
		return label;
	}

	private void createNameDescriptionSection(Composite parent) {
		Section sctnNameAndDescription = createSection(parent, "Name and Description");
		sctnNameAndDescription.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		
		Composite container = formToolkit.createComposite(sctnNameAndDescription, SWT.WRAP);
		formToolkit.paintBordersFor(container);
		sctnNameAndDescription.setClient(container);
		container.setLayout(FormUtils.createSectionClientGridLayout(false, 2));
		
		GridData textGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		
		createFormFieldLabel(container, "ID:");
		
		txtIdtxt = formToolkit.createText(container, "", SWT.NONE);
		txtIdtxt.setLayoutData(textGridData);
		
		createFormFieldLabel(container, "Name:");
		
		txtName = formToolkit.createText(container, "", SWT.NONE);
		GridDataFactory.createFrom(textGridData).applyTo(txtName);
		
		createFormFieldLabel(container, "Version:");
		
		txtVersion = formToolkit.createText(container, "", SWT.NONE);
		GridDataFactory.createFrom(textGridData).applyTo(txtVersion);
		
		createFormFieldLabel(container, "Description:");
		
		txtDescription = formToolkit.createText(container, "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.createFrom(textGridData).hint(SWT.DEFAULT, 100).applyTo(txtDescription);
		
		createFormFieldLabel(container, "Content Source:");
		
		txtContentsource = formToolkit.createText(container, "", SWT.NONE);
		GridDataFactory.createFrom(textGridData).applyTo(txtContentsource);
	}
	
	private void createEngineSection(Composite parent) {
		Section sctnEngines = createSection(parent, "Hybrid Mobile Engine");
		sctnEngines.setLayout(FormUtils.createClearTableWrapLayout(false, 1));
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		sctnEngines.setLayoutData(data);

		Composite container = formToolkit.createComposite(sctnEngines, SWT.WRAP);
		sctnEngines.setClient(container);
		container.setLayout(FormUtils.createSectionClientGridLayout(false, 1));

		engineSection = new AvailableCordovaEnginesSection();
		engineSection.createControl(container);

		engineListener = new PropertyChangeListener() {
			// We can't use updateActiveEngines() here because config.xml changes before
			// the active engine in the HybridProject -- the change fires while active
			// engine the old one.
			@Override
			public void propertyChange(PropertyChangeEvent ev) {
				Display.getDefault().asyncExec(new Runnable () {

					@Override
					public void run() {
						// The PropertyChangeEvent can fire before engines are actually
						// updated, so we have to rely on the Widget for active engines
						// rather than call HybridMobileEngineManager.getActiveEngines()
						getEnginesFromWidget();
					}
				});
			}
		};

		getWidget().addPropertyChangeListener("engines", engineListener);
		
		updateActiveEngines();

		engineSection.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection newSelection =
						(IStructuredSelection) event.getSelection();
				WidgetModel model = getWidgetModel();
				Widget w = getWidget();
				// Null check is required since w.getEngines() will return null
				// when there are no engine tags in config.xml.
				if (w.getEngines() != null){
					
					if (newSelection == null || newSelection.isEmpty()) {
						for (Engine e : w.getEngines()) {
							w.removeEngine(e);
						}
					} else {
						for (Iterator<?> iter = newSelection.iterator(); iter.hasNext(); ) {
							HybridMobileEngine hybridEngine = (HybridMobileEngine) iter.next();
							Engine definedEngine = engineIsDefined(w, hybridEngine.getName());
							boolean engineRemoved = false;
							if(definedEngine != null){
								if(!definedEngine.getSpec().equals(hybridEngine.getSpec())){
									w.removeEngine(definedEngine);
									engineRemoved = true;
								}
							}
							if(engineRemoved){
								Engine engine = model.createEngine(w);
								engine.setName(hybridEngine.getName());
								engine.setSpec(hybridEngine.getSpec());
								w.addEngine(engine);
							}
							
						}
					}
				} else {
					for (Iterator<?> iter = newSelection.iterator(); iter.hasNext(); ) {
						HybridMobileEngine hybridEngine = (HybridMobileEngine) iter.next();
						Engine engine = model.createEngine(w);
						engine.setName(hybridEngine.getName());
						engine.setSpec(hybridEngine.getSpec());
						w.addEngine(engine);
					}
				}
			}
		});
	}
	
	private Engine engineIsDefined(Widget widget, String platformId){
		for(Engine e : widget.getEngines()) {
			if(e.getName().equals(platformId)){
				return e;
			}
		}
		return null;
	}

	private void getEnginesFromWidget() {
		List<Engine> widgetEngines = getWidget().getEngines();
		// getEngines() can return null; property change fires when engine is removed
		if (widgetEngines == null || widgetEngines.size() == 0) {
			engineSection.setSelection(new StructuredSelection());
			return;
		}
		Set<HybridMobileEngine> engines = new HashSet<HybridMobileEngine>();
		CordovaEngineProvider enginesProvider = CordovaEngineProvider.getInstance();
		Set<HybridMobileEngine> availableEngines = enginesProvider.getAvailableEngines();
		for(Engine widgetEngine : widgetEngines){
			HybridMobileEngine e = enginesProvider.createEngine(widgetEngine.getName(), widgetEngine.getSpec());
			if(e.isValid()){
				engines.add(e);
				if(!availableEngines.contains(e)){
					enginesProvider.engineFound(e);
				}
			}
		}
		if (engines.size() != 0) {
			engineSection.updateAvailableEngines(null);
			engineSection.setSelection(new StructuredSelection(engines.toArray()));
		}
	}

	private void updateActiveEngines() {
		IFile file = (IFile) getEditor().getEditorInput().getAdapter(IFile.class);
		HybridProject project = HybridProject.getHybridProject(file.getProject());
		HybridMobileEngine[] activeEngines = project.getEngineManager().getEngines();
		if (activeEngines != null) {
			engineSection.updateAvailableEngines(new HashSet<>(Arrays.asList(activeEngines)));
			engineSection.setSelection(new StructuredSelection(activeEngines));
		}
	}

	private Section createSection(Composite parent, String text){
		Section sctn = formToolkit.createSection(parent, Section.TITLE_BAR);
		sctn.clientVerticalSpacing = FormUtils.SECTION_HEADER_VERTICAL_SPACING;
		sctn.setText(text);
		return sctn;
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
			authorListener = new PropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					value.getRealm().exec(new Runnable() {
						@Override
						public void run() {
							if (evt.getNewValue() == null) {
								value.setValue(new DummyAuthor());
							} else {
								value.setValue(evt.getNewValue());
							}
						}
					});
				}
			};
			getWidget().addPropertyChangeListener("author", authorListener);
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
			contentListener = new PropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					value.getRealm().exec(new Runnable() {
						@Override
						public void run() {
							if (evt.getNewValue() == null) {
								value.setValue(new DummyContent());
							} else {
								value.setValue(evt.getNewValue());
							}
						}
					});
				}
			};
			getWidget().addPropertyChangeListener("content", contentListener);
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

	@Override
	public void linkActivated(HyperlinkEvent e) {
		String href = (String) e.getHref();
		Action action = null;
		if("export.app".equals(href)){
			action = new NativeArtifactExportAction(getConfigEditor());
		}
		else
		if("plugin.folder".equals(href)){
			action = new LaunchCordovaPluginWizardAction(getConfigEditor(), CordovaPluginSelectionPage.PLUGIN_SOURCE_DIRECTORY);
		}
		else
		if("plugin.git".equals(href)){
			action = new LaunchCordovaPluginWizardAction(getConfigEditor(), CordovaPluginSelectionPage.PLUGIN_SOURCE_GIT);
		}
		else
		if("plugin.registry".equals(href)){
			action = new LaunchCordovaPluginWizardAction(getConfigEditor(), CordovaPluginSelectionPage.PLUGIN_SOURCE_REGISTRY);
		}
		Assert.isNotNull(action);
		action.run();
		
	}
	
	@Override
	public void linkEntered(HyperlinkEvent e) {
	}

	@Override
	public void linkExited(HyperlinkEvent e) {
	}

	public void dispose() {
		Widget w = getWidget();
		w.removePropertyChangeListener(engineListener);
		w.removePropertyChangeListener(authorListener);
		w.removePropertyChangeListener(contentListener);
		super.dispose();
	}
}
