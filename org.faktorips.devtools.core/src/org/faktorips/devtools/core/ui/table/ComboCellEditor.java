/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import java.util.Arrays;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;

/**
 * 
 * @author Stefan Widmaier
 */
public class ComboCellEditor extends TableCellEditor {

    private Combo comboControl;
    
    public ComboCellEditor(TableViewer tableViewer, int columnIndex, Combo comboControl){
        super(tableViewer, columnIndex, comboControl);
        this.comboControl= comboControl;
    }
    
    /**
     * Does NOT add a keylistener to the combo control, as up/down arrows are needed
     * to navigate the list of items inside the combo.
     * {@inheritDoc}
     */
    protected void initKeyListener(Control control) {
    }
    
    /**
     * Returns the text of the currently selected item in the combobox (which is always a String).
     * {@inheritDoc}
     */
    protected Object doGetValue() { 
        Object field = comboControl.getData();
        if (field instanceof EnumDatatypeField){
            // map the id by using the stored EnumDatatypeField
            return ((EnumDatatypeField)field).getValue();
        } else {
            return comboControl.getText();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetFocus() {
        comboControl.setFocus();
    }

    /**
     * Selects the item in the combobox' list of items that equals the given value and thereby sets 
     * the current text of the combobox to the given value.
     * {@inheritDoc}
     */
    protected void doSetValue(Object value) {
        if((comboControl != null) && value instanceof String){
            Object field = comboControl.getData();
            if (field instanceof EnumDatatypeField){
                // map the value by using the stored EnumDatatypeField
                ((EnumDatatypeField)field).setValue(value);
            } else {
                comboControl.select(getIndexForValue((String) value));  
            }
        }else{
            comboControl.select(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMappedValue() {
        return true;
    }
    
    /*
     * Searches the combo's list of items for the given text and returns the first index at 
     * which an equal item is found. If the given text cannot be found -1 is returned.
     */
    private int getIndexForValue(String text) {
        return Arrays.asList(comboControl.getItems()).indexOf(text);
    }    
}
