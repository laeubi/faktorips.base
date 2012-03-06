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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * Editor page to edit the test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeEditorPage extends IpsObjectEditorPage {

    public static final String PAGE_ID = "TestCaseTypeEditorPage"; //$NON-NLS-1$

    private String sectionTitle;
    private String sectionDetailTitle;

    private TestCaseTypeSection section;

    public TestCaseTypeEditorPage(TestCaseTypeEditor editor, String title, String sectionTitle,
            String sectionDetailTitle) {

        super(editor, PAGE_ID, title);
        this.sectionTitle = sectionTitle;
        this.sectionDetailTitle = sectionDetailTitle;
    }

    @Override
    public void dispose() {
        section.dispose();
        super.dispose();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        super.createPageContent(formBody, toolkit);
        formBody.setLayout(createPageLayout(1, false));
        section = new TestCaseTypeSection(formBody, toolkit, ((TestCaseTypeEditor)getEditor()).getTestCaseType(),
                sectionTitle, sectionDetailTitle, getManagedForm().getForm());
    }

    public void refreshInclStructuralChanges() {
        section.refreshTreeAndDetailArea();
    }

    @Override
    protected void createToolbarActions(IToolBarManager toolbarManager) {
        toolbarManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));

        IMenuService menuService = (IMenuService)getSite().getService(IMenuService.class);
        menuService.populateContributionManager((ContributionManager)toolbarManager,
                MenuUtil.toolbarUri(IpsMenuId.TOOLBAR_TEST_CASE_TYPE_EDITOR_PAGE.getId()));
    }

}