/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * A directed relationship between to product component types.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeAssociation extends IAssociation {

    /**
     * The list of applicable types. For product component types only aggregations and associations
     * are supported.
     */
    public static final AssociationType[] APPLICABLE_ASSOCIATION_TYPES = new AssociationType[] {
            AssociationType.AGGREGATION, AssociationType.ASSOCIATION };

    public static final String PROPERTY_MATCHING_ASSOCIATION_SOURCE = "matchingAssociationSource"; //$NON-NLS-1$

    public static final String PROPERTY_MATCHING_ASSOCIATION_NAME = "matchingAssociationName"; //$NON-NLS-1$

    public static final String PROPERTY_CHANGING_OVER_TIME = "changingOverTime"; //$NON-NLS-1$

    public static final String PROPERTY_VISIBLE = "visible"; //$NON-NLS-1$

    /**
     * Message code for validation messages when the matching association was not found
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_NOT_FOUND = MSGCODE_PREFIX + "MatchingAssociationNotFound"; //$NON-NLS-1$

    /**
     * Message code for validation messages when the matching association is invalid
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_INVALID = MSGCODE_PREFIX + "MatchingAssociationInvalid"; //$NON-NLS-1$

    /**
     * Message code when derived unions and their subsets have different changing over time
     * properties. i.e. a subset is defined as changing over time, but the derived union is defined
     * as static.
     */
    public static final String MSGCODE_DERIVED_UNION_CHANGING_OVER_TIME_MISMATCH = MSGCODE_PREFIX
            + "DerivedUnionChangingOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that two associations matching associations with the same
     * name. Although these associations could be two different ones we would generate duplicated
     * methods.
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_DUPLICATE_NAME = MSGCODE_PREFIX
            + "MatchingAssociationDuplicateName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the property change over time mismatch with the
     * constrained association
     */
    public static final String MSGCODE_CONSTRAINED_CHANGEOVERTIME_MISMATCH = MSGCODE_PREFIX
            + "ConstrainedChangeOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Returns the product component type this relation belongs to. Never returns <code>null</code>.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Returns the target product component type or <code>null</code> if either this relation hasn't
     * got a target or the target does not exists.
     * 
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public IProductCmptType findTargetProductCmptType(IIpsProject project) throws CoreException;

    /**
     * Returns <code>true</code> if this association constrains a policy component type association,
     * otherwise <code>false</code>. If this method returns <code>true</code>,
     * {@link #findMatchingPolicyCmptTypeAssociation(IIpsProject)} returns the constrained
     * association, otherwise the finder method returns <code>null</code>.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the matching association.
     */
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the corresponding policy component type association or <code>null</code> if no such
     * association is found. If both {@link #getMatchingAssociationSource()} and
     * {@link #getMatchingAssociationName()} are not empty, the explicitly specified matching
     * association will be returned. Otherwise this method would try to find a matching association
     * automatically by comparing the associations target and order.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the matching association.
     */
    public IPolicyCmptTypeAssociation findMatchingPolicyCmptTypeAssociation(IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns the corresponding policy component type association that is set per default or
     * <code>null</code> if no such association is found. This method would try to find a matching
     * association automatically by comparing the associations target and order.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the matching association.
     */
    public IPolicyCmptTypeAssociation findDefaultPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException;

    /**
     * Finding all {@link IPolicyCmptTypeAssociation}s that could be configured by this
     * {@link IProductCmptTypeAssociation}. In normal scenario these are all Associations (never
     * Detail-To-Master) from the {@link IPolicyCmptType} that is configured by the source of this
     * association to the {@link IPolicyCmptType} that is configured by the target of this
     * association. But there could also be associations in {@link PolicyCmptType}s that are not
     * configured by any {@link IProductCmptType}. This scenario is described in FIPS-563.
     * 
     * @param ipsProject The {@link IIpsProject} used as search base project
     * @return the list of all {@link IPolicyCmptTypeAssociation} that could potentially be
     *         configured by this {@link IProductCmptTypeAssociation}
     * 
     * @throws CoreException In case of a core exception while loading the objects and resources
     */
    Set<IPolicyCmptTypeAssociation> findPossiblyMatchingPolicyCmptTypeAssociations(IIpsProject ipsProject)
            throws CoreException;

    /**
     * Setting the name of the explicitly specified matching association. If the matching
     * association should be found automatically this field should be set to
     * {@link StringUtils#EMPTY}
     * <p>
     * The policy component type of the explicitly matching association is set by
     * {@link #setMatchingAssociationSource(String)}
     * 
     * @param matchingAssociationName the name of the matching association
     */
    void setMatchingAssociationName(String matchingAssociationName);

    /**
     * Getting the name of the explicitly specified matching association. If the matching
     * association should be found automatically, this field is empty.
     * <p>
     * The explicitly matching association is located in the policy component type specified by
     * {@link #getMatchingAssociationSource()}
     * 
     * @return The name of the matching association
     */
    String getMatchingAssociationName();

    /**
     * Setting the qualified name of the explicitly specified matching association source. If the
     * matching association should be found automatically, this field should be set to
     * {@link StringUtils#EMPTY}.
     * <p>
     * The name of the explicitly matching association is set by
     * {@link #setMatchingAssociationName(String)}
     * 
     * @param matchingAssociationSource The name of the policy component type which association
     *            should be constrained
     */
    void setMatchingAssociationSource(String matchingAssociationSource);

    /**
     * Getting the qualified name of the policy component type for the explicitly specified matching
     * association. If the matching association should be found automatically, this field should be
     * empty.
     * <p>
     * The name of the explicitly matching association is get by
     * {@link #getMatchingAssociationName()}
     * 
     * @return The qualified name of the policy component type which association is constrained
     */
    String getMatchingAssociationSource();

    /**
     * Returns whether this association is marked as changing over time (<code>true</code>) or as
     * static (<code>false</code>). Instances of changing-over-time associations (
     * {@link IProductCmptLink product component links}) are part of {@link IProductCmptGeneration
     * product component generations}. Each generation may specify a different target component.
     * Instances of static associations are part of the {@link IProductCmpt product component}
     * directly.
     * 
     * @return <code>true</code> if instances of this associations change over time,
     *         <code>false</code> if they are static.
     */
    boolean isChangingOverTime();

    /**
     * Marks this association as changing over time (<code>true</code>) or static (
     * <code>false</code>). Instances of changing-over-time associations ({@link IProductCmptLink
     * product component links}) will be part of {@link IProductCmptGeneration product component
     * generations}. Each generation may specify a different target component. Instances of static
     * associations will be part of the {@link IProductCmpt product component} directly.
     * 
     * @param changingOverTime <code>true</code> if instances of this associations change over time,
     *            <code>false</code> if they are static.
     */
    void setChangingOverTime(boolean changingOverTime);

    /**
     * Returns whether this association is visible or not.
     * <p>
     * If this method returns false, the association will not be displayed in the component editor
     * and its value cannot be modified in the editor. If the method returns true, the association
     * will be displayed in the editor and can be edited by the user.
     * <p>
     * The default value is true.
     * 
     * @return true if the association is visible, false otherwise
     */
    boolean isVisible();

    /**
     * Setting the property <code>visible</code> for this association.
     * <p>
     * If this association is marked as visible, the association will be displayed in the component
     * editor. If not marked as visible, the association will not be displayed in the component
     * editor.
     * <p>
     * This flag is useful in combination with overwritten attributes when an insurance class needs
     * the association but does not need to modify its value.
     * 
     * @param visible true to mark the association as visible, false to mark it as invisible
     */
    void setVisible(boolean visible);

}
