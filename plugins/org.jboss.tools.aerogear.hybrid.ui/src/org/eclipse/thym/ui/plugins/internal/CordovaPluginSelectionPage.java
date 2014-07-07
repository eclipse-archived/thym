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
package org.eclipse.thym.ui.plugins.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.thym.ui.wizard.export.DirectorySelectionGroup;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginInfo;
/**
 * A wizard page that allows users to view cordova plug-in registry and select plug-ins either 
 * from registry or through other supported means. This page can be used within a wizard 
 * that implements {@link ICordovaPluginWizard} together with {@link RegistryConfirmPage}.
 * 
 * @author Gorkem Ercan
 * @see ICordovaPluginWizard
 * @see RegistryConfirmPage
 *
 */
public class CordovaPluginSelectionPage extends WizardPage {

	public static final int PLUGIN_SOURCE_REGISTRY =1;
	public static final int PLUGIN_SOURCE_GIT =2;
	public static final int PLUGIN_SOURCE_DIRECTORY =3;
	private static final String PAGE_NAME = "Cordova Plug-in Selection Page";
	private static final String PAGE_TITLE = "Install Cordova Plug-in";
	private static final String PAGE_DESCRIPTION = "Discover and Install Cordova Plug-ins";

	private List<CordovaRegistryPluginInfo> cordovaPluginInfos;
	private HybridProject fixedProject;
	private boolean noProject;
	private IStructuredSelection initialSelection;
	private TabFolder tabFolder;
	private TabItem registryTab;
	private CordovaPluginCatalogViewer catalogViewer;
	private TabItem gitTab;
	private TabItem directoryTab;
	private DirectorySelectionGroup destinationDirectoryGroup;
	private Text textProject;
	private Group grpRepositoryUrl;
	private Text gitUrlTxt;
	private final CordovaPluginRegistryManager client = new CordovaPluginRegistryManager(CordovaPluginRegistryManager.DEFAULT_REGISTRY_URL);
	

	public CordovaPluginSelectionPage(){
		super(PAGE_NAME,PAGE_TITLE, HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, CordovaPluginWizard.IMAGE_WIZBAN));
		setDescription(PAGE_DESCRIPTION);
	}
	
	protected CordovaPluginSelectionPage(IStructuredSelection selection) {
		this();
		this.initialSelection = selection;
	}
	/**
	 * If constructed with a {@link HybridProject} this page does not 
	 * present a project selection UI and all operations are assumed to be
	 * fixed to the passed project.
	 * @param pageName
	 * @param project
	 */
	protected CordovaPluginSelectionPage(HybridProject project){
		this();
		this.fixedProject= project;
	}
	/**
	 * noProject constructor to signal that wizards is running for 
	 * a project that has not been created yet.
	 * @param noProject
	 */
	public CordovaPluginSelectionPage(boolean noProject){
		this();
		this.noProject = noProject;
	}

	@SuppressWarnings("restriction")
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		if( fixedProject == null && !noProject){//let user select a project
			createProjectGroup(container);
		}
		
		tabFolder = new TabFolder(container, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tabFolder);
		
		registryTab = new TabItem(tabFolder, SWT.NONE);
		registryTab.setText("Registry");
		catalogViewer = new CordovaPluginCatalogViewer((noProject?SWT.NULL: CordovaPluginCatalogViewer.FILTER_INSTALLED));
		catalogViewer.createControl(tabFolder);
		catalogViewer.getControl().setFocus();
		
		
		registryTab.setControl(catalogViewer.getControl());
		
		gitTab = new TabItem(tabFolder, SWT.NONE);
		gitTab.setText("Git");
		
		grpRepositoryUrl = new Group(tabFolder, SWT.NONE);
		grpRepositoryUrl.setText("Repository");
		gitTab.setControl(grpRepositoryUrl);
		grpRepositoryUrl.setLayout(new GridLayout(2, false));
		
		Label lblUrl = new Label(grpRepositoryUrl, SWT.NONE);
		lblUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUrl.setText("URL:");
		
		gitUrlTxt = new Text(grpRepositoryUrl, SWT.BORDER);
		gitUrlTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gitUrlTxt.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}
		});
		
		directoryTab = new TabItem(tabFolder, SWT.NONE);
		directoryTab.setText("Directory");
		
		destinationDirectoryGroup = new DirectorySelectionGroup(tabFolder, SWT.NONE);
		destinationDirectoryGroup.setText("Plug-in:");
		destinationDirectoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		destinationDirectoryGroup.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}


		});
		directoryTab.setControl(destinationDirectoryGroup);
		tabFolder.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}
		});
		setupFromInitialSelection();
		restoreWidgetValues();
		updateProjectOnViewer();
		catalogViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(validatePage());
			}
		});
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible && cordovaPluginInfos == null){
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					populatePluginInfos();
				}
			});
		}
	}

	private void createProjectGroup(Composite container) {
		Group grpProject = new Group(container, SWT.NONE);
		grpProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpProject.setText("Project");
		grpProject.setLayout(new GridLayout(3, false));
		
		Label lblProject = new Label(grpProject, SWT.NONE);
		lblProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProject.setText("Project:");
		
		textProject = new Text(grpProject, SWT.BORDER);
		textProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textProject.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				boolean isValidProject = isValidProject(textProject.getText());
				setPageComplete(validatePage());
				if(isValidProject && getPluginSourceType() == PLUGIN_SOURCE_REGISTRY){
					updateProjectOnViewer();
				}
			}
		});
		
		
		Button btnProjectBrowse = new Button(grpProject, SWT.NONE);
		btnProjectBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog es = new ElementListSelectionDialog(getShell(), WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
				es.setElements(HybridCore.getHybridProjects().toArray());
				es.setTitle("Project Selection");
				es.setMessage("Select a project to run");
				if (es.open() == Window.OK) {			
					HybridProject project = (HybridProject) es.getFirstResult();
					textProject.setText(project.getProject().getName());
				}		
			}
		});
		btnProjectBrowse.setText("Browse...");
	}

	private void populatePluginInfos() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					try {
						if(cordovaPluginInfos == null ){
							cordovaPluginInfos = client.retrievePluginInfos(monitor);
						}
					} catch (CoreException ce) {
						throw new InvocationTargetException(ce);
					}
					if(cordovaPluginInfos == null){
						CoreException e =  new CoreException(new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, 
								"Error while retrieving the Cordova Plug-in Registry Catalog"));
						throw new InvocationTargetException(e);
					}
					final Object[] pluginInfos = cordovaPluginInfos.toArray();
					final Display display = getControl().getDisplay();
					display.syncExec(new Runnable() {
						@Override
						public void run() {
							BusyIndicator.showWhile(display, new Runnable() {
								@SuppressWarnings("restriction")
								@Override
								public void run() {
									if(!getControl().isDisposed() && isCurrentPage()){
										catalogViewer.getViewer().setInput(pluginInfos);
									}
								}
							});
						}
					});
				}
			});
		} catch (InvocationTargetException inve) {
			if (inve.getTargetException() != null) {
				if(inve.getTargetException() instanceof CoreException ){
					StatusManager.handle((CoreException) inve.getTargetException());
				}else{
					ErrorDialog.openError(getShell(), "Registry catalog eror ",null, 
							new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Error retrieving the Cordova plug-in registry catalog", inve.getTargetException() ));
				}
			}
		} catch (InterruptedException inte) {
			throw new OperationCanceledException(inte.getMessage());
		}
	}
	
	private void setupFromInitialSelection() {
		if(initialSelection != null && !initialSelection.isEmpty()){
			Iterator<?> selects = initialSelection.iterator();
			while (selects.hasNext()) {
				Object obj  = selects.next();
				if(obj instanceof IResource ){
					IResource res = (IResource)obj;
					IProject project = res.getProject();
					HybridProject hybrid = HybridProject.getHybridProject(project);
					if(hybrid != null ){
						textProject.setText(project.getName());
					}
				}
			}
		}
	}
	
	@Override
	public IWizardPage getNextPage() {
		if( getSelectedTabItem()!=registryTab){
			return null;
		}
		List<CordovaRegistryPluginInfo> infos = getCheckedCordovaRegistryItems();
		if(getPluginWizard().isPluginSelectionOptional() && (infos == null || infos.isEmpty())){
			return null;
		}
		ICordovaPluginWizard wiz = (ICordovaPluginWizard) getWizard();
		RegistryConfirmPage confirmPage = (RegistryConfirmPage)wiz.getRegistryConfirmPage();
		confirmPage.setSelectedPlugins(getCheckedCordovaRegistryItems());
		return super.getNextPage();
	}
	
	
	
	public int getPluginSourceType(){
		TabItem selected = getSelectedTabItem();
		if(selected == gitTab )
			return PLUGIN_SOURCE_GIT;
		if(selected == directoryTab )
			return PLUGIN_SOURCE_DIRECTORY;
		return PLUGIN_SOURCE_REGISTRY; // defaults to registry;
	}
	
	public String getSelectedDirectory(){
		System.out.println("getSelectedDirectory");
		return this.destinationDirectoryGroup.getValue();
	}
	
	public String getSpecifiedGitURL(){
		System.out.println("getSpecifiedGitUrl");
		return this.gitUrlTxt.getText();
	}
	
	public String getProjectName(){
		if(fixedProject != null ){
			return fixedProject.getProject().getName();
		}
		if(noProject){
			return "";
		}
		return textProject.getText();
	}
	
	@SuppressWarnings("unchecked")
	private List<CordovaRegistryPluginInfo> getCheckedCordovaRegistryItems(){
		IStructuredSelection selection = catalogViewer.getSelection();
		if(selection == null || selection.isEmpty())
			return Collections.emptyList();
		return selection.toList();
	}
	
	private TabItem getSelectedTabItem(){
		TabItem[] selections = tabFolder.getSelection();
		Assert.isTrue(selections.length>0);
		return selections[0];
	}
	
	private boolean validatePage() {
		//Check project
		if (fixedProject == null && !noProject) {
			String projectName = textProject.getText();
			if (!isValidProject(projectName)) {
				return false;
			}
		}
		//Now tabs
		
		boolean valid = false;
		switch (getPluginSourceType()) {
		case PLUGIN_SOURCE_DIRECTORY:
			valid = validateDirectroyTab();
			break;
		case PLUGIN_SOURCE_GIT:	
			valid = validateGitTab(); 
			break;
		case PLUGIN_SOURCE_REGISTRY:
			valid = validateRegistryTab();
			break;
		}
		if(valid){
			setMessage(null,NONE);
		}
		return valid;
	}

	private boolean isValidProject(String projectName) {
		if (projectName == null || projectName.isEmpty()) {
			setMessage("Specify a project", ERROR);
			return false;
		}
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject prj = ws.getRoot().getProject(projectName);
		if(!prj.exists()){
			setMessage("Project does not exist", ERROR);
			return false;
		}
		if(!prj.isOpen()){
			setMessage("Project is not open", ERROR);
			return false;
		}
		List<HybridProject> projects = HybridCore.getHybridProjects();
		boolean projectValid = false;
		for (HybridProject hybridProject : projects) {
			if (hybridProject.getProject().getName().equals(projectName)) {
				projectValid = true;
				break;
			}
		}
		if (!projectValid) {
			setMessage(
					"Specified project is not suitable for Cordova plug-in installation",
					ERROR);
			return false;
		}
		return true;
	}
	
	private boolean validateRegistryTab() {
		List<CordovaRegistryPluginInfo> infos = getCheckedCordovaRegistryItems();
		if (!getPluginWizard().isPluginSelectionOptional() && infos.isEmpty()){
			setMessage("Specify Cordova plug-in(s) for installation", ERROR);
			return false;
		}
		return true;
	}
	
	private void updateProjectOnViewer() {
		HybridProject project = null;
		if(fixedProject != null ){
			project = fixedProject;
		}if(noProject) {
			project = null;
		}else if(project == null && textProject != null ){
			project = HybridProject.getHybridProject(textProject.getText());
		}
		catalogViewer.setProject(project);
	}

	private boolean validateGitTab(){
		
		String url = gitUrlTxt.getText();
		if( !getPluginWizard().isPluginSelectionOptional() && (url == null || url.isEmpty()) ){
			setMessage("Specify a git repository for fetching the Cordova plug-in",ERROR);
			return false;
		}
		try {
			new URI(url);
		} catch (URISyntaxException e) {
			setMessage("Specify a valid address",ERROR);
			return false;
		}
		return true;
	}
	
	private boolean validateDirectroyTab(){
		String directory = destinationDirectoryGroup.getValue();
		if(directory == null || directory.isEmpty() ){
			if(getPluginWizard().isPluginSelectionOptional()){
				//can be empty because it is optional
				return true;
			}
			setMessage("Select the directory for the Cordova plug-in",ERROR);
			return false;
		}
		File pluginFile = new File(directory);
		if(!DirectorySelectionGroup.isValidDirectory(pluginFile)){
			setMessage(directory +" is not a valid directory",ERROR);
			return false;
		}
		if(!pluginFile.isDirectory()){
			setMessage("Select an existing directory", ERROR);
			return false;
		}
		File pluginXML = new File(pluginFile, PlatformConstants.FILE_XML_PLUGIN);
		if(!pluginXML.isFile()){
			setMessage("Specified directory is not a valid plug-in directory",ERROR);
			return false;
		}
		return true;
		
	}
	
	void saveWidgetValues(){
		IDialogSettings settings = getDialogSettings();
		if(settings != null ){
			destinationDirectoryGroup.saveHistory(settings);
		}
	}
	
	private void restoreWidgetValues(){
		IDialogSettings settings = getDialogSettings();
		if(settings != null ){
			destinationDirectoryGroup.restoreHistory(settings);
		}
	}
	
	private ICordovaPluginWizard getPluginWizard(){
		return (ICordovaPluginWizard) getWizard();
	}
	
}
