package org.faktorips.devtools.core.model.pctype;

/**
 *
 */
public interface ITypeHierarchy {
    
    /**
     * Returns the type this hierarchy was computed for.
     */
    public IPolicyCmptType getType();
    
    /**
     * Returns the supertype for the given type or null if the type either has
     * no supertype or the hierarchy does not contain information about that type.
     */
    public IPolicyCmptType getSupertype(IPolicyCmptType type);

    /**
     * Returns the type's supertypes by travelling up the hierarchy. The first
     * element in the array (if any) is the given type's direkt supertype.
     */
    public IPolicyCmptType[] getAllSupertypes(IPolicyCmptType type);
    
    /**
     * Returns the type's supertypes and itself. The first element in the array is
     * the type itself.
     */
    public IPolicyCmptType[] getAllSupertypesInclSelf(IPolicyCmptType type);
    
    /**
     * Returns <code>true</code> if the candidate is a supertype of the indicated subtype,
     * otherwise <code>false</code>. Returns <code>false</code> if either candidate or
     * subtype is <code>null</code>.
     */
    public boolean isSupertypeOf(IPolicyCmptType candidate, IPolicyCmptType subtype);
    
    /**
     * Returns <code>true</code> if the candidate is a subtype of the indicated subtype,
     * otherwise <code>false</code>. Returns <code>false</code> if either candidate or
     * supertype is <code>null</code>.
     */
    public boolean isSubtypeOf(IPolicyCmptType candidate, IPolicyCmptType supertype);

    /**
     * Returns all attributes of the given type either defined in the type itself
     * or any of it's supertyes found in the hierarchy.
     */
    public IAttribute[] getAllAttributes(IPolicyCmptType type);
    
    /**
     * Returns all methods of the given type either defined in the type itself
     * or any of it's supertyes found in the hierarchy.
     */
    public IMethod[] getAllMethods(IPolicyCmptType type);
    
    /**
     * Returns the attribute with the given name if either the type or one of
     * it's supertypes contains an attribute with that name. Returns <code>null</code>
     * if no attribute with the given name is found.  
     */
    public IAttribute findAttribute(IPolicyCmptType type, String attributeName);
    
    /**
     * Returns the relation with the given target role if either the type or one of
     * it's supertypes contains a relation with that target role. Returns <code>null</code>
     * if no relation with the given target role is found.  
     */
    public IRelation findRelation(IPolicyCmptType type, String targetRole);
    
    public IPolicyCmptType[] getSubtypes(IPolicyCmptType type);
}