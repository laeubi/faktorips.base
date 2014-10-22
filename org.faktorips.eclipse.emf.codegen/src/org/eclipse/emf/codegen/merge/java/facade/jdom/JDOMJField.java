/**
 * <copyright>
 *
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: JDOMJField.java,v 1.2 2006/12/06 03:48:07 marcelop Exp $
 */
package org.eclipse.emf.codegen.merge.java.facade.jdom;

import org.eclipse.emf.codegen.merge.java.facade.JField;
import org.eclipse.jdt.core.jdom.IDOMField;

/**
 * @since 2.2.0
 */
@SuppressWarnings({ "deprecation" })
public class JDOMJField extends JDOMJMember implements JField {
    public JDOMJField(IDOMField field) {
        super(field);
    }

    @Override
    protected IDOMField getWrappedObject() {
        return (IDOMField)super.getWrappedObject();
    }

    @Override
    public String getInitializer() {
        return getWrappedObject().getInitializer();
    }

    @Override
    public void setInitializer(String initializer) {
        getWrappedObject().setInitializer(initializer);
    }

    @Override
    public String getType() {
        return getWrappedObject().getType();
    }

    @Override
    public void setType(String typeName) {
        getWrappedObject().setType(typeName);
    }
}
