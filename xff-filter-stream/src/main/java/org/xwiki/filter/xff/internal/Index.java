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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.filter.xff.input.AbstractReader;

/**
 * This class is used to navigate into the <tt>index.txt</tt> file which contains the paths to parse from XFF package.
 * 
 * @version $Id$
 * @since 7.1
 */
public class Index
{
    /**
     * Name of the file that contains the index of paths.
     */
    public static final String INDEX_FILENAME = "index.txt";

    /**
     * Logger to report errors, warnings, etc.
     */
    private Logger logger = LoggerFactory.getLogger(AbstractReader.class);

    /**
     * Buffer containing the content of the file <tt>index.txt</tt>.
     */
    private BufferedReader buffer;

    /**
     * Load the <tt>index.txt</tt> file from its path.
     * 
     * @param path to the <tt>index.txt</tt> file
     * @throws FileNotFoundException whenever the path does not correspond to any file or there has been read
     *             restriction on the file
     */
    public Index(Path path) throws FileNotFoundException
    {
        File file = new File(path.toUri());
        InputStream inputStream = new FileInputStream(file);
        Reader reader = new InputStreamReader(inputStream);
        this.buffer = new BufferedReader(reader);
    }

    /**
     * Load the <tt>index.txt</tt> file from a stream.
     * 
     * @param inputStream of <tt>index.txt</tt>
     */
    public Index(InputStream inputStream)
    {
        Reader reader = new InputStreamReader(inputStream);
        this.buffer = new BufferedReader(reader);
    }

    /**
     * Check if the <tt>index.txt</tt> has another line after the line currently being read.
     * 
     * @return true if another line is available; return false if no
     */
    public boolean hasMoreElements()
    {
        boolean hasMoreElement = true;

        try {
            this.buffer.mark(1024);
            if (this.buffer.readLine() == null) {
                hasMoreElement = false;
            }
            this.buffer.reset();
        } catch (IOException e) {
            String message = String.format("Unable to check for another line in '%s'", Index.INDEX_FILENAME);
            logger.error(message, e);
            return false;
        }

        return hasMoreElement;
    }

    /**
     * Return the next line of the <tt>index.txt</tt> file as a {@link Path}.
     * 
     * @return a {@link Path} of the next line
     */
    public Path nextElement()
    {
        try {
            String nextElement = this.buffer.readLine();
            return Paths.get(nextElement);
        } catch (IOException e) {
            String message = String.format("Unable to look for a next element in '%s'", Index.INDEX_FILENAME);
            logger.error(message, e);
            return null;
        }
    }

    private boolean isKeyword(String element)
    {
        if ("wikis".equals(element)) {
            return true;
        }
        if ("spaces".equals(element)) {
            return true;
        }
        if ("pages".equals(element)) {
            return true;
        }
        return false;
    }

    /**
     * The path in <tt>index.txt</tt> is given as a complete like in REST services (spaces are prefixed with
     * <tt>spaces</tt>, same for wikis and pages). But in order to find the corresponding file in the XFF package
     * hierarchy, we'll have to remove this keywords. For example, you may have a path like the following in
     * <tt>index.txt</tt>.
     * 
     * <pre>
     * wikis / xwiki / spaces / Space / pages / Page / page.xml
     * </pre>
     * 
     * but the file has to be found to the corresponding {@link Path}
     * 
     * <pre>
     * xwiki / Space / Page / page.xml
     * </pre>
     * 
     * The algorithm is simple, we remove all elements named <tt>wikis</tt>, <tt>spaces</tt> or <tt>pages</tt> from the
     * {@link Path} if:
     * <ul>
     * <li>they are at most at the 6th position in the {@link Path}</li>
     * <li>their position is an even number</li>
     * </ul>
     * 
     * @param path is the path to convert from REST-like {@link Path} to the real {@link Path}
     * @return the real path to the file
     */
    public String extractFilePath(Path path)
    {
        String filePath = "";
        String sep = "";
        Iterator<Path> pathIterator = path.iterator();
        int counter = 0;
        while (pathIterator.hasNext()) {
            String element = pathIterator.next().toString();
            if (counter > 6 || counter % 2 != 0 || !isKeyword(element)) {
                filePath += sep + element;
                sep = "/";
            }
            counter += 1;
        }
        return filePath;
    }
}
