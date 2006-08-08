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

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Implementation of <code>IpsPackageFragment<code>.
 */
public class IpsPackageFragment extends IpsElement implements IIpsPackageFragment {

    IpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getRoot() {
        return (IIpsPackageFragmentRoot)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        String path = name.replace('.', IPath.SEPARATOR);
        IFolder folder = (IFolder)getParent().getCorrespondingResource();
        return folder.getFolder(new Path(path));
    }
    
    /**
     * {@inheritDoc}
     * IpsPackageFragments are always returned, whether they are output locations 
     * of the javaproject corresponding to this packagefragments IpsProject or not.
     */
    public IIpsPackageFragment[] getIpsChildPackageFragments() throws CoreException {
    	IFolder folder = (IFolder)getCorrespondingResource();
    	IResource[] content = folder.members();
    	IIpsPackageFragment[] result = new IIpsPackageFragment[content.length];
    	int count = 0;
    	for (int i = 0; i < content.length; i++) {
    		if (content[i].getType() == IFolder.FOLDER) {
        		String packageName = this.getName().equals("")?content[i].getName():this.getName() + "." + content[i].getName(); //$NON-NLS-1$ //$NON-NLS-2$
        		result[count] = new IpsPackageFragment(this.getParent(), packageName);  
        		count++;
    		}
    	}
    	
    	IIpsPackageFragment[] shrink = new IIpsPackageFragment[count];
    	System.arraycopy(result, 0, shrink, 0, count);
    	return shrink;
	}
    /**
     * {@inheritDoc}
     */
	public Object[] getNonIpsResources() throws CoreException {
		IResource res= getCorrespondingResource();
		IWorkbenchAdapter adapter= null;
		if(res instanceof IAdaptable){
			adapter= (IWorkbenchAdapter) ((IAdaptable) res).getAdapter(IWorkbenchAdapter.class);
		}
        if (adapter != null) {
        	List childResources= new ArrayList(); 
            IResource[] children= (IResource[]) adapter.getChildren(res);
            for (int i = 0; i < children.length; i++) {
        		if(!isResourceIpsContent(children[i])){
        			childResources.add(children[i]);
        		}
			}
            return childResources.toArray();
        }
        return new Object[0];
	}
	
	/**
	 * Returns true if the given IResource is a file or folder that corresponds to
	 * an IpsObject or IpsPackageFragment contained in this IpsPackageFragment,
	 * false otherwise.
	 */
	private boolean isResourceIpsContent(IResource res) throws CoreException {
		IIpsElement[] children= getIpsChildPackageFragments();
		for (int i = 0; i < children.length; i++) {
			if(children[i].getCorrespondingResource().equals(res)){
				return true;
			}
		}
		children= getChildren();
		for (int i = 0; i < children.length; i++) {
			if(children[i].getCorrespondingResource().equals(res)){
				return true;
			}
		}
		return false;
	}

    /**
     * {@inheritDoc}
     */
	public IIpsPackageFragment getParentIpsPackageFragment() {
		int lastIndex= getName().lastIndexOf("."); //$NON-NLS-1$
		if(lastIndex<0){
			if(isDefaultPackage()){
				return null;
			}else{
				return getRoot().getIpsDefaultPackageFragment();
			}
		}else{
			String parentPath= getName().substring(0, lastIndex);
			return new IpsPackageFragment(this.getParent(), parentPath);
		}
	}

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        IIpsElement[] children = new IIpsElement[members.length];
        int counter = 0;
        for (int i=0; i<children.length; i++) {
            if (members[i].getType()==IResource.FILE) {
                IFile file = (IFile)members[i];
                if (IpsObjectType.getTypeForExtension(file.getFileExtension())!=null) {
                    children[counter] = new IpsSrcFile(this, file.getName());
                    counter++;
                }
            }
        }
        if (counter==children.length) {
            return children;
        }
        IIpsElement[] shrinked = new IIpsElement[counter];
        System.arraycopy(children, 0, shrinked, 0, counter);
        return shrinked;
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsPackageFragment.gif"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile getIpsSrcFile(String name) {
    	
    	IpsObjectType type = IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
    	if(type != null){
    		return new IpsSrcFile(this, name);
    	}
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IFile file = folder.getFile(name);
        file.create(source, force, monitor);
        return getIpsSrcFile(name);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor) throws CoreException {
        try {
	        InputStream is = new ByteArrayInputStream(content.getBytes(StringUtil.CHARSET_UTF8));
	        return createIpsFile(name, is, force, monitor);
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /** 
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor) throws CoreException {
        String filename = type.getFileName(ipsObjectName);
        IIpsObject ipsObject = type.newObject(getIpsSrcFile(filename));
        
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        Element element = ipsObject.toXml(doc);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            IIpsSrcFile result = createIpsFile(filename, contents, force, monitor);
            
            if (type == IpsObjectType.PRODUCT_CMPT) {
            	IProductCmpt productCmpt = (IProductCmpt)result.getIpsObject();
            	productCmpt.setRuntimeId(ipsObject.getIpsProject().getRuntimeId(productCmpt));
            	result.save(force, monitor);
            }
            
            return result;
        } catch (TransformerException e) {
            throw new RuntimeException(e); 
            // This is a programing error, rethrow as runtime exception
        }        
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile createIpsFileFromTemplate(String name, IIpsObject template, GregorianCalendar date, boolean force, IProgressMonitor monitor) throws CoreException {
    	IpsObjectType type = template.getIpsObjectType();
    	String filename = type.getFileName(name);
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
    	Element element;

    	IIpsSrcFile file;
    	if (template instanceof ITimedIpsObject) {
    		IIpsObjectGeneration source = ((ITimedIpsObject)template).findGenerationEffectiveOn(date);
    		if (source == null) {
    			source = getFirstGeneration((ITimedIpsObject)template, date);
    		}
    		
    		if (source == null) {
    			throw new CoreException(new IpsStatus("No generation found for the given date " + date.getTime().toString() + " in " + template.getQualifiedName())); //$NON-NLS-1$ //$NON-NLS-2$
    		}
    		file = createIpsFile(type, name, force, monitor);
    		ITimedIpsObject newObject = (ITimedIpsObject)file.getIpsObject();
    		IIpsObjectGeneration target = newObject.newGeneration();
    		target.initFromGeneration(source);
    		target.setValidFrom(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
    		if (template instanceof IProductCmpt) {
    			((IProductCmpt)newObject).setPolicyCmptType(((IProductCmpt)template).getPolicyCmptType());
    		}
    		file.save(true, null);
    		
    		if (type == IpsObjectType.PRODUCT_CMPT) {
            	((IProductCmpt)newObject).setRuntimeId(newObject.getIpsProject().getRuntimeId((IProductCmpt)newObject));
    		}
    	}
    	else {
    		element = template.toXml(doc);
            try {
                String encoding = getIpsProject().getXmlFileCharset();
                String contents = XmlUtil.nodeToString(element, encoding);
                file = createIpsFile(filename, contents, force, monitor);
            } catch (TransformerException e) {
                throw new RuntimeException(e); 
            }        
    	}
    	return file;
	}

    /**
     * Returns the first generation of the given timed ips object, if this generation is valid from 
     * before the given date or <code>null</code> otherwise.
     * 
     * @param timed The timed ips object to get the first generation from.
     * @param date The date the first generation of the timed ips objects valid date has to be before.
     * @return The first generation or <code>null</code>.
     */
    private IIpsObjectGeneration getFirstGeneration(ITimedIpsObject timed, GregorianCalendar date) {
    	IIpsObjectGeneration first = timed.getFirstGeneration();
    	if ((first != null) && (!first.getValidFrom().before(date))) {
    		first = null;
    	}
    	return first;
    }
    
	/**
     * Searches all objects of the given type and adds them to the result. 
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        for (int i=0; i<members.length; i++) {
            if (members[i].getType()==IResource.FILE) {
                IFile file = (IFile)members[i];
                if (type == IpsObjectType.PRODUCT_CMPT_TYPE && IpsObjectType.POLICY_CMPT_TYPE.getFileExtension().equals(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject()!=null) {
                    	IPolicyCmptType policyCmptType = (IPolicyCmptType)srcFile.getIpsObject();
                    	IProductCmptType productCmptType = policyCmptType.findProductCmptType();
                    	if (productCmptType != null) {
                            result.add(productCmptType);    
                    	}
                    }
                } else if (type.getFileExtension().equals(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject()!=null) {
                        result.add(srcFile.getIpsObject());    
                    }
                }
            }
        }
    }

    /**
     * Searches all objects of the given type starting with the given prefix and adds
     * them to the result.
     * 
     * @throws NullPointerException if either type, prefix or result is null.
     * @throws CoreException if an error occurs while searching.
     *  
     */
    public void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result) throws CoreException {
        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String newPrefix = ignoreCase ? prefix.toLowerCase() : prefix;
        for (int i=0; i<members.length; i++) {
            if (members[i].getType()==IResource.FILE) {
                IFile file = (IFile)members[i];
                if (type.getFileExtension().equals(file.getFileExtension())) {
                    String filename = ignoreCase ? file.getName().toLowerCase() : file.getName();
                    if (filename.startsWith(newPrefix)) {
                        IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                        if (srcFile.getIpsObject()!=null) {
                            result.add(srcFile.getIpsObject());    
                        }
                    }
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public IPath getRelativePath() {
        return new Path(getName().replace('.', '/'));
    }
    
    /**
     * {@inheritDoc}
     */
	public String getFolderName() {
		return this.getCorrespondingResource().getName();
	}

    /**
     * {@inheritDoc}
     */
	public boolean isDefaultPackage() {
		return this.name.equals(""); //$NON-NLS-1$
	}

    
    
}
