package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.util.StringUtil;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of IAttribute.
 */
public class Attribute extends Member implements IAttribute {

    private final static String[] JAVA_METHOD_PREFIX;
    private final static int[] JAVA_METHOD_OF_TYPE;

    static {
        JAVA_METHOD_PREFIX = new String[JAVA_NUMOF_METHOD];

        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_IMPLEMENATION] = "get";
        JAVA_METHOD_PREFIX[JAVA_SETTER_METHOD_IMPLEMENATION] = "set";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_INTERFACE] = "get";
        JAVA_METHOD_PREFIX[JAVA_SETTER_METHOD_INTERFACE] = "set";
        JAVA_METHOD_PREFIX[JAVA_COMPUTE_ATTRIBUTE_METHOD_PRODUCTCMPT_INTERFACE] = "compute";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_DEFAULTVALUE_PRODUCT_INTERFACE] = "getVorgabewert";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_VALUE_PRODUCT_INTERFACE] = "get";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_DEFAULTVALUE_PRODUCT_IMPL] = "getVorgabewert";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_VALUE_PRODUCT_IMPL] = "get";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_MAX_VALUESET_POLICY_INTERFACE] = "getMaxWertebereich";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_MAX_VALUESET_POLICY_IMPL] = "getMaxWertebereich";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_MAX_VALUESET_PRODUCT_INTERFACE] = "getMaxWertebereich";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_MAX_VALUESET_PRODUCT_IMPL] = "getMaxWertebereich";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_VALUESET_POLICY_INTERFACE] = "getWertebereich";
        JAVA_METHOD_PREFIX[JAVA_GETTER_METHOD_VALUESET_POLICY_IMPL] = "getWertebereich";

        JAVA_METHOD_OF_TYPE = new int[JAVA_NUMOF_METHOD];

        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_IMPLEMENATION] = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_SETTER_METHOD_IMPLEMENATION] = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_INTERFACE] = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_SETTER_METHOD_INTERFACE] = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_COMPUTE_ATTRIBUTE_METHOD_PRODUCTCMPT_INTERFACE] = IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_DEFAULTVALUE_PRODUCT_INTERFACE] = IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_VALUE_PRODUCT_INTERFACE] = IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_DEFAULTVALUE_PRODUCT_IMPL] = IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_VALUE_PRODUCT_IMPL] = IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_MAX_VALUESET_POLICY_INTERFACE] = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_MAX_VALUESET_POLICY_IMPL] = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_MAX_VALUESET_PRODUCT_INTERFACE] = IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_MAX_VALUESET_PRODUCT_IMPL] = IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_VALUESET_POLICY_INTERFACE] = IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE;
        JAVA_METHOD_OF_TYPE[JAVA_GETTER_METHOD_VALUESET_POLICY_IMPL] = IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE;
    }
    
    private static final String TAG_PROPERTY_PARAMETER = "FormulaParameter";
    final static String TAG_NAME = "Attribute";
    private static final String TAG_PARAM_NAME = "name";
    private static final String TAG_PARAM_DATATYPE = "datatype";

    // member variables.
    private String datatype = "";
    private boolean productRelevant = true;
    private AttributeType attributeType = AttributeType.CHANGEABLE;
    private String defaultValue = "";
    private Modifier modifier = Modifier.PUBLISHED;
    private Parameter[] parameters = new Parameter[0];
    private ValueSet valueSet = ValueSet.ALL_VALUES;

    /**
     * Creates a new attribute.
     * 
     * @param pcType The type the attribute belongs to.
     * @param id The attribute's unique id within the type.
     */
    Attribute(PolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    Attribute() {
    }

    PolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }

    /**
     * Overridden method.
     */
    public void delete() {
        getPolicyCmptType().removeAttribute(this);
        updateSrcFile();
    }

    /**
     * Overridden method.
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * Overridden.
     */
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        this.datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }
    
    /**
     * Overridden.
     */
    public Datatype findDatatype() throws CoreException {
        return getIpsProject().findDatatype(datatype);
    }

    /**
     * Overridden method.
     */
    public void setAttributeType(AttributeType newType) {
        AttributeType oldType = attributeType;
        attributeType = newType;
        valueChanged(oldType, newType);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#getAttributeType()
     */
    public AttributeType getAttributeType() {
        return attributeType;
    }

    /**
     * Overridden method.
     */
    public boolean isChangeable() {
		return attributeType==AttributeType.CHANGEABLE;
	}
    
    /**
     * Overridden IMethod.
     */
	public boolean isDerivedOrComputed() {
        return attributeType==AttributeType.DERIVED || attributeType==AttributeType.COMPUTED;
    }

    /**
     * Overridden method.
     */
    public Modifier getModifier() {
        return modifier;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#setModifier(org.faktorips.devtools.core.model.pctype.Modifier)
     */
    public void setModifier(Modifier newModifier) {
        Modifier oldModifier = modifier;
        modifier = newModifier;
        valueChanged(oldModifier, newModifier);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#isProductRelevant()
     */
    public boolean isProductRelevant() {
        return productRelevant;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#setProductRelevant(boolean)
     */
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#getDefaultValue()
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String newValue) {
        String oldValue = defaultValue;
        defaultValue = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#getValueSet()
     */
    public ValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#setValueSet(org.faktorips.devtools.core.model.ValueSet)
     */
    public void setValueSet(ValueSet valueSet) {
        this.valueSet = valueSet;
        updateSrcFile();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#getConfigElementType()
     */
    public ConfigElementType getConfigElementType() {
        if (!productRelevant) {
            return null;
        }
        if (attributeType == AttributeType.CHANGEABLE) {
            return ConfigElementType.POLICY_ATTRIBUTE;
        }
        if (attributeType == AttributeType.CONSTANT) {
            return ConfigElementType.PRODUCT_ATTRIBUTE;
        }
        if (attributeType == AttributeType.COMPUTED || attributeType == AttributeType.DERIVED) {
            return ConfigElementType.FORMULA;
        }
        throw new RuntimeException("Unkown AttributeType!");
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        if (modifier == Modifier.PRIVATE) {
            return IpsPlugin.getDefault().getImage("AttributePrivate.gif");
        } else {
            return IpsPlugin.getDefault().getImage("AttributePublic.gif");
        }

    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getParameters()
     */
    public Parameter[] getFormulaParameters() {
        Parameter[] copy = new Parameter[parameters.length];
        System.arraycopy(parameters, 0, copy, 0, parameters.length);
        return copy;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IMethod#getNumOfParameters()
     */
    public int getNumOfFormulaParameters() {
        return parameters.length;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IMethod#setParameters(org.faktorips.devtools.core.model.pctype.Parameter[])
     */
    public void setFormulaParameters(Parameter[] params) {
        parameters = new Parameter[params.length];
        System.arraycopy(params, 0, parameters, 0, params.length);
        updateSrcFile();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#getJavaMethod(int)
     */
    public IMethod getJavaMethod(int type) throws CoreException {
        if (type < 0 || type > JAVA_NUMOF_METHOD) {
            throw new IllegalArgumentException("Unkown type " + type);
        }
        String[] paramTypeSignature;
        if (type == JAVA_COMPUTE_ATTRIBUTE_METHOD_PRODUCTCMPT_INTERFACE) {
            return getJavaComputationMethod();
        }
        if (type == JAVA_SETTER_METHOD_IMPLEMENATION || type == JAVA_SETTER_METHOD_INTERFACE) {
            String paramTypeName = getIpsProject().findDatatype(datatype).getJavaClassName();
            String param = Signature.createTypeSignature(paramTypeName, false);
            paramTypeSignature = new String[] { param };
        } else {
            paramTypeSignature = new String[0];
        }
        String methodName = JAVA_METHOD_PREFIX[type] + StringUtils.capitalise(getName());
        return getPolicyCmptType().getJavaType(JAVA_METHOD_OF_TYPE[type]).getMethod(methodName, paramTypeSignature);
    }

    /*
     * Returns the compuation method in the product component interface.
     */
    private IMethod getJavaComputationMethod() throws CoreException {
        if (getAttributeType() != AttributeType.COMPUTED && getAttributeType() != AttributeType.DERIVED) {
            return null;
        }
        String methodname = "compute" + StringUtils.capitalise(getName());
        IType javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE);
        Parameter[] params = getFormulaParameters();
        String[] typeSignatures = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            Datatype datatype = getIpsProject().findDatatype(params[i].getDatatype());
            String datatypeClassname = StringUtil.unqualifiedName(datatype.getJavaClassName());
            typeSignatures[i] = Signature.createTypeSignature(datatypeClassname, false);
        }
        return javaType.getMethod(methodname, typeSignatures);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.pctype.IAttribute#getJavaField(int)
     */
    public IField getJavaField(int type) throws CoreException {
        if (type != JAVA_FIELD_VALUE_POLICY && type != JAVA_FIELD_VALUE_PRODUCT && type != JAVA_FIELD_VALUESET_POLICY
                && type != JAVA_FIELD_VALUESET_PRODUCT) {
            throw new IllegalArgumentException("Unkown type " + type);
        }
        String name = getName();
        if (type == JAVA_FIELD_VALUESET_POLICY || type == JAVA_FIELD_VALUESET_PRODUCT) {
            name = "maxWertebereich" + StringUtils.capitalise(name);
        }

        IType javaType;
        if (type == JAVA_FIELD_VALUE_POLICY) {
            if (getModifier() == Modifier.PUBLISHED && getAttributeType() == AttributeType.CONSTANT
                    && !isProductRelevant()) {
                javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE);
            } else {
                javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE);
            }
        } else if (type == JAVA_FIELD_VALUESET_POLICY) {
            if (getModifier() == Modifier.PUBLISHED && !isProductRelevant()) {
                javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE);
            } else {
                javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE);
            }
        } else {
            javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE);
        }
        return javaType.getField(name);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPart#validate(org.faktorips.util.message.MessageList)
     */
    public void validate(MessageList result) throws CoreException {
    	super.validate(result);
        IStatus status = JavaConventions.validateFieldName(name);
        if (!status.isOK()) {
            result.add(new Message("", "Invalid attribute name " + name + "!", Message.ERROR, this, PROPERTY_NAME));
        }
        Datatype datatypeObject = ValidationUtils.checkDatatypeReference(datatype, true, false, this,
                PROPERTY_DATATYPE, result);
        if (datatypeObject == null) {
            if (!StringUtils.isEmpty(defaultValue)) {
                String text = "The default value " + defaultValue + " can't be parsed because the datatype is unkown!";
                result.add(new Message("", text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE));
            } else {
            }
        } else {
            if (!datatypeObject.isValueDatatype()) {
                if (!StringUtils.isEmpty(datatype)) {
                    String text = "The default value " + defaultValue + " can't be parsed because the datatype is not a value datatype!";
                    result.add(new Message("", text, Message.WARNING, this, PROPERTY_DEFAULT_VALUE));
                } else {
                }
            } else {
                ValueDatatype valueDatatype = (ValueDatatype)datatypeObject;
                if (StringUtils.isNotEmpty(defaultValue)) {
                    if (!valueDatatype.isParsable(defaultValue)) {
                        String text = "The default value " + defaultValue + " is not a " + datatype + ".";
                        result.add(new Message("", text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE));
                        return;
                    }
                    if (valueSet != null) {
                        if (valueSet.contains(defaultValue, valueDatatype) == false) {
                            result.add(new Message("", "The default value " + defaultValue
                                    + " is no member of the specified valueSet!", Message.ERROR, this,
                                    PROPERTY_DEFAULT_VALUE));
                        }
                    }
                }
                valueSet.validate(valueDatatype, result);
            }
            if (isDerivedOrComputed() && isProductRelevant() && parameters.length == 0) {
                String text = "No parameters are defined as input for the calculation formulas.";
                result.add(new Message("", text, Message.WARNING, this));
            }
        }
        for (int i = 0; i < parameters.length; i++) {
            validate(parameters[i], result);
        }

    }

    private void validate(Parameter param, MessageList result) throws CoreException {
        if (!isDerivedOrComputed() && !isProductRelevant()) {
            String text = "The definition of calculation parameters is only neccessary for computed attributes that are product relevant.";
            result.add(new Message("", text, Message.WARNING, param));
        }
        if (StringUtils.isEmpty(param.getName())) {
            result.add(new Message("", "The name is empty!", Message.ERROR, param, PROPERTY_FORMULAPARAM_NAME));
        } else {
            IStatus status = JavaConventions.validateIdentifier(param.getName());
            if (!status.isOK()) {
                result
                        .add(new Message("", "Invalid parameter name.", Message.ERROR, param,
                                PROPERTY_FORMULAPARAM_NAME));
            }
        }
        if (StringUtils.isEmpty(param.getDatatype())) {
            result.add(new Message("", "The datatype is empty!", Message.ERROR, param, PROPERTY_FORMULAPARAM_DATATYPE));
        } else {
            Datatype datatypeObject = getIpsProject().findDatatype(param.getDatatype());
            if (datatypeObject == null) {
                result
                        .add(new Message("", "The datatype " + param.getDatatype()
                                + " does not exists on the object path!", Message.ERROR, param,
                                PROPERTY_FORMULAPARAM_DATATYPE));
            }
        }
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        attributeType = AttributeType.getAttributeType(element.getAttribute(PROPERTY_ATTRIBUTE_TYPE));
        productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
        defaultValue = element.getAttribute(PROPERTY_DEFAULT_VALUE);
        Element valueSetEl = XmlUtil.getFirstElement(element, ValueSet.XML_TAG);
        if (valueSetEl == null) {
            valueSet = ValueSet.ALL_VALUES;
        } else {
            valueSet = ValueSet.createFromXml(valueSetEl);
        }
        // get the nodes with the parameter information
        NodeList nl = element.getElementsByTagName(TAG_PROPERTY_PARAMETER);
        List params = new ArrayList();
        int paramIndex = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element paramElement = (Element)nl.item(i);
                Parameter newParam = new Parameter(paramIndex);
                newParam.setName(paramElement.getAttribute(TAG_PARAM_NAME));
                newParam.setDatatype(paramElement.getAttribute(TAG_PARAM_DATATYPE));
                params.add(newParam);
                paramIndex++;
            }
        }
        parameters = (Parameter[])params.toArray(new Parameter[0]);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant);
        element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        element.setAttribute(PROPERTY_ATTRIBUTE_TYPE, attributeType.getId());
        element.setAttribute(PROPERTY_DEFAULT_VALUE, defaultValue);
        Document doc = element.getOwnerDocument();
        element.appendChild(valueSet.toXml(doc));
        for (int i = 0; i < parameters.length; i++) {
            Element newParamElement = doc.createElement(TAG_PROPERTY_PARAMETER);
            newParamElement.setAttribute(TAG_PARAM_NAME, parameters[i].getName());
            newParamElement.setAttribute(TAG_PARAM_DATATYPE, parameters[i].getDatatype());
            element.appendChild(newParamElement);
        }
    }
    
   
}