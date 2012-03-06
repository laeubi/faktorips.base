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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.IpsAction;

/**
 * Action for add a new row in a tableviewer.
 * 
 * @author Joerg Ortmann
 */
public class NewRowAction extends IpsAction {

    /**
     * The TableViewer this action operates in.
     */
    private TableViewer tableViewer;
    private ContentPage contentPage;

    /**
     * Creates an action that, when run, addes a new row in the given <code>TableViewer</code>.
     */
    public NewRowAction(TableViewer tableViewer, ContentPage page) {
        super(tableViewer);
        this.tableViewer = tableViewer;
        contentPage = page;
        setControlWithDataChangeableSupport(page);
        setText(Messages.NewRowAction_Label);
        setToolTipText(Messages.NewRowAction_Tooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("InsertRowAfter.gif")); //$NON-NLS-1$
    }

    /**
     * Creates a new row in the tableviewer and refreshes thereafter. {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        ITableContents tableContents = (ITableContents)tableViewer.getInput();

        ITableContentsGeneration tableContentsGeneration = (ITableContentsGeneration)tableContents.getFirstGeneration();

        IRow newRow = null;
        int position = Integer.MAX_VALUE;
        Object selected = selection.getFirstElement();
        if (selected instanceof IRow) {
            position = ((IRow)selected).getRowNumber();
            newRow = tableContentsGeneration.insertRowAfter(position);
            tableViewer.insert(newRow, position);
        } else {
            tableContentsGeneration.newRow();
        }

        tableViewer.refresh(true);
        contentPage.redrawTable();
    }
}