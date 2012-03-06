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

package org.faktorips.devtools.core.ui.wizards.ipsarchiveexport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsArchivePackageWizardPage_Title;
    public static String IpsArchiveExportWizard_Export;
    public static String IpsArchivePackageWizardPage_Description_EnterDestination;
    public static String IpsArchivePackageWizardPage_Label_IncludeJavaSources;
    public static String IpsArchivePackageWizardPage_Label_IncludeJavaBinaries;
    public static String IpsArchivePackageWizardPage_Label_Target;
    public static String IpsArchivePackageWizardPage_Label_Browse;
    public static String IpsArchivePackageWizardPage_Description_EnterValidDestination;
    public static String IpsArchivePackageWizardPage_Description_DefineWhichResource;
    public static String IpsArchivePackageWizardPage_Description;
    public static String IpsArchivePackageWizardPage_WarningNoIpsProjectSelected;

}