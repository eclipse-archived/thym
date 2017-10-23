/******************************************************************************* 
 * Copyright (c) 2007-2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.eclipse.thym.core.jobs;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class JobUtils {

	private static final long MAX_IDLE = 20 * 60 * 1000L;
	private static final long DEFAULT_DELAY = 500;

	public static void waitForIdle() {
		waitForIdle(DEFAULT_DELAY);
	}

	public static void waitForIdle(long delay) {
		waitForIdle(delay, MAX_IDLE);
	}

	public static void waitForIdle(long delay, long maxIdle) {
		long start = System.currentTimeMillis();
		while (!isIdle()) {
			delay(delay);
			if ((System.currentTimeMillis() - start) > maxIdle) {
				Job[] jobs = Job.getJobManager().find(null);
				StringBuffer str = new StringBuffer();
				for (Job job : jobs) {
					if (job.getThread() != null && !shouldIgnoreJob(job)) {
						str.append("\n").append(job.getName()).append(" (").append(job.getClass()).append(")");
					}
				}
				if (str.length() > 0)
					throw new RuntimeException("Long running tasks detected:" + str.toString()); //$NON-NLS-1$
			}
		}
	}

	private static boolean isIdle() {
		boolean isIdle = Job.getJobManager().isIdle();
		if (!isIdle) {
			Job[] jobs = Job.getJobManager().find(null);
			for (Job job : jobs) {
				if (job.getState() == Job.SLEEPING) {
					continue;
				}
				if (!shouldIgnoreJob(job)) {
					return false;
				}
			}
		}
		return true;
	}

	// The list of non-build related long running jobs
	private static final String[] IGNORE_JOBS_NAMES = new String[] { "workbench auto-save job" };

	/**
	 * A workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=405456 in
	 * Eclipse 4.3.0M7 (since -Dorg.eclipse.ui.testsDisableWorkbenchAutoSave=true
	 * option is not yet implemented in M7)
	 *
	 * @param job
	 * @return
	 */
	private static boolean shouldIgnoreJob(Job job) {
		for (String name : IGNORE_JOBS_NAMES) {
			if (name != null && job != null && job.getName() != null && name.equalsIgnoreCase(job.getName().trim())) {
				System.out.println("Ignoring the non-build long running job: " + job.getName());
				return true;
			}
		}
		return false;
	}

	public static void delay(long waitTimeMillis) {
		Display display = Display.getCurrent();
		if (PlatformUI.isWorkbenchRunning() && display != null) {
			DisplayDelayHelper delay = new DisplayDelayHelper(waitTimeMillis);
			delay.waitForCondition(display, waitTimeMillis);
		} else { // Otherwise, perform a simple sleep.
			try {
				Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}

	public static void runDeferredEvents() {
		while (Display.getCurrent().readAndDispatch())
			;
	}
}
