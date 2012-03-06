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

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableAccessFunction extends AtomicIpsObjectPart implements ITableAccessFunction {

    private String accessedColumn;
    private String type;
    private String[] argTypes = new String[0];

    public TableAccessFunction(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    public TableAccessFunction() {
        super();
    }

    @Override
    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return null;
    }

    @Override
    public String getName() {
        return getTableStructure().getName() + '.' + getAccessedColumn();
    }

    @Override
    public String getAccessedColumn() {
        return accessedColumn;
    }

    @Override
    public void setAccessedColumn(String columnName) {
        accessedColumn = columnName;
    }

    @Override
    public IColumn findAccessedColumn() {
        return getTableStructure().getColumn(accessedColumn);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String newType) {
        type = newType;
    }

    @Override
    public void setArgTypes(String[] types) {
        // make a defensive copy.
        argTypes = new String[types.length];
        System.arraycopy(types, 0, argTypes, 0, types.length);
    }

    @Override
    public String[] getArgTypes() {
        String[] types = new String[argTypes.length];
        System.arraycopy(argTypes, 0, types, 0, argTypes.length);
        return types; // return defensive copy
    }

}