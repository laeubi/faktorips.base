/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.IProductAssociationModel;

public class ProductAssociationModel extends ModelTypeAssociation implements IProductAssociationModel {

    private boolean changingOverTime;

    public ProductAssociationModel(ModelType modelType, Method getterMethod, boolean changingOverTime) {
        super(modelType, getterMethod);
        this.changingOverTime = changingOverTime;
    }

    @Override
    public ProductAssociationModel createOverwritingAssociationFor(ModelType subModelType) {
        return new ProductAssociationModel(subModelType, getGetterMethod(), changingOverTime);
    }

    @Override
    public ProductModel getModelType() {
        return (ProductModel)super.getModelType();
    }

    @Override
    public IProductModel getTarget() {
        return (IProductModel)super.getTarget();
    }

    @Override
    public List<IProductComponent> getTargetObjects(IProductComponent productComponentSource, Calendar effectiveDate) {
        List<IProductComponent> targets = new ArrayList<IProductComponent>();
        Object source = getRelevantProductObject(productComponentSource, effectiveDate, isChangingOverTime());
        Object returnValue = invokeMethod(getGetterMethod(), source);
        if (returnValue instanceof Iterable<?>) {
            for (Object target : (Iterable<?>)returnValue) {
                targets.add((IProductComponent)target);
            }
        } else if (returnValue instanceof IProductComponent) {
            targets.add((IProductComponent)returnValue);
        }
        return targets;
    }

    @Override
    public IPolicyModel getMatchingAssociationSourceType() {
        return (IPolicyModel)super.getMatchingAssociationSourceType();
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }
}