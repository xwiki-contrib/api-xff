package org.xwiki.filter.xar2.input;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.rest.model.jaxb.Page;

/**
 * Aggregate all informations (class, attachments, objects, etc.) about a document page.
 * 
 * @version $Id$
 * @since 7.1
 *
 */
public class DocumentStack
{
    /**
     * Reference of the current document.
     */
    private DocumentReference reference;

    /**
     * All information relative to the page (see Page JAX-B object from xwiki-platform-rest-model).
     */
    private Page xPage;

    /**
     * All information relative to the class (see Page JAX-B object from xwiki-platform-rest-model).
     */
    private Class xClass;

    /**
     * 
     * @return a Page object relative to the current Document Stack
     */
    public Page getxPage()
    {
        return xPage;
    }

    /**
     * @param xPage is a Page object (see JAX-B object from xwiki-platform-rest-model).
     */
    public void setxPage(Page xPage)
    {
        this.xPage = xPage;
    }

    /**
     * @return a Class object relative to the current Document Stack.
     */
    public Class getxClass()
    {
        return xClass;
    }

    /**
     * @param xClass is a Class object (see JAX-B object from xwiki-platform-rest-model).
     */
    public void setxClass(Class xClass)
    {
        this.xClass = xClass;
    }

    /**
     * @return a DocumentReference relative to the current Document Stack.
     */
    public DocumentReference getReference()
    {
        return reference;
    }

    /**
     * @param reference describes the reference of the current Document Stack.
     */
    public void setReference(DocumentReference reference)
    {
        this.reference = reference;
    }
}
