/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.lang.reflect.Array;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAttribute extends AbstractModelElement implements IModelTypeAttribute {

    private Class<?> datatype;
    private String datatypeName;
    private ValueSetType valueSetType = ValueSetType.AllValues;
    private AttributeType attributeType = AttributeType.changeable;
    private boolean isProductRelevant = false;

    public ModelTypeAttribute(IRuntimeRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassNotFoundException
     */
    public Class<?> getDatatype() throws ClassNotFoundException {
        if (datatype == null) {
            datatype = findDatatype();
        }
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public AttributeType getAttributeType() {
        return attributeType;
    }

    /**
     * {@inheritDoc}
     */
    public ValueSetType getValueSetType() {
        return valueSetType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("datatype")) {
                this.datatypeName = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals("valueSetType")) {
                this.valueSetType = ValueSetType.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals("attributeType")) {
                this.attributeType = AttributeType.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals("isProductRelevant")) {
                this.isProductRelevant = Boolean.valueOf(parser.getAttributeValue(i));
            }
        }
        initExtPropertiesFromXml(parser);
    }

    protected Class<?> findDatatype() throws ClassNotFoundException {
        Class<?> clazz = null;
        int arrays = 0;
        while (datatypeName.lastIndexOf('[') > 0) {
            datatypeName = datatypeName.substring(0, datatypeName.lastIndexOf('['));
            arrays++;
        }
        if (datatypeName.equals(int.class.getName())) {
            clazz = int.class;
        } else if (datatypeName.equals(boolean.class.getName())) {
            clazz = boolean.class;
        } else {
            clazz = this.getClass().getClassLoader().loadClass(datatypeName);
        }
        for (int i = 0; i < arrays; i++) {
            clazz = Array.newInstance(clazz, 0).getClass();
        }
        return clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(datatypeName);
        sb.append('(');
        sb.append(attributeType);
        sb.append(", ");
        sb.append(valueSetType);
        if (isProductRelevant) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

}
