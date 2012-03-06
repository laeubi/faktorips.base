/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BigDecimalDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.datatype.classtypes.LongDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.junit.Test;

public class PersistenceUtilTest {

    @Test
    public void testIsValidTableName() {
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier(null));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier(""));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("    "));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("_notValidBecauseOfTrailingSpace "));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier(" notValidBecauseOfLeadingSpace"));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("spaces in between"));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("2_digit_start"));

        assertTrue(PersistenceUtil.isValidDatabaseIdentifier("_TABLE"));
        assertTrue(PersistenceUtil.isValidDatabaseIdentifier("VALID_TABLE_NAME_2"));
        assertTrue(PersistenceUtil.isValidDatabaseIdentifier("_2_ALSO_VALID"));
    }

    @Test
    public void testValueDatatypeSupport() {
        assertSupportsFor(new BigDecimalDatatype(), false, true, false);
        assertSupportsFor(new BooleanDatatype(), false, false, false);
        assertSupportsFor(new DateDatatype(), false, false, true);
        assertSupportsFor(new DecimalDatatype(), false, true, false);
        assertSupportsFor(new DoubleDatatype(), false, true, false);
        assertSupportsFor(new GregorianCalendarDatatype("g", true), false, false, true);
        assertSupportsFor(new IntegerDatatype(), false, false, false);
        assertSupportsFor(new LongDatatype(), false, false, false);
        assertSupportsFor(new MoneyDatatype(), false, true, false);
        assertSupportsFor(new StringDatatype(), true, false, false);
    }

    private void assertSupportsFor(ValueDatatype valueDatatype,
            boolean lengthSupport,
            boolean decinalPlacesSupport,
            boolean temporalTypeSupport) {

        assertEquals(lengthSupport, PersistenceUtil.isSupportingLenght(valueDatatype));
        assertEquals(decinalPlacesSupport, PersistenceUtil.isSupportingDecimalPlaces(valueDatatype));
        assertEquals(temporalTypeSupport, PersistenceUtil.isSupportingTemporalType(valueDatatype));

    }

}