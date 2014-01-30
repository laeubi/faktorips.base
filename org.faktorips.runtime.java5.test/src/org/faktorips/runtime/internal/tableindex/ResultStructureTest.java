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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultStructureTest {

    private ResultStructure<Integer> resultStructure;

    @Test
    public void testGetObject() throws Exception {
        resultStructure = new ResultStructure<Integer>();

        SearchStructure<Integer> structure = resultStructure.get(1);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGetObject_Null() throws Exception {
        resultStructure = new ResultStructure<Integer>();
        SearchStructure<Integer> structure = resultStructure.get(null);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGet() throws Exception {
        resultStructure = new ResultStructure<Integer>(123);

        Set<Integer> set = resultStructure.get();

        assertThat(set, hasItem(123));
    }

    @Test
    public void testMerge() throws Exception {
        resultStructure = new ResultStructure<Integer>(321);
        ResultStructure<Integer> resultStructure2 = new ResultStructure<Integer>(123);

        resultStructure.merge(resultStructure2);

        assertThat(resultStructure.get(), hasItem(123));
        assertThat(resultStructure.get(), hasItem(321));
    }

    @Test
    public void testMerge_emptyResults() throws Exception {
        resultStructure = new ResultStructure<Integer>();
        ResultStructure<Integer> resultStructure2 = new ResultStructure<Integer>();

        resultStructure.merge(resultStructure2);

        assertTrue(resultStructure.get().isEmpty());
    }

}