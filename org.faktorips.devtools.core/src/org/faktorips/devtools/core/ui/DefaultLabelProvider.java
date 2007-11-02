/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;


/**
 * Label provider that provides default images and labels (texts) for ips elements.
 * 
 * @author Jan Ortmann
 */
public class DefaultLabelProvider extends LabelProvider {

    private static Image abstractMethodImage = null; 
    private static Image overloadedMethodImage = null; 
    private static Image abstractAndOverloadedMethodImage = null; 
    
    /* indicates the mapping of an ips source files to the their corresponding ips objects */
    private boolean ispSourceFile2IpsObjectMapping = false;
    
    /**
     * Creates an DefaultLabelProvider with additional IpsSourceFile mapping support: In case of an
     * IpsSourceFile the text and the image of the corresponding IpsObject will be returned.s
     */
    public static ILabelProvider createWithIpsSourceFileMapping() {
        return new DefaultLabelProvider(true);
    }
    
    public DefaultLabelProvider() {
        super();
    }

    protected DefaultLabelProvider(boolean ispSourceFile2IpsObjectMapping) {
        super();
        this.ispSourceFile2IpsObjectMapping = ispSourceFile2IpsObjectMapping;
    }
    
    public void setIspSourceFile2IpsObjectMapping(boolean ispSourceFile2IpsObjectMapping) {
        this.ispSourceFile2IpsObjectMapping = ispSourceFile2IpsObjectMapping;
    }

    /** 
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        try {
            if (element instanceof IIpsSrcFile && ispSourceFile2IpsObjectMapping){
                return getMappedImageForIpsSrcFile((IIpsSrcFile) element);
            }
            if (element instanceof IMethod) {
                return getMethodImage((IMethod)element);
            }
            if ((element instanceof IIpsElement)) {
                return ((IIpsElement)element).getImage();
            }
            if (element instanceof Datatype) {
                return IpsPlugin.getDefault().getImage("Datatype.gif"); //$NON-NLS-1$
            }
            if (element instanceof FlFunction) {
                return IpsPlugin.getDefault().getImage("Function.gif"); //$NON-NLS-1$
            }
            return super.getImage(element); 
        } catch (Exception e) {
            IpsPlugin.log(e);
            return super.getImage(element);
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getText(Object element) {
        if (element==null) {
            return "null"; //$NON-NLS-1$
        }
        if (element instanceof IIpsSrcFile && ispSourceFile2IpsObjectMapping){
            return getMappedNameForIpsSrcFile((IIpsSrcFile) element);
        }
        if (!(element instanceof IIpsElement)) {
            return element.toString();
        }
        IIpsElement ipsElement = (IIpsElement)element;
        if (element instanceof IIpsPackageFragment) {
            if (ipsElement.getName().equals("")) { //$NON-NLS-1$
                return Messages.DefaultLabelProvider_labelDefaultPackage;
            }
        }
        if (element instanceof IMethod) {
            return getMethodLabel((IMethod)element);
        }
        if (element instanceof IAttribute) {
            IAttribute a = (IAttribute)element;
            StringBuffer sb = new StringBuffer();
            if (a.isDerived()) {
                sb.append("/");
            }
            sb.append(a.getName());
            sb.append(" : "); //$NON-NLS-1$
            sb.append(a.getDatatype());
            return sb.toString();
        }
        return ipsElement.getName();
    }
    
    private String getMappedNameForIpsSrcFile(IIpsSrcFile file) {
        return StringUtil.getFilenameWithoutExtension(file.getName());
    }

    private Image getMappedImageForIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        if (ipsSrcFile.exists()){
            return ipsSrcFile.getIpsObjectType().getEnabledImage();
        } else {
            // @see IpsObject#getImage()
            return IpsObjectType.IPS_SOURCE_FILE.getEnabledImage();
        }
    }   
    
    private String getMethodLabel(IMethod method) {
        StringBuffer buffer = new StringBuffer(method.getName());
        buffer.append('(');
        IParameter[] params = method.getParameters();
        for (int i=0; i<params.length; i++) {
            if (i>0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(QNameUtil.getUnqualifiedName(params[i].getDatatype()));
        }
        buffer.append(") : ");
        buffer.append(QNameUtil.getUnqualifiedName(method.getDatatype()));
        return buffer.toString();
    }
    
    private Image getMethodImage(IMethod method) throws CoreException {
        boolean overloaded = method.findOverridingMethod(method.getType(), method.getIpsProject())!=null;
        if (method.isAbstract()) {
            if (overloaded) {
                return getAbstractOverloadedMethodImage(method);
            } else {
                return getAbstractMethodImage(method);
            }
        } 
        if (overloaded) {
            return getOverloadedMethodImage(method);
        }
        return method.getImage();
    }
    
    private static Image getAbstractMethodImage(IMethod method) {
        if (abstractMethodImage==null) {
            abstractMethodImage = new AbstractPropertyImageDescriptor(method.getImage()).createImage();
        }
        return abstractMethodImage;
    }
    
    private static Image getOverloadedMethodImage(IMethod method) {
        if (overloadedMethodImage==null) {
            overloadedMethodImage = new OverrideImageDescriptor(method.getImage()).createImage();
        }
        return overloadedMethodImage;
    }

    private static Image getAbstractOverloadedMethodImage(IMethod method) {
        if (abstractAndOverloadedMethodImage==null) {
            abstractAndOverloadedMethodImage = new OverrideImageDescriptor(getAbstractMethodImage(method)).createImage();
        }
        return abstractAndOverloadedMethodImage;
    }

    private static class AbstractPropertyImageDescriptor extends CompositeImageDescriptor {

        private final static Point DEFAULT_SIZE = new Point(16, 16);
        
        private Image baseImage;
        private Point size = DEFAULT_SIZE;
        
        public AbstractPropertyImageDescriptor(Image image) {
            ArgumentCheck.notNull(image);
            baseImage = image;
        }

        /** 
         * {@inheritDoc}
         */
        protected void drawCompositeImage(int width, int height) {
            drawImage(baseImage.getImageData(), 0, 0);
            drawImage(IpsPlugin.getDefault().getImage("AbstractIndicator.gif").getImageData(), 8, 0); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        protected Point getSize() {
            return size;
        }
    }

    
}
