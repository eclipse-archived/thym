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
package org.eclipse.thym.ui.internal.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.engine.internal.cordova.DownloadableCordovaEngine;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.PlatformImage;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.github.zafarkhaja.semver.Version;

public class EngineDownloadDialog extends TitleAreaDialog{

	private static final int TREE_WIDTH = 350;
	private static final int TREE_HEIGHT = 175;
	
    private CheckboxTreeViewer platformList;
    private CordovaEngineProvider engineProvider;
    private List<HybridMobileEngine> engines;
    private ProgressMonitorPart progressMonitorPart;


    private class DownloadableEngineLabelProvider extends LabelProvider implements ITableLabelProvider{

    	
    	@Override
    	public Image getImage(Object element) {
    		return getColumnImage(element, 0);
    	}
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if(element instanceof PlatformSupport){
				PlatformSupport platform = (PlatformSupport)element;
				return PlatformImage.getImageFor(PlatformImage.ATTR_PLATFORM_SUPPORT, platform.getID());
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof PlatformSupport){
				return ((PlatformSupport)element).getPlatform();
			}
			if(element instanceof DownloadableCordovaEngine){
				return ((DownloadableCordovaEngine) element).getVersion();
			}
			return null;
		}
		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
    }

    private class DownloadableEngineComparator extends ViewerComparator implements Comparator<DownloadableCordovaEngine>{

    	@Override
    	public int compare(Viewer viewer, Object e1, Object e2) {
    		if(e1 instanceof DownloadableCordovaEngine && e2 instanceof DownloadableCordovaEngine){
    			DownloadableCordovaEngine o1 = (DownloadableCordovaEngine) e1;
    			DownloadableCordovaEngine o2 = (DownloadableCordovaEngine) e2;
    			return compare(o1, o2);
    		}
    		return 1;
		}

		@Override
		public int compare(DownloadableCordovaEngine o1, DownloadableCordovaEngine o2) {
			Version v1 = Version.valueOf(o1.getVersion());
			Version v2 = Version.valueOf(o2.getVersion());
			//Make it descending switch v1 to v2
			return v2.compareTo(v1);
		}

    }

    private class InstalledVersionsFilter extends ViewerFilter{
        @Override
        public boolean select(Viewer viewer, Object parentElement,
                Object element) {
        	if(element instanceof DownloadableCordovaEngine ){
        		DownloadableCordovaEngine e = (DownloadableCordovaEngine)element;
        		return !isInstalled(e.getVersion(), e.getPlatformId());
        	}
        	return true;
        }
    }



    private class DownloadableVersionsContentProvider implements ITreeContentProvider{
        private DownloadableCordovaEngine[] downloadables;
        private PlatformSupport[] platforms;

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if(newInput != null ){
                @SuppressWarnings("unchecked")
                List<DownloadableCordovaEngine> list = (List<DownloadableCordovaEngine>)newInput;
                downloadables = list.toArray(new DownloadableCordovaEngine[list.size()]);
            }else{
                downloadables =  null;
            }

        }

		@Override
		public Object[] getElements(Object inputElement) {
			if(platforms == null ){
				List<PlatformSupport> allPlatforms = HybridCore.getPlatformSupports();
				ArrayList<PlatformSupport> elements = new ArrayList<PlatformSupport>();
				IEvaluationService service = (IEvaluationService)PlatformUI.getWorkbench().getService(IEvaluationService.class);
				for (PlatformSupport generator : allPlatforms) {
					try {
						if(generator.isEnabled(service.getCurrentState())){
							elements.add(generator);
						}
						
					} catch (CoreException e) {
						HybridUI.log(IStatus.ERROR, "Error filtering objects", e);
					}
				}
				platforms = elements.toArray(new PlatformSupport[elements.size()]);
			}
			return platforms;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if(downloadables == null || !hasChildren(parentElement)){
				return null;
			}
			PlatformSupport platform = (PlatformSupport) parentElement;
			ArrayList<DownloadableCordovaEngine> platformDownloadables = new ArrayList<DownloadableCordovaEngine>();
			for (DownloadableCordovaEngine engine : downloadables) {
				if(engine.getPlatformId().equals(platform.getPlatformId())){
					platformDownloadables.add(engine);
				}
			}
			return platformDownloadables.toArray(new DownloadableCordovaEngine[platformDownloadables.size()]);
		}

		@Override
		public Object getParent(Object element) {
			if(platforms != null && element instanceof DownloadableCordovaEngine){
				DownloadableCordovaEngine engine = (DownloadableCordovaEngine) element;
				for (PlatformSupport platformSupport : platforms) {
					if(engine.getPlatformId().equals(platformSupport.getPlatformId())){
						return platformSupport;
					}
				}
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return element instanceof PlatformSupport;
		}

    }

    public EngineDownloadDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle()| SWT.SHEET);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        toggleOKButton(false);
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("Download Hybrid Mobile Engine");
        setMessage("Download a new engine version");
        parent.getShell().setText("Download Hybrid Mobile Engine");

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).spacing(LayoutConstants.getSpacing()).numColumns(1).applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

        Tree tree = new Tree(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).minSize(new Point(TREE_WIDTH, TREE_HEIGHT)).applyTo(tree);
        tree.setHeaderVisible(false);
        tree.setLinesVisible(true);

        platformList = new CheckboxTreeViewer(tree);
        platformList.setContentProvider(new DownloadableVersionsContentProvider());
        platformList.setLabelProvider(new DownloadableEngineLabelProvider());
        platformList.setComparator(new DownloadableEngineComparator());
        platformList.addFilter(new InstalledVersionsFilter());
        
        platformList.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
            	if(event.getElement() instanceof PlatformSupport ){
            		platformList.setChecked(event.getElement(), false);
            	}
                validate();
            }
        });

        createProgressMonitorPart(composite);
        engineProvider = new CordovaEngineProvider();
        try {
            platformList.setInput(engineProvider.getDownloadableVersions());
        } catch (CoreException e) {
            setErrorMessage("Unable to retrieve the downloadable versions list, please try again later.");
            HybridUI.log(IStatus.ERROR, "Unable to retrieve the downloadable versions list", e);
        }
        return composite;
    }

    private void createProgressMonitorPart(final Composite composite) {
        progressMonitorPart = new ProgressMonitorPart(composite, new GridLayout());
        progressMonitorPart.setVisible(true);
        GridDataFactory.fillDefaults().applyTo(progressMonitorPart);
    }

    private boolean isInstalled(String version, String platformId){
        if(version == null || platformId == null ) return false;
        if(engines == null ){
            engines = engineProvider.getAvailableEngines();
        }
        for (HybridMobileEngine engine : engines) {
            if(engine.getVersion().equals(version)
                    && engine.getId().equals(platformId)){
                return true;
            }
        }
        return false;
    }

    private void validate() {
        Object[] checked = platformList.getCheckedElements();
        if(checked == null || checked.length == 0 ){
            toggleOKButton(false);
            return;
        }
        setErrorMessage(null);
        toggleOKButton(true);
    }


    private void toggleOKButton(boolean state) {
        Button ok = getButton(IDialogConstants.OK_ID);
        if(ok != null && !ok.isDisposed()){
            ok.setEnabled(state);
        }
    }

    @Override
    protected void okPressed() {
        final Object[] checked = platformList.getCheckedElements();
        final DownloadableCordovaEngine[] downloads = new DownloadableCordovaEngine[checked.length];
        for (int i = 0; i < checked.length; i++) {
            DownloadableCordovaEngine dce = (DownloadableCordovaEngine) checked[i];
            downloads[i] = dce;
        }
        run(new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException,
                    InterruptedException {
                getShell().getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        Button okButton = getButton(IDialogConstants.OK_ID);
                        okButton.setEnabled(false);
                    }
                });
                engineProvider.downloadEngine(downloads, monitor);
            }
        });
        super.okPressed();
    }

    private void run(IRunnableWithProgress runnable) {

        progressMonitorPart.attachToCancelComponent(getButton(IDialogConstants.CANCEL_ID));

        try {
            ModalContext.run(runnable, true, progressMonitorPart, getShell().getDisplay());

        } catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                if(e.getTargetException() instanceof CoreException ){
                    StatusManager.handle((CoreException) e.getTargetException());
                }else{
                    ErrorDialog.openError(getShell(), "Error downloading engine",null,
                            new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Error downloading the engine", e.getTargetException() ));
                }
            }
        } catch (InterruptedException e) {
            throw new OperationCanceledException();
        }
        progressMonitorPart.removeFromCancelComponent(getButton(IDialogConstants.CANCEL_ID));

    }

}
