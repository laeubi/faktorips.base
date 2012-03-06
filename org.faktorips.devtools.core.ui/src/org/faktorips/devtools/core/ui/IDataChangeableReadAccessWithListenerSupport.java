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

package org.faktorips.devtools.core.ui;

/**
 * Extension of the SwitchDataChangeableSupport with additional listener support.
 * 
 * @author Jan Ortmann
 */
public interface IDataChangeableReadAccessWithListenerSupport extends IDataChangeableReadAccess {

    /**
     * Adds the listener.
     */
    public void addDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

    /**
     * Removes the listener.
     */
    public void removeDataChangeableStateChangeListener(IDataChangeableStateChangeListener listener);

}