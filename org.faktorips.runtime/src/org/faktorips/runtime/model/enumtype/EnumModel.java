/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.enumtype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnum;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Description of an enum's attributes and extensibility.
 */
public class EnumModel extends AbstractModelElement {

    public static final String KIND_NAME = "EnumType";

    private final MessagesHelper messagesHelper;

    private List<String> attributeNames;

    private LinkedHashMap<String, EnumAttributeModel> attributeModels;

    private IpsExtensibleEnum ipsExtensibleEnum;

    public EnumModel(Class<?> enumTypeClass) {
        super(enumTypeClass.getAnnotation(IpsEnum.class).name(), enumTypeClass
                .getAnnotation(IpsExtensionProperties.class));
        IpsEnum annotation = enumTypeClass.getAnnotation(IpsEnum.class);
        attributeNames = Arrays.asList(annotation.attributeNames());
        attributeModels = EnumAttributeModel.createFrom(this, enumTypeClass);
        ipsExtensibleEnum = enumTypeClass.getAnnotation(IpsExtensibleEnum.class);
        messagesHelper = createMessageHelper(enumTypeClass.getAnnotation(IpsDocumented.class),
                enumTypeClass.getClassLoader());
    }

    /**
     * Whether the enum's values can be extended in an enum content provided in a
     * {@link IRuntimeRepository}.
     */
    public boolean isExtensible() {
        return ipsExtensibleEnum != null;
    }

    /**
     * The qualified name an enum content extending this enum must have.
     * 
     * @see #isExtensible()
     */
    public String getEnumContentQualifiedName() {
        return isExtensible() ? ipsExtensibleEnum.enumContentName() : null;
    }

    /**
     * Returns models for all this enum's attributes
     */
    public List<EnumAttributeModel> getAttributes() {
        return new ArrayList<EnumAttributeModel>(attributeModels.values());
    }

    /**
     * Returns the model for the attribute with the given name or {@code null} if no such attribute
     * exists.
     */
    public EnumAttributeModel getAttribute(String name) {
        return attributeModels.get(name);
    }

    /**
     * Returns the names of all this enum's attributes.
     */
    public List<String> getAttributenames() {
        return attributeNames;
    }

    /**
     * The model for the attribute used to uniquely identify an instance of this enum.
     */
    public EnumAttributeModel getIdAttribute() {
        return findMarkedAttribute("Identifier", new AttributeMatcher() {

            @Override
            public boolean matches(EnumAttributeModel attributeModel) {
                return attributeModel.isIdentifier();
            }
        });
    }

    /**
     * The model for the attribute used to display an instance of this enum in human readable form.
     */
    public EnumAttributeModel getDisplayNameAttribute() {
        return findMarkedAttribute("DisplayName", new AttributeMatcher() {

            @Override
            public boolean matches(EnumAttributeModel attributeModel) {
                return attributeModel.isDisplayName();
            }
        });
    }

    /**
     * finds the first attribute matched by the given {@link AttributeMatcher}
     */
    private EnumAttributeModel findMarkedAttribute(String marker, AttributeMatcher matcher) {
        for (EnumAttributeModel attributeModel : attributeModels.values()) {
            if (matcher.matches(attributeModel)) {
                return attributeModel;
            }
        }
        throw new IllegalStateException("No attribute of the enum \"" + getName() + "\" is marked as " + marker);
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return messagesHelper;
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(getName(), KIND_NAME, IpsStringUtils.EMPTY);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        if (isExtensible()) {
            sb.append('[');
            sb.append(getEnumContentQualifiedName());
            sb.append(']');
        }
        sb.append("(");
        boolean first = true;
        for (String attributeName : attributeNames) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(attributeName);
        }
        sb.append(")");
        return sb.toString();
    }

    // @FunctionalInterface
    private static interface AttributeMatcher {
        boolean matches(EnumAttributeModel attributeModel);
    }

}
