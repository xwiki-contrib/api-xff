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
     * The reader to whom route files.
     */
    private WikiReader wikiReader;

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
     * Test if a file is a zip.
     * 
     * From http://www.java2s.com/Code/Java/File-Input-Output/DeterminewhetherafileisaZIPFile.htm
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
            this.wikiReader = new WikiReader(filter, proxyFilter);
            try {
                File inputFile = ((FileInputSource) source).getFile();
                if (isZip(inputFile)) {
                    parseXAR2File(inputFile, filter, proxyFilter);
                } else if (inputFile.isDirectory()) {
                    Path relativePath = Paths.get(inputFile.getPath());
                    Path rootPath = Paths.get("/", relativePath.subpath(0, relativePath.getNameCount() - 1).toString());
                    Path path = relativePath.getName(relativePath.getNameCount() - 1);
                    parseXAR2Dir(rootPath, path, filter, proxyFilter);
                } else {
                    this.logger.error("Don't know how to parse this kind of XAR2 format");
                }
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
        Enumeration<? extends ZipEntry> entries = zf.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            Path path = Paths.get(entry.getName());
            InputStream inputStream = zf.getInputStream(entry);
            this.wikiReader.route(path, inputStream, null);
        }

        wikiReader.finish();
        zf.close();
    }

    private void parseXAR2Dir(Path rootPath, Path path, Object filter, XAR2InputFilter proxyFilter) throws IOException,
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
                parseXAR2Dir(rootPath, subPath, filter, proxyFilter);
            }
        }
        if (path.getNameCount() == 1) {
            this.wikiReader.finish();
        }
    }
}
