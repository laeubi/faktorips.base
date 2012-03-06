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

package org.faktorips.devtools.tableconversion.excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class MoneyValueConverterTest {

    @Test
    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        MoneyValueConverter converter = new MoneyValueConverter();
        String value = converter.getIpsValue("1EUR", ml);
        assertTrue(Datatype.MONEY.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("1.2 EUR", ml);
        assertTrue(Datatype.MONEY.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("0", ml);
        assertFalse(ml.isEmpty());
        assertEquals("0", value);

        ml.clear();
        value = converter.getIpsValue(new Double(Double.MAX_VALUE), ml);
        assertFalse(ml.isEmpty());
        assertEquals(new Double(Double.MAX_VALUE).toString(), value);
    }

    @Test
    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        MoneyValueConverter converter = new MoneyValueConverter();
        final String VALID = "12.34EUR";
        final String INVALID = "invalid";

        assertTrue(Datatype.MONEY.isParsable(VALID));
        assertFalse(Datatype.MONEY.isParsable(INVALID));

        Object value = converter.getExternalDataValue(VALID, ml);
        assertEquals(VALID, value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(INVALID, ml);
        assertFalse(ml.isEmpty());
        assertEquals(INVALID, value);
    }

}