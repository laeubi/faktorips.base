/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.edit;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.faktorips.devtools.core.ui.bf.commands.DeleteBFElementCommand;
import org.faktorips.devtools.model.bf.IBFElement;
import org.faktorips.devtools.model.bf.IBusinessFunction;

/**
 * This policy creates the deletion command for business function elements.
 * 
 * @author Peter Erzberger
 * @deprecated for removal since 21.6
 */
@Deprecated
public class NodeComponentEditPolicy extends org.eclipse.gef.editpolicies.ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        IBusinessFunction bf = (IBusinessFunction)getHost().getParent().getModel();
        DeleteBFElementCommand deleteCmd = new DeleteBFElementCommand(bf, (IBFElement)getHost().getModel());
        return deleteCmd;
    }
}
