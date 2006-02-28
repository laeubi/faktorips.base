package org.faktorips.devtools.core.internal.refactor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

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
		this.sourceObjects = sources;
		
		this.targetNames = new String[sources.length];
		
		String prefix = target.getName();
		if (!prefix.equals("")) { //$NON-NLS-1$
			prefix += "."; //$NON-NLS-1$
		}
		for (int i = 0; i < sources.length; i++) {
			if (sources[i] instanceof IIpsPackageFragment) {
				targetNames[i] = prefix + ((IIpsPackageFragment)sources[i]).getFolderName();
			}
			else {
				targetNames[i] = prefix + sources[i].getName();
			}
		}

		checkSources(sources);
		checkTargets(sources, targetNames);
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
				if (this.sourceObjects[i] instanceof IProductCmpt) {
					IProductCmpt product = (IProductCmpt)this.sourceObjects[i];
					IIpsPackageFragmentRoot root = product.getIpsPackageFragment().getRoot();
					IIpsPackageFragment pack = root.getIpsPackageFragment(getPackageName(this.targetNames[i]));
					IIpsSrcFile file = pack.getIpsSrcFile(product.getIpsObjectType().getFileName(getUnqualifiedName(this.targetNames[i])));
					move((IProductCmpt)this.sourceObjects[i], file, monitor);
				}
				else if (this.sourceObjects[i] instanceof IIpsPackageFragment) {
					IIpsPackageFragment pack = (IIpsPackageFragment)this.sourceObjects[i];
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
						IProductCmpt cmpt = (IProductCmpt)root.getIpsPackageFragment(buildPackageName(pack.getName(), "", fileInfos[0])).getIpsSrcFile(fileInfos[1]).getIpsObject(); //$NON-NLS-1$
						move(cmpt, file, monitor);
					}

					// third, remove remaining folders
				    pack.getEnclosingResource().delete(true, monitor);
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}

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
	 * Moves one product component to the given target file.
	 */
	private void move(IProductCmpt source, IIpsSrcFile targetFile, IProgressMonitor monitor) {
		try {
			// first, find all objects refering the source (which will be deleted later)
			IProductCmptGeneration[] refs = source.getIpsProject().findReferencingProductCmptGenerations(source.getQualifiedName());
			
			// second, create the target
			IIpsPackageFragment pack = targetFile.getIpsPackageFragment();
			if (!pack.exists()) {
				pack.getRoot().createPackageFragment(pack.getName(), true, monitor);
			}
			pack.createIpsFile(targetFile.getName(), source.getIpsSrcFile().getContents(), true, monitor);
			
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
			if (sources[i] instanceof IProductCmpt) {
				IProductCmpt product = (IProductCmpt)sources[i];
				IIpsPackageFragmentRoot root = product.getIpsPackageFragment().getRoot();
				IIpsPackageFragment pack = root.getIpsPackageFragment(getPackageName(targets[i]));
				if (pack.getIpsSrcFile(getUnqualifiedName(targets[i])).exists()) {
					IpsStatus status = new IpsStatus("Target file " + targets[i] + " allready exists."); //$NON-NLS-1$ //$NON-NLS-2$
					throw new CoreException(status);
				}
			}
			else if (sources[i] instanceof IIpsPackageFragment) {
				IIpsPackageFragment sourcePack = (IIpsPackageFragment)sources[i];
				IIpsPackageFragmentRoot root = sourcePack.getRoot();
				IIpsPackageFragment pack = root.getIpsPackageFragment(targets[i]);
				if (pack.exists()) {
					IpsStatus status = new IpsStatus("Target package " + targets[i] + " allready exists."); //$NON-NLS-1$ //$NON-NLS-2$
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
			
			if (toTest instanceof IProductCmpt) {
				IProductCmpt product = (IProductCmpt)toTest;
				if (!product.getIpsSrcFile().exists()) {
					IpsStatus status = new IpsStatus("Source file " + getQualifiedSourceName(product) + " does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
					throw new CoreException(status);
				}
				
				if (product.getIpsSrcFile().isDirty()) {
					IpsStatus status = new IpsStatus("Source file " + getQualifiedSourceName(product) + " modified."); //$NON-NLS-1$ //$NON-NLS-2$
					throw new CoreException(status);
				}
			}
			else if (toTest instanceof IIpsPackageFragment) {
				IIpsPackageFragment pack = (IIpsPackageFragment)toTest;
				if (!pack.exists()) {
					IpsStatus status = new IpsStatus("Source package " + pack.getName() + " does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
					throw new CoreException(status);
				}
				checkSources(pack.getChildren());
			}
			else {
				
				IpsStatus status = new IpsStatus("Object of type " + toTest.getName() + " not supported."); //$NON-NLS-1$ //$NON-NLS-2$
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
