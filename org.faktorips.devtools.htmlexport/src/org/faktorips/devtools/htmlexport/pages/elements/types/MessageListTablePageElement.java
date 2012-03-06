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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.Description;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

public class MessageListTablePageElement extends AbstractStandardTablePageElement {

    protected final MessageList messageList;
    protected final DocumentationContext context;

    public MessageListTablePageElement(MessageList messageList, DocumentationContext context) {
        super();
        this.messageList = messageList;
        this.context = context;
    }

    @Override
    protected void addDataRows() {
        for (Message message : messageList) {
            addMessageRow(message);
        }
    }

    /**
     * adds a row for the given message
     * 
     */
    protected void addMessageRow(Message message) {
        int severity = message.getSeverity();
        addSubElement(new TableRowPageElement(new IPageElement[] {
                createInvalidObjectPropertiesPageElement(message),
                new TextPageElement(message.getText()),
                new TextPageElement(severity == Message.ERROR ? getContext().getMessage(
                        "MessageListTablePageElement_error") : severity == Message.WARNING ? getContext().getMessage( //$NON-NLS-1$
                        "MessageListTablePageElement_warning") : severity == Message.INFO ? getContext().getMessage( //$NON-NLS-1$
                        "MessageListTablePageElement_info") : getContext().getMessage( //$NON-NLS-1$
                        "MessageListTablePageElement_severity") //$NON-NLS-1$
                        + severity) }));
    }

    protected IPageElement createInvalidObjectPropertiesPageElement(Message message) {
        if (message.getInvalidObjectProperties().length == 0) {
            return new TextPageElement(""); //$NON-NLS-1$
        }

        if (message.getInvalidObjectProperties().length == 1) {
            return createInvalidObjectPropertiesItem(message.getInvalidObjectProperties()[0]);
        }

        ListPageElement objectPropertiesList = new ListPageElement();

        ObjectProperty[] invalidObjectProperties = message.getInvalidObjectProperties();
        for (ObjectProperty objectProperty : invalidObjectProperties) {
            objectPropertiesList.addPageElements(createInvalidObjectPropertiesItem(objectProperty));
        }

        return objectPropertiesList;
    }

    protected IPageElement createInvalidObjectPropertiesItem(ObjectProperty objectProperty) {
        IIpsSrcFile srcFile = getLinkableSrcFile(objectProperty.getObject(), getContext());
        if (srcFile != null) {
            return new PageElementUtils().createLinkPageElement(context, srcFile, "content", //$NON-NLS-1$
                    srcFile.getIpsObjectName(), true);
        }
        return new TextPageElement(objectProperty.getObject().toString());
    }

    private IIpsSrcFile getLinkableSrcFile(Object object, DocumentationContext context) {
        if (object instanceof String) {
            return null;
        }
        if (object instanceof IIpsSrcFile) {
            return (IIpsSrcFile)object;
        }
        if (object instanceof Description) {
            return getLinkableSrcFile(((Description)object).getParent(), context);
        }
        if (object instanceof IIpsObjectPartContainer) {
            return ((IIpsObjectPartContainer)object).getIpsSrcFile();
        }

        context.addStatus(new IpsStatus(IStatus.WARNING, "No IpsSrcFile for class " + object.getClass())); //$NON-NLS-1$
        return null;
    }

    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        headline.add(getContext().getMessage(HtmlExportMessages.MessageListTablePageElement_headlineProperties));
        headline.add(getContext().getMessage(HtmlExportMessages.MessageListTablePageElement_headlineMessage));
        headline.add(getContext().getMessage(HtmlExportMessages.MessageListTablePageElement_headlineSeverity));

        return headline;
    }

    @Override
    public boolean isEmpty() {
        return messageList.isEmpty();
    }

    protected DocumentationContext getContext() {
        return context;
    }

}