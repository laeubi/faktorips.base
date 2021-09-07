/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the annotated element as generated by Faktor-IPS. This annotation is not automatically
 * generated but must be activated by setting it as additional annotation in the .ipsproject
 * configuration file:
 *
 * <pre>
 * <code>
 * &lt;Property name="additionalAnnotations" value="org.faktorips.runtime.annotation.IpsGenerated"/&gt;
 * </code>
 * </pre>
 *
 * This annotation can be used to exclude generated code from analysis, for example by
 * SonarQube/JaCoCo, as it is retained in compiled classes, unlike
 * {@code javax.annotation.Generated}.
 * 
 * @since 21.12
 */
@Retention(CLASS)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
public @interface IpsGenerated {
    // just a marker annotation
}