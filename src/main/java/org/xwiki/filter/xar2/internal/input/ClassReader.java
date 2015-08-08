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
import org.xwiki.rest.model.jaxb.Attribute;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.rest.model.jaxb.Property;

/**
 * Read file from XAR2 and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
public class ClassReader extends AbstractReader
{
    /**
     * Name of the file to describe a wiki.
     */
    static final String CLASS_FILENAME = "class.xml";

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

    /**
     * Constructor that only call the abstract constructor.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public ClassReader(Object filter, XAR2InputFilter proxyFilter)
    {
        super(filter, proxyFilter);
    }

    private void setReference(String className, EntityReference parentReference)
    {
        this.reference = (DocumentReference) parentReference;
    }

    private void filterProperties() throws FilterException
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

    @Override
    public void open(Path path, EntityReference parentReference, InputStream inputStream) throws FilterException
    {
        if (inputStream != null) {
            this.xClass = (Class) this.unmarshal(inputStream, Class.class);
        }
        String className = this.xClass.getId();
        if (className == null) {
            className = path.getName(5).toString();
        }
        this.setReference(className, parentReference);
        proxyFilter.beginWikiClass(this.parameters);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        // TODO: Route paths
    }

    @Override
    public void close() throws FilterException
    {
        if (this.reference != null) {
            filterProperties();
            proxyFilter.endWikiClass(this.parameters);
        }
    }
}
