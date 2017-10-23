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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.internal.util.EngineUtils;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.PlatformImage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.services.IEvaluationService;

import com.github.zafarkhaja.semver.Version;

public class AvailableCordovaEnginesSection implements ISelectionProvider {

	private static final int TREE_HEIGHT = 250;
	private static final int TREE_WIDTH = 500;

	private ListenerList<ISelectionChangedListener> selectionListeners;
	private ListenerList<EngineListChangeListener> engineChangeListeners;
	private CheckboxTreeViewer engineList;
	private ISelection prevSelection = new StructuredSelection();
	private Button removeBtn;
	private FormToolkit formToolkit;
	private boolean fireSelectionChanged = true;

	public static interface EngineListChangeListener {
		public void listChanged();
	}

	private static class CordovaEnginesContentProvider implements ITreeContentProvider {
		private Set<HybridMobileEngine> engines;
		private PlatformSupport[] platforms;

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.engines = (Set<HybridMobileEngine>) newInput;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (platforms == null) {
				List<PlatformSupport> allPlatforms = HybridCore.getPlatformSupports();
				ArrayList<PlatformSupport> elements = new ArrayList<PlatformSupport>();
				IEvaluationService service = (IEvaluationService) PlatformUI.getWorkbench()
						.getService(IEvaluationService.class);
				for (PlatformSupport generator : allPlatforms) {
					try {
						if (generator.isEnabled(service.getCurrentState())) {
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
			if (engines == null || !(parentElement instanceof PlatformSupport)) {
				return null;
			}
			PlatformSupport platform = (PlatformSupport) parentElement;
			ArrayList<HybridMobileEngine> platformEngines = new ArrayList<HybridMobileEngine>();
			for (HybridMobileEngine hybridMobileEngine : engines) {
				if (hybridMobileEngine.getName().equals(platform.getPlatformId())) {
					platformEngines.add(hybridMobileEngine);
				}
			}
			return platformEngines.toArray(new HybridMobileEngine[platformEngines.size()]);
		}

		@Override
		public Object getParent(Object element) {
			if (platforms != null && element instanceof HybridMobileEngine) {
				HybridMobileEngine engine = (HybridMobileEngine) element;
				for (PlatformSupport platformSupport : platforms) {
					if (engine.getName().equals(platformSupport.getPlatformId())) {
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

	private class CordovaEngineLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {
		private Font boldFont;

		@Override
		public Image getImage(Object element) {
			if (element instanceof PlatformSupport) {
				PlatformSupport platform = (PlatformSupport) element;
				return PlatformImage.getImageFor(PlatformImage.ATTR_PLATFORM_SUPPORT, platform.getID());
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof PlatformSupport) {
				PlatformSupport platform = (PlatformSupport) element;
				return platform.getPlatform();
			}
			if (element instanceof HybridMobileEngine) {
				HybridMobileEngine engine = (HybridMobileEngine) element;
				String identifier = EngineUtils.getExactVersion(engine.getSpec());
				return NLS.bind("{0}@{1}", new String[] { engine.getName(), identifier });
			}
			return null;
		}

		private String getLocationText(Object element) {
			if (element instanceof HybridMobileEngine) {
				HybridMobileEngine engine = (HybridMobileEngine) element;
				return engine.getSpec();
			}
			return null;
		}

		@Override
		public Font getFont(Object element) {
			if (!engineList.getChecked(element))
				return null;
			if (boldFont == null) {
				FontDescriptor fontDescriptor = JFaceResources.getDialogFontDescriptor();
				fontDescriptor = fontDescriptor.setStyle(SWT.BOLD);
				boldFont = fontDescriptor.createFont(Display.getCurrent());
			}
			return boldFont;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return getText(element);
			case 1:
				return getLocationText(element);
			default:
				return "invalid";
			}
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return getImage(element);
			}
			return null;
		}

	}

	private static class EngineVersionComparator extends ViewerComparator implements Comparator<HybridMobileEngine> {
		private boolean descending = true;

		public EngineVersionComparator(boolean isDescending) {
			descending = isDescending;
		}

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (!(o1 instanceof HybridMobileEngine)) {
				return 1;
			}
			HybridMobileEngine e1 = (HybridMobileEngine) o1;
			HybridMobileEngine e2 = (HybridMobileEngine) o2;
			return compare(e1, e2);
		}

		@Override
		public int compare(HybridMobileEngine o1, HybridMobileEngine o2) {
			try {
				Version version1 = Version.valueOf(o1.getSpec());
				Version version2 = Version.valueOf(o2.getSpec());
				if (descending) {
					return version2.compareTo(version1);
				}
				return version1.compareTo(version2);
			} catch (Exception e) {
				return 1;
			}
		}
	}

	public AvailableCordovaEnginesSection() {
		this.selectionListeners = new ListenerList<ISelectionChangedListener>();
		this.engineChangeListeners = new ListenerList<EngineListChangeListener>();
		this.formToolkit = null;
	}

	public AvailableCordovaEnginesSection(FormToolkit formToolkit) {
		this();
		this.formToolkit = formToolkit;
	}

	public void createControl(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

		Label tableLbl = new Label(composite, SWT.NULL);
		tableLbl.setText("Available Engines: ");
		GridDataFactory.generate(tableLbl, 2, 1);

		Tree tree = new Tree(composite, SWT.CHECK | SWT.FULL_SELECTION);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
				.minSize(new Point(TREE_WIDTH, TREE_HEIGHT)).applyTo(tree);

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		TreeColumn col_0 = new TreeColumn(tree, SWT.LEFT);
		col_0.setText("Engine");
		col_0.setWidth(TREE_WIDTH / 2);
		col_0.setMoveable(false);
		TreeColumn col_1 = new TreeColumn(tree, SWT.LEFT);
		col_1.setText("Location");
		col_1.setWidth(TREE_WIDTH / 2);
		col_1.setMoveable(false);

		engineList = new CheckboxTreeViewer(tree);
		engineList.setContentProvider(new CordovaEnginesContentProvider());
		engineList.setLabelProvider(new CordovaEngineLabelProvider());
		engineList.setComparator(new EngineVersionComparator(true));
		engineList.setUseHashlookup(true);

		engineList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					ITreeContentProvider cp = (ITreeContentProvider) engineList.getContentProvider();
					if (event.getElement() instanceof PlatformSupport) {
						engineList.setChecked(event.getElement(), false);
						HybridMobileEngine[] children = (HybridMobileEngine[]) cp.getChildren(event.getElement());
						if (children != null && children.length > 0) {
							// Sort so that we can select the highest version number.
							Arrays.sort(children, new EngineVersionComparator(true));
							engineList.setChecked(children[0], true);
							for (int i = 1; i < children.length; i++) {// start with index 1
								engineList.setChecked(children[i], false);
							}
						}
					} else {
						Object[] siblings = cp.getChildren(cp.getParent(event.getElement()));
						for (int i = 0; i < siblings.length; i++) {
							if (siblings[i] != event.getElement()) {
								engineList.setChecked(siblings[i], false);
							}
						}
					}

				}
				Object[] checked = engineList.getCheckedElements();
				if (checked != null && checked.length > 0) {
					setSelection(new StructuredSelection(checked));
				} else {
					setSelection(new StructuredSelection());
				}
			}
		});

		engineList.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();

			}
		});

		Composite buttonsContainer = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).applyTo(buttonsContainer);
		GridLayoutFactory.fillDefaults().spacing(0, 3).numColumns(1).applyTo(buttonsContainer);

		Button downloadBtn = new Button(buttonsContainer, SWT.PUSH);
		downloadBtn.setText("Add...");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(downloadBtn);
		;
		downloadBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				EngineAddDialog downloadDialog = new EngineAddDialog(engineList.getControl().getShell());
				int status = downloadDialog.open();
				if (status == Window.OK) {
					updateAvailableEngines(downloadDialog.getEngines());
				}

			}
		});

		removeBtn = new Button(buttonsContainer, SWT.PUSH);
		removeBtn.setText("Remove");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeBtn);
		removeBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				handleRemoveEngine();
			}
		});

		updateAvailableEngines(null);
		updateButtons();

		if (formToolkit != null) {
			formToolkit.adapt(composite);
			formToolkit.paintBordersFor(composite);
			formToolkit.adapt(tree, true, false);
			formToolkit.adapt(buttonsContainer);
			formToolkit.adapt(downloadBtn, true, false);
			formToolkit.adapt(removeBtn, true, false);
		}
	}

	private void updateButtons() {
		IStructuredSelection selection = (IStructuredSelection) engineList.getSelection();
		removeBtn.setEnabled(!selection.isEmpty() && selection.getFirstElement() instanceof HybridMobileEngine);
	}

	public void updateAvailableEngines(Set<HybridMobileEngine> additionalUserEngines) {
		CordovaEngineProvider provider = CordovaEngineProvider.getInstance();
		Set<HybridMobileEngine> engines = provider.getAvailableEngines();
		if (additionalUserEngines != null) {
			engines.addAll(additionalUserEngines);
		}
		engineList.setInput(engines);
		prevSelection = null;
		fireEngineListChanged();

	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	public void addEngineListChangeListener(EngineListChangeListener listener) {
		engineChangeListeners.add(listener);
	}

	public void removeEngineListChangeListener(EngineListChangeListener listener) {
		engineChangeListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return new StructuredSelection(engineList.getCheckedElements());
	}

	/**
	 * Returns the list of {@link HybridMobileEngine}s that are listed null if the
	 * list is empty.
	 * 
	 * @return
	 */
	public List<HybridMobileEngine> getListedEngines() {
		Object o = engineList.getInput();
		if (o == null)
			return null;
		return (List<HybridMobileEngine>) o;

	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(selectionListeners);
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.equals(prevSelection)) {
				prevSelection = selection;
				if (selection.isEmpty()) {
					engineList.setCheckedElements(new Object[0]);
				} else {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					List<HybridMobileEngine> checkedEngines = structuredSelection.toList();
					for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
						HybridMobileEngine engine = (HybridMobileEngine) iterator.next();
						engineList.reveal(engine);
					}
					engineList.setCheckedElements(checkedEngines.toArray());
				}
				engineList.refresh(true);
				fireSelectionChanged();
			}
		}
	}

	public void disableSelectionChangedFire() {
		fireSelectionChanged = false;
	}

	public void enableSelectionCHangedFire() {
		fireSelectionChanged = true;
	}

	private void fireSelectionChanged() {
		if (fireSelectionChanged) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
			Object[] listeners = selectionListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
				listener.selectionChanged(event);
			}
		}
	}

	private void fireEngineListChanged() {
		Object[] listeners = engineChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			EngineListChangeListener l = (EngineListChangeListener) listeners[i];
			l.listChanged();
		}
	}

	private void handleRemoveEngine() {
		IStructuredSelection selection = (IStructuredSelection) engineList.getSelection();
		if (selection.isEmpty())
			return;
		Object selectedObject = selection.getFirstElement();
		if (!(selectedObject instanceof HybridMobileEngine)) {
			return;
		}
		HybridMobileEngine selectedEngine = (HybridMobileEngine) selectedObject;
		boolean deleteConfirm = MessageDialog.openConfirm(this.engineList.getTree().getShell(), "Confirm Delete",
				NLS.bind("Remove {0} {1} ?", new String[] { selectedEngine.getName(), selectedEngine.getSpec() }));
		if (deleteConfirm) {
			CordovaEngineProvider.getInstance().deleteEngine(selectedEngine);
			updateAvailableEngines(null);
			ISelection cSelection = getSelection();
			HybridMobileEngine checkedEngine = null;
			if (cSelection != null && !cSelection.isEmpty()) {
				IStructuredSelection css = (IStructuredSelection) cSelection;
				checkedEngine = (HybridMobileEngine) css.getFirstElement();
			}
			if (checkedEngine != null && checkedEngine == selectedEngine) {
				setSelection(new StructuredSelection());
			}
		}
	}

}
