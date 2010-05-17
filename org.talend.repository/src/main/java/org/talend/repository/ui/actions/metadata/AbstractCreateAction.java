// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.actions.metadata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNode.EProperties;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC tguiu class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public abstract class AbstractCreateAction extends AContextualAction {

    private String[] existingNames;

    protected RepositoryNode repositoryNode;

    /**
     * DOC tguiu AbstractCreateAction constructor comment.
     */
    public AbstractCreateAction() {
        super();
    }

    public void init(final TreeViewer viewer, final IStructuredSelection selection) {
        setEnabled(false);
        Object o = selection.getFirstElement();
        if (selection.isEmpty() || selection.size() != 1 || !(o instanceof RepositoryNode)) {
            return;
        }
        init((RepositoryNode) o);
        repositoryNode = (RepositoryNode) o;
    }

    protected abstract void init(RepositoryNode node);

    /**
     * DOC tguiu Comment method "getExistingNames".
     * 
     * @return
     */
    protected String[] getExistingNames() {
        if (existingNames == null) {
            init(repositoryNode);
        }
        return existingNames;
    }

    protected void collectChildNames(final RepositoryNode node) {
        List<String> names = doCollectChildNames(node);
        existingNames = names.toArray(new String[names.size()]);
    }

    private List<String> doCollectChildNames(final RepositoryNode node) {
        List<String> names = new ArrayList<String>();
        for (RepositoryNode sibling : node.getChildren()) {
            names.add((String) sibling.getProperties(EProperties.LABEL));
        }
        return names;
    }

    protected void collectSiblingNames(final RepositoryNode node) {
        List<String> names = doCollectChildNames(node.getParent());
        names.remove((String) node.getProperties(EProperties.LABEL));
        existingNames = names.toArray(new String[names.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.AContextualAction#updateNodeToLastVersion()
     */
    @Override
    protected void updateNodeToLastVersion() {
        if (repositoryNode == null || repositoryNode.getObject() == null
                || !(repositoryNode.getObject() instanceof RepositoryNode)) {
            return;
        }
        try {
            ProxyRepositoryFactory.getInstance().initialize();
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
        Property property = repositoryNode.getObject().getProperty();
        Property updatedProperty = null;

        try {
            updatedProperty = ProxyRepositoryFactory.getInstance().getLastVersion(
                    new Project(ProjectManager.getInstance().getProject(property.getItem())), property.getId()).getProperty();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        // update the property of the node repository object
        repositoryNode.getObject().setProperty(updatedProperty);
    }
}
