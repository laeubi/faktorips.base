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

package org.faktorips.devtools.core.internal.model.ipsproject.jdtcontainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArchiveEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.jdtcontainer.JdtClasspathEntryCreator.EntryCreator;
import org.faktorips.devtools.core.internal.model.ipsproject.jdtcontainer.JdtClasspathEntryCreator.ReferenceFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdtClasspathEntryCreatorTest {

    @Mock
    private IClasspathEntry entry;

    @Mock
    private IpsObjectPath ipsObjectPath;

    @Mock
    private IPath path;

    @Mock
    private ReferenceFactory referenceFactory;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProject refProject;

    @Mock
    private IProject project;

    @Mock
    private IpsProjectRefEntry expectedProjectReference;

    @Mock
    private IpsArchiveEntry expectedArchiveEntry;

    @Mock
    private IpsBundleEntry expectedBundleEntry;

    @Mock
    private MessageList archiveMessageList;

    @Mock
    private MessageList bundleMessageList;

    private EntryCreator entryCreateor;

    @Before
    public void createJdtClasspathEntryCreator() throws Exception {
        entryCreateor = new EntryCreator(entry, ipsObjectPath);
        entryCreateor.setReferenceFactory(referenceFactory);
    }

    @Test
    public void testCreateIpsProjectRefEntry_notExisingProject() throws Exception {
        mockEntryAndPath();

        IIpsProjectRefEntry ipsProjectRefEntry = entryCreateor.createIpsProjectRefEntry();

        assertNull(ipsProjectRefEntry);
    }

    @Test
    public void testCreateIpsProjectRefEntry_ipsProjectEntry() throws Exception {
        mockEntryAndPath();
        mockReferences();
        when(refProject.exists()).thenReturn(true);

        IIpsProjectRefEntry ipsProjectRefEntry = entryCreateor.createIpsProjectRefEntry();

        assertEquals(expectedProjectReference, ipsProjectRefEntry);
    }

    @Test
    public void testCreateLibraryEntry_invalidPath() throws Exception {
        mockEntryAndPath();
        mockProject();
        mockReferences();
        when(archiveMessageList.containsErrorMsg()).thenReturn(true);
        when(bundleMessageList.containsErrorMsg()).thenReturn(true);

        IpsObjectPathEntry libraryEntry = entryCreateor.createLibraryEntry();

        assertNull(libraryEntry);
    }

    @Test
    public void testCreateLibraryEntry_archive() throws Exception {
        mockEntryAndPath();
        mockProject();
        mockReferences();

        IpsObjectPathEntry libraryEntry = entryCreateor.createLibraryEntry();

        assertEquals(expectedArchiveEntry, libraryEntry);
    }

    @Test
    public void testCreateLibraryEntry_library() throws Exception {
        mockEntryAndPath();
        mockProject();
        mockReferences();
        when(archiveMessageList.containsErrorMsg()).thenReturn(true);

        IpsObjectPathEntry libraryEntry = entryCreateor.createLibraryEntry();

        assertEquals(expectedBundleEntry, libraryEntry);
    }

    private void mockReferences() {
        when(referenceFactory.createArchiveEntry()).thenReturn(expectedArchiveEntry);
        when(expectedArchiveEntry.validate()).thenReturn(archiveMessageList);
        when(referenceFactory.createIpsBundleEntry()).thenReturn(expectedBundleEntry);
        when(expectedBundleEntry.validate()).thenReturn(bundleMessageList);
        when(referenceFactory.createProjectRefEntry(refProject)).thenReturn(expectedProjectReference);
    }

    private void mockEntryAndPath() {
        when(entry.getPath()).thenReturn(path);
        when(path.lastSegment()).thenReturn("myProjectName");
        when(referenceFactory.getIpsProject(path)).thenReturn(refProject);
    }

    private void mockProject() {
        when(ipsObjectPath.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getProject()).thenReturn(project);
    }

}