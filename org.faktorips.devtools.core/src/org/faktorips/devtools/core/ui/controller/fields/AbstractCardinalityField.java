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

package org.faktorips.devtools.core.ui.controller.fields;

import org.apache.commons.lang.StringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * Base-class to represent and edit cardinality values (which means int-values
 * and the asterisk (*)). The askerisk is mapped to Integer.MAX_VALUE on object
 * conversions and vice versa.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractCardinalityField extends DefaultEditField {
	
	/**
	 * {@inheritDoc}
	 */
	public Object getValue() {
		String text = getText();
        
		if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("Can't return an Integer, field is empty."); //$NON-NLS-1$
        }

        Integer retValue = null;
		if (text.equals("*")) { //$NON-NLS-1$
			retValue = new Integer(Integer.MAX_VALUE);
		}
		else {
			retValue = Integer.valueOf(text);
		}

		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(Object newValue) {
        ArgumentCheck.isInstanceOf(newValue, Integer.class);
		if (newValue instanceof Integer) {
			Integer value = (Integer)newValue;
			if (value.intValue() == Integer.MAX_VALUE) {
				setText("*"); //$NON-NLS-1$
			}
			else {
				setText(value.toString());
			}
		}
	}

	/**
	 * Method to set the text unmodified to the underlying control
	 */
	abstract void setTextInternal(String newText);
	
	/**
	 * {@inheritDoc}
	 */
	public void setText(String newText) {
		try {
			Integer value = Integer.valueOf(newText);
			if (value.intValue() == Integer.MAX_VALUE) {
				setTextInternal("*"); //$NON-NLS-1$
			}
			else {
				setTextInternal(value.toString());
			}
		} catch (NumberFormatException e) {
			setTextInternal(newText);
		}		
	}
}
