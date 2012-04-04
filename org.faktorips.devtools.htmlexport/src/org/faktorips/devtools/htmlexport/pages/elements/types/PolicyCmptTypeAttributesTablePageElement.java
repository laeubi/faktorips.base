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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

public class PolicyCmptTypeAttributesTablePageElement extends AttributesTablePageElement {

    public PolicyCmptTypeAttributesTablePageElement(IPolicyCmptType type, DocumentationContext context) {
        this(type, type.getAttributes(), context);
    }

    public PolicyCmptTypeAttributesTablePageElement(IPolicyCmptType type, List<IAttribute> attributes,
            DocumentationContext context) {
        super(type, attributes, context);
    }

    @Override
    protected List<String> getAttributeData(IAttribute attribute) {
        List<String> attributeData = super.getAttributeData(attribute);

        PolicyCmptTypeAttribute polAttribute = (PolicyCmptTypeAttribute)attribute;

        attributeData.add(polAttribute.isProductRelevant() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        attributeData.add(polAttribute.getAttributeType().getName());
        attributeData.add(polAttribute.isOverwrite() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

        return attributeData;
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(IAttribute attribute) {
        List<IPageElement> rowWithIpsObjectPart = super.createRowWithIpsObjectPart(attribute);

        if (!getContext().showInheritedObjectPartsInTable()) {
            return rowWithIpsObjectPart;
        }

        List<IPageElement> rows = new ArrayList<IPageElement>(rowWithIpsObjectPart);
        rows.add(createPageElementForDefiningSuperType(attribute));
        return rows;
    }

    private IPageElement createPageElementForDefiningSuperType(IAttribute attribute) {
        IPageElement pageElement;
        PolicyCmptTypeAttribute polAttribute = (PolicyCmptTypeAttribute)attribute;
        IPolicyCmptType attributeDefiningPolicyCmptType = polAttribute.getPolicyCmptType();

        if (attributeDefiningPolicyCmptType.equals(getType())) {
            pageElement = new TextPageElement("-"); //$NON-NLS-1$
        } else {
            pageElement = new PageElementUtils().createLinkPageElement(getContext(), attributeDefiningPolicyCmptType,
                    TargetType.CONTENT, attributeDefiningPolicyCmptType.getName(), true);
        }
        return pageElement;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = super.getHeadlineWithIpsObjectPart();

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_productRelevant),
                Style.CENTER);

        headline.add(getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_attributeType));

        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_overwrite), Style.CENTER);

        if (getContext().showInheritedObjectPartsInTable()) {
            headline.add(getContext().getMessage(HtmlExportMessages.PolicyCmptTypeContentPageElement_inheritedFrom));
        }

        return headline;
    }
}