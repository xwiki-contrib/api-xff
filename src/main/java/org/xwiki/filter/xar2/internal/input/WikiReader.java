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
import org.xwiki.model.reference.WikiReference;
import org.xwiki.rest.model.jaxb.Wiki;

/**
 * Read file from XAR2 and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
public class WikiReader extends AbstractReader
{
    /**
     * Name of the file to describe a wiki.
     */
    static final String WIKI_FILENAME = "__wiki.xml";

    /**
     * Reference to the current wiki.
     */
    private WikiReference reference;

    /**
     * Contain the model of a wiki (see xwiki-platform-rest-model).
     */
    private Wiki xWiki = new Wiki();

    /**
     * Retain the last space path in order to close and reinit if the current space is a new one.
     */
    private Path previousSpacePath;

    /**
     * Child reader for spaces.
     */
    private SpaceReader spaceReader;

    /**
     * Constructor that instantiate the child space reader.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public WikiReader(Object filter, XAR2InputFilter proxyFilter)
    {
        super(filter, proxyFilter);
        this.spaceReader = new SpaceReader(filter, proxyFilter);
    }

    private void reset()
    {
        this.reference = null;
        this.xWiki = new Wiki();
        this.previousSpacePath = null;
    }

    private void setReference(String wikiName)
    {
        this.reference = new WikiReference(wikiName);
    }

    @Override
    public void open(Path path, EntityReference parentReference, InputStream inputStream) throws FilterException
    {
        this.reset();
        if (inputStream != null) {
            this.xWiki = (Wiki) this.unmarshal(inputStream, Wiki.class);
        }
        String wikiName = this.xWiki.getId();
        if (wikiName == null) {
            wikiName = path.getName(0).toString();
        }
        this.setReference(wikiName);
        this.proxyFilter.beginWiki(this.reference.getName(), FilterEventParameters.EMPTY);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        Path spacePath = path.subpath(0, 2);
        if (!spacePath.equals(this.previousSpacePath)) {
            if (this.previousSpacePath != null) {
                this.spaceReader.close();
            }
            if (path.endsWith(SpaceReader.SPACE_FILENAME) && path.getNameCount() == 3) {
                this.spaceReader.open(path, this.reference, inputStream);
            } else {
                this.spaceReader.open(path, this.reference, null);
                this.spaceReader.route(path, inputStream);
            }
        } else {
            this.spaceReader.route(path, inputStream);
        }
        this.previousSpacePath = spacePath;
    }

    @Override
    public void close() throws FilterException
    {
        this.spaceReader.close();
        if (this.reference != null) {
            this.proxyFilter.endWiki(this.reference.getName(), FilterEventParameters.EMPTY);
        }
        this.reset();
    }
}
