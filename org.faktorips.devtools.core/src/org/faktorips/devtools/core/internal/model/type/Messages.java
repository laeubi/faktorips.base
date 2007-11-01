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

package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.type.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String Association_msg_MaxCardinalityMustBeAtLeast1;
    public static String Association_msg_MaxCardinalityForDerivedUnionTooLow;
    public static String Association_msg_MinCardinalityGreaterThanMaxCardinality;
    public static String Association_msg_TargetRolePlural;
    public static String Association_msg_TargetRoleSingular;
    public static String Association_msg_TargetRoleSingularIlleaglySameAsTargetRolePlural;
    public static String Association_msg_DerivedUnionDoesNotExist;
    public static String Association_msg_NotMarkedAsDerivedUnion;
    public static String Association_msg_TargetOfDerivedUnionDoesNotExist;
    public static String Association_msg_TargetNotSubclass;
    
	public static String Parameter_msg_NameEmpty;

	public static String Parameter_msg_InvalidParameterName;



}
