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

package org.faktorips.devtools.core.ui.actions;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.commands.IpsObjectPartStateListTransfer;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for IpsCutAction.
 * 
 * @author Thorsten Guenther
 */
public class IpsCutActionTest extends AbstractIpsPluginTest {

    IpsCutAction cutAction;
    IPolicyCmptTypeAttribute attribute;
    IPolicyCmptType pcType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject project = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("products.folder", true, null);

        IIpsSrcFile pdSrcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newPolicyCmptTypeAttribute();

        cutAction = new IpsCutAction(new TestSelectionProvider(), IpsPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getShell());

    }

    @Test
    public void testRun() {
        String current = new IpsObjectPartState(attribute).toString();

        assertEquals(1, pcType.getNumOfAttributes());
        cutAction.run();
        Clipboard clipboard = new Clipboard(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell()
                .getDisplay());
        IpsObjectPartState[] states = (IpsObjectPartState[])clipboard.getContents(new IpsObjectPartStateListTransfer(
                pcType.getClass().getClassLoader()));

        assertEquals(current, states[0].toString());

        assertEquals(0, pcType.getNumOfAttributes());
    }

    private class TestSelectionProvider implements ISelectionProvider {

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public ISelection getSelection() {
            return new StructuredSelection(attribute);
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public void setSelection(ISelection selection) {
        }
    }
}