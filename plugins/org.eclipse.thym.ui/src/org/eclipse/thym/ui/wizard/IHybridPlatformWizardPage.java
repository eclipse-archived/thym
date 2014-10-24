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
package org.eclipse.thym.ui.wizard;

import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.thym.ui.internal.wizard.PlatformPageWizard;
/**
 * Interface for pages for {@link PlatformPageWizard}s.
 * <p>
 * Clients should implement this interface and use the
 * <code>org.eclipse.thym.ui.platformWizardPage</code> extension
 * ppint to contribute a platform specific page to a {@link PlatformPageWizard}
 * </p>
 *
 * @see PlatformPageWizard
 *
 * @author Gorkem Ercan
 *
 */
public interface IHybridPlatformWizardPage extends IWizardPage{

	/**
	 * Returns the values collected by this wizard page.
	 *
	 * @return key value map
	 */
	public Map<String, Object> getValues();

}
