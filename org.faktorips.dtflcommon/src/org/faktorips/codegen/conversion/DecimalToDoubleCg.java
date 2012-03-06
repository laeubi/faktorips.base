/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

public class DecimalToDoubleCg extends AbstractSingleConversionCg {

    public DecimalToDoubleCg() {
        super(Datatype.DECIMAL, Datatype.DOUBLE);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // Decimal.valueOf(fromValue)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(fromValue) //
                .append(".doubleValue()"); //$NON-NLS-1$ 
        return fragment;
    }

}