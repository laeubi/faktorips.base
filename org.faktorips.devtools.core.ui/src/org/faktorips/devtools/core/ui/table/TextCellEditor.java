/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * 
 * @author Stefan Widmaier
 */
public class TextCellEditor extends TableCellEditor {

    private Text textControl;

    public TextCellEditor(Text textControl) {
        super(textControl);
        this.textControl = textControl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        String returnValue = textControl.getText();
        if (IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(returnValue)) {
            return null;
        }
        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetFocus() {
        textControl.selectAll();
        textControl.setFocus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetValue(Object value) {
        if (null == value) {
            textControl.setText((IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()));
            return;
        }
        if (value instanceof String) {
            textControl.setText((String)value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMappedValue() {
        return false;
    }
}
