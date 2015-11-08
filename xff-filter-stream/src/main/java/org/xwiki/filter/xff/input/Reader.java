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

import org.xwiki.component.annotation.Role;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.internal.input.XFFInputFilter;
import org.xwiki.model.reference.EntityReference;

/**
 * Interface for {@link Reader} that defines 3 methods, one for initializing the {@link Reader}, one for routing and
 * parsing the input elements, and finally one to close the {@link Reader}.
 * 
 * @version $Id$
 * @since 7.1
 */
@Role
public interface Reader
{
    /**
     * Initialize a new {@link Reader} before using it.
     * 
     * @param id related to the instance of the {@link Reader} (for example, the name of the {@link org.xwiki.rest.model.jaxb.Page}, or the
     *            {@link org.xwiki.rest.model.jaxb.Object} class)
     * @param parentReference is a reference about the {@link Reader} that is initializing the current one
     * @param filter to be used in the parsing process
     * @param proxyFilter to be used to send event during the parsing process
     * @throws FilterException whenever there is problem to initialize the {@link Reader}
     */
    void open(String id, EntityReference parentReference, Object filter, XFFInputFilter proxyFilter)
        throws FilterException;

    /**
     * Push a new file to the reader. When a XFF is read, it's read file by file. The master filter (XFFFilter) will
     * route these information to the child filter (wiki, then space, then page, etc.) until the right filter is in
     * charge (e.g. a path 'xwiki/Space/Page/index.xml' will be treated by the PageFilter).
     * 
     * @param path is the relative path of the file being read
     * @param inputStream is the stream of the read file
     * @throws FilterException whenever there is problem to generate an event
     */
    void route(Path path, InputStream inputStream) throws FilterException;

    /**
     * When all elements has been pushed, this method should be called to close properly the filter.
     * 
     * @throws FilterException whenever there is problem to generate an event
     */
    void close() throws FilterException;
}
