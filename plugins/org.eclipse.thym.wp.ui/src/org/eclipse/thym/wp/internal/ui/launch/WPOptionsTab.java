/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.wp.internal.ui.launch;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.window.Window;
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
import org.eclipse.thym.core.HybridProjectLaunchConfigConstants;
import org.eclipse.thym.wp.core.WPCore;
import org.eclipse.thym.wp.core.vstudio.WPConstants;
import org.eclipse.thym.wp.core.vstudio.WPEmulator;
import org.eclipse.thym.wp.internal.ui.Messages;
import org.eclipse.thym.wp.internal.ui.SDKLocationHelper;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Launch configuration page for Windows Phone related settings.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WPOptionsTab extends AbstractLaunchConfigurationTab {

	private static final String DEFAULT_EMULATOR = Messages.WPOptionsTab_DefaultEmulator;

	private Text projectText;
	private Listener dirtyListener;
	private Combo devicesCombo;

	private Map<String, Integer> devices;

	private class DirtyListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}

	public WPOptionsTab() {
		this.dirtyListener = new DirtyListener();
		this.devices = new TreeMap<String, Integer>();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout(1, false));

		Group projectGroup = new Group(comp, SWT.NONE);
		projectGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		projectGroup.setText(Messages.WPOptionsTab_ProjectGroup);
		projectGroup.setLayout(new GridLayout(3, false));

		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		projectLabel.setText(Messages.WPOptionsTab_ProjectLabel);

		projectText = new Text(projectGroup, SWT.BORDER);
		projectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		projectText.addListener(SWT.Modify, dirtyListener);

		Button browseButton = new Button(projectGroup, SWT.NONE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
						getShell(), WorkbenchLabelProvider
								.getDecoratingWorkbenchLabelProvider());
				dialog.setTitle(Messages.WPOptionsTab_ProjectSelection);
				dialog.setMessage(Messages.WPOptionsTab_SelectonDesc);
				dialog.setElements(HybridCore.getHybridProjects().toArray());
				if (dialog.open() == Window.OK) {
					HybridProject project = (HybridProject) dialog
							.getFirstResult();
					projectText.setText(project.getProject().getName());
				}
			}
		});
		browseButton.setText(Messages.WPOptionsTab_BrowseLabel);

		Group emulatorGroup = new Group(comp, SWT.NONE);
		emulatorGroup.setLayout(new GridLayout(2, false));
		emulatorGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		emulatorGroup.setText(Messages.WPOptionsTab_EmulatorGroup);

		Label deviceLabel = new Label(emulatorGroup, SWT.NONE);
		deviceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		deviceLabel.setText(Messages.WPOptionsTab_DeviceName);

		devicesCombo = new Combo(emulatorGroup, SWT.READ_ONLY);
		devicesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		devicesCombo.addListener(SWT.Selection, dirtyListener);

		try {
			if (SDKLocationHelper.isSDKLocationDefined()) {
				WPEmulator emulator = new WPEmulator(WPCore.getSDKLocation());
				devices = emulator.getDevices();
				if (devices != null) {
					Set<String> names = devices.keySet();
					for (String name : names) {
						devicesCombo.add(name);
					}
				}
			}
		} catch (CoreException e) {
			// let it fall back to default
			devicesCombo.removeAll();
		}
		if (devices != null && !devices.isEmpty()) {
			devicesCombo.add(DEFAULT_EMULATOR);
		}
	}

	@Override
	public String getName() {
		return Messages.WPOptionsTab_TabName;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			if (SDKLocationHelper.defineSDKLocationIfNecessary()) {
				setErrorMessage(Messages.WPOptionsTab_SDKNotDefinedError);
			}

			String projectName = null;
			projectName = configuration.getAttribute(
					HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE,
					(String) null);
			if (projectName != null) {
				projectText.setText(projectName);
			}

			if (SDKLocationHelper.isSDKLocationDefined()) {
				try {
					int deviceId = configuration.getAttribute(
							WPConstants.ATTR_DEVICE_IDENTIFIER, 0);
					if (deviceId != -1) {
						Set<String> names = devices.keySet();
						int index = 0;
						for (String name : names) {
							if (devices.get(name) == deviceId) {
								break;
							}
							index++;
						}
						devicesCombo.select(index);
					} else {
						devicesCombo.select(devicesCombo
								.indexOf(DEFAULT_EMULATOR));
					}
				} catch (CoreException e) {
				}
			}
			setDirty(false);
		} catch (CoreException e) {
			WPCore.log(
					IStatus.ERROR,
					"Could not initialize launch configuration for Windows Phone 8 Emulator", //$NON-NLS-1$
					e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE,
				projectText.getText());
		String device = devicesCombo.getText();
		if (device != null && !device.isEmpty()) {
			int id = -1;
			if (!DEFAULT_EMULATOR.equals(device)) {
				id = devices.get(device);
			}
			configuration.setAttribute(WPConstants.ATTR_DEVICE_IDENTIFIER, id);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (devices != null && !devices.isEmpty()) {
			devicesCombo.select(devicesCombo.indexOf(DEFAULT_EMULATOR));
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		if(!System.getProperty("os.name").toLowerCase().startsWith("win")) {
			setErrorMessage("Windows emulator can run only on Windows");
			return false;
		}
		try {
			return isTabValid() && WPCore.getSDKLocation() != null
					&& super.isValid(launchConfig);
		} catch (CoreException e) {
			WPCore.log(IStatus.ERROR, "Error during SDK location validation", e); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	public boolean canSave() {
		return isTabValid();
	}

	/**
	 * Validate all fields on the tab.
	 * 
	 * @return <code>true</code> if all fields on the tab are valid; otherwise
	 *         return <code>false</code>
	 */
	private boolean isTabValid() {
		setMessage(Messages.WPOptionsTab_Description);
		setErrorMessage(null);
		// firstly check if there are emulators in the SDK
		if (devices.isEmpty()) {
			setErrorMessage(Messages.WPOptionsTab_NoEmulatorsError);
			return false;
		}
		String projectName = projectText.getText();
		if (projectName.isEmpty()) {
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		if (!project.exists()) {
			return false;
		}

		String device = devicesCombo.getText();
		if (device.isEmpty()) {
			return false;
		}
		return true;
	}

}
