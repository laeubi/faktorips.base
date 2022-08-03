/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.ProductComponentLink;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.values.ObjectUtil;
import org.junit.Before;
import org.junit.Test;

public class ProductAssociationTest {

    private IRuntimeRepository repository;

    private final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    private final ProductCmptType productCmptType = IpsModel.getProductCmptType(Source.class);
    private final ProductAssociation association = productCmptType.getAssociation("asso");
    private final ProductAssociation association2 = productCmptType.getAssociation("asso2");
    private final ProductAssociation association3 = productCmptType.getAssociation("asso3");
    private final ProductAssociation overriddenAsso = productCmptType.getAssociation("overriddenAsso");

    @Before
    public void setUpRepository() {
        repository = new InMemoryRuntimeRepository();
    }

    @Test
    public void testGetTarget() {
        assertEquals(Target.class, association.getTarget().getJavaClass());
        assertEquals(Target.class, association2.getTarget().getJavaClass());
    }

    @Test
    public void testGetNamePlural() {
        assertEquals("testPluralLabelProduct", association.getLabelForPlural(Locale.CANADA));
        assertEquals("assos2", association2.getLabelForPlural(Locale.CANADA));
    }

    @Test
    public void testGetAssociationKind() {
        assertEquals(AssociationKind.Association, association.getAssociationKind());
        assertEquals(AssociationKind.Composition, association2.getAssociationKind());
    }

    @Test
    public void testGetMinCardinality() {
        assertEquals(0, association.getMinCardinality());
        assertEquals(1, association2.getMinCardinality());
    }

    @Test
    public void testGetMaxCardinality() {
        assertEquals(1, association.getMaxCardinality());
        assertEquals(10, association2.getMaxCardinality());
    }

    @Test
    public void testGetTargetObjects() {
        Source source = new Source();
        source.target = new Target();
        ProductGen productGen = source.productGen;
        ProductGen productGen2 = source.productGen2;
        productGen.target = new Target();
        productGen2.target = new Target();
        productGen2.target2 = new Target();

        assertThat(association.getTargetObjects(source, null).size(), is(1));
        assertThat(association.getTargetObjects(source, null), hasItem(source.target));

        assertThat(association2.getTargetObjects(source, null).size(), is(1));
        assertThat(association2.getTargetObjects(source, null), hasItem(productGen.target));

        assertThat(association2.getTargetObjects(source, effectiveDate).size(), is(1));
        assertThat(association2.getTargetObjects(source, effectiveDate), hasItem(productGen.target));

        assertThat(association2.getTargetObjects(source, new GregorianCalendar(1999, 2, 2)).size(), is(2));
        assertThat(association2.getTargetObjects(source, new GregorianCalendar(1999, 2, 2)),
                hasItem(productGen2.target));
        assertThat(association2.getTargetObjects(source, new GregorianCalendar(1999, 2, 2)),
                hasItem(productGen2.target2));

        assertThat(overriddenAsso.getTargetObjects(source, null).size(), is(1));
        assertThat(overriddenAsso.getTargetObjects(source, null), hasItem(source.target));
    }

    @Test
    public void testGetTargetObjects_Overridden() {
        SubSource source = new SubSource();
        source.target = new SubTarget();

        ProductAssociation inheritedAssociation = IpsModel.getProductCmptType(source).getAssociation("asso");

        assertThat(inheritedAssociation.getTargetObjects(source, null).size(), is(1));
        assertThat(inheritedAssociation.getTargetObjects(source, null), hasItem(source.target));

        ProductAssociation overriddenAssociation = IpsModel.getProductCmptType(source).getAssociation("overriddenAsso");

        assertThat(overriddenAssociation.getTargetObjects(source, null).size(), is(1));
        assertThat(overriddenAssociation.getTargetObjects(source, null), hasItem(source.target));
    }

    @Test
    public void testGetUsedName() {
        assertEquals("asso", association.getUsedName());
        assertEquals("assos2", association2.getUsedName());
    }

    @Test
    public void testIsDerivedUnion() {
        assertTrue(association.isDerivedUnion());
        assertFalse(association2.isDerivedUnion());
    }

    @Test
    public void testIsSubsetOfADerivedUnion() {
        assertTrue(association.isSubsetOfADerivedUnion());
        assertFalse(association2.isSubsetOfADerivedUnion());
    }

    @Test
    public void testGetInverseAssociation() {
        assertNull(association.getInverseAssociation());
        assertEquals("Inverse", association2.getInverseAssociation());
    }

    @Test
    public void testIsMatchingAssociationPresent() {
        assertFalse(association.isMatchingAssociationPresent());
        assertTrue(association2.isMatchingAssociationPresent());
    }

    @Test
    public void testGetMatchingAssociationName() {
        assertNull(association.getMatchingAssociationName());
        assertEquals("Matching", association2.getMatchingAssociationName());
    }

    @Test
    public void testGetMatchingAssociationSource() {
        assertNull(association.getMatchingAssociationSource());
        assertEquals("MyPolicy", association2.getMatchingAssociationSource());
    }

    @Test
    public void testGetMatchingAssociationSourceType() throws Exception {
        assertNull(association.getMatchingAssociationSourceType());
        assertEquals(Policy.class, association2.getMatchingAssociationSourceType().getJavaClass());
    }

    @Test
    public void testGetMatchingAssociation() {
        PolicyAssociation matchingAssociation = overriddenAsso.getMatchingAssociation();
        assertEquals("Matching", matchingAssociation.getName());
        assertEquals("Matching", matchingAssociation.getNamePlural());
        assertEquals(Policy.class, matchingAssociation.getType().getJavaClass());
    }

    @Test
    public void testGetMatchingAssociation_NoMatching() {
        assertNull(association.getMatchingAssociation());
    }

    @Test
    public void testFindMatchingAssociation() {
        PolicyAssociation matchingAssociation = overriddenAsso.getMatchingAssociation();
        assertEquals("Matching", matchingAssociation.getName());
        assertEquals("Matching", matchingAssociation.getNamePlural());
        assertEquals(Policy.class, matchingAssociation.getType().getJavaClass());
    }

    @Test
    public void testFindMatchingAssociation_NoMatching() {
        assertEquals(Optional.empty(), association.findMatchingAssociation());
    }

    @Test
    public void testGetLinks_Static() {
        Source source = new Source();
        source.target = new Target("id2");

        Collection<IProductComponentLink<Target>> links = association.getLinks(source, null);

        assertEquals(1, links.size());
        assertSame(source.target.getId(), links.iterator().next().getTargetId());
    }

    @Test
    public void testGetLinks_to1NoLink() {
        Source source = new Source();
        source.target = new Target("id2");

        Collection<IProductComponentLink<Target>> links = overriddenAsso.getLinks(source, null);
        assertTrue(links.isEmpty());
    }

    @Test
    public void testGetLinks_OnGeneration() {
        Source source = new Source();
        ProductGen productGen = source.productGen;
        ProductGen productGen2 = source.productGen2;
        productGen.target = new Target("id3");
        productGen2.target = new Target("id4");
        productGen2.target2 = new Target("id5");

        Collection<IProductComponentLink<Target>> linksGen1 = association2.getLinks(source, effectiveDate);
        assertEquals(1, linksGen1.size());
        assertSame(productGen.target.getId(), linksGen1.iterator().next().getTargetId());

        Collection<IProductComponentLink<Target>> linksGen2 = association2.getLinks(source, new GregorianCalendar());
        assertEquals(2, linksGen2.size());
        Iterator<IProductComponentLink<Target>> linksGen2Iter = linksGen2.iterator();
        assertSame(productGen2.target.getId(), linksGen2Iter.next().getTargetId());
        assertSame(productGen2.target2.getId(), linksGen2Iter.next().getTargetId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLinks_NoMethod() {
        Source source = new Source();
        ProductAssociation association4 = productCmptType.getAssociation("asso4");

        association4.getLinks(source, effectiveDate);
    }

    @Test
    public void testGetLinks_Overridden() {
        SubSource source = new SubSource();
        source.subAssoTarget = new Target("id2");

        Collection<IProductComponentLink<Target>> links = IpsModel.getProductCmptType(source)
                .getAssociation("overriddenAsso").getLinks(source, null);
        assertTrue(links.isEmpty());
    }

    @Test
    public void testIsOverriding() throws Exception {
        ProductCmptType subSource = IpsModel.getProductCmptType(SubSource.class);
        assertFalse(subSource.getAssociation("SubAsso").isOverriding());
        assertTrue(subSource.getAssociation("overriddenAsso").isOverriding());
    }

    @Test
    public void testGetSuperAssociation() throws Exception {
        ProductCmptType subSource = IpsModel.getProductCmptType(SubSource.class);
        assertThat(subSource.getAssociation("SubAsso").getSuperAssociation(), is(nullValue()));
        assertSame(subSource.getAssociation("overriddenAsso").getSuperAssociation(),
                productCmptType.getAssociation("overriddenAsso"));
    }

    @Test
    public void testAddTargetObjects_To1_Association() {
        SubSource source = new SubSource();
        Target target = new Target();

        association.addTargetObjects(source, null, target);

        assertThat(source.getTarget(), is(target));
    }

    @Test
    public void testAddTargetObjects_AssociationWithCardinality() {
        Source source = new Source();
        Target target = new Target();
        CardinalityRange cardinality = new CardinalityRange(0, 10, 1);

        association2.addTargetObject(source, null, target, cardinality);

        assertThat(source.productGen.getTargets(), hasItem(target));
        assertThat(source.productGen.getCardinalityForTarget(target), is(cardinality));
    }

    @Test
    public void testAddTargetObject_AssociationWithCardinalityInGeneration1() {
        Source source = new Source();
        Target target = new Target();
        CardinalityRange cardinality = new CardinalityRange(0, 10, 1);

        association2.addTargetObject(source, effectiveDate, target, cardinality);

        assertThat(source.productGen.getTargets(), hasItem(target));
        assertThat(source.productGen.getCardinalityForTarget(target), is(cardinality));
        assertNull(source.productGen2.target);
    }

    @Test
    public void testAddTargetObject_AssociationWithCardinalityInGeneration2() {
        Source source = new Source();
        Target target = new Target();
        CardinalityRange cardinality = new CardinalityRange(0, 10, 1);

        association2.addTargetObject(source, new GregorianCalendar(1999, 2, 2), target, cardinality);

        assertThat(source.productGen2.getTargets(), hasItem(target));
        assertThat(source.productGen2.getCardinalityForTarget(target), is(cardinality));
        assertNull(source.productGen.target);
    }

    @Test
    public void testAddTargetObjects_To1_ReplaceAssociatedObject() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();
        association.addTargetObjects(source, null, target);

        association.addTargetObjects(source, null, target2);

        assertThat(source.getTarget(), is(target2));
    }

    @Test
    public void testAddTargetObjects_To1_ReplaceAssociatedObjectWithCardinality() {
        SubSource source = new SubSource();
        ProductAssociation overriddenAssoInSub = IpsModel.getProductCmptType(source)
                .getAssociation("overriddenAsso");
        Target target = new SubTarget();
        Target target2 = new SubTarget();
        CardinalityRange cardinality = new CardinalityRange(0, 10, 1);
        CardinalityRange cardinality2 = new CardinalityRange(10, 20, 10);
        overriddenAssoInSub.addTargetObject(source, null, target, cardinality);

        overriddenAssoInSub.addTargetObject(source, null, target2, cardinality2);

        assertThat(source.getTarget(), is(target2));
        assertSame(cardinality2, source.getCardinalityForTarget(target2));
    }

    @Test
    public void testAddTargetObjects_ToN_AddMultipleTargets() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association2.addTargetObjects(source, null, target);
        association2.addTargetObjects(source, null, target2);

        assertThat(source.productGen.getTargets().size(), is(2));
    }

    @Test
    public void testAddTargetObjects_ToN_ExisingTarget() {
        Source source = new Source();
        Target existingTarget = new Target();
        Target newTarget = new Target();
        source.productGen.target = existingTarget;

        association2.addTargetObjects(source, null, newTarget);

        assertThat(source.productGen.getTargets().size(), is(2));
        assertThat(source.productGen.getTargets().get(0), is(existingTarget));
        assertThat(source.productGen.getTargets().get(1), is(newTarget));
    }

    @Test
    public void testAddTargetObjects_VarArg() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association2.addTargetObjects(source, null, target, target2);

        assertThat(source.productGen.getTargets().size(), is(2));
    }

    @Test
    public void testAddTargetObjects_List() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association2.addTargetObjects(source, null, Arrays.<IProductComponent> asList(target, target2));

        assertThat(source.productGen.getTargets().size(), is(2));
    }

    @Test
    public void testAddTargetObjects_ToN_DoNothingIfEmptyVarArg() {
        Source source = new Source();

        association2.addTargetObjects(source, null);

        assertThat(source.productGen.getTargets().size(), is(0));
    }

    @Test
    public void testAddTargetObjects_ToN_DoNothingIfEmptyList() {
        Source source = new Source();

        association2.addTargetObjects(source, null, new ArrayList<IProductComponent>());

        assertThat(source.productGen.getTargets().size(), is(0));
    }

    @Test
    public void testAddTargetObjects_To1_MultipleObjects() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        try {
            association.addTargetObjects(source, null, Arrays.<IProductComponent> asList(target, target2));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertNull(source.target);
        }
    }

    @Test
    public void testAddTargetObjects_NoMethod() {
        Source source = new Source();
        Target target = new Target();

        try {
            association3.addTargetObjects(source, null, target);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertNull(source.target);
        }
    }

    @Test
    public void testAddTargetObjects_To1_Overridden() {
        SubSource subSource = new SubSource();
        Target subTarget = new SubTarget();
        ProductAssociation overridingAssociation = IpsModel.getProductCmptType(subSource)
                .getAssociation("overriddenAsso");

        overridingAssociation.addTargetObjects(subSource, null, subTarget);

        assertThat(subSource.getOverriddenAsso(), is(subTarget));
    }

    @Test
    public void testAddTargetObjects_To1_Overridden_WrongType() {
        SubSource subSource = new SubSource();
        Target wrongTarget = new Target();
        ProductAssociation overridingAssociation = IpsModel.getProductCmptType(subSource)
                .getAssociation("overriddenAsso");
        try {
            overridingAssociation.addTargetObjects(subSource, null, wrongTarget);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertNull(subSource.getOverriddenAsso());
        }
    }

    @Test
    public void testRemoveTargetObjects_To1_Association() {
        Source source = new Source();
        Target target = new Target();
        source.setTarget(target);

        association.removeTargetObjects(source, null, target);

        assertNull(source.getTarget());
    }

    @Test
    public void testRemoveTargetObjects_To1_DoNothingIfDifferentObject() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();
        source.setTarget(target);

        association.removeTargetObjects(source, null, target2);

        assertThat(source.getTarget(), is(target));
    }

    @Test
    public void testRemoveTargetObjects_ToN() {
        Source source = new Source();
        ProductGen generation = (ProductGen)source.getLatestProductComponentGeneration();
        Target target = new Target();
        generation.addTarget(target);

        association2.removeTargetObjects(source, effectiveDate, target);
        association2.removeTargetObjects(source, effectiveDate, target);

        assertThat(generation.getTargets().size(), is(0));
    }

    @Test
    public void testRemoveTargetObjects_To1_Overridden() {
        SubSource subSource = new SubSource();
        Target subTarget = new SubTarget();
        subSource.setOverriddenAsso(subTarget);
        ProductAssociation overridingAssociation = IpsModel.getProductCmptType(subSource)
                .getAssociation("overriddenAsso");

        overridingAssociation.removeTargetObjects(subSource, null, subTarget);

        assertNull(subSource.getOverriddenAsso());
    }

    @Test
    public void testRemoveTargetObjects_ToN_DoNothingIfNotPresent() {
        Source source = new Source();
        ProductGen generation = (ProductGen)source.getLatestProductComponentGeneration();
        Target target = new Target();
        Target target2 = new Target();
        generation.addTarget(target);

        association2.removeTargetObjects(source, effectiveDate, target2);

        assertThat(generation.getTargets(), hasItem(target));
    }

    @Test
    public void testRemoveTargetObjects_ToN_DoNothingIfEmptyVarArg() {
        Source source = new Source();
        ProductGen generation = (ProductGen)source.getLatestProductComponentGeneration();
        Target target = new Target();
        generation.addTarget(target);

        association2.removeTargetObjects(source, effectiveDate);

        assertThat(generation.getTargets().get(0), is(target));
    }

    @Test
    public void testRemoveTargetObjects_ToN_DoNothingIfEmptyList() {
        Source source = new Source();
        ProductGen generation = (ProductGen)source.getLatestProductComponentGeneration();
        Target target = new Target();
        generation.addTarget(target);

        association2.removeTargetObjects(source, effectiveDate, new ArrayList<IProductComponent>());

        assertThat(generation.getTargets().get(0), is(target));
    }

    @Test
    public void testRemoveTargetObjects_VarArg() {
        Source source = new Source();
        ProductGen generation = (ProductGen)source.getLatestProductComponentGeneration();
        Target target = new Target();
        Target target2 = new Target();
        association2.addTargetObjects(source, effectiveDate, target, target2);

        association2.removeTargetObjects(source, effectiveDate, target, target2);

        assertThat(generation.getTargets().size(), is(0));
    }

    @Test
    public void testRemoveTargetObjects_List() {
        Source source = new Source();
        ProductGen generation = (ProductGen)source.getLatestProductComponentGeneration();
        Target target = new Target();
        Target target2 = new Target();
        association2.addTargetObjects(source, effectiveDate, target, target2);

        association2.removeTargetObjects(source, effectiveDate, Arrays.<IProductComponent> asList(target, target2));

        assertThat(generation.getTargets().size(), is(0));
    }

    @Test
    public void testRemoveTargetObjects_To1_MultipleObjects() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();
        source.setTarget(target);

        try {
            association.removeTargetObjects(source, effectiveDate, Arrays.<IProductComponent> asList(target, target2));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(source.getTarget(), is(target));
        }
    }

    @Test
    public void testRemoveTargetObjects_NoMethod() {
        Source source = new Source();
        Target target = new Target();
        source.setTarget(target);

        try {
            association3.removeTargetObjects(source, effectiveDate, target);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(association3.getTargetObjects(source, effectiveDate), hasItem(target));
        }
    }

    @Test
    public void testRemoveTargetObjects_CorrectGenerationAffected() {
        Source source = new Source();
        Target target1 = new Target();
        Target target2 = new Target();
        Target target3 = new Target();
        Target target4 = new Target();
        Calendar date2ndGeneration = new GregorianCalendar(1999, 2, 2);
        ProductGen generation1 = source.productGen;
        ProductGen generation2 = source.productGen2;
        association2.addTargetObjects(source, effectiveDate, target1, target2);
        association2.addTargetObjects(source, date2ndGeneration, target3, target4);

        association2.removeTargetObjects(source, date2ndGeneration, target3, target4);

        assertThat(generation1.getTargets(), hasItem(target1));
        assertThat(generation1.getTargets(), hasItem(target2));
        assertThat(generation2.getTargets().size(), is(0));
    }

    @IpsProductCmptType(name = "MySource")
    @IpsAssociations({ "asso", "asso2", "asso3", "overriddenAsso", "asso4" })
    @IpsChangingOverTime(ProductGen.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private class Source extends ProductComponent {

        Target target;
        private final ProductGen productGen = new ProductGen(this);
        private final ProductGen productGen2 = new ProductGen(this);

        public Source() {
            super(repository, "id", "productKindId", "versionId");
        }

        @IpsAssociation(name = "asso", pluralName = "assos", min = 0, max = 1, kind = AssociationKind.Association, targetClass = Target.class)
        @IpsDerivedUnion
        @IpsSubsetOfDerivedUnion("derivedUnion")
        public Target getTarget() {
            return target;
        }

        @IpsAssociationAdder(association = "asso")
        public void setTarget(Target target) {
            this.target = target;
        }

        @IpsAssociationRemover(association = "asso")
        public void removeTarget(Target target) {
            if (target.equals(this.target)) {
                this.target = null;
            }
        }

        @IpsAssociationLinks(association = "asso")
        public IProductComponentLink<Target> getLinkForAsso() {
            return new ProductComponentLink<>(this, target);
        }

        @IpsAssociation(name = "overriddenAsso", pluralName = "assos", min = 0, max = 1, kind = AssociationKind.Association, targetClass = Target.class)
        @IpsMatchingAssociation(source = Policy.class, name = "Matching")
        public Target getOverriddenAsso() {
            return target;
        }

        @IpsAssociationLinks(association = "overriddenAsso")
        public IProductComponentLink<Target> getLinkForOverriddenAsso() {
            return null;
        }

        @IpsAssociation(name = "asso3", pluralName = "assos3", min = 1, max = 10, kind = AssociationKind.Association, targetClass = Target.class)
        public List<Target> getTargets() {
            return Arrays.asList(target);
        }

        @IpsAssociation(name = "asso4", pluralName = "assos4", min = 0, max = 1, kind = AssociationKind.Association, targetClass = Target.class)
        @IpsDerivedUnion
        public Target getTarget4() {
            return target;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            if (effectiveDate != ProductAssociationTest.this.effectiveDate) {
                return productGen2;
            }
            return productGen;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return productGen;
        }

    }

    @IpsProductCmptType(name = "MySubSource")
    @IpsAssociations({ "SubAsso", "overriddenAsso", "asso", "asso2", "asso4" })
    @IpsChangingOverTime(ProductGen.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private class SubSource extends Source {

        private Target subAssoTarget;
        private CardinalityRange cardinality;

        @IpsAssociation(name = "SubAsso", pluralName = "SubAssos", min = 0, max = 1, kind = AssociationKind.Association, targetClass = Target.class)
        @IpsDerivedUnion
        public Target getSubAsso() {
            return subAssoTarget;
        }

        @IpsAssociationLinks(association = "SubAsso")
        public IProductComponentLink<Target> getLinkForSubAsso() {
            return new ProductComponentLink<>(this, subAssoTarget);
        }

        @Override
        @IpsAssociation(name = "overriddenAsso", pluralName = "overriddenAssos", min = 0, max = 1, kind = AssociationKind.Association, targetClass = Target.class)
        public Target getOverriddenAsso() {
            return super.getOverriddenAsso();
        }

        @IpsAssociationAdder(association = "overriddenAsso")
        public void setOverriddenAsso(Target target) {
            ObjectUtil.checkInstanceOf(target, SubTarget.class);
            super.target = target;
        }

        @IpsAssociationAdder(association = "overriddenAsso", withCardinality = true)
        public void setOverriddenAsso(Target target, CardinalityRange cardinality) {
            ObjectUtil.checkInstanceOf(target, SubTarget.class);
            this.cardinality = cardinality;
            super.target = target;
        }

        public CardinalityRange getCardinalityForTarget(Target productCmpt) {
            if (productCmpt.equals(super.target)) {
                return cardinality;
            }
            return null;
        }
    }

    private class ProductGen extends ProductComponentGeneration {

        private Target target;
        private Target target2;

        public CardinalityRange cardinality;

        public CardinalityRange cardinality2;

        public ProductGen(Source product) {
            super(product);
        }

        @IpsAssociation(name = "asso2", pluralName = "assos2", min = 1, max = 10, kind = AssociationKind.Composition, targetClass = Target.class)
        @IpsMatchingAssociation(source = Policy.class, name = "Matching")
        @IpsInverseAssociation("Inverse")
        public List<Target> getTargets() {
            ArrayList<Target> targets = new ArrayList<>(2);
            if (target != null) {
                targets.add(target);
            }
            if (target2 != null) {
                targets.add(target2);
            }
            return targets;
        }

        @IpsAssociationAdder(association = "asso2")
        public void addTarget(Target target) {
            if (this.target == null) {
                this.target = target;
            } else {
                target2 = target;
            }
        }

        @IpsAssociationAdder(association = "asso2", withCardinality = true)
        public void addTarget(Target target, CardinalityRange cardinality) {
            if (this.target == null) {
                this.target = target;
                this.cardinality = cardinality;
            } else {
                target2 = target;
                cardinality2 = cardinality;
            }
        }

        @IpsAssociationRemover(association = "asso2")
        public void removeTarget(Target target) {
            if (target.equals(this.target)) {
                this.target = null;
                cardinality = null;
            } else if (target.equals(target2)) {
                target2 = null;
                cardinality2 = null;
            }
        }

        public CardinalityRange getCardinalityForTarget(Target productCmpt) {
            if (productCmpt.equals(target)) {
                return cardinality;
            } else if (productCmpt.equals(target2)) {
                return cardinality2;
            }

            return null;
        }

        @IpsAssociationLinks(association = "asso2")
        public List<IProductComponentLink<Target>> getLinksForTargets() {
            List<IProductComponentLink<Target>> list = new ArrayList<>();
            if (target != null) {
                list.add(new ProductComponentLink<>(this, target));
            }
            if (target2 != null) {
                list.add(new ProductComponentLink<>(this, target2));
            }
            return list;
        }
    }

    @IpsProductCmptType(name = "MyTarget")
    private class Target extends ProductComponent {

        public Target(String id) {
            super(repository, id, "productKindId2", "versionId2");
        }

        public Target() {
            this("id2");
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }
    }

    @IpsProductCmptType(name = "MySubTarget")
    private class SubTarget extends Target {
        // another target
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsAssociations({ "Matching" })
    @IpsConfiguredBy(Source.class)
    private class Policy implements IModelObject {

        private Target target;

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @IpsAssociation(name = "Matching", pluralName = "Matching", min = 1, max = 10, kind = AssociationKind.Composition, targetClass = Target.class)
        public List<Target> getMatchings() {
            return Arrays.asList(target);
        }
    }
}
