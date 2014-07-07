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
package org.eclipse.thym.ui.wizard.export;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;

public class CheckboxTableSelectionGroup extends Group {
	
	private CheckboxTableViewer tableViewer;

	public CheckboxTableSelectionGroup(Composite parent, int style) {
		super(parent, style);
		createGroup();
	}
	
	private void createGroup() {
		setLayout(new GridLayout(2, false));
		tableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.FULL_SELECTION);
		Table table =tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				sendSelectionEvent();
			}
		});
		
		Composite projectButtons = new Composite(this, SWT.NONE);
		projectButtons.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		RowLayout rl_projectButtons = new RowLayout(SWT.VERTICAL);
		rl_projectButtons.center = true;
		rl_projectButtons.fill = true;
		rl_projectButtons.justify = true;
		rl_projectButtons.pack = false;
		projectButtons.setLayout(rl_projectButtons);
		
		Button btnSelectAll = new Button(projectButtons, SWT.NONE);
		btnSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setAllChecked(true);
				sendSelectionEvent();
			}
		});
		btnSelectAll.setText("Select All");
		
		Button btnDeselectAll = new Button(projectButtons, SWT.NONE);
		btnDeselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setAllChecked(false);
				sendSelectionEvent();
			}
		});
		btnDeselectAll.setText("Deselect All");
	}

	@Override
	protected void checkSubclass() {
	}
	
	private void sendSelectionEvent(){
		Event e = new Event();
		e.type =SWT.Selection;
		e.widget =this;
		e.data = tableViewer.getCheckedElements();
		notifyListeners(SWT.Selection, e);
	}
	
	public CheckboxTableViewer getTableViewer(){
		return tableViewer;
	}

}
