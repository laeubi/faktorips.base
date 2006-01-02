package org.faktorips.devtools.core.ui.controller;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class IpsPartUIController extends DefaultUIController {

    private IIpsObjectPart part;
    
    public IpsPartUIController(IIpsObjectPart part) {
        super();
        this.part = part;
    }
    
    public IIpsObjectPart getIpsObjectPart() {
        return part;
    }
    
    public void add(EditField field, String propertyName) {
        IExtensionPropertyDefinition extProperty = part.getIpsModel().getExtensionPropertyDefinition(part.getClass(), propertyName, true);
        if (extProperty!=null) {
            addMapping(new FieldExtensionPropertyMapping(field, part, propertyName));
        } else {
            super.add(field, part, propertyName);
        }
    }
    
    public void updateUI() {
        super.updateUI();
        validatePartAndUpdateUI();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener#valueChanged(org.faktorips.devtools.core.ui.controls.ContentChangeEvent)
     */
    public void valueChanged(FieldValueChangedEvent e) {
        super.valueChanged(e);
        validatePartAndUpdateUI();
    }
    
    protected void validatePartAndUpdateUI() {
        try {
            MessageList list = part.validate();    
	        for (Iterator it=mappings.iterator(); it.hasNext();) {
	            FieldPropertyMapping mapping = (FieldPropertyMapping)it.next();
	            MessageList controlList = list.getMessagesFor(mapping.getObject(), mapping.getPropertyName());
	            mapping.getField().setMessages(controlList);
	        }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
}
