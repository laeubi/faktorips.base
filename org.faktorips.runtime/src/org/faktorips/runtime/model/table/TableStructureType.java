/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.table;

/**
 * Runtime mirror for table structure types.
 */
public enum TableStructureType {
    /**
     * Single content - for this table structure only on table content is allowed.
     */
    SINGLE_CONTENT,
    /**
     * Multiple contents - for this table structure one or more table contents are allowed.
     */
    MULTIPLE_CONTENTS;
}
