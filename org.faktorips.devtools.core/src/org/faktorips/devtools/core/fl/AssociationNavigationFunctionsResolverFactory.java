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

package org.faktorips.devtools.core.fl;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.fl.AssociationNavigationFunctionsResolver;
import org.faktorips.fl.FunctionResolver;

/**
 * A {@link IFunctionResolverFactory function resolver factory} that creates a function resolver for
 * a set of functions for association navigation.
 */
public class AssociationNavigationFunctionsResolverFactory implements IFunctionResolverFactory<JavaCodeFragment> {

    /**
     * Returns a function resolver factory that creates a function resolver for a set of functions
     * for association navigation.
     */
    @Override
    public FunctionResolver<JavaCodeFragment> newFunctionResolver(Locale locale) {
        return new AssociationNavigationFunctionsResolver(locale);
    }

}