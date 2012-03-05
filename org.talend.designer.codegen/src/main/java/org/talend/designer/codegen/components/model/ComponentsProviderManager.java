// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.components.model;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.codegen.i18n.Messages;

/***/
public final class ComponentsProviderManager {

    private static Logger log = Logger.getLogger(ComponentsProviderManager.class);

    private static final ComponentsProviderManager INSTANCE = new ComponentsProviderManager();

    private ArrayList<AbstractComponentsProvider> providers;

    private ComponentsProviderManager() {
    }

    public static ComponentsProviderManager getInstance() {
        return INSTANCE;
    }

    public ArrayList<AbstractComponentsProvider> getProviders() {
        loadComponentsProvidersFromExtension();
        return providers;
    }

    private void loadComponentsProvidersFromExtension() {
        providers = new ArrayList<AbstractComponentsProvider>();

        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.talend.core.components_provider"); //$NON-NLS-1$
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configurationElement : configurationElements) {
                String id = configurationElement.getAttribute("id"); //$NON-NLS-1$
                String folderName = configurationElement.getAttribute("folderName"); //$NON-NLS-1$
                String contributerName = configurationElement.getContributor().getName();
                IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                        IBrandingService.class);
                if (!brandingService.isPoweredOnlyCamel()
                        && id.equals("org.talend.designer.camel.components.localprovider.CamelLocalComponentsProvider")) {
                    folderName = "camel";
                }
                try {
                    AbstractComponentsProvider componentsProvider = (AbstractComponentsProvider) configurationElement
                            .createExecutableExtension("class"); //$NON-NLS-1$
                    componentsProvider.setId(id);
                    componentsProvider.setFolderName(folderName);
                    componentsProvider.setContributer(contributerName);
                    // fix for bug TDI-19889 ,put the UserComponentsProvider to the first place to get
                    // correct component provider when compare the user component path with install folder
                    if (id.equals("org.talend.designer.components.model.UserComponentsProvider")) {
                        providers.add(0, componentsProvider);
                    } else {
                        providers.add(componentsProvider);
                    }
                } catch (CoreException e) {
                    log.error(Messages.getString("ComponentsProviderManager.unableLoad") + id, e); //$NON-NLS-1$
                }
            }
        }
    }

    public AbstractComponentsProvider loadUserComponentsProvidersFromExtension() {
        providers = new ArrayList<AbstractComponentsProvider>();

        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint("org.talend.core.components_provider"); //$NON-NLS-1$
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configurationElement : configurationElements) {
                String id = configurationElement.getAttribute("id"); //$NON-NLS-1$
                if ("org.talend.designer.components.model.UserComponentsProvider".equals(id)) {//$NON-NLS-1$
                    String folderName = configurationElement.getAttribute("folderName"); //$NON-NLS-1$
                    try {
                        AbstractComponentsProvider componentsProvider = (AbstractComponentsProvider) configurationElement
                                .createExecutableExtension("class"); //$NON-NLS-1$
                        componentsProvider.setId(id);
                        componentsProvider.setFolderName(folderName);
                        return componentsProvider;
                    } catch (CoreException e) {
                        log.error(Messages.getString("ComponentsProviderManager.unableLoad") + id, e); //$NON-NLS-1$
                    }
                }
            }
        }
        return null;
    }
}
