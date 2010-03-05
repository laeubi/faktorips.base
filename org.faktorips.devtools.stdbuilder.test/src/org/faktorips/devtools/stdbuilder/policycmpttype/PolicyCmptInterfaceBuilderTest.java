/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class PolicyCmptInterfaceBuilderTest extends PolicyCmptTypeBuilderTest {

    private PolicyCmptInterfaceBuilder builder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        StandardBuilderSet builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
        builder = new PolicyCmptInterfaceBuilder(builderSet, DefaultBuilderSet.KIND_POLICY_CMPT_INTERFACE);
    }

    public void testGetGeneratedJavaElements() {
        List<IJavaElement> javaElements = builder.getGeneratedJavaElements(policyCmptType);
        assertTrue(javaElements.contains(getGeneratedJavaType(true)));
    }

}
