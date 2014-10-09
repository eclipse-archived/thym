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
package org.eclipse.thym.ios.ui;

import static org.eclipse.thym.core.HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE;
import static org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants.ATTR_DEVICE_IDENTIFIER;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.ios.core.simulator.IOSDevice;
import org.eclipse.thym.ios.core.simulator.IOSSimulator;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class IOSSimOptionsTab extends AbstractLaunchConfigurationTab {
	private Text textProject;
	private Listener dirtyFlagListener;
	private Combo comboSDKVer;
	private ComboViewer comboViewer;
	
	private final class SDKContentProvider implements
			IStructuredContentProvider {
		private IOSDevice[] simulators;
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.simulators = (IOSDevice[]) newInput;
		}

		@Override
		public void dispose() {
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if(simulators == null ){
				return new Object[0];
			}
			return simulators;
		}
	}

	private class DirtyListener implements Listener{
		@Override
		public void handleEvent(Event event) {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}
	
	public IOSSimOptionsTab() {
		this.dirtyFlagListener = new DirtyListener();
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout(1, false));
		
		Group grpProject = new Group(comp, SWT.NONE);
		grpProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpProject.setText("Project");
		grpProject.setLayout(new GridLayout(3, false));
		
		Label lblProject = new Label(grpProject, SWT.NONE);
		lblProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProject.setText("Project:");
		
		textProject = new Text(grpProject, SWT.BORDER);
		textProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textProject.addListener(SWT.Modify, dirtyFlagListener);
		
		
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
		
		Group grpSimulator = new Group(comp, SWT.NONE);
		grpSimulator.setLayout(new GridLayout(2, false));
		grpSimulator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpSimulator.setText("Simulator");
		
		Label lblSdkVersion = new Label(grpSimulator, SWT.NONE);
		lblSdkVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSdkVersion.setText("Device:");
		
		comboSDKVer = new Combo(grpSimulator, SWT.READ_ONLY);
		
		comboSDKVer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboSDKVer.addListener(SWT.Selection, dirtyFlagListener);
	
		comboViewer = new ComboViewer(comboSDKVer);
		comboViewer.setContentProvider(new SDKContentProvider());
		comboViewer.setLabelProvider( new LabelProvider() {
			@Override
			public String getText(Object element) {
				IOSDevice device = (IOSDevice) element;
				return NLS.bind("{0} ({1})", new String[]{device.getDeviceName(), device.getiOSName()});
			}
		});
		comboViewer.setInput(getSimulatorDevices());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		String projectName =null;
		try {
			projectName = configuration.getAttribute(ATTR_BUILD_SCOPE, (String)null);
		} catch (CoreException e) {
			// not handled
		}
		if(projectName != null){
			textProject.setText(projectName);
		}
		
		
		try{ 
			String deviceId = configuration.getAttribute(ATTR_DEVICE_IDENTIFIER,new String());
			SDKContentProvider contentProvider = (SDKContentProvider) comboViewer.getContentProvider();
			//it is possible that the selected SDK version is no longer available
			// it can be either uninstalled or the launch config is shared. fall back to default
			comboSDKVer.select(0);
			if(contentProvider.simulators != null){
				for (IOSDevice sim : contentProvider.simulators) {
					if(sim.getDeviceId().equals(deviceId)){
						comboViewer.setSelection(new StructuredSelection(sim));
					}
				}
			}
		}catch(CoreException ce){
			//ignored
		}
		

				
		setDirty(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();
		if(!selection.isEmpty()){
			IOSDevice selectedDevice = (IOSDevice) selection.getFirstElement();
			configuration.setAttribute(ATTR_DEVICE_IDENTIFIER, selectedDevice.getDeviceId());
		}
	}

	@Override
	public String getName() {
		return "Simulator";
	}
	
	private IOSDevice[] getSimulatorDevices() {
		try{
			List<IOSDevice> devices = IOSSimulator.listDevices(new NullProgressMonitor());
			return devices.toArray(new IOSDevice[devices.size()]);
		}
		catch (CoreException e) {
				return new IOSDevice[0];
		}
	}
	
}
