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

package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * AbstractTextGenerator generates Text without using an {@link ILayouter}
 * 
 * <br/>
 * Useful for structuring data like e.g. frame definitions
 * 
 * @author dicker
 * 
 */
public abstract class AbstractTextGenerator implements IGenerator {

    @Override
    public final byte[] generate() {
        if (!Charset.isSupported(CHARSET)) {
            return generateText().getBytes();
        }
        try {
            return generateText().getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            // should never be reached
            return generateText().getBytes();
        }
    }

    /**
     * returns the generated String
     */
    public abstract String generateText();

}