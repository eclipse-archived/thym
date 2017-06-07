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
package org.eclipse.thym.ui.plugins.internal;

import static org.eclipse.thym.ui.plugins.internal.CordovaPluginSelectionPage.PLUGIN_SOURCE_DIRECTORY;
import static org.eclipse.thym.ui.plugins.internal.CordovaPluginSelectionPage.PLUGIN_SOURCE_GIT;
import static org.eclipse.thym.ui.plugins.internal.CordovaPluginSelectionPage.PLUGIN_SOURCE_REGISTRY;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin.RegistryPluginVersion;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class CordovaPluginWizard extends Wizard implements IWorkbenchWizard, ICordovaPluginWizard{
	
	static final String IMAGE_WIZBAN = "/icons/wizban/cordova_plugin_wiz.png";
	private static final String DIALOG_SETTINGS_KEY = "CordovaPluginWizard";
	
	private CordovaPluginSelectionPage pageOne;
	private RegistryConfirmPage pageTwo;
	private IStructuredSelection initialSelection;
	private HybridProject fixedProject;
	private int initialSource;
	
	private class PluginInstallOperation extends WorkspaceModifyOperation{
		
		private CordovaPluginManager pm;
		private int opType;
		private File dir;
		private URI gitRepo;
		private List<RegistryPluginVersion> plugins;
		
		private PluginInstallOperation(CordovaPluginManager pm){
			this.pm = pm;
		}
		
		public PluginInstallOperation(File directory, CordovaPluginManager pm){
			this(pm);
			this.dir = directory;
			opType = PLUGIN_SOURCE_DIRECTORY;
		}
		
		public PluginInstallOperation(URI gitRepo, CordovaPluginManager pm){
			this(pm);
			this.gitRepo = gitRepo;
			opType = PLUGIN_SOURCE_GIT;
		}
		
		public PluginInstallOperation(List<RegistryPluginVersion> plugins, CordovaPluginManager pm){
			this(pm);
			this.plugins = plugins;
			opType = PLUGIN_SOURCE_REGISTRY;
		}

		@Override
		protected void execute(IProgressMonitor monitor) throws CoreException,
				InvocationTargetException, InterruptedException {
			
			switch (opType){
			case PLUGIN_SOURCE_DIRECTORY:
				pm.installPlugin(this.dir, monitor);
				break;
			case PLUGIN_SOURCE_GIT:
				pm.installPlugin(this.gitRepo, monitor );
				break;
			case PLUGIN_SOURCE_REGISTRY:
				for (RegistryPluginVersion cordovaRegistryPluginVersion : plugins) {
					pm.installPlugin(cordovaRegistryPluginVersion, monitor);
				}
				break;
			default:
				Assert.isTrue(false, "No valid plugin source can be determined");
				break;
			}
		}
	}
	
	public CordovaPluginWizard() {
		setWindowTitle("Cordova Plug-in Discovery");
		setNeedsProgressMonitor(true);
		IDialogSettings workbenchSettings= HybridUI.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
		setDialogSettings(section);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		initialSelection = selection;
	}
	/**
	 * Causes the wizard to work with a fixed project, and does not enable
	 * users to select a different project to operate on.
	 * @param project
	 */
	public void init(HybridProject project,  int initialSourceTab){
		this.fixedProject = project;
		this.initialSource = initialSourceTab;
	}
	
	@Override
	public boolean performFinish() {	
		HybridProject project = HybridProject.getHybridProject(pageOne.getProjectName());
		if(project == null )
			return false;
		CordovaPluginManager pm = new CordovaPluginManager(project);
		PluginInstallOperation op = null;
		switch (pageOne.getPluginSourceType()) {
		case PLUGIN_SOURCE_DIRECTORY:
			File directory = new File(pageOne.getSelectedDirectory());
			op=new PluginInstallOperation(directory, pm); 
			break;
		case PLUGIN_SOURCE_GIT:
			URI uri = URI.create(pageOne.getSpecifiedGitURL());
			op = new PluginInstallOperation(uri, pm);
			break;
		case PLUGIN_SOURCE_REGISTRY:
			List<RegistryPluginVersion> plugins = pageTwo.getSelectedPluginVersions();
			op = new PluginInstallOperation(plugins, pm);
			break;
		default:
			Assert.isTrue(false, "No valid plugin source can be determined");
		}
		
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				if(e.getTargetException() instanceof CoreException ){
					StatusManager.handle((CoreException) e.getTargetException());
				}else{
				ErrorDialog.openError(getShell(), "Plug-in installation problem", null, 
						new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Errors occured during plug-in installation", e.getTargetException() ));
				return false;
				}
			}
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		savePageSettings();
		return true;
	}
	
	@Override
	public void addPages() {
		if(fixedProject == null ){
			pageOne = new CordovaPluginSelectionPage(this.initialSelection);
		}else{
			pageOne = new CordovaPluginSelectionPage(fixedProject,initialSource);
		}
		addPage(pageOne);
		pageTwo = new RegistryConfirmPage();
		addPage(pageTwo);
	}
	
	public WizardPage getRegistryConfirmPage(){
		return pageTwo;
	}
	
	HybridProject getFixedProject(){
		return fixedProject;
	}
	
	private void savePageSettings() {
		IDialogSettings workbenchSettings = HybridUI.getDefault()
				.getDialogSettings();
		IDialogSettings section = workbenchSettings
				.getSection(DIALOG_SETTINGS_KEY);
		if (section == null) {
			section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
		}
		setDialogSettings(section);
		pageOne.saveWidgetValues();
	}

	@Override
	public boolean isPluginSelectionOptional() {
		return false;
	}
}
