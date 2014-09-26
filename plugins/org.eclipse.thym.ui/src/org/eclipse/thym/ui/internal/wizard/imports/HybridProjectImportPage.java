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
package org.eclipse.thym.ui.internal.wizard.imports;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.FileOverwriteCallback;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreator;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

public class HybridProjectImportPage extends WizardPage implements IOverwriteQuery{
	
	private class ProjectCandidate {
		private Widget widget;
		File wwwLocation;
		File configLocation;
		boolean conflicts;
		
		
		public ProjectCandidate(File www, File config){
			this.configLocation = config;
			this.wwwLocation = www;
		}
		
		String getLabel(){
			if(getWidget() == null ){
				return NLS.bind("INVALID {0}",wwwLocation.toString()); 
			}
			String appName = getProjectName();
			return NLS.bind("{0} ({1})", new String[]{appName,wwwLocation.toString() });
		}

		String getProjectName() {
			String projectName = getWidget().getId();
			if(widget.getName() != null ){
				projectName = getWidget().getName();
			}
			return projectName;
		}
		
		boolean exists(){
			if(getWidget() == null )
				return true;
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final String projectName = getProjectName();
			IPath wsPath = root.getLocation();
			IPath localProjectPath = wsPath.append(projectName);
			IProject project = root.getProject(projectName);
			return project.exists() || localProjectPath.toFile().exists();
		}
		
		Widget getWidget(){
			if(widget == null ){
				try {
					widget = WidgetModel.parseToWidget(configLocation);
				} catch (CoreException e) {
					HybridUI.log(IStatus.ERROR, "Error parsing the config.xml for import project", e);
				}
			}
			return widget;
		}
	}
	
	private final class ProjectCandidateLabelProvider extends LabelProvider implements IColorProvider{
		public String getText(Object element) {
			return ((ProjectCandidate) element).getLabel();
		}

		@Override
		public Color getForeground(Object element) {
			ProjectCandidate candie = (ProjectCandidate) element;
			if(candie.conflicts || candie.exists()){
				return getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
			}
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}
	}
	
	private final static String SETTINGSKEY_DIRECTORIES = "HybridProjectImportPage.DIRECTORIES";//$NON-NLS-1$
	private final static String SETTINGSKEY_COPY = "HybridProjectImportPage.COPY";//$NON-NLS-1$
	   
	private Combo directoryPathField;
	private String previouslyBrowsedDirectory ="";
	private ProjectCandidate[] candidates;
	private CheckboxTreeViewer projectList;
	private Button copyCheckbox;
	private boolean copyFiles;

	protected HybridProjectImportPage() {
		super("HybridProjectImportPage");
		setTitle("Cordova Project Import");
		setDescription("Select a directory to search for Cordova projects");
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		Composite workArea = new Composite(parent, SWT.NONE);
		setControl(workArea);
		
		GridLayoutFactory.fillDefaults().applyTo(workArea);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(workArea);
		
		createProjectRoot(workArea);
		createProjectsList(workArea);
		createOptionsGroup(workArea);
		restoreFromHistory();
		setPageComplete(validatePage());
		Dialog.applyDialogFont(workArea);
	}

	private void createProjectsList( final Composite workArea) {
		final Label projectsLabel = new Label(workArea,SWT.NULL);
		projectsLabel.setText("Projects:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(projectsLabel);
		
		Composite projectListGroup = new Composite(workArea, SWT.NULL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(projectListGroup);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(projectListGroup);
		projectList = new CheckboxTreeViewer(projectListGroup);
		PixelConverter pc = new PixelConverter(projectList.getControl());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL)
			.hint(pc.convertWidthInCharsToPixels(25),pc.convertHeightInCharsToPixels(10)).applyTo(projectList.getControl());
		
		projectList.setLabelProvider(new ProjectCandidateLabelProvider());
		projectList.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public boolean hasChildren(Object element) {
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if(candidates == null )
					return new ProjectCandidate[0];
				return candidates;
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
		
		projectList.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				ProjectCandidate candidate = (ProjectCandidate)event.getElement();
				//Cancel out existing projects
				if(candidate.exists()){
					projectList.setChecked(candidate, false);
					return;
				}
				updateConflicts(candidate, event.getChecked());
				projectList.refresh(true);
				setPageComplete(validatePage());
			}
		});
		
		
		final Composite selectButtonGroup = new Composite(projectListGroup, SWT.NULL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(selectButtonGroup);
		GridLayoutFactory.fillDefaults().applyTo(selectButtonGroup);
		
		Button selectAll = new Button(selectButtonGroup,SWT.PUSH);
		selectAll.setText("Select All");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(selectAll);
	
		Button deselectAll = new Button(selectButtonGroup, SWT.PUSH);
		deselectAll.setText("Deselect All");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(deselectAll);
		
		Button refresh = new Button(selectButtonGroup, SWT.PUSH);
		refresh.setText("Refresh");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(refresh);
		
		selectAll.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if(candidates != null ){
					for (ProjectCandidate candie : candidates) {
						if(!candie.conflicts && !candie.exists()){
							projectList.setChecked(candie, true);
							updateConflicts(candie, true);
						}
					}
				}
				setPageComplete(validatePage());
			}
		});
		
		deselectAll.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (candidates != null) {
					for (ProjectCandidate candie : candidates) {
						projectList.setChecked(candie, false);
						updateConflicts(candie, false);
					}
				}
				setPageComplete(false);
			}
		});
		
		refresh.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateProjectsList(directoryPathField.getText());
			}
		});
		
		projectList.setInput(this);
	}

	private void createProjectRoot(final Composite workArea) {
		final Composite projectRootGroup = new Composite(workArea, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 10).applyTo(projectRootGroup);
		GridDataFactory.fillDefaults().grab(true,false).align(SWT.FILL,SWT.FILL).applyTo(projectRootGroup);
	
		final Label directoryLabel = new Label(projectRootGroup, SWT.NULL);
		directoryLabel.setText("Select root directory:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(directoryLabel);
		
		directoryPathField = new Combo(projectRootGroup, SWT.BORDER);
		PixelConverter pixelConverter = new PixelConverter(directoryPathField);
		GridDataFactory.fillDefaults().grab(true, false).hint(pixelConverter.convertWidthInCharsToPixels(25),SWT.DEFAULT).applyTo(directoryPathField);
		
		final Button browseDirectoriesButton = new Button(projectRootGroup, SWT.PUSH);
		browseDirectoriesButton.setText("Browse...");
		GridDataFactory.fillDefaults().applyTo(browseDirectoriesButton);
		browseDirectoriesButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				handleBrowseButtonPressed();
			}
		});
		
		directoryPathField.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				updateProjectsList(directoryPathField.getText());
			}
		});
	}
	
	private void handleBrowseButtonPressed() {
		final DirectoryDialog dialog = new DirectoryDialog(
				directoryPathField.getShell(), SWT.SHEET);
		dialog.setMessage("Select search directory");

		String dirName = directoryPathField.getText().trim();
		if (dirName.isEmpty()) {
			dirName = previouslyBrowsedDirectory;
		}

		if (dirName.isEmpty()) {
			dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
		} else {
			File path = new File(dirName);
			if (path.exists()) {
				dialog.setFilterPath(new Path(dirName).toOSString());
			}
		}
		
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			previouslyBrowsedDirectory = selectedDirectory;
			directoryPathField.setText(previouslyBrowsedDirectory);
			updateProjectsList(selectedDirectory);
		}
	}
	private void updateConflicts(ProjectCandidate candidate, boolean checked){
		for(ProjectCandidate elem: candidates ){
			Widget w1 = elem.getWidget();
			Widget w2 = candidate.getWidget();
			if(w1.getId().equals(w2.getId()) &&
					w1.getName().equals(w2.getName()) &&
					!elem.configLocation.equals(candidate.configLocation)){
				if(projectList.getChecked(elem)){
					projectList.setChecked(candidate, false);
				}else{
					elem.conflicts = checked;
				}
			}
			
		}
		
	}
	private void updateProjectsList(final String selectedDirectory) {
		if(selectedDirectory == null || selectedDirectory.isEmpty()){
			candidates = null;
			projectList.refresh(true);
			return;
		}
		final File directory = new File(selectedDirectory);

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					if(directory.isDirectory()){
						List<ProjectCandidate> candies= new ArrayList<HybridProjectImportPage.ProjectCandidate>();
						collectProjectCandidates(candies, directory, monitor);
						candidates = candies.toArray(new ProjectCandidate[candies.size()]); 
					}
				}
			});
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				if(e.getTargetException() instanceof CoreException ){
					StatusManager.handle((CoreException) e.getTargetException());
				}else{
					ErrorDialog.openError(getShell(), "Error finding projects to import",null, 
							new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Error while searching for projects to import", e.getTargetException() ));
				}
			}
		} catch (InterruptedException e) {
			HybridUI.log(IStatus.ERROR, "Error searchig projects to import", e);
		}
		projectList.refresh(true);
	}

	protected void collectProjectCandidates(List<ProjectCandidate> candidates,
			File directory, IProgressMonitor monitor) {
		if(monitor.isCanceled()){
			return;
		}
		
		File[] configXMLs = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return PlatformConstants.FILE_XML_CONFIG.equals(name);
			}
		});
		
		if(configXMLs == null){
			return;
		}
	
		for (File config: configXMLs) {
			File parent = config.getParentFile();
			ProjectCandidate candidate = null;
			if (config.isFile()) {
				if (parent.getName().equals(PlatformConstants.DIR_WWW)) {
					candidate = new ProjectCandidate(parent, config);
				} else {
					File sameLevelWWW = new File(parent, PlatformConstants.DIR_WWW);
					if (sameLevelWWW.isDirectory()) {
						candidate = new ProjectCandidate(sameLevelWWW, config);
					}
				}
			}
			if(candidate != null){
				candidates.add(candidate);
				return;
			}
		}
		
		File[] dirs = directory.listFiles();
		for (File dir : dirs) {
			collectProjectCandidates(candidates, dir, monitor);
		}
	}
	
	private void createOptionsGroup(Composite workArea) {
		final Group optionsGroup = new Group(workArea, SWT.NULL);
		optionsGroup.setText("Options:");
		GridLayoutFactory.fillDefaults().margins(10,10).applyTo(optionsGroup);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(optionsGroup);
		
		
		copyCheckbox = new Button(optionsGroup, SWT.CHECK);
		copyCheckbox.setText("Copy into workspace");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL);
		copyCheckbox.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				copyFiles = copyCheckbox.getSelection();
				setPageComplete(validatePage());
			}
		});
	}
	
	private void restoreFromHistory(){
		//Directories
		IDialogSettings settings = getDialogSettings();
		if (settings == null) return;
		String[] sourceNames = settings.getArray(SETTINGSKEY_DIRECTORIES);
		if (sourceNames == null) {
			return; 
		}
		for (String dirname : sourceNames) {
			directoryPathField.add(dirname);
		}
		//copy to workspace
		copyFiles = settings.getBoolean(SETTINGSKEY_COPY);
		copyCheckbox.setSelection(copyFiles);
	}
	
	private void saveInHistroy(){
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// Directories 
			String[] sourceNames = settings.getArray(SETTINGSKEY_DIRECTORIES);
			if (sourceNames == null) {
				sourceNames = new String[0];
			}
			List<String> l = new ArrayList<String>(Arrays.asList(sourceNames));
			l.remove(directoryPathField.getText());
			l.add(0,directoryPathField.getText());
			sourceNames = l.toArray(new String[l.size()]);
			settings.put(SETTINGSKEY_DIRECTORIES, sourceNames);
			
			//Copy to workspace
			settings.put(SETTINGSKEY_COPY, copyCheckbox.getSelection());
		}
	}
	
	boolean createProjects(){
		saveInHistroy();
		
		final FileOverwriteCallback cb = new FileOverwriteCallback() {
			
			@Override
			public boolean isOverwiteAllowed(String[] files) {
				return true;
			}
		};
		
		final Object[] selectedCandidates = projectList.getCheckedElements();
		WorkspaceModifyOperation wop = new WorkspaceModifyOperation() {
			
			@Override
			protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("", selectedCandidates.length);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				try {
					for (int i = 0; i < selectedCandidates.length; i++) {
						ProjectCandidate pc = (ProjectCandidate) selectedCandidates[i];
						IProject prj = doCreateProject(pc, new SubProgressMonitor(monitor, 1));
						HybridProject project = HybridProject.getHybridProject(prj);
						CordovaPluginManager pman = project.getPluginManager();
						
						//Create a copy of the installed plugins since the original list will change 
						//as we iterate
						List<CordovaPlugin> plugins = new ArrayList<CordovaPlugin>(pman.getInstalledPlugins());
						for (CordovaPlugin cordovaPlugin : plugins) {
							pman.fixInstalledPlugin(cordovaPlugin, cb,monitor);
						}
					}
				}
				catch(CoreException e){
					throw new InvocationTargetException(e);
				}finally{
					monitor.done();
				}
			}
		};
		
		try {
			getContainer().run(true, true, wop);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				if(e.getTargetException() instanceof CoreException ){
					StatusManager.handle((CoreException) e.getTargetException());
				}else{
					ErrorDialog.openError(getShell(), "Error importing project",null, 
							new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Project import error", e.getTargetException() ));
				}
			}

		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
		return true;
	}
	

	private IProject doCreateProject(ProjectCandidate pc, IProgressMonitor monitor) throws CoreException, InterruptedException {
		HybridProjectCreator projectCreator = new HybridProjectCreator();
		Widget w = pc.getWidget();
		String projectName = pc.getProjectName();
		URI location = null;
		if(!copyFiles){
			location = pc.wwwLocation.getParentFile().toURI();
		}
		IProject project = projectCreator.createProject(projectName, location, w.getName(), w.getId(), HybridMobileEngineManager.getDefaultEngine(), monitor);
		if(copyFiles){
			ImportOperation operation = new ImportOperation(project
					.getFullPath(), pc.wwwLocation.getParentFile(), FileSystemStructureProvider.INSTANCE
					, this);
			operation.setContext(getShell());
			operation.setOverwriteResources(true); 
			operation.setCreateContainerStructure(false);
			
			try {
				operation.run(monitor);
			} catch (InvocationTargetException e) {
				if(e.getCause() != null  && e.getCause() instanceof CoreException){
					CoreException corex = (CoreException) e.getCause();
					throw corex;
				}
			}
			IStatus status = operation.getStatus();
			if (!status.isOK())
				throw new CoreException(status);
		}
		return project;

	}
	
	private boolean validatePage(){
		final Object[] selectedCandidates = projectList.getCheckedElements();
		if(selectedCandidates.length < 1 ){
			String msg = null;
			if(candidates != null ){
				for (ProjectCandidate candie : candidates){
					if(candie.exists()){
						msg = "Some projects cannot be imported because they already exist in the workspace";
					}
				}
			}
			setMessage(msg, WARNING);
			setErrorMessage(null);
			return false;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		for (int i = 0; i < selectedCandidates.length; i++) {
			ProjectCandidate pc = (ProjectCandidate) selectedCandidates[i];
			
			final IStatus nameStatus= workspace.validateName(pc.getProjectName(), IResource.PROJECT);
			if (!nameStatus.isOK()) {
				setErrorMessage(nameStatus.getMessage());
				return false;
			}
			final IProject handle= workspace.getRoot().getProject(pc.getProjectName());
			if (handle.exists()) {
				setErrorMessage(NLS.bind("Project {0} already exists in the workspace", pc.getProjectName()));
				return false;
			}			
			
			IPath projectLocation= ResourcesPlugin.getWorkspace().getRoot().getLocation().append(pc.getProjectName());
			if (projectLocation.toFile().exists()) {
				try {
					//correct casing
					String canonicalPath= projectLocation.toFile().getCanonicalPath();
					projectLocation= new Path(canonicalPath);
				} catch (IOException e) {
					HybridUI.log(IStatus.WARNING, "Error getting canonical path", e);
				}

				String existingName= projectLocation.lastSegment();
				if (!existingName.equals(pc.getProjectName())) {
					setErrorMessage(NLS.bind("Project name {0} already exists on the workspace", existingName));
					return false;
				}
			}
			
			IPath projectPath = null;
			if(!copyFiles){
				projectPath = new Path(pc.wwwLocation.getParentFile().toString());
			}
			final IStatus locationStatus= workspace.validateProjectLocation(handle, projectPath);
			if (!locationStatus.isOK()) {
				setErrorMessage(locationStatus.getMessage());
				return false;
			}
		}
		setMessage(null);
		setErrorMessage(null);
		return true;
	}

	@Override
	public String queryOverwrite(String pathString) {
		return IOverwriteQuery.ALL;
	}
	
}
