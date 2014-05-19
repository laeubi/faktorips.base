/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of AbstractEnumDatatypeBasedField. This edit field selects one value out of an
 * EnumValueSet. A combo box is used to select the value. If EnumValueSet is based on an
 * EnumDatatype which supports value names, depending on the preferences these names are displayed
 * instead of the value IDs.
 * 
 * @author Peter Erzberger
 */
public class EnumValueSetField extends AbstractEnumDatatypeBasedField {

    private IValueSetOwner valueSetOwner;

    /**
     * Creates a new EnumValueSetField.
     * 
     * @param combo the control of this EditField
     * @param valueSet the value set which is displayed by this edit field
     * @param datatype the datatype the value set bases on
     * @param defaultValueField <code>true</code> if this field is created for a default value,
     *            false if it is created for other purposes.
     */
    public EnumValueSetField(Combo combo, IEnumValueSet valueSet, ValueDatatype datatype, boolean defaultValueField) {
        super(combo, datatype, defaultValueField);
        ArgumentCheck.notNull(valueSet, this);
        this.valueSetOwner = valueSet.getValueSetOwner();
        reInitInternal();
    }

    @Override
    protected List<String> getDatatypeValueIds() {
        IValueSet newValueSet = valueSetOwner.getValueSet();
        return fillList(newValueSet);
    }

    private List<String> fillList(IValueSet newValueSet) {
        List<String> ids = new ArrayList<String>();
        if (newValueSet instanceof IEnumValueSet) {
            IEnumValueSet newEnumValueSet = (IEnumValueSet)newValueSet;
            ids.addAll(Arrays.asList(newEnumValueSet.getValues()));
        }
        if (!ids.contains(null) && isDefaultValueField()) {
            // For default values there is always the option of 'no default' value
            ids.add(null);
        }
        if (ids.isEmpty()) {
            ids.add(null);
        }
        return ids;
    }

}
