/*******************************************************************************
  * Copyright (c) 2010 - 2015 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
package org.eclipse.thym.core.jobs;

public class DisplayDelayHelper extends DisplayHelper {

	private long currentTime;
	private long delay;

	public DisplayDelayHelper(long delay) {
		super();
		this.currentTime = System.currentTimeMillis();
		this.delay = delay;
	}

	@Override
	protected boolean condition() {
		return System.currentTimeMillis() > currentTime + delay;
	}

}