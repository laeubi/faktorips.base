/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath();
    }
    
    public void testGetIndex() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        assertEquals(1, path.getEntries().length);
        
        IIpsObjectPathEntry entry0 = path.getEntries()[0];
        assertEquals(0, entry0.getIndex());
        
        IIpsObjectPathEntry entry1 = path.newArchiveEntry(ipsProject.getProject().getFile("someArchive.jar"));
        assertEquals(0, entry0.getIndex());
        assertEquals(1, entry1.getIndex());
    }

    public void testCreateFromXml() {
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(0), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_SRC_FOLDER, entry.getType());
        entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(1), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE, entry.getType());
    }

}
