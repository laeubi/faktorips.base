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

package org.faktorips.devtools.core.internal.refactor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

/**
 * Moves (and renames) product components.
 * 
 * @author Thorsten Guenther
 */
public class MoveOperation implements IRunnableWithProgress {

	/**
	 * All product components or package fragements to move/rename.
	 */
	private IIpsElement[] sourceObjects;
	
	/**
	 * The new (package-qualified) names for the objects to move/rename.
	 */
	private String[] targetNames;
	
	/**
	 * The package fragment where to place the moved objects.
	 */
	private IIpsPackageFragment target;
	
	/**
	 * Creates a new operation to move or rename the given product. After the run-method has returned,
	 * all references of other products to the moved/renamed one are updated to refer to the new name.
	 * 
	 * @param source The product to rename.
	 * @param target The new location/name.
	 * @throws CoreException If the source does not exist or ist modiefied (if a product component) 
	 * or if the target allready exists.
	 */
	public MoveOperation(IProductCmpt source, String target) throws CoreException {
		this(new IIpsElement[] {source}, new String[] {target});
	}
	
	/**
	 * Move all the given package fragements and product components to the given targets. 
	 * 
	 * @param sources An array containing <code>IProductCmpt</code> or <code>IIpsPackageFragement</code>
	 * objects to move.
	 * @param targetRoot An array of the new, qualified names for the objects to move. The names are object-names, 
	 * not filenames, so do not append any file extension to the name.
	 * @throws CoreException if the both arrays are not of the same lenth.
	 */
	public MoveOperation(IIpsElement[] sources, String[] targets) throws CoreException {
		if (sources.length != targets.length) {
			IpsStatus status = new IpsStatus("Number of source- and target-objects is not the same."); //$NON-NLS-1$
			throw new CoreException(status);
		}
		
		checkSources(sources);
		checkTargets(sources, targets);

		this.sourceObjects = sources;
		this.targetNames = targets;
	}

	/**
	 * Creates a new operation to move or rename the given product. After the run-method has returned,
	 * all references of other products to the moved/renamed one are updated to refer to the new name.
	 * 
	 * @param source The product to rename.
	 * @param target The new location/name.
	 * @throws CoreException If the source does not exist or ist modiefied or if the target allready exists.
	 */
	public MoveOperation(IIpsElement[] sources, IIpsPackageFragment target) throws CoreException {
		this.target = target;
		this.sourceObjects = prepare(sources);
		this.targetNames = getTragetNames(this.sourceObjects, target);
		
		checkSources(sources);
		checkTargets(sources, targetNames);
	}

	/**
	 * Creates the new qualified names for the moved objects.
	 *  
	 * @param sources The objects to move
	 * @param target The package fragment to move to.
	 */
	private String[] getTragetNames(IIpsElement[] sources, IIpsPackageFragment target) {
		String[] result = new String[sources.length];
		
		String prefix = target.getName();
		if (!prefix.equals("")) { //$NON-NLS-1$
			prefix += "."; //$NON-NLS-1$
		}
		for (int i = 0; i < sources.length; i++) {
			if (sources[i] instanceof IIpsPackageFragment) {
				result[i] = prefix + ((IIpsPackageFragment)sources[i]).getFolderName();
			}
			else {
				result[i] = prefix + sources[i].getName();
			}
		}
		
		return result;
	}
	
	/**
	 * Converts any contained IIpsSrcFiles to the objects contained within.
	 * 
	 * @param rawSources The IIpsElements to prepare.
	 * @throws CoreException If an IIpsSrcFile is contained which can not return the IIpsObject stored within.
	 */
	private IIpsElement[] prepare(IIpsElement[] rawSources) throws CoreException {
		IIpsElement[] result = new IIpsElement[rawSources.length];
		
		for (int i = 0; i < result.length; i++) {
			if (rawSources[i] instanceof IIpsSrcFile) {
				result[i] = ((IIpsSrcFile)rawSources[i]).getIpsObject();
			}
			else {
				result[i] = rawSources[i];
			}
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		for (int i = 0; i < this.sourceObjects.length; i++) {
			try {
				IIpsElement toMove;
				if (sourceObjects[i] instanceof IIpsSrcFile) {
					toMove = ((IIpsSrcFile)sourceObjects[i]).getIpsObject();
				}
				else {
					toMove = sourceObjects[i];
				}
				
				if (toMove instanceof IProductCmpt) {
					IProductCmpt product = (IProductCmpt)toMove;
					IIpsSrcFile file = createTarget(product, this.targetNames[i]);
					move(product, file, monitor);
				}
				else if (toMove instanceof IIpsPackageFragment) {
					IIpsPackageFragment pack = (IIpsPackageFragment)toMove;
					String newName = this.targetNames[i];
					IIpsPackageFragment parent = pack.getIpsParentPackageFragment();
					
					// first, find all products contained in this folder
				    ArrayList files = new ArrayList();
					getRelativeFileNames("", (IFolder)pack.getEnclosingResource(), files); //$NON-NLS-1$
					
					// second, move them all
					IIpsPackageFragmentRoot root = parent.getRoot();
					for (Iterator iter = files.iterator(); iter.hasNext();) {
						String[] fileInfos = (String[]) iter.next();
						IIpsPackageFragment targetPackage = root.getIpsPackageFragment(buildPackageName("", newName, fileInfos[0])); //$NON-NLS-1$
						if (!targetPackage.exists()) {
							root.createPackageFragment(targetPackage.getName(), true, null);
						}
						IIpsSrcFile file = targetPackage.getIpsSrcFile(fileInfos[1]);
						IIpsPackageFragment sourcePackage = root.getIpsPackageFragment(buildPackageName(pack.getName(), "", fileInfos[0])); //$NON-NLS-1$
						IIpsSrcFile cmptFile = sourcePackage.getIpsSrcFile(fileInfos[1]);  //$NON-NLS-1$
						if (cmptFile != null) {
							// we got an IIpsSrcFile, so we have to move it correctly
							IProductCmpt cmpt = (IProductCmpt)cmptFile.getIpsObject();
							move(cmpt, file, monitor);
						} else {
							// we dont have a IIpsSrcFile, so move the file as resource operation
							IFolder folder = (IFolder)sourcePackage.getEnclosingResource(); 
							IFile rawFile = folder.getFile(fileInfos[1]);
							IPath destination = ((IFolder)targetPackage.getCorrespondingResource()).getFullPath().append(fileInfos[1]);
							rawFile.move(destination, true, monitor);
						}
					}

					// third, remove remaining folders
				    pack.getEnclosingResource().delete(true, monitor);
				}
				else if (toMove instanceof ITableContents) {
					IIpsSrcFile file = createTarget((ITableContents)toMove, this.targetNames[i]);
					move((ITableContents)toMove, file, monitor);
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}

	}
	
	/**
	 * Creates the IIpsSrcFile for the given target. The IpsObjectType associated with 
	 * the new file is the one stored in the given source. The target is created in the 
	 * package fragment root of the given source.
	 */
	private IIpsSrcFile createTarget(IIpsObject source, String targetName) {
		IIpsPackageFragmentRoot root = source.getIpsPackageFragment().getRoot();
		IIpsPackageFragment pack = root.getIpsPackageFragment(getPackageName(targetName));
		return pack.getIpsSrcFile(source.getIpsObjectType().getFileName(getUnqualifiedName(targetName)));
	}
	
	/**
	 * Builds a package name by concatenating the given parts wiht dots. Every of the three parts
	 * can be empty.
	 */
	private String buildPackageName(String prefix, String middle, String postfix) {
		String result = prefix;
		
		if (!result.equals("") && !middle.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			result += "."; //$NON-NLS-1$
		}
		
		if (!middle.equals("")) { //$NON-NLS-1$
			result += middle;
		}
		
		if (!result.equals("") && !postfix.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			result += "."; //$NON-NLS-1$
		}
		
		if (!postfix.equals("")) { //$NON-NLS-1$
			result += postfix;
			
		}
		return result;
	}

	/**
	 * Recursively descend the path down the folders and collect all files found in the given list.
	 */
	private void getRelativeFileNames(String path, IFolder folder, ArrayList result) throws CoreException {
		IResource[] members = folder.members();
		for (int i = 0; i < members.length; i++) {
			if (members[i].getType() == IResource.FOLDER) {
				getRelativeFileNames(path + "." + members[i].getName(), (IFolder)members[i], result); //$NON-NLS-1$
			}
			else if (members[i].getType() == IResource.FILE) {
				result.add(new String[] {path, members[i].getName()});
			}
		}
	}

	/**
	 * Returns the package name for the given, full qualified name (which means all segements 
	 * except the last one, segments seperated by dots). The name must not be a filename with extension.
	 */
	private String getPackageName(String qualifiedName) {
		String result = ""; //$NON-NLS-1$
		int index = qualifiedName.lastIndexOf('.');
		if (index > -1) {
			result = qualifiedName.substring(0, index);
		}
		return result;
	}
	
	/**
	 * Returns the unqualified name for the given, full qualified name. The qualified name must 
	 * not be a filename with extension. 
	 */
	private String getUnqualifiedName(String qualifiedName) {
		String result = qualifiedName;
		int index = qualifiedName.lastIndexOf('.');
		if (index > -1) {
			result = qualifiedName.substring(index + 1);
		}
		return result;
	}
	
	/**
	 * Moves one table content to the given target file.
	 */
	private void move(ITableContents source, IIpsSrcFile targetFile, IProgressMonitor monitor) {
		try {
			createCopy(source.getIpsSrcFile(), targetFile, monitor);
			source.getEnclosingResource().delete(true, monitor);
		} catch (CoreException e) {
			Shell shell = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.MoveOperation_titleAborted, Messages.MoveOperation_msgAborted);
			IpsPlugin.log(e);
		}		
		
	}
	
	/**
	 * Moves one product component to the given target file.
	 */
	private void move(IProductCmpt source, IIpsSrcFile targetFile, IProgressMonitor monitor) {
		try {
			// first, find all objects refering the source (which will be deleted later)
			IProductCmptGeneration[] refs = source.getIpsProject().findReferencingProductCmptGenerations(source.getQualifiedName());
			
			// second, create the target
			createCopy(source.getIpsSrcFile(), targetFile, monitor);
			
			// third, update references
			for (int i = 0; i < refs.length; i++) {
				fixRelations(refs[i], source.getQualifiedName(), targetFile.getIpsObject().getQualifiedName(), monitor);
			}			
			
			// at least, delete the source
			source.getEnclosingResource().delete(true, monitor);
		} catch (CoreException e) {
			Shell shell = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(shell, Messages.MoveOperation_titleAborted, Messages.MoveOperation_msgAborted);
			IpsPlugin.log(e);
		}		
	}
	
	private void createCopy(IIpsSrcFile source, IIpsSrcFile targetFile, IProgressMonitor monitor) throws CoreException {
		IIpsPackageFragment pack = targetFile.getIpsPackageFragment();
		if (!pack.exists()) {
			pack.getRoot().createPackageFragment(pack.getName(), true, monitor);
		}
		pack.createIpsFile(targetFile.getName(), source.getContents(), true, monitor);
	}
	
	/**
	 * Resets the target of all realations of the given generation, if the target equals the 
	 * old name, to the new name.
	 * 
	 * @param generation The generation to fix the relations at.
	 * @param oldName The old, qualified name of the target.
	 * @param newName The new, qualified name of the target
	 * @param monitor Progress monitor to show progress.
	 */
	private void fixRelations(IProductCmptGeneration generation, String oldName, String newName, IProgressMonitor monitor) throws CoreException {
		
		IProductCmptRelation[] relations = generation.getRelations();
		
		for (int i = 0; i < relations.length; i++) {
			String target = relations[i].getTarget();
			if (target.equals(oldName)) {
				relations[i].setTarget(newName);
			}
		}
		
		generation.getIpsObject().getIpsSrcFile().save(true, monitor);
	}

	/**
	 * Check all targets not to exist. If an existing target is found, a core exception is thrown.
	 * 
	 * @param sources The array of source objects. Used to get the type for the target.
	 * @param targets The qualified names of the targets to test.
	 * @throws CoreException if a target exists.
	 */
	private void checkTargets(IIpsElement[] sources, String[] targets) throws CoreException {
		for (int i = 0; i < targets.length; i++) {
			IIpsElement toTest;
			if (sources[i] instanceof IIpsSrcFile) {
				toTest = ((IIpsSrcFile)sources[i]).getIpsObject();
			}
			else {
				toTest = sources[i];
			}

			if (toTest instanceof IProductCmpt) {
				IProductCmpt product = (IProductCmpt)toTest;
				IIpsPackageFragmentRoot root = product.getIpsPackageFragment().getRoot();
				IIpsPackageFragment pack = root.getIpsPackageFragment(getPackageName(targets[i]));
				if (pack.getIpsSrcFile(IpsObjectType.PRODUCT_CMPT.getFileName(getUnqualifiedName(targets[i]))).exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
					IpsStatus status = new IpsStatus(msg);
					throw new CoreException(status);
				}
			}
			else if (toTest instanceof IIpsPackageFragment) {
				IIpsPackageFragment sourcePack = (IIpsPackageFragment)toTest;
				IIpsPackageFragmentRoot root = sourcePack.getRoot();
				IIpsPackageFragment pack = root.getIpsPackageFragment(targets[i]);
				if (pack.exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgPackageExists, targets[i]);
					IpsStatus status = new IpsStatus(msg);
					throw new CoreException(status);
				}
			}
			else if (toTest instanceof ITableContents) {
				ITableContents table = (ITableContents)toTest;
				IIpsPackageFragmentRoot root = table.getIpsPackageFragment().getRoot();
				IIpsPackageFragment pack = root.getIpsPackageFragment(getPackageName(targets[i]));
				if (pack.getIpsSrcFile(IpsObjectType.TABLE_CONTENTS.getFileName(getUnqualifiedName(targets[i]))).exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgFileExists, targets[i]);
					IpsStatus status = new IpsStatus(msg);
					throw new CoreException(status);
				}
			}
		}
	}
	
	/**
	 * Check all sources to exist and to be saved. If not so, a core exception is thrown.
	 */
	private void checkSources(IIpsElement[] source) throws CoreException {
		for (int i = 0; i < source.length; i++) { 
			IIpsElement toTest;
			if (source[i] instanceof IIpsSrcFile) {
				toTest = ((IIpsSrcFile)source[i]).getIpsObject();
			}
			else {
				toTest = source[i];
			}
			
			if (this.target != null && !toTest.getIpsProject().equals(this.target.getIpsProject())) {
				IpsStatus status = new IpsStatus(Messages.MoveOperation_msgMoveBetweenProjectsNotSupported); 
				throw new CoreException(status);
			}
			
			if (toTest instanceof IProductCmpt) {
				IProductCmpt product = (IProductCmpt)toTest;
				if (!product.getIpsSrcFile().exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgSourceMissing, getQualifiedSourceName(product));
					IpsStatus status = new IpsStatus(msg); 
					throw new CoreException(status);
				}
				
				if (product.getIpsSrcFile().isDirty()) {
					String msg = NLS.bind(Messages.MoveOperation_msgSourceModified, getQualifiedSourceName(product));
					IpsStatus status = new IpsStatus(msg); 
					throw new CoreException(status);
				}
			}
			else if (toTest instanceof IIpsPackageFragment) {
				IIpsPackageFragment pack = (IIpsPackageFragment)toTest;
				if (!pack.exists()) {
					String msg = NLS.bind(Messages.MoveOperation_msgPackageMissing, pack.getName());
					IpsStatus status = new IpsStatus(msg); 
					throw new CoreException(status);
				}
				checkSources(pack.getChildren());
			}
			else if (toTest instanceof ITableContents) {
				ITableContents table = (ITableContents)toTest;
				if (!table.exists()) {
					String msg = NLS.bind("Tablecontent {0} is missing", table.getName()); //$NON-NLS-1$
					IpsStatus status = new IpsStatus(msg); 
					throw new CoreException(status);
				}
			}
			else {				
				String msg = NLS.bind(Messages.MoveOperation_msgUnsupportedType, toTest.getName());
				IpsStatus status = new IpsStatus(msg); 
				throw new CoreException(status);
			}
		}
	}
	
	/**
	 * Returns the qualified source-name (including file extension) for the given product.
	 */
	private String getQualifiedSourceName(IProductCmpt product) {
		return product.getQualifiedName() + "." + product.getIpsObjectType().getFileExtension(); //$NON-NLS-1$
	}
	
}
