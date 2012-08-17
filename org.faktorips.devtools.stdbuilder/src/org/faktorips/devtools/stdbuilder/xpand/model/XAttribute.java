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

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.util.StringUtil;

/**
 * Contains common behavior for product- and policy-attributes alike.
 * 
 * @author widmaier
 */
public abstract class XAttribute extends AbstractGeneratorModelNode {

    private DatatypeHelper datatypeHelper;

    public XAttribute(IAttribute attribute, GeneratorModelContext context, ModelService modelService) {
        super(attribute, context, modelService);
        try {
            datatypeHelper = attribute.getIpsProject().findDatatypeHelper(attribute.getDatatype());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IAttribute getIpsObjectPartContainer() {
        return (IAttribute)super.getIpsObjectPartContainer();
    }

    public IAttribute getAttribute() {
        return getIpsObjectPartContainer();
    }

    public DatatypeHelper getDatatypeHelper() {
        return datatypeHelper;
    }

    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getName(), getDatatype());
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    public String fieldPropertyNameSuffix() {
        return getContext().isGenerateSeparatedCamelCase() ? StringUtil.camelCaseToUnderscore(getName()).toUpperCase()
                : getName().toUpperCase();
    }

    public String getDefaultValueCode() {
        JavaCodeFragment newInstance = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        addImport(newInstance.getImportDeclaration());
        return newInstance.getSourcecode();
    }

    public final ValueDatatype getDatatype() {
        return (ValueDatatype)getDatatypeHelper().getDatatype();
    }

    /**
     * Returns a string representing the java datatype for this attribute. This datatype may however
     * be a primitive or a complex type. In case of a complex type (e.g. BigDecimal) an import is
     * added and the unqualified type name is returned. In case of a primitive the name of the type
     * is returned without adding an import.
     */
    public String getJavaClassName() {
        String javaClassName = getDatatypeHelper().getJavaClassName();
        return addImport(javaClassName);
    }

    public String getNewInstanceFromExpression(String expression) {
        JavaCodeFragment fragment = getDatatypeHelper().newInstanceFromExpression(expression);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public String getNewInstance() {
        JavaCodeFragment fragment = getDatatypeHelper().newInstance("");
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public String getToStringExpression(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().getToStringExpression(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public boolean isPublished() {
        return getAttribute().getModifier().isPublished();
    }

    public final boolean isOverwrite() {
        return getAttribute().isOverwrite();
    }

    public String getReferenceOrSafeCopyIfNecessary(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().referenceOrSafeCopyIfNeccessary(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

    public String getConstantNamePropertyName() {
        return "PROPERTY_" + StringUtils.upperCase(getFieldName());
    }

}
