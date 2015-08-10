/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.filter.xar2.internal.input;

import java.io.InputStream;
import java.nio.file.Path;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xar2.input.AbstractReader;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.rest.model.jaxb.Property;

/**
 * Read file from XAR2 and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
public class ObjectReader extends AbstractReader
{
    /**
     * Name of the file to describe an object.
     */
    static final String OBJECT_FILENAME = "__object.xml";

    /**
     * Reference to the current document.
     */
    private ObjectReference reference;

    /**
     * Reference to the number of the object.
     */
    private int number = -1;

    /**
     * Contain the model of an object (see xwiki-platform-rest-model).
     */
    private org.xwiki.rest.model.jaxb.Object xObject = new org.xwiki.rest.model.jaxb.Object();

    /**
     * Parameters to send to the current class.
     */
    private FilterEventParameters parameters = FilterEventParameters.EMPTY;

    /**
     * Constructor that only call the abstract constructor.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public ObjectReader(Object filter, XAR2InputFilter proxyFilter)
    {
        super(filter, proxyFilter);
    }

    private void reset()
    {
        this.reference = null;
        this.number = -1;
        this.xObject = new org.xwiki.rest.model.jaxb.Object();
        this.parameters = FilterEventParameters.EMPTY;
    }

    private void setReference(String className, EntityReference parentReference)
    {
        this.reference = new ObjectReference(className, (DocumentReference) parentReference);
    }

    private String getName()
    {
        StringBuilder nameBuilder = new StringBuilder(this.reference.getName());
        nameBuilder.append('[');
        nameBuilder.append(this.number);
        nameBuilder.append(']');
        return nameBuilder.toString();
    }

    private void filterProperties() throws FilterException
    {
        for (Property property : this.xObject.getProperties()) {
            this.proxyFilter.onWikiObjectProperty(property.getName(), property.getValue(), FilterEventParameters.EMPTY);
        }
    }

    public void open(Path path, EntityReference parentReference, InputStream inputStream) throws FilterException
    {
        this.reset();
        if (inputStream != null) {
            this.xObject =
                (org.xwiki.rest.model.jaxb.Object) this.unmarshal(inputStream, org.xwiki.rest.model.jaxb.Object.class);
        }
        String objectName = this.xObject.getClassName();
        if (objectName == null) {
            objectName = path.getName(4).toString();
        }
        this.setReference(objectName, parentReference);
        this.number = Integer.parseInt(path.getName(5).toString());
        this.proxyFilter.beginWikiObject(this.getName(), this.parameters);
    }

    @Override
    public void route(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        // TODO: Route paths
    }

    @Override
    public void finish() throws FilterException
    {
        if (this.reference != null) {
            filterProperties();
            this.proxyFilter.endWikiObject(this.getName(), this.parameters);
        }
        this.reset();
    }
}
