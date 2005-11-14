package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the multiplication of two decimals. 
 */
public class MultiplyDecimalDecimal extends AbstractBinaryOperation {

    public MultiplyDecimalDecimal() {
        super("*", Datatype.DECIMAL, Datatype.DECIMAL);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(".multiply(");
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        return lhs;
    }

}
