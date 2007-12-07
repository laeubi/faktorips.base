/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 *
 * @author Jan Ortmann
 */
public class ArchiveIpsPackageFragmentRootTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IpsArchiveEntry entry;
    private IFile archiveFile;
    private ArchiveIpsPackageFragmentRoot root;
    private IPolicyCmptType type;

    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        type = newPolicyCmptType(archiveProject, "motor.Policy");
        type.getIpsSrcFile().save(true, null);
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "motor.collision.CollisionCoverage").getIpsSrcFile().save(true, null);
        newProductCmpt(archiveProject, "motor.MotorProduct").getIpsSrcFile().save(true, null);

        project = newIpsProject();
        archiveFile = project.getProject().getFile("test.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        entry = (IpsArchiveEntry)path.newArchiveEntry(archiveFile);
        project.setIpsObjectPath(path);
        root = (ArchiveIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[1];
    }

    public void testGetIpsObjectPathEntry() throws CoreException {
        assertEquals(entry.getArchiveFile(), root.getIpsArchive().getArchiveFile());
    }

    public void testGetParent() {
        assertEquals(project, root.getParent());
    }

    public void testGetIpsPackageFragments() throws CoreException {
        IIpsPackageFragment[] packs = root.getIpsPackageFragments();
        assertEquals(2, packs.length);
        assertEquals("motor", packs[0].getName());
        assertEquals("motor.collision", packs[1].getName());
    }

    public void testGetNonIpsResources() throws CoreException {
        IResource[] res = root.getNonIpsResources();
        assertEquals(0, res.length);
    }

    public void testGetCorrespondingResource() {
        assertEquals(archiveFile, root.getCorrespondingResource());
    }

    public void testGetEnclosingResource() {
        assertEquals(archiveFile, root.getEnclosingResource());
    }

    public void testFindIpsObject() throws Throwable {
        IIpsObject type = root.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "motor.Policy");
        assertNotNull(type);

        IIpsObject type2 = root.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "motor.Policy");
        assertEquals(type, type2);

        assertNull(root.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "Unkown"));
        assertNull(root.findIpsObject(IpsObjectType.PRODUCT_CMPT, "motor.Policy"));
    }

    public void testFindIpsObject_ProductCmptType() throws Throwable {
        assertNotNull(root.findIpsObject(IpsObjectType.OLD_PRODUCT_CMPT_TYPE, type.getProductCmptType()));
    }

    public void testFindIpsObjectsByIpsObjectType() throws CoreException {
        List result = new ArrayList();
        root.findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(root.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "motor.Policy")));
        assertTrue(result.contains(root.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "motor.collision.CollisionCoverage")));
    }

    public void testFindIpsObjects() throws CoreException {
        List result = new ArrayList();
        root.findIpsObjects(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(root.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "motor.Policy")));
        assertTrue(result.contains(root.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE_V2, "motor.PolicyProductCmpt")));
        assertTrue(result.contains(root.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "motor.collision.CollisionCoverage")));
        assertTrue(result.contains(root.findIpsObject(IpsObjectType.PRODUCT_CMPT, "motor.MotorProduct")));
    }

    public void testGetSortedIpsPackageFragments() throws CoreException {
        IIpsPackageFragment[] packs = root.getSortedIpsPackageFragments();
        assertEquals(2, packs.length);
        assertEquals("motor", packs[0].getName());
        assertEquals("motor.collision", packs[1].getName());
    }

    public void testFindIpsSourceFiles() throws CoreException{
        List result = new ArrayList();
        root.findIpsSourceFiles(IpsObjectType.POLICY_CMPT_TYPE, result);
        assertEquals(2, result.size());
        List qualifiedNameTypes = new ArrayList();
        for (Iterator it = result.iterator(); it.hasNext();) {
            IIpsSrcFile pcTypeSrcFile = (IIpsSrcFile)it.next();
            qualifiedNameTypes.add(pcTypeSrcFile.getQualifiedNameType());
        }
        assertTrue(qualifiedNameTypes.contains(new QualifiedNameType("motor.Policy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qualifiedNameTypes.contains(new QualifiedNameType("motor.collision.CollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        
        result = new ArrayList();
        root.findIpsSourceFiles(IpsObjectType.PRODUCT_CMPT_TYPE_V2, result);
        assertEquals(1, result.size());

        result = new ArrayList();
        root.findIpsSourceFiles(IpsObjectType.PRODUCT_CMPT, result);
        assertEquals(1, result.size());

    }
}
