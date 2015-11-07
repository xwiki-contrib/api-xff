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

import java.io.InputStream;
import java.nio.file.Path;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.input.AbstractReader;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.rest.model.jaxb.Space;

/**
 * Read file from XFF and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
public class SpaceReader extends AbstractReader
{
    /**
     * Name of the file to describe a space.
     */
    private static final String SPACE_FILENAME = "space.xml";

    /**
     * Reference to the current space.
     */
    private SpaceReference reference;

    /**
     * Contain the model of a space (see xwiki-platform-rest-model).
     */
    private Space xSpace = new Space();

    /**
     * Retain information about if a space has been opened.
     */
    private boolean started;

    /**
     * Retain the last space path in order to close and reinit if the current space is a new one.
     */
    private Path previousSpacePath;

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
    public SpaceReader(Object filter, XFFInputFilter proxyFilter)
    {
        super(filter, proxyFilter);
        this.pageReader = new PageReader(filter, proxyFilter);
    }

    private void reset()
    {
        this.reference = null;
        this.xSpace = new Space();
        this.started = false;
    }

    private void setReference(String spaceName, EntityReference parentReference)
    {
        this.reference = new SpaceReference(spaceName, parentReference);
    }

    private void init(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        this.reset();
        if (inputStream != null) {
            this.xSpace = (Space) this.unmarshal(inputStream, Space.class);
        }
        String spaceName = this.xSpace.getName();
        if (spaceName == null) {
            spaceName = path.getName(1).toString();
        }
        this.setReference(spaceName, parentReference);
    }

    private void start() throws FilterException
    {
        this.proxyFilter.beginWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
        this.started = true;
    }

    @Override
    public void route(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        Path spacePath;
        try {
            spacePath = path.subpath(0, 2);
        } catch (IllegalArgumentException e) {
            String message = String.format("Unable to extract space path from '%s'.", path.toString());
            throw new FilterException(message);
        }

        // Close previous space before starting a new one
        if (!spacePath.equals(this.previousSpacePath)) {
            this.finish();
        }
        // Parse files relative to space or reroute them to the PageReader
        if (path.endsWith(SpaceReader.SPACE_FILENAME)) {
            this.init(path, inputStream, parentReference);
        } else {
            // If the space has not been initialized, initializes it with only the path
            if (this.reference == null) {
                this.init(path, null, parentReference);
            }
            // Before routing any other file, start the space
            if (!this.started) {
                this.start();
            }
            this.pageReader.route(path, inputStream, this.reference);
        }
        this.previousSpacePath = spacePath;
    }

    @Override
    public void finish() throws FilterException
    {
        this.pageReader.finish();
        if (this.reference != null) {
            if (!this.started) {
                this.start();
            }
            this.proxyFilter.endWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
        }
        this.reset();
    }
}
