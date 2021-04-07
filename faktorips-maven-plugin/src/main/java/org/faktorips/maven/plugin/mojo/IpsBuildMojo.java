/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.EquinoxInstallationFactory;
import org.eclipse.sisu.equinox.launching.EquinoxLauncher;
import org.eclipse.tycho.core.maven.ToolchainProvider;
import org.faktorips.maven.plugin.mojo.internal.EclipseRunMojo;
import org.faktorips.maven.plugin.mojo.internal.Repository;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Builds the Faktor-IPS project.
 * <p>
 * By default, the latest Faktor-IPS is used with an Eclipse 2019-03 runtime, all installed from
 * <a href="https://faktorzehn.org">faktorzehn.org</a> update sites.
 * <p>
 * To change from where the plugins are installed, see {@link #additionalRepositories},
 * {@link #repositories}, {@link #fipsRepository}/{@link #fipsRepositoryVersion},
 * {@link #eclipseRepository} and {@link #thirdpartyRepository}.
 * <p>
 * Additional plugins (like the Faktor-IPS Product Variant Plugin) can be configured with
 * {@link #additionalPlugins}.
 */
@Mojo(name = "faktorips-build", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class IpsBuildMojo extends AbstractMojo {

    /**
     * Whether to add default dependencies to bundles org.eclipse.equinox.launcher, org.eclipse.osgi
     * and org.eclipse.core.runtime.
     */
    // @Parameter(defaultValue = "true")
    private boolean addDefaultDependencies = true;

    /**
     * Execution environment profile name used to resolve dependencies and run Faktor-IPS. Must be
     * at least JavaSE-11.
     */
    @Parameter(defaultValue = "JavaSE-11")
    private String executionEnvironment;

    /**
     * Whether to skip mojo execution.
     */
    // @Parameter(property = "eclipserun.skip", defaultValue = "false")
    private boolean skip = false;

    /**
     * Dependencies which will be resolved transitively to make up the eclipse runtime.
     */
    // Not a parameter, for internal use only
    private List<Dependency> dependencies = new ArrayList<>();

    /**
     * Additional Eclipse-plugins which will be resolved transitively to make up the eclipse
     * runtime.
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <additionalPlugins>
     *  <dependency>
     *   <artifactId>org.faktorips.productvariant.core</artifactId>
     *   <type>eclipse-plugin</type>
     *  </dependency>
     * </additionalPlugins>
     * }
     * </pre>
     */
    @Parameter
    private List<Dependency> additionalPlugins = new ArrayList<>();

    /**
     * List of JVM arguments set on the command line.
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <jvmArgs>
     *   <args>-Xdebug</args>
     *   <args>-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044</args>
     * </jvmArgs>
     * }
     * </pre>
     */
    // @Parameter
    private List<String> jvmArgs = new ArrayList<>();

    /**
     * List of applications arguments set on the command line.
     * <p>
     * Example:
     *
     * <pre>
     * {@code
     * <applicationsArgs>
     *  <args>-buildfile</args>
     *  <args>build-test.xml</args>
     * </applicationsArgs>
     * }
     * </pre>
     */
    // @Parameter
    private List<String> applicationsArgs = new ArrayList<>();

    /**
     * p2 repositories which will be used to resolve dependencies. If the default values should be
     * used this parameter must remain unused. Additional repositories can then be defined using the
     * {@link #additionalRepositories} parameter. The paths of the default repositories can be
     * changed individually using the parameters {@link #fipsRepository}, {@link #eclipseRepository}
     * and {@link #thirdpartyRepository} or the properties {@code repository.fips},
     * {@code repository.eclipse} and {@code repository.thirdparty}.
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <repositories>
     *  <repository>
     *   <id>faktor-ips-21-6</id>
     *   <layout>p2</layout>
     *   <url>https://update.faktorzehn.org/faktorips/v21_6/</url>
     *  </repository>
     *  <repository>
     *   <id>eclipse-2020-12</id>
     *   <layout>p2</layout>
     *   <url>http://download.eclipse.org/eclipse/updates/4.18/</url>
     *  </repository>
     *  <repository>
     *   <id>thirdparty</id>
     *   <layout>p2</layout>
     *   <url>https://drone.faktorzehn.de/p2/thirdparty-1.6</url>
     *  </repository>
     * </repositories>
     * }
     * </pre>
     * 
     * @see #additionalRepositories
     */
    @Parameter
    private List<Repository> repositories = new ArrayList<>();
    /**
     * This parameter makes it possible to define additional repositories while using the default
     * repositories (for which the {@link #repositories} parameter must remain unused).
     * <p>
     * Example:
     * 
     * <pre>
     * {@code
     * <additionalRepositories>
     *  <repository>
     *   <id>productvariants</id>
     *   <layout>p2</layout>
     *   <url>https://update.faktorzehn.org/faktorips/productvariants/21.6</url>
     *  </repository>
     * </additionalRepositories>
     * }
     * </pre>
     */
    @Parameter
    private List<Repository> additionalRepositories = new ArrayList<>();

    /**
     * Kill the forked process after a certain number of seconds. If set to 0, wait forever for the
     * process, never timing out.
     */
    // @Parameter(property = "eclipserun.timeout")
    private int forkedProcessTimeoutInSeconds;

    /**
     * Additional environments to set for the forked JVM.
     */
    // @Parameter
    private Map<String, String> environmentVariables;

    /**
     * Work area. This includes:
     * <ul>
     * <li><b>&lt;work&gt;/configuration</b>: The configuration area (<b>-configuration</b>)
     * <li><b>&lt;work&gt;/data</b>: The data ('workspace') area (<b>-data</b>)
     * </ul>
     */
    @Parameter(defaultValue = "${java.io.tmpdir}/${project.name}/eclipserun-work")
    private File work;

    @Parameter(property = "session", readonly = true, required = true)
    private MavenSession session;

    /**
     * Path to an ant build file. If no path is specified, a new script is generated.
     */
    @Parameter(property = "ant.script")
    private String antScriptPath;

    /**
     * Whether to include the HTML export. It will be generated in {@code target/html}.
     * <p>
     * <em>UI-libraries are required for the HTML export to work, so if you run this build on a CI
     * server like Jenkins, make sure to install for example the Xvfb fake X server.</em>
     * <p>
     * To package the generated HTML export in its own JAR, configure the maven-jar-plugin as
     * follows:
     *
     * <pre>
     * &lt;plugin&gt;
     *   &lt;artifactId&gt;maven-jar-plugin&lt;/artifactId&gt;
     *   &lt;executions&gt;
     *     &lt;execution&gt;
     *       &lt;configuration&gt;
     *         &lt;classifier&gt;html&lt;/classifier&gt;
     *         &lt;classesDirectory&gt;${project.build.directory}/html&lt;/classesDirectory&gt;
     *         &lt;includes&gt;**&#47;*&lt;/includes&gt;
     *       &lt;/configuration&gt;
     *       &lt;id&gt;pack-html&lt;/id&gt;
     *       &lt;phase&gt;package&lt;/phase&gt;
     *       &lt;goals&gt;
     *         &lt;goal&gt;jar&lt;/goal&gt;
     *       &lt;/goals&gt;
     *     &lt;/execution&gt;
     *   &lt;/executions&gt;
     * &lt;/plugin&gt;
     * </pre>
     */
    // we can't use @code for this Javadoc, because it includes } and **/
    @Parameter(defaultValue = "false")
    private boolean exportHtml;

    /**
     * Path to the JDK the project should build against. If no path is specified, the parameter
     * {@link #jdkId} will be evaluated. If both parameters do not contain a value, the
     * {@link #executionEnvironment} will be used as default.
     */
    @Parameter(property = "jdk.dir")
    private String jdkDir;

    /**
     * ID of the JDK the project should build against. The corresponding JDK must be configured in
     * the Maven {@code toolchains.xml} This parameter is only evaluated if {@link #jdkDir} is not
     * specified. If this parameter does not contain a value either, the
     * {@link #executionEnvironment} will be used as default.
     */
    @Parameter(property = "jdk.id")
    private String jdkId;

    /**
     * The version of Faktor-IPS to be installed.
     */
    @Parameter(property = "faktorips.repository.version", defaultValue = "latest")
    private String fipsRepositoryVersion;

    /**
     * Path to the update site to install Faktor-IPS.
     */
    @Parameter(property = "repository.fips", defaultValue = "https://update.faktorzehn.org/faktorips/${faktorips.repository.version}/")
    private String fipsRepository;

    /**
     * Path to the third-party repository.
     */
    @Parameter(property = "repository.thirdparty", defaultValue = "https://drone.faktorzehn.de/p2/thirdparty-1.6")
    private String thirdpartyRepository;

    /**
     * Path to the update site to install Eclipse.
     */
    @Parameter(property = "repository.eclipse", defaultValue = "http://update.faktorzehn.org/p2repositories/2019-03/")
    private String eclipseRepository;

    /**
     * Starts the build in debug mode and pauses it until a remote debugger has been connected. The
     * default debug port is 8000; a different port can be configured with {@link #debugPort}.
     */
    @Parameter(property = "faktorips.debug")
    private boolean debug;

    /**
     * The port on which the started Faktor-IPS build will listen for a remote debugger.
     *
     * @see #debug
     */
    @Parameter(property = "faktorips.debug.port", defaultValue = "8000")
    private int debugPort;

    @Component
    private MavenProject project;

    @Component
    private EquinoxInstallationFactory installationFactory;

    @Component
    private EquinoxLauncher launcher;

    @Component
    private ToolchainProvider toolchainProvider;

    @Component
    private EquinoxServiceFactory equinox;

    @Component
    private Logger logger;

    @Component
    private ToolchainManager toolchainManager;

    public String getProjectName() {
        File eclipseProjectFile = new File(project.getBasedir().getAbsolutePath(), ".project");
        if (eclipseProjectFile.exists()) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = documentBuilder.parse(eclipseProjectFile);
                return doc.getElementsByTagName("name").item(0).getTextContent();
            } catch (SAXException | IOException | ParserConfigurationException e) {
                getLog().error("Can't read Eclipse .project file to find project name", e);
            }
        }
        // TODO FIPS-5457 : when we import the project as a maven project instead of as an existing
        // eclipse project, we need to use
        // return project.getName();
        return project.getBasedir().getName();
    }

    public String getFipsRepository() {
        return fipsRepository.replace("${faktorips.repository.version}", fipsRepositoryVersion);
    }

    public String getPathToAntScript() {
        if (antScriptPath == null) {
            try {
                antScriptPath = project.getBuild().getDirectory() + "/importProjects.xml";
                FileUtils.forceMkdir(new File(project.getBuild().getDirectory()));
                String script = IOUtils.toString(getClass().getResourceAsStream("/importProjects.xml"),
                        StandardCharsets.UTF_8);

                boolean usesCustomJdk = usesCustomJdk();
                List<String> replaced = script.lines()
                        .filter(line -> usesCustomJdk || !line.contains("faktorips.configureJdk"))
                        .filter(line -> exportHtml || !line.contains("faktorips.exportHtml"))
                        .collect(Collectors.toList());
                Files.write(Paths.get(antScriptPath), replaced, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                getLog().error("Can't create ant script in " + antScriptPath, e);
            }
        }
        return antScriptPath;
    }

    private boolean usesCustomJdk() {
        return jdkDir != null || jdkId != null;
    }

    public String getPathToJdk() throws MojoExecutionException {
        if (jdkDir != null) {
            return jdkDir;
        } else {
            if (jdkId != null) {
                Toolchain tc = toolchainProvider.findMatchingJavaToolChain(session, jdkId);
                if (tc != null) {
                    getLog().info("Toolchain in faktorips-maven-plugin: " + tc);
                    return new File(tc.findTool("java")).getParentFile().getParent();
                } else if (Objects.equals(jdkId, "JavaSE-" + Runtime.version().feature())) {
                    getLog().debug("Using current Java runtime to build project as it matches the configured JDK ID.");
                } else {
                    getLog().warn("No toolchain was found in faktorips-maven-plugin for " + jdkId
                            + ". Current Java runtime will be used to build the project.");
                }
            }
            return null;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // add default repositories if no repositories are specified in the pom.xml
        if (repositories.isEmpty()) {
            addRepository(eclipseRepository);
            addRepository(getFipsRepository());
            addRepository(thirdpartyRepository);
        }
        repositories.addAll(additionalRepositories);

        // default values for parameter dependencies
        addDependency("org.faktorips.devtools.core");
        addDependency("org.faktorips.devtools.stdbuilder");
        addDependency("org.faktorips.runtime.groovy");
        addDependency("org.faktorips.valuetypes.joda");
        addDependency("org.faktorips.devtools.ant");
        addDependency("org.eclipse.jdt.junit");
        addDependency("org.eclipse.jdt.junit5.runtime");
        if (exportHtml) {
            addDependency("org.faktorips.devtools.htmlexport");
        }
        dependencies.addAll(additionalPlugins);

        // default values for parameter applicationArgs
        applicationsArgs.add("-consoleLog");
        applicationsArgs.add("-application");
        applicationsArgs.add("org.eclipse.ant.core.antRunner");
        applicationsArgs.add("-buildfile");
        applicationsArgs.add(getPathToAntScript());
        applicationsArgs.add("import");

        // default values for parameter jvmArgs
        jvmArgs.add("-Xmx1024m");
        jvmArgs.add("-XX:+HeapDumpOnOutOfMemoryError");
        jvmArgs.add("-DjavacFailOnError=true");
        if (usesCustomJdk()) {
            jvmArgs.add("-Djdk.dir=" + getPathToJdk());
        }
        jvmArgs.add("-DprojectName=" + getProjectName());
        jvmArgs.add("-Dsourcedir=" + project.getBasedir().getAbsolutePath());
        if (debug) {
            jvmArgs.add("-Xdebug");
            jvmArgs.add("-Xnoagent");
            jvmArgs.add("-Xrunjdwp:transport=dt_socket,address=" + debugPort + ",server=y,suspend=y");
        }

        File workDir = work.getAbsoluteFile();
        if (workDir.exists()) {
            try {
                FileUtils.deleteDirectory(workDir);
            } catch (IOException e) {
                throw new MojoExecutionException("Error while cleaning work directory " + workDir.getAbsolutePath(), e);
            }
        }
        // no need to clean as we just deleted the parent directory
        boolean clearWorkspaceBeforeLaunch = false;

        copyMavenSettings();

        EclipseRunMojo eclipseRunMojo = new EclipseRunMojo(work,
                clearWorkspaceBeforeLaunch,
                project,
                dependencies,
                addDefaultDependencies,
                executionEnvironment,
                repositories,
                session,
                jvmArgs,
                skip,
                applicationsArgs,
                forkedProcessTimeoutInSeconds,
                environmentVariables,
                installationFactory,
                launcher,
                toolchainProvider,
                equinox,
                logger,
                toolchainManager);
        eclipseRunMojo.execute();
    }

    private void copyMavenSettings() {
        try {
            File settingsDir = new File(work,
                    "data/.metadata/.plugins/org.eclipse.core.runtime/.settings")
                            .getAbsoluteFile();
            FileUtils.forceMkdir(settingsDir);

            Properties p = new Properties();
            p.put("eclipse.m2.userSettingsFile", session.getRequest().getUserSettingsFile().getAbsolutePath());
            p.put("eclipse.preferences.version", "1");
            p.store(new FileOutputStream(new File(settingsDir, "org.eclipse.m2e.core.prefs")), "IpsBuildMojo");
        } catch (IOException e) {
            getLog().error("Error while copying the maven settings into the workspace", e);
        }
    }

    private void addRepository(String url) {
        Repository repo = new Repository(URI.create(url));
        repo.setLayout("p2");
        repositories.add(repo);
    }

    private void addDependency(String artifactId) {
        Dependency dependency = new Dependency();
        dependency.setArtifactId(artifactId);
        dependency.setType("eclipse-plugin");
        dependencies.add(dependency);
    }
}