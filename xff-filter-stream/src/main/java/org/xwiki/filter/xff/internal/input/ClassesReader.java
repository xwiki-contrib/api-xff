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
import org.xwiki.rest.model.jaxb.Attribute;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.rest.model.jaxb.Property;

/**
 * Read file from XFF and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
@Component
@Named("classes")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ClassesReader extends AbstractReader
{
    /**
     * Name of the file to describe a class.
     */
    private static final String CLASS_FILENAME = "class.xml";

    /**
     * Reference to the current document.
     */
    private DocumentReference reference;

    /**
     * Contain the model of a page (see xwiki-platform-rest-model).
     */
    private Class xClass = new Class();

    /**
     * Parameters to send to the current class.
     */
    private FilterEventParameters parameters = FilterEventParameters.EMPTY;

    private void parseClass(InputStream inputStream) throws FilterException
    {
        if (inputStream != null) {
            this.xClass = (Class) this.unmarshal(inputStream, Class.class);
        }
    }

    private void routeProperties() throws FilterException
    {
        for (Property property : this.xClass.getProperties()) {
            this.proxyFilter
                .beginWikiClassProperty(property.getName(), property.getType(), FilterEventParameters.EMPTY);
            for (Attribute attribute : property.getAttributes()) {
                this.proxyFilter.onWikiClassPropertyField(attribute.getName(), attribute.getValue(),
                    FilterEventParameters.EMPTY);
            }
            this.proxyFilter.endWikiClassProperty(property.getName(), property.getType(), FilterEventParameters.EMPTY);
        }
    }

    private void start() throws FilterException
    {
        if (!this.started) {
            this.proxyFilter.beginWikiClass(this.parameters);
            this.started = true;
        }
    }

    private void end() throws FilterException
    {
        if (this.started) {
            this.proxyFilter.endWikiClass(this.parameters);
        }
    }

    private void routeMetadata(Path path, InputStream inputStream) throws FilterException
    {
        String filename = path.getFileName().toString();
        String filePropertyName = path.getName(path.getNameCount() - 2).toString();
        for (Property property : this.xClass.getProperties()) {
            String propertyName = property.getName();
            if (filePropertyName.equals(propertyName)) {
                for (Attribute attribute : property.getAttributes()) {
                    String attributeName = attribute.getName();
                    if (filename.startsWith(attributeName + '.')) {
                        try {
                            attribute.setValue(IOUtils.toString(inputStream));
                        } catch (IOException e) {
                            String message = String.format("Unable to read a string from '%s'.", path.toString());
                            throw new FilterException(message, e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void open(String id, EntityReference parentReference, Object filter, XFFInputFilter proxyFilter)
        throws FilterException
    {
        this.reference = new DocumentReference((DocumentReference) parentReference);
        this.setFilters(filter, proxyFilter);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        String fileName = path.toString();
        if (fileName.equals(ClassesReader.CLASS_FILENAME)) {
            this.parseClass(inputStream);
            this.start();
            return;
        } else {
            this.start();
        }
        if (path.toString().startsWith("metadata")) {
            this.routeMetadata(path, inputStream);
        } else {
            String message = String.format("ClassesReader don't know how to route '%s'.", path.toString());
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
