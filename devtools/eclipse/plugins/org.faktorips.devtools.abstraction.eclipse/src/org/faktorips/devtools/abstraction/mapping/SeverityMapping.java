/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.mapping;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.runtime.Severity;

/**
 * Mapping between Eclipse {@link IStatus} severity constants and {@link Severity} instances.
 */
public class SeverityMapping {

    private SeverityMapping() {
        // util
    }

    /**
     * Maps the given Eclipse {@link IStatus} severity constants to a Faktor-IPS {@link Severity}
     * instance.
     */
    public static Severity toIps(int eclipseSeverity) {
        switch (eclipseSeverity) {
            case IStatus.INFO:
                return Severity.INFO;
            case IStatus.WARNING:
                return Severity.WARNING;
            case IStatus.ERROR:
                return Severity.ERROR;
            default:
                return Severity.NONE;
        }
    }

}
