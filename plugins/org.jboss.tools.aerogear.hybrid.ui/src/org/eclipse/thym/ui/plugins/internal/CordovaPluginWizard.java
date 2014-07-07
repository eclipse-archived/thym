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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.FileOverwriteCallback;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginVersion;

public class CordovaPluginWizard extends Wizard implements IWorkbenchWizard, FileOverwriteCallback, ICordovaPluginWizard{
	static final String IMAGE_WIZBAN = "/icons/wizban/cordova_plugin_wiz.png";
	private static final String DIALOG_SETTINGS_KEY = "CordovaPluginWizard";
	
	private static class OverwriteDialog extends MessageDialog{
		public static final int YES_TO_ALL_INDEX = 0;
		private String[] paths;

		public OverwriteDialog(Shell parentShell, String[] paths) {
			super(parentShell, "Overwrite Files", null, "Listed files will be overwritten, would you like to proceed with the installation?",
					MessageDialog.QUESTION, new String[]{IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL}, 1);
			this.paths = paths;
		}
		
		@Override
		protected Control createCustomArea(Composite parent) {
			org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(parent, SWT.SINGLE);
			for (int i = 0; i < paths.length; i++) {
				list.add(paths[i]);
			}
			return list;
		}
		
	}
	
	private CordovaPluginSelectionPage pageOne;
	private RegistryConfirmPage pageTwo;
	private IStructuredSelection initialSelection;
	private HybridProject fixedProject;
	
	private class PluginInstallOperation extends WorkspaceModifyOperation{
		
		private CordovaPluginManager pm;
		private int opType;
		private File dir;
		private URI gitRepo;
		private List<CordovaRegistryPluginVersion> plugins;
		private FileOverwriteCallback fileOverwriteCallback;
		
		private PluginInstallOperation(CordovaPluginManager pm, FileOverwriteCallback overwrite){
			this.pm = pm;
			this.fileOverwriteCallback = overwrite;
		}
		
		public PluginInstallOperation(File directory, CordovaPluginManager pm, FileOverwriteCallback overwite ){
			this(pm,overwite);
			this.dir = directory;
			opType = PLUGIN_SOURCE_DIRECTORY;
		}
		
		public PluginInstallOperation(URI gitRepo, CordovaPluginManager pm, FileOverwriteCallback overwite){
			this(pm, overwite);
			this.gitRepo = gitRepo;
			opType = PLUGIN_SOURCE_GIT;
		}
		
		public PluginInstallOperation(List<CordovaRegistryPluginVersion> plugins, CordovaPluginManager pm, FileOverwriteCallback overwite ){
			this(pm,overwite);
			this.plugins = plugins;
			opType = PLUGIN_SOURCE_REGISTRY;
		}

		@Override
		protected void execute(IProgressMonitor monitor) throws CoreException,
				InvocationTargetException, InterruptedException {
			
			switch (opType){
			case PLUGIN_SOURCE_DIRECTORY:
				pm.installPlugin(this.dir,fileOverwriteCallback, monitor);
				break;
			case PLUGIN_SOURCE_GIT:
				pm.installPlugin(this.gitRepo,null,null,fileOverwriteCallback,monitor );
				break;
			case PLUGIN_SOURCE_REGISTRY:
				CordovaPluginRegistryManager regMgr = new CordovaPluginRegistryManager(CordovaPluginRegistryManager.DEFAULT_REGISTRY_URL);
				for (CordovaRegistryPluginVersion cordovaRegistryPluginVersion : plugins) {
					pm.installPlugin(regMgr.getInstallationDirectory(cordovaRegistryPluginVersion,monitor),fileOverwriteCallback,monitor);
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
	public void init(HybridProject project){
		this.fixedProject = project;
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
			op=new PluginInstallOperation(directory, pm,this); 
			break;
		case PLUGIN_SOURCE_GIT:
			URI uri = URI.create(pageOne.getSpecifiedGitURL());
			op = new PluginInstallOperation(uri, pm, this);
			break;
		case PLUGIN_SOURCE_REGISTRY:
			List<CordovaRegistryPluginVersion> plugins = pageTwo.getSelectedPluginVersions();
			op = new PluginInstallOperation(plugins, pm, this);
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
			pageOne = new CordovaPluginSelectionPage(fixedProject);
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
	public boolean isOverwiteAllowed(String[] files) {
		final OverwriteDialog dialog = new OverwriteDialog(this.getShell(), files);
		getShell().getDisplay().syncExec(new Runnable() {
			
			@Override
			public void run() {
				dialog.open();
			}
		});
		return dialog.getReturnCode() == OverwriteDialog.YES_TO_ALL_INDEX;
	}

	@Override
	public boolean isPluginSelectionOptional() {
		return false;
	}
}
