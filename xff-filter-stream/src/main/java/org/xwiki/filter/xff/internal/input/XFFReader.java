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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.FileInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.xff.input.Reader;
import org.xwiki.filter.xff.input.XFFInputProperties;
import org.xwiki.filter.xff.internal.UncloseableZipInputStream;
import org.xwiki.xff.core.XFFExplorer;

/**
 * @version $Id$
 * @since 7.1
 */
@Component(roles = XFFReader.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XFFReader
{
    /**
     * The component manager. We need it because we have to access components dynamically.
     */
    @Inject
    private ComponentManager componentManager;

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
     * @param path is the file to be tested
     * @return true if the file is a zip, return false in other cases
     */
    private boolean isZip(Path path)
    {
        try {
            if (Files.isDirectory(path) || !Files.isReadable(path) || Files.size(path) < 4) {
                return false;
            }
        } catch (IOException e1) {
            return false;
        }
        int test = 0;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));
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
     * Entry point for reading a XFF file.
     * 
     * @param filter is the input filter
     * @param proxyFilter is the filter into which you translate
     */
    public void read(Object filter, XFFInputFilter proxyFilter)
    {
        InputSource source = this.properties.getSource();
        if (source instanceof FileInputSource) {
            try {
                Path path = ((FileInputSource) source).getFile().toPath();
                if (isZip(path)) {
                    parseXFFFile(path, filter, proxyFilter);
                } else if (Files.isDirectory(path)) {
                    parseXFFDir(path, filter, proxyFilter);
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

    private Reader updateReader(Reader reader, String hint, String id, String previousId, Object filter,
        XFFInputFilter proxyFilter) throws FilterException
    {
        Reader newReader = reader;
        if (!id.equals(previousId)) {
            if (reader != null) {
                reader.close();
            }
            try {
                newReader = this.componentManager.getInstance(Reader.class, hint);
                newReader.open(id, null, filter, proxyFilter);
            } catch (ComponentLookupException e) {
                String message = String.format("Incorrect hint '%s', cannot get the corresponding Reader.", hint);
                throw new FilterException(message, e);
            }
        }
        return newReader;
    }

    private void parseXFFFile(Path path, Object filter, XFFInputFilter proxyFilter) throws IOException, FilterException
    {
        if (isZip(path)) {
            InputStream inputStream = Files.newInputStream(path);
            parseXFFInputStream(inputStream, filter, proxyFilter);
        }
    }

    private void parseXFFInputStream(InputStream inputStream, Object filter, XFFInputFilter proxyFilter)
        throws FilterException, IOException
    {
        UncloseableZipInputStream zis = new UncloseableZipInputStream(inputStream);
        ZipEntry entry = null;
        Reader reader = null;
        String previousId = null;

        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                Path path = Paths.get(entry.getName());
                String hint = path.subpath(0, 1).toString();
                String id = path.subpath(1, 2).toString();
                Path subPath = path.subpath(2, path.getNameCount());
                reader = updateReader(reader, hint, id, previousId, filter, proxyFilter);
                reader.route(subPath, zis);
                previousId = id;
            }
            zis.closeEntry();
        }
        if (reader != null) {
            reader.close();
        }
        zis.close(true);
    }

    private void parseXFFDir(Path rootPath, Object filter, XFFInputFilter proxyFilter) throws IOException,
        FilterException
    {
        XFFExplorer packageExplorer = new XFFExplorer(rootPath);
        Reader reader = null;
        String previousId = null;

        while (packageExplorer.hasNext()) {
            Path path = packageExplorer.next();
            String hint = path.subpath(0, 1).toString();
            String id = path.subpath(1, 2).toString();
            Path subPath = path.subpath(2, path.getNameCount());
            reader = updateReader(reader, hint, id, previousId, filter, proxyFilter);
            Path filePath = rootPath.resolve(path);
            if (!Files.isDirectory(filePath)) {
                InputStream inputStream = Files.newInputStream(filePath);
                reader.route(subPath, inputStream);
                inputStream.close();
            }
            previousId = id;
        }
        if (reader != null) {
            reader.close();
        }
    }
}
