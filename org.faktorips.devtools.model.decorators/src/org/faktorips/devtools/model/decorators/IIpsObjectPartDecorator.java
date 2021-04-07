package org.faktorips.devtools.model.decorators;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * An {@link IIpsElementDecorator} for {@link IIpsObjectPart IIpsObjectParts}.
 *
 * @since 21.6
 */
public interface IIpsObjectPartDecorator extends IIpsElementDecorator {

    @Override
    default ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)ipsElement;
            return getImageDescriptor(ipsObjectPart);
        }
        return getDefaultImageDescriptor();
    }

    /**
     * Returns the {@link ImageDescriptor} for the given {@link IIpsObjectPart}.
     */
    ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart);

    @Override
    default String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)ipsElement;
            return getLabel(ipsObjectPart);
        }
        return IpsStringUtils.EMPTY;
    }

    /**
     * Returns the label for the given {@link IIpsObjectPart}.
     */
    default String getLabel(IIpsObjectPart ipsObjectPart) {
        return ipsObjectPart == null ? IpsStringUtils.EMPTY : ipsObjectPart.getName();
    }

}