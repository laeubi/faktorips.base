package org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmptbuilder.XPolicyBuilder


import org.faktorips.devtools.stdbuilder.xmodel.policycmptbuilder.XPolicyBuilderAssociation
import static org.faktorips.devtools.stdbuilder.xtend.builder.template.CommonBuilderNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class PolicyAssociationBuilderTmpl{


    def static body (XPolicyBuilder it) '''
        «IF builderAssociations.size > 0»
            /**
            *«localizedJDoc("CLASS_BUILDER")»
            *
            * @generated
            */
            public static class «associationBuilder()» «extendSuperclassAssociation(it)» {

                «policyBuilderField»

                «constructorWithPolicyBuilder(false, it)»

                «FOR association : builderAssociations» «associationEvaluation(association)»«ENDFOR»

                «get»

                «getRepository(it)»
            }
        «ENDIF»

    «IF builderAssociations.size > 0 || superBuilderAssociations.size > 0»
            /**
            *«localizedJDoc("CLASS_BUILDER_MULTI", implClassName)»
            *
            * @generated
            */
            public static class «addAssociationBuilder()» «extendSuperclassAssociations»{

                «policyBuilderField»

                «constructorWithPolicyBuilder(true, it)»

                «FOR association : builderAssociations» «associationsEvaluation(it, false, association)» «ENDFOR»
                «««for inherited associations
                «FOR association : superBuilderAssociations» «associationsEvaluation(it, true, association)» «ENDFOR»


                /**
                * @generated
                */
                «overrideAnnotationIf(hasSuperAssociationBuilder)»
                protected «implClassName» done() {
                    «IF hasSuperAssociationBuilder»
                        return («implClassName») super.done();
                    «ELSE»
                        return policyBuilder;
                    «ENDIF»
                }

                «get»

                «getRepository(it)»
            }
        «ENDIF»
    '''

    //Adds the extension of the super class of the policy
    def private static extendSuperclassAssociation (XPolicyBuilder it) '''
        «IF hasSuperAssociationBuilder»
            extends «superBuilderForAssociationBuilder.implClassName».AssociationBuilder
        «ENDIF»
    '''

    // Adds the extension of the super class of the policy
    def private static extendSuperclassAssociations (XPolicyBuilder it) '''
        «IF hasSuperAssociationBuilder»
            extends «supertype.implClassName».«addAssociationBuilder»
        «ENDIF»
    '''


    //    An association setter is only generated for associations that are not derived or inverse composition.
    //    If this is the case, an association setter with a generic subtype of the target class is always generated.
    //    This makes it possible to set a target that is known to the workspace.
    //    If the target is in addition not abstract, a setter is generated to directly instantiate the target class.


    def private static associationEvaluation(XPolicyBuilder sourceBuilder, XPolicyBuilderAssociation it) '''
        ««« for generic subtype
        «associationSetterWithType(sourceBuilder, it, false, false, targetBuilderClass)»

        «IF !it.targetBuilderClass.abstract»
            «associationSetter(sourceBuilder, it, false, false, targetBuilderClass)»
        «ENDIF»
    '''

    def private static associationsEvaluation(XPolicyBuilder builder, boolean isSuper, XPolicyBuilderAssociation it) '''

        «standardAssociationSetter(builder, it, isSuper, targetBuilderClass)»

        «associationSetterWithType(builder, it, true, isSuper, targetBuilderClass)»

        «IF !targetBuilderClass.abstract»
            «associationSetter(builder, it, true, isSuper, targetBuilderClass)»
        «ENDIF»
    '''

    def private static standardAssociationSetter(XPolicyBuilder builder, XPolicyBuilderAssociation association, boolean isSuper, XPolicyBuilder it) '''
        /**
        *«localizedJDoc("METHOD_ASS_STD", policyName, association.name)»
        *
        * @generated
        */
        «IF isSuper || association.needOverrideForConstrainNewChildMethod && association.targetSameAsOverwrittenAssociationsTarget»@Override«ENDIF»
        public «builder.implClassName» «association.method(association.methodName, policyPublishedInterfaceName, "targetPolicy")»{
            «IF isSuper»
                super.«association.methodName»(targetPolicy);
            «ELSE»
                «getResult()».«association.methodNameSetOrAdd»(targetPolicy);
            «ENDIF»
            return done();
        }
    '''

    //Association target setter for generic subtype of the target. see associationEvaluation. super:wether this is for inherited associations
    def private static associationSetterWithType(XPolicyBuilder sourceBuilder, XPolicyBuilderAssociation association, boolean multi, boolean isSuper, XPolicyBuilder it) '''
        /**
        *«localizedJDoc("METHOD_ASS_TYPE", policyName,  association.name)»
        *
        * @generated
        */
        «IF isSuper || association.needOverrideForConstrainNewChildMethod && association.targetSameAsOverwrittenAssociationsTarget»@Override«ENDIF»
        public «returnType(sourceBuilder, multi, true, it)» «association.methodName»(«targetBuilderType(multi, it)» targetBuilder){
            «IF isSuper»
                super.«association.methodName»(targetBuilder);
            «ELSE»
                «getResult()».«association.methodNameSetOrAdd»(targetBuilder.«getResult()»);
            «ENDIF»
            return «returnValue(multi, it)»;
        }
    '''

    // Create and set association target for non abstract targets
    def private static associationSetter(XPolicyBuilder sourceBuilder, XPolicyBuilderAssociation association, boolean multi, boolean isSuper, XPolicyBuilder it) '''
        /**
        *«localizedJDoc("METHOD_ASS", policyName, association.name)»
        *
        * @generated
        */
        «IF isSuper || (association.needOverrideForConstrainNewChildMethod && association.overwrittenTargetNotAbstract)» @Override «ENDIF»
        public «returnType(sourceBuilder, multi, false, it)» «association.method(association.methodName)»{
            «IF isSuper»
                super.«association.methodName»();
                return done();
            «ELSE»
                «IF generatePublishedInterfaces»
                    «implClassName» targetBuilder = new «factoryImplClassName»().«builder()»;
                «ELSE»
                    «implClassName» targetBuilder = «typeImplClassName».«builder()»;
                «ENDIF»
                «getResult()».«association.methodNameSetOrAdd»(targetBuilder.«getResult()»);
                return «returnValue(multi, it)»;
            «ENDIF»
        }

        «IF configured»
            /**
            *«localizedJDoc("METHOD_ASS_PROD", policyName, association.name)»
            *
            * @generated
            */
            «IF isSuper || (association.needOverrideForConstrainNewChildMethod && association.overwrittenNotAbstractConfigured)» @Override «ENDIF»
            public «returnType(sourceBuilder, multi, false, it)» «association.method(association.methodName, "String", "productCmptId")»{
                «IF isSuper»
                    super.«association.methodName»(productCmptId);
                    return done();
                «ELSE»
                    if(«getRepository» == null) {
                        throw new «RuntimeException»("«localizedText("EXCEPTION_NO_REPOSITORY")»");
                    }
                    «implClassName» targetBuilder = null;
                    «IF sourceBuilder.configured && productCmptNode.changingOverTime»
                        if(«getResult».getEffectiveFromAsCalendar() != null){
                            «IF generatePublishedInterfaces»
                                targetBuilder = new «factoryImplClassName»().«builder(getRepository, "productCmptId", getResult + ".getEffectiveFromAsCalendar()")»;
                            «ELSE»
                                targetBuilder = «typeImplClassName».«builder(getRepository, "productCmptId", getResult + ".getEffectiveFromAsCalendar()")»;
                            «ENDIF»
                        } else{
                    «ENDIF»
                        «IF generatePublishedInterfaces»
                            targetBuilder = new «factoryImplClassName»().«builder(getRepository, "productCmptId")»;
                        «ELSE»
                            targetBuilder = «typeImplClassName».«builder(getRepository, "productCmptId")»;
                        «ENDIF»
                    «IF sourceBuilder.configured && productCmptNode.changingOverTime»
                        }
                    «ENDIF»
                    «getResult».«association.methodNameSetOrAdd»(targetBuilder.«getResult»);
                    return «returnValue(multi, it)»;
                «ENDIF»
            }
        «ENDIF»
    '''

    //Associtionbuilder returns the target policy builder while associatonSbuilder returns the origin builder again.
    def private static returnType(XPolicyBuilder builder, boolean multi, boolean generic, XPolicyBuilder it) '''
        «IF multi» «builder.implClassName»
        «ELSEIF generic»<T extends «implClassName»> T
        «ELSE»«implClassName»
        «ENDIF»
    '''

    def private static targetBuilderType(boolean multi, XPolicyBuilder it) '''
        «IF multi»«implClassName»
        «ELSE»T
        «ENDIF»
    '''

    def private static returnValue(boolean multi, XPolicyBuilder it) '''
        «IF multi» done()
        «ELSE»    targetBuilder
        «ENDIF»
    '''

    def private static policyBuilderField (XPolicyBuilder it) '''
        «IF !hasSuperAssociationBuilder()»
            /**
            * @generated
            */
            private «implClassName» policyBuilder;
        «ENDIF»
    '''

    def private static constructorWithPolicyBuilder(boolean multi, XPolicyBuilder it) '''
        /**
        * @generated
        */
        protected «IF multi»«addAssociationBuilder»«ELSE»«associationBuilder»«ENDIF»(«implClassName» policyBuilder){
            «IF hasSuperAssociationBuilder»
                super(policyBuilder);
            «ELSE»
                this.policyBuilder = policyBuilder;
            «ENDIF»
        }
    '''

    def private static get (XPolicyBuilder it) '''
        /**
        * @generated
        */
        «IF hasSuperAssociationBuilder»@Override«ENDIF»
        protected «policyPublishedInterfaceName» «getResult» {
            «IF hasSuperAssociationBuilder»
                return («policyPublishedInterfaceName») super.«getResult»;
            «ELSE»
                return policyBuilder.«getResult»;
            «ENDIF»
        }
    '''

    def private static getRepository (XPolicyBuilder it) '''
        /**
        * @generated
        */
        «IF !hasSuperAssociationBuilder»
        protected «IRuntimeRepository» «getRepository» {
            return policyBuilder.«getRepository»;
        }
        «ENDIF»
    '''
}