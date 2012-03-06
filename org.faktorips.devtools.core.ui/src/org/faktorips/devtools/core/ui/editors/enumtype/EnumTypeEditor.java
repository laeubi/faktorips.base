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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.EnumsDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;

/**
 * The Faktor-IPS editor to edit <tt>IEnumType</tt> objects with.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    @Override
    protected String getUniformPageTitle() {
        return Messages.EnumTypeEditor_title + getIpsObject().getName();
    }

    /** Returns the <tt>IEnumType</tt> this editor is currently editing. */
    IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        addPage(new EnumTypeEditorPage(this));
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        return new EnumsDescriptionPage(getEnumType());
    }

}