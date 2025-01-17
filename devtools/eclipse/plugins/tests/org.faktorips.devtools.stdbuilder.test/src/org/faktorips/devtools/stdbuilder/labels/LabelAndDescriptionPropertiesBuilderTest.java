/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.labels;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Test;

public class LabelAndDescriptionPropertiesBuilderTest {

    @Test
    public void testGetResourceBundleBaseName() {
        IIpsSrcFolderEntry entry = mock(IpsSrcFolderEntry.class);
        when(entry.getUniqueQualifier()).thenReturn("UniqueQualifier");
        when(entry.getBasePackageNameForDerivedJavaClasses()).thenReturn("org.faktorips.test");
        when(entry.getIpsPackageFragmentRootName()).thenReturn("Model-Folder");
        when(entry.getUniqueBasePackageNameForDerivedArtifacts()).thenCallRealMethod();

        LabelAndDescriptionPropertiesBuilder labelAndDesc = new LabelAndDescriptionPropertiesBuilder(
                new StandardBuilderSet());

        assertThat(labelAndDesc.getResourceBundleBaseName(entry),
                is("org.faktorips.test.UniqueQualifier.Model-Folder-label-and-descriptions"));
    }

    @Test
    public void testGetResourceBundleBaseName_EmptyUniqueQualifier() {
        IIpsSrcFolderEntry entry = mock(IpsSrcFolderEntry.class);
        when(entry.getBasePackageNameForDerivedJavaClasses()).thenReturn("org.faktorips.test");
        when(entry.getIpsPackageFragmentRootName()).thenReturn("Model-Folder");
        when(entry.getUniqueBasePackageNameForDerivedArtifacts()).thenCallRealMethod();

        LabelAndDescriptionPropertiesBuilder labelAndDesc = new LabelAndDescriptionPropertiesBuilder(
                new StandardBuilderSet());

        assertThat(labelAndDesc.getResourceBundleBaseName(entry),
                is("org.faktorips.test.Model-Folder-label-and-descriptions"));
    }

}
