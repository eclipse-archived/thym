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
package org.eclipse.thym.core.engine.internal.cordova;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IIncomingFileTransfer;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.internal.util.TarException;

public class EngineDownloadReceiver implements IFileTransferListener {
	 private File folder;
	 private File tempFile;
	 private IProgressMonitor monitor;
	 private int percentComplete;
	 private Object lock;
	
	public EngineDownloadReceiver(String version, String platformId, Object lock, IProgressMonitor pm ){
		 folder = new File(CordovaEngineProvider.getLibFolder().toFile(),platformId+"/"+CordovaEngineProvider.CORDOVA_ENGINE_ID+"/"+version);
		 tempFile = new File(folder, platformId+"_"+version+"_"+"engine.tgz");
		 this.monitor = pm;
		 this.lock = lock;
	}

	@Override
	public void handleTransferEvent(IFileTransferEvent event) {
		
		 if (event instanceof IIncomingFileTransferReceiveStartEvent) {
			 IIncomingFileTransferReceiveStartEvent startEvent = (IIncomingFileTransferReceiveStartEvent) event;
			 handleStart(startEvent);
		 
		 }else if(event instanceof IIncomingFileTransferReceiveDataEvent){
			 IIncomingFileTransferReceiveDataEvent dataEvent = (IIncomingFileTransferReceiveDataEvent) event;
			 handleDataReceived(dataEvent);

		 }
		 else if(event instanceof IIncomingFileTransferReceiveDoneEvent ){
			 IIncomingFileTransferReceiveDoneEvent doneEvent = (IIncomingFileTransferReceiveDoneEvent) event; 
			 handleDone(doneEvent);
				 
		 }

	}

	private void handleDataReceived(
			IIncomingFileTransferReceiveDataEvent dataEvent) {
		 IIncomingFileTransfer source = dataEvent.getSource();
		 if(monitor.isCanceled()){
			 source.cancel();
			 return;
		 }
		 int completed = (int) (source.getPercentComplete() *100);
		 if(completed > 0 ){//supports reporting progress
			 int worked = percentComplete - completed;
			 if(worked > 0){
				 monitor.worked(worked);
			 }
			 percentComplete = completed;
		 }
	}

	private void handleDone(IIncomingFileTransferReceiveDoneEvent doneEvent) {
		try {
			if(doneEvent.getException() != null )
				return;
			
				org.eclipse.thym.core.internal.util.FileUtils.untarFile(tempFile, folder);
				File[] files = folder.listFiles();
				for (int i = 0; i < files.length; i++) {
					if(files[i].isDirectory()){
						FileUtils.copyDirectory(files[i], folder);
						FileUtils.deleteQuietly(files[i]);
						break;
					}
				}
				FileUtils.deleteQuietly(tempFile);
			} catch (IOException e) {
				HybridCore.log(IStatus.ERROR, "Error while saving downlaoded engine", e);
			} catch (TarException e) {
				HybridCore.log(IStatus.ERROR, "Error while extracting downlaoded engine ", e);
			}
			finally{
				synchronized (lock) {
					lock.notifyAll();
				}
				monitor.done();
			}
	}

	private void handleStart(IIncomingFileTransferReceiveStartEvent startEvent) {
		monitor.beginTask("Downloading platform", 100);
		if(folder.isDirectory()){
			FileUtils.deleteQuietly(folder);
		}
		try {
			if(monitor.isCanceled()){
				return;
			}
			folder.mkdirs();
			startEvent.receive(tempFile);
		} catch (IOException e) {
			HybridCore.log(IStatus.ERROR, "Error starting engine download", e);
		}
	}
}
