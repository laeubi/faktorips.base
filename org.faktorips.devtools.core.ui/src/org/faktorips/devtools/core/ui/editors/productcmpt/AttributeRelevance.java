/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * The relevance of an attribute can be expressed through its {@linkplain IValueSet value set}. An
 * {@linkplain IValueSet#isEmpty() empty set} is considered {@link #Irrelevant}, as no value is
 * allowed. If the value set contains values, {@link #Mandatory} means a value must be set, so
 * {@link IValueSet#isContainsNull()} must be {@code false}, otherwise the attribute is considered
 * {@link #Optional}.
 * <p>
 * This is a helper class modifying a given {@link IConfiguredValueSet} to match a desired relevance
 * setting.
 */
public enum AttributeRelevance {
    Optional {
        @Override
        public void set(IConfiguredValueSet configuredValueSet) {
            nonEmpty(configuredValueSet).setContainsNull(true);
        }
    },
    Mandatory {
        @Override
        public void set(IConfiguredValueSet configuredValueSet) {
            nonEmpty(configuredValueSet).setContainsNull(false);
        }
    },
    Irrelevant {
        @Override
        public void set(IConfiguredValueSet configuredValueSet) {
            IValueSet valueSet = configuredValueSet.getValueSet();
            if (valueSet.isRange()) {
                ((IRangeValueSet)valueSet).setEmpty(true);
            } else {
                configuredValueSet.changeValueSetType(ValueSetType.ENUM).setContainsNull(false);
            }
        }
    };

    public abstract void set(IConfiguredValueSet configuredValueSet);

    protected IValueSet nonEmpty(IConfiguredValueSet configuredValueSet) {
        if (configuredValueSet.getValueSet().isEmpty()) {
            try {
                IPolicyCmptTypeAttribute attribute = configuredValueSet
                        .findPcTypeAttribute(configuredValueSet.getIpsProject());
                IValueSet parentValueSet = attribute.getValueSet();
                if (parentValueSet.isEnum()) {
                    configuredValueSet.convertValueSetToEnumType();
                    List<String> values = ((IEnumValueSet)parentValueSet).getValuesAsList();
                    ((IEnumValueSet)configuredValueSet.getValueSet()).addValues(values);
                } else {
                    if (configuredValueSet.getValueSet().isRange()) {
                        ((IRangeValueSet)configuredValueSet.getValueSet()).setEmpty(false);
                    } else {
                        configuredValueSet.changeValueSetType(parentValueSet.getValueSetType());
                    }
                    if (parentValueSet.isRange()) {
                        RangeValueSet range = (RangeValueSet)configuredValueSet.getValueSet();
                        range.copyPropertiesFrom(parentValueSet);
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return configuredValueSet.getValueSet();
    }

    public static AttributeRelevance of(IValueSet valueSet) {
        if (valueSet.isEmpty()) {
            return AttributeRelevance.Irrelevant;
        } else {
            return valueSet.isContainsNull() ? AttributeRelevance.Optional : AttributeRelevance.Mandatory;
        }
    }
}