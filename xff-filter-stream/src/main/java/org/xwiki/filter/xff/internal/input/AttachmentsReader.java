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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.inject.Named;

import org.apache.commons.compress.utils.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.input.AbstractReader;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

/**
 * Read file from XFF and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
@Component
@Named("attachments")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class AttachmentsReader extends AbstractReader
{
    /**
     * Reference to the current attachment.
     */
    private AttachmentReference reference;

    /**
     * Parameters to send to the current class.
     */
    private FilterEventParameters parameters = FilterEventParameters.EMPTY;

    @Override
    public void open(String id, EntityReference parentReference, Object filter, XFFInputFilter proxyFilter)
        throws FilterException
    {
        this.reference = new AttachmentReference(id, (DocumentReference) parentReference);
        this.setFilters(filter, proxyFilter);
    }
    
    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(inputStream);
            // FIXME: Why do we need length if we use an InputStream?
            this.proxyFilter.onWikiAttachment(this.reference.getName(), inputStream, Long.valueOf(bytes.length),
                this.parameters);
        } catch (IOException e) {
            String message = String.format("Error in writing '%s'.", this.reference.getName());
            throw new FilterException(message, e);
        }
    }

    @Override
    public void close() throws FilterException
    {
    }
}
