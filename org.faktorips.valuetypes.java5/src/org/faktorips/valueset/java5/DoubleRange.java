/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.valueset.java5;


/**
 * A Range class where upper and lower bounds are Doubles.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class DoubleRange extends DefaultRange<Double>{

    public DoubleRange(Double lower, Double upper, boolean containsNull) {
        super(lower, upper, containsNull);
    }

    public DoubleRange(Double lower, Double upper) {
        this(lower, upper, false);
    }

    private static final long serialVersionUID = 852049468927747406L;

    /**
     * Creates an DoubleRange based on the indicated Strings. The Strings are parsed with the
     * Double.valueOf() method. An asterisk (*) is interpreted as the maximum/minimum available Double
     * value.
     * @param containsNull defines if null is part of the range or not
     */
    public static DoubleRange valueOf(String lower, String upper, boolean containsNull) {
        Double min = null;
        if (lower != null) {
            if (!lower.equals("*")) {
                min = Double.valueOf(lower);
            }
        }

        Double max = null;
        if (upper != null) {
            if (!upper.equals("*")) {
                max = Double.valueOf(upper);
            }
        }
        return new DoubleRange(min, max, containsNull);
    }
}
