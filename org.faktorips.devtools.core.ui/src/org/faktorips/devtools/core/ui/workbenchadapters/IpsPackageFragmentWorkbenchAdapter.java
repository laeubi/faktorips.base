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

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.Messages;

public class IpsPackageFragmentWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
            try {
                IIpsElement[] children = packageFragment.getChildren();
                if (children != null && children.length > 0) {
                    return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsPackageFragment.gif", true); //$NON-NLS-1$
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsPackageFragmentEmpty.gif", true); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("IpsPackageFragment.gif", true); //$NON-NLS-1$
    }

    @Override
    protected String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
            if (StringUtils.isEmpty(packageFragment.getName())) {
                return Messages.DefaultLabelProvider_labelDefaultPackage;
            }
        }
        return super.getLabel(ipsElement);
    }

}