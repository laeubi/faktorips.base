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

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.ui.IpsSrcFileViewItem;

/**
 * Standard drag listener for IPS elements in a structured viewer.
 */
public class IpsElementDragListener implements DragSourceListener {

    StructuredViewer dragSource;

    /**
     * Constructor for <tt>IpsElementDragListener</tt> needs a <tt>StructuredViewer</tt>
     * 
     * @param dragSource The source you want to add drag support to; you want to drag from this
     *            structured viewer
     */
    public IpsElementDragListener(StructuredViewer dragSource) {
        this.dragSource = dragSource;
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        event.doit = getFilenames((IStructuredSelection)dragSource.getSelection()).length > 0;
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        event.data = getFilenames((IStructuredSelection)dragSource.getSelection());
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        // Nothing to do.
    }

    /**
     * To get the filenames of the selected elements in the structured selection
     * 
     * @param selection the selection you want to get the selected filenames from
     * 
     * @return an array of string containing the filenames
     */
    public static String[] getFilenames(IStructuredSelection selection) {
        ArrayList<String> list = new ArrayList<String>(selection.size());
        Iterator<?> iter = selection.iterator();
        while (iter.hasNext()) {
            Object selected = iter.next();
            if (selected instanceof Object[]) {
                Object[] objetcs = (Object[])selected;
                for (Object objetc : objetcs) {
                    addSelectedObject(list, objetc);
                }
            } else {
                addSelectedObject(list, selected);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    private static void addSelectedObject(List<String> list, Object selected) {
        if (selected instanceof IResource) {
            list.add(((IResource)selected).getLocation().toOSString());
        } else if (selected instanceof IIpsElement) {
            if (((IIpsElement)selected).getEnclosingResource() != null) {
                list.add(((IIpsElement)selected).getEnclosingResource().getLocation().toOSString());
            }
        } else if (selected instanceof IProductCmptStructureReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)selected;
            if (reference.getWrappedIpsObject() != null) {
                list.add(reference.getWrappedIpsObject().getEnclosingResource().getLocation().toOSString());
            }
        } else if (selected instanceof IpsSrcFileViewItem) {
            IpsSrcFileViewItem ipsSrcFileWrapper = (IpsSrcFileViewItem)selected;
            list.add(ipsSrcFileWrapper.getIpsSrcFile().getEnclosingResource().getLocation().toOSString());
        }
    }

}