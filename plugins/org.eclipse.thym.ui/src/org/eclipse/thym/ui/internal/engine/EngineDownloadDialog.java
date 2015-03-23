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
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.engine.internal.cordova.DownloadableCordovaEngine;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.projectGenerator.ProjectGeneratorContentProvider;
import org.eclipse.thym.ui.internal.projectGenerator.ProjectGeneratorLabelProvider;
import org.eclipse.thym.ui.internal.status.StatusManager;

import com.github.zafarkhaja.semver.Version;

public class EngineDownloadDialog extends TitleAreaDialog{

	private static final int TABLE_WIDTH = 350;
	private static final int TABLE_HEIGHT = 100;
	
    private ComboViewer versionViewer;
    private CheckboxTableViewer platformList;
    private CordovaEngineProvider engineProvider;
    private List<HybridMobileEngine> engines;
    private ProgressMonitorPart progressMonitorPart;


    private class DownloadableEngineLabelProvider extends BaseLabelProvider implements ILabelProvider{

        public String getText(Object element) {
            DownloadableCordovaEngine engine = (DownloadableCordovaEngine) element;
            return engine == null ? "" : engine.getVersion();
        }

        @Override
        public Image getImage(Object element) {
            return null;
        }
    }

    private class VersionStringComparator implements Comparator<String>{

        @Override
        public int compare(String o1, String o2) {
            Version version1 = Version.valueOf(o1);
            Version version2 = Version.valueOf(o2);
            //This is reversed intentionally to sort the
            //latest version to the top
            return version2.compareTo(version1);
        }

    }

    private class ContentProviderSupportFilter extends ViewerFilter{

        @Override
        public boolean select(Viewer viewer, Object parentElement,
                Object element) {
            PlatformSupport gen = (PlatformSupport) element;
            String version = (String) parentElement;

            return engineProvider.isSupportedPlatform(version, gen.getPlatformId())
                    && !isInstalled(version, gen.getPlatformId());
        }
    }

    private class PlatformsLabelProvider extends ProjectGeneratorLabelProvider implements ITableLabelProvider{

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return super.getImage(element);
            default:
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return super.getText(element);
            default:
                return null;
            }
        }
    }

    private class DownloadableVersionsContentProvider implements IStructuredContentProvider{
        private DownloadableCordovaEngine[] versions;

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if(newInput != null ){
                @SuppressWarnings("unchecked")
                List<DownloadableCordovaEngine> list = (List<DownloadableCordovaEngine>)newInput;
                versions = list.toArray(new DownloadableCordovaEngine[list.size()]);
            }else{
                versions =  null;
            }

        }

        @Override
        public Object[] getElements(Object inputElement) {
            if(versions == null ){
                 engineProvider = new CordovaEngineProvider();
                try {
                    List<DownloadableCordovaEngine> engineList = engineProvider.getDownloadableVersions();
                    versions = engineList.toArray(new DownloadableCordovaEngine[engineList.size()]);
                } catch (CoreException e) {
                    StatusManager.handle(e);
                }
            }
            return versions;
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
        setMessage("Download a new engine version or add a platform to an existing one");
        parent.getShell().setText("Download Hybrid Mobile Engine");


        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).spacing(LayoutConstants.getSpacing()).numColumns(2).applyTo(composite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
        Label versionLbl = new Label(composite, SWT.NONE);
        versionLbl.setText("Version:");
        Combo versionCombo = new Combo(composite, SWT.READ_ONLY);
        versionViewer = new ComboViewer(versionCombo);
        versionViewer.setContentProvider(new DownloadableVersionsContentProvider());
        versionViewer.setComparator(new ViewerComparator(new VersionStringComparator()));
        versionViewer.setLabelProvider(new DownloadableEngineLabelProvider());
        versionViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if(event.getSelection().isEmpty()) return;
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                DownloadableCordovaEngine engine = (DownloadableCordovaEngine) sel.getFirstElement();
                platformList.setInput(engine.getVersion());
                validate();
            }
        });

        Table table= new Table(composite, SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).minSize(new Point(TABLE_WIDTH, TABLE_HEIGHT)).applyTo(table);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);

        TableColumn col = new TableColumn(table, SWT.NONE);
        col.setWidth(120);
        col.setText("Platform");

        platformList = new CheckboxTableViewer(table);
        // Use ProjectGeneratorContentProvider which gives us the supported platforms.
        // we then filter out the platforms that are not supported by the content provider
        // and the already installed using the ContentProviderSupportFilter
        platformList.setContentProvider(new ProjectGeneratorContentProvider());
        platformList.setFilters(new ViewerFilter[]{ new ContentProviderSupportFilter()});
        platformList.setLabelProvider(new PlatformsLabelProvider());
        platformList.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                validate();
            }
        });

        createProgressMonitorPart(composite);

        engineProvider = new CordovaEngineProvider();
        try {
            versionViewer.setInput(engineProvider.getDownloadableVersions());
        } catch (CoreException e) {
            setErrorMessage("Unable to retrieve the downloadable versions list, please try again later.");
            HybridUI.log(IStatus.ERROR, "Unable to retrieve the downloadable versions list", e);
        }
        return composite;
    }

    private void createProgressMonitorPart(final Composite composite) {
        progressMonitorPart = new ProgressMonitorPart(composite, new GridLayout());
//		progressMonitorPart.attachToCancelComponent(getButton(IDialogConstants.CANCEL_ID));
        progressMonitorPart.setVisible(true);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(progressMonitorPart);
    }

    public void setVersion(String version){
        versionViewer.setSelection(new StructuredSelection(version));
        validate();
    }

    public String getVersion(){
        IStructuredSelection sel = (IStructuredSelection) versionViewer.getSelection();
        if(sel.isEmpty()) return null;
        DownloadableCordovaEngine engine = (DownloadableCordovaEngine)sel.getFirstElement();
        return engine.getVersion();
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
        if(platformList.getElementAt(0) == null ){
            setErrorMessage(NLS.bind("All supported platforms are already installed for {0} {1}", new String[]{engineProvider.getName(),getVersion()} ));
            toggleOKButton(false);
            return;
        }
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
        Object[] checked = platformList.getCheckedElements();
        final String[] platforms = new String[checked.length];
        for (int i = 0; i < checked.length; i++) {
            PlatformSupport gen = (PlatformSupport) checked[i];
            platforms[i] = gen.getPlatformId();
        }
        final String version = getVersion();
        versionViewer.getCombo().setEnabled(false);
        platformList.getTable().setEnabled(false);
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
                engineProvider.downloadEngine(version, monitor, platforms);
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
