/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.StaticContentSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Messages;

/**
 * Opens the wizard to create a new product component relation.
 * 
 * @author Thorsten Guenther
 */
// TODO VS: abstrakte basisklasse wegen dupliziertem code?
public class AddProductCmptLinkCommand extends AbstractHandler {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.commands.AddProductCmptLink"; //$NON-NLS-1$

    public AddProductCmptLinkCommand() {
        super();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            if (structuredSelection.getFirstElement() instanceof IProductCmptStructureReference) {
                addLinkOnReference(event);
                return null;
            } else {
                addLinksOnAssociation(event);
                return null;
            }
        }
        return null;
    }

    private void addLinksOnAssociation(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<String> typedSelection = new TypedSelection<String>(String.class, selection);
        if (!typedSelection.isValid()) {
            return;
        }

        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (!(editor instanceof ProductCmptEditor)) {
            return;
        }

        ProductCmptEditor productCmptEditor = (ProductCmptEditor)editor;
        IProductCmpt productCmpt = productCmptEditor.getProductCmpt();
        try {
            IProductCmptType productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());

            String associationName = typedSelection.getFirstElement();
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)productCmptType.findAssociation(
                    associationName, productCmpt.getIpsProject());

            IProductCmptType targetProductCmptType = association.findTargetProductCmptType(productCmpt.getIpsProject());
            IIpsSrcFile[] ipsSrcFiles = productCmpt.getIpsProject().findAllProductCmptSrcFiles(targetProductCmptType,
                    true);
            final StaticContentSelectIpsObjectContext context = new StaticContentSelectIpsObjectContext();
            context.setElements(ipsSrcFiles);
            final OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(
                    HandlerUtil.getActiveShell(event), Messages.AddLinkAction_selectDialogTitle, context, true);
            int rc = dialog.open();
            if (rc == Window.OK && !dialog.getSelectedObjects().isEmpty()) {
                addLinksToActiveProductCmptGeneration((IProductCmptGeneration)productCmptEditor.getActiveGeneration(),
                        association, dialog.getSelectedObjects());
            }

        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void addLinksToActiveProductCmptGeneration(final IProductCmptGeneration activeProductCmptGeneration,
            final IProductCmptTypeAssociation association,
            final List<IIpsElement> selectedIpsElements) throws CoreException {

        activeProductCmptGeneration.getIpsModel().runAndQueueChangeEvents(new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                for (IIpsElement element : selectedIpsElements) {
                    if (element instanceof IIpsSrcFile) {
                        IIpsSrcFile ipsSrcFile = (IIpsSrcFile)element;
                        IProductCmpt selectedProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                        IProductCmptLink link = activeProductCmptGeneration.newLink(association);
                        link.setAssociation(association.getName());
                        link.setTarget(selectedProductCmpt.getQualifiedName());
                        link.setMaxCardinality(1);
                        link.setMinCardinality(0);
                    }
                }
            }
        }, new NullProgressMonitor());
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        IWorkbenchWindow activeWorkbenchWindow = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            setBaseEnabled(false);
            return;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        Object selectedElement = structuredSelection.getFirstElement();
        if (selectedElement instanceof IProductCmptReference) {
            setBaseEnabled(((IProductCmptReference)selectedElement).hasAssociationChildren());
        } else if (selectedElement instanceof String) {
            setBaseEnabled(isValidAssociationName((String)selectedElement, activeWorkbenchWindow));
        } else {
            setBaseEnabled(true);
        }
    }

    /**
     * Queries to the target type of the selected element, target type must be found.
     * 
     */
    private boolean isValidAssociationName(String associationName, IWorkbenchWindow activeWorkbenchWindow) {
        // TODO VS: was mit cast machen?
        IProductCmpt productCmpt = ((ProductCmptEditor)activeWorkbenchWindow.getActivePage().getActiveEditor())
                .getProductCmpt();

        IProductCmptType productCmptType = null;
        try {
            productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (productCmptType == null) {
            return false;
        }
        IProductCmptTypeAssociation typeAssociation = null;
        try {
            typeAssociation = (IProductCmptTypeAssociation)productCmptType.findAssociation(associationName,
                    productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (typeAssociation == null) {
            return false;
        }

        IProductCmptType targetProductCmptType = null;
        try {
            targetProductCmptType = typeAssociation.findTargetProductCmptType(productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return targetProductCmptType != null;

    }

    private void addLinkOnReference(ExecutionEvent event) {
        LinkCreatorUtil linkCreator = new LinkCreatorUtil(true);
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TypedSelection<IProductCmptStructureReference> typedSelection = new TypedSelection<IProductCmptStructureReference>(
                IProductCmptStructureReference.class, selection);
        if (!typedSelection.isValid()) {
            return;
        }
        try {
            IIpsProject ipsProject = null;
            IProductCmptStructureReference structureReference = typedSelection.getFirstElement();
            ipsProject = structureReference.getWrappedIpsObject().getIpsProject();
            if (ipsProject != null) {
                List<IProductCmpt> selectedResults = selectProductCmpt(ipsProject, structureReference,
                        HandlerUtil.getActiveShell(event));
                linkCreator.createLinks(selectedResults, structureReference);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * returns a list of product cmpts because the link creator requests a list
     */
    private List<IProductCmpt> selectProductCmpt(IIpsProject ipsProject,
            IProductCmptStructureReference linkTarget,
            Shell shell) throws CoreException {

        List<IProductCmpt> selectedResults = new ArrayList<IProductCmpt>();
        OpenIpsObjectSelectionDialog dialog = getSelectDialog(ipsProject, linkTarget, shell);
        // TODO set multi select in dialog
        if (dialog.open() == Window.OK) {
            IIpsElement selectedResult = dialog.getSelectedObject();
            if (selectedResult instanceof IIpsSrcFile) {
                IIpsObject selectedIpsObject = ((IIpsSrcFile)selectedResult).getIpsObject();
                if (selectedIpsObject instanceof IProductCmpt) {
                    IProductCmpt selectResultCmpt = (IProductCmpt)selectedIpsObject;
                    selectedResults.add(selectResultCmpt);
                }
            }
        }
        return selectedResults;
    }

    private OpenIpsObjectSelectionDialog getSelectDialog(IIpsProject ipsProject,
            IProductCmptStructureReference linkTarget,
            Shell shell) {
        SingleTypeSelectIpsObjectContext context = new SingleTypeSelectIpsObjectContext(ipsProject,
                IpsObjectType.PRODUCT_CMPT, new LinkViewerFilter(linkTarget));
        OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(shell,
                Messages.AddLinkAction_selectDialogTitle, context);
        return dialog;
    }

    private static class LinkViewerFilter extends ViewerFilter {

        private final IProductCmptStructureReference linkTarget;
        private final LinkCreatorUtil linkCreator;

        public LinkViewerFilter(IProductCmptStructureReference linkTarget) {
            this.linkTarget = linkTarget;
            linkCreator = new LinkCreatorUtil(true);
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            try {
                if (element instanceof IIpsSrcFile) {
                    IIpsSrcFile srcFile = (IIpsSrcFile)element;
                    if (!srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
                        return false;
                    }
                    List<IProductCmpt> productCmptList = new ArrayList<IProductCmpt>(1);
                    productCmptList.add((IProductCmpt)srcFile.getIpsObject());
                    return linkCreator.canCreateLinks(linkTarget, productCmptList);
                } else {
                    return false;
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }
    }

}
