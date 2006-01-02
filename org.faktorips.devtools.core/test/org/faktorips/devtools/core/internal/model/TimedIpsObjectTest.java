package org.faktorips.devtools.core.internal.model;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class TimedIpsObjectTest extends IpsObjectTestCase {
    
    private ITimedIpsObject timedObject;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.PRODUCT_CMPT);
    }
    
    protected void createObjectAndPart() {
        // we use the ProductCmptImpl to test the TimedIpsObject class
        // because TimedIpsObject is abstract.
        timedObject = new ProductCmpt(pdSrcFile);
    }
    
    public void testGetChildren() throws CoreException {
        assertEquals(0, timedObject.getChildren().length);
        IIpsObjectGeneration gen = timedObject.newGeneration();
        assertEquals(1, timedObject.getChildren().length);
        assertEquals(gen, timedObject.getChildren()[0]);
        
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        assertEquals(2, timedObject.getChildren().length);
        assertEquals(gen, timedObject.getChildren()[0]);
        assertEquals(gen2, timedObject.getChildren()[1]);
    }

    public void testChangesOn() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        assertFalse(timedObject.changesOn(date));
        timedObject.newGeneration().setValidFrom(new GregorianCalendar(2004, 0, 1));
        assertFalse(timedObject.changesOn(date));
        timedObject.newGeneration().setValidFrom(new GregorianCalendar(2005, 0, 1));
        assertTrue(timedObject.changesOn(date));
    }

    public void testGetGenerations() {
        IIpsObjectGeneration[] generations = timedObject.getGenerations();
        assertEquals(0, generations.length);
        
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 1, 1));
        generations = timedObject.getGenerations();
        assertEquals(1, generations.length);
        assertEquals(gen1, generations[0]);
        
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 1, 1));
        generations = timedObject.getGenerations();
        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);
        
        // change gen2 valid from date, so that now gen2 should come first
        gen2.setValidFrom(new GregorianCalendar(2003, 1, 1));
        generations = timedObject.getGenerations();
        assertEquals(2, generations.length);
        assertEquals(gen2, generations[0]);
        assertEquals(gen1, generations[1]);
        
        // gen2 has valid from date null => is should come last
        gen2.setValidFrom(null);
        generations = timedObject.getGenerations();
        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);
        
        // now also gen1 has valid from date null
        gen1.setValidFrom(null);
        generations = timedObject.getGenerations();
        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);
        
    }

    public void testFindGenerationEffectiveOn() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));
        
        IIpsObjectGeneration genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2004, 0, 1));
        assertEquals(gen1, genFound);
        
        genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2004, 3, 1));
        assertEquals(gen1, genFound);
        
        genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2005, 0, 1));
        assertEquals(gen2, genFound);
        
        genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2003, 0, 1));
        assertNull(genFound);
        
        genFound = timedObject.findGenerationEffectiveOn(null);
        assertNull(genFound);
    }

    public void testGetGenerationByEffectiveDate() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));
        
        IIpsObjectGeneration genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 0, 1));
        assertEquals(gen1, genFound);
        
        genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 3, 1));
        assertNull(genFound);
        
        genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2005, 0, 1));
        assertEquals(gen2, genFound);
        
        genFound = timedObject.getGenerationByEffectiveDate(null);
        assertNull(genFound);
        
    }

    public void testNewGeneration() {
        IIpsObjectGeneration gen = timedObject.newGeneration();
        assertEquals(timedObject, gen.getParent());
        assertEquals(1, timedObject.getNumOfGenerations());
        assertEquals(gen, timedObject.getGenerations()[0]);
    }

    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        timedObject.initFromXml((Element)doc.getDocumentElement());
        assertEquals(2, timedObject.getNumOfGenerations());
    }

    public void testToXml() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2005, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2006, 0, 1));
        
        Element element = timedObject.toXml(newDocument());
        gen1.delete();
        gen2.delete();
        timedObject.initFromXml(element);
        assertEquals(2, timedObject.getNumOfGenerations());
    }

}
