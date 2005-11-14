package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the addition of two decimals. 
 */
public class AddDecimalInteger extends AbstractBinaryOperation {

    public AddDecimalInteger() {
        super("+", Datatype.DECIMAL, Datatype.INTEGER);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(".add(");
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        return lhs;
    }

}
