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

package org.faktorips.devtools.core.model.extproperties;

import static org.junit.Assert.assertEquals;

import javax.naming.directory.Attribute;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.devtools.core.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPartStringExtPropertyTest {

    private StringExtensionPropertyDefinition property;

    @Before
    public void setUp() throws Exception {
        property = new StringExtensionPropertyDefinition();
        property.setPropertyId("id");
        property.setExtendedType(Attribute.class);
        property.setDefaultValue("defaultValue");
    }

    @Test
    public void testValueToXml() throws ParserConfigurationException, FactoryConfigurationError {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = doc.createElement("Value");

        // not null
        property.valueToXml(el, "blabla");
        assertEquals("blabla", XmlUtil.getFirstCDataSection(el).getData());
        assertEquals("blabla", property.getValueFromXml(el));

        // not null, special characters
        el = doc.createElement("Value");
        property.valueToXml(el, "<>&");
        assertEquals("<>&", XmlUtil.getFirstCDataSection(el).getData());
        assertEquals("<>&", property.getValueFromXml(el));
    }

    @Test
    public void testGetValueFromXml() throws ParserConfigurationException, FactoryConfigurationError {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = doc.createElement("Value");

        // not null
        el.appendChild(doc.createCDATASection("blabla"));
        assertEquals("blabla", property.getValueFromXml(el));

        // not null, special characters
        el = doc.createElement("Value");
        el.appendChild(doc.createCDATASection("<>&"));
        assertEquals("<>&", property.getValueFromXml(el));
    }

    @Test
    public void testSetDefaultValue() {
        property.setDefaultValue("blabla");
        assertEquals("blabla", property.getDefaultValue());
    }

}