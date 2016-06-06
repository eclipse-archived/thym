/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc. 
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Access;
import org.eclipse.thym.core.config.Feature;
import org.eclipse.thym.core.config.Plugin;
import org.eclipse.thym.core.config.Preference;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.plugins.internal.LaunchCordovaPluginWizardAction;
import org.eclipse.thym.ui.plugins.internal.PluginUninstallAction;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.xml.core.internal.cleanup.CleanupProcessorXML;

public class PropertiesPage extends AbstactConfigEditorPage{
	
	private static final String BTN_LBL_REMOVE = "Remove";
	private static final String BTN_LBL_ADD = "Add...";

	private DataBindingContext m_bindingContext;

	private FormToolkit formToolkit;
	private Table preferencesTable;
	private Table accessTable;
	private TableViewer preferencesViewer;
	private TableViewer accessViewer;
	private TableViewer featuresTableViewer;
	private Table featuresTable;
	private Table paramsTable;

	private TableViewer featureParamsTableViewer;
	
	public PropertiesPage(FormEditor editor) {
		super(editor, "properties", "Platform Properties");
		formToolkit = editor.getToolkit();
	}
	
	private Widget getWidget(){
		return ((ConfigEditor)getEditor()).getWidget();
	}
	
	private WidgetModel getWidgetModel(){
		return ((ConfigEditor)getEditor()).getWidgetModel();
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		final ScrolledForm form = managedForm.getForm();
		
		formToolkit.decorateFormHeading( form.getForm());
		managedForm.getForm().setText(getTitle());
	
		GridLayout formGridLayout = new GridLayout();
		formGridLayout.horizontalSpacing = FormUtils.FORM_BODY_HORIZONTAL_SPACING;
		formGridLayout.verticalSpacing = FormUtils.FORM_BODY_VERTICAL_SPACING;
		formGridLayout.marginBottom = FormUtils.FORM_BODY_MARGIN_BOTTOM;
		formGridLayout.marginTop = FormUtils.FORM_BODY_MARGIN_TOP;
		formGridLayout.marginRight = FormUtils.FORM_BODY_MARGIN_RIGHT;
		formGridLayout.marginLeft = FormUtils.FORM_BODY_MARGIN_LEFT;
		formGridLayout.marginWidth =FormUtils.FORM_BODY_MARGIN_WIDTH;
		formGridLayout.marginHeight = FormUtils.FORM_BODY_MARGIN_HEIGHT;
		formGridLayout.makeColumnsEqualWidth = true;
		formGridLayout.numColumns = 2;
		Composite body = managedForm.getForm().getBody();
		body.setLayout(formGridLayout);
		
		Composite left, right;
		left = formToolkit.createComposite(body);
		left.setLayout(FormUtils.createFormPaneGridLayout(false, 1));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(left);;
		right = formToolkit.createComposite(body);
		right.setLayout(FormUtils.createFormPaneGridLayout(false, 1));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(right);
		
		
		createFeaturesSection(left);
		createFeatureParamsSection(right);
		createPreferencesSection(left);
		createAccessSection(right);
		
		m_bindingContext = initDataBindings();
		
		selectFirstFeature();
		
	}

	private void createAccessSection(Composite right) {
		Section sctnAccess = formToolkit.createSection(right, Section.TITLE_BAR);
		GridDataFactory.fillDefaults().grab(true,true).applyTo(sctnAccess);
		formToolkit.paintBordersFor(sctnAccess);
		sctnAccess.setText("Access");
		
		Composite compositea = formToolkit.createComposite(sctnAccess, SWT.WRAP);
		formToolkit.paintBordersFor(compositea);
		sctnAccess.setClient(compositea);
		compositea.setLayout(FormUtils.createSectionClientGridLayout(false, 2));
		
		accessViewer = new TableViewer(compositea, SWT.BORDER | SWT.FULL_SELECTION);
		accessTable = accessViewer.getTable();
		accessTable.setLinesVisible(true);
		accessTable.setHeaderVisible(true);
		formToolkit.paintBordersFor(accessTable);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(accessTable);
		
		TableViewerColumn tableViewerColumnOrigin = new TableViewerColumn(accessViewer, SWT.NONE);
		TableColumn tblclmnOrigin = tableViewerColumnOrigin.getColumn();
		tblclmnOrigin.setWidth(100);
		tblclmnOrigin.setText("origin");
		
		TableViewerColumn tableViewerColumnSubdomains = new TableViewerColumn(accessViewer, SWT.NONE);
		TableColumn tblclmnSubdomains = tableViewerColumnSubdomains.getColumn();
		tblclmnSubdomains.setWidth(100);
		tblclmnSubdomains.setText("subdomains");
		
		TableViewerColumn tableViewerColumnBrowserOnly = new TableViewerColumn(accessViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tableViewerColumnBrowserOnly.getColumn();
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("browserOnly");
		
		Composite buttonComposite = formToolkit.createComposite(compositea, SWT.NONE);
		formToolkit.paintBordersFor(buttonComposite);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttonComposite);

		Button btnAccessAdd = createButton(buttonComposite, BTN_LBL_ADD);
		btnAccessAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewAccessDialog dialog = new NewAccessDialog(getSite().getShell(), getWidgetModel());
				if(dialog.open() == Window.OK && dialog.getAccess() != null){
					getWidget().addAccess(dialog.getAccess());
					reformatDocument();
				}
			}
		});
		
		Button btnAccessRemove = createButton(buttonComposite, BTN_LBL_REMOVE);
		btnAccessRemove.setEnabled(false);
		btnAccessRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)accessViewer.getSelection();
				if(selection.isEmpty())
					return;
				Access access = (Access)selection.getFirstElement();
				getWidget().removeAccess(access);
			}
		});
		accessViewer.addSelectionChangedListener(new ButtonStateUpdater( btnAccessRemove ));
	}

	private void createPreferencesSection(Composite left) {
		Section sctnPreferences = formToolkit.createSection(left, Section.TITLE_BAR);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sctnPreferences);
		formToolkit.paintBordersFor(sctnPreferences);
		sctnPreferences.setText("Preferences");
		
		Composite composite = formToolkit.createComposite(sctnPreferences, SWT.WRAP);
		formToolkit.paintBordersFor(composite);
		sctnPreferences.setClient(composite);
		composite.setLayout(FormUtils.createSectionClientGridLayout(false, 2));
		
		preferencesViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		preferencesTable = preferencesViewer.getTable();
		preferencesTable.setLinesVisible(true);
		preferencesTable.setHeaderVisible(true);
		formToolkit.paintBordersFor(preferencesTable);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(preferencesTable);
		
		TableViewerColumn tableViewerColumnName = new TableViewerColumn(preferencesViewer, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumnName.getColumn();
		tblclmnName.setWidth(100);
		tblclmnName.setText("name");
		
		TableViewerColumn tableViewerColumnValue = new TableViewerColumn(preferencesViewer, SWT.NONE);
		TableColumn tblclmnValue = tableViewerColumnValue.getColumn();
		tblclmnValue.setWidth(100);
		tblclmnValue.setText("value");
		
		Composite buttonComposite = formToolkit.createComposite(composite, SWT.NONE);
		formToolkit.paintBordersFor(buttonComposite);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttonComposite);


		Button btnPreferenceAdd = createButton(buttonComposite, BTN_LBL_ADD);
		btnPreferenceAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				NewNameValueDialog dialog = new NewNameValueDialog(getSite().getShell(),"New Preference");
				if (dialog.open() == Window.OK ){
					Preference pref = getWidgetModel().createPreference(getWidget());
					pref.setName(dialog.getName());
					pref.setValue(dialog.getValue());
					getWidget().addPreference(pref);
					reformatDocument();
				}
			}
		});
				
		Button btnPreferenceRemove = createButton(buttonComposite, BTN_LBL_REMOVE);
		btnPreferenceRemove.setEnabled(false);
		btnPreferenceRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)preferencesViewer.getSelection();
				if(selection.isEmpty() )
					return;
				Preference preference = (Preference)selection.getFirstElement();
				getWidget().removePreference(preference);
			}
		});
		preferencesViewer.addSelectionChangedListener(new ButtonStateUpdater(btnPreferenceRemove));
	}

	private void createFeatureParamsSection(Composite right) {
		// Params section 
		
		Section sctnParams = formToolkit.createSection(right, Section.TITLE_BAR| Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true,true).applyTo(sctnParams);
		formToolkit.paintBordersFor(sctnParams);
		sctnParams.setText("Params");
		sctnParams.setDescription("Specify parameters for the selected plug-in");
		
		Composite paramsComposite = formToolkit.createComposite(sctnParams, SWT.NONE);
		formToolkit.paintBordersFor(paramsComposite);
		sctnParams.setClient(paramsComposite);
		paramsComposite.setLayout(FormUtils.createSectionClientGridLayout(false, 2));

		

		featureParamsTableViewer = new TableViewer(paramsComposite, SWT.BORDER | SWT.FULL_SELECTION);
		featuresTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if( !(event.getSelection() instanceof IStructuredSelection)) return;
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				if(sel.isEmpty()) {
					featureParamsTableViewer.setInput(null);
					return;
				}
//				Feature feature = (Feature)sel.getFirstElement();
//				featureParamsTableViewer.setInput(feature.getParams());
//				feature.addPropertyChangeListener("param", new PropertyChangeListener() {
//
//					@Override
//					public void propertyChange(PropertyChangeEvent event) {
//						featureParamsTableViewer.setInput(event.getNewValue());
//					}
//				});
			}
		});
		featureParamsTableViewer.setContentProvider(new IStructuredContentProvider() {
			private	Map<String, String> items;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				this.items =(Map)newInput;
			}
			@Override
			public void dispose() {
			}
			@Override
			public Object[] getElements(Object inputElement) {
				if(items == null ){
					return new Object[0];
				}
				return items.entrySet().toArray();
			}
		});

		paramsTable = featureParamsTableViewer.getTable();
		paramsTable.setLinesVisible(true);
		paramsTable.setHeaderVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(paramsTable);;
		formToolkit.paintBordersFor(paramsTable);

		TableViewerColumn paramTableColumnViewer = new TableViewerColumn(featureParamsTableViewer, SWT.NONE);
		TableColumn tblclmnParamName = paramTableColumnViewer.getColumn();
		tblclmnParamName.setWidth(100);
		tblclmnParamName.setText("name");
		paramTableColumnViewer.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getKey();
			}
		});

		paramTableColumnViewer = new TableViewerColumn(featureParamsTableViewer, SWT.NONE);
		TableColumn tblclmnValueColumn = paramTableColumnViewer.getColumn();
		tblclmnValueColumn.setWidth(200);
		tblclmnValueColumn.setText("value");
		paramTableColumnViewer.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getValue();
			}
		});

		Composite featureParamBtnsComposite = new Composite(paramsComposite, SWT.NONE);
		formToolkit.adapt(featureParamBtnsComposite);
		formToolkit.paintBordersFor(featureParamBtnsComposite);
		featureParamBtnsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(featureParamBtnsComposite);


		Button btnAdd = createButton(featureParamBtnsComposite, BTN_LBL_ADD);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) featuresTableViewer.getSelection();
				if(sel.isEmpty())//should not happen as we always have a selection
					return;
				NewNameValueDialog dialog = new NewNameValueDialog(getSite().getShell(),"New Parameter");
				if (dialog.open() == Window.OK ){
					Feature selectedFeature = (Feature) sel.getFirstElement();
					selectedFeature.addParam(dialog.getName(), dialog.getValue());
					reformatDocument();
				}
			}
		});
		this.featuresTableViewer.addSelectionChangedListener(new ButtonStateUpdater(btnAdd));
		
		Button btnRemove = createButton(featureParamBtnsComposite, BTN_LBL_REMOVE);
		btnRemove.setEnabled(false);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) featuresTableViewer.getSelection();
				if(sel.isEmpty())//should not happen as we always have a selection
					return;
				Feature selectedFeature = (Feature) sel.getFirstElement();
				sel = (IStructuredSelection)featureParamsTableViewer.getSelection();
				Entry<String,String > param = (Entry<String, String>) sel.getFirstElement();
				selectedFeature.removeParam(param.getKey());
			}
		});
		featureParamsTableViewer.addSelectionChangedListener(new ButtonStateUpdater(
				btnRemove));
	}

	private void createFeaturesSection(Composite left) {
		Section sctnFeatures = formToolkit.createSection(left, Section.TITLE_BAR | Section.DESCRIPTION);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sctnFeatures);
		formToolkit.paintBordersFor(sctnFeatures);
		sctnFeatures.setText("Plugins");
		sctnFeatures.setDescription("Define plug-ins to be used in this application");
		
		Composite featuresComposite = formToolkit.createComposite(sctnFeatures, SWT.NONE);
		formToolkit.paintBordersFor(featuresComposite);
		sctnFeatures.setClient(featuresComposite);
		featuresComposite.setLayout(FormUtils.createSectionClientGridLayout( false,2));
		
		featuresTableViewer = new TableViewer(featuresComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		featuresTable = featuresTableViewer.getTable();
		featuresTable.setLinesVisible(true);
		featuresTable.setHeaderVisible(true);
		GridData featureTableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		
		featuresTable.setLayoutData(featureTableLayoutData);
		formToolkit.paintBordersFor(featuresTable);

		TableViewerColumn tableViewerColumnName = new TableViewerColumn(featuresTableViewer, SWT.NONE);
		TableColumn tblclmnFeatureName = tableViewerColumnName.getColumn();
		tblclmnFeatureName.setWidth(250);
		tblclmnFeatureName.setText("name");

		TableViewerColumn tableViewerColumnSpec= new TableViewerColumn(featuresTableViewer, SWT.NONE);
		TableColumn tblclmnFeatureSpec = tableViewerColumnSpec.getColumn();
		tblclmnFeatureSpec.setWidth(200);
		tblclmnFeatureSpec.setText("spec");

		Composite featureBtnsComposite= formToolkit.createComposite(featuresComposite, SWT.NONE);
		formToolkit.paintBordersFor(featureBtnsComposite);
		featureBtnsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(featureBtnsComposite);

		Button btnFeatureAdd = createButton(featureBtnsComposite, BTN_LBL_ADD);
		btnFeatureAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	

				LaunchCordovaPluginWizardAction action = new LaunchCordovaPluginWizardAction(getConfigEditor());
				action.run();
				selectFirstFeature();
			}
		});

		Button btnFeatureRemove = createButton(featureBtnsComposite, BTN_LBL_REMOVE);
		btnFeatureRemove.setEnabled(false);
		btnFeatureRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) featuresTableViewer
						.getSelection();
				if (selection.isEmpty())
					return;
				Plugin feature = (Plugin) selection.getFirstElement();
				IResource resource = (IResource) getEditorInput().getAdapter(IResource.class);
				HybridProject prj = HybridProject.getHybridProject(resource.getProject());
				boolean pluginFoundAndRemoved = false;
				try {
					List<CordovaPlugin> plugins = prj.getPluginManager().getInstalledPlugins();
					for (CordovaPlugin cordovaPlugin : plugins) {
						if (cordovaPlugin.getId() != null && cordovaPlugin.getId().equals(feature.getName())) {
							PluginUninstallAction action = new PluginUninstallAction(
									cordovaPlugin);
							action.run();
							pluginFoundAndRemoved = true;
							break;
						}
					}
				} catch (CoreException ex) {
					HybridUI.log(IStatus.ERROR, "Error removing the installed plugin", ex);
				}
				if(!pluginFoundAndRemoved){
					getWidget().removePlugin(feature);
					featureParamsTableViewer.setInput(null);
				}
				selectFirstFeature();
			}
		});
		
		featuresTableViewer.addSelectionChangedListener(new ButtonStateUpdater(
				btnFeatureRemove));
	}

	private Button createButton(Composite parent, String label) {
		Button button = formToolkit.createButton( parent, label, SWT.NULL);
		button.setFont(JFaceResources.getDialogFont());
		
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).hint(widthHint, SWT.DEFAULT).applyTo(button);

		return button;
	}

	private void selectFirstFeature() {
		TableItem[] items = this.featuresTable.getItems();
		if(items.length >0 ){
			Object obj =items[0].getData();
			featuresTableViewer.setSelection(new StructuredSelection(obj));
		}
	}
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
		IObservableMap[] observeMaps = BeansObservables.observeMaps(listContentProvider.getKnownElements(), Preference.class, new String[]{"name", "value"});
		preferencesViewer.setLabelProvider(new ObservableMapLabelProvider(observeMaps));
		preferencesViewer.setContentProvider(listContentProvider);
		//
		IObservableList preferencesGetWidgetObserveList = BeanProperties.list("preferences").observe(getWidget());
		preferencesViewer.setInput(preferencesGetWidgetObserveList);
		//
		ObservableListContentProvider listContentProvider_1 = new ObservableListContentProvider();
		IObservableMap[] observeMaps_1 = BeansObservables.observeMaps(listContentProvider_1.getKnownElements(), Access.class, new String[]{"origin", "subdomains", "browserOnly"});
		accessViewer.setLabelProvider(new ObservableMapLabelProvider(observeMaps_1));
		accessViewer.setContentProvider(listContentProvider_1);
		//
		IObservableList accessesGetWidgetObserveList = BeanProperties.list("accesses").observe(getWidget());
		accessViewer.setInput(accessesGetWidgetObserveList);
		//
		ObservableListContentProvider listContentProvider_2 = new ObservableListContentProvider();
		IObservableMap[] observeMapsPlugin = BeansObservables.observeMaps(listContentProvider_2.getKnownElements(), Plugin.class, new String[]{"name", "spec"});
		featuresTableViewer.setLabelProvider(new ObservableMapLabelProvider(observeMapsPlugin));
		featuresTableViewer.setContentProvider(listContentProvider_2);
		//
		IObservableList featuresGetWidgetObserveList = BeanProperties.list("plugins").observe(getWidget());
		featuresTableViewer.setInput(featuresGetWidgetObserveList);
		//
		return bindingContext;
	}

	private void reformatDocument() {
		CleanupProcessorXML formatter = new CleanupProcessorXML();
		formatter.cleanupModel(getWidgetModel().underLyingModel);
	}

	private final class ButtonStateUpdater implements ISelectionChangedListener{
		private Button btn;
		public ButtonStateUpdater(final Button btn) {
			this.btn = btn;
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			btn.setEnabled(!event.getSelection().isEmpty());
		}
	}

}
