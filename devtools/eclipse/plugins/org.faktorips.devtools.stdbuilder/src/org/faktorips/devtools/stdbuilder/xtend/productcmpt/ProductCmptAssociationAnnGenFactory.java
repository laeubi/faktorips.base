/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xtend.association.AssociationWithCardinalityAnnGen;
import org.faktorips.devtools.stdbuilder.xtend.association.SimpleAssociationAnnGen;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;

public class ProductCmptAssociationAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        switch (type) {
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER:
                return new ProductCmptAssociationAnnGen();
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_LINKS:
                return new SimpleAssociationAnnGen(XProductAssociation.class, IpsAssociationLinks.class);
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER:
                return new SimpleAssociationAnnGen(XProductAssociation.class, IpsAssociationAdder.class);
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_WITH_CARDINALITY_SETTER_ADDER:
                return new AssociationWithCardinalityAnnGen(XProductAssociation.class, IpsAssociationAdder.class);
            case PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_REMOVER:
                return new SimpleAssociationAnnGen(XProductAssociation.class, IpsAssociationRemover.class);
            default:
                return null;
        }
    }

}
