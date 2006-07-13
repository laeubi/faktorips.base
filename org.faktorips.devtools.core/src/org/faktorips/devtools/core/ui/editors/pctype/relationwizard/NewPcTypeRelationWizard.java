/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.relationwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.UIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.memento.Memento;

/**
 * Relation wizard class.
 */
public class NewPcTypeRelationWizard extends Wizard {
	private final static int NEW_REVERSE_RELATION = 0;
	private final static int USE_EXISTING_REVERSE_RELATION = 1;
	private final static int NONE_REVERSE_RELATION = 2;
	
	/** UI controllers */
	private IpsPartUIController uiControllerRelation;
	private IpsPartUIController uiControllerReverseRelation;

	private UIToolkit uiToolkit = new UIToolkit(null);

	/** Model objects */
	private IRelation relation;
	private IRelation reverseRelation;
	private IPolicyCmptType targetPolicyCmptType;
	
	/** State variables */
	private Memento mementoTargetBeforeNewRelation;
	
	// stores if a new an existing or none reverse relation will be created/used on the target
	private int reverseRelationManipulation = NONE_REVERSE_RELATION;
	
	// stores if the chosen target was dirty before manipulated by this wizard
	private boolean targetIsDirty;
	
	// True if the next pages after the property page was displayed.
	// The first time next can only be clicked if the relation is valid,
	// but aftwerwards the next button can be used every time, because
	// maybe the validation error is on a page after the property page
	private boolean reverseRelationPageDisplayed = false;
	
	private boolean isError = false;
	
	/** Wizard pages */
	private ReverseRelationPropertiesPage reverseRelationPropertiesPage;
	private ErrorPage errorPage;
	private List pages = new ArrayList();
	
	public NewPcTypeRelationWizard(IRelation relation) {
		super();
		super.setWindowTitle(Messages.NewPcTypeRelationWizard_title);
		this.relation = relation;

		uiControllerRelation = createUIController(this.relation);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void addPages() {
		try {
			WizardPage relationTargetPage = new RelationTargetPage(this);
			WizardPage containerRelationPagePage = new ContainerRelationPage(
					this);
			WizardPage propertiesPage = new PropertiesPage(this);
			WizardPage reverseRelationPage = new ReverseRelationPage(this);
			reverseRelationPropertiesPage = new ReverseRelationPropertiesPage(
					this);
			errorPage = new ErrorPage(this);
			
			pages.add(relationTargetPage);
			addPage(relationTargetPage);

			pages.add(containerRelationPagePage);
			addPage(containerRelationPagePage);

			pages.add(propertiesPage);
			addPage(propertiesPage);

			pages.add(reverseRelationPage);
			addPage(reverseRelationPage);

			pages.add(reverseRelationPropertiesPage);
			addPage(reverseRelationPropertiesPage);
			
			pages.add(errorPage);
			addPage(errorPage);			
		} catch (Exception e) {
			IpsPlugin.logAndShowErrorDialog(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		AbstractPcTypeRelationWizardPage nextPage = null;

		if (isError)
			// in case of an error no next page will be displayed
			return null;
		
		int index = pages.indexOf(page);
		if (index == pages.size() - 1 || index == -1)
			// last page or page not found
			return null;
		nextPage = (AbstractPcTypeRelationWizardPage) pages
				.get(index + 1);
		while (!nextPage.isPageVisible()) {
			index++;
			if (index == pages.size() - 1)
				// last page
				return null;
			nextPage = (AbstractPcTypeRelationWizardPage) pages.get(index + 1);
		}

		return (IWizardPage) nextPage;
	}
    
	/**
	 * Save changes and check if the source file of the target is dirty,
	 * if the source file of the target is dirty then a dialog is shown to ask for automatically saving.
	 * 
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		try {
			boolean saveTargetAutomatically = false;
			if (targetPolicyCmptType != null){
				if (targetIsDirty){
					// target policy component type was dirty before editing by the wizard,
					//   ask to save automatically
					String msg = Messages.NewPcTypeRelationWizard_target_askForAutomaticallySaving;
					saveTargetAutomatically = MessageDialog.openQuestion(getShell(),
							Messages.NewPcTypeRelationWizard_target_askForAutomaticallySavingTitle, msg);
				}else{
					// target policy component type is not dirty, therefore save the changed on the target
					saveTargetAutomatically = true;
				}
			}
			
			// Note: if the target policy component type source file is open in an other editor,
			// the first changes will be refreshed correctly in the open editor 
			// after automatically saving; remark: the refresh works only if the refresh functionality is enabled for alle editor
			// views (not only the active one) this feature is currently not enabled.
			// But after this saving, the content (the currently displayed object) within this target editor 
			// will be detached from the model, because the editor control holds a copy of the object
			// and after saving the object, the cache for this object will be destroyed 
			// and therefore the copy will not be updated any more.
			// The consequence is that all further background changes on this object will not be displayed in the target editor.
			// Only if the target editor will be reopened then the current object is displayed correctly.
			if (saveTargetAutomatically) {
				targetPolicyCmptType.getIpsSrcFile().save(true, null);
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
			showErrorPage(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
    public boolean performCancel() {
    	restoreMementoTargetBeforeChange();    	
    	return true;
    }
	
	/**
	 * Set the default values depending on the relation type and read-only container flag.
	 */
	void setDefaultsByRelationType(IRelation newRelation){
		RelationType type = newRelation.getRelationType();
		if (type != null) {
			if (type.isComposition()) {
				newRelation.setMaxCardinality(Integer.MAX_VALUE);
				newRelation.setProductRelevant(newRelation.getPolicyCmptType().isConfigurableByProductCmptType());
			} else if (type.isReverseComposition()) {
				newRelation.setMinCardinality(1);
				newRelation.setMaxCardinality(1);
				newRelation.setTargetRolePluralProductSide(""); //$NON-NLS-1$
				newRelation.setTargetRoleSingularProductSide(""); //$NON-NLS-1$
				newRelation.setProductRelevant(false);
			} else if (type.isAssoziation()) {
				newRelation.setContainerRelation(""); //$NON-NLS-1$
				newRelation.setReadOnlyContainer(false);
				if (newRelation.isReadOnlyContainer()){
					newRelation.setMaxCardinality(Integer.MAX_VALUE);
				}
				newRelation.setProductRelevant(newRelation.getPolicyCmptType().isConfigurableByProductCmptType());
			}
		}
	}
	
	/**
	 * Return the ui toolkit.
	 */
	UIToolkit getUiToolkit() {
		return uiToolkit;
	}
	
	/**
	 * Add an edit field to the relation ui controller linked by the given property.
	 * If the ui controller not exists yet nothing will be added.
	 */
	void addToUiControllerRelation(EditField edit, String propertyName) {
		if (uiControllerRelation != null)
			uiControllerRelation.add(edit, propertyName);
	}

	/**
	 * Add an edit field to the reverse relation ui controller linked by the given property.
	 * If the ui controller not exists yet nothing will be added
	 */
	void addToUiControllerReverseRelation(EditField edit, String propertyName) {
		if (uiControllerReverseRelation != null)
			uiControllerReverseRelation.add(edit, propertyName);
	}
	
	/**
	 * Removes an edit field from the reverse relation ui controller linked by the given property.
	 * If the ui controller not exists yet nothing will be added
	 */
	void removeFromUiControllerReverseRelation(EditField edit) {
		if (uiControllerReverseRelation != null)
			uiControllerReverseRelation.remove(edit);
	}

	/**
	 * Stores a new memento  for the target relation.
	 */
	void storeMementoTargetBeforeChange(){
		if (targetPolicyCmptType==null)
			return;
		
		mementoTargetBeforeNewRelation = targetPolicyCmptType.newMemento();
	}
	
	/**
	 * Restores the target relation memento.
	 */	
	void restoreMementoTargetBeforeChange(){
		if (targetPolicyCmptType==null || mementoTargetBeforeNewRelation == null)
			return;
		targetPolicyCmptType.setState(mementoTargetBeforeNewRelation);
		mementoTargetBeforeNewRelation = null;
	}
	
	/**
	 * Create a new ui controller for a reverse relation.
	 */
	void createUIControllerReverseRelation(IRelation newReverseRelation){
		uiControllerReverseRelation = createUIController(newReverseRelation);
		reverseRelationPropertiesPage.connectToModel();
	}
		
	/**
	 * Udates the description on the reverse relation property page.
	 */
	void updateDescriptionReverseRelationPropertiesPage(String description){
		reverseRelationPropertiesPage.setDescription(description);
	}
	
	/**
	 * Returns the relation object ui controller.
	 * Or null if the controller not exists.
	 */
	UIController getUiControllerRelation() {
		return uiControllerRelation;
	}
	
	/**
	 * Returns the relation object ui controller.
	 * Or null if the controller not exists.
	 */
	UIController getUiControllerReverseRelation() {
		return uiControllerReverseRelation;
	}
	
	/**
	 * Returns the relation.
	 */
	IRelation getRelation() {
		return relation;
	}

	/**
	 * Returns the reverse relation.
	 */
	IRelation getReverseRelation() {
		return reverseRelation;
	}
	
	/**
	 * Stores a reverse relation.
	 * Additional the correct reverse relation names will be set in both relation.
	 */
	void storeReverseRelation(IRelation reverseRelation){
		if (reverseRelation != null){
			relation.setReverseRelation(reverseRelation.getTargetRoleSingular());
			reverseRelation.setReverseRelation(relation.getTargetRoleSingular());
		}
		this.reverseRelation = reverseRelation;
	}
	
    /**
	 * Returns the qualified name of the currently editing policy component
	 * type.
	 */
    String getPolicyCmptTypeQualifiedName() {
		return relation.getPolicyCmptType().getQualifiedName();
	}

	/**
	 * Returns the policy component type of the target.
	 */
	IPolicyCmptType getTargetPolicyCmptType() {
		return targetPolicyCmptType;
	}

	/**
	 * Stores the policy component type object of the target.
	 * And check if the target source file is dirty to ask the
	 * user about the automatically saving when finishing the wizard.
	 */
	void storeTargetPolicyCmptType(IPolicyCmptType targetPolicyCmptType) {
		this.targetPolicyCmptType = targetPolicyCmptType;
		if (targetPolicyCmptType != null)
			targetIsDirty = targetPolicyCmptType.getIpsSrcFile().isDirty();
	}
	
	/**
	 * Returns true if the revese relation is an existing relation on the target.
	 */
	boolean isExistingReverseRelation() {
		return reverseRelationManipulation == USE_EXISTING_REVERSE_RELATION;
	}	
	
	/**
	 * Returns true if the revese relation is new relation on the target.
	 */
	boolean isNewReverseRelation() {
		return reverseRelationManipulation == NEW_REVERSE_RELATION;
	}

	/**
	 * Sets that the revese relation is an existing relation on the target.
	 */
	void setExistingReverseRelation() {
		reverseRelationManipulation = USE_EXISTING_REVERSE_RELATION;
	}	
	
	/**
	 * Sets that the revese relation is a new relation on the target.
	 */
	void setNewReverseRelation() {
		reverseRelationManipulation = NEW_REVERSE_RELATION;
	}	
	
	/**
	 * Sets that the revese relation will not defined by using this wizard.
	 */
	void setNoneReverseRelation() {
		reverseRelationManipulation = NONE_REVERSE_RELATION;
	}	
	
	/**
	 * Returns the corresponding relation type
	 */
	RelationType getCorrespondingRelationType(RelationType sourceRelType){
		return sourceRelType.isAssoziation()        ? RelationType.ASSOZIATION :
			   sourceRelType.isReverseComposition() ? RelationType.COMPOSITION :
			   sourceRelType.isComposition()        ? RelationType.REVERSE_COMPOSITION : null;
	}

	/** 
	 * Creates a new ui controller for the given object.
	 */
	private IpsPartUIController createUIController(IIpsObjectPart part) {
		IpsPartUIController controller = new IpsPartUIController(part) {
			public void valueChanged(FieldValueChangedEvent e) {
				try {
					super.valueChanged(e);
				} catch (Exception ex) {
					IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
		};
		return controller;
	}

	/**
	 * True if the reverse property page was displayed.
	 */
	public boolean isReverseRelationPageDisplayed() {
		return reverseRelationPageDisplayed;
	}

	/**
	 * Sets if the reverse property page was displayed.
	 */
	public void setReverseRelationPageDisplayed(boolean isNextPageDisplayed) {
		this.reverseRelationPageDisplayed = isNextPageDisplayed;
	}

	/**
	 * Shows the error page.
	 */
	public void showErrorPage(Exception e) {
		isError = true;
		errorPage.storeErrorDetails(e.getLocalizedMessage());
		getContainer().showPage(errorPage);
		getContainer().updateButtons();
	}

	/**
	 * Returns true if there was an error.
	 */
	public boolean isError() {
		return isError;
	}
}
