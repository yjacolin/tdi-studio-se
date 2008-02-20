// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.views;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.repository.model.RepositoryNode;

/***/
public class RepositoryTreeViewer extends TreeViewer implements ITreeViewerListener {

    private Map<String, Boolean> expanded = new HashMap<String, Boolean>();

    public RepositoryTreeViewer(Composite parent, int style) {
        super(parent, style);
    }

    private RepositoryNode getRepositoryNode(Item node) {
        Object data = node.getData();
        RepositoryNode repositoryNode = null;
        if (data instanceof RepositoryNode) {
            repositoryNode = (RepositoryNode) data;
        }
        return repositoryNode;
    }

    protected boolean getExpanded(Item item) {
        RepositoryNode repositoryNode = getRepositoryNode(item);
        if (repositoryNode != null) {
            Boolean result = expanded.get(repositoryNode.getId());
            if (result != null) {
                if (item instanceof TreeItem) {
                    TreeItem treeItem = (TreeItem) item;
                    treeItem.setExpanded(result);
                }
            }
        }
        return super.getExpanded(item);
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        Object element = event.getElement();
        if (element instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) element;
            if (!repositoryNode.getId().equals(RepositoryNode.NO_ID)) {
                expanded.put(repositoryNode.getId(), false);
            }
            emptyExpandedChildren(repositoryNode);
        }
    }

    public void treeExpanded(TreeExpansionEvent event) {
        Object element = event.getElement();
        if (element instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) element;
            if (!repositoryNode.getId().equals(RepositoryNode.NO_ID)) {
                expanded.put(repositoryNode.getId(), true);
            }
        }
    }

    private void emptyExpandedChildren(RepositoryNode repositoryNode) {
        for (RepositoryNode children : repositoryNode.getChildren()) {
            if (!children.getId().equals(RepositoryNode.NO_ID)) {
                expanded.remove(children.getId());
            }
            emptyExpandedChildren(children);
        }
    }

}
