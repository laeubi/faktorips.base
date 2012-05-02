/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueListExtractor;
import org.faktorips.devtools.core.ui.dialogs.MultiValueDialog;
import org.faktorips.devtools.core.ui.dialogs.MultiValueSubsetDialog;

public class MultiValueAttributeHandler {
    private final IAttributeValue attributeValue;
    private final Shell shell;
    private final ValueDatatype datatype;
    private final IValueSet modelValueSet;

    public MultiValueAttributeHandler(Shell shell, IProductCmptTypeAttribute productCmptTypeAttribute,
            IAttributeValue attributeValue, ValueDatatype datatype) {
        this.shell = shell;
        this.attributeValue = attributeValue;
        this.datatype = datatype;
        modelValueSet = productCmptTypeAttribute.getValueSet();
    }

    private boolean isEnumValueSet() {
        return modelValueSet instanceof IEnumValueSet;
    }

    public void editValues() {
        if (isEnumValueSet()) {
            openMultiValueSubsetDialog((IEnumValueSet)modelValueSet);
        } else {
            if (datatype.isEnum()) {
                openMultiValueSubsetDialog((EnumDatatype)datatype);
            } else {
                openMultiValueDialog();
            }
        }
    }

    protected void openMultiValueSubsetDialog(EnumDatatype enumDatatype) {
        openMultiValueSubsetDialog(ValueListExtractor.extractValues(enumDatatype));
    }

    protected void openMultiValueSubsetDialog(IEnumValueSet sourceValueSet) {
        openMultiValueSubsetDialog(sourceValueSet.getValuesAsList());
    }

    protected void openMultiValueSubsetDialog(List<String> sourceValues) {
        new MultiValueSubsetDialog(shell, sourceValues, attributeValue, datatype).open();
        // changes are applied directly to the provided target value set
    }

    protected void openMultiValueDialog() {
        MultiValueDialog multiValueDialog = new MultiValueDialog(shell, attributeValue, datatype);
        multiValueDialog.open();
        // values are applied in the dialog's okPressed() method
    }

}