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

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.type.IType;

/**
 * An abstract class for IPS adapter factories, providing standard adaptation methods.
 */
public abstract class AbstractIpsAdapterFactory implements IAdapterFactory {

    /**
     * Getting the {@link IIpsObject} of an {@link IpsSrcFile} if the file is of this type.
     */
    protected IIpsObject adaptToIpsObject(IIpsSrcFile file) {
        if (file == null) {
            return null;
        }
        try {
            return file.getIpsObject();
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    /**
     * Getting the {@link IType} of an {@link IpsSrcFile} if the file is of this type.
     */
    protected IType adaptToType(IIpsSrcFile file) {
        if (file == null) {
            return null;
        }

        IpsObjectType type = file.getIpsObjectType();
        if (type.equals(IpsObjectType.PRODUCT_CMPT_TYPE) || type.equals(IpsObjectType.POLICY_CMPT_TYPE)) {
            try {
                return (IType)file.getIpsObject();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        return null;
    }

    /**
     * Getting the ProductCmptType of an {@link IpsSrcFile} if the file is of this type.
     */
    protected IProductCmpt adaptToProductCmpt(IIpsSrcFile file) {
        if (file == null) {
            return null;
        }
        if (file.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            try {
                return (IProductCmpt)file.getIpsObject();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        return null;
    }

}