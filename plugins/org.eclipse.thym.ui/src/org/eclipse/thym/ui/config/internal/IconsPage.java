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

import java.util.Arrays;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.core.config.Icon;
import org.eclipse.thym.core.config.Splash;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class IconsPage extends FormPage {
	private DataBindingContext m_bindingContext;

	private FormToolkit formToolkit; 
	private Table iconsTable;
	private Table splashTable;
	private Text txtWidth;
	private Text txtHeight;
	private Text txtPlatform;
	private Text txtDensity;
	private TableViewer iconsTableViewer;
	private TableViewer splashTableViewer;
	private Text txtSplshWidth;
	private Text txtSplshHeight;
	private Text txtSplshPlatform;
	private Text txtSplshDensity;
	
	
	public IconsPage(FormEditor editor) {
		super(editor, "icons", "Icons && Splash Screen");
		formToolkit = editor.getToolkit();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {	
		final ScrolledForm form = managedForm.getForm();
		formToolkit.decorateFormHeading( form.getForm());
		managedForm.getForm().setText(getTitle());
		{
			TableWrapLayout tableWrapLayout = new TableWrapLayout();
			tableWrapLayout.makeColumnsEqualWidth = true;
			tableWrapLayout.numColumns = 1;
			managedForm.getForm().getBody().setLayout(tableWrapLayout);
		}
		
		Section sctnIcons = managedForm.getToolkit().createSection(managedForm.getForm().getBody(), Section.TITLE_BAR);
		TableWrapData twd_sctnIcons = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP, 1, 1);
		twd_sctnIcons.grabHorizontal = true;
		sctnIcons.setLayoutData(twd_sctnIcons);
		managedForm.getToolkit().paintBordersFor(sctnIcons);
		sctnIcons.setText("Icons");
		
		Composite composite = managedForm.getToolkit().createComposite(sctnIcons, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite);
		sctnIcons.setClient(composite);
		composite.setLayout(new GridLayout(3, false));
		
		iconsTableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);

		iconsTable = iconsTableViewer.getTable();
		
		iconsTable.setHeaderVisible(true);
		GridData gd_iconsTable = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_iconsTable.heightHint = 125;
		iconsTable.setLayoutData(gd_iconsTable);
		managedForm.getToolkit().paintBordersFor(iconsTable);
		
		Composite composite_3 = managedForm.getToolkit().createComposite(composite, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		managedForm.getToolkit().paintBordersFor(composite_3);
		composite_3.setLayout(new FillLayout(SWT.VERTICAL));
		
		Button btnIconAdd = managedForm.getToolkit().createButton(composite_3, "Add...", SWT.NONE);
		btnIconAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String src = getImageSrc();
				ConfigEditor editor = (ConfigEditor) getEditor();
				Icon icon = editor.getWidgetModel().createIcon(getWidget());
				icon.setSrc(src);
				getWidget().addIcon(icon);
			}
		});
		
		Button btnIconRemove = managedForm.getToolkit().createButton(composite_3, "Remove", SWT.NONE);
		
		Group iconDetailGrp = new Group(composite, SWT.NONE);
		iconDetailGrp.setText("Icon Image Details");
		iconDetailGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		managedForm.getToolkit().adapt(iconDetailGrp);
		managedForm.getToolkit().paintBordersFor(iconDetailGrp);
		iconDetailGrp.setLayout(new GridLayout(1, false));
		
		Composite imageDetailComposite = managedForm.getToolkit().createComposite(iconDetailGrp, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(imageDetailComposite);
		imageDetailComposite.setLayout(new GridLayout(2, false));
		
		Label lblWidth = managedForm.getToolkit().createLabel(imageDetailComposite, "Width:", SWT.NONE);
		
		txtWidth = managedForm.getToolkit().createText(imageDetailComposite, "New Text", SWT.NONE);
		txtWidth.setText("");
		txtWidth.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label lblHeight = managedForm.getToolkit().createLabel(imageDetailComposite, "Height:", SWT.NONE);
		
		txtHeight = managedForm.getToolkit().createText(imageDetailComposite, "New Text", SWT.NONE);
		txtHeight.setText("");
		txtHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPlatform = managedForm.getToolkit().createLabel(imageDetailComposite, "Platform:", SWT.NONE);
		
		txtPlatform = managedForm.getToolkit().createText(imageDetailComposite, "New Text", SWT.NONE);
		txtPlatform.setText("");
		txtPlatform.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblDensity = managedForm.getToolkit().createLabel(imageDetailComposite, "Density:", SWT.NONE);
		
		txtDensity = managedForm.getToolkit().createText(imageDetailComposite, "New Text", SWT.NONE);
		txtDensity.setText("");
		txtDensity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnIconRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)iconsTableViewer.getSelection();
				if(selection.isEmpty())
					return;
				Icon icon = (Icon)selection.getFirstElement();
				getWidget().removeIcon(icon);
			}
		});
		
		Section sctnSplashes = managedForm.getToolkit().createSection(managedForm.getForm().getBody(), Section.TITLE_BAR);
		sctnSplashes.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP, 1, 1));
		managedForm.getToolkit().paintBordersFor(sctnSplashes);
		sctnSplashes.setText("Splash Screens");
		
		Composite composite_1 = managedForm.getToolkit().createComposite(sctnSplashes, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite_1);
		sctnSplashes.setClient(composite_1);
		composite_1.setLayout(new GridLayout(3, false));
		
		splashTableViewer = new TableViewer(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
		splashTable = splashTableViewer.getTable();

		splashTable.setHeaderVisible(true);
		GridData gd_splashTable = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_splashTable.heightHint = 125;
		splashTable.setLayoutData(gd_splashTable);
		managedForm.getToolkit().paintBordersFor(splashTable);
		
		Composite composite_4 = managedForm.getToolkit().createComposite(composite_1, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		managedForm.getToolkit().paintBordersFor(composite_4);
		composite_4.setLayout(new FillLayout(SWT.VERTICAL));
		
		Button btnSplashAdd = managedForm.getToolkit().createButton(composite_4, "Add...", SWT.NONE);
		btnSplashAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String src = getImageSrc();
				Splash splash = ((ConfigEditor) getEditor()).getWidgetModel().createSplash(getWidget());
				splash.setSrc(src);
				getWidget().addSplash(splash);
			}
		});
		
		Button btnSplashRemove = managedForm.getToolkit().createButton(composite_4, "Remove", SWT.NONE);
		Group splashDetailGrp = new Group(composite_1, SWT.NONE);
		splashDetailGrp.setText("Splash Image Details");
		managedForm.getToolkit().adapt(splashDetailGrp);
		managedForm.getToolkit().paintBordersFor(splashDetailGrp);
		splashDetailGrp.setLayout(new GridLayout(1, false));
		
		Composite splashDetailComposite = managedForm.getToolkit().createComposite(splashDetailGrp, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(splashDetailComposite);
		splashDetailComposite.setLayout(new GridLayout(2, false));
		
		Label lblSplshWidth = managedForm.getToolkit().createLabel(splashDetailComposite, "Width:", SWT.NONE);
		
		txtSplshWidth = managedForm.getToolkit().createText(splashDetailComposite, "New Text", SWT.NONE);
		
		txtSplshWidth.setText("");
		txtSplshWidth.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label lblSplshHeight = managedForm.getToolkit().createLabel(splashDetailComposite, "Height:", SWT.NONE);
		
		txtSplshHeight = managedForm.getToolkit().createText(splashDetailComposite, "New Text", SWT.NONE);
		txtSplshHeight.setText("");
		txtSplshHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblSplshPlatform = managedForm.getToolkit().createLabel(splashDetailComposite, "Platform:", SWT.NONE);
		
		txtSplshPlatform = managedForm.getToolkit().createText(splashDetailComposite, "New Text", SWT.NONE);
		txtSplshPlatform.setText("");
		txtSplshPlatform.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblSplshDensity = managedForm.getToolkit().createLabel(splashDetailComposite, "Density:", SWT.NONE);
		
		txtSplshDensity = managedForm.getToolkit().createText(splashDetailComposite, "New Text", SWT.NONE);
		txtSplshDensity.setText("");
		txtSplshDensity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnSplashRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)splashTableViewer.getSelection();
				if(selection.isEmpty())
					return;
				Splash splash = (Splash)selection.getFirstElement();
				getWidget().removeSplash(splash);
			}
		});


		
		m_bindingContext = initDataBindings();
	}

	private String getImageSrc(){
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getSite().getShell(),
				new WorkbenchLabelProvider(),
				new WorkbenchContentProvider());
		dialog.setTitle("Choose image");
		IProject currentProject = getProject();
		dialog.setInput(currentProject.getFolder("www"));
		dialog.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
			IResource resource = (IResource) element;
			return resource.getType() == IResource.FOLDER || 
					Arrays.binarySearch(WidgetModel.ICON_EXTENSIONS,
							resource.getFileExtension()) >= 0 ;
			}
		});
		
		dialog.setValidator(new ISelectionStatusValidator() {
			
		    @Override
		    public IStatus validate(Object[] selection) {

			if (selection.length == 0 || selection.length > 1) {
			    return new Status(IStatus.ERROR,
				    HybridUI.PLUGIN_ID, "Must have selection");
			}
			IResource resource = (IResource) selection[0];
			if (resource.getType() == IResource.FOLDER) {
			    return new Status(IStatus.ERROR,
				    HybridUI.PLUGIN_ID, "Can not select folder");
			    }
			return Status.OK_STATUS;
		    }
		});
		
		if(dialog.open() == Window.OK){
		    IResource resource = (IResource) dialog.getFirstResult();
			String src = resource.getProjectRelativePath().toString()
				    .substring("www".length() + 1);
			return src;
			
		}
		return null;
		
		
	}
	
	private IProject getProject() {
		IEditorPart editorPart = HybridUI.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) editorPart
					.getEditorInput();
			IProject activeProject = input.getFile().getProject();
			return activeProject;
		} else {
			return null;
		}
	}
	
	
	private Widget getWidget(){
 		return ((ConfigEditor)getEditor()).getWidget();
	}
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
		IObservableMap observeMap = BeansObservables.observeMap(listContentProvider.getKnownElements(), Icon.class, "src");
		iconsTableViewer.setLabelProvider(new ObservableMapLabelProvider(observeMap));
		iconsTableViewer.setContentProvider(listContentProvider);
		//
		IObservableList iconsGetWidgetObserveList = BeanProperties.list("icons").observe(getWidget());
		iconsTableViewer.setInput(iconsGetWidgetObserveList);
		//
		ObservableListContentProvider listContentProvider_1 = new ObservableListContentProvider();
		IObservableMap observeMap_1 = BeansObservables.observeMap(listContentProvider_1.getKnownElements(), Splash.class, "src");
		splashTableViewer.setLabelProvider(new ObservableMapLabelProvider(observeMap_1));
		splashTableViewer.setContentProvider(listContentProvider_1);
		//
		IObservableList splashesGetWidgetObserveList = BeanProperties.list("splashes").observe(getWidget());
		splashTableViewer.setInput(splashesGetWidgetObserveList);
		//
		IObservableValue observeSingleSelectionIconsTableViewer = ViewerProperties.singleSelection().observe(iconsTableViewer);
		IObservableValue iconsTableViewerWidthObserveDetailValue = BeanProperties.value(Icon.class, "width", int.class).observeDetail(observeSingleSelectionIconsTableViewer);
		IObservableValue observeTextTxtWidthObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtWidth);
		bindingContext.bindValue(iconsTableViewerWidthObserveDetailValue, observeTextTxtWidthObserveWidget, null, null);
		//
		IObservableValue observeSingleSelectionIconsTableViewer_1 = ViewerProperties.singleSelection().observe(iconsTableViewer);
		IObservableValue iconsTableViewerHeightObserveDetailValue = BeanProperties.value(Icon.class, "height", int.class).observeDetail(observeSingleSelectionIconsTableViewer_1);
		IObservableValue observeTextTxtHeightObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtHeight);
		bindingContext.bindValue(iconsTableViewerHeightObserveDetailValue, observeTextTxtHeightObserveWidget, null, null);
		//
		IObservableValue observeSingleSelectionIconsTableViewer_2 = ViewerProperties.singleSelection().observe(iconsTableViewer);
		IObservableValue iconsTableViewerPlatformObserveDetailValue = BeanProperties.value(Icon.class, "platform", String.class).observeDetail(observeSingleSelectionIconsTableViewer_2);
		IObservableValue observeTextTxtPlatformObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtPlatform);
		bindingContext.bindValue(iconsTableViewerPlatformObserveDetailValue, observeTextTxtPlatformObserveWidget, null, null);
		//
		IObservableValue observeSingleSelectionIconsTableViewer_3 = ViewerProperties.singleSelection().observe(iconsTableViewer);
		IObservableValue iconsTableViewerDensityObserveDetailValue = BeanProperties.value(Icon.class, "density", String.class).observeDetail(observeSingleSelectionIconsTableViewer_3);
		IObservableValue observeTextTxtDensityObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtDensity);
		bindingContext.bindValue(iconsTableViewerDensityObserveDetailValue, observeTextTxtDensityObserveWidget, null, null);
		//
		IObservableValue observeSingleSelectionSplashTableViewer = ViewerProperties.singleSelection().observe(splashTableViewer);
		IObservableValue splashTableViewerWidthObserveDetailValue = BeanProperties.value(Splash.class, "width", int.class).observeDetail(observeSingleSelectionSplashTableViewer);
		IObservableValue observeTextTxtSplshWidthObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtSplshWidth);
		bindingContext.bindValue(splashTableViewerWidthObserveDetailValue, observeTextTxtSplshWidthObserveWidget, null, null);
		//
		IObservableValue observeSingleSelectionSplashTableViewer_1 = ViewerProperties.singleSelection().observe(splashTableViewer);
		IObservableValue splashTableViewerHeightObserveDetailValue = BeanProperties.value(Splash.class, "height", int.class).observeDetail(observeSingleSelectionSplashTableViewer_1);
		IObservableValue observeTextTxtSplshHeightObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtSplshHeight);
		bindingContext.bindValue(splashTableViewerHeightObserveDetailValue, observeTextTxtSplshHeightObserveWidget, null, null);
		//
		IObservableValue observeSingleSelectionSplashTableViewer_2 = ViewerProperties.singleSelection().observe(splashTableViewer);
		IObservableValue splashTableViewerPlatformObserveDetailValue = BeanProperties.value(Splash.class, "platform", String.class).observeDetail(observeSingleSelectionSplashTableViewer_2);
		IObservableValue observeTextTxtSplshPlatformObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtSplshPlatform);
		bindingContext.bindValue(splashTableViewerPlatformObserveDetailValue, observeTextTxtSplshPlatformObserveWidget, null, null);
		//
		IObservableValue observeSingleSelectionSplashTableViewer_3 = ViewerProperties.singleSelection().observe(splashTableViewer);
		IObservableValue splashTableViewerDensityObserveDetailValue = BeanProperties.value(Splash.class, "density", String.class).observeDetail(observeSingleSelectionSplashTableViewer_3);
		IObservableValue observeTextTxtSplshDensityObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtSplshDensity);
		bindingContext.bindValue(splashTableViewerDensityObserveDetailValue, observeTextTxtSplshDensityObserveWidget, null, null);
		//
		return bindingContext;
	}
}
