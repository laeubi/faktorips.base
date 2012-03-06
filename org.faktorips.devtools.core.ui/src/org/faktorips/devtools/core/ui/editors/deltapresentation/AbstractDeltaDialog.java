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

package org.faktorips.devtools.core.ui.editors.deltapresentation;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * @author dirmeier
 */
public abstract class AbstractDeltaDialog extends TitleAreaDialog {

    protected UIToolkit toolkit;

    public AbstractDeltaDialog(Shell parentShell) {
        super(parentShell);
        toolkit = new UIToolkit(null);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control buttons = super.createButtonBar(parent);
        super.getButton(OK).setText(Messages.AbstractDeltaDialog_Button_Fix);
        super.getButton(CANCEL).setText(Messages.AbstractDeltaDialog_Button_Ignore);
        return buttons;
    }

    protected void updateDeltaView(Object delta) {
        getTreeViewer().setInput(delta);
        getTreeViewer().refresh();
        getTreeViewer().expandAll();
        getTreeViewer().getControl().redraw();
        getParentShell().layout();
    }

    protected abstract TreeViewer getTreeViewer();

}