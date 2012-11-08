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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.LinkChangingOverTimeMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.LinkWithoutAssociationEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptGenerationToTypeDeltaTest {

    @Mock
    private IProductCmptGeneration gen;
    @Mock
    private IProductCmpt prodCmpt;
    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IProductCmptLink link1;
    @Mock
    private IProductCmptLink link2;
    @Mock
    private IProductCmptLink link3;
    @Mock
    private IProductCmptLink staticLink1;
    @Mock
    private IProductCmptLink staticLink2;
    @Mock
    private IProductCmptType type;
    @Mock
    private IProductCmptTypeAssociation staticAssociation;
    @Mock
    private IProductCmptTypeAssociation assoc1;
    @Mock
    private IProductCmptTypeAssociation assoc2;
    private ProductCmptGenerationToTypeDelta delta;

    @Before
    public void setUp() throws CoreException {
        delta = spy(new ProductCmptGenerationToTypeDelta(gen, ipsProject));

        List<IProductCmptLink> genLinks = new ArrayList<IProductCmptLink>();
        genLinks.add(link1);
        genLinks.add(link2);
        genLinks.add(link3);
        when(gen.getLinksAsList()).thenReturn(genLinks);
        List<IProductCmptLink> cmptLinks = new ArrayList<IProductCmptLink>();
        cmptLinks.add(staticLink1);
        cmptLinks.add(staticLink2);
        when(prodCmpt.getLinksAsList()).thenReturn(cmptLinks);

        when(link1.getAssociation()).thenReturn("assoc1");
        when(link2.getAssociation()).thenReturn("assoc2");
        when(link3.getAssociation()).thenReturn("assoc2");
        when(link1.getProductCmptLinkContainer()).thenReturn(gen);
        when(link2.getProductCmptLinkContainer()).thenReturn(gen);
        when(link3.getProductCmptLinkContainer()).thenReturn(gen);
        when(staticLink1.getAssociation()).thenReturn("staticAssociation");
        when(staticLink2.getAssociation()).thenReturn("staticAssociation");
        when(staticLink1.getProductCmptLinkContainer()).thenReturn(prodCmpt);
        when(staticLink2.getProductCmptLinkContainer()).thenReturn(prodCmpt);

        doReturn(type).when(delta).getProductCmptType();
        doReturn(ipsProject).when(delta).getIpsProject();

        when(type.findAssociation("assoc1", ipsProject)).thenReturn(assoc1);
        when(type.findAssociation("assoc2", ipsProject)).thenReturn(assoc2);
        when(type.findAssociation("staticAssociation", ipsProject)).thenReturn(staticAssociation);
        when(gen.isContainerFor(assoc1)).thenReturn(true);
        when(gen.isContainerFor(assoc2)).thenReturn(true);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(true);
    }

    @Test
    public void testCreateLinkWithoutAssociationEntry1() throws CoreException {
        verifyAddEntryForLink("assoc1", link1);
    }

    @Test
    public void testCreateLinkWithoutAssociationEntry2() throws CoreException {
        verifyAddEntryForLink("assoc2", link2, link3);
    }

    @Test
    public void testChangingOverTimeMismatch() throws CoreException {
        when(gen.isContainerFor(assoc1)).thenReturn(false);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(true);
        ArgumentCaptor<LinkChangingOverTimeMismatchEntry> captor = ArgumentCaptor
                .forClass(LinkChangingOverTimeMismatchEntry.class);

        delta.createEntriesForLinks();

        verify(delta).addEntry(captor.capture());
        assertEquals(link1, captor.getValue().getLink());
    }

    @Test
    public void testChangingOverTimeMismatch2() throws CoreException {
        when(gen.isContainerFor(assoc2)).thenReturn(false);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(true);
        ArgumentCaptor<LinkChangingOverTimeMismatchEntry> captor = ArgumentCaptor
                .forClass(LinkChangingOverTimeMismatchEntry.class);

        delta.createEntriesForLinks();

        verify(delta, times(2)).addEntry(captor.capture());
        assertEquals(link2, captor.getAllValues().get(0).getLink());
        assertEquals(link3, captor.getAllValues().get(1).getLink());
    }

    @Test
    public void testChangingOverTimeMismatchStaticAssociation() throws CoreException {
        when(gen.isContainerFor(staticAssociation)).thenReturn(true);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(false);
        ArgumentCaptor<LinkChangingOverTimeMismatchEntry> captor = ArgumentCaptor
                .forClass(LinkChangingOverTimeMismatchEntry.class);
        // fake prod cmpt as link container so mismatch entries will be created for static links
        doReturn(prodCmpt).when(delta).getLinkContainer();

        delta.createEntriesForLinks();

        verify(delta, times(2)).addEntry(captor.capture());
        assertEquals(staticLink1, captor.getAllValues().get(0).getLink());
        assertEquals(staticLink2, captor.getAllValues().get(1).getLink());
    }

    private void verifyAddEntryForLink(String assocName, IProductCmptLink... links) throws CoreException {
        when(type.findAssociation(assocName, ipsProject)).thenReturn(null);
        ArgumentCaptor<LinkWithoutAssociationEntry> captor = ArgumentCaptor.forClass(LinkWithoutAssociationEntry.class);

        delta.createEntriesForLinks();

        verify(delta, times(links.length)).addEntry(captor.capture());
        for (int i = 0; i < links.length; i++) {
            assertEquals(links[i], captor.getAllValues().get(i).getLink());
        }
    }
}