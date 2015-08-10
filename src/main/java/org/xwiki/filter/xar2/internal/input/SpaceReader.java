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
     * Name of the file to describe a space.
     */
    static final String SPACE_FILENAME = "__space.xml";

    /**
     * Reference to the current space.
     */
    private SpaceReference reference;

    /**
     * Contain the model of a space (see xwiki-platform-rest-model).
     */
    private Space xSpace = new Space();

    /**
     * Retain the last page path in order to close and reinit if the current page is a new one.
     */
    private Path previousPagePath;

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

    private void reset()
    {
        this.reference = null;
        this.xSpace = new Space();
        this.previousPagePath = null;
    }

    private void setReference(String spaceName, EntityReference parentReference)
    {
        this.reference = new SpaceReference(spaceName, parentReference);
    }

    @Override
    public void open(Path path, EntityReference parentReference, InputStream inputStream) throws FilterException
    {
        this.reset();
        if (inputStream != null) {
            this.xSpace = (Space) this.unmarshal(inputStream, Space.class);
        }
        String spaceName = this.xSpace.getId();
        if (spaceName == null) {
            spaceName = path.getName(1).toString();
        }
        this.setReference(spaceName, parentReference);
        this.proxyFilter.beginWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        Path pagePath = path.subpath(0, 3);
        if (!pagePath.equals(this.previousPagePath)) {
            if (this.previousPagePath != null) {
                this.pageReader.close();
            }
            if (path.endsWith(PageReader.PAGE_FILENAME) && path.getNameCount() == 4) {
                this.pageReader.open(path, this.reference, inputStream);
            } else {
                this.pageReader.open(path, this.reference, null);
                this.pageReader.route(path, inputStream);
            }
        } else {
            this.pageReader.route(path, inputStream);
        }
        this.previousPagePath = pagePath;
    }

    @Override
    public void close() throws FilterException
    {
        this.pageReader.close();
        if (this.reference != null) {
            this.proxyFilter.endWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
        }
        this.reset();
    }
}
