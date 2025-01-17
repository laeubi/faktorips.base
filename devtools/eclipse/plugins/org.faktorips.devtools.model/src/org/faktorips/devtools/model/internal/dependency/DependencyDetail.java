/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.dependency;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.BeanUtil;
import org.faktorips.util.ArgumentCheck;

public class DependencyDetail implements IDependencyDetail {

    private IIpsObjectPartContainer part;
    private String propertyName;

    /**
     * Creates a new Instance. Parameters must not be <code>null</code>.
     * 
     * @param part The part of the source causing the dependency.
     * @param propertyName The name of the property causing this dependency.
     */
    public DependencyDetail(IIpsObjectPartContainer part, String propertyName) {
        ArgumentCheck.notNull(part, "The part of a DependencyDetail must not be null"); //$NON-NLS-1$
        ArgumentCheck.notNull(propertyName, "The propertyName of a DependencyDetail must not be null"); //$NON-NLS-1$

        this.part = part;
        this.propertyName = propertyName;
    }

    @Override
    public IIpsObjectPartContainer getPart() {
        return part;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public void refactorAfterRename(IIpsPackageFragment targetIpsPackageFragment, String newName) {
        try {
            updateProperty(targetIpsPackageFragment, newName);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    private void updateProperty(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws IllegalAccessException, InvocationTargetException {
        PropertyDescriptor property = BeanUtil.getPropertyDescriptor(getPart().getClass(), getPropertyName());
        String newQualifiedName = buildQualifiedName(targetIpsPackageFragment, newName);
        property.getWriteMethod().invoke(getPart(), newQualifiedName);
    }

    private String buildQualifiedName(IIpsPackageFragment ipsPackageFragment, String name) {
        return ipsPackageFragment.isDefaultPackage() ? name : ipsPackageFragment.getName() + "." + name; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(part, propertyName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof DependencyDetail)) {
            return false;
        }
        DependencyDetail other = (DependencyDetail)obj;
        return Objects.equals(part, other.part)
                && Objects.equals(propertyName, other.propertyName);
    }

}
