/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class IpsModelExtensionsViaEclipsePlugins extends IpsModelExtensionsViaExtensionPoints {

    private static /* final */ IpsModelExtensionsViaEclipsePlugins instance = new IpsModelExtensionsViaEclipsePlugins();

    private IpsModelExtensionsViaEclipsePlugins() {
        super(Platform.getExtensionRegistry());
    }

    /**
     * <em><strong>For testing with a custom {@link IExtensionRegistry} only.</strong></em>
     *
     * @see IpsModelExtensionsViaEclipsePlugins#get IpsModelExtensionsViaEclipsePlugins#get for the
     *          singleton instance initialized from the Eclipse {@link Platform}.
     */
    protected IpsModelExtensionsViaEclipsePlugins(IExtensionRegistry extensionRegistry) {
        super(extensionRegistry);
    }

    /**
     * Returns the singleton instance initialized from the Eclipse {@link Platform}.
     */
    public static IpsModelExtensionsViaEclipsePlugins get() {
        return instance;
    }

    /**
     * <em><strong>For testing with a custom {@link IExtensionRegistry} only.</strong></em>
     * 
     * @param testInstance an IpsModelExtensionsViaEclipsePlugins with test data
     */
    protected static void setInstanceForTest(IpsModelExtensionsViaEclipsePlugins testInstance) {
        instance = testInstance;
    }

}
