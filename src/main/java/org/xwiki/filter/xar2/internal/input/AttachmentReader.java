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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.compress.utils.IOUtils;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xar2.input.AbstractReader;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

/**
 * Read file from XAR2 and parse them or reroute them to child readers.
 * 
 * @version $Id$
 * @since 7.1
 */
public class AttachmentReader extends AbstractReader
{
    /**
     * Reference to the current attachment.
     */
    private AttachmentReference reference;

    /**
     * Parameters to send to the current class.
     */
    private FilterEventParameters parameters = FilterEventParameters.EMPTY;

    /**
     * Constructor that only call the abstract constructor.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the output filter
     */
    public AttachmentReader(Object filter, XAR2InputFilter proxyFilter)
    {
        super(filter, proxyFilter);
    }

    private void reset()
    {
        this.reference = null;
        this.parameters = FilterEventParameters.EMPTY;
    }

    private void setReference(String attachmentName, EntityReference parentReference)
    {
        this.reference = new AttachmentReference(attachmentName, (DocumentReference) parentReference);
    }

    @Override
    public void open(Path path, EntityReference parentReference, InputStream inputStream) throws FilterException
    {
        String attachmentName = path.getFileName().toString();
        this.setReference(attachmentName, parentReference);
    }

    @Override
    public void route(Path path, InputStream inputStream) throws FilterException
    {

        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(inputStream);
            // TODO: Why do we need length if we use an InputStream?
            this.proxyFilter.onWikiAttachment(this.reference.getName(), inputStream, Long.valueOf(bytes.length),
                this.parameters);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void close() throws FilterException
    {
        this.reset();
    }
}
