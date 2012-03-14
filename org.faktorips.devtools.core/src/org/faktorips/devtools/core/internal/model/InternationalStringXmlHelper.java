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

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.model.IInternationalString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Helper class to load {@link IInternationalString} from XML and sore the to XML.
 * <p>
 * The helper should not be initialized, just use the static util methods.
 * 
 * @author dirmeier
 */
public final class InternationalStringXmlHelper {

    private InternationalStringXmlHelper() {
        // do not instatiate
    }

    /**
     * Stores the given {@link IInternationalString} to a new XML element that is a child of the
     * given parent element. The name of the new element is given by the parameter xmlTagName
     * 
     * @param internationalString The international string to save
     * @param parentElement the parent XML element
     * @param xmlTagName the name of the new XML element holding the international string
     */
    public static void toXml(IInternationalString internationalString, Element parentElement, String xmlTagName) {
        Document doc = parentElement.getOwnerDocument();
        Element msgTextElement = doc.createElement(xmlTagName);
        parentElement.appendChild(msgTextElement);
        msgTextElement.appendChild(internationalString.toXml(doc));
    }

    /**
     * Loads an {@link IInternationalString} from an XML element. This method loads the first child
     * element with the XML tag name {@link InternationalString#XML_TAG}.
     * 
     * @param internationalString An instance of international string that will be initialized with
     *            the XML content
     * @param element The XML element holding the international string element
     */
    public static void initFromXml(IInternationalString internationalString, Element element) {
        NodeList childNodes = element.getElementsByTagName(InternationalString.XML_TAG);
        for (int j = 0; j < childNodes.getLength(); j++) {
            if (childNodes.item(j) instanceof Element) {
                Element internationalStringElement = (Element)childNodes.item(j);
                internationalString.initFromXml(internationalStringElement);
                break;
            }
        }
    }

}
