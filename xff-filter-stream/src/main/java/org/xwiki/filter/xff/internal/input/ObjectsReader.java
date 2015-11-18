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
package org.xwiki.filter.xff.internal.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.input.AbstractReader;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.rest.model.jaxb.Property;

/**
 * Read file from XFF and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
@Component
@Named(org.xwiki.xff.core.model.Object.OBJECT_HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ObjectsReader extends AbstractReader
{
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

    private void parseObject(InputStream inputStream) throws FilterException
    {
        if (inputStream != null) {
            this.xObject =
                (org.xwiki.rest.model.jaxb.Object) this.unmarshal(inputStream, org.xwiki.rest.model.jaxb.Object.class);
        }
        String objectName = this.xObject.getId();
        if (objectName != null) {
            this.reference = new ObjectReference(objectName, (DocumentReference) this.reference.getParent());
        }
    }

    private String getName()
    {
        StringBuilder nameBuilder = new StringBuilder(this.reference.getName());
        nameBuilder.append('[');
        nameBuilder.append(this.number);
        nameBuilder.append(']');
        return nameBuilder.toString();
    }

    private void routeProperties() throws FilterException
    {
        for (Property property : this.xObject.getProperties()) {
            this.proxyFilter.onWikiObjectProperty(property.getName(), property.getValue(), FilterEventParameters.EMPTY);
        }
    }

    private void start() throws FilterException
    {
        if (!this.started) {
            this.proxyFilter.beginWikiObject(this.getName(), this.parameters);
            this.started = true;
        }
    }

    private void end() throws FilterException
    {
        if (this.started) {
            this.proxyFilter.endWikiObject(this.getName(), this.parameters);
        }
    }

    private void routeProperty(Path path, InputStream inputStream) throws FilterException
    {
        String filename = path.getFileName().toString();
        for (Property property : this.xObject.getProperties()) {
            String propertyName = property.getName();
            if (filename.startsWith(propertyName + '.')) {
                try {
                    property.setValue(IOUtils.toString(inputStream));
                } catch (IOException e) {
                    String message = String.format("Unable to read a string from '%s'.", path.toString());
                    throw new FilterException(message, e);
                }
            }
        }
    }

    @Override
    public void open(String id, EntityReference parentReference, Object filter, XFFInputFilter proxyFilter)
        throws FilterException
    {
        String[] idList = id.split("/");
        String objectClassName = idList[0];
        String objectNumber = idList[1];
        this.reference = new ObjectReference(objectClassName, (DocumentReference) parentReference);
        this.number = Integer.parseInt(objectNumber);
        this.setFilters(filter, proxyFilter);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        String fileName = path.toString();
        if (fileName.equals(org.xwiki.xff.core.model.Object.OBJECT_FILENAME)) {
            this.parseObject(inputStream);
            this.start();
            return;
        } else {
            this.start();
        }
        if (path.toString().startsWith(org.xwiki.xff.core.model.Property.PROPERTY_HINT)) {
            this.routeProperty(path, inputStream);
        } else {
            String message = String.format("ObjectReader don't know how to route '%s'.", path.toString());
            throw new FilterException(message);
        }
    }

    @Override
    public void close() throws FilterException
    {
        if (this.started) {
            this.routeProperties();
            this.end();
        }
    }
}
