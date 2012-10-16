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

package org.faktorips.devtools.stdbuilder.xpand;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.xtend.expression.ResourceManager;
import org.faktorips.devtools.core.builder.AbstractBuilderSet;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportHandler;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportStatement;

/**
 * This class holds all the context information needed to generate the java code with our XPAND
 * builder framework. Context information are for example the java class naming or the builder
 * configuration.
 * <p>
 * The import handler for a single file build is also stored in this context and need to be reseted
 * for every new file. In fact this is not the optimum but ok for the moment. To be thread safe the
 * import handler is stored as {@link ThreadLocal} variable.
 * 
 * 
 * @author widmaier
 */
public class GeneratorModelContext {

    private final JavaClassNaming javaClassNaming;

    /**
     * The import handler holds the import statements for a single file. However this context is the
     * same for all file generations. Because every file is generated sequentially in one thread we
     * could reuse a {@link ThreadLocal} variable in this model context. Every new file have to
     * clear its {@link ImportHandler} before starting generation.
     */
    private final ThreadLocal<ImportHandler> importHandlerThreadLocal = new ThreadLocal<ImportHandler>();

    private final ThreadLocal<GeneratorModelCaches> generatorModelCacheThreadLocal = new ThreadLocal<GeneratorModelCaches>();

    private final ThreadLocal<Long> generatorRunCount = new ThreadLocal<Long>();

    private final IIpsArtefactBuilderSetConfig config;

    private final Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap;

    private final ResourceManager resourceManager = new OptimizedResourceManager();

    private final IJavaPackageStructure javaPackageStructure;

    public GeneratorModelContext(IIpsArtefactBuilderSetConfig config, IJavaPackageStructure javaPackageStructure,
            Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap) {
        this.config = config;
        this.javaPackageStructure = javaPackageStructure;
        this.annotationGeneratorMap = annotationGeneratorMap;
        this.javaClassNaming = new JavaClassNaming(javaPackageStructure, true);
    }

    public void newBuilderProcess(String packageOfArtifacts) {
        importHandlerThreadLocal.set(new ImportHandler(packageOfArtifacts));
        generatorModelCacheThreadLocal.set(new GeneratorModelCaches());
        if (generatorRunCount.get() != null) {
            generatorRunCount.set(generatorRunCount.get() + 1);
        } else {
            generatorRunCount.set(0L);
        }
    }

    public long getGeneratorRunCount() {
        return generatorRunCount.get();
    }

    IIpsArtefactBuilderSetConfig getConfig() {
        return config;
    }

    /**
     * Returns the thread local import handler. The import handler stores all import statements
     * needed in the generated class file.
     * <p>
     * The import handler is stored as {@link ThreadLocal} variable to have the ability to generate
     * different files in different threads
     * 
     * @return The thread local import handler
     */
    public ImportHandler getImportHandler() {
        return importHandlerThreadLocal.get();
    }

    /**
     * Sets the thread local import handler. The import handler stores all import statements needed
     * in the generated class file.
     * <p>
     * The import handler is stored as {@link ThreadLocal} variable to have the ability to generate
     * different files in different threads
     * 
     * @param importHandler The thread local import handler
     */
    protected void setImportHandler(ImportHandler importHandler) {
        this.importHandlerThreadLocal.set(importHandler);
    }

    /**
     * Returns the thread local generator model cache. The generator model cache stores all cached
     * object references that may change on any time.
     * <p>
     * The generator model cache is stored as {@link ThreadLocal} variable to have the ability to
     * generate different files in different threads
     * 
     * @return The thread local generator model cache
     */
    public GeneratorModelCaches getGeneratorModelCache() {
        return generatorModelCacheThreadLocal.get();
    }

    /**
     * Getting the set of collected import statements.
     * 
     * @return Returns the imports.
     */
    public Set<ImportStatement> getImports() {
        return getImportHandler().getImports();
    }

    /**
     * Adds a new import. The import statement should be the full qualified name of a class.
     * 
     * @param importStatement The full qualified name of a class that should be imported.
     * @return the qualified or unqualified class name depending on whether it is required.
     * @see ImportHandler#addImportAndReturnClassName(String)
     */
    public String addImport(String importStatement) {
        return getImportHandler().addImportAndReturnClassName(importStatement);
    }

    public boolean removeImport(String importStatement) {
        return getImportHandler().remove(importStatement);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public Locale getLanguageUsedInGeneratedSourceCode() {
        String localeString = getConfig().getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_GENERATOR_LOCALE);
        if (localeString == null) {
            return Locale.ENGLISH;
        }
        return AbstractBuilderSet.getLocale(localeString);
    }

    /**
     * Returns the list of annotation generators for the given type. This method never returns null.
     * If there is no annotation generator for the specified type an empty list will be returned.
     * 
     * @param type The {@link AnnotatedJavaElementType} you want to get the generators for
     * @return the list of {@link IAnnotationGenerator annotation generators} or an empty list if
     *         there is none
     */
    public List<IAnnotationGenerator> getAnnotationGenerator(AnnotatedJavaElementType type) {
        List<IAnnotationGenerator> result = annotationGeneratorMap.get(type);
        if (result == null) {
            result = new ArrayList<IAnnotationGenerator>();
        }
        return result;
    }

    public JavaClassNaming getJavaClassNaming() {
        return javaClassNaming;
    }

    public String getValidationMessageBundleBaseName(IIpsSrcFolderEntry entry) {
        String baseName = javaPackageStructure.getBasePackageName(entry, false, false) + "."
                + entry.getValidationMessagesBundle();
        return baseName;
    }

    public boolean isGenerateChangeSupport() {
        return config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER)
                .booleanValue();
    }

    /**
     * Returns whether to generate camel case constant names with underscore separator or without.
     * For example if this property is true, the constant for the property
     * checkAnythingAndDoSomething would be generated as CHECK_ANYTHING_AND_DO_SOMETHING, if the
     * property is false the constant name would be CHECKANYTHINGANDDOSOMETHING.
     * 
     * @see StandardBuilderSet#CONFIG_PROPERTY_CAMELCASE_SEPARATED
     */
    public boolean isGenerateSeparatedCamelCase() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_CAMELCASE_SEPARATED);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean.booleanValue();
    }

    public boolean isGenerateDeltaSupport() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGenerateCopySupport() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_COPY_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGenerateVisitorSupport() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGenerateToXmlSupport() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_TO_XML_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGeneratePublishedInterfaces() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES);
        return propertyValueAsBoolean == null ? true : propertyValueAsBoolean.booleanValue();
    }

}
