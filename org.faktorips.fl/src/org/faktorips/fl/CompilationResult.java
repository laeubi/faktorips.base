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

package org.faktorips.fl;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;


/**
 * The result of a compilation. The result consists of a list of messages generated
 * during the compilation process. If no error has occured (and thus none of
 * the messages is an error message) the result contains the Java sourcecode
 * that represents the compiled expression along with the expression's datatype.  
 */
public interface CompilationResult {
    
    /**
     * Returns the generated Java sourcecode.
     */
    public JavaCodeFragment getCodeFragment();

    /**
     * Returns the compiled expression's datatype.
     */
    public Datatype getDatatype();

    /**
     * Returns the messages generated during compilation. 
     */
    public MessageList getMessages();
    
    /**
     * Returns true if the compilation was successfull, otherwise false.
     */
    public boolean successfull();
    
    /**
     * Returns true if the compilation has failed, otherwise false.
     * If the method returns true, there is a least one error message in the message
     * list.   
     */
    public boolean failed();
}