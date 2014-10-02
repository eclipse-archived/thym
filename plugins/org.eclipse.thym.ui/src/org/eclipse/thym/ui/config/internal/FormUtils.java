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

import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class FormUtils {

	// FORM BODY
	public static final int FORM_BODY_MARGIN_TOP = 12;
	public static final int FORM_BODY_MARGIN_BOTTOM = 12;
	public static final int FORM_BODY_MARGIN_LEFT = 6;
	public static final int FORM_BODY_MARGIN_RIGHT = 6;
	public static final int FORM_BODY_HORIZONTAL_SPACING = 20;
	public static final int FORM_BODY_VERTICAL_SPACING = 17;
	public static final int FORM_BODY_MARGIN_HEIGHT = 0;
	public static final int FORM_BODY_MARGIN_WIDTH = 0;

	// FORM PANE
	public static final int FORM_PANE_MARGIN_TOP = 0;
	public static final int FORM_PANE_MARGIN_BOTTOM = 0;
	public static final int FORM_PANE_MARGIN_LEFT = 0;
	public static final int FORM_PANE_MARGIN_RIGHT = 0;
	public static final int FORM_PANE_HORIZONTAL_SPACING = FORM_BODY_HORIZONTAL_SPACING;
	public static final int FORM_PANE_VERTICAL_SPACING = FORM_BODY_VERTICAL_SPACING;
	public static final int FORM_PANE_MARGIN_HEIGHT = 0;
	public static final int FORM_PANE_MARGIN_WIDTH = 0;
	
	// SECTION
	public static final int SECTION_HEADER_VERTICAL_SPACING = 6;
	
	// CLEAR
	private static final int DEFAULT_CLEAR_MARGIN = 2;
	public static final int CLEAR_MARGIN_TOP = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_MARGIN_BOTTOM = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_MARGIN_LEFT = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_MARGIN_RIGHT = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_HORIZONTAL_SPACING = 0;
	public static final int CLEAR_VERTICAL_SPACING = 0;
	public static final int CLEAR_MARGIN_HEIGHT = 0;
	public static final int CLEAR_MARGIN_WIDTH = 0;
	

	public static TableWrapLayout createFormTableWrapLayout(int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = FORM_BODY_MARGIN_TOP;
		layout.bottomMargin = FORM_BODY_MARGIN_BOTTOM;
		layout.leftMargin = FORM_BODY_MARGIN_LEFT;
		layout.rightMargin = FORM_BODY_MARGIN_RIGHT;

		layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
		layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;

		layout.makeColumnsEqualWidth = true;
		layout.numColumns = numColumns;
		return layout;
	}

	public static TableWrapLayout createFormPaneTableWrapLayout(int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();

		layout.topMargin = FORM_PANE_MARGIN_TOP;
		layout.bottomMargin = FORM_PANE_MARGIN_BOTTOM;
		layout.leftMargin = FORM_PANE_MARGIN_LEFT;
		layout.rightMargin = FORM_PANE_MARGIN_RIGHT;

		layout.horizontalSpacing = FORM_PANE_HORIZONTAL_SPACING;
		layout.verticalSpacing = FORM_PANE_VERTICAL_SPACING;

		layout.numColumns = numColumns;

		return layout;
	}
	
	
	public static TableWrapLayout createClearTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
	TableWrapLayout layout = new TableWrapLayout();
	
	layout.topMargin = CLEAR_MARGIN_TOP;
	layout.bottomMargin = CLEAR_MARGIN_BOTTOM;
	layout.leftMargin = CLEAR_MARGIN_LEFT;
	layout.rightMargin = CLEAR_MARGIN_RIGHT;
	
	layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
	layout.verticalSpacing = CLEAR_VERTICAL_SPACING;
	
	layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
	layout.numColumns = numColumns;
	
	return layout;
	}

}
