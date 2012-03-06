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

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * Delta entry for a product definition property.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaEntryForProperty extends IDeltaEntry {

    /**
     * Returns the type of the property this entry refers.
     */
    public ProductCmptPropertyType getPropertyType();

    /**
     * Returns the name of the product definition property this entry relates.
     */
    public String getPropertyName();

}