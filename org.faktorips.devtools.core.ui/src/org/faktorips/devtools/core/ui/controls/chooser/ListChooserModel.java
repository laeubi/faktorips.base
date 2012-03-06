/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.chooser;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class ListChooserModel extends PresentationModelObject {

    private static final String RESULTING_VALUES = "resultingValues"; //$NON-NLS-1$

    private final List<String> preDefinedValues;

    private final IEnumValueSet resultingEnumValueSet;

    public ListChooserModel(List<String> preDefinedValues, IEnumValueSet resultingEnumValueSet) {
        this.preDefinedValues = preDefinedValues;
        this.resultingEnumValueSet = resultingEnumValueSet;
    }

    /**
     * Adds the given values to the resulting list of enum values and removes them from the
     * predefined list of values.
     * <p>
     * IOW the values are moved from the predefined values to the list of resulting values.
     * <p>
     * Unknown values are ignored.
     * 
     * @param values the values to be moved
     */
    public void moveValuesFromPreDefinedToResulting(List<String> values) {
        List<String> oldValues = getResultingValues();
        for (String value : new CopyOnWriteArrayList<String>(values)) {
            preDefinedValues.remove(value);
            resultingEnumValueSet.addValue(value);
        }
        notifyListeners(new PropertyChangeEvent(this, RESULTING_VALUES, oldValues, getResultingValues()));
    }

    public void moveAllValuesFromPreDefinedToResulting() {
        moveValuesFromPreDefinedToResulting(preDefinedValues);
    }

    public void moveValuesFromResultingToPredefined(List<String> values) {
        List<String> oldValues = getResultingValues();
        for (String value : new CopyOnWriteArrayList<String>(values)) {
            resultingEnumValueSet.removeValue(value);
            preDefinedValues.add(value);
        }
        notifyListeners(new PropertyChangeEvent(this, RESULTING_VALUES, oldValues, getResultingValues()));
    }

    public void moveAllValuesFromResultingToPreDefined() {
        moveValuesFromResultingToPredefined(resultingEnumValueSet.getValuesAsList());
    }

    public List<String> getPreDefinedValues() {
        return preDefinedValues;
    }

    public List<String> getResultingValues() {
        return resultingEnumValueSet.getValuesAsList();
    }

    public void move(List<String> selectedValues, boolean up) {
        List<String> oldValues = getResultingValues();
        List<Integer> indexes = new ArrayList<Integer>();
        for (String value : selectedValues) {
            List<Integer> positions = resultingEnumValueSet.getPositions(value);
            indexes.addAll(positions);
        }
        resultingEnumValueSet.move(indexes, up);
        notifyListeners(new PropertyChangeEvent(this, RESULTING_VALUES, oldValues, getResultingValues()));
    }

    public void moveUp(List<String> selectedValues) {
        move(selectedValues, true);
    }

    public void moveDown(List<String> selectedValues) {
        move(selectedValues, false);
    }

}