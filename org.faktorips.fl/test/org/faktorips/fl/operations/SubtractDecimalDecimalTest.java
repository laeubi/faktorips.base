/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilerAbstractTest;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.values.Decimal;



/**
 *
 */
public class SubtractDecimalDecimalTest extends CompilerAbstractTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[]{new SubtractDecimalDecimal()});
    }
    
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("10.123 - 8.1", Decimal.valueOf("2.023"), Datatype.DECIMAL);
        execAndTestSuccessfull("8.1 - 10.123", Decimal.valueOf("-2.023"), Datatype.DECIMAL);
    }
    
    public void testLhsError() throws Exception {
        execAndTestFail("a a - 8.1", ExprCompiler.SYNTAX_ERROR);
    }
    
    public void testRhsError() throws Exception {
        execAndTestFail("8.1 - a a", ExprCompiler.SYNTAX_ERROR);
    }

}
