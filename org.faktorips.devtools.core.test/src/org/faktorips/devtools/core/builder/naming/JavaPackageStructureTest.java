/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder.naming;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JavaPackageStructureTest {

    @Mock
    private JavaSourceFileBuilder builder;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    private JavaPackageStructure packageStructure;

    @Mock
    private IIpsPackageFragment packageFragment;

    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        packageStructure = new JavaPackageStructure();

        when(ipsSrcFile.getBasePackageNameForMergableArtefacts()).thenReturn("mergable");
        when(ipsSrcFile.getBasePackageNameForDerivedArtefacts()).thenReturn("derived");
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(packageFragment);
        when(packageFragment.getName()).thenReturn("ubx");
    }

    @Test
    public void testGetPackageMergablePublished() throws Exception {
        String package1 = packageStructure.getPackageName(ipsSrcFile, true, true);
        assertEquals("mergable.ubx", package1);
    }

    @Test
    public void testGetPackageDerivedPublished() throws Exception {
        String package1 = packageStructure.getPackageName(ipsSrcFile, true, false);
        assertEquals("derived.ubx", package1);
    }

    @Test
    public void testGetPackageMergableInternal() throws Exception {
        String package1 = packageStructure.getPackageName(ipsSrcFile, false, true);
        assertEquals("mergable.internal.ubx", package1);
    }

    @Test
    public void testGetPackageDerivedInternal() throws Exception {
        when(builder.isBuildingPublishedSourceFile()).thenReturn(false);
        when(builder.buildsDerivedArtefacts()).thenReturn(true);

        String package1 = packageStructure.getPackageName(ipsSrcFile, false, false);
        assertEquals("derived.internal.ubx", package1);
    }

}