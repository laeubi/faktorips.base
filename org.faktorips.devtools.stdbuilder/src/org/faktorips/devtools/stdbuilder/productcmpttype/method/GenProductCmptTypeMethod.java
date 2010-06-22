/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpttype.method;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.type.GenMethod;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * 
 * @author Daniel Hohenberger
 */
public class GenProductCmptTypeMethod extends GenMethod {

    public GenProductCmptTypeMethod(GenProductCmptType genProductCmptType, IProductCmptTypeMethod method) {
        super(genProductCmptType, method, new LocalizedStringsSet(GenProductCmptTypeMethod.class));
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (generatesInterface) {
            generateMethodInterface(builder, ipsProject);
        } else {
            generateMethodImpl(builder, ipsProject);
        }
    }

    private void generateMethodImpl(JavaCodeFragmentBuilder methodsBuilder, IIpsProject ipsProject)
            throws CoreException {

        if (((IProductCmptTypeMethod)getMethod()).isFormulaSignatureDefinition()) {
            if (getGenType().getBuilderSet().getFormulaCompiling().compileToXml()) {
                generateFormulaEvaluatorCall(methodsBuilder, ipsProject);
            } else {
                if (isPublished()) {
                    /*
                     * Nothing to do, signature is generated by the interface builder,
                     * implementation by the product component builder.
                     */
                } else {
                    methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(),
                            JavaSourceFileBuilder.ANNOTATION_GENERATED);
                    generateSignatureForModelMethod(true, false, methodsBuilder, ipsProject);
                    methodsBuilder.append(';');
                }
            }
            if (((IProductCmptTypeMethod)getMethod()).isOverloadsFormula()) {
                IProductCmptTypeMethod overloadedFormulaMethod = ((IProductCmptTypeMethod)getMethod())
                        .findOverloadedFormulaMethod(ipsProject);
                methodsBuilder.appendln();
                methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(),
                        JavaSourceFileBuilder.ANNOTATION_GENERATED);
                ((GenProductCmptType)getGenType()).getGenerator(overloadedFormulaMethod)
                        .generateSignatureForModelMethod(false, false, methodsBuilder, ipsProject);
                methodsBuilder.openBracket();
                methodsBuilder.appendln("// TODO a delegation to the method " + getMethod().getSignatureString()
                        + " needs to be implemented here");
                methodsBuilder.appendln("// And make sure to disable the regeneration of this method.");
                methodsBuilder.append("throw new ");
                methodsBuilder.appendClassName(RuntimeException.class);
                methodsBuilder.appendln("(\"Not implemented yet.\");");
                methodsBuilder.closeBracket();
            }

        } else {

            if (isPublished()) {
                methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(),
                        JavaSourceFileBuilder.ANNOTATION_GENERATED);
            } else {
                methodsBuilder.javaDoc(getMethod().getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
            }

            generateSignatureForModelMethod(getMethod().isAbstract(), false, methodsBuilder, ipsProject);
            if (!getMethod().isAbstract()) {
                methodsBuilder.openBracket();
                methodsBuilder.appendln("// TODO implement method!");
                Datatype datatype = getMethod().getIpsProject().findDatatype(getMethod().getDatatype());
                if (!datatype.isVoid()) {
                    if (datatype.isValueDatatype()) {
                        methodsBuilder.appendln("return " + ((ValueDatatype)datatype).getDefaultValue() + ';');
                    } else {
                        methodsBuilder.appendln("return null;");
                    }
                }
                methodsBuilder.closeBracket();
            } else {
                methodsBuilder.append(';');
            }
        }
    }

    /**
     * Delegates the formula call to the formula evaluator.
     * 
     * Code sample:
     * 
     * formulaEvaluator.setVariable("param1", 2); return (String)
     * formulaEvaluator.evaluate("testFormula", param0);
     * 
     */
    private void generateFormulaEvaluatorCall(JavaCodeFragmentBuilder methodsBuilder, IIpsProject ipsProject)
            throws CoreException {
        ProductCmptTypeMethod productCmptTypeMethod = (ProductCmptTypeMethod)getMethod();
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureForModelMethod(false, false, methodsBuilder, ipsProject);
        methodsBuilder.openBracket();
        Datatype datatype = getMethod().findDatatype(ipsProject);
        String returnJavaType;
        if (datatype.isPrimitive()) {
            ValueDatatype wrapperType = ((ValueDatatype)datatype).getWrapperType();
            returnJavaType = wrapperType.getJavaClassName();
        } else {
            returnJavaType = datatype.getJavaClassName();
        }
        methodsBuilder.append("return (").appendClassName(returnJavaType).append(')'). //
                append("getFormulaEvaluator().evaluate(\"").append(productCmptTypeMethod.getName()).append('"');
        for (String parameterName : getMethod().getParameterNames()) {
            methodsBuilder.append(", ").append(parameterName);
        }
        methodsBuilder.appendln(");");
        methodsBuilder.closeBracket();
    }

    private void generateMethodInterface(JavaCodeFragmentBuilder methodsBuilder, IIpsProject ipsProject)
            throws CoreException {

        if (isPublished()) {
            methodsBuilder.javaDoc(getMethod().getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
            generateSignatureForModelMethod(false, false, methodsBuilder, ipsProject);
            methodsBuilder.append(';');
        }
    }

    public void generateSignatureForModelMethod(boolean isAbstract,
            boolean parametersFinal,
            JavaCodeFragmentBuilder methodsBuilder,
            IIpsProject ipsProject) throws CoreException {

        generateSignatureForModelMethod(isAbstract, parametersFinal, methodsBuilder, null, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY, ipsProject);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age) throws FormulaException
     * </pre>
     */
    public void generateSignatureForModelMethod(boolean isAbstract,
            boolean parametersFinal,
            JavaCodeFragmentBuilder methodsBuilder,
            String methodSuffix,
            String[] testParameterNames,
            String[] testParameterTypes,
            IIpsProject ipsProject) throws CoreException {

        boolean formulaTest = testParameterNames.length > 0 && testParameterTypes.length > 0 || methodSuffix != null;

        IParameter[] parameters = getMethod().getParameters();
        int modifier = getMethod().getJavaModifier() | (isAbstract ? Modifier.ABSTRACT : 0);
        boolean resolveTypesToPublishedInterface = getMethod().getModifier().isPublished();
        String returnClass = StdBuilderHelper.transformDatatypeToJavaClassName(getMethod().getDatatype(),
                resolveTypesToPublishedInterface, getGenType().getBuilderSet(), getMethod().getIpsProject());

        String[] parameterNames = null;
        if (formulaTest) {
            List<IParameter> parametersWithoutTypes = new ArrayList<IParameter>();
            for (IParameter parameter : parameters) {
                Datatype datatype = parameter.findDatatype(ipsProject);
                if (!(datatype instanceof IPolicyCmptType || datatype instanceof IProductCmptType)) {
                    parametersWithoutTypes.add(parameter);
                }
            }
            parameters = parametersWithoutTypes.toArray(new IParameter[parametersWithoutTypes.size()]);
        }
        parameterNames = BuilderHelper.extractParameterNames(parameters);
        String[] parameterTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters,
                resolveTypesToPublishedInterface, getGenType().getBuilderSet(), getMethod().getIpsProject());
        String[] parameterInSignatur = parameterNames;
        String[] parameterTypesInSignatur = parameterTypes;
        if (formulaTest) {
            // add test parameters
            parameterInSignatur = extendArray(parameterNames, testParameterNames);
            parameterTypesInSignatur = extendArray(parameterTypes, testParameterTypes);
        } else {
            parameterInSignatur = parameterNames;
            parameterTypesInSignatur = parameterTypes;
        }

        String methodName = getMethod().getName();
        // extend the method signature with the given parameter names
        if (methodSuffix != null) {
            methodName = getMethod().getName() + methodSuffix;
        }
        methodsBuilder.signature(modifier, returnClass, methodName, parameterInSignatur, parameterTypesInSignatur,
                parametersFinal);

        if (isFormulaSignatureDefinition()) {
            methodsBuilder.append(" throws ");
            methodsBuilder.appendClassName(FormulaExecutionException.class);
        }
    }

    private String[] extendArray(String[] source1, String[] source2) {
        String[] dest = new String[source1.length + source2.length];
        System.arraycopy(source1, 0, dest, 0, source1.length);
        System.arraycopy(source2, 0, dest, source1.length, source2.length);
        return dest;
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        if (isFormulaSignatureDefinition()) {
            if (!(isPublished())) {
                addMethodToGeneratedJavaElements(javaElements, generatedJavaType);
            }
        } else {
            addMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        if (isPublished()) {
            addMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

    public boolean isFormulaSignatureDefinition() {
        return ((IProductCmptTypeMethod)getMethod()).isFormulaSignatureDefinition();
    }

}
