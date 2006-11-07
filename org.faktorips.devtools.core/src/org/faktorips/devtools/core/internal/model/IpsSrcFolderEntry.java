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

import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsSrcFolderEntry.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntry extends IpsObjectPathEntry implements IIpsSrcFolderEntry {

    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return "Sourcefolder:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR  //$NON-NLS-1$
             + "    type=\"src\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    sourceFolder=\"model\"             Folder in the project that contains the FaktorIPS model and product definition files." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    outputFolderGenerated=\"src\"      Folder in the project where the Generator puts the Java source files." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    basePackageGenerated=\"org.foo\"   The package prefix for all generated classes." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    outputFolderExtension=\"\"         The FaktorIPS standard builder merges developer changes to the generated files while generating" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                     Other builders can choose to maintain user code in a separate folder which is defined here." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                     If you use the standard builder, leave the atribute empty." + SystemUtils.LINE_SEPARATOR  //$NON-NLS-1$
             + "    basePackageExtension=\"\">         Package prefix for Java classes in the output folder for extenions. See above." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                     If you use the standard builder, leave the atribute empty." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + " </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR;  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    // the folder containg the ips objects
    private IFolder sourceFolder;

    // the output folder containing the generated Java files.
    private IFolder outputFolderGenerated;

    // the name of the base package containing the generated Java files.
    private String basePackageGenerated = ""; //$NON-NLS-1$

    // the output folder containing the Java files where the developer adds it's own code.
    private IFolder outputFolderExtension;

    // the name of the base package containing the Java files where the developer adds it's own code.
    private String basePackageExtension = ""; //$NON-NLS-1$

    public IpsSrcFolderEntry(IpsObjectPath path) {
        super(path);
    }

    public IpsSrcFolderEntry(IpsObjectPath path, IFolder sourceFolder) {
        super(path);
        ArgumentCheck.notNull(sourceFolder);
        this.sourceFolder = sourceFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TYPE_SRC_FOLDER;
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getSourceFolder() {
        return sourceFolder;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(IIpsProject ipsProject) {
        return ipsProject.getIpsPackageFragmentRoot(sourceFolder.getName());
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolderForGeneratedJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderGenerated;
        }
        return getIpsObjectPath().getOutputFolderForGeneratedJavaFiles();
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getSpecificOutputFolderForGeneratedJavaFiles() {
        return outputFolderGenerated;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificOutputFolderForGeneratedJavaFiles(IFolder outputFolder) {
        this.outputFolderGenerated = outputFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForGeneratedJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageGenerated;
        }
        return getIpsObjectPath().getBasePackageNameForGeneratedJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public String getSpecificBasePackageNameForGeneratedJavaClasses() {
        return basePackageGenerated;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificBasePackageNameForGeneratedJavaClasses(String name) {
        this.basePackageGenerated = name;
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolderForExtensionJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderExtension;
        }
        return getIpsObjectPath().getOutputFolderForExtensionJavaFiles();
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getSpecificOutputFolderForExtensionJavaFiles() {
        return outputFolderExtension;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificOutputFolderForExtensionJavaFiles(IFolder outputFolder) {
        outputFolderExtension = outputFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForExtensionJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageExtension;
        }
        return getIpsObjectPath().getBasePackageNameForExtensionJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public String getSpecificBasePackageNameForExtensionJavaClasses() {
        return basePackageExtension;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificBasePackageNameForExtensionJavaClasses(String name) {
        basePackageExtension = name;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(IIpsProject ipsProject, QualifiedNameType nameType) throws CoreException {
        return getIpsPackageFragmentRoot(ipsProject).findIpsObject(nameType);
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjects(IIpsProject ipsProject, IpsObjectType type, List result) throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot(ipsProject)).findIpsObjects(type, result);
    }

    /**
     * {@inheritDoc}
     */
    protected void findIpsObjectsStartingWith(IIpsProject ipsProject, IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot(ipsProject)).findIpsObjectsStartingWith(type, prefix, ignoreCase,
                result);
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(Element element, IProject project) {
        String sourceFolderPath = element.getAttribute("sourceFolder"); //$NON-NLS-1$
        sourceFolder = project.getFolder(new Path(sourceFolderPath));
        String outputFolderPathGenerated = element.getAttribute("outputFolderGenerated"); //$NON-NLS-1$
        outputFolderGenerated = outputFolderPathGenerated.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathGenerated));
        basePackageGenerated = element.getAttribute("basePackageGenerated"); //$NON-NLS-1$
        String outputFolderPathExtension = element.getAttribute("outputFolderExtension"); //$NON-NLS-1$
        outputFolderExtension = outputFolderPathExtension.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathExtension));
        basePackageExtension = element.getAttribute("basePackageExtension"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(IpsObjectPathEntry.XML_ELEMENT);
        element.setAttribute("type", TYPE_SRC_FOLDER); //$NON-NLS-1$
        element.setAttribute("sourceFolder", sourceFolder.getProjectRelativePath().toString()); //$NON-NLS-1$
        element.setAttribute("outputFolderGenerated", outputFolderGenerated == null ? "" : outputFolderGenerated //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageGenerated", basePackageGenerated==null ? "" : basePackageGenerated); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("outputFolderExtension", outputFolderExtension == null ? "" : outputFolderExtension //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageExtension", basePackageExtension==null ? "" : basePackageExtension); //$NON-NLS-1$ //$NON-NLS-2$
        return element;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        // the sourceFolder will never be null (see this#initFromXml)
        result.add(validateFolder(sourceFolder));

        if (outputFolderGenerated != null) {
            result.add(validateFolder(outputFolderGenerated));
        }
        if (outputFolderExtension != null) {
            result.add(validateFolder(outputFolderExtension));
        }
        return result;
    }
    
    /*
     * Validate that the given folder exists.
     */
    private MessageList validateFolder(IFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()){
            String text = NLS.bind(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }    
}