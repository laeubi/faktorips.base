/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class RenameIpsPackageFragmentProcessorTest extends AbstractIpsPluginTest {

    private RenameIpsPackageFragmentProcessor processor;
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRoot;

    private static final String POLICY_CMPT_TYPE_QNAME = "data.coverages.PolicyCmptType"; //$NON-NLS-1$
    private static final String COVERAGE_QNAME = "data.coverages.Coverage"; //$NON-NLS-1$
    private static final String PRODUCT_A_QNAME = "data.products.ProductA"; //$NON-NLS-1$
    private static final String PRODUCT_B_QNAME = "data.products.ProductB"; //$NON-NLS-1$
    private static final String PRODUCT_C_QNAME = "data.products.subproducts.ProductC"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_NAME = "CoverageType"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_STATIC_NAME = "StaticCoverageType"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_QNAME = "model." + COVERAGE_TYPE_NAME; //$NON-NLS-1$
    private static final String PRODUCT_QNAME = "model.Product"; //$NON-NLS-1$

    private IProductCmpt productA;
    private IProductCmptGeneration productAGen;
    private IProductCmpt productB;
    private IProductCmptGeneration productBGen;
    private IProductCmpt coverage;

    private IPolicyCmptType policyCmptType;

    private IProductCmptType productCmptType1;
    private IProductCmptType productCmptType2;
    private IProductCmpt productC;
    private IIpsPackageFragment source;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        ipsRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        source = ipsRoot.getIpsPackageFragment("data.products");
        processor = new RenameIpsPackageFragmentProcessor(source);
        processor.setNewName("data.newproducts");

        productCmptType1 = newProductCmptType(ipsProject, PRODUCT_QNAME);
        productCmptType2 = newProductCmptType(ipsProject, COVERAGE_TYPE_QNAME);
        IProductCmptTypeAssociation association = productCmptType1.newProductCmptTypeAssociation();
        association.setTarget(productCmptType2.getQualifiedName());
        association.setTargetRoleSingular(COVERAGE_TYPE_NAME);
        association.setTargetRolePlural(COVERAGE_TYPE_NAME + "s");
        IProductCmptTypeAssociation staticAssociation = productCmptType1.newProductCmptTypeAssociation();
        staticAssociation.setTarget(productCmptType2.getQualifiedName());
        staticAssociation.setTargetRoleSingular(COVERAGE_TYPE_STATIC_NAME);
        staticAssociation.setTargetRolePlural(COVERAGE_TYPE_STATIC_NAME + "s");
        staticAssociation.setChangingOverTime(false);
        productCmptType1.getIpsSrcFile().save(true, null);
        productCmptType2.getIpsSrcFile().save(true, null);

        coverage = newProductCmpt(productCmptType2, COVERAGE_QNAME);

        productA = newProductCmpt(productCmptType1, PRODUCT_A_QNAME);
        productAGen = productA.getProductCmptGeneration(0);
        productAGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productA.getIpsSrcFile().save(true, null);

        productB = newProductCmpt(productCmptType1, PRODUCT_B_QNAME);
        productBGen = productB.getProductCmptGeneration(0);
        productBGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productB.getIpsSrcFile().save(true, null);

        productC = newProductCmpt(productCmptType1, PRODUCT_C_QNAME);
        IProductCmptGeneration productCGen = productC.getProductCmptGeneration(0);
        productCGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productC.getIpsSrcFile().save(true, null);

        policyCmptType = newPolicyCmptType(ipsProject, POLICY_CMPT_TYPE_QNAME);
        policyCmptType.getIpsSrcFile().save(true, null);

        IFile file = ((IFolder)source.getCorrespondingResource()).getFile("test.unknown");
        file.create(StringUtil.getInputStreamForString("Test content for file.", "UTF-8"), true, null);
        assertTrue(file.exists());
    }

    @Test
    public void testCheckInitialConditionsThis() throws Exception {
        RefactoringStatus status = new RefactoringStatus();
        processor.checkInitialConditionsThis(status, new NullProgressMonitor());
        assertTrue(status.isOK());
    }

    @Test
    public void testCheckInitialConditionsThis_StatusNOK() throws Exception {
        // No Target RolePlural
        IProductCmptTypeAssociation association2 = productCmptType1.newProductCmptTypeAssociation();
        association2.setTarget(productCmptType2.getQualifiedName());
        association2.setTargetRoleSingular(COVERAGE_TYPE_NAME);
        RefactoringStatus status = new RefactoringStatus();
        processor.checkInitialConditionsThis(status, new NullProgressMonitor());
        assertTrue(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsThis() throws Exception {
        RefactoringStatus status = new RefactoringStatus();
        CheckConditionsContext context = new CheckConditionsContext();
        processor.checkFinalConditionsThis(status, new NullProgressMonitor(), context);

        assertTrue(status.isOK());
        assertTrue(source.exists());
        assertFalse(ipsRoot.getIpsPackageFragment("data.newproducts").exists());
        assertTrue(source.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT).exists());
        assertTrue(source.getIpsSrcFile("ProductB", IpsObjectType.PRODUCT_CMPT).exists());
        assertTrue(source.getSubPackage("subproducts").getIpsSrcFile("ProductC", IpsObjectType.PRODUCT_CMPT).exists());
    }

    @Test
    public void testValidateUserInputThis() throws CoreRuntimeException {
        RefactoringStatus status = new RefactoringStatus();
        processor.validateUserInputThis(status, new NullProgressMonitor());
        assertTrue(status.isOK());
    }

    @Test
    public void testValidateUserInputThis_RenameToSameFolder() throws CoreRuntimeException {
        processor.setNewName("data.products");
        RefactoringStatus status = new RefactoringStatus();
        processor.validateUserInputThis(status, new NullProgressMonitor());
        assertTrue(status.hasFatalError());
        assertNotNull(status.getMessageMatchingSeverity(RefactoringStatus.FATAL));
    }

    @Test
    public void testValidateUserInputThis_RenameToNotValidFolder() throws CoreRuntimeException {
        processor.setNewName("data.");
        RefactoringStatus status = new RefactoringStatus();
        processor.validateUserInputThis(status, new NullProgressMonitor());
        assertTrue(status.hasFatalError());
        assertNotNull(status.getMessageMatchingSeverity(RefactoringStatus.FATAL));
    }

    @Test
    public void testRefactorIpsModel() throws Exception {
        processor.refactorIpsModel(new NullProgressMonitor());

        IIpsSrcFile oldIpsSourceFileProductA = source.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertFalse(oldIpsSourceFileProductA.exists());

        IIpsPackageFragment newTarget = ipsRoot.getIpsPackageFragment("data.newproducts");
        assertTrue(newTarget.exists());

        IFile newFile = ((IFolder)newTarget.getCorrespondingResource()).getFile("test.unknown");
        assertTrue(newFile.exists());

        IIpsSrcFile newIpsSrcFileProductA = newTarget.getIpsSrcFile("ProductA", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductA.exists());

        IIpsSrcFile newIpsSrcFileProductB = newTarget.getIpsSrcFile("ProductB", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductB.exists());

        IIpsPackageFragment newTargetSubPackage = ipsRoot.getIpsPackageFragment("data.newproducts.subproducts");

        IIpsSrcFile newIpsSrcFileProductC = newTargetSubPackage.getIpsSrcFile("ProductC", IpsObjectType.PRODUCT_CMPT);
        assertTrue(newIpsSrcFileProductC.exists());
    }

    @Test
    public void testGetAffectedIpsSrcFiles() throws Exception {
        Set<IIpsSrcFile> ipsSrcFiles = processor.getAffectedIpsSrcFiles();
        assertEquals(3, ipsSrcFiles.size());

        ipsSrcFiles.contains(productA.getIpsSrcFile());
        ipsSrcFiles.contains(productB.getIpsSrcFile());
        ipsSrcFiles.contains(productC.getIpsSrcFile());
    }

    @Test
    public void testGetIdentifier() throws Exception {
        assertEquals("org.faktorips.devtools.core.internal.refactor.RenameIpsPackageFragmentProcessor",
                processor.getIdentifier());
    }

    @Test
    public void testGetProcessorName() throws Exception {
        assertEquals(Messages.RenameIpsPackageFragmentProcessor_processorName, processor.getProcessorName());
    }

    @Test
    public void testIsSourceFilesSavedRequired() throws Exception {
        assertFalse(processor.isSourceFilesSavedRequired());
    }
}
