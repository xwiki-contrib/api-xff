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
package org.xwiki.filter.xff.input;

import java.io.InputStream;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.internal.input.XFFInputFilter;
import org.xwiki.model.reference.EntityReference;

/**
 * Abstract class that each element from XFF (wiki, space, page, object, class, etc.) should implement.
 * 
 * @version $Id$
 * @since 7.1
 */
public abstract class AbstractReader
{
    /**
     * Logger to report errors, warnings, etc.
     */
    @Inject
    protected Logger logger;

    /**
     * Filter.
     */
    protected Object filter;

    /**
     * Proxy filter to use for the readers.
     */
    protected XFFInputFilter proxyFilter;

    /**
     * Constructor that stores filters (there is no empty constructor for readers).
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public AbstractReader(Object filter, XFFInputFilter proxyFilter)
    {
        this.filter = filter;
        this.proxyFilter = proxyFilter;
    }

    /**
     * Convert XML input stream into a JAX-B object.
     * 
     * @param inputStream is the XML as input.
     * @param type is the class in which the XML should be converted
     * @return the converted class
     */
    protected Object unmarshal(InputStream inputStream, Class<?> type)
    {
        try {
            JAXBContext jc = JAXBContext.newInstance(type);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return unmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            String message = String.format("Unable to unmarshal the type '%s'", type.toString());
            logger.error(message, e);
            return null;
        }
    }

    /**
     * Push a new file to the reader. When a XFF is read, it's read file by file. The master filter (XFFFilter) will
     * route these information to the child filter (wiki, then space, then page, etc.) until the right filter is in
     * charge (e.g. a path 'xwiki/Space/Page/index.xml' will be treated by the PageFilter).
     * 
     * @param path is the relative path of the file being read
     * @param inputStream is the stream of the read file
     * @param parentReference is a reference of the element that call this method
     * @throws FilterException whenever there is problem to generate an event
     */
    public abstract void route(Path path, InputStream inputStream, EntityReference parentReference)
        throws FilterException;

    /**
     * When all elements has been pushed, this method should be called to close properly the filter.
     * 
     * @throws FilterException whenever there is problem to generate an event
     */
    public abstract void finish() throws FilterException;
}
