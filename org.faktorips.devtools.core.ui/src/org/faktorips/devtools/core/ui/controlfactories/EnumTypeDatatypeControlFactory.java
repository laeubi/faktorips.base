/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumTypeDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.devtools.core.ui.table.TableTraversalStrategy;

/**
 * A control factory for the {@link IEnumType} which implements the {@link EnumDatatype} interface.
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeControlFactory extends ValueDatatypeControlFactory {

    public EnumTypeDatatypeControlFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return datatype instanceof EnumTypeDatatypeAdapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditField createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        Combo combo = toolkit.createCombo(parent);
        if (valueSet != null && valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            return new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
        }
        EnumTypeDatatypeAdapter datatypeAdapter = (EnumTypeDatatypeAdapter)datatype;
        return new EnumTypeDatatypeField(combo, datatypeAdapter);
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        return createEditField(toolkit, parent, datatype, valueSet, ipsProject).getControl();
    }

    /**
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public TableCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        return createTableCellEditor(toolkit, datatype, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a <code>ComboCellEditor</code> for the given valueset and Datatype. The created
     * CellEditor contains a <code>Combo</code> control that is filled with the corresponding values
     * from the given <code>ValueSet</code>. If the given valueset is either not an
     * <code>EnumValueSet</code> or <code>null</code> a <code>ComboCellEditor</code> is created with
     * a <code>Combo</code> control for the given <code>DataType</code>. In this case the Combo
     * contains the value IDs (not the names) of the given <code>EnumDatatype</code> {@inheritDoc}
     */
    @Override
    public TableCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        EditField editField = createEditField(toolkit, tableViewer.getTable(), datatype, valueSet, ipsProject);
        editField.getControl().setData(editField);
        ComboCellEditor cellEditor = new ComboCellEditor((Combo)editField.getControl());
        cellEditor.setTraversalStrategy(new TableTraversalStrategy(cellEditor, tableViewer, columnIndex));
        return cellEditor;
    }

    /**
     * Creates a <code>ComboCellEditor</code> for the given valueset and Datatype. The created
     * CellEditor contains a <code>Combo</code> control that is filled with the corresponding values
     * from the given <code>ValueSet</code>. If the given valueset is either not an
     * <code>EnumValueSet</code> or <code>null</code> a <code>ComboCellEditor</code> is created with
     * a <code>Combo</code> control for the given <code>DataType</code>. In this case the Combo
     * contains the value IDs (not the names) of the given <code>EnumDatatype</code> {@inheritDoc}
     */
    @Override
    public TableCellEditor createGridCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            ColumnViewer columnViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        EditField editField = createEditField(toolkit, (Composite)columnViewer.getControl(), datatype, valueSet,
                ipsProject);
        editField.getControl().setData(editField);
        return new ComboCellEditor((Combo)editField.getControl());
    }
}
