/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.validation;

import java.math.BigDecimal;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.valueset.BigDecimalRange;
import org.faktorips.valueset.DecimalRange;
import org.faktorips.valueset.DefaultRange;
import org.faktorips.valueset.DoubleRange;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.LongRange;
import org.faktorips.valueset.MoneyRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Defines the relevance of a {@link PolicyAttribute}, derived from its
 * {@link PolicyAttribute#getValueSet(IModelObject) value set}:
 * <ul>
 * <li>An attribute with an empty (or {@code null}) value set is considered {@link #IRRELEVANT} - no
 * value should be set</li>
 * <li>An attribute with value set {@link ValueSet#containsNull() containing} {@code null} is
 * considered {@link #OPTIONAL} - a value can be set but is not required</li>
 * <li>An attribute with a non-empty value set not {@link ValueSet#containsNull() containing}
 * {@code null} is considered {@link #MANDATORY} - a value must be set</li>
 * </ul>
 * An attribute that is not {@link #IRRELEVANT} ({@link #OPTIONAL} or {@link #MANDATORY}) is
 * considered relevant - its value should for example be checked against the value set.
 */
public enum Relevance {
    /**
     * An attribute with an empty (or {@code null}) value set - no value should be set.
     */
    IRRELEVANT {

        @Override
        public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
                PolicyAttribute policyAttribute,
                ValueSet<T> parentValueSet) {
            @SuppressWarnings("unchecked")
            Class<T> datatype = (Class<T>)policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();
            if (valueSetKind == ValueSetKind.Range) {
                @SuppressWarnings("unchecked")
                ValueSet<T> range = (ValueSet<T>)createEmptyRange(datatype);
                return range;
            }
            return new OrderedValueSet<>(false, null);
        }
    },
    /**
     * An attribute with value set {@link ValueSet#containsNull() containing} {@code null} - a value
     * can be set but is not required.
     */
    OPTIONAL {

        @Override
        public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
                PolicyAttribute policyAttribute,
                ValueSet<T> parentValueSet) {
            @SuppressWarnings("unchecked")
            Class<T> datatype = (Class<T>)policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();
            return asValueSet(datatype, valueSetKind, true, parentValueSet);
        }
    },
    /**
     * An attribute with a non-empty value set not {@link ValueSet#containsNull() containing}
     * {@code null} - a value must be set.
     */
    MANDATORY {

        @Override
        public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
                PolicyAttribute policyAttribute,
                ValueSet<T> parentValueSet) {
            @SuppressWarnings("unchecked")
            Class<T> datatype = (Class<T>)policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();
            return asValueSet(datatype, valueSetKind, false, parentValueSet);
        }
    };

    private static <T> ValueSet<T> asValueSet(Class<T> datatype,
            ValueSetKind valueSetKind,
            boolean containsNull,
            ValueSet<T> parentValueSet) {
        if (isCompatible(parentValueSet, valueSetKind) && parentValueSet.containsNull() == containsNull) {
            return parentValueSet;
        }
        if (isBoolean(datatype)) {
            return asBooleanValueSet(containsNull, parentValueSet);
        }
        if (isRange(valueSetKind, parentValueSet)) {
            return asRange(datatype, containsNull, parentValueSet);
        }
        if (isEnum(valueSetKind, parentValueSet)) {
            return asEnum(datatype, containsNull, parentValueSet);
        }
        return new UnrestrictedValueSet<>(containsNull);
    }

    private static <T> boolean isEnum(ValueSetKind valueSetKind, ValueSet<T> parentValueSet) {
        return valueSetKind == ValueSetKind.Enum
                || (parentValueSet != null && parentValueSet instanceof OrderedValueSet);
    }

    private static <T> boolean isRange(ValueSetKind valueSetKind, ValueSet<T> parentValueSet) {
        return valueSetKind == ValueSetKind.Range || (parentValueSet != null && parentValueSet.isRange());
    }

    private static <T> boolean isBoolean(Class<T> datatype) {
        return Boolean.class.equals(datatype) || boolean.class.equals(datatype);
    }

    private static <T> ValueSet<T> asEnum(Class<T> datatype, boolean containsNull, ValueSet<T> parentValueSet) {
        if (parentValueSet != null) {
            return new OrderedValueSet<>(parentValueSet.getValues(true), containsNull, nullValue(datatype));
        } else {
            return new OrderedValueSet<>(containsNull, nullValue(datatype), datatype.getEnumConstants());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T nullValue(Class<T> datatype) {
        if (Money.class.isAssignableFrom(datatype)) {
            return (T)Money.NULL;
        }
        if (Decimal.class.isAssignableFrom(datatype)) {
            return (T)Decimal.NULL;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> ValueSet<T> asRange(Class<T> datatype, boolean containsNull, ValueSet<T> parentValueSet) {
        if (parentValueSet != null) {
            return (ValueSet<T>)changeRangeRelevance(parentValueSet, containsNull);
        } else {
            return (ValueSet<T>)createRangeRelevance(datatype, containsNull);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ValueSet<T> asBooleanValueSet(boolean containsNull, ValueSet<T> parentValueSet) {
        if (parentValueSet != null) {
            return new OrderedValueSet<>(parentValueSet.getValues(true), containsNull, null);
        } else {
            return (ValueSet<T>)new OrderedValueSet<Boolean>(containsNull, null, Boolean.TRUE, Boolean.FALSE);
        }
    }

    private static boolean isCompatible(ValueSet<?> parentValueSet, ValueSetKind valueSetKind) {
        if (parentValueSet == null) {
            return false;
        }
        switch (valueSetKind) {
            case AllValues:
                return true;
            case Enum:
                return parentValueSet instanceof OrderedValueSet;
            case Range:
                return parentValueSet.isRange();
            default:
                return false;
        }
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #IRRELEVANT}
     * for the given model object.
     */
    public static boolean isIrrelevant(IModelObject modelObject, String property) {
        return isIrrelevant(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #IRRELEVANT} for the given model
     * object.
     */
    public static boolean isIrrelevant(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return IRRELEVANT == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #MANDATORY}
     * for the given model object.
     */
    public static boolean isMandatory(IModelObject modelObject, String property) {
        return isMandatory(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #MANDATORY} for the given model
     * object.
     */
    public static boolean isMandatory(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return MANDATORY == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #OPTIONAL}
     * for the given model object.
     */
    public static boolean isOptional(IModelObject modelObject, String property) {
        return isOptional(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #OPTIONAL} for the given model
     * object.
     */
    public static boolean isOptional(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return OPTIONAL == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the attribute with the given property name is considered relevant for the
     * given model object.
     */
    public static boolean isRelevant(IModelObject modelObject, String property) {
        return isRelevant(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered relevant for the given model object.
     */
    public static boolean isRelevant(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return !isIrrelevant(modelObject, policyAttribute);
    }

    /**
     * Returns the {@link Relevance} of the {@link PolicyAttribute} identified by the given property
     * name for the given model object.
     */
    public static Relevance of(IModelObject modelObject, String property) {
        return Relevance.of(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns the {@link Relevance} of the given {@link PolicyAttribute} for the given model
     * object.
     */
    public static Relevance of(IModelObject modelObject, PolicyAttribute policyAttribute) {
        ValueSet<?> valueSet = policyAttribute.getValueSet(modelObject);
        if (valueSet == null || valueSet.isEmpty()) {
            return Relevance.IRRELEVANT;
        } else if (valueSet.containsNull()) {
            return Relevance.OPTIONAL;
        } else {
            return Relevance.MANDATORY;
        }
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute that matches this
     * {@link Relevance}.
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return asValueSetFor(modelObject, policyAttribute, null);
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute identified by the given
     * property name that matches this {@link Relevance}.
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject, String property) {
        return this.asValueSetFor(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute that matches this
     * {@link Relevance}. If a parent value set is given, the returned value set will be of the same
     * type (if allowed by the attribute's {@link ValueSetKind}) and contain no values not allowed
     * by that parent value set (with the exception of a null value if the value set is converted to
     * {@link #OPTIONAL}).
     *
     * @param modelObject a model object
     * @param policyAttribute an attribute present on the model object's {@link PolicyCmptType}
     * @param parentValueSet an optional parent {@link ValueSet}, which can limit the allowed values
     *            for the returned value set
     */
    public abstract <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
            PolicyAttribute policyAttribute,
            @CheckForNull ValueSet<T> parentValueSet);

    /**
     * Returns a {@link ValueSet} for the given model object's attribute identified by the given
     * property name that matches this {@link Relevance}. If a parent value set is given, the
     * returned value set will be of the same type (if allowed by the attribute's
     * {@link ValueSetKind}) and contain no values not allowed by that parent value set (with the
     * exception of a null value if the value set is converted to {@link #OPTIONAL}).
     *
     * @param modelObject a model object
     * @param property the name of an attribute present on the model object's {@link PolicyCmptType}
     * @param parentValueSet an optional parent {@link ValueSet}, which can limit the allowed values
     *            for the returned value set
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
            String property,
            @CheckForNull ValueSet<T> parentValueSet) {
        return this.asValueSetFor(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property),
                parentValueSet);
    }

    private static ValueSet<?> changeRangeRelevance(ValueSet<?> valueSet, boolean containsNull) {
        if (valueSet instanceof BigDecimalRange) {
            BigDecimalRange bigDecimalRange = (BigDecimalRange)valueSet;
            return BigDecimalRange.valueOf(bigDecimalRange.getLowerBound(), bigDecimalRange.getUpperBound(),
                    bigDecimalRange.getStep(), containsNull);
        }
        if (valueSet instanceof DecimalRange) {
            DecimalRange decimalRange = (DecimalRange)valueSet;
            return DecimalRange.valueOf(decimalRange.getLowerBound(), decimalRange.getUpperBound(),
                    decimalRange.getStep(), containsNull);
        }
        if (valueSet instanceof DoubleRange) {
            DoubleRange doubleRange = (DoubleRange)valueSet;
            return DoubleRange.valueOf(doubleRange.getLowerBound(), doubleRange.getUpperBound(), doubleRange.getStep(),
                    containsNull);
        }
        if (valueSet instanceof IntegerRange) {
            IntegerRange integerRange = (IntegerRange)valueSet;
            return IntegerRange.valueOf(integerRange.getLowerBound(), integerRange.getUpperBound(),
                    integerRange.getStep(), containsNull);
        }
        if (valueSet instanceof LongRange) {
            LongRange longRange = (LongRange)valueSet;
            return LongRange.valueOf(longRange.getLowerBound(), longRange.getUpperBound(),
                    longRange.getStep(), containsNull);
        }
        if (valueSet instanceof MoneyRange) {
            MoneyRange moneyRange = (MoneyRange)valueSet;
            return MoneyRange.valueOf(moneyRange.getLowerBound(), moneyRange.getUpperBound(),
                    moneyRange.getStep(), containsNull);
        }
        return null;
    }

    private static ValueSet<?> createRangeRelevance(Class<?> datatype, boolean containsNull) {
        if (BigDecimal.class.isAssignableFrom(datatype)) {
            return BigDecimalRange.valueOf((BigDecimal)null, null, null, containsNull);
        }
        if (Decimal.class.isAssignableFrom(datatype)) {
            return DecimalRange.valueOf((Decimal)null, null, null, containsNull);
        }
        if (Double.class.isAssignableFrom(datatype) || Double.TYPE.isAssignableFrom(datatype)) {
            return DoubleRange.valueOf((Double)null, null, null, containsNull);
        }
        if (Integer.class.isAssignableFrom(datatype) || Integer.TYPE.isAssignableFrom(datatype)) {
            return IntegerRange.valueOf((Integer)null, null, null, containsNull);
        }
        if (Long.class.isAssignableFrom(datatype) || Long.TYPE.isAssignableFrom(datatype)) {
            return LongRange.valueOf((Long)null, null, null, containsNull);
        }
        if (Money.class.isAssignableFrom(datatype)) {
            return MoneyRange.valueOf((Money)null, null, null, containsNull);
        }
        return null;
    }

    private static ValueSet<?> createEmptyRange(Class<?> datatype) {
        if (BigDecimal.class.isAssignableFrom(datatype)) {
            return new BigDecimalRange();
        }
        if (Decimal.class.isAssignableFrom(datatype)) {
            return new DecimalRange();
        }
        if (Double.class.isAssignableFrom(datatype) || Double.TYPE.isAssignableFrom(datatype)) {
            return new DoubleRange();
        }
        if (Integer.class.isAssignableFrom(datatype) || Integer.TYPE.isAssignableFrom(datatype)) {
            return new IntegerRange();
        }
        if (Long.class.isAssignableFrom(datatype) || Long.TYPE.isAssignableFrom(datatype)) {
            return new LongRange();
        }
        if (Money.class.isAssignableFrom(datatype)) {
            return new MoneyRange();
        }
        @SuppressWarnings("rawtypes")
        DefaultRange defaultRange = new DefaultRange();
        return defaultRange;
    }

}
