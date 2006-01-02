package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.text.DateFormat;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.editors.TimedPdoEditor;


/**
 *
 */
public class TableContentsEditor extends TimedPdoEditor {

    /**
     * 
     */
    public TableContentsEditor() {
        super();
    }
    
    protected ITableContents getTableContents() {
        return (ITableContents)getIpsObject();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            addPage(new ContentPage(this));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditor#getUniformPageTitle()
     */
    protected String getUniformPageTitle() {
        ITableContentsGeneration generation = (ITableContentsGeneration)getActiveGeneration();
        String title = "Table Contents: " + getTableContents().getName();
        if (generation==null) {
            return title;
        }
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return title + ", Generation " + format.format(generation.getValidFrom().getTime());
    }

}

