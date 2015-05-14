/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040
 *******************************************************************************/
package org.eclipse.thym.ui.plugins.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Based on {@link org.eclipse.ui.internal.progress.ProgressInfoItem}.
 * 
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public abstract class ControlListItem<T> extends Composite {


	interface IndexListener {

		/**
		 * Select the item previous to the receiver.
		 */
		public void selectPrevious();

		/**
		 * Select the next previous to the receiver.
		 */
		public void selectNext();

		/**
		 * Select the receiver.
		 */
		public void select();

		public void open();

	}

	IndexListener indexListener;
	private boolean selected;
	private final MouseAdapter mouseListener;
	protected boolean isShowing = false;

	/**
	 * Create a new instance of the receiver with the specified parent, style and info object/
	 * 
	 * @param parent
	 * @param style
	 * @param element
	 */
	public ControlListItem(Composite parent, int style, T element) {
		super(parent, style | SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED );
		Assert.isNotNull(element);
		super.setData(element);
		setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		mouseListener = doCreateMouseListener();
		registerChild(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getData() {
		return (T) super.getData();
	}

	@Override
	public void setData(Object data) {
		throw new IllegalArgumentException();
	}

	private MouseAdapter doCreateMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (indexListener != null) {
					if (e.count == 2) {
						indexListener.open();
					} else {
						indexListener.select();
					}
				}
			}
		};
	}


	protected void registerChild(Control child) {
		child.addMouseListener(mouseListener);

	}

	/**
	 * Refresh the contents of the receiver.
	 */
	protected abstract void refresh();


	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
		Control[] children = getChildren();
		for (Control child : children) {
			child.setForeground(color);
		}
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		Control[] children = getChildren();
		for (Control child : children) {
			child.setBackground(color);
		}
	}

	/**
	 * Set the selection colors.
	 * 
	 * @param select
	 *            boolean that indicates whether or not to show selection.
	 */
	public void setSelected(boolean select) {
		selected = select;
	}

	/**
	 * Set the listener for index changes.
	 * 
	 * @param indexListener
	 */
	void setIndexListener(IndexListener indexListener) {
		this.indexListener = indexListener;
	}

	/**
	 * Return whether or not the receiver is selected.
	 * 
	 * @return boolean
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set whether or not the receiver is being displayed based on the top and bottom of the currently visible area.
	 * 
	 * @param top
	 * @param bottom
	 */
	void setDisplayed(int top, int bottom) {
		int itemTop = getLocation().y;
		int itemBottom = itemTop + getBounds().height;
		setDisplayed(itemTop <= bottom && itemBottom > top);

	}

	/**
	 * Set whether or not the receiver is being displayed
	 * 
	 * @param displayed
	 */
	private void setDisplayed(boolean displayed) {
		// See if this element has been turned off
		boolean refresh = (isShowing != displayed);
		isShowing = displayed;
		if (refresh) {
			refresh();
		}
	}

}
