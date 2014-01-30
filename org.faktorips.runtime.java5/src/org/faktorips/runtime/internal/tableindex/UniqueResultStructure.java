/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link UniqueResultStructure UniqueResultStructures} are the leaves in the tree of nested
 * {@link SearchStructure SearchStructures}. Each {@link UniqueResultStructure} defines a result of
 * a search. It is designed to hold exactly one value, in contrast to {@link ResultStructure
 * ResultStructures}. Because of this {@link UniqueResultStructure UniqueResultStructures} throw an
 * {@link UnsupportedOperationException} when trying to merge them. The benefit is that setting up a
 * {@link SearchStructure} with ambiguous results provokes an exception, even before it is put to
 * use.
 */
public class UniqueResultStructure<R> extends SearchStructure<R> implements Mergeable<UniqueResultStructure<R>> {

    private final R uniqueResult;

    UniqueResultStructure(R result) {
        if (result == null) {
            throw new NullPointerException("Result value must not be null");
        }
        uniqueResult = result;
    }

    /**
     * Creates a new {@link ResultSet} with the given resultValue as its only result value. The
     * resultValue must not be null.
     */
    public static <R> UniqueResultStructure<R> createWith(R resultValue) {
        return new UniqueResultStructure<R>(resultValue);
    }

    @Override
    public SearchStructure<R> get(Object key) {
        return this;
    }

    @Override
    public Set<R> get() {
        HashSet<R> result = new HashSet<R>();
        result.add(uniqueResult);
        return result;
    }

    public void merge(UniqueResultStructure<R> otherStructure) {
        throw new UnsupportedOperationException("Unique key violation: " + this + " cannot be merged with "
                + otherStructure.uniqueResult + "");
    }

    @Override
    public R getUnique(R defaultValue) {
        return uniqueResult;
    }

    @Override
    public R getUnique() {
        return uniqueResult;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + uniqueResult.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        UniqueResultStructure<R> other = (UniqueResultStructure<R>)obj;
        return other.uniqueResult.equals(uniqueResult);
    }

    @Override
    public String toString() {
        return "UniqueResultStructure [" + uniqueResult + "]";
    }

}