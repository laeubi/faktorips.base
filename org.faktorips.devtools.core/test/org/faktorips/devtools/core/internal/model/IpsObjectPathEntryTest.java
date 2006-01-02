package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathEntryTest extends PluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath(ipsProject);
    }

    public void testCreateFromXml() {
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(0));
        assertEquals(IIpsObjectPathEntry.TYPE_SRC_FOLDER, entry.getType());
        entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(1));
        assertEquals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE, entry.getType());
    }

}
