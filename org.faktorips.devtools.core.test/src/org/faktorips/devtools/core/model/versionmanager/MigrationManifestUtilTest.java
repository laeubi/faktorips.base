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

package org.faktorips.devtools.core.model.versionmanager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.faktorips.devtools.core.model.versionmanager.MigrationManifestUtil.ManifestFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

@RunWith(MockitoJUnitRunner.class)
public class MigrationManifestUtilTest {

    @Mock
    private Manifest manifest;

    @Mock
    private Attributes attributes;

    @Mock
    private IFile file;

    @Mock
    private ManifestFactory manifestFactory;

    private static final String MY_REQUIRE_BUNDLE = "my.Require.Bundle";
    private static final String MY_REQUIRE_BUNDLE_VERSION = MY_REQUIRE_BUNDLE + ";"
            + Constants.BUNDLE_VERSION_ATTRIBUTE;

    private static final String MY_REQUIRE_BUNDLE_WITH_VERSION = MY_REQUIRE_BUNDLE_VERSION + "=\"[3.9.0,3.10.0)\"";
    private static final String OTHER_BUNDLE1_WITH_VERSION = "other.Require.Bundle1" + ";"
            + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"3.4.0\"";

    private static final String OTHER_BUNDLE2_WITH_VERSION = "other.Require.Bundle2" + ";"
            + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"3.4.0\"";
    private static final String START_OTHER_BUNDLE = OTHER_BUNDLE1_WITH_VERSION + ",";
    private static final String END_OTHER_BUNDLE = "," + OTHER_BUNDLE2_WITH_VERSION;
    private static final String VISIBILITY_REEXPORT = ";visibility:=reexport";

    private static final Version VERSION_3_10 = new Version("3.10.0");
    private static final Version VERSION_3_11 = new Version("3.11.0");
    private static final VersionRange RANGE1 = new VersionRange(VERSION_3_10, true, VERSION_3_11, true);
    private static final VersionRange RANGE2 = new VersionRange(VERSION_3_10, true, VERSION_3_11, false);

    @Before
    public void mockManifest() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(manifest.getMainAttributes()).thenReturn(attributes);
        when(manifestFactory.loadManifest(file)).thenReturn(manifest);
    }

    @Test(expected = NullPointerException.class)
    public void testManifestNull() throws IOException {
        new MigrationManifestUtil(null, manifestFactory);
    }

    @Test(expected = NullPointerException.class)
    public void testSetPluginDependencyPluginNull() throws IOException {
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(null, RANGE1);
    }

    @Test(expected = NullPointerException.class)
    public void testSetPluginDependencyVersionRangeNull() throws IOException {
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, null);
    }

    @Test
    public void testSetPluginDependencyIncludeMaxVersion() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(MY_REQUIRE_BUNDLE_WITH_VERSION);
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE1);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE, MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0]\"");
    }

    @Test
    public void testSetPluginDependencyExcludeMaxVersion() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(
                MY_REQUIRE_BUNDLE_WITH_VERSION + VISIBILITY_REEXPORT);
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE,
                MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"" + VISIBILITY_REEXPORT);
    }

    @Test
    public void testSetPluginDependencyInside() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(
                START_OTHER_BUNDLE + MY_REQUIRE_BUNDLE_WITH_VERSION + END_OTHER_BUNDLE);
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE,
                START_OTHER_BUNDLE + MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"" + END_OTHER_BUNDLE);
    }

    @Test
    public void testSetPluginDependencyNotfound() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(OTHER_BUNDLE1_WITH_VERSION);
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE,
                START_OTHER_BUNDLE + MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"");
    }

    @Test
    public void testSetPluginDependencyEmpty() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(null);
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE, MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"");
    }

    @Test
    public void testWriteManifest() throws IOException, CoreException {
        MigrationManifestUtil migrationUtil = createMigrationManifestUtil();
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(MY_REQUIRE_BUNDLE_WITH_VERSION);
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE1);
        migrationUtil.writeManifest();
        verify(manifest).write(any(ByteArrayOutputStream.class));
        verify(file).setContents(any(ByteArrayInputStream.class), eq(true), eq(true), any(NullProgressMonitor.class));
    }

    private MigrationManifestUtil createMigrationManifestUtil() throws IOException {
        return new MigrationManifestUtil(file, manifestFactory);
    }
}