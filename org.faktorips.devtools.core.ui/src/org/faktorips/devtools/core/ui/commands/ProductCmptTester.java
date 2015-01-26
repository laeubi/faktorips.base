/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * This tester is used to test {@link IProductCmpt} properties. The properties to test are defined
 * in the org.faktorips.devtools.core.ui.plugin.xml file.
 */
public class ProductCmptTester extends PropertyTester {

    private static final String ALLOW_GENERATION = "allowGenerations"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof IProductCmpt) {
            if (ALLOW_GENERATION.equals(property)) {
                return allowGenerations((IProductCmpt)receiver);
            }
        }
        return false;
    }

    private boolean allowGenerations(IProductCmpt productCmpt) {
        return productCmpt.allowGenerations();
    }

}
