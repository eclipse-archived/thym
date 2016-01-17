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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.cordova.RequirementsUtility;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class PluginUninstallAction extends Action{
	
	private static final String CONFIRM_DIALOG_TITLE = "Uninstall Cordova plug-in(s)";
	private CordovaPlugin plugin;
	public PluginUninstallAction() {
		super("Uninstall Cordova plug-in");
	}
	
	public PluginUninstallAction(CordovaPlugin cordovaPlugin) {
		this();
		this.plugin = cordovaPlugin;
	}

	@Override
	public void run() {
		final ArrayList<CordovaPlugin> pluginsToRemove = new ArrayList<CordovaPlugin>();
		if(this.plugin != null ){
			pluginsToRemove.add(plugin);
		}else{
			IStructuredSelection selection = getSelection();
			if(selection.isEmpty())
				return;
			@SuppressWarnings("rawtypes")
			Iterator it = selection.iterator();
			while(it.hasNext()){
				Object o = it.next();
				if(o instanceof CordovaPlugin ){
					pluginsToRemove.add((CordovaPlugin)o);
				}
			}
		}
		if(pluginsToRemove.isEmpty()){
			return;
		}
		
		HybridProject project = HybridProject.getHybridProject(pluginsToRemove.get(0).getFolder().getProject());
		if(!RequirementsUtility.checkCordovaRequirements(project)){
			return;
		}
	
		String message =null;
		if(pluginsToRemove.size() == 1){
			message = NLS.bind("Are you sure you want to uninstall {0} plug-in?",
					new String[]{ pluginsToRemove.get(0).getId()});
		}else{
			message = NLS.bind("Are you sure you want to uninstall the selected {0} plug-ins?",
					new String[]{Integer.toString(pluginsToRemove.size())});
		}
		
		if( !MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CONFIRM_DIALOG_TITLE, message)){
			return;
		}
		
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					try {
						for (CordovaPlugin cordovaPlugin : pluginsToRemove) {
							HybridProject project = HybridProject.getHybridProject(cordovaPlugin.getFolder().getProject());
							monitor.subTask(NLS.bind("Uninstalling {0}", cordovaPlugin.getId()));
							project.getPluginManager().unInstallPlugin(cordovaPlugin.getId(), monitor);
						}
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}

				}
			});
		} catch (InvocationTargetException e) {
			Throwable t = e;
			if(e.getTargetException() != null ){
				t =e.getTargetException();
			}
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error removing Cordova plug-in",null, 
					new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Error when removing the Cordova plug-in", t ));
		} catch (InterruptedException e) {
			HybridUI.log(IStatus.ERROR, "Error while removing a Cordova plugin " ,e);
		}
	}
	
	private IStructuredSelection getSelection(){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window != null ){
			ISelection selection = window.getSelectionService().getSelection();
			if(selection instanceof IStructuredSelection)
				return (IStructuredSelection)selection;
		}
		return StructuredSelection.EMPTY;
	}
	
}