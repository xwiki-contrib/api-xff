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
package org.xwiki.filter.xff.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Wrapper of ZipInputStream that block the close function unless it's explicitly forced to close. When parsing an
 * {@link InputStream} (for example, using {@link Unmarshaller#unmarshal(InputStream)} in
 * {@link org.xwiki.filter.xff.input.AbstractReader}) on each InputStream (each one of
 * {@link ZipInputStream#getNextEntry()}), the {@link InputStream} will be automatically closed. Consequently,
 * ZipInputStream will be closed on first parsed entry. So the {@link ZipInputStream#close()} must be override in order
 * to keep the stream open.
 * 
 * @version $Id$
 * @since 7.1
 */
public class UncloseableZipInputStream extends ZipInputStream
{

    /**
     * Only a wrapper of the ZipInputStream.
     * 
     * @param inputStream the stream to use
     */
    public UncloseableZipInputStream(InputStream inputStream)
    {
        super(inputStream);
    }

    @Override
    public void close() throws IOException
    {
        close(false);
    }

    /**
     * Wrapper of the close() method that must be called with force=true to force the close of the ZipInputStream.
     * 
     * @param force will explicitly force the close of the InputStream
     * @throws IOException whenever {@see ZipInputStream#close()} send an exception
     */
    public void close(boolean force) throws IOException
    {
        if (force) {
            super.close();
        }
    }
}
