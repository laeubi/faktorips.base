/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;

/**
 * A runtime repository that keeps its data in memory.
 */
public class InMemoryRuntimeRepository extends AbstractRuntimeRepository {

    /** Contains product component IDs as keys and instances of product components as values. */
    private HashMap<String, IProductComponent> productCmpts = new HashMap<>();

    /**
     * A map that contains a list (value) by product component id (key). Each list contains the
     * generations for the product component.
     */
    private HashMap<String, SortedSet<IProductComponentGeneration>> productCmptGenLists = new HashMap<>();

    private List<ITable<?>> tables = new ArrayList<>();

    /**
     * Contains the table contents for structures that allow multiple contents key is the qName,
     * value the table.
     */
    private Map<String, ITable<?>> multipleContentTables = new HashMap<>();

    /** Contains all test cases with their qualified name as key. */
    private HashMap<String, IpsTestCaseBase> testCasesByQName = new HashMap<>();

    /**
     * Contains all enumeration values for the Faktor-IPS enumerations which content is deferred.
     */
    private Map<Class<?>, List<?>> enumValuesMap = new HashMap<>();

    private List<XmlAdapter<?, ?>> enumXmlAdapters = new LinkedList<>();

    /** Contains all maps for all other runtime objects with their qualified name as key. */
    private Map<Class<?>, Map<String, IRuntimeObject>> customRuntimeObjectsByType = new HashMap<>();

    public InMemoryRuntimeRepository() {
        super("InMemoryRuntimeRepository");
    }

    public InMemoryRuntimeRepository(String name) {
        super(name);
    }

    @Override
    protected IProductComponent getProductComponentInternal(String id) {
        return productCmpts.get(id);
    }

    @Override
    protected IProductComponent getProductComponentInternal(String kindId, String versionId) {
        if (kindId == null) {
            return null;
        }
        if (versionId == null) {
            throw new RuntimeException("Not implemented yet.");
        }
        for (IProductComponent cmpt : productCmpts.values()) {
            if (kindId.equals(cmpt.getKindId()) && versionId.equals(cmpt.getVersionId())) {
                return cmpt;
            }
        }
        return null;
    }

    @Override
    public void getAllProductComponents(String kindId, List<IProductComponent> result) {
        for (IProductComponent cmpt : productCmpts.values()) {
            if (kindId.equals(cmpt.getKindId())) {
                result.add(cmpt);
            }
        }
    }

    private SortedSet<IProductComponentGeneration> getLoadedProductCmptGenerations(String productCmptId) {
        return productCmptGenLists.get(productCmptId);
    }

    @Override
    public void getProductComponentGenerations(IProductComponent productCmpt,
            List<IProductComponentGeneration> result) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(productCmpt.getId());
        if (genSet != null) {
            result.addAll(genSet);
        }
    }

    /**
     * Nothing to do for the in memory repository.
     */
    public void initialize() {
        // nothing to do
    }

    @Override
    protected void getAllTables(List<ITable<?>> result) {
        result.addAll(tables);
    }

    /**
     * InMemoryRepository also searches for tables that are instances of subclasses of the given
     * tableClass. This allows to mock a table class for testing purposes.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected <T extends ITable<?>> T getTableInternal(Class<T> tableClass) {
        for (ITable<?> table : tables) {
            if (tableClass.isAssignableFrom(table.getClass())) {
                return tableClass.cast(table);
            }
        }
        return null;
    }

    /**
     * Puts the table into the repository. Replaces any table instance of the same class or any of
     * its superclasses. The latter check is needed to replace tables with mock implementations.
     * 
     * @throws NullPointerException if table is <code>null</code>.
     */
    public void putTable(ITable<?> table) {
        @SuppressWarnings("rawtypes")
        Class<? extends ITable> tableClass = table.getClass();
        for (Iterator<ITable<?>> it = tables.iterator(); it.hasNext();) {
            ITable<?> each = it.next();
            if (each.getClass().isAssignableFrom(tableClass)) {
                it.remove();
            }
        }
        tables.add(table);
    }

    /**
     * Puts the table with the indicated name into the repository with . Replaces any table instance
     * with the same qualified name.
     * 
     * @throws NullPointerException if table or qName is <code>null</code>.
     */
    public void putTable(ITable<?> table, String qName) {
        multipleContentTables.put(qName, table);
        putTable(table);
    }

    /**
     * Puts the given enum values in the repository replacing all existing values for the given
     * enumType.
     * 
     * @param enumType The Java class representing the enumeration type.
     * @param enumValues The value of the enumeration type as list.
     */
    public <T> void putEnumValues(Class<T> enumType, List<T> enumValues) {
        List<T> copy = new ArrayList<>(enumValues);
        enumValuesMap.put(enumType, copy);
    }

    @Override
    protected ITable<?> getTableInternal(String qualifiedTableName) {
        return multipleContentTables.get(qualifiedTableName);
    }

    @Override
    public void getAllProductComponents(List<IProductComponent> result) {
        result.addAll(productCmpts.values());
    }

    @Override
    public void getAllProductComponentIds(List<String> result) {
        result.addAll(productCmpts.keySet());
    }

    @Override
    protected void getAllEnumClasses(LinkedHashSet<Class<?>> result) {
        result.addAll(enumValuesMap.keySet());
    }

    @Override
    public boolean isModifiable() {
        return true;
    }

    /**
     * Puts the product component into the repository. If the repository already contains a
     * component with the same id, the new component replaces the old one.
     * 
     * @throws IllegalRepositoryModificationException if this repository does not allows to modify
     * @throws NullPointerException if productCmpt is <code>null</code> its contents.
     * 
     * @see IRuntimeRepository#isModifiable()
     */
    public void putProductComponent(IProductComponent productCmpt) {
        if (productCmpt == null) {
            throw new NullPointerException();
        }
        productCmpts.put(productCmpt.getId(), productCmpt);
    }

    @Override
    protected IProductComponentGeneration getProductComponentGenerationInternal(String productCmptId,
            Calendar effectiveDate) {
        if ((productCmptId == null) || (effectiveDate == null)) {
            return null;
        }
        SortedSet<IProductComponentGeneration> genSortedSet = getGenerationSortedSet(productCmptId);
        IProductComponentGeneration foundGen = null;
        long effectiveTime = effectiveDate.getTimeInMillis();
        long foundGenValidFrom = Long.MIN_VALUE;
        long genValidFrom;
        for (IProductComponentGeneration gen : genSortedSet) {
            genValidFrom = gen.getValidFrom(effectiveDate.getTimeZone()).getTime();
            if (effectiveTime >= genValidFrom && genValidFrom > foundGenValidFrom) {
                foundGen = gen;
                foundGenValidFrom = genValidFrom;
            }
        }
        return foundGen;
    }

    /**
     * Puts the product component generation and its product component into the repository. If the
     * repository already contains a generation with the same id, the new component replaces the old
     * one. The same applies for the product component.
     * 
     * @throws IllegalRepositoryModificationException if this repository does not allows to modify
     *             its contents.
     * @throws NullPointerException if generation is <code>null</code>
     * 
     * @see IRuntimeRepository#isModifiable()
     */
    public void putProductCmptGeneration(IProductComponentGeneration generation) {
        if (generation == null) {
            throw new NullPointerException();
        }
        getGenerationSortedSet(generation.getProductComponent().getId()).add(generation);
        putProductComponent(generation.getProductComponent());
    }

    private SortedSet<IProductComponentGeneration> getGenerationSortedSet(String productCmptId) {
        return productCmptGenLists.computeIfAbsent(productCmptId,
                $ -> new TreeSet<>(
                        new ProductCmptGenerationComparator(TimeZone.getDefault())));
    }

    @Override
    protected void getAllIpsTestCases(List<IpsTest2> result, IRuntimeRepository runtimeRepository) {
        // ignore the runtimeRepository to instantiate the test case because we using in memory
        result.addAll(testCasesByQName.values());
    }

    @Override
    protected void getIpsTestCasesStartingWith(String qNamePrefix,
            List<IpsTest2> result,
            IRuntimeRepository runtimeRepository) {
        for (String qName : testCasesByQName.keySet()) {
            if (qName.startsWith(qNamePrefix)) {
                result.add(testCasesByQName.get(qName));
            }
        }
    }

    @Override
    protected IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository) {
        // ignore the runtimeRepository to instantiate the test case because we using in memory
        return testCasesByQName.get(qName);
    }

    /**
     * Puts the test case into the repository.
     */
    public void putIpsTestCase(IpsTestCaseBase test) {
        testCasesByQName.put(test.getQualifiedName(), test);
    }

    @Override
    protected void getAllModelTypeImplementationClasses(Set<String> result) {
        throw new RuntimeException("Currently not supported by InMemoryRuntimeRepository.");
    }

    @Override
    protected IProductComponentGeneration getNextProductComponentGenerationInternal(
            IProductComponentGeneration generation) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(
                generation.getProductComponent().getId());
        SortedSet<IProductComponentGeneration> successor = genSet.tailSet(generation);
        if (successor == null) {
            return null;
        }
        for (IProductComponentGeneration next : successor) {
            if (next.equals(generation)) {
                continue;
            }
            return next;
        }
        return null;
    }

    @Override
    protected int getNumberOfProductComponentGenerationsInternal(IProductComponent productCmpt) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(productCmpt.getId());
        return genSet.size();
    }

    @Override
    protected IProductComponentGeneration getPreviousProductComponentGenerationInternal(
            IProductComponentGeneration generation) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(
                generation.getProductComponent().getId());
        SortedSet<IProductComponentGeneration> predecessors = genSet.headSet(generation);
        if (predecessors.isEmpty()) {
            return null;
        }
        return predecessors.last();
    }

    @Override
    protected IProductComponentGeneration getLatestProductComponentGenerationInternal(IProductComponent productCmpt) {
        if (productCmpt == null) {
            throw new NullPointerException("The parameter productCmpt cannot be null.");
        }
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(productCmpt.getId());
        return genSet.last();
    }

    @Override
    protected <T> List<T> getEnumValuesInternal(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        List<T> values = (List<T>)enumValuesMap.get(clazz);
        if (values == null) {
            return null;
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Adds an {@link XmlAdapter} for a Faktor-IPS enumeration that defers its content to a
     * enumeration content to this repository.
     */
    public void addEnumXmlAdapter(XmlAdapter<?, ?> enumXmlAdapter) {
        enumXmlAdapters.add(enumXmlAdapter);
    }

    @Override
    protected List<XmlAdapter<?, ?>> getAllInternalEnumXmlAdapters(IRuntimeRepository repository) {
        return enumXmlAdapters;
    }

    /**
     * Puts the runtimeObject into the repository.
     */
    public <T extends IRuntimeObject> void putCustomRuntimeObject(Class<T> type,
            String ipsObjectQualifiedName,
            T runtimeObject) {
        Map<String, IRuntimeObject> customRuntimeObjects = customRuntimeObjectsByType.computeIfAbsent(type,
                $ -> new HashMap<>());
        customRuntimeObjects.put(ipsObjectQualifiedName, runtimeObject);
    }

    @Override
    protected <T> T getCustomRuntimeObjectInternal(Class<T> type, String id) {
        Map<String, IRuntimeObject> otherRuntimeObjects = customRuntimeObjectsByType.get(type);
        if (otherRuntimeObjects != null) {
            return cast(otherRuntimeObjects.get(id));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(IRuntimeObject runtimeObject) {
        return (T)runtimeObject;
    }

    private static class ProductCmptGenerationComparator implements Comparator<IProductComponentGeneration> {

        private TimeZone timeZone;

        private ProductCmptGenerationComparator(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        @Override
        public int compare(IProductComponentGeneration gen1, IProductComponentGeneration gen2) {

            if (Objects.equals(gen1, gen2)) {
                return 0;
            }

            if (gen1.getValidFrom(timeZone).before(gen2.getValidFrom(timeZone))) {
                return -1;
            }
            if (gen1.getValidFrom(timeZone).after(gen2.getValidFrom(timeZone))) {
                return 1;
            }
            return 0;
        }

    }

}
