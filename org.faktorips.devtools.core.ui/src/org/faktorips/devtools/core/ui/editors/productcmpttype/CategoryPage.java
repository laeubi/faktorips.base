/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.PartAdapter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.DialogMementoHelper;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.productcmpt.LinksSection;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Page that allows to edit the {@link IProductCmptCategory}s of an {@link IProductCmptType}.
 * <p>
 * The page provides a {@link CategorySection} for each {@link IProductCmptCategory}.
 * 
 * @author Alexander Weickmann
 */
public class CategoryPage extends IpsObjectEditorPage {

    private static final String PAGE_ID = "org.faktorips.devtools.core.ui.editors.productcmpttype.CategoryPage"; //$NON-NLS-1$

    private CategorySectionRefreshizer categorySectionRefreshizer;

    public CategoryPage(ProductCmptTypeEditor editor) {
        super(editor, PAGE_ID, Messages.CategoryPage_tagPageName);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        super.createPageContent(formBody, toolkit);

        formBody.setLayout(createPageLayout(1, false));
        CategoryCompositionSection categoryCompositionSection = new CategoryCompositionSection(formBody, toolkit);

        addRecreateOnActivationListener(categoryCompositionSection);
    }

    private void addRecreateOnActivationListener(CategoryCompositionSection categoryCompositionSection) {
        categorySectionRefreshizer = new CategorySectionRefreshizer(getIpsObjectEditor(), this,
                categoryCompositionSection);
        getEditor().getEditorSite().getPage().addPartListener(categorySectionRefreshizer);
    }

    @Override
    public void dispose() {
        if (categorySectionRefreshizer != null) {
            getEditor().getEditorSite().getPage().removePartListener(categorySectionRefreshizer);
        }
        super.dispose();
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            categorySectionRefreshizer.refreshCategorySections();
        }
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    class CategoryCompositionSection extends IpsSection {

        private final Map<IProductCmptCategory, CategorySection> categoriesToSections = new LinkedHashMap<IProductCmptCategory, CategorySection>();

        private final IAction newCategoryAction;

        private Composite left;

        private Composite right;

        private IpsSection linksCategorySection;

        public CategoryCompositionSection(Composite parent, UIToolkit toolkit) {
            super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

            newCategoryAction = new NewCategoryAction();

            initControls();
        }

        @Override
        protected String getSectionTitle() {
            return Messages.CategoryCompositionSection_sectionTitle;
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            setLayout(client);
            createLeftComposite(client);
            createRightComposite(client);
            createCategorySections();
            createLinksCategorySection();
            setInitialFocus();
        }

        private void setInitialFocus() {
            if (categoriesToSections.isEmpty()) {
                left.setFocus();
            } else {
                categoriesToSections.values().toArray(new CategorySection[categoriesToSections.size()])[0].setFocus();
            }
        }

        private void setLayout(Composite parent) {
            GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 1;
            layout.marginHeight = 2;
            parent.setLayout(layout);
        }

        private void createLeftComposite(Composite parent) {
            left = createColumnComposite(parent);
        }

        private void createRightComposite(Composite parent) {
            right = createColumnComposite(parent);
        }

        private Composite createColumnComposite(Composite parent) {
            return getToolkit().createGridComposite(parent, 1, true, true);
        }

        private void createCategorySections() {
            // Determine lastCategoryState
            List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>();
            try {
                categories.addAll(getProductCmptType().findCategories(getProductCmptType().getIpsProject()));
            } catch (CoreException e) {
                // Recover by not displaying any lastCategoryState
                IpsPlugin.log(e);
            }

            for (IProductCmptCategory category : categories) {
                Composite parent = category.isAtLeftPosition() ? left : right;
                CategorySection categorySection = new CategorySection(category, getProductCmptType(), this, parent,
                        getToolkit());
                categoriesToSections.put(category, categorySection);
            }
        }

        private void createLinksCategorySection() {
            linksCategorySection = new LinksCategorySection(right, getToolkit());
        }

        @Override
        protected void populateToolBar(IToolBarManager toolBarManager) {
            toolBarManager.add(newCategoryAction);
        }

        @Override
        protected void performRefresh() {
            for (CategorySection categorySection : categoriesToSections.values()) {
                categorySection.refresh();
            }
        }

        /**
         * Disposes all {@link CategorySection}s and recreates them in the order they are provided
         * by the underlying model.
         * 
         * @param newFocusCategory the {@link IProductCmptCategory} that shall have focus after the
         *            operation or null to let the focus be automatically set to the first
         *            {@link CategorySection}
         */
        public void recreateCategorySections(IProductCmptCategory newFocusCategory) {
            disposeCategorySections();
            createCategorySections();
            createLinksCategorySection();

            relayout();

            if (newFocusCategory != null || getFirstCategory() != null) {
                setFocus(newFocusCategory != null ? newFocusCategory : getFirstCategory());
            }

            refresh();
        }

        private IProductCmptCategory getFirstCategory() {
            if (categoriesToSections.size() == 0) {
                return null;
            }
            return categoriesToSections.keySet().toArray(new IProductCmptCategory[categoriesToSections.size()])[0];
        }

        private void disposeCategorySections() {
            for (CategorySection categorySection : categoriesToSections.values()) {
                categorySection.dispose();
            }
            if (linksCategorySection != null) {
                linksCategorySection.dispose();
            }
            categoriesToSections.clear();
        }

        /**
         * Disposes of the {@link CategorySection} corresponding to the indicated
         * {@link IProductCmptCategory}.
         * <p>
         * Does nothing if no {@link CategorySection} exists for the indicated
         * {@link IProductCmptCategory}.
         */
        public void deleteCategorySection(IProductCmptCategory category) {
            CategorySection categorySection = getCategorySection(category);
            if (categorySection != null) {
                categorySection.dispose();
                categoriesToSections.remove(category);
            }
            relayout();
        }

        /**
         * Recomputes the layout of the entire page.
         */
        private void relayout() {
            left.layout();
            right.layout();
            getManagedForm().reflow(true);
        }

        /**
         * Returns the {@link CategorySection} corresponding to the indicated
         * {@link IProductCmptCategory} or null if no {@link CategorySection} exists for the
         * indicated {@link IProductCmptCategory}.
         */
        public CategorySection getCategorySection(IProductCmptCategory category) {
            return categoriesToSections.get(category);
        }

        /**
         * Sets the focus to the {@link CategorySection} corresponding to the indicated
         * {@link IProductCmptCategory}.
         * <p>
         * Does nothing if no {@link CategorySection} exists for the indicated
         * {@link IProductCmptCategory}.
         */
        private void setFocus(IProductCmptCategory category) {
            CategorySection categorySection = categoriesToSections.get(category);
            if (categorySection != null) {
                categorySection.setFocus();
            }
        }

        /**
         * An {@link IpsSection} that represents the <em>Associations</em> category.
         * <p>
         * As of now, {@link IAssociation} cannot be assigned to {@link IProductCmptCategory} and in
         * the {@link ProductCmptEditor}, the {@link LinksSection} is automatically placed at the
         * bottom right.
         * <p>
         * This section represents the {@link LinksSection} in the
         * {@link CategoryCompositionSection}. It cannot be moved or modified by the user in any
         * way. It is merely a reminder to the user that the {@link LinksSection} is placed
         * automatically at the bottom right by the {@link ProductCmptEditor}.
         */
        private class LinksCategorySection extends IpsSection {

            private Composite rootPane;

            private LinksCategorySection(Composite parent, UIToolkit toolkit) {
                super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
                initControls();
                setText(Messages.LinksCategorySection_title);
                setEnabled(false);
            }

            @Override
            protected void initClientComposite(Composite client, UIToolkit toolkit) {
                setLayout(client);
                createRootPane(client);
                createExplanationLabel();
            }

            private void setLayout(Composite composite) {
                GridLayout layout = new GridLayout(1, true);
                layout.marginWidth = 1;
                layout.marginHeight = 2;
                composite.setLayout(layout);
            }

            private void createRootPane(Composite client) {
                rootPane = getToolkit().createGridComposite(client, 1, false, true);
                getToolkit().addBorder(rootPane);
            }

            private void createExplanationLabel() {
                getToolkit().createLabel(rootPane, Messages.LinksCategorySection_explanation);
            }

        }

        private class NewCategoryAction extends Action {

            private static final String IMAGE_FILENAME = "Add.gif"; //$NON-NLS-1$

            private NewCategoryAction() {
                setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
                setText(Messages.NewCategoryAction_label);
                setToolTipText(Messages.NewCategoryAction_tooltip);
            }

            @Override
            public void run() {
                class NewCategoryDialogMementoHelper extends DialogMementoHelper {
                    private IProductCmptCategory newCategory;

                    @Override
                    protected Dialog createDialog() {
                        newCategory = getProductCmptType().newCategory();
                        Shell shell = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
                        return new CategoryEditDialog(newCategory, shell);
                    }
                }
                NewCategoryDialogMementoHelper dialogHelper = new NewCategoryDialogMementoHelper();
                int returnCode = dialogHelper.openDialogWithMemento(getProductCmptType());
                if (returnCode == Window.OK) {
                    recreateCategorySections(dialogHelper.newCategory);
                }
            }

        }

    }

    /**
     * Provides functionality to recreate the category sections if a valid supertype can be found so
     * that changes to the supertype hierarchy's categories are reflected in the editor.
     * <p>
     * To preserve performance, the sections are only recreated if necessary.
     */
    private static class CategorySectionRefreshizer extends PartAdapter {

        private final IpsObjectEditor editor;

        private final IpsObjectEditorPage page;

        private final CategoryCompositionSection categoryCompositionSection;

        /**
         * Upon each editor activation, the listener remembers the composition of the categories
         * from the supertype hierarchy. If categories have been added, deleted or moved, the
         * category sections of the {@link CategoryCompositionSection} are recreated to reflect the
         * current state.
         */
        private Object lastCategoryState;

        private CategorySectionRefreshizer(IpsObjectEditor editor, IpsObjectEditorPage page,
                CategoryCompositionSection categoryCompositionSection) {

            this.editor = editor;
            this.page = page;
            this.categoryCompositionSection = categoryCompositionSection;
        }

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {
            /*
             * Only refresh if the category page is active (if it is not, the refresh will be
             * handled by the page's setActive(boolean) method).
             */
            if (partRef.getPart(false) != editor || !page.equals(editor.getActiveIpsObjectEditorPage())) {
                return;
            }
            refreshCategorySections();
        }

        private void refreshCategorySections() {
            IProductCmptType productCmptType = (IProductCmptType)editor.getIpsObject();
            try {
                List<IProductCmptCategory> categories = productCmptType.findCategories(productCmptType.getIpsProject());
                if (lastCategoryState == null || !lastCategoryState.equals(categories)) {
                    if (categoryCompositionSection != null
                            && productCmptType.hasExistingSupertype(productCmptType.getIpsProject())) {
                        categoryCompositionSection.recreateCategorySections(null);
                    }
                    lastCategoryState = categories;
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

    }

}
