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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of the published interface.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeMethod extends Method implements IProductCmptTypeMethod {

    private boolean formulaSignatureDefinition = true;
    private String formulaName = ""; //$NON-NLS-1$
    private boolean overloadsFormula = false;
    
    
    public ProductCmptTypeMethod(IProductCmptType parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public String getFormulaName() {
        return formulaName;
    }

    /**
     * {@inheritDoc}
     */
    public void setFormulaName(String newName) {
        String oldName = formulaName;
        formulaName = newName;
        valueChanged(oldName, newName);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isFormulaSignatureDefinition() {
        return formulaSignatureDefinition;
    }

    /**
     * {@inheritDoc}
     */
    public void setFormulaSignatureDefinition(boolean newValue) {
        boolean oldValue = formulaSignatureDefinition;
        formulaSignatureDefinition = newValue;
        if (!formulaSignatureDefinition) {
            formulaName = ""; //$NON-NLS-1$
            overloadsFormula = false; //$NON-NLS-1$
        } else {
            setAbstract(false);
        }
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public void setOverloadsFormula(boolean enabled) {
        boolean oldValue = this.overloadsFormula;
        this.overloadsFormula = enabled;
        valueChanged(oldValue, enabled);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOverloadsFormula(){
        return overloadsFormula; 
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod findOverloadedFormulaMethod(IIpsProject ipsProject) throws CoreException{
        if(!isOverloadsFormula()){
            return null;
        }
        FormulaNameFinder finder = new FormulaNameFinder(ipsProject);
        finder.start(getProductCmptType().findSupertype(ipsProject));
        return finder.method;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultMethodName() {
        if (isFormulaSignatureDefinition()) {
            return "compute" + StringUtils.capitalize(getFormulaName()); //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }
    
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        formulaSignatureDefinition = Boolean.valueOf(element.getAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION)).booleanValue();
        formulaName = element.getAttribute(PROPERTY_FORMULA_NAME);
        overloadsFormula = Boolean.valueOf(element.getAttribute(PROPERTY_OVERLOADS_FORMULA)).booleanValue();
    }

    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_FORMULA_SIGNATURE_DEFINITION, "" + formulaSignatureDefinition); //$NON-NLS-1$
        element.setAttribute(PROPERTY_FORMULA_NAME, formulaName);
        element.setAttribute(PROPERTY_OVERLOADS_FORMULA, String.valueOf(overloadsFormula));
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyName() {
        if (formulaSignatureDefinition) {
            return formulaName;
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.FORMULA;
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyDatatype() {
        return getDatatype();
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        if (!isFormulaSignatureDefinition()) {
            return;
        }
        if (StringUtils.isEmpty(formulaName)) {
            String text = Messages.ProductCmptTypeMethod_FormulaNameIsMissing;
            result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY, text, Message.ERROR, this, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME));
        }
        Datatype datatype = findDatatype(ipsProject);
        if (datatype!=null) {
            if (datatype.isVoid() || !datatype.isValueDatatype()) {
                String text = Messages.ProductCmptTypeMethod_FormulaSignatureDatatypeMustBeAValueDatatype;
                result.add(new Message(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES, text, Message.ERROR, this, IMethod.PROPERTY_DATATYPE));
            }
        }
        if (isAbstract()) {
            String text = Messages.ProductCmptTypeMethod_FormulaSignatureMustntBeAbstract;
            result.add(new Message(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT, text, Message.ERROR, this, IMethod.PROPERTY_ABSTRACT));
        }
        if (isFormulaSignatureDefinition() && isOverloadsFormula()) {
            FormulaNameFinder finder = new FormulaNameFinder(ipsProject);
            finder.start(getProductCmptType().findSuperProductCmptType(ipsProject));
            if (!StringUtils.isEmpty(formulaName) && !finder.formulaNameFound()) {
                result.add(new Message(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY,
                        Messages.ProductCmptTypeMethod_msgNoOverloadableFormulaInSupertypeHierarchy, Message.ERROR,
                        this, IProductCmptTypeMethod.PROPERTY_OVERLOADS_FORMULA));
            }
        }
    }
    
    
    /*
     * Looks for a formula in the supertype hierarchy with the same name than the formula name of
     * this formula. It stops looking when the first formula method is found that meets this
     * condition or if the super type hierarchy ends.
     */
    private class FormulaNameFinder extends ProductCmptTypeHierarchyVisitor {

        private IProductCmptTypeMethod method;
        
        public FormulaNameFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        private boolean formulaNameFound(){
            return method != null;
        }
        
        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            if(StringUtils.isEmpty(formulaName) || currentType == null){
                return false;
            }
            method = currentType.getFormulaSignature(formulaName);
            return method == null;
        }
    }

}
