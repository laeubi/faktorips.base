/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Interface that marks an object as owning a value set.
 * 
 * @author Jan Ortmann
 */
public interface IValueSetOwner extends IIpsObjectPart {

    public static final String PROPERTY_VALUE_SET = "valueSet"; //$NON-NLS-1$

    /**
     * Returns the set of allowed values.
     */
    public IValueSet getValueSet();

    /**
     * Returns the list of allowed value set types.
     * 
     * @throws CoreRuntimeException If an error occurs.
     */
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Sets the type of the value set.
     */
    public void setValueSetType(ValueSetType type);

    /**
     * Changes the type of the value set to the new type. The old value set is removed and a new
     * value set of the given new type is created. Returns the new value set.
     * 
     * @param newType The new value set type.
     */
    public IValueSet changeValueSetType(ValueSetType newType);

    /**
     * Returns <code>true</code> if it is possible to update the value set owned by this owner.
     */
    public boolean isValueSetUpdateable();

    /**
     * Returns the value data type all values in the value set must be "instances" of.
     * 
     * @param ipsProject The project which IPS object path is used to search the data type. This is
     *            not necessarily the project this value set owner is part of.
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject);

}
