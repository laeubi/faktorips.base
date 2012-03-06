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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.runtime.IValidationContext;
import org.junit.Before;
import org.junit.Test;

public class GenChangeableAttributeTest extends GenPolicyCmptTypeAttributeTest {

    private static final String SUB_POLICY_NAME = "SubPolicy";

    private GenChangeableAttribute genPublishedAttribute;

    private GenChangeableAttribute genPublicAttribute;

    private GenChangeableAttribute genOverwrittenPublishedAttribute;

    private GenChangeableAttribute genOverwrittenPublicAttribute;

    private IPolicyCmptType subPolicyCmptType;

    private IPolicyCmptTypeAttribute overwrittenPublishedAttribute;

    private IPolicyCmptTypeAttribute overwrittenPublicAttribute;

    private IType subJavaInterface;

    private IType subJavaClass;

    private GenPolicyCmptType genSubPolicyCmptType;

    public GenChangeableAttributeTest() {
        super(AttributeType.CHANGEABLE);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        genPublishedAttribute = new GenChangeableAttribute(genPolicyCmptType, publishedAttribute);
        genPublicAttribute = new GenChangeableAttribute(genPolicyCmptType, publicAttribute);

        subPolicyCmptType = newPolicyCmptType(ipsProject, SUB_POLICY_NAME);
        subPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());

        overwrittenPublishedAttribute = subPolicyCmptType.newPolicyCmptTypeAttribute();
        overwrittenPublishedAttribute.setName(publishedAttribute.getName());
        overwrittenPublishedAttribute.setOverwrite(true);
        overwrittenPublishedAttribute.setDatatype(publishedAttribute.getDatatype());
        overwrittenPublishedAttribute.setAttributeType(publishedAttribute.getAttributeType());
        overwrittenPublishedAttribute.setModifier(publishedAttribute.getModifier());

        overwrittenPublicAttribute = subPolicyCmptType.newPolicyCmptTypeAttribute();
        overwrittenPublicAttribute.setName(publicAttribute.getName());
        overwrittenPublicAttribute.setOverwrite(true);
        overwrittenPublicAttribute.setDatatype(publicAttribute.getDatatype());
        overwrittenPublicAttribute.setAttributeType(publicAttribute.getAttributeType());
        overwrittenPublicAttribute.setModifier(publicAttribute.getModifier());

        subJavaInterface = getGeneratedJavaInterface(subPolicyCmptType, false,
                builderSet.getBuildersByClass(PolicyCmptInterfaceBuilder.class).get(0), SUB_POLICY_NAME);
        subJavaClass = getGeneratedJavaClass(subPolicyCmptType, false,
                builderSet.getBuildersByClass(PolicyCmptImplClassBuilder.class).get(0), SUB_POLICY_NAME);

        genSubPolicyCmptType = new GenPolicyCmptType(subPolicyCmptType, builderSet);
        genOverwrittenPublishedAttribute = new GenChangeableAttribute(genSubPolicyCmptType,
                overwrittenPublishedAttribute);
        genOverwrittenPublicAttribute = new GenChangeableAttribute(genSubPolicyCmptType, overwrittenPublicAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenPublishedAttribute() {
        genPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publishedAttribute);
        expectationsForPublishedInterfaceGivenPublishedAttribute(genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenPublicAttribute() {
        genPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        expectationsForPublishedInterfaceGivenPublicAttribute();
    }

    private void expectationsForPublishedInterfaceGivenPublicAttribute() {
        assertTrue(generatedJavaElements.isEmpty());
    }

    private void expectationsForPublishedInterfaceGivenPublishedAttribute(GenChangeableAttribute genChangeableAttribute) {
        expectPropertyConstant(0, javaInterface, genChangeableAttribute);
        expectGetterMethod(javaInterface, genChangeableAttribute);
        expectSetterMethod(javaInterface, genChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenProductRelevantPublishedAttribute()
            throws CoreException {

        publishedAttribute.setProductRelevant(true);
        genPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publishedAttribute);
        expectationsForPublishedInterfaceGivenProductRelevantPublishedAttribute(genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenProductRelevantPublicAttribute() {
        publicAttribute.setProductRelevant(true);
        genPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        expectationsForPublishedInterfaceGivenProductRelevantPublicAttribute();
    }

    private void expectationsForPublishedInterfaceGivenProductRelevantPublishedAttribute(GenChangeableAttribute genChangeableAttribute)
            throws CoreException {

        expectPropertyConstant(0, javaInterface, genChangeableAttribute);
        expectGetterMethod(javaInterface, genChangeableAttribute);
        expectSetterMethod(javaInterface, genChangeableAttribute);
        expectGetValueSetMethod(javaInterface, genChangeableAttribute);

        IType javaInterfaceProductGen = genPublishedAttribute.findGeneratedJavaTypeForProductCmptTypeGen(true);
        expectGetDefaultValueMethod(javaInterfaceProductGen, genChangeableAttribute);
        expectGetValueSetMethod(javaInterfaceProductGen, genChangeableAttribute);
        assertEquals(6, generatedJavaElements.size());
    }

    private void expectationsForPublishedInterfaceGivenProductRelevantPublicAttribute() {
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenOverwrittenPublishedAttribute() {
        genOverwrittenPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                subJavaInterface, overwrittenPublishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenOverwrittenPublicAttribute() {
        genOverwrittenPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                subJavaClass, overwrittenPublicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenProductRelevantOverwrittenPublishedAttribute() {
        publishedAttribute.setProductRelevant(true);
        overwrittenPublishedAttribute.setProductRelevant(true);

        genOverwrittenPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                subJavaInterface, overwrittenPublishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenProductRelevantOverwrittenPublicAttribute() {
        publicAttribute.setProductRelevant(true);
        overwrittenPublicAttribute.setProductRelevant(true);

        genOverwrittenPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                subJavaClass, overwrittenPublicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenPublishedAttribute() {
        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectationsForImplementationGivenPublishedAttribute(genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenPublicAttribute() {
        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, publicAttribute);
        expectationsForImplementationGivenPublicAttribute(genPublicAttribute);
    }

    private void expectationsForImplementationGivenPublishedAttribute(GenChangeableAttribute genChangeableAttribute) {
        expectMemberVar(0, javaClass, genChangeableAttribute);
        expectGetterMethod(javaClass, genChangeableAttribute);
        expectSetterMethod(javaClass, genChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());
    }

    private void expectationsForImplementationGivenPublicAttribute(GenChangeableAttribute genChangeableAttribute) {
        expectPropertyConstant(0, javaClass, genChangeableAttribute);
        expectMemberVar(1, javaClass, genChangeableAttribute);
        expectGetterMethod(javaClass, genChangeableAttribute);
        expectSetterMethod(javaClass, genChangeableAttribute);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenProductRelevantPublishedAttribute()
            throws CoreException {

        publishedAttribute.setProductRelevant(true);
        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectationsForImplementationGivenProductRelevantPublishedAttribute(genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenProductRelevantPublicAttribute() throws CoreException {
        publicAttribute.setProductRelevant(true);
        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, publicAttribute);
        expectationsForImplementationGivenProductRelevantPublicAttribute(genPublicAttribute);
    }

    private void expectationsForImplementationGivenProductRelevantPublishedAttribute(GenChangeableAttribute genChangeableAttribute)
            throws CoreException {

        expectMemberVar(0, javaClass, genChangeableAttribute);
        expectGetterMethod(javaClass, genChangeableAttribute);
        expectSetterMethod(javaClass, genChangeableAttribute);
        expectGetValueSetMethod(javaClass, genChangeableAttribute);

        IType javaClassProductGen = genPublishedAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false);
        expectDefaultMemberVariable(4, javaClassProductGen, genChangeableAttribute);
        expectValueSetMemberVariable(5, javaClassProductGen, genChangeableAttribute);
        expectGetDefaultValueMethod(javaClassProductGen, genChangeableAttribute);
        expectGetValueSetMethod(javaClassProductGen, genChangeableAttribute);
        assertEquals(8, generatedJavaElements.size());
    }

    private void expectationsForImplementationGivenProductRelevantPublicAttribute(GenChangeableAttribute genChangeableAttribute)
            throws CoreException {

        expectPropertyConstant(0, javaClass, genChangeableAttribute);
        expectMemberVar(1, javaClass, genChangeableAttribute);
        expectGetterMethod(javaClass, genChangeableAttribute);
        expectSetterMethod(javaClass, genChangeableAttribute);
        expectGetValueSetMethod(javaClass, genChangeableAttribute);

        IType javaClassProductGen = genPublicAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false);
        expectDefaultMemberVariable(5, javaClassProductGen, genChangeableAttribute);
        expectValueSetMemberVariable(6, javaClassProductGen, genChangeableAttribute);
        expectGetDefaultValueMethod(javaClassProductGen, genChangeableAttribute);
        expectGetValueSetMethod(javaClassProductGen, genChangeableAttribute);
        assertEquals(9, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenOverwrittenPublishedAttribute() {
        genOverwrittenPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, subJavaClass,
                overwrittenPublishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenOverwrittenPublicAttribute() {
        genOverwrittenPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, subJavaClass,
                overwrittenPublicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenProductRelevantOverwrittenPublishedAttribute() {
        publishedAttribute.setProductRelevant(true);
        overwrittenPublishedAttribute.setProductRelevant(true);

        genOverwrittenPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, subJavaClass,
                overwrittenPublishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenProductRelevantOverwrittenPublicAttribute() {
        publicAttribute.setProductRelevant(true);
        overwrittenPublicAttribute.setProductRelevant(true);

        genOverwrittenPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, subJavaClass,
                overwrittenPublicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenPublishedAttributeValueSetEnum() {
        publishedAttribute.setValueSetType(ValueSetType.ENUM);

        genPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publishedAttribute);
        expectValueSetConstant(1, javaInterface, genPublishedAttribute);
        expectGetValueSetMethod(javaInterface, genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenPublicAttributeValueSetEnum() {
        publicAttribute.setValueSetType(ValueSetType.ENUM);

        genPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenPublishedAttributeValueSetEnum() {
        publishedAttribute.setValueSetType(ValueSetType.ENUM);

        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectGetValueSetMethod(javaClass, genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenPublicAttributeValueSetEnum() {
        publicAttribute.setValueSetType(ValueSetType.ENUM);

        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, publicAttribute);
        expectValueSetConstant(1, javaClass, genPublicAttribute);
        expectGetValueSetMethod(javaClass, genPublicAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenPublishedAttributeValueSetRange() {
        publishedAttribute.setValueSetType(ValueSetType.RANGE);

        genPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publishedAttribute);
        expectValueSetConstant(1, javaInterface, genPublishedAttribute);
        expectGetValueSetMethod(javaInterface, genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceGivenPublicAttributeValueSetRange() {
        publicAttribute.setValueSetType(ValueSetType.RANGE);

        genPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenPublishedAttributeValueSetRange() {
        publishedAttribute.setValueSetType(ValueSetType.RANGE);

        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectGetValueSetMethod(javaClass, genPublishedAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationGivenPublicAttributeValueSetRange() {
        publicAttribute.setValueSetType(ValueSetType.RANGE);

        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, publicAttribute);
        expectValueSetConstant(1, javaClass, genPublicAttribute);
        expectGetValueSetMethod(javaClass, genPublicAttribute);
    }

    private void expectValueSetMemberVariable(int index, IType javaType, GenChangeableAttribute genChangeableAttribute) {
        expectField(index, javaType, genChangeableAttribute.getFieldNameSetOfAllowedValues());
    }

    private void expectValueSetConstant(int index, IType javaType, GenChangeableAttribute genChangeableAttribute) {
        expectField(index, javaType, genChangeableAttribute.getConstantName(genChangeableAttribute.getValueSet()));
    }

    private void expectDefaultMemberVariable(int index, IType javaType, GenChangeableAttribute genChangeableAttribute) {
        expectField(index, javaType, genChangeableAttribute.getFieldNameDefaultValue());
    }

    private void expectGetValueSetMethod(IType javaType, GenChangeableAttribute genChangeableAttribute) {
        expectMethod(javaType, genChangeableAttribute.getMethodNameGetSetOfAllowedValues(),
                unresolvedParam(IValidationContext.class));
    }

    private void expectGetDefaultValueMethod(IType javaType, GenChangeableAttribute genChangeableAttribute) {
        expectMethod(javaType, genChangeableAttribute.getMethodNameGetDefaultValue());
    }

}