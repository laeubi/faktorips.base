/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcasetype;

import java.text.MessageFormat;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test attribute class. Defines an attribute for a specific policy component parameter class within
 * a test case type definition.
 * 
 * @author Joerg Ortmann
 */
public class TestAttribute extends AtomicIpsObjectPart implements ITestAttribute {

    static final String TAG_NAME = "TestAttribute"; //$NON-NLS-1$

    private String attribute = ""; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$

    private String policyCmptType = ""; //$NON-NLS-1$

    private TestParameterType type = TestParameterType.COMBINED;

    public TestAttribute(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public void setAttribute(String newAttribute) {
        String oldAttribute = attribute;
        attribute = newAttribute;
        valueChanged(oldAttribute, newAttribute);
    }

    @Override
    public void setAttribute(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        String oldAttribute = attribute;
        String oldPolicyCmptTypeNameOfAttribute = policyCmptType;

        attribute = policyCmptTypeAttribute.getName();

        /*
         * set the policy cmpt type only if the given attribute belongs to a different type the test
         * policy cmpt type parameter belongs to. Because only in this case it is necessary, in all
         * other cases it is better to leave the policy cmpt type property empty to get a minimum
         * dependency to the model objects (e.g. if the attribute will be moved to a other type in
         * the hierarchy later on).
         */
        String policyCmptTypeNameOfAttribute = policyCmptTypeAttribute.getPolicyCmptType().getQualifiedName();
        if (!policyCmptTypeNameOfAttribute.equals(getTestPolicyCmptTypeParameter().getPolicyCmptType())) {
            policyCmptType = policyCmptTypeNameOfAttribute;
        } else {
            policyCmptType = ""; //$NON-NLS-1$
        }

        if (!valueChanged(oldAttribute, attribute)) {
            valueChanged(oldPolicyCmptTypeNameOfAttribute, policyCmptType);
        }
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }

    @Override
    public ValueDatatype findDatatype(IIpsProject project) {
        if (IpsStringUtils.isEmpty(attribute)) {
            return project.findValueDatatype(datatype);
        }
        IPolicyCmptTypeAttribute attr = findAttribute(project);
        if (attr == null) {
            return null;
        }
        return attr.findDatatype(project);
    }

    @Override
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    @Override
    public void setPolicyCmptType(String policyCmptType) {
        String oldPolicyCmptType = this.policyCmptType;
        this.policyCmptType = policyCmptType;
        valueChanged(oldPolicyCmptType, policyCmptType);
    }

    @Override
    public String getCorrespondingPolicyCmptType() {
        IPolicyCmptTypeAttribute attribute = findAttribute(getIpsProject());
        if (attribute != null) {
            return attribute.getPolicyCmptType().getQualifiedName();
        }

        // attribute wasn't found, return at least the stored policy cmpt type
        return policyCmptType;
    }

    @Override
    public TestPolicyCmptTypeParameter getTestPolicyCmptTypeParameter() {
        return (TestPolicyCmptTypeParameter)getParent();
    }

    @Override
    public IPolicyCmptTypeAttribute findAttribute(IIpsProject ipsProject) {
        if (IpsStringUtils.isEmpty(attribute)) {
            return null;
        }
        // select the policy cmpt type the searching of the attribute will be started (lowest type
        // in the hierarchy),
        // a) if the policyCmptType property is not given then use the policy cmpt type
        // of the test policy cmpt type parameter
        // b) if the policyCmptType property is set then use the specified policyCmptType,
        // because maybe the test parameter points to a abstract (general)
        // policy cmpt type and this attribute is a attribute of a subclass.
        IPolicyCmptType pcType = null;
        if (IpsStringUtils.isEmpty(policyCmptType)) {
            pcType = ((TestPolicyCmptTypeParameter)getParent()).findPolicyCmptType(ipsProject);
        } else {
            pcType = ipsProject.findPolicyCmptType(policyCmptType);
        }
        if (pcType == null) {
            return null;
        }
        return pcType.findPolicyCmptTypeAttribute(attribute, ipsProject);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        attribute = element.getAttribute(PROPERTY_ATTRIBUTE);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        policyCmptType = element.getAttribute(PROPERTY_POLICYCMPTTYPE_OF_ATTRIBUTE);
        type = TestParameterType.getTestParameterType(element.getAttribute(PROPERTY_TEST_ATTRIBUTE_TYPE));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_ATTRIBUTE, attribute);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_POLICYCMPTTYPE_OF_ATTRIBUTE, policyCmptType);
        element.setAttribute(PROPERTY_TEST_ATTRIBUTE_TYPE, type.getId());
    }

    @Override
    public boolean isExpextedResultAttribute() {
        return type == TestParameterType.EXPECTED_RESULT;
    }

    @Override
    public boolean isInputAttribute() {
        return type == TestParameterType.INPUT;
    }

    @Override
    public TestParameterType getTestAttributeType() {
        return type;
    }

    @Override
    public void setTestAttributeType(TestParameterType type) {
        // assert that the given type is an input or an expected result type,
        // because attributes could have the type combined (input and expected result together)
        ArgumentCheck.isTrue(type == TestParameterType.INPUT || type == TestParameterType.EXPECTED_RESULT);
        TestParameterType oldType = this.type;
        this.type = type;
        valueChanged(oldType, type);
    }

    @Override
    public boolean isAttributeRelevantByProductCmpt(IProductCmpt productCmpt, IIpsProject ipsProject) {
        boolean reqProductCmpt = ((ITestPolicyCmptTypeParameter)getParent()).isRequiresProductCmpt();
        if (!reqProductCmpt) {
            return true;
        }
        if (productCmpt == null) {
            return false;
        }
        IPolicyCmptType policyCmptType = productCmpt.findPolicyCmptType(ipsProject);
        return !(policyCmptType.findPolicyCmptTypeAttribute(getAttribute(), ipsProject) == null);
    }

    @Override
    public boolean isBasedOnModelAttribute() {
        return !IpsStringUtils.isEmpty(attribute) && IpsStringUtils.isEmpty(datatype);
    }

    @Override
    protected void validateThis(MessageList messageList, IIpsProject ipsProject) {
        super.validateThis(messageList, ipsProject);

        // check if the name is not empty
        if (IpsStringUtils.isEmpty(getName())) {
            String text = Messages.TestAttribute_TestAttribute_Error_NameIsEmpty;
            Message msg = new Message(MSGCODE_ATTRIBUTE_NAME_IS_EMPTY, text, Message.ERROR, this,
                    IIpsElement.PROPERTY_NAME);
            messageList.add(msg);
        }

        // check if the attribute exists
        IPolicyCmptTypeAttribute modelAttribute = findAttribute(ipsProject);
        if (isBasedOnModelAttribute() && modelAttribute == null) {
            String text = MessageFormat.format(Messages.TestAttribute_Error_AttributeNotFound, getAttribute());
            Message msg = new Message(MSGCODE_ATTRIBUTE_NOT_FOUND, text, Message.ERROR, this,
                    ITestAttribute.PROPERTY_ATTRIBUTE);
            messageList.add(msg);
        }

        // check if the attribute and the datatype are set together
        if (IpsStringUtils.isNotEmpty(attribute) && IpsStringUtils.isNotEmpty(datatype)) {
            String text = Messages.TestAttribute_Error_AttributeAndDatatypeGiven;
            Message msg = new Message(MSGCODE_DATATYPE_AND_ATTRIBUTE_GIVEN, text, Message.ERROR, this,
                    ITestAttribute.PROPERTY_DATATYPE);
            messageList.add(msg);
        }

        // check if the datatype exists
        if (!isBasedOnModelAttribute()) {
            ValidationUtils.checkDatatypeReference(datatype, false, this, ITestAttribute.PROPERTY_DATATYPE,
                    MSGCODE_DATATYPE_NOT_FOUND, messageList, ipsProject);
        }

        // check the correct type
        if (!isInputAttribute() && !isExpextedResultAttribute()) {
            String text = MessageFormat.format(Messages.TestAttribute_Error_TypeNotAllowed, type, name);
            Message msg = new Message(MSGCODE_WRONG_TYPE, text, Message.ERROR, this, PROPERTY_TEST_ATTRIBUTE_TYPE);
            messageList.add(msg);
        }

        // check if the type of the attribute matches the type of the parent
        TestParameterType parentType = ((ITestPolicyCmptTypeParameter)getParent()).getTestParameterType();
        if (!TestParameterType.isChildTypeMatching(type, parentType)) {
            String text = MessageFormat.format(Messages.TestAttribute_Error_TypeNotAllowedIfParent, type,
                    parentType.getName());
            Message msg = new Message(MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE, text, Message.ERROR, this,
                    PROPERTY_TEST_ATTRIBUTE_TYPE);
            messageList.add(msg);
        }

        // check for duplicate test attribute names and types
        TestPolicyCmptTypeParameter typeParam = (TestPolicyCmptTypeParameter)getParent();
        ITestAttribute[] testAttributes = typeParam.getTestAttributes();
        for (ITestAttribute testAttribute : testAttributes) {
            if (testAttribute != this && testAttribute.getName().equals(name)) {
                // duplicate name
                String text = MessageFormat.format(Messages.TestAttribute_Error_DuplicateName, name);
                Message msg = new Message(MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                messageList.add(msg);
                break;
            } else if (isBasedOnModelAttribute() && testAttribute != this
                    && testAttribute.getAttribute().equals(attribute) && testAttribute.getTestAttributeType() == type) {
                // duplicate attribute and type
                String text = MessageFormat.format(Messages.TestAttribute_ValidationError_DuplicateAttributeAndType,
                        attribute,
                        type.getName());
                Message msg = new Message(MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE, text, Message.ERROR, this,
                        PROPERTY_ATTRIBUTE);
                messageList.add(msg);
                break;
            }
        }

        if (modelAttribute != null) {
            // check that derived (computed on the fly) attributes are not supported
            if (AttributeType.DERIVED_ON_THE_FLY.equals(modelAttribute.getAttributeType())) {
                String text = Messages.TestAttribute_ValidationWarning_DerivedOnTheFlyAttributesAreNotSupported;
                Message msg = new Message(MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED, text, Message.WARNING,
                        this, PROPERTY_TEST_ATTRIBUTE_TYPE);
                messageList.add(msg);
            }
            // check that abstract attributes are not supported
            if (isBasedOnModelAttribute()) {
                ValueDatatype valueDatatype = findDatatype(getIpsProject());
                if (valueDatatype != null && valueDatatype.isAbstract()) {
                    String text = MessageFormat.format(Messages.TestAttribute_ValidationError_AbstractAttribute,
                            attribute,
                            valueDatatype);
                    Message msg = new Message(MSGCODE_ABSTRACT_ATTRIBUTES_NOT_SUPPORTED, text, Message.ERROR,
                            this);
                    messageList.add(msg);
                }
            }
        }

        validateName(messageList, ipsProject);
    }

    private void validateName(MessageList messageList, IIpsProject ipsProject) {
        if (isBasedOnModelAttribute()) {
            return;
        }
        /*
         * the name of extension attributes must be a valid java name, because for this type of
         * attributes a constant will be generated inside the test case java class
         */
        IStatus status = ValidationUtils.validateFieldName(name, ipsProject);
        if (!status.isOK()) {
            messageList.add(new Message(MSGCODE_INVALID_TEST_ATTRIBUTE_NAME,
                    MessageFormat.format(Messages.TestAttribute_TestAttribute_Error_InvalidTestAttributeName, name),
                    Message.ERROR,
                    this, PROPERTY_NAME));
        }
    }

}
