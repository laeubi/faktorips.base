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

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;

/**
 * Operation for the multiplication of two decimals.
 */
public class MultiplyDecimalDecimal extends AbstractBinaryOperation {

    public MultiplyDecimalDecimal() {
        super("*", Datatype.DECIMAL, Datatype.DECIMAL); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *      org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(".multiply("); //$NON-NLS-1$
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        return lhs;
    }

}