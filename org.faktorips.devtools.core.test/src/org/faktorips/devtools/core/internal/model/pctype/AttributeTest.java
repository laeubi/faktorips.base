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

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.EnumValueSet;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 */
public class AttributeTest extends AbstractIpsPluginTest {
    
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile ipsSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptTypeAttribute attribute;
    private IIpsProject project;
    
    protected void setUp() throws Exception {
        super.setUp();
        project = this.newIpsProject("TestProject");
        ipsRootFolder = project.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("products.folder", true, null);
        ipsSrcFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)ipsSrcFile.getIpsObject();
        attribute = pcType.newPolicyCmptTypeAttribute();
    }
    
    public void testFindOverwrittenAttribute() throws CoreException {
        attribute.setName("a");
        IPolicyCmptType supertype = newPolicyCmptType(project, "Supertype");
        IPolicyCmptType supersupertype = newPolicyCmptType(project, "SuperSupertype");
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        assertNull(attribute.findOverwrittenAttribute(project));
        
        IPolicyCmptTypeAttribute aInSupertype = supersupertype.newPolicyCmptTypeAttribute();
        aInSupertype.setName("a");
        
        assertEquals(aInSupertype, attribute.findOverwrittenAttribute(project));
        
        // cycle in type hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        assertEquals(aInSupertype, attribute.findOverwrittenAttribute(project));

        aInSupertype.delete();
        assertNull(attribute.findOverwrittenAttribute(project)); // this should not return a itself!
    }
    
    public void testGetConfigElementType() {
        attribute.setProductRelevant(false);
        assertNull(attribute.getConfigElementType());
        
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        assertEquals(ConfigElementType.POLICY_ATTRIBUTE, attribute.getConfigElementType());

        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        assertNull(attribute.getConfigElementType());
        
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        assertNull(attribute.getConfigElementType());
    }
    
    public void testRemove() {
        attribute.delete();
        assertEquals(0, pcType.getPolicyCmptTypeAttributes().length);
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testSetDatatype() {
        attribute.setDatatype("Money");
        assertEquals("Money", attribute.getDatatype());
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testSetComputed() {
        attribute.setProductRelevant(true);
        assertEquals(true, attribute.isProductRelevant());
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        Element root =(Element)doc.getDocumentElement();
        NodeList nl =root.getElementsByTagName("Attribute");
        attribute.initFromXml((Element)nl.item(0));
        assertEquals(42, attribute.getId());
        assertEquals("premium", attribute.getName());
        assertEquals("money", attribute.getDatatype());
        assertFalse(attribute.isProductRelevant());
        assertEquals(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL, attribute.getAttributeType());
        assertEquals("42EUR", attribute.getDefaultValue());
        assertNotNull(attribute.getValueSet());
        assertFalse(attribute.getOverwrites());
        
        attribute.initFromXml((Element)nl.item(1));
        assertEquals(2, attribute.getId());
        assertNull(attribute.getDefaultValue());
        assertNotNull(attribute.getValueSet());
        assertEquals(EnumValueSet.class,attribute.getValueSet().getClass());

        attribute.initFromXml((Element)nl.item(2));
        assertTrue(attribute.getOverwrites());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXml() {
        attribute = pcType.newPolicyCmptTypeAttribute();  // => id=1 as this is the type's 2 attribute
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        attribute.setOverwrites(false);
        attribute.setValueSetType(ValueSetType.RANGE);
        RangeValueSet set = (RangeValueSet)attribute.getValueSet();
        set.setLowerBound("unten");
        set.setUpperBound("oben");
        set.setStep("step");
        Element element = attribute.toXml(this.newDocument());
        
        IPolicyCmptTypeAttribute copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("age", copy.getName());
        assertEquals("decimal", copy.getDatatype());
        assertTrue(copy.isProductRelevant());
        assertFalse(copy.getOverwrites());
        assertEquals(AttributeType.CONSTANT, copy.getAttributeType());
        assertEquals("18", copy.getDefaultValue());
        assertEquals("unten",((IRangeValueSet)copy.getValueSet()).getLowerBound());
        assertEquals("oben",((IRangeValueSet)copy.getValueSet()).getUpperBound());
        assertEquals("step",((IRangeValueSet)copy.getValueSet()).getStep());

        // Nun ein Attribut mit GenericEnumvalueset testen.
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        attribute.setValueSetType(ValueSetType.ENUM);
        EnumValueSet set2 = (EnumValueSet)attribute.getValueSet();
        set2.addValue("a");
        set2.addValue("b");
        set2.addValue("x");
        
        element = attribute.toXml(this.newDocument());
        copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertEquals("age", attribute.getName());
        assertEquals("decimal", attribute.getDatatype());
        assertTrue(attribute.isProductRelevant());
        assertEquals(AttributeType.CONSTANT, attribute.getAttributeType());
        assertEquals("18", attribute.getDefaultValue());
        String [] vekt = ((IEnumValueSet)copy.getValueSet()).getValues();
        assertEquals("a", vekt[0]);
        assertEquals("b", vekt[1]);
        assertEquals("x", vekt[2]);
        
        // and now an attribute which overwrites
        attribute.setOverwrites(true);
        element = attribute.toXml(this.newDocument());
        copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertTrue(attribute.getOverwrites());
        assertEquals("", attribute.getDatatype());
    }
    
    /**
     * Tests for the correct type of excetion to be thrwon - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			attribute.newPart(IPolicyCmptTypeAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testValidate_productRelevant() throws Exception {
    	pcType.setConfigurableByProductCmptType(true);
    	attribute.setProductRelevant(true);
    	
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    	
    	pcType.setConfigurableByProductCmptType(false);
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    }

    public void testOverwrites() throws Exception {
    	IPolicyCmptType supersupertype = newPolicyCmptType(project, "sup.SuperSuperType");
    	IPolicyCmptTypeAttribute supersuperAttr = supersupertype.newPolicyCmptTypeAttribute();
    	supersuperAttr.setDatatype("superDatatype");
    	supersuperAttr.setProductRelevant(false);
    	supersuperAttr.setModifier(Modifier.PUBLIC);
    	supersuperAttr.setAttributeType(AttributeType.CHANGEABLE);
    	supersuperAttr.setName("name");

    	pcType.setSupertype(supersupertype.getQualifiedName());
    	attribute.setDatatype("Datatype");
    	attribute.setProductRelevant(true);
    	attribute.setModifier(Modifier.PUBLISHED);
    	attribute.setAttributeType(AttributeType.CONSTANT);
    	attribute.setName("name");
    	
    	assertFalse(attribute.getDatatype().equals(supersuperAttr.getDatatype()));
    	assertFalse(attribute.isProductRelevant() == supersuperAttr.isProductRelevant());
    	assertFalse(attribute.getModifier() == supersuperAttr.getModifier());
    	assertFalse(attribute.getAttributeType() == supersuperAttr.getAttributeType());
    	
    	attribute.setOverwrites(true);
    	assertTrue(attribute.getDatatype().equals(supersuperAttr.getDatatype()));
    	assertTrue(attribute.isProductRelevant() == supersuperAttr.isProductRelevant());
    	assertTrue(attribute.getModifier() == supersuperAttr.getModifier());
    	assertTrue(attribute.getAttributeType() == supersuperAttr.getAttributeType());

    	IPolicyCmptType supertype = newPolicyCmptType(project, "sup.SuperType");
    	pcType.setSupertype(supertype.getQualifiedName());
    	supertype.setSupertype(supersupertype.getQualifiedName());
    	
    	IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute();
    	superAttr.setName("name");
    	superAttr.setOverwrites(true);

    	assertTrue(attribute.getDatatype().equals(supersuperAttr.getDatatype()));
    	assertTrue(attribute.isProductRelevant() == supersuperAttr.isProductRelevant());
    	assertTrue(attribute.getModifier() == supersuperAttr.getModifier());
    	assertTrue(attribute.getAttributeType() == supersuperAttr.getAttributeType());
    	
    }
    
    public void testValidate_nothingToOverwrite() throws Exception {
    	attribute.setName("name");

    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));
    	
    	attribute.setOverwrites(true);
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));
    	
    	IPolicyCmptType supertype = newPolicyCmptType(project, "sup.SuperType");
    	IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute();
    	superAttr.setName("name");
    	pcType.setSupertype(supertype.getQualifiedName());
    	
    	ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));
    }

    public void testValidate_nameCollision() throws Exception {
    	IPolicyCmptType supertype = newPolicyCmptType(project, "sup.SuperType");
    	IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute();
    	superAttr.setName("name");
    	pcType.setSupertype(supertype.getQualifiedName());    	
    	attribute.setName("name");
    	
    	MessageList ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NAME_COLLISION));
    	
    	attribute.setName("abc");
    	ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NAME_COLLISION));
    }
}

    