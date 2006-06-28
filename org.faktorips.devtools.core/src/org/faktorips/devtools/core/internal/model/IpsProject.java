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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.ArrayOfValueDatatypeHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IpsBuilder;
import org.faktorips.devtools.core.internal.model.product.DefaultRuntimeIdInitStrategy;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptRuntimeIdInitStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class IpsProject extends IpsElement implements IIpsProject {

	private IProductCmptRuntimeIdInitStrategy runtimeIdStrategy = null;
	
    /**
     * Constructor needed for <code>IProject.getNature()</code> and
     * <code>IProject.addNature()</code>.
     * 
     * @see #setProject(IProject)
     */
    public IpsProject() {
    }

    IpsProject(IIpsModel model, String name) {
        super(model, name);
    }

    public IProject getProject() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
    
    /**
	 * {@inheritDoc}
	 */
	public IIpsProjectProperties getProperties() {
    	return new IpsProjectProperties(this, (IpsProjectProperties)getPropertiesInternal());
	}

	/*
	 * Returns the properties from the model.
	 */
	private IpsProjectProperties getPropertiesInternal() {
		return ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setProperties(IIpsProjectProperties properties) throws CoreException {
		((IpsModel)getIpsModel()).setIpsProjectProperties(this, properties);
		saveProjectProperties(new IpsProjectProperties(this, (IpsProjectProperties)properties));
	}

	/**
     * Saves the project properties to the .ipsproject file.
     * 
     * @throws CoreException if an error occurs while saving the data.
     */
    private void saveProjectProperties(IIpsProjectProperties properties) throws CoreException {
    	Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
    	Element propertiesEl = ((IpsProjectProperties)properties).toXml(doc);
        IFile file = getIpsProjectPropertiesFile();
        String charset = getXmlFileCharset();
        String contents;
        try {
            contents = XmlUtil.nodeToString(propertiesEl, charset);
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Error tranforming project data to xml string", e)); //$NON-NLS-1$
        }
        ByteArrayInputStream is;
        try {
            is = new ByteArrayInputStream(contents.getBytes(charset));
        } catch (Exception e) {
            throw new CoreException(new IpsStatus("Error creating byte stream", e)); //$NON-NLS-1$
        }
        if (file.exists()) {
            file.setContents(is, true, true, null);
        } else {
            file.create(is, true, null);
        }
    }
    
	/**
     * Returns the file that stores the project's properties. Note that the file need not exist.
     */
    public IFile getIpsProjectPropertiesFile() {
    	return getProject().getFile(".ipsproject"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IJavaProject getJavaProject() {
        return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProject(getName());
    }
    
    /**
	 * {@inheritDoc}
	 */
	public boolean canBeBuild() {
		try {
			return !validate().containsErrorMsg();
		} catch (CoreException e) {
			IpsPlugin.log(e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public String getXmlFileCharset() {
        return "UTF-8"; //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isModelProject() {
		return getPropertiesInternal().isModelProject();
	}

    /**
     * {@inheritDoc}
     */
	public boolean isProductDefinitionProject() {
		return getPropertiesInternal().isProductDefinitionProject();
	}

	/**
	 * {@inheritDoc}
	 */
    public IIpsObjectPath getIpsObjectPath() throws CoreException {
    	return getProperties().getIpsObjectPath();
    }
    
    /**
     * {@inheritDoc}
     */
    public IFolder[] getOutputFolders() throws CoreException {
    	return getIpsObjectPathInternal().getOutputFolders();
    }

	/**
	 * Returns a <strong>reference</strong> to the ips object path, in
	 * contrast to the getIpsObjectPath() method that returns a copy.
	 */
    public IpsObjectPath getIpsObjectPathInternal() throws CoreException {
    	return (IpsObjectPath)getPropertiesInternal().getIpsObjectPath();
    }

    /**
	 * {@inheritDoc}
	 */
    public void setCurrentArtefactBuilderSet(String id) throws CoreException {
    	IIpsProjectProperties properties = getProperties();
    	properties.setBuilderSetId(id);
    	saveProjectProperties(properties);
    }
    
	/**
	 * {@inheritDoc}
	 */
    public void setValueDatatypes(String[] ids) throws CoreException {
        IIpsProjectProperties properties = getProperties();
        properties.setPredefinedDatatypesUsed(ids);
        saveProjectProperties(properties);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setValueDatatypes(ValueDatatype[] types) throws CoreException {
    	String[] ids = new String[types.length];
    	for (int i = 0; i < types.length; i++) {
			ids[i] = types[i].getQualifiedName();
		}
		setValueDatatypes(ids);
	}
    
    /**
     * {@inheritDoc}
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException {
    	IpsProjectProperties properties = ((IpsModel)getIpsModel()).getIpsProjectProperties(this);
    	properties.setIpsObjectPath(newPath);
    	saveProjectProperties(properties);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name) {
        return new IpsPackageFragmentRoot(this, name);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() throws CoreException {
        List roots = new ArrayList();
        IIpsObjectPathEntry[] entries = getIpsObjectPathInternal().getEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] instanceof IIpsSrcFolderEntry) {
                roots.add(((IIpsSrcFolderEntry)entries[i]).getIpsPackageFragmentRoot(this));
            }
        }
        return (IIpsPackageFragmentRoot[])roots.toArray(new IIpsPackageFragmentRoot[roots.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        return getCorrespondingResource().exists();
    }

    /**
     * {@inheritDoc}
     */
    public Locale getExpressionLanguageFunctionsLanguage() {
        return Locale.GERMAN;
    }

    /**
     * {@inheritDoc}
     */
    public Locale getGeneratedJavaSourcecodeDocumentationLanguage() {
    	IpsProjectProperties properties = getPropertiesInternal();
    	return properties.getJavaSrcLanguage();
    }

    /**
     * {@inheritDoc}
     */
	public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode() {
    	IpsProjectProperties properties = getPropertiesInternal();
    	return getIpsModel().getChangesOverTimeNamingConvention(properties.getChangesOverTimeNamingConventionIdForGeneratedCode());
	}

	/**
     * Overridden.
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsProject.gif"); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public IResource getCorrespondingResource() {
        return getProject();
    }

    /**
     * Overridden.
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragmentRoots();
    }

    /**
     * Overridden.
     */
    public IIpsProject getIpsProject() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() throws CoreException {
        IProjectDescription description = getProject().getDescription();
        ICommand command = getPdBuildCommand();
        if (command == null) {
            // Add a product definition build command to the build spec
            ICommand newBuildCommand = description.newCommand();
            newBuildCommand.setBuilderName(IpsBuilder.BUILDER_ID);
            addCommandAtFirstPosition(description, newBuildCommand);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deconfigure() throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    public void setProject(IProject project) {
        this.name = project.getName();
    }

    /**
     * Finds the specific command for product definition builder.
     */
    private ICommand getPdBuildCommand() throws CoreException {
        ICommand[] commands = getProject().getDescription().getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(IpsBuilder.BUILDER_ID)) {
                return commands[i];
            }
        }
        return null;
    }

    /*
     * Adds the command to the build spec
     */
    private void addCommandAtFirstPosition(IProjectDescription description, ICommand newCommand)
            throws CoreException {

        ICommand[] oldCommands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[oldCommands.length + 1];
        System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
        newCommands[0] = newCommand;
        // Commit the spec change into the project
        description.setBuildSpec(newCommands);
        getProject().setDescription(description, null);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObject(this, nameType);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObject(this, type, qualifiedName);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject[] findIpsObjectsStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase) throws CoreException {
        ArrayList result = new ArrayList();
        findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }

    /**
     * Searches all objects of the given type starting with the given prefix found on the project's
     * path and adds them to the given result list.
     * 
     * @throws CoreException if an error occurs while searching for the objects.
     */
    public void findIpsObjectsStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result) throws CoreException {
        ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObjectsStartingWith(this, type, prefix,
            ignoreCase, result);
    }

    /**
     * Overridden.
     */
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) throws CoreException {
        return (IPolicyCmptType)findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }
    
    /**
	 * Overridden.
	 */
	public IProductCmptType findProductCmptType(String qualifiedName) throws CoreException {
		return (IProductCmptType)findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, qualifiedName);
	}

	/**
     * Overridden.
     */
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException {
        return ((IpsObjectPath)getIpsObjectPathInternal()).findIpsObjects(this, type);
    }

    /**
     * Returns all IpsObjects within this IpsProject and the IpsProjects this one depends on.
     * @throws CoreException
     */
    public void findAllIpsObjects(List result) throws CoreException{
        getIpsObjectPathInternal().findIpsObjects(this, IpsObjectType.POLICY_CMPT_TYPE, result);
        getIpsObjectPathInternal().findIpsObjects(this, IpsObjectType.PRODUCT_CMPT, result);
        getIpsObjectPathInternal().findIpsObjects(this, IpsObjectType.TABLE_STRUCTURE, result);
        getIpsObjectPathInternal().findIpsObjects(this, IpsObjectType.TABLE_CONTENTS, result);
        getIpsObjectPathInternal().findIpsObjects(this, IpsObjectType.BUSINESS_FUNCTION, result);
    }
    
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
    	getIpsObjectPathInternal().findIpsObjects(this, type, result);
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype[] getValueDatatypes(boolean includeVoid) {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, true, result);
        return (ValueDatatype[])result.toArray(new ValueDatatype[result.size()]);
    }
    
    /**
	 * {@inheritDoc}
	 */
	public ValueDatatype[] getValueDatatypes(boolean includeVoid, boolean includePrimitives) {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, includePrimitives, result);
        return (ValueDatatype[])result.toArray(new ValueDatatype[result.size()]);
	}

	private void getValueDatatypes(boolean includeVoid, boolean includePrimitives, Set result) {
        if (includeVoid) {
            result.add(Datatype.VOID);
        }
        getValueDatatypes(this, result, new HashSet());
        if (!includePrimitives) {
        	// remove primitives from the result
        	for (Iterator it=result.iterator(); it.hasNext(); ) {
        		ValueDatatype type = (ValueDatatype)it.next();
        		if (type.isPrimitive()) {
        			it.remove();
        		}
        	}
        }
    }
    
    private void getValueDatatypes(IIpsProject ipsProject, Set result, Set visitedProjects){
        try {
            ((IpsModel)getIpsModel()).getValueDatatypes(ipsProject, result);
            IIpsProject[] projects = ((IpsProject)ipsProject).getIpsObjectPathInternal().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                if(!visitedProjects.contains(projects[i])){
                	visitedProjects.add(projects[i]);
                	getValueDatatypes(projects[i], result, visitedProjects);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid)
            throws CoreException {
    	return findDatatypes(valuetypesOnly, includeVoid, true);
    }
    
    /**
	 * {@inheritDoc}
	 */
	public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid, boolean includePrimitives) throws CoreException {
        Set result = new LinkedHashSet();
        getValueDatatypes(includeVoid, includePrimitives, result);
        if (!valuetypesOnly) {
            List refDatatypes = new ArrayList();
            findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, refDatatypes);
            result.addAll(refDatatypes);
        }
        Datatype[] array = new Datatype[result.size()];
        result.toArray(array);
        return array;
	}

	/**
     * {@inheritDoc}
     */
    public EnumDatatype[] findEnumDatatypes() throws CoreException{
    	//TODO this implementation can be improved and instanceof can be avoided. Therefore the storage of EnumDatatypes an Datatypes
    	//has to be separated within the IpsModel class
    	
    	Datatype[] datatypes = findDatatypes(true, false);
    	ArrayList enumDatatypeList = new ArrayList();
    	for (int i = 0; i < datatypes.length; i++) {
			if(datatypes[i] instanceof EnumDatatype){
				enumDatatypeList.add(datatypes[i]);
			}
		}
    	return (EnumDatatype[])enumDatatypeList.toArray(new EnumDatatype[enumDatatypeList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public EnumDatatype findEnumDatatype(String qualifiedName) throws CoreException{
    	EnumDatatype[] allEnums = findEnumDatatypes();
    	for (int i = 0; i < allEnums.length; i++) {
			if(allEnums[i].getQualifiedName().equals(qualifiedName)){
				return allEnums[i];
			}
		}
    	return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public Datatype findDatatype(String qualifiedName) throws CoreException {
    	if (qualifiedName.equals(Datatype.VOID.getQualifiedName())) {
    		return Datatype.VOID;
    	}
    	Datatype type = findValueDatatype(qualifiedName);
        if (type!=null) {
        	return type;
        }
    	int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedName);
    	if(arrayDimension > 0){
    		qualifiedName = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedName);
    	}
    	IpsObjectType[] objectTypes = IpsObjectType.ALL_TYPES;
    	for (int i = 0; i < objectTypes.length; i++) {
			if (objectTypes[i].isDatatype()) {
	    		type = (Datatype)findIpsObject(objectTypes[i], qualifiedName);
				if (type!=null) {
					break;
				}
			}
		}
        if (arrayDimension==0) {
        	return type;
        }
    	if(type instanceof ValueDatatype){
    		return new ArrayOfValueDatatype(type, arrayDimension);
    	}
    	throw new IllegalArgumentException("The qualified name: \"" + qualifiedName +  //$NON-NLS-1$
    			"\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findValueDatatype(String qualifiedName) throws CoreException {
    	int arrayDimension = ArrayOfValueDatatype.getDimension(qualifiedName);
    	if(arrayDimension > 0){
    		qualifiedName = ArrayOfValueDatatype.getBasicDatatypeName(qualifiedName);
    	}
    	ValueDatatype type = findValueDatatype(this, qualifiedName, new HashSet());
        if (arrayDimension==0) {
        	return type;
        }
    	if(type instanceof ValueDatatype){
    		return new ArrayOfValueDatatype(type, arrayDimension);
    	}
    	throw new IllegalArgumentException("The qualified name: \"" + qualifiedName +  //$NON-NLS-1$
    			"\" specifies an array of a non value datatype. This is currently not supported."); //$NON-NLS-1$
    }

    private ValueDatatype findValueDatatype(IpsProject ipsProject, String qualifiedName, HashSet visitedProjects) throws CoreException {
            ValueDatatype datatype = ((IpsModel)getIpsModel()).getValueDatatype(ipsProject, qualifiedName);
            if (datatype!=null) {
            	return datatype;
            }
            IIpsProject[] projects = ((IpsProject)ipsProject).getIpsObjectPathInternal().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                if(!visitedProjects.contains(projects[i])){
                	visitedProjects.add(projects[i]);
                	datatype = findValueDatatype((IpsProject)projects[i], qualifiedName, visitedProjects);
                	if (datatype!=null) {
                		return datatype;
                	}
                }
            }
            return null;
    }

    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (!(datatype instanceof ValueDatatype)) {
            return null;
        }
        if(datatype instanceof ArrayOfValueDatatype){
        	return new ArrayOfValueDatatypeHelper(datatype);
        }
        DatatypeHelper helper = ((IpsModel)getIpsModel()).getDatatypeHelper(this,
            (ValueDatatype)datatype);
        if (helper != null) {
            return helper;
        }
        try {
            IIpsProject[] projects = getIpsObjectPathInternal().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                //helper = ((IpsModel)getIpsModel()).getDatatypeHelper(projects[i],
                 //   (ValueDatatype)datatype);
            	helper = projects[i].getDatatypeHelper(datatype);
                if (helper != null) {
                    return helper;
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public DatatypeHelper findDatatypeHelper(String qName) throws CoreException {
		Datatype datatype = findDatatype(qName);
		return getDatatypeHelper(datatype);
	}

	/**
	 * {@inheritDoc}
	 */
    public ValueSetType[] getValueSetTypes(ValueDatatype datatype) throws CoreException {
        ArgumentCheck.notNull(datatype);
        if (datatype instanceof NumericDatatype) {
            return ValueSetType.getValueSetTypes();
        }
        if (datatype instanceof MoneyDatatype) {
            return ValueSetType.getValueSetTypes();
        }
        if(datatype instanceof ArrayOfValueDatatype){
        	return new ValueSetType[] {ValueSetType.ALL_VALUES};
        }
        return new ValueSetType[] { ValueSetType.ALL_VALUES, ValueSetType.ENUM };
    }

	/**
	 * {@inheritDoc}
	 */
    public IProductCmpt[] findProductCmpts(String qualifiedTypeName, boolean includeSubytpes)
            throws CoreException {
    	
        List result = new ArrayList();
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            ((IpsPackageFragmentRoot)roots[i]).findProductCmpts(qualifiedTypeName,
                includeSubytpes, result);
        }
        IProductCmpt[] array = new IProductCmpt[result.size()];
        result.toArray(array);
        return array;
    }

    /**
     * {@inheritDoc}
     */
	public IProductCmpt findProductCmpt(String runtimeId) throws CoreException {
		
		if(runtimeId == null){
			return null;
		}

		IIpsObject[] all = findIpsObjects(IpsObjectType.PRODUCT_CMPT);
		
		for (int i = 0; i < all.length; i++) {
			if (((IProductCmpt)all[i]).getRuntimeId().equals(runtimeId)) {
				return (IProductCmpt)all[i];
			}
		}
		return null;
	}

	public IProductCmpt findProductCmptByQualifiedName(String qualifiedName) throws CoreException{
	
		if(qualifiedName == null){
			return null;
		}
		
		IIpsObject[] all = findIpsObjects(IpsObjectType.PRODUCT_CMPT);
		
		for (int i = 0; i < all.length; i++) {
			if (((IProductCmpt)all[i]).getQualifiedName().equals(qualifiedName)) {
				return (IProductCmpt)all[i];
			}
		}
		return null;
	}
	
	/**
     * Overridden.
     */
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() throws CoreException {
        List result = new ArrayList();
        getSourcePdPckFragmentRoots(result);
        IIpsPackageFragmentRoot[] sourceRoots = new IIpsPackageFragmentRoot[result.size()];
        result.toArray(sourceRoots);
        return sourceRoots;
    }

    void getSourcePdPckFragmentRoots(List result) throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].containsSourceFiles()) {
                result.add(roots[i]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilderSet getArtefactBuilderSet() throws CoreException {
        return ((IpsModel)getIpsModel()).getIpsArtefactBuilderSet(this);
    }

    /**
     * {@inheritDoc}
     */
	public IProductCmptGeneration[] findReferencingProductCmptGenerations(String qualifiedProductCmptName) throws CoreException {
		ArrayList result = new ArrayList();
		IIpsObject[] allProductCmpts = this.findIpsObjects(IpsObjectType.PRODUCT_CMPT);
		
		for (int i = 0; i < allProductCmpts.length; i++) {
			IProductCmptGeneration generation = (IProductCmptGeneration) ((IProductCmpt) allProductCmpts[i])
					.findGenerationEffectiveOn(IpsPlugin.getDefault()
							.getIpsPreferences().getWorkingDate());
			if (generation == null) {
				// it is possible have the working date set to a date in the past
				// where no generation exists for a product cmpt. In this case,
				// we ignore this product cmpt.
				continue;
			}
			IProductCmptRelation[] relations = generation.getRelations();
			for (int j = 0; j < relations.length; j++) {
				if (relations[j].getTarget().equals(qualifiedProductCmptName)) {
					result.add(generation);
					break;
				}
			}
		}
		IProductCmptGeneration[] resultArray = new IProductCmptGeneration[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptNamingStrategy getProductCmptNamingStratgey() throws CoreException {
		return getPropertiesInternal().getProductCmptNamingStrategy();
	}

	public void addDynamicValueDataType(DynamicValueDatatype newDatatype) throws CoreException  {
		((IpsProjectProperties)getPropertiesInternal()).addDefinedDatatype(newDatatype);
		saveProjectProperties(getProperties());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeId(IProductCmpt productCmpt) throws CoreException {
		if (runtimeIdStrategy == null) {
			runtimeIdStrategy = new DefaultRuntimeIdInitStrategy(); 
		}
		return runtimeIdStrategy.getRuntimeId(productCmpt);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeIdPrefix() {
		return getPropertiesInternal().getRuntimeIdPrefix();
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validate() throws CoreException {
		MessageList result = new MessageList();
		if (!getIpsProjectPropertiesFile().exists()) {
			String text = Messages.IpsProject_msgMissingDotIpsprojectFile;
			Message msg = new Message(IIpsProject.MSGCODE_MISSING_PROPERTY_FILE, text, Message.ERROR, this);
			result.add(msg);
			return result;
		}
		IpsProjectProperties props = (IpsProjectProperties)getPropertiesInternal();
		if (!props.isCreatedFromParsableFileContents()) {
			String text = Messages.IpsProject_msgUnparsableDotIpsprojectFile;
			Message msg = new Message(IIpsProject.MSGCODE_UNPARSABLE_PROPERTY_FILE, text, Message.ERROR, this);
			result.add(msg);
			return result;
		}
		MessageList list = props.validate(this);
		result.add(list);
		return result;
	}

	/**
	 * Returns the ClassLoaderProvider for the Java project that belongs to this ips project.
	 */
	public ClassLoaderProvider getClassLoaderProviderForJavaProject() {
		return ((IpsModel)getIpsModel()).getClassLoaderProvider(this);
	}
}
