/**
 * 
 */
package org.faktorips.devtools.core.internal.migration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Empty Migration 
 * 
 * @author Peter Erzberger
 */
public class Migration_2_0_1 extends AbstractIpsProjectMigrationOperation {

    public Migration_2_0_1(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "2.0.2"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        return new MessageList();
    }
}