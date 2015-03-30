/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.valueset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UnrestrictedValueSetTest extends AbstractIpsPluginTest {

    private static final String MY_ENUM_CONTENT = "MyEnumContent";

    private static final String MY_EXTENSIBLE_ENUM = "MyExtensibleEnum";

    private static final String MY_SUPER_ENUM = "MySuperEnum";

    private IPolicyCmptTypeAttribute attr;
    private IConfigElement ce;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

    private IPolicyCmptType policyCmptType;

    private IIpsProject productIpsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject();
        productIpsProject = super.newIpsProject();
        IIpsObjectPath ipsObjectPath = productIpsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject);
        productIpsProject.setIpsObjectPath(ipsObjectPath);
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "test.Base", "test.Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());

        IProductCmpt cmpt = newProductCmpt(productIpsProject, "test.Product");
        cmpt.setProductCmptType(productCmptType.getQualifiedName());
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("attr");

        EnumType enumType = newEnumType(ipsProject, MY_EXTENSIBLE_ENUM);
        enumType.setExtensible(true);
        enumType.setSuperEnumType(MY_SUPER_ENUM);
        enumType.setEnumContentName(MY_ENUM_CONTENT);
        EnumContent newEnumContent = newEnumContent(productIpsProject, MY_ENUM_CONTENT);
        newEnumContent.setEnumType(MY_EXTENSIBLE_ENUM);

        EnumType superEnumType = newEnumType(ipsProject, MY_SUPER_ENUM);
        superEnumType.setAbstract(true);
    }

    @Test
    public void testUnrestrictedValueSet() {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        assertTrue(unrestricted.isContainsNull());

        unrestricted = new UnrestrictedValueSet(ce, "1", true);
        assertTrue(unrestricted.isContainsNull());

        unrestricted = new UnrestrictedValueSet(ce, "1", false);
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testPropertiesToXml() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        Element xml = unrestricted.toXml(newDocument());

        IUnrestrictedValueSet unrestricted2 = new UnrestrictedValueSet(ce, "1");
        unrestricted2.initFromXml(xml);
        assertTrue(unrestricted2.isContainsNull());
    }

    @Test
    public void testInitPropertiesFromXml() throws Exception {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();

        // first
        Element element = XmlUtil.getFirstElement(root);
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.initFromXml(element);
        assertFalse(unrestricted.isContainsNull());

        // second
        element = XmlUtil.getElement(root, 1);
        unrestricted = new UnrestrictedValueSet(ce, "2");
        unrestricted.initFromXml(element);
        assertTrue(unrestricted.isContainsNull());

        // third
        element = XmlUtil.getElement(root, 2);
        unrestricted = new UnrestrictedValueSet(ce, "3");
        unrestricted.initFromXml(element);
        assertTrue(unrestricted.isContainsNull());
    }

    @Test
    public void testCopy() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.setContainsNull(false);

        IUnrestrictedValueSet unrestricted2 = (UnrestrictedValueSet)unrestricted.copy(ce, "2");
        assertFalse(unrestricted2.isContainsNull());
    }

    @Test
    public void testCopyPropertiesFrom() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.setContainsNull(false);

        IUnrestrictedValueSet unrestricted2 = new UnrestrictedValueSet(ce, "2");
        assertTrue(unrestricted2.isContainsNull());

        unrestricted2.copyFrom(unrestricted);
        assertFalse(unrestricted2.isContainsNull());
    }

    @Test
    public void testIsContainsNull() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        assertTrue(unrestricted.isContainsNull());
    }

    @Test
    public void testSetContainsNull() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");
        unrestricted.setContainsNull(false);
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testIsContainsNullPrimitive() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(ce, "1");

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testContainsValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", false);

        assertTrue(unrestrictedValueSet.containsValue("10EUR", ipsProject));
    }

    @Test
    public void testContainsValue_DatatypeIsNull() throws Exception {
        attr.setDatatype(null);
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", false);

        assertFalse(unrestrictedValueSet.containsValue("someValue", ipsProject));
    }

    @Test
    public void testContainsValue_isNotParsableValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", false);

        assertFalse(unrestrictedValueSet.containsValue("notParsable", ipsProject));
    }

    @Test
    public void testContainsValue_ValueIsNull_UnrestrictedValueSetWithoutNull() throws Exception {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", false);

        assertFalse(unrestrictedValueSet.containsValue(null, ipsProject));
    }

    @Test
    public void testContainsValue_ValueIsNull_UnrestrictedValueSetWithNull() throws Exception {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", true);

        assertTrue(unrestrictedValueSet.containsValue(null, ipsProject));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithoutNull() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", false);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(ce, "1", false);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithNull() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(ce, "1", true);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSet() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(ce, "1", false);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSetNot() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(ce, "1", false);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(ce, "1", true);

        assertFalse(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_enum() {
        attr.setDatatype(MY_EXTENSIBLE_ENUM);

        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(ce, "1", true);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_differentDatatypes() throws Exception {
        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr");
        attr2.setDatatype(MY_EXTENSIBLE_ENUM);

        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(attr2, "1", true);

        assertFalse(subSet.containsValueSet(unrestrictedValueSet));
    }

    @Test
    public void testContainsValueSet_covariant() throws Exception {
        attr.setDatatype(MY_SUPER_ENUM);
        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr");
        attr2.setDatatype(MY_EXTENSIBLE_ENUM);

        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(attr2, "1", true);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
        assertFalse(subSet.containsValueSet(unrestrictedValueSet));
    }

}
