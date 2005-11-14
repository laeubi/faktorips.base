package org.faktorips.fl;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.DefaultFunctionResolver;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.functions.AbstractFlFunction;


/**
 *
 */
public class DefaultFunctionResolverTest extends TestCase {
    
    private DefaultFunctionResolver resolver;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        resolver = new DefaultFunctionResolver();
    }

    public void testAdd() {
        FlFunction fct = new TestFlFunction("fct1", Datatype.DECIMAL, new Datatype[0]);
        resolver.add(fct);
        assertEquals(1, resolver.getFunctions().length);
        assertSame(fct, resolver.getFunctions()[0]);
    }

    public void testRemove() {
        TestFlFunction fct1 = new TestFlFunction("fct1", Datatype.DECIMAL, new Datatype[0]); 
        resolver.add(fct1);
        resolver.remove(fct1);
        assertEquals(0, resolver.getFunctions().length);
        
        resolver.remove(fct1); // should do nothing
    }


    static class TestFlFunction extends AbstractFlFunction {
        
        // result to be returned.
        private CompilationResult result;
        
        TestFlFunction(String name, Datatype type, Datatype[] argTypes) {
            super(name, "", type, argTypes);
        }
        
        public CompilationResult compile(CompilationResult[] argResults) {
            return result;
        }
    }
}
