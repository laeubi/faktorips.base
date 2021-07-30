/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.ant;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;

public class MavenProjectImportTask extends AbstractIpsTask {

    private String projectDir;

    public MavenProjectImportTask() {
        super("MavenProjektImportTask");
    }

    /**
     * Sets the Ant attribute which describes the location of the maven project to import.
     * 
     * @param dir Path to the Project as String
     */
    public void setDir(String dir) {
        this.projectDir = dir;
    }

    /**
     * Returns the path of the maven project to import as String
     * 
     * @return Path as String
     */
    public String getDir() {
        return this.projectDir;
    }

    @Override
    protected void executeInternal() throws Exception {
        NullProgressMonitor monitor = new NullProgressMonitor();

        if (new File(getDir(), "pom.xml").exists()) {

            IProjectConfigurationManager projectConfigManager = MavenPlugin.getProjectConfigurationManager();
            LocalProjectScanner scanner = new LocalProjectScanner(
                    ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(),
                    getDir(),
                    false,
                    MavenPlugin.getMavenModelManager());
            scanner.run(monitor);

            Set<MavenProjectInfo> projectSet = projectConfigManager.collectProjects(scanner.getProjects());
            if (projectSet.isEmpty()) {
                System.out.println("No Maven-Projects found in: " + getDir());
                return;
            }

            ProjectImportConfiguration configuration = new ProjectImportConfiguration();
            List<IMavenProjectImportResult> importResults = projectConfigManager.importProjects(
                    projectSet,
                    configuration,
                    monitor);

            for (IMavenProjectImportResult result : importResults) {

                if (result.getProject() != null) {
                    System.out.println("importing: " + result.getProject().getName());
                } else {
                    System.out.println("already in workspace: " + getDir());
                }
            }

        }
    }
}