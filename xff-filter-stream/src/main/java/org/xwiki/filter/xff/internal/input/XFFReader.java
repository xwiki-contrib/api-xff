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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.FileInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.xff.input.XFFInputProperties;
import org.xwiki.filter.xff.internal.UncloseableZipInputStream;

/**
 * @version $Id$
 * @since 7.1
 */
@Component(roles = XFFReader.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XFFReader
{
    /**
     * Logger to report errors, warnings, etc.
     */
    @Inject
    private Logger logger;

    /**
     * Properties of the reader.
     */
    private XFFInputProperties properties;

    /**
     * The reader to whom route files.
     */
    private WikiReader wikiReader;

    /**
     * Set the properties before launching the reader.
     * 
     * @param properties contains all properties for input source
     */
    public void setProperties(XFFInputProperties properties)
    {
        this.properties = properties;
    }

    /**
     * Test if a file is a zip. From http://www.java2s.com/Code/Java/File-Input-Output/
     * DeterminewhetherafileisaZIPFile.htm
     * 
     * @param file is the file to be tested
     * @return true if the file is a zip, return false in other cases
     */
    private boolean isZip(File file)
    {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.canRead()) {
            return false;
        }
        if (file.length() < 4) {
            return false;
        }
        int test = 0;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            test = in.readInt();
            in.close();
        } catch (IOException e) {
            return false;
        }
        // Magic bytes code
        // https://en.wikipedia.org/wiki/List_of_file_signatures
        return test == 0x504b0304 || test == 0x504b0506 || test == 0x504b0708;
    }

    private boolean isZip(InputStream inputStream)
    {
        boolean isZipped = false;
        try {
            isZipped = new ZipInputStream(inputStream).getNextEntry() != null;
        } catch (IOException e) {
            return false;
        }
        return isZipped;
    }

    /**
     * Entry point for reading a XFF file.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the filter into which you translate
     */
    public void read(Object filter, XFFInputFilter proxyFilter)
    {
        InputSource source = this.properties.getSource();
        this.wikiReader = new WikiReader(filter, proxyFilter);
        if (source instanceof FileInputSource) {
            try {
                File inputFile = ((FileInputSource) source).getFile();
                if (isZip(inputFile)) {
                    parseXFFFile(inputFile, filter, proxyFilter);
                } else if (inputFile.isDirectory()) {
                    Path relativePath = Paths.get(inputFile.getPath());
                    Path rootPath = Paths.get("/", relativePath.subpath(0, relativePath.getNameCount() - 1).toString());
                    Path path = relativePath.getName(relativePath.getNameCount() - 1);
                    parseXFFDir(rootPath, path, filter, proxyFilter);
                } else {
                    this.logger.error("Don't know how to parse this kind of XFF format");
                }
            } catch (FilterException e) {
                this.logger.error("Fail to filter the file from input source", e);
            } catch (IOException e) {
                this.logger.error("Fail to get file from input source.", e);
            }
        } else if (source instanceof InputStreamInputSource) {
            try {
                InputStream inputStream = ((InputStreamInputSource) source).getInputStream();
                parseXFFInputStream(inputStream, filter, proxyFilter);
            } catch (FilterException e) {
                this.logger.error("Fail to filter from input stream input source", e);
            } catch (IOException e) {
                this.logger.error("Fail to get input stream input source.", e);
            }
        } else {
            this.logger.error("Fail to read XFF file descriptor.");
        }
    }

    private void parseXFFFile(File file, Object filter, XFFInputFilter proxyFilter) throws IOException, FilterException
    {
        ZipFile zf = new ZipFile(file, ZipFile.OPEN_READ);
        Enumeration<? extends ZipEntry> entries = zf.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                Path path = Paths.get(entry.getName());
                InputStream inputStream = zf.getInputStream(entry);
                this.wikiReader.route(path, inputStream, null);
            }
        }

        wikiReader.finish();
        zf.close();
    }

    private void parseXFFInputStream(InputStream inputStream, Object filter, XFFInputFilter proxyFilter)
        throws FilterException, IOException
    {
        UncloseableZipInputStream zis = new UncloseableZipInputStream(inputStream);

        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            if (!entry.isDirectory()) {
                Path path = Paths.get(entry.getName());
                this.wikiReader.route(path, zis, null);
            }
            zis.closeEntry();
            entry = zis.getNextEntry();
        }

        wikiReader.finish();
        zis.close(true);
    }

    private void parseXFFDir(Path rootPath, Path path, Object filter, XFFInputFilter proxyFilter) throws IOException,
        FilterException
    {
        Path filePath = Paths.get(rootPath.toString(), path.toString());
        File file = new File(filePath.toString());
        if (file.isFile()) {
            InputStream inputStream = new FileInputStream(file);
            this.wikiReader.route(path, inputStream, null);
        }

        if (file.isDirectory()) {
            String[] subPaths = file.list();
            Arrays.sort(subPaths);
            for (String filename : subPaths) {
                Path subPath = Paths.get(path.toString(), filename);
                parseXFFDir(rootPath, subPath, filter, proxyFilter);
            }
        }
        if (path.getNameCount() == 1) {
            this.wikiReader.finish();
        }
    }
}
