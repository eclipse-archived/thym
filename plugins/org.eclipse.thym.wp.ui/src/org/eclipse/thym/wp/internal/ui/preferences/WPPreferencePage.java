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
package org.eclipse.thym.wp.internal.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.wp.core.vstudio.WPConstants;
import org.eclipse.thym.wp.internal.ui.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for Windows Phone 8 support.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WPPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public static final String PAGE_ID = "org.eclipse.thym.wp.ui.WPPreferencePages"; //$NON-NLS-1$

	private static class WindowsPhoneSDKDirectoryFieldEditor extends
			DirectoryFieldEditor {
		public WindowsPhoneSDKDirectoryFieldEditor(String key, String label,
				Composite fieldEditorParent) {
			super(key, label, fieldEditorParent);
			setEmptyStringAllowed(true);
		}

		protected boolean doCheckState() {
			String filename = getTextControl().getText();
			filename = filename.trim();
			if (filename.isEmpty()) {
				setMessage(Messages.WPPreferencePage_NotSpecifiedWarning,
						IStatus.WARNING);
				return true;
			} else {
				// clear the warning message
				setMessage(null, 0);
			}

			if (!filename.endsWith(File.separator)) {
				filename = filename + File.separator;
			}

			File file = new File(filename);
			if (!file.isDirectory()) {
				setErrorMessage(Messages.WPPreferencePage_NotDirectoryError);
				return false;
			}
			// check sdk_root/Libraries
			File fileToValidate = new File(file, "Libraries"); //$NON-NLS-1$
			if (!validateFile(fileToValidate)) {
				return false;
			}
			// check sdk_root/Libraries/Microsoft.Phone.Controls.dll
			fileToValidate = new File(fileToValidate,
					"Microsoft.Phone.Controls.dll"); //$NON-NLS-1$
			if (!validateFile(fileToValidate)) {
				return false;
			}
			// check sdk_root/Tools
			fileToValidate = new File(file, "Tools"); //$NON-NLS-1$
			if (!validateFile(fileToValidate)) {
				return false;
			}
			return true;
		}

		public void setValidateStrategy(int value) {
			super.setValidateStrategy(VALIDATE_ON_KEY_STROKE);
		}

		private boolean validateFile(File file) {
			if (!file.exists()) {
				setErrorMessage(Messages.WPPreferencePage_NotValidError);
				return false;
			}
			return true;
		}

		private void setMessage(String message, int status) {
			if (message != null) {
				this.getPage().setMessage(message, status);
			} else {
				this.getPage().setMessage(null);
			}
		}

	}

	public WPPreferencePage() {
		super(GRID);
		setPreferenceStore(HybridUI.getDefault().getPreferenceStore());
		setDescription(Messages.WPPreferencePage_Description);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		WindowsPhoneSDKDirectoryFieldEditor editor = new WindowsPhoneSDKDirectoryFieldEditor(
				WPConstants.WINDOWS_PHONE_SDK_LOCATION_PREF,
				Messages.WPPreferencePage_LocationLabel, getFieldEditorParent());
		addField(editor);
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing to do
	}

}