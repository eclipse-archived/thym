package org.eclipse.thym.ui.decorators;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class WWWFolderDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private static final ImageDescriptor wwwOverlay =
			AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.thym.ui",
					"icons/decorators/www_decorator.gif");

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (!(element instanceof IFolder)) {
			return;
		}

		IFolder folder = (IFolder)element;
		try {
			if (folder.getProject().hasNature("org.eclipse.thym.core.HybridAppNature")
				&& folder.getProjectRelativePath().toPortableString().equals("www")) {

				decoration.addOverlay(wwwOverlay);
			}
		} catch (CoreException e) {
			HybridUI.log(IStatus.WARNING, "Could not determine Nature of project", e);
		}
	}
}
