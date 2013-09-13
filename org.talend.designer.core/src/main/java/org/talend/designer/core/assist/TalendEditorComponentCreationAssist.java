package org.talend.designer.core.assist;

import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.keys.IBindingService;
import org.talend.core.model.components.IComponent;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.PaletteComponentFactory;
import org.talend.designer.core.ui.editor.nodes.NodePart;

class TalendEditorComponentCreationAssist {

    private static Text assistText;

    private GraphicalViewer graphicViewer;

    private Control graphicControl;

    private Map<String, IComponent> components;

    private ContentProposalAdapter contentProposalAdapter;

    /*
     * this used to disable all other key listeners registered on Display during assistant activate
     */
    private IBindingService bindingService = null;

    private boolean isKeyFilterEnabled = true;

    public TalendEditorComponentCreationAssist(String categoryName, GraphicalViewer viewer, CommandStack commandStack) {
        this.graphicViewer = viewer;
        this.graphicControl = viewer.getControl();
        this.components = TalendEditorComponentCreationUtil.getComponentsInCategory(categoryName);

        Object service = DesignerPlugin.getDefault().getWorkbench().getService(IBindingService.class);
        if (service != null && service instanceof IBindingService) {
            bindingService = (IBindingService) service;
            isKeyFilterEnabled = bindingService.isKeyFilterEnabled();
        }
    }

    /**
     * open the creation assist according to the trigger character
     * 
     * @param triggerChar
     */
    public void showComponentCreationAssist(char triggerChar) {

        org.eclipse.swt.graphics.Point cursorRelativePosition = calculatePosition();
        if (cursorRelativePosition == null) {
            return;
        }

        /*
         * only one assist text at the same time in all editors
         */
        disposeAssistText();

        createAssistText(cursorRelativePosition);

        /*
         * add listeners to control when the proposal dialog shows or hide
         */
        // TODO this may need improvement.
        initListeners();

        activateAssist(triggerChar);
    }

    private org.eclipse.swt.graphics.Point calculatePosition() {
        /*
         * calculate the cursor position on current editor
         */
        org.eclipse.swt.graphics.Point cursorAbsLocation = graphicControl.getDisplay().getCursorLocation();
        org.eclipse.swt.graphics.Point cursorRelativePosition = graphicControl.getDisplay().map(null, graphicControl,
                cursorAbsLocation);
        if (!isInsideGraphic(cursorRelativePosition, graphicControl.getSize())) {
            return null;
        }
        return cursorRelativePosition;
    }

    private void activateAssist(char triggerChar) {
        // set init text content
        assistText.setText(triggerChar + "");
        assistText.setSelection(assistText.getText().length());

        // trigger proposal dialog
        Event event = new Event();
        event.character = triggerChar;
        assistText.notifyListeners(SWT.KeyDown, event);
        assistText.notifyListeners(SWT.Modify, event);
    }

    private void createAssistText(org.eclipse.swt.graphics.Point cursorRelativePosition) {
        // disable key event filter on Display
        if (bindingService != null) {
            bindingService.setKeyFilterEnabled(false);
        }

        // create assist input text
        assistText = new Text((Composite) graphicControl, SWT.BORDER);
        assistText.setLocation(cursorRelativePosition.x, cursorRelativePosition.y - assistText.getLineHeight());
        assistText.setSize(200, assistText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        assistText.setFocus();

        /*
         * create the proposal by using available components list
         */
        // TODO the trigger way may need improved, currently, any visible character will trigger it
        TalendEditorComponentProposalProvider proposalProvider = new TalendEditorComponentProposalProvider(components);
        contentProposalAdapter = new ContentProposalAdapter(assistText, new TextContentAdapter(), proposalProvider, null, null);
        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        contentProposalAdapter.setLabelProvider(new TalendEditorComponentLabelProvider(components));
    }

    private void initListeners() {
        assistText.addKeyListener(new KeyListener() {

            public void keyReleased(KeyEvent e) {
                if (e.stateMask == SWT.NONE) {
                    if (e.keyCode == SWT.ESC) {
                        disposeAssistText();
                    } else if (e.keyCode == SWT.CR) {
                        acceptProposal();
                    }
                }
            }

            public void keyPressed(KeyEvent e) {
            }
        });
        assistText.addFocusListener(new FocusListener() {

            public void focusLost(FocusEvent e) {
                if (!(contentProposalAdapter.isProposalPopupOpen())) {
                    disposeAssistText();
                }
            }

            public void focusGained(FocusEvent e) {

            }
        });
        contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {

            public void proposalPopupOpened(ContentProposalAdapter adapter) {

            }

            public void proposalPopupClosed(ContentProposalAdapter adapter) {
                if (!assistText.isFocusControl()) {
                    disposeAssistText();
                }
            }
        });
        contentProposalAdapter.addContentProposalListener(new IContentProposalListener() {

            public void proposalAccepted(IContentProposal proposal) {
                acceptProposal();
            }
        });
    }

    /**
     * create component at current position, according to select proposal label DOC talend2 Comment method
     * "createComponent".
     * 
     * @param componentName
     * @param location
     */
    protected void acceptProposal() {
        String componentName = assistText.getText().trim();
        org.eclipse.swt.graphics.Point componentLocation = assistText.getLocation();
        disposeAssistText();
        Object createdNode = createComponent(components.get(componentName), componentLocation);
        selectComponent(createdNode);
    }

    private void selectComponent(Object createdNode) {
        Object nodePart = graphicViewer.getEditPartRegistry().get(createdNode);
        if (nodePart != null && nodePart instanceof NodePart) {
            graphicViewer.select((EditPart) nodePart);
        }
    }

    protected Object createComponent(IComponent component, org.eclipse.swt.graphics.Point location) {
        if (component == null) {
            return null;
        }
        /*
         * TODO support to insert the component on Connection
         */
        Event e = new Event();
        e.x = location.x;
        e.y = location.y;
        e.button = 1;
        e.count = 1;
        e.stateMask = 0;
        e.widget = graphicControl;
        MouseEvent mouseEvent = new MouseEvent(e);

        TalendAssistantCreationTool creationTool = new TalendAssistantCreationTool(new PaletteComponentFactory(component));
        creationTool.mouseMove(mouseEvent, graphicViewer);

        graphicViewer.getEditDomain().setActiveTool(creationTool);

        graphicViewer.getEditDomain().mouseMove(mouseEvent, graphicViewer);
        graphicViewer.getEditDomain().mouseDown(mouseEvent, graphicViewer);
        graphicViewer.getEditDomain().mouseUp(mouseEvent, graphicViewer);
        return creationTool.getCreateRequest().getNewObject();
        // CreateRequest createRequest = new CreateRequest();
        // createRequest.setLocation(new Point(location.x, location.y));
        // createRequest.setFactory(new PaletteComponentFactory(component));
        // Command command = graphicViewer.getContents().getCommand(createRequest);
        // if (!command.canExecute()) {
        // MessageDialog.openWarning(graphicControl.getShell(), "Failed", "Component can't be created here");
        // return null;
        // }
        // commandStack.execute(command);
        // Object obj = createRequest.getNewObject();
        // createRequest = null;
        // return obj;
    }

    /*
     * this used to judge the cursor is inside the editor or not
     */
    private boolean isInsideGraphic(org.eclipse.swt.graphics.Point cursorPosition, org.eclipse.swt.graphics.Point graphicSize) {
        if (cursorPosition.x < 0 || cursorPosition.y < 0) {
            return false;
        }
        if (cursorPosition.x > graphicSize.x || cursorPosition.y > graphicSize.y) {
            return false;
        }
        return true;
    }

    private void disposeAssistText() {
        if (assistText != null && !assistText.isDisposed()) {
            assistText.dispose();
        }
        assistText = null;
        // restore key event filter on Display
        if (bindingService != null) {
            bindingService.setKeyFilterEnabled(isKeyFilterEnabled);
        }
    }

    class TalendAssistantCreationTool extends CreationTool {

        private CreateRequest request = null;

        public TalendAssistantCreationTool(CreationFactory aFactory) {
            super(aFactory);
        }

        @Override
        public CreateRequest getCreateRequest() {
            if (request == null) {
                request = super.getCreateRequest();
            }
            return request;
        }
    }
}
