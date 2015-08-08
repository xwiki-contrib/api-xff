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
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.rest.model.jaxb.Space;

/**
 * Read file from XAR2 and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
public class SpaceReader extends AbstractReader
{
    /**
     * Name of the file to describe a wiki.
     */
    static final String SPACE_FILENAME = "space.xml";

    /**
     * Reference to the current space.
     */
    private SpaceReference reference;

    /**
     * Retain the last page path in order to close and reinit if the current page is a new one.
     */
    private Path previousPagePath;

    /**
     * Contain the model of a space (see xwiki-platform-rest-model).
     */
    private Space xSpace = new Space();

    /**
     * Child reader for page.
     */
    private PageReader pageReader;

    /**
     * Constructor that instantiate the child page reader.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public SpaceReader(Object filter, XAR2InputFilter proxyFilter)
    {
        super(filter, proxyFilter);
        this.pageReader = new PageReader(filter, proxyFilter);
    }

    private void setReference(String spaceName, EntityReference parentReference)
    {
        this.reference = new SpaceReference(spaceName, parentReference);
    }

    @Override
    public void open(Path path, EntityReference parentReference, InputStream inputStream) throws FilterException
    {
        if (inputStream != null) {
            this.xSpace = (Space) this.unmarshal(inputStream, Space.class);
        }
        String spaceName = this.xSpace.getId();
        if (spaceName == null) {
            spaceName = path.getName(1).toString();
        }
        this.setReference(spaceName, parentReference);
        proxyFilter.beginWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        Path pagePath = path.subpath(0, 3);
        if (!pagePath.equals(previousPagePath)) {
            if (previousPagePath != null) {
                pageReader.close();
            }
            if (path.endsWith(PageReader.PAGE_FILENAME) && path.getNameCount() == 4) {
                pageReader.open(path, this.reference, inputStream);
            } else {
                pageReader.open(path, this.reference, null);
                pageReader.route(path, inputStream);
            }
        } else {
            pageReader.route(path, inputStream);
        }
        previousPagePath = pagePath;
    }

    @Override
    public void close() throws FilterException
    {
        this.pageReader.close();
        if (this.reference != null) {
            proxyFilter.endWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
        }

        this.previousPagePath = null;
    }
}
