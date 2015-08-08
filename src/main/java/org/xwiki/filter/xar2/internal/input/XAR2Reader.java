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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.FileInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.xar2.input.XAR2InputProperties;

/**
 * @version $Id$
 * @since 7.1
 */
@Component(roles = XAR2Reader.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XAR2Reader
{
    /**
     * Logger to report errors, warnings, etc.
     */
    @Inject
    private Logger logger;

    /**
     * Properties of the reader.
     */
    private XAR2InputProperties properties;

    /**
     * Set the properties before launching the reader.
     * 
     * @param properties contains all properties for input source
     */
    public void setProperties(XAR2InputProperties properties)
    {
        this.properties = properties;
    }

    /**
     * Entry point for reading a XAR2 file.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the filter into which you translate
     */
    public void read(Object filter, XAR2InputFilter proxyFilter)
    {
        InputSource source = this.properties.getSource();
        if (source instanceof FileInputSource) {
            try {
                parseXAR2File(((FileInputSource) source).getFile(), filter, proxyFilter);
            } catch (FilterException e) {
                this.logger.error("Fail to filter the file from input source", e);
            } catch (IOException e) {
                this.logger.error("Fail to get file from input source.", e);
            }
        } else {
            this.logger.error("Fail to read XAR2 file descriptor.");
        }
    }

    private void parseXAR2File(File file, Object filter, XAR2InputFilter proxyFilter) throws IOException,
        FilterException
    {
        ZipFile zf = new ZipFile(file, ZipFile.OPEN_READ);
        WikiReader wikiReader = new WikiReader(filter, proxyFilter);
        Enumeration<? extends ZipEntry> entries = zf.entries();

        Path previousWikiPath = null;
        Path wikiPath = null;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            Path path = Paths.get(entry.getName());
            wikiPath = path.subpath(0, 1);
            InputStream inputStream = zf.getInputStream(entry);
            if (!wikiPath.equals(previousWikiPath)) {
                if (previousWikiPath != null) {
                    wikiReader.close();
                }
                if (path.endsWith(WikiReader.WIKI_FILENAME) && path.getNameCount() == 2) {
                    wikiReader.open(path, null, inputStream);
                } else {
                    wikiReader.open(path, null, null);
                    wikiReader.route(path, inputStream);
                }
            } else {
                wikiReader.route(path, inputStream);
            }
            previousWikiPath = wikiPath;
        }
        if (wikiPath != null) {
            wikiReader.close();
        }
        zf.close();
    }
}
