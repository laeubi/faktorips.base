package org.faktorips.devtools.stdbuilder.backup;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class ProductCmptInterfaceCuBuilder extends AbstractPcTypeBuilder {

    private final static String ATTRIBUTE_INTERFACE_GETTER_JAVADOC = "ATTRIBUTE_INTERFACE_GETTER_JAVADOC";
    private final static String ATTRIBUTE_INTERFACE_COMPUTE_JAVADOC = "ATTRIBUTE_INTERFACE_COMPUTE_JAVADOC";
    private final static String JAVA_GETTER_METHOD_MAX_VALUESET = "JAVA_GETTER_METHOD_MAX_VALUESET";
    private final static String JAVA_CREATE_POLICY_CMPT_METHOD = "JAVA_CREATE_POLICY_CMPT_METHOD";

    private PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder;

    public ProductCmptInterfaceCuBuilder(IJavaPackageStructure packageStructure, String kindId) throws CoreException {
        super(packageStructure, kindId, new LocalizedStringsSet(ProductCmptInterfaceCuBuilder.class));
        setMergeEnabled(true);
    }

    public void setPolicyCmptTypeInterfaceBuilder(PolicyCmptInterfaceBuilder policyCmptTypeInterfaceBuilder) {
        this.policyCmptTypeInterfaceBuilder = policyCmptTypeInterfaceBuilder;
    }

    /**
     * Overridden.
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + "Pk";
    }

    /**
     * Overridden.
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    private void buildAttributeValueSetDeclaration(JavaCodeFragmentBuilder methodsBuilder,
            IAttribute a,
            Datatype datatype,
            DatatypeHelper helper) throws CoreException {
        if (a.getValueSet().isAllValues()) {
            return;
        }

        String javaDocMax = getLocalizedText(JAVA_GETTER_METHOD_MAX_VALUESET, a.getName());
        if (a.getValueSet().isRange()) {
            methodsBuilder.methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT, helper.getRangeJavaClassName(),
                    getProductInterfaceGetMaxValueSetMethodName(a), new String[0], new String[0], javaDocMax,
                    ANNOTATION_GENERATED);
        } else {
            methodsBuilder.methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT, datatype.getJavaClassName() + "[]",
                    getProductInterfaceGetMaxValueSetMethodName(a), new String[0], new String[0], javaDocMax,
                    ANNOTATION_GENERATED);
        }
        methodsBuilder.append(';');
    }

    private String getProductInterfaceGetMaxValueSetMethodName(IAttribute a) {
        return "getMaxWertebereich" + StringUtils.capitalise(a.getName());
    }

    /**
     * @param a
     * @param datatype
     * @throws CoreException
     * @throws JavaModelException
     */
    private void createAttributeComputeDeclaration(JavaCodeFragmentBuilder methodsBuilder, IAttribute a, Datatype datatype) throws JavaModelException,
            CoreException {
        String methodName = "compute" + StringUtils.capitalise(a.getName());

        String javaDoc = getLocalizedText(ATTRIBUTE_INTERFACE_COMPUTE_JAVADOC, a.getName());

        Parameter[] parameters = a.getFormulaParameters();
        methodsBuilder.methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT, datatype.getJavaClassName(),
                methodName, BuilderHelper.extractParameterNames(parameters),
                BuilderHelper.transformParameterTypesToJavaClassNames(a.getIpsProject(), parameters), javaDoc,
                ANNOTATION_GENERATED);
        methodsBuilder.append(';');
    }

    /**
     * @param a
     * @param datatype
     */
    private void createAttributeGetterDeclaration(JavaCodeFragmentBuilder methodsBuilder, IAttribute a, Datatype datatype) throws CoreException {
        String methodName;
        if (a.getAttributeType() == AttributeType.CHANGEABLE) {
            methodName = getPcInterfaceGetDefaultValueMethodName(a);
        } else {
            methodName = getPcInterfaceGetValueMethodName(a);
        }
        String javaDoc = getLocalizedText(ATTRIBUTE_INTERFACE_GETTER_JAVADOC, a.getName());

        methodsBuilder.methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT, datatype.getJavaClassName(),
                methodName, new String[0], new String[0], javaDoc, ANNOTATION_GENERATED);
        methodsBuilder.append(';');
    }

    private void buildCreateMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(JAVA_CREATE_POLICY_CMPT_METHOD);
        methodsBuilder.methodBegin(Modifier.PUBLIC | Modifier.ABSTRACT,
                policyCmptTypeInterfaceBuilder.getQualifiedClassName(getPcType().getIpsSrcFile()),
                "create" + StringUtils.capitalise(getPcType().getName()), new String[0], new String[0],
                javaDoc, ANNOTATION_GENERATED);
        methodsBuilder.append(';');
        return;
    }

    // duplicate in ProductCmptImplCuBuilder and PolicyCmptTypeImplCuBuilder
    private String getPcInterfaceGetDefaultValueMethodName(IAttribute a) {
        return "getVorgabewert" + StringUtils.capitalise(a.getName());
    }
    
    // duplicate in ProductCmptImplCuBuilder
    private String getPcInterfaceGetValueMethodName(IAttribute a) {
        return "get" + StringUtils.capitalise(a.getName());
    }

    protected void assertConditionsBeforeGenerating() {
        String builderName = null;

        if (policyCmptTypeInterfaceBuilder == null) {
            builderName = PolicyCmptInterfaceBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException("One of the builders this builder depends on is not set: " + builderName);
        }
    }

    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    protected void generateOther(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        if (!getPcType().isAbstract()) {
            buildCreateMethod(memberVarsBuilder);
        }
    }

    protected boolean generatesInterface() {
        return true;
    }

    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        // TODO Auto-generated method stub

    }

    protected String getSuperclass() throws CoreException {
        return null;
    }

    protected String[] getExtendedInterfaces() throws CoreException {
        String javaSupertype = IProductComponent.class.getName();
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            IPolicyCmptType supertype = getPcType().getIpsProject().findPolicyCmptType(getPcType().getSupertype());
            javaSupertype = supertype == null ? javaSupertype : getQualifiedClassName(supertype.getIpsSrcFile());
        }
        return new String[] { javaSupertype };
    }

    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (attribute.isProductRelevant()) {
            Datatype datatype = getPcType().getIpsProject().findDatatype(attribute.getDatatype());
            if (attribute.getAttributeType() == AttributeType.COMPUTED
                    || attribute.getAttributeType() == AttributeType.DERIVED) {
                createAttributeComputeDeclaration(methodsBuilder, attribute, datatype);
            } else {
                createAttributeGetterDeclaration(methodsBuilder, attribute, datatype);
            }
            buildAttributeValueSetDeclaration(methodsBuilder, attribute, datatype, datatypeHelper);
        }
    }

    protected void generateCodeForRelation(IRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        ProductCmptInterfaceRelationBuilder relationBuilder = new ProductCmptInterfaceRelationBuilder(this);
        relationBuilder.buildRelation(methodsBuilder, relation);
    }

    /**
     * Empty implementation.
     */
    protected void generateCodeForContainerRelations(IRelation containerRelation,
            IRelation[] subRelations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
    }
}