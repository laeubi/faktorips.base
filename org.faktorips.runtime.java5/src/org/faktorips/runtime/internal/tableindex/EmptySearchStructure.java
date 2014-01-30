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

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A {@link SearchStructure} containing no values. Represents a "nothing found"-result of a search.
 */
class EmptySearchStructure<R> extends SearchStructure<R> {

    @Override
    public SearchStructure<R> get(Object key) {
        return this;
    }

    @Override
    public Set<R> get() {
        return Collections.emptySet();
    }

    @Override
    public R getUnique() {
        throw new NoSuchElementException();
    }

    @Override
    public R getUnique(R defaultValue) {
        return defaultValue;
    }
}