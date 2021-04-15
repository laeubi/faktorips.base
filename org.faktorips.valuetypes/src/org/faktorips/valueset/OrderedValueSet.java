/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the {@link ValueSet} interface for ordered values.
 */
public class OrderedValueSet<E> implements ValueSet<E>, Iterable<E> {

    private static final long serialVersionUID = 1735375680693336950L;

    private boolean containsNull;
    private E nullValue;
    private final LinkedHashSet<E> set = new LinkedHashSet<>();

    private int hashCode;

    /**
     * Creates a new instance of {@link OrderedValueSet}.
     * 
     * @param values the values of this set. If these values contain {@code null} or the
     *            {@code null} representation value the parameter {@code containsNull} must be set
     *            to {@code true}.
     * 
     * @param containsNull indicates whether the provided values contain {@code null} or the
     *            {@code null} representation value
     * @param nullValue the java {@code null} value or {@code null} representation value for the
     *            datatype of this enumeration value set
     * @throws IllegalArgumentException if the values array contains duplicate entries
     */
    @SafeVarargs
    public OrderedValueSet(boolean containsNull, E nullValue, E... values) {
        if (values != null) {
            for (E e : values) {
                if (set.contains(e)) {
                    throw new IllegalArgumentException("The provided values array contains duplicate entries.");
                }
                set.add(e);
            }
        }
        initialize(containsNull, nullValue);
    }

    /**
     * Creates a new instance of {@link OrderedValueSet}.
     * 
     * @param values the values of this set. If these values contain {@code null} or the
     *            {@code null} representation value the parameter {@code containsNull} must be set
     *            to {@code true}. If {@code values} is {@code null} the created set does not
     *            contain any values.
     * 
     * @param containsNull indicates whether the provided values contain {@code null} or the
     *            {@code null} representation value
     * @param nullValue the java {@code null} value or {@code null} representation value for the
     *            datatype of this value set
     * @throws IllegalArgumentException if the value collection contains duplicate entries
     */
    public OrderedValueSet(Collection<E> values, boolean containsNull, E nullValue) {
        if (values != null) {
            for (E e : values) {
                if (set.contains(e)) {
                    throw new IllegalArgumentException("The provided values Collection contains duplicate entries.");
                }
                set.add(e);
            }
        }
        initialize(containsNull, nullValue);
    }

    /**
     * Creates a new instance of {@link OrderedValueSet}.
     * 
     * @param values the values of this set. If these values contain {@code null}
     *            {@link #containsNull()} will return {@code true}. If {@code values} is
     *            {@code null} the created set does not contain any values.
     * 
     * @throws IllegalArgumentException if the value collection contains duplicate entries
     */
    public OrderedValueSet(Collection<E> values) {
        if (values != null) {
            for (E e : values) {
                if (set.contains(e)) {
                    throw new IllegalArgumentException("The provided values Collection contains duplicate entries.");
                }
                if (e == null) {
                    containsNull = true;
                    nullValue = null;
                }
                set.add(e);
            }
        }
        initialize(containsNull, nullValue);
    }

    /**
     * Returns a new instance of {@link OrderedValueSet} with an empty set.
     */
    public static <E> OrderedValueSet<E> empty() {
        return new OrderedValueSet<>(null);
    }

    /**
     * Returns a new instance of {@link OrderedValueSet} containing the passed values.
     *
     * @param values the values of this set. If these values contain {@code null}
     *            {@link #containsNull()} will return {@code true}. If {@code values} is
     *            {@code null} the created set does not contain any values.
     * 
     * @throws IllegalArgumentException if the values Collection contains duplicate entries
     */
    public static <E> OrderedValueSet<E> of(Collection<E> values) {
        return new OrderedValueSet<>(values);
    }

    /**
     * Returns a new instance of {@link OrderedValueSet} containing the passed values.
     *
     * @param values the values of this set. If these values contain {@code null}
     *            {@link #containsNull()} will return {@code true}.
     * 
     * @throws IllegalArgumentException if the values Collection contains duplicate entries
     */
    @SafeVarargs
    public static <E> OrderedValueSet<E> of(E... values) {
        return of(Arrays.asList(values));
    }

    private void initialize(boolean containsNull, E nullValue) {
        this.containsNull = containsNull;
        this.nullValue = nullValue;
        if (containsNull && !set.contains(nullValue)) {
            set.add(nullValue);
        }
        calculateHashCode();
    }

    private void calculateHashCode() {
        int result = 17;
        for (E item : set) {
            if (item != null) {
                result = result * 37 + item.hashCode();
            }
        }
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Returns all values of this set except a possible {@code null} or {@code null} representation
     * value.
     */
    @SuppressWarnings("unchecked")
    private Set<E> getValuesWithoutNull() {
        if (containsNull) {
            Set<E> set2 = (Set<E>)set.clone();
            set2.remove(nullValue);
            return Collections.unmodifiableSet(set2);
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<E> getValues(boolean excludeNull) {
        return excludeNull ? getValuesWithoutNull() : getValues();
    }

    /**
     * Returns all values of this set including a possible {@code null} or {@code null}
     * representation value.
     */
    public Set<E> getValues() {
        return Collections.unmodifiableSet(set);
    }

    @Override
    public final boolean isDiscrete() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj instanceof OrderedValueSet) {
            OrderedValueSet<? extends E> other = (OrderedValueSet<? extends E>)obj;
            return set.equals(other.set) && containsNull == other.containsNull
                    && (containsNull
                            ? ((null == nullValue && null == other.nullValue) || (nullValue.equals(other.nullValue)))
                            : true);
        }
        return false;
    }

    @Override
    public boolean containsNull() {
        return containsNull;
    }

    @Override
    public String toString() {
        return set.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public boolean contains(E value) {
        return set.contains(value);
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean isRange() {
        return false;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    public Stream<E> stream() {
        return set.stream();
    }

}
