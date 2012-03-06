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

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.junit.Test;

public class MinMaxDecimalTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxDecimal("MAX", "", true));
        execAndTestSuccessfull("MAX(3.0; 4.0)", Decimal.valueOf("4.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MAX(4.0; 3.0)", Decimal.valueOf("4.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MAX(4; 3)", Decimal.valueOf("4"), Datatype.DECIMAL);

        registerFunction(new MinMaxDecimal("MIN", "", false));
        execAndTestSuccessfull("MIN(3.0; 4.0)", Decimal.valueOf("3.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MIN(4.0; 3.0)", Decimal.valueOf("3.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MIN(4; 3)", Decimal.valueOf("3"), Datatype.DECIMAL);
    }

}