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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Hashtable;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

/**
 * 
 * @author Thorsten Guenther
 */
public class GenerationSelectionDialog extends TitleAreaDialog {

	private IProductCmpt cmpt;
	private int choice;
	private int generationIndex;
	
	public static final int CHOICE_CREATE = 0;
	public static final int CHOICE_BROWSE = 1;
	public static final int CHOICE_SWITCH = 2;
	
	private static final String STORED_CHOICE_ID = IpsPlugin.PLUGIN_ID + ".generationSelectionDialogChoice"; //$NON-NLS-1$
	
	private Button switchButton;
	private Button createButton;
	private Button browseButton;
	
	private String generationConceptName;
	private IpsPreferences prefs;
	private Hashtable choices = new Hashtable();
	
	/**
	 * @param parentShell
	 */
	public GenerationSelectionDialog(Shell parentShell, IProductCmpt cmpt) {
		super(parentShell);
		this.cmpt = cmpt;
		prefs = IpsPlugin.getDefault().getIpsPreferences();
		generationConceptName = prefs.getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
		setBlockOnOpen(false);
	}

	/**
	 * {@inheritDoc}
	 */
	protected Control createDialogArea(Composite parent) {
		Composite workArea = (Composite) super.createDialogArea(parent);
		
		Composite selectPane = new Composite(workArea, SWT.None);
		selectPane.setLayout(new GridLayout(2, false));
		
		createButton = new Button(selectPane, SWT.RADIO);
		createButton.addSelectionListener(new MySelectionListener(null));
		Label l1 = new Label(selectPane, SWT.NONE);
		l1.setText(Messages.bind(Messages.GenerationSelectionDialog_labelCreate, generationConceptName, prefs.getFormattedWorkingDate()));
		l1.addMouseListener(new ActivateButtonOnClickListener(createButton));
		choices.put(createButton, new Integer(CHOICE_CREATE));
		
		if (cmpt.findGenerationEffectiveOn(prefs.getWorkingDate()) != null) {
			browseButton = new Button(selectPane, SWT.RADIO);
			browseButton.addSelectionListener(new MySelectionListener(null));
			Label l2 = new Label(selectPane, SWT.NONE);
			l2.setText(Messages.bind(Messages.GenerationSelectionDialog_labelBrowse, generationConceptName, prefs.getFormattedWorkingDate()));
			l2.addMouseListener(new ActivateButtonOnClickListener(browseButton));
			choices.put(browseButton, new Integer(CHOICE_BROWSE));
		}

		switchButton = new Button(selectPane, SWT.RADIO);
		Composite switchPane = new Composite(selectPane, SWT.NONE);
		switchPane.setLayout(new GridLayout(2, false));
		Label l3 = new Label(switchPane, SWT.NONE);
		l3.setText(Messages.bind(Messages.GenerationSelectionDialog_labelSwitch, generationConceptName));
		l3.addMouseListener(new ActivateButtonOnClickListener(switchButton));
		choices.put(switchButton, new Integer(CHOICE_SWITCH));
		
		Combo combo = new Combo(switchPane, SWT.DROP_DOWN);
		combo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		
			public void widgetSelected(SelectionEvent e) {
				switchButton.setSelection(true);
			}
		});
		
		IIpsObjectGeneration[] generations = cmpt.getGenerations();
		for (int i = 0; i < generations.length; i++) {
			combo.add(generations[i].getName());
		}
		combo.select(0);
		
		switchButton.addSelectionListener(new MySelectionListener(combo));
		
		String winTitle = Messages.bind(
				Messages.ProductCmptEditor_title_GenerationMissmatch, cmpt
						.getName(), generationConceptName);
		getShell().setText(winTitle);
		
		String description = Messages.bind(Messages.GenerationSelectionDialog_description, prefs
					.getFormattedWorkingDate(), generationConceptName);
		setTitle(description);
		
		initSelectionFromPreferences();
		
		return workArea;
	}

	private void initSelectionFromPreferences() {
		choice = IpsPlugin.getDefault().getPreferenceStore().getInt(STORED_CHOICE_ID);
		createButton.setSelection(choice == CHOICE_CREATE);
		if (browseButton != null) {
			browseButton.setSelection(choice == CHOICE_BROWSE);
		}
		switchButton.setSelection(choice == CHOICE_SWITCH);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == TitleAreaDialog.OK) {
			IpsPlugin.getDefault().getPluginPreferences().setValue(STORED_CHOICE_ID, choice);
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Returns the choice of the user. The returned value is one of the CHOICE-constants 
	 * of this class.
	 */
	public int getChoice() {
		return choice;
	}

	/**
	 * Returns the currently selected generation or null, if no generation
	 * was selected.
	 */
	public IProductCmptGeneration getSelectedGeneration() {
		if (generationIndex <= -1) {
			return null;
		}
		return (IProductCmptGeneration)cmpt.getGenerations()[generationIndex];
	}

	private void updateChoice(Button choosen) {
		choice = ((Integer)choices.get(choosen)).intValue();
	}
	
	private class MySelectionListener implements SelectionListener {
		private Combo data;
		
		public MySelectionListener(Combo data) {
			this.data = data;
		}
		
		public void widgetSelected(SelectionEvent e) {

			updateChoice((Button)e.widget);
			
			if (data != null) {
				GenerationSelectionDialog.this.generationIndex = data.getSelectionIndex();
			}
			else {
				GenerationSelectionDialog.this.generationIndex = -1;
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
	}
	
	private class ActivateButtonOnClickListener implements MouseListener {
		private Button toSelect;
		
		public ActivateButtonOnClickListener(Button toSelect) {
			this.toSelect = toSelect;
		}
		
		public void mouseDoubleClick(MouseEvent e) {
			// nothing to do
		}

		public void mouseDown(MouseEvent e) {
			// nothing to do
		}

		public void mouseUp(MouseEvent e) {
			if (browseButton != null) {
				browseButton.setSelection(false);
			}
			createButton.setSelection(false);
			switchButton.setSelection(false);
			toSelect.setSelection(true);
			updateChoice(toSelect);
		}
		
	}
	
	
}
