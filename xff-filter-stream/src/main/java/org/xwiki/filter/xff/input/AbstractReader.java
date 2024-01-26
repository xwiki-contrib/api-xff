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

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.internal.input.XFFInputFilter;

/**
 * Abstract class that each element from XFF (wiki, space, page, object, class, etc.) should implement.
 * 
 * @version $Id$
 * @since 7.1
 */
public abstract class AbstractReader implements Reader
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
     * Child reader.
     */
    protected Reader reader;

    /**
     * Retain the status of the reader.
     */
    protected boolean started;

    /**
     * Retain the id of the last reader.
     */
    protected String lastReaderId;

    /**
     * The component manager. We need it because we have to access components dynamically.
     */
    @Inject
    private ComponentManager componentManager;

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
     * Will load the correct reader considering the next path. For example, if the next path is
     * <tt>/wikis/xwiki/spaces/Space</tt> , then it will load the {@link org.xwiki.filter.xff.internal.input.WikisReader}
     * because of the hint <tt>wikis</tt> .
     * 
     * @param hint the role hint of the {@link Reader} component
     * @return an instance of the corresponding {@link Reader}
     * @throws FilterException
     */
    protected Reader getReader(String hint) throws FilterException
    {
        try {
            this.reader = (Reader) this.componentManager.getInstance(Reader.class, hint);
            return this.reader;
        } catch (ComponentLookupException e) {
            String message =
                String.format("Unable to find a component org.xwiki.filter.xff.input.Reader with hint '%s'.", hint);
            throw new FilterException(message, e);
        }
    }

    protected void setFilters(Object filter, XFFInputFilter proxyFilter)
    {
        this.filter = filter;
        this.proxyFilter = proxyFilter;
    }
}
