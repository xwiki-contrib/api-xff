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
import org.xwiki.model.reference.WikiReference;
import org.xwiki.rest.model.jaxb.Wiki;

/**
 * Read file from XFF and parse them or reroute them to child readers.
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
     * Retain information about if a wiki has been opened.
     */
    private boolean started;

    /**
     * Retain the last wiki path in order to close and reinit if the current wiki is a new one.
     */
    private Path previousWikiPath;

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
    public WikiReader(Object filter, XFFInputFilter proxyFilter)
    {
        super(filter, proxyFilter);
        this.spaceReader = new SpaceReader(filter, proxyFilter);
    }

    private void reset()
    {
        this.reference = null;
        this.xWiki = new Wiki();
        this.started = false;
    }

    private void setReference(String wikiName)
    {
        this.reference = new WikiReference(wikiName);
    }

    private void init(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
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
    }

    private void start() throws FilterException
    {
        this.proxyFilter.beginWiki(this.reference.getName(), FilterEventParameters.EMPTY);
        this.started = true;
    }

    @Override
    public void route(Path path, InputStream inputStream, EntityReference parentReference) throws FilterException
    {
        Path wikiPath = path.subpath(0, 1);
        // Close previous wiki before starting a new one
        if (!wikiPath.equals(this.previousWikiPath)) {
            this.finish();
        }
        // Parse files relative to wiki or reroute them to the SpaceReader
        if (path.endsWith(WikiReader.WIKI_FILENAME)) {
            this.init(path, inputStream, parentReference);
        } else {
            // If the wiki has not been initialized, initializes it with only the path
            if (this.reference == null) {
                this.init(path, null, parentReference);
            }
            // Before routing any other file, start the wiki
            if (!this.started) {
                this.start();
            }
            this.spaceReader.route(path, inputStream, this.reference);
        }
        this.previousWikiPath = wikiPath;
    }

    @Override
    public void finish() throws FilterException
    {
        this.spaceReader.finish();
        if (this.reference != null) {
            if (!this.started) {
                this.start();
            }
            this.proxyFilter.endWiki(this.reference.getName(), FilterEventParameters.EMPTY);
        }
        this.reset();
    }
}
