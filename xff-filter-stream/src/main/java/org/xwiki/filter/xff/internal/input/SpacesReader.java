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

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
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
@Component
@Named(org.xwiki.xff.core.model.Space.SPACE_HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class SpacesReader extends AbstractReader
{
    /**
     * Reference to the current space.
     */
    private SpaceReference reference;

    /**
     * Contain the model of a space (see xwiki-platform-rest-model).
     */
    private Space xSpace = new Space();

    private void parseSpace(InputStream inputStream) throws FilterException
    {
        if (inputStream != null) {
            this.xSpace = (Space) this.unmarshal(inputStream, Space.class);
        }
        String spaceName = this.xSpace.getName();
        if (spaceName != null) {
            this.reference = new SpaceReference(spaceName, this.reference.getParent());
        }
    }

    private void start() throws FilterException
    {
        if (!this.started) {
            this.proxyFilter.beginWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
            this.started = true;
        }
    }

    private void end() throws FilterException
    {
        if (this.started) {
            this.proxyFilter.endWikiSpace(this.reference.getName(), FilterEventParameters.EMPTY);
        }
    }

    @Override
    public void open(String id, EntityReference parentReference, Object filter, XFFInputFilter proxyFilter)
        throws FilterException
    {
        this.reference = new SpaceReference(id, parentReference);
        this.setFilters(filter, proxyFilter);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        String fileName = path.toString();
        if (fileName.equals(org.xwiki.xff.core.model.Space.SPACE_FILENAME)) {
            this.parseSpace(inputStream);
            this.start();
            return;
        } else {
            this.start();
        }
        String hint = path.subpath(0, 1).toString();
        String childId = path.subpath(1, 2).toString();
        // Get a new reader only if the child change
        if (!childId.equals(this.lastReaderId)) {
            if (this.reader != null) {
                this.reader.close();
            }
            this.reader = this.getReader(hint);
            this.reader.open(childId, this.reference, this.filter, this.proxyFilter);
        }
        Path childPath = path.subpath(2, path.getNameCount());
        this.reader.route(childPath, inputStream);
        this.lastReaderId = childId;
    }

    @Override
    public void close() throws FilterException
    {
        if (this.reader != null) {
            this.reader.close();
        }
        this.end();
    }
}
