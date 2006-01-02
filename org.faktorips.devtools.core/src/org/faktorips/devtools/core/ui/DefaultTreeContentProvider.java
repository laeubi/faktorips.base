package org.faktorips.devtools.core.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;


/**
 *
 */
public abstract class DefaultTreeContentProvider implements ITreeContentProvider {
    
    public DefaultTreeContentProvider() {
        super();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (!(parentElement instanceof IIpsElement)) {
            return new Object[0];
        }
        try {
            return ((IIpsElement)parentElement).getChildren();    
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return new Object[0]; 
        }
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        return ((IIpsElement)element).getParent();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        try {
            return ((IIpsElement)element).hasChildren();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }


}
