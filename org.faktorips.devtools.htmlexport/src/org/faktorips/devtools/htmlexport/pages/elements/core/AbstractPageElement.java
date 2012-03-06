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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * @author dicker
 * 
 */
public abstract class AbstractPageElement implements IPageElement {

    protected Set<Style> styles = new LinkedHashSet<Style>();

    private String id;
    private String anchor;

    public AbstractPageElement() {
        super();
    }

    public AbstractPageElement(Set<Style> styles) {
        super();
        this.styles = styles;
    }

    @Override
    public Set<Style> getStyles() {
        if (styles == null) {
            return Collections.emptySet();
        }
        return new HashSet<Style>(styles);
    }

    @Override
    public IPageElement addStyles(Style... style) {
        styles.addAll(Arrays.asList(style));
        return this;
    }

    @Override
    public void removeStyles(Style... style) {
        styles.removeAll(Arrays.asList(style));
    }

    @Override
    public boolean hasStyle(Style style) {
        return getStyles().contains(style);
    }

    @Override
    public void build() {
        // override in subclass
    }

    @Override
    public abstract void acceptLayouter(ILayouter layouter);

    @Override
    public void makeBlock() {
        addStyles(Style.BLOCK);
    }

    @Override
    public String getId() {
        if (id == null) {
            createId();
        }
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * creates the Id of a page e.g. for internal links
     * 
     */
    protected void createId() {
        // could be overridden
    }

    public boolean hasId() {
        return StringUtils.isNotBlank(getId());
    }

    @Override
    public String getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }
}