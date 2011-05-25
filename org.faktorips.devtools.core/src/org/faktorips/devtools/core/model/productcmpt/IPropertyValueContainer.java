/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

public interface IPropertyValueContainer extends IIpsObjectPartContainer {

    /**
     * Returns the property value for the given property or <code>null</code> if no value is defined
     * for this generation. In this case {@link #computeDeltaToModel(IIpsProject)} returns a delta
     * containing an entry for the missing property value.
     * <p>
     * Returns <code>null</code> if property is <code>null</code>.
     * <p>
     * Note that this method searches only the property values that have the same property type as
     * the indicated property. If you want to search only by name, use
     * {@link #getPropertyValue(String)}.
     */
    public abstract IPropertyValue getPropertyValue(IProductCmptProperty property);

    /**
     * Returns the property values for the given property name or <code>null</code> if no value is
     * defined for this generation. In this case {@link #computeDeltaToModel(IIpsProject)} returns a
     * delta containing an entry for the missing property value.
     * <p>
     * Returns <code>null</code> if propertyName is <code>null</code>.
     */
    public abstract IPropertyValue getPropertyValue(String propertyName);

    /**
     * Returns all property values for the given type. Returns an empty array if type is
     * <code>null</code> or no property values were found for the given type.
     */
    public abstract <T extends IPropertyValue> List<T> getPropertyValues(Class<T> type);

    /**
     * Creates a new property value for the given property.
     * 
     * @throws NullPointerException if property is <code>null</code>.
     */
    public abstract IPropertyValue newPropertyValue(IProductCmptProperty property);

    /**
     * Check whether this container contains values that change over time or only contains not
     * changing values. If method returns true for all {@link IPropertyValue} in this container, the
     * corresponding {@link IProductCmptProperty#isChangingOverTime()} have to return true.
     * 
     * @return True if the values does change over time. False if it is constant
     */
    public boolean isChangingOverTimeContainer();

    /**
     * Returns the delta between this product component and it's product component type.
     * 
     * @param ipsProject The ips project which search path is used to search the type.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IPropertyValueContainerToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the qualified name of the product component type this property value container is
     * based on.
     */
    public String getProductCmptType();

    /**
     * Finding the {@link IProductCmptType} this this property value container is based on.
     * 
     * @param ipsProject The {@link IIpsProject} used as base project to search
     * @return the product component type or null if no one was found
     * @throws CoreException in case of getting a core exception while searching the model
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

}