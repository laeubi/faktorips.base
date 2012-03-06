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

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptTypeBuilderTest;
import org.junit.Before;

public abstract class GenAssociationTest extends PolicyCmptTypeBuilderTest {

    protected static final String TARGET_POLICY_NAME = "Policy2";

    protected static final String TARGET_ROLE_SINGULAR = "Policy2";

    protected static final String TARGET_ROLE_PLURAL = "Policy2s";

    protected IType javaInterfaceTargetType;

    protected IPolicyCmptTypeAssociation association;

    protected IPolicyCmptType targetPolicyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        targetPolicyCmptType = newPolicyCmptType(ipsProject, TARGET_POLICY_NAME);
        targetPolicyCmptType.setConfigurableByProductCmptType(false);
        targetPolicyCmptType.setProductCmptType("");

        javaInterfaceTargetType = getGeneratedJavaInterface(targetPolicyCmptType, false,
                builderSet.getBuildersByClass(PolicyCmptInterfaceBuilder.class).get(0), TARGET_POLICY_NAME);

        association = policyCmptType.newPolicyCmptTypeAssociation();
        association.setMinCardinality(0);
        association.setTarget(targetPolicyCmptType.getQualifiedName());
        association.setTargetRoleSingular(TARGET_ROLE_SINGULAR);
        association.setTargetRolePlural(TARGET_ROLE_PLURAL);
    }

    protected final void expectFieldGetMaxCardinalityFor(int index, GenAssociation genAssociation, IType javaType) {
        expectField(index, javaType, genAssociation.getFieldNameGetMaxCardinalityFor());
    }

    protected final void expectFieldAssociation(int index, GenAssociation genAssociation, IType javaType) {
        try {
            expectField(index, javaType, genAssociation.getFieldNameForAssociation());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void expectFieldAssociationName(int index, GenAssociation genAssociation, IType javaType) {
        expectField(index, javaType, genAssociation.getStaticConstantAssociationName());
    }

    protected final void expectMethodNewChild(GenAssociation genAssociation, IType javaType) {
        expectMethod(javaType, genAssociation.getMethodNameNewChild());
    }

    protected final void expectMethodNewChildConfigured(GenAssociation genAssociation,
            IType javaType,
            IType javaInterfaceTargetConfiguringProductCmptType) {

        expectMethod(javaType, genAssociation.getMethodNameNewChild(),
                unresolvedParam(javaInterfaceTargetConfiguringProductCmptType.getElementName()));
    }

    protected final IProductCmptType setUpTargetConfigurable() throws CoreException {
        IProductCmptType configurationForTarget = newProductCmptType(ipsProject, "Product2");
        configurationForTarget.setConfigurationForPolicyCmptType(true);
        configurationForTarget.setPolicyCmptType(targetPolicyCmptType.getQualifiedName());
        targetPolicyCmptType.setConfigurableByProductCmptType(true);
        targetPolicyCmptType.setProductCmptType(configurationForTarget.getQualifiedName());
        return configurationForTarget;
    }

}