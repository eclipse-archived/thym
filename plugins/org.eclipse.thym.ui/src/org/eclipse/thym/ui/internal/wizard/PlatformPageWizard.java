/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *       Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.wizard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.thym.ui.wizard.IHybridPlatformWizardPage;
/**
 * Abstract class for wizards that want to enable platform specific pages.
 *
 *<p>Platform specific pages needs to implement {@link IHybridPlatformWizardPage}.
 *They can be introduced with the <code>org.eclipse.thym.ui.platformWizardPage</code>
 *extension point.
 *</p>
 *
 * @see IHybridPlatformWizardPage
 *
 * @author Gorkem Ercan
 *
 */
public abstract class PlatformPageWizard extends Wizard{

    private static final String ATTR_PLATFORM = "platform";
    private IWizardPage prePlatformPage;
    private Map<String, IHybridPlatformWizardPage> platformPages;
    private String[] selectedPlatforms;
    private final static String ATTR_WIZARD = "wizard";
    private final static String ATTR_CLASS = "class";


    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if(page == prePlatformPage || page instanceof IHybridPlatformWizardPage ){
            return getNextPlatformPage(page);
        }
        return super.getNextPage(page);
    }


    /**
     * Signals the wizard to stop showing the platform pages.
     */
    public void doNotShowPlatformPages(){
        prePlatformPage = null;
        updateNextButton();
    }

    /**
     * Signals the wizard to start showing platform pages after rootPage
     *
     * @param rootPage page to show platform pages after (can not be null)
     */
    public void showPlatformPages(IWizardPage rootPage){
        Assert.isNotNull(rootPage);
        prePlatformPage = rootPage;
        initPlatformPages();
        updateNextButton();
    }

    /**
     * Pass the list of platform pages to show. If this method is not called
     * or passed in null, no platform wizard pages will be shown.
     *
     * @param platformIDs list of platform ids
     */
    public void selectPlatformPages(String[] platformIDs){
        this.selectedPlatforms = platformIDs;
        updateNextButton();
    }

    public Map<String, Object> getPlatformValues(String platformId){
        Map<String, Object > values = Collections.emptyMap();
        if(platformPages != null ){
            IHybridPlatformWizardPage page = platformPages.get(platformId);
            if(page != null ){
                values= page.getValues();
                Assert.isNotNull(values, "Platform pages can not return null");
            }
        }
        return values;
    }

    /**
     * Returns the ID of this wizard. Platform pages are selected by
     * comparing this value.
     *
     * @return the wizard ID as defined on extension point
     */
    protected abstract String getWizardID();
    
    
    protected boolean performPlatformFinish(){
        if (selectedPlatforms != null && platformPages != null) {
            for (int i = 0; i < selectedPlatforms.length; i++) {
                IHybridPlatformWizardPage page = platformPages.get(selectedPlatforms[i]);
                if (page != null && !page.finish()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateNextButton() {
        setForcePreviousAndNextButtons(prePlatformPage != null &&
                selectedPlatforms != null &&
                selectedPlatforms.length >0
                );
        getContainer().updateButtons();
    }

    private void initPlatformPages() {
        if(platformPages != null ){
            return;
        }
        platformPages = new HashMap<String,IHybridPlatformWizardPage>();
        IConfigurationElement[] elements = RegistryFactory.getRegistry().getConfigurationElementsFor("org.eclipse.thym.ui.platformWizardsPage");
        for (IConfigurationElement element : elements) {
            String wizardID = element.getAttribute(ATTR_WIZARD);
            if(wizardID.equals(getWizardID())){
                try {
                    String platformID = element.getAttribute(ATTR_PLATFORM);
                    IHybridPlatformWizardPage page = (IHybridPlatformWizardPage) element.createExecutableExtension(ATTR_CLASS);
                    page.setWizard(this);
                    platformPages.put(platformID,page);
                } catch (CoreException e) {
                    StatusManager.handle(e);
                }
            }
        }
    }

    private IWizardPage getNextPlatformPage(IWizardPage page) {
        if (platformPages != null && !platformPages.isEmpty() &&
                selectedPlatforms != null && selectedPlatforms.length >0) {
                int index =  selectedPlatformIndexForPage(page)+1; // we start from 0 when we do not find the platform for page
                if(index < selectedPlatforms.length){
                    IWizardPage platformPage = platformPages.get(selectedPlatforms[index]);
                    return platformPage;
                }
        }
        return null;
    }

    private int selectedPlatformIndexForPage(IWizardPage page){
        if(platformPages == null || selectedPlatforms == null ){
            return -1;
        }
        for(int i = 0 ; i < selectedPlatforms.length; i++){
            if(platformPages.get(selectedPlatforms[i]) == page){
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public boolean performFinish() {
    	return performPlatformFinish();
    }
    
    @Override
    public boolean canFinish() {
        boolean canFinish = super.canFinish();
        if(canFinish){
            if (selectedPlatforms != null && platformPages != null) {
                for (int i = 0; i < selectedPlatforms.length; i++) {
                    IHybridPlatformWizardPage page = platformPages.get(selectedPlatforms[i]);
                    if (page != null && !page.isPageComplete()) {
                        canFinish = false;
                    }
                }
            }
        }
        return canFinish;
    }
}
