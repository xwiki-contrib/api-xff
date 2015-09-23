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

import org.apache.commons.io.IOUtils;
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
public class ClassReader extends AbstractReader
{
    /**
     * Name of the file to describe a class.
     */
    static final String CLASS_FILENAME = "__class.xml";

    /**
     * Reference to the current document.
     */
    private DocumentReference reference;

    /**
     * Contain the model of a page (see xwiki-platform-rest-model).
     */
    private Class xClass = new Class();

    /**
     * Retain information about if a class has been opened.
     */
    private boolean started;

    /**
     * Retain the last class in order to close and reinit if the current class is a new one.
     */
    private Path previousClassPath;

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
    public ClassReader(Object filter, XFFInputFilter proxyFilter)
    {
        super(filter, proxyFilter);
    }

    private void reset()
    {
        this.reference = null;
        this.xClass = new Class();
        this.parameters = FilterEventParameters.EMPTY;
        this.started = false;
    }

    private void setReference(EntityReference parentReference)
    {
        this.reference = (DocumentReference) parentReference;
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

    private void init(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        this.reset();
        if (inputStream != null) {
            this.xClass = (Class) this.unmarshal(inputStream, Class.class);
        }
        this.setReference(parentReference);
    }

    private void start() throws FilterException
    {
        this.proxyFilter.beginWikiClass(this.parameters);
        this.started = true;
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
    public void route(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        Path classPath;
        try {
            classPath = path.subpath(0, 4);
        } catch (IllegalArgumentException e) {
            String message = String.format("Unable to extract class path from '%s'.", path.toString());
            throw new FilterException(message);
        }

        // Close previous class before starting a new one
        if (!classPath.equals(this.previousClassPath)) {
            this.finish();
        }
        // Parse files relative to page or reroute them
        if (path.endsWith(ClassReader.CLASS_FILENAME)) {
            this.init(classPath, inputStream, parentReference);
        } else if (classPath.relativize(path).toString().startsWith("_metadata")) {
            this.routeMetadata(path, inputStream);
        } else {
            String message = String.format("ClassReader don't know how to route '%s'.", path.toString());
            throw new FilterException(message);
        }
        this.previousClassPath = classPath;
    }

    @Override
    public void finish() throws FilterException
    {
        if (this.reference != null) {
            if (!this.started) {
                this.start();
            }
            routeProperties();
            this.proxyFilter.endWikiClass(this.parameters);
        }
        this.reset();
    }
}
