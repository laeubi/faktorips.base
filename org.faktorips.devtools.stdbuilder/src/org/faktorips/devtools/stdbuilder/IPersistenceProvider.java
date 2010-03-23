/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;

/**
 * Allows to specify several vendor specific JPA annotations.
 * 
 * @author Joerg Ortmann
 */
public interface IPersistenceProvider {

    /**
     * Returns <code>true</code> if the persistent provider supports orphan removal (private owned)
     * annotation.
     */
    public boolean isSuppotingOrphanRemoval();

    /**
     * Returns <code>true</code> if the persistent provider supports the join fetch type annotation.
     */
    public boolean isSupportingJoinFetchType();

    /**
     * Returns <code>true</code> if the persistent provider supports converter.
     */
    public boolean isSupportingConverter();

    /**
     * If orphan removal is supported then this method must be used to add the necessary annotation
     * to the given java code fragment.
     */
    public void addAnnotationOrphanRemoval(JavaCodeFragment javaCodeFragment);

    /**
     * If the join fetch type is supported then this method must be used to add the necessary
     * annotation to the given java code fragment.
     */
    public void addAnnotationJoinFetchType(JavaCodeFragment javaCodeFragment, String joinFetchTypeValue);

    /**
     * If converter are supported then this method must be used to add the necessary annotation to
     * the given java code fragment.
     */
    public void addAnnotationConverter(JavaCodeFragment javaCodeFragment,
            IPersistentAttributeInfo persistentAttributeInfo);
}
