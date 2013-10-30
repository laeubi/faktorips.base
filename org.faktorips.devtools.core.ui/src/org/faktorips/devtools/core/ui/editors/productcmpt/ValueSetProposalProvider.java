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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.ui.internal.ContentProposal;

public class ValueSetProposalProvider implements IContentProposalProvider {

    private static final String SPLIT_SEPERATOR = "|"; //$NON-NLS-1$

    private final IConfigElement configElement;

    private SearchPattern searchPattern = new SearchPattern();

    public ValueSetProposalProvider(IConfigElement propertyValue) {
        this.configElement = propertyValue;
    }

    private EnumValueSet getValueSet() {
        return (EnumValueSet)this.configElement.getValueSet();
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String prefix = StringUtils.left(contents, position);
        String identifier = getLastIdentifier(prefix);
        boolean needSeparator = needSeparator(prefix, identifier);
        List<String> splitList = getContentAsList(contents);

        searchPattern.setPattern(identifier);
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        for (String value : getValueSet().getValuesAsList()) {
            if (!splitList.contains(value) && searchPattern.matches(value)) {
                ContentProposal contentProposal = new ContentProposal(addSeparatorIfNecessary(value, needSeparator),
                        value, null, identifier);
                result.add(contentProposal);
            }
        }
        return result.toArray(new IContentProposal[result.size()]);
    }

    private List<String> getContentAsList(String contents) {
        List<String> contentsList = new ArrayList<String>();
        String[] splitContent = contents.trim().split("\\" + SPLIT_SEPERATOR); //$NON-NLS-1$
        for (String content : splitContent) {
            contentsList.add(content.trim());
        }
        return contentsList;
    }

    private String addSeparatorIfNecessary(String value, boolean needSeparator) {
        if (needSeparator) {
            return SPLIT_SEPERATOR + " " + value; //$NON-NLS-1$
        }
        return value;
    }

    private boolean needSeparator(String s, String identifier) {
        if (StringUtils.isEmpty(s) || s.equals(identifier)) {
            return false;
        }
        int pos = s.indexOf(identifier);
        if (pos == 0) {
            return !endsWithSeparator(s);
        } else if (pos > 0) {
            return !endsWithSeparator(s.substring(0, pos));
        }
        return true;
    }

    private boolean endsWithSeparator(String s) {
        return s.trim().endsWith(SPLIT_SEPERATOR);
    }

    /**
     * The characters that are checked within this method have to be in synch with the identifier
     * tokens defined in the ffl.jjt grammar
     */
    private String getLastIdentifier(String s) {
        if (StringUtils.isEmpty(s)) {
            return StringUtils.EMPTY;
        }
        int i = s.length() - 1;
        boolean isInQuotes = false;
        while (i >= 0) {
            char c = s.charAt(i);
            if (c == '"') {
                isInQuotes = !isInQuotes;
            } else if (!isLegalChar(c, isInQuotes)) {
                break;
            }
            i--;
        }
        return s.substring(i + 1);
    }

    private boolean isLegalChar(char c, boolean isInQuotes) {
        return Character.isLetterOrDigit(c) || (isInQuotes && c == ' ');
    }
}
