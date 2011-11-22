/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 *
 */
public class ProductCmptPage extends WizardPage {

    private boolean canModifyRuntimeId;

    // product cmpt template
    private IProductCmpt sourceProductCmpt;

    public ProductCmptPage() {
        super("create product component");
        setTitle(Messages.ProductCmptPage_title);
        canModifyRuntimeId = IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createComposite(parent);
        // TODO Auto-generated method stub
        setControl(composite);
    }

}
