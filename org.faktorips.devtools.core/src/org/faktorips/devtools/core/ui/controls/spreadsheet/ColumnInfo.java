package org.faktorips.devtools.core.ui.controls.spreadsheet;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.controller.EditField;


/**
 *
 */
public abstract class ColumnInfo {
    
    public String columnName;
    public int style = SWT.LEFT;
    public int initialWidth = 100;
    public boolean modifiable = false;
    public Comparator comparator = DEFAULT_COMPARATOR;
    
    /**
     * 
     */
    public ColumnInfo(String columnName, int style, int initialWidth, boolean modifiable) {
        this.columnName = columnName;
        this.style = style;
        this.initialWidth = initialWidth;
        this.modifiable = modifiable;
    }
    
    public abstract Object getValue(Object rowElement);
    
    public abstract String getText(Object rowElement);
    
    public abstract Image getImage(Object rowElement);
    
    public abstract void setValue(Object rowElement, Object newValue);
    
    public abstract EditField createEditField(Table table);
    
    private final static ToStringComparator DEFAULT_COMPARATOR = new ToStringComparator();
    
    private static class ToStringComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1==null && o2==null) {
                return 0;
            }
            if (o1==null) {
                return -1;
            }
            if (o2==null) {
                return 1;    
            }
            return o1.toString().compareTo(o2.toString());
        }
        
    }

}
