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
package org.eclipse.thym.core.plugin.registry;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.thym.core.internal.util.FileUtils;
import org.eclipse.thym.core.internal.util.TarException;

public class PluginReceiver implements IFileTransferListener{
	private final File cacheDirectory;
	private final Object lock;
	private final IProgressMonitor monitor;
	private int percentComplete;

	
	public PluginReceiver(File directory, IProgressMonitor monitor,Object lock) {
		this.cacheDirectory = directory;
		this.lock = lock;
		this.monitor = monitor;
	}

	@Override
	public void handleTransferEvent(IFileTransferEvent event) {
		File tarFile = new File(cacheDirectory,"package.tgz");

		 if (event instanceof IIncomingFileTransferReceiveStartEvent) {
			 IIncomingFileTransferReceiveStartEvent startEvent = (IIncomingFileTransferReceiveStartEvent) event;
			 try {
				 if(!cacheDirectory.exists())
					 cacheDirectory.mkdirs();
				 
				startEvent.receive(tarFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }else if(event instanceof IIncomingFileTransferReceiveDataEvent){
			 IIncomingFileTransferReceiveDataEvent dataEvent = (IIncomingFileTransferReceiveDataEvent) event;
			 int completed = (int) (dataEvent.getSource().getPercentComplete() *100);
//			 monitor.worked((percentComplete - completed));
			 percentComplete = completed;
		 }else if(event instanceof IIncomingFileTransferReceiveDoneEvent ){
			 try {
				 IIncomingFileTransferReceiveDoneEvent doneEvent = (IIncomingFileTransferReceiveDoneEvent) event;
				 Exception ex = doneEvent.getException();
				 
				 if(ex == null){
					FileUtils.untarFile(tarFile, cacheDirectory);
				 }else{
					 ex.printStackTrace();
				 }
				 
			 }catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TarException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					synchronized (lock) {
						lock.notifyAll();
					}
			 }
		 }
	
		
	}

}
