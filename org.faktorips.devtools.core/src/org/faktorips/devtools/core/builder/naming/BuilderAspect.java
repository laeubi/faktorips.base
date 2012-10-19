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

package org.faktorips.devtools.core.builder.naming;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public enum BuilderAspect {

    IMPLEMENTATION {
        @Override
        public String getJavaClassName(IIpsSrcFile ipsSrcFile, IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.getImplClassName(ipsSrcFile);
        }

        @Override
        public boolean isPublishedArtifact(IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.isImplClassPublishedArtifact();
        }
    },

    INTERFACE {
        @Override
        public String getJavaClassName(IIpsSrcFile ipsSrcFile, IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.getInterfaceName(ipsSrcFile);
        }

        @Override
        public boolean isPublishedArtifact(IJavaClassNameProvider javaClassNameProvider) {
            return javaClassNameProvider.isInterfacePublishedArtifact();
        }
    };

    public abstract String getJavaClassName(IIpsSrcFile ipsSrcFile, IJavaClassNameProvider javaClassNameProvider);

    public abstract boolean isPublishedArtifact(IJavaClassNameProvider javaClassNameProvider);

    public static BuilderAspect getValue(boolean isInterface) {
        if (isInterface) {
            return INTERFACE;
        } else {
            return IMPLEMENTATION;
        }
    }
}