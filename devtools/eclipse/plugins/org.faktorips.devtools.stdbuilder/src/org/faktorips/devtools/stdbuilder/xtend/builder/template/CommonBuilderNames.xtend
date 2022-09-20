package org.faktorips.devtools.stdbuilder.xtend.builder.template
     * This method returns the name of the policy field if this builder doesn't have super type,
     * else returns the name of the getter method from the super type and append the String to cast
     * it to the required policy class.
     * 
     * @return the name of the policy field or super call
     */
            if(generatePublishedInterfaces) "((" + typeImplClassName + ")" + getResult() + ")" else getResult()
        )
    // Class Names
    // ---------------------------------
    def static addAssociationBuilder() { "AddAssociationBuilder" }
    // Methods in IProductComponent
    // ---------------------------------
    def static getGenerationBase(String paramName) { "getGenerationBase(" + paramName + ")" }
    // Methods in IConfigurableModelObject
    // ---------------------------------
    def static getProductComponentFromId(String argument) { "getProductComponent(" + argument + ")" }
    // Methods in IRuntimeRepository
    // ---------------------------------
    def static getProductComponent(String idParam) { "getProductComponent(" + idParam + ")" }