package org.faktorips.devtools.htmlexport.test.documentor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.Documentor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.helper.html.HtmlLayouter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public abstract class AbstractFipsDocTest extends AbstractIpsPluginTest {

	protected static final String FIPSDOC_GENERIERT_HOME = "/home/dicker/fipsdoc/generiert";
	protected IIpsProject ipsProject;
	protected DocumentorConfiguration documentorConfig;
	protected Documentor documentor;

	public AbstractFipsDocTest() {
		super();
	}

	public AbstractFipsDocTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ipsProject = newIpsProject("TestProjekt");

		documentorConfig = new DocumentorConfiguration();
		documentorConfig.setPath(FIPSDOC_GENERIERT_HOME);
		documentorConfig.setIpsProject(ipsProject);
		documentorConfig.setLayouter(new HtmlLayouter());

		documentor = new Documentor(documentorConfig);
	}

	protected void createStandardProjekt() {
		try {
			newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
			newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
			newPolicyCmptType(ipsProject, "BVB");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public void testCreateMassivProjektOhneValidierungsFehler() {
		createMassivProjekt();

		MessageList ml;
		try {
			List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
			ipsProject.findAllIpsSrcFiles(srcFiles);

			ml = new MessageList();
			for (IIpsSrcFile ipsSrcFile : srcFiles) {
				ml.add(ipsSrcFile.getIpsObject().validate(ipsProject));
			}
			
			assertEquals(ml.toString(), 0, ml.getNoOfMessages(Message.ERROR));

		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	protected void createMassivProjekt() {
		try {
			PolicyCmptType vertrag = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
			PolicyCmptType lvb = newPolicyAndProductCmptType(ipsProject, "LVB", "StandardLVB");
			PolicyCmptType baseBVB = newPolicyAndProductCmptType(ipsProject, "base.BVB", "base.BVBArt");
			PolicyCmptType versObj = newPolicyAndProductCmptType(ipsProject, "base.versobj.VersObj",
					"base.versobj.VersObjArt");

			IPolicyCmptTypeAssociation assoLvbBvb = lvb.newPolicyCmptTypeAssociation();
			assoLvbBvb.setTarget(baseBVB.getQualifiedName());
			assoLvbBvb.setQualified(true);
			assoLvbBvb.setMinCardinality(2);
			assoLvbBvb.setMaxCardinality(5);
			assoLvbBvb.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
			assoLvbBvb.setTargetRolePlural("BVBs");
			assoLvbBvb.setTargetRoleSingular("BVB");

			IPolicyCmptTypeAssociation assoBvbLvb = assoLvbBvb.newInverseAssociation();
			assoBvbLvb.setTargetRoleSingular("LVB");
			assoBvbLvb.setTargetRolePlural("LVBs");

			IPolicyCmptTypeAssociation assoLvbVersObj = lvb.newPolicyCmptTypeAssociation();
			assoLvbVersObj.setTarget(versObj.getQualifiedName());
			assoLvbVersObj.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
			assoLvbVersObj.setDescription("Das übliche Blabla");
			assoLvbVersObj.setTargetRoleSingular("Versichertes Objekt");

			IPolicyCmptTypeAssociation assoVersObjLvb = assoLvbVersObj.newInverseAssociation();
			assoVersObjLvb.setTargetRoleSingular("LVB");
			assoVersObjLvb.setTargetRolePlural("LVBs");

			IPolicyCmptTypeAssociation assoVertragLvb = vertrag.newPolicyCmptTypeAssociation();
			assoVertragLvb.setTarget(lvb.getQualifiedName());
			assoVertragLvb.setTargetRoleSingular("LVB");
			assoVertragLvb.setTargetRolePlural("LVBs");

			IPolicyCmptTypeAssociation assoLvbVertrag = assoVertragLvb.newInverseAssociation();
			assoLvbVertrag.setTargetRoleSingular("Vertrag");
			assoLvbVertrag.setTargetRolePlural("Verträge");

			PolicyCmptType baseSubBVB = newPolicyAndProductCmptType(ipsProject, "base.sub.SubBVB", "base.sub.SubBVBArt");
			baseSubBVB.setSupertype(baseBVB.getQualifiedName());

			PolicyCmptType krankenBVB = newPolicyAndProductCmptType(ipsProject, "kranken.KrankenBVB",
					"kranken.KrankenBVBArt");
			krankenBVB.setSupertype(baseSubBVB.getQualifiedName());

			krankenBVB.setDescription("blablablabla sdfishiurgh sdfiugfughs \n\nodfiug sodufhgosdfzgosdfgsdfg \nENDE");

			addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Text1", "String", Modifier.PUBLISHED,
					AttributeType.CHANGEABLE);
			addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Geld2", "Money", Modifier.PUBLIC,
					AttributeType.CONSTANT);
			addPolicyCmptTypeAttribute(krankenBVB.newPolicyCmptTypeAttribute(), "Zahl3", "Integer", Modifier.PUBLIC,
					AttributeType.DERIVED_ON_THE_FLY);
			IPolicyCmptTypeAttribute attributeZahl3 = krankenBVB.getPolicyCmptTypeAttribute("Zahl3");
			
			IProductCmptTypeMethod methodCompZahl3 = ipsProject.findProductCmptType(krankenBVB.getProductCmptType()).newProductCmptTypeMethod();
			String methodNameZahl3 = "berechneZahl3";
			methodCompZahl3.setName(methodNameZahl3);
			methodCompZahl3.setDatatype("Integer");
			methodCompZahl3.setFormulaSignatureDefinition(false);
			attributeZahl3.setComputationMethodSignature(methodNameZahl3 + "()");
			

			ProductCmptType newProductCmp = (ProductCmptType) ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE,
					krankenBVB.getProductCmptType());
			newProductCmp.setPolicyCmptType(krankenBVB.getQualifiedName());
			newProductCmp.setSupertype(baseSubBVB.getProductCmptType());

			addAttribute(newProductCmp.newAttribute(), "Geld1", "Money", Modifier.PUBLIC);
			addAttribute(newProductCmp.newAttribute(), "Zahl2", "Integer", Modifier.PUBLISHED);
			addAttribute(newProductCmp.newAttribute(), "Text3", "Money", Modifier.PUBLIC);
			newProductCmp
					.setDescription("Produkt blabla\nblablablabla sdfishiurgh sdfiugfughs \n\nodfiug sodufhgosdfzgosdfgsdfg \nENDE");

			newPolicyAndProductCmptType(ipsProject, "kranken.sub.KrankenSubBVB", "kranken.sub.KrankenSubBVBArt");

		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void addPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute policyAttribute, String name, String datatype,
			Modifier modifier, AttributeType attributeType) {
		addAttribute(policyAttribute, name, datatype, modifier);

		policyAttribute.setAttributeType(attributeType);
		policyAttribute.setProductRelevant(name.contains("l"));

	}

	private void addAttribute(IAttribute newAttribute, String name, String datatype, Modifier modifier) {
		newAttribute.setName(name);
		newAttribute.setDatatype(datatype);
		newAttribute.setModifier(modifier);
		newAttribute.setDescription(name + " - " + datatype);
	}

	protected void deletePreviousGeneratedFiles() {
		File file = new File(documentorConfig.getPath());
		if (file.exists()) {
			file.delete();
		}
	}

}