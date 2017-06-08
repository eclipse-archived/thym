/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc.
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator.EngineSearchListener;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.engine.internal.cordova.DownloadableCordovaEngine;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.PlatformImage;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.services.IEvaluationService;

import com.github.zafarkhaja.semver.Version;

public class EngineAddDialog extends TitleAreaDialog{

	private static final int TREE_WIDTH = 450;
	private static final int TREE_HEIGHT = 175;
	
    private CheckboxTreeViewer platformList;
    private CordovaEngineProvider engineProvider;
    private Set<HybridMobileEngine> engines;
    private Button nightlyBuilds;
    private Set<HybridMobileEngine> newEngines;
    private Text customEngineLocation;
    private CheckboxTreeViewer customEnginesList;
    private Set<HybridMobileEngine> customEngines = new HashSet<>();
    
    private class CustomEngineContentProvider implements ITreeContentProvider{

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

		@Override
		public Object[] getElements(Object inputElement) {
			return customEngines.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

    }
    
    private class CustomEngineLabelProvider extends LabelProvider implements ITableLabelProvider{

    	
    	@Override
    	public Image getImage(Object element) {
    		return null;
    	}
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof HybridMobileEngine){
				HybridMobileEngine engine = (HybridMobileEngine)element;
				return engine.getName()+"@"+engine.getSpec();
			}
			return null;
		}
		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
    }



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
        private boolean nightlyBuilds = false;

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if(newInput != null && newInput instanceof List){
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
					if(nightlyBuilds) {
						platformDownloadables.add(engine);
					} else if (!engine.isNightlyBuild()){
						platformDownloadables.add(engine);
					}
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
		
		public void showNightlyBuilds(boolean nightlyBuilds){
			this.nightlyBuilds = nightlyBuilds;
		}

    }

    public EngineAddDialog(Shell parent) {
        super(parent);
        setShellStyle(getShellStyle()| SWT.SHEET);
        newEngines  = new LinkedHashSet<>();
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        toggleOKButton(false);
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("Add Hybrid Mobile Engine");
        setMessage("Add a new engine version");
        parent.getShell().setText("Add Hybrid Mobile Engine");

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).spacing(LayoutConstants.getSpacing()).numColumns(1).applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
        
        Group downloadableGroup = new Group(composite, SWT.NONE);
        downloadableGroup.setText("Downloadable engines");
        
        GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).spacing(LayoutConstants.getSpacing()).numColumns(1).applyTo(downloadableGroup);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(downloadableGroup);

        Tree tree = new Tree(downloadableGroup, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).minSize(new Point(TREE_WIDTH, TREE_HEIGHT)).applyTo(tree);
        tree.setHeaderVisible(false);
        tree.setLinesVisible(true);

        platformList = new CheckboxTreeViewer(tree);
        final DownloadableVersionsContentProvider provider = new DownloadableVersionsContentProvider();
        platformList.setContentProvider(provider);
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

        
        nightlyBuilds = new Button(downloadableGroup, SWT.CHECK);
        nightlyBuilds.setText("Show nightly builds");
        nightlyBuilds.setSelection(false);
        nightlyBuilds.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		provider.showNightlyBuilds(nightlyBuilds.getSelection());
        		platformList.refresh();
        		validate();
        	}
		});
        
        final ExpandableComposite ex = new ExpandableComposite(composite,ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		ex.setText("Local engines"); //$NON-NLS-1$
		
		GridLayoutFactory.fillDefaults().applyTo(ex);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(ex);
        
        ex.addExpansionListener(new ExpansionAdapter() {
        	
        	@Override
        	public void expansionStateChanged(ExpansionEvent e) {
        		initializeBounds();
        		//getShell().pack();
        		//getShell().layout();
        	}
        	
        });
        
		
		
        
        Group customEngineGroup = new Group(ex, SWT.NONE);
        customEngineGroup.setText("Local engines");
        
        ex.setClient(customEngineGroup);
        
        GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).spacing(LayoutConstants.getSpacing()).numColumns(1).applyTo(customEngineGroup);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(customEngineGroup);
        
        customEngineLocation = new Text(customEngineGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(customEngineLocation);
        customEngineLocation.setMessage("Type location or use Browse button");
        
        final Composite customEnginesComposite = new Composite(customEngineGroup, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(customEnginesComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(customEnginesComposite);
        
        Button browseBtn = new Button(customEnginesComposite, SWT.PUSH);
		browseBtn.setText("Browse...");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(browseBtn);
		browseBtn.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setMessage("Select the directory in which to search for hybrid mobile engines");
				directoryDialog.setText("Search for Hybrid Mobile Engines");
				String pathStr = directoryDialog.open();
				handleSearch(pathStr);
			}
		});
		
		Button refreshBtn = new Button(customEnginesComposite, SWT.PUSH);
		refreshBtn.setText("Refresh");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(browseBtn);
		refreshBtn.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				handleSearch(customEngineLocation.getText());
			}
		});
        
		Tree customEnginesTree = new Tree(customEnginesComposite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(2, 1).grab(true, true).minSize(new Point(TREE_WIDTH, TREE_HEIGHT)).applyTo(customEnginesTree);
        customEnginesTree.setHeaderVisible(false);
        customEnginesTree.setLinesVisible(true);
        
        customEnginesList = new CheckboxTreeViewer(customEnginesTree);
        customEnginesList.setContentProvider(new CustomEngineContentProvider());
        customEnginesList.setLabelProvider(new CustomEngineLabelProvider());
        customEnginesList.setInput(customEngines);
        customEnginesList.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				HybridMobileEngine engine = (HybridMobileEngine)event.getElement();
				if(event.getChecked()){
					newEngines.add(engine);
				} else {
					newEngines.remove(engine);
				}
				validate();
				
			}
		});
        
        engineProvider = CordovaEngineProvider.getInstance();
        try {
            platformList.setInput(engineProvider.getDownloadableEngines());
        } catch (CoreException e) {
        	platformList.setInput(new Object());//Set the input to allow platforms to be displayed
            setErrorMessage(e.getMessage());
            HybridUI.log(IStatus.ERROR, "Unable to retrieve the downloadable versions list", e);
        }
        return composite;
    }
    
    private void handleSearch(final String pathStr) {
		if (pathStr == null)
			return;
		
		final IPath path = new Path(pathStr);
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		dialog.setBlockOnOpen(false);
		dialog.setCancelable(true);
		dialog.open();
		final EngineSearchListener listener = new EngineSearchListener() {
			
			@Override
			public void engineFound(final HybridMobileEngine engine) {
				getShell().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						customEngines.add(engine);
						customEnginesList.refresh();
						validate();	
					}
				});
			}
		};
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				List<HybridMobileEngineLocator> locators = HybridCore.getEngineLocators();
				for (HybridMobileEngineLocator locator : locators) {
					locator.searchForRuntimes(path, listener, monitor);
				}
			}
		};
		
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				if(e.getTargetException() instanceof CoreException ){
					StatusManager.handle((CoreException) e.getTargetException());
				}else{
					ErrorDialog.openError(getShell(), "Local Engine Search Error",null, 
							new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Error when searching for local hybrid mobile engines", e.getTargetException() ));
				}
			}
		} catch (InterruptedException e) {
			HybridUI.log(IStatus.ERROR, "Search for Cordova Engines error", e);
		}
	}

    private boolean isInstalled(String version, String platformId){
        if(version == null || platformId == null ) return false;
        if(engines == null ){
            engines = engineProvider.getAvailableEngines();
        }
        for (HybridMobileEngine engine : engines) {
            if(engine.getSpec().equals(version)
                    && engine.getName().equals(platformId)){
                return true;
            }
        }
        return false;
    }

    private void validate() {
    	toggleOKButton(false);
    	setErrorMessage(null);
    	
        Object[] checked = platformList.getCheckedElements();
        if(checked != null && checked.length > 0 ){
            toggleOKButton(true);
            return;
        }
        checked = customEnginesList.getCheckedElements();
        if(checked != null && checked.length > 0 ){
            toggleOKButton(true);
            return;
        }
    }


    private void toggleOKButton(boolean state) {
        Button ok = getButton(IDialogConstants.OK_ID);
        if(ok != null && !ok.isDisposed()){
            ok.setEnabled(state);
        }
    }
    
    public Set<HybridMobileEngine> getEngines(){
    	return newEngines;
    }
    
    @Override
    protected void okPressed() {
        DownloadableCordovaEngine[] downloads = getDownloadsList();
        CordovaEngineProvider engineProvider = CordovaEngineProvider.getInstance();
        for(DownloadableCordovaEngine e: downloads){
        	HybridMobileEngine engine = engineProvider.createEngine(e.getPlatformId(), e.getVersion());
        	newEngines.add(engine);
        }
        super.okPressed();
    }

	private DownloadableCordovaEngine[] getDownloadsList() {
		final Object[] checked = platformList.getCheckedElements();
        final DownloadableCordovaEngine[] downloads = new DownloadableCordovaEngine[checked.length];
        for (int i = 0; i < checked.length; i++) {
            DownloadableCordovaEngine dce = (DownloadableCordovaEngine) checked[i];
            downloads[i] = dce;
        }
		return downloads;
	}

}
