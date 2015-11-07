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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.filter.xff.input.AbstractReader;

public class Index
{
    public static final String INDEX_FILENAME = "index.txt";

    /**
     * Logger to report errors, warnings, etc.
     */
    private Logger logger = LoggerFactory.getLogger(AbstractReader.class);

    /**
     * Buffer containing the content of the file 'index.txt'
     */
    private BufferedReader buffer;

    public Index(Path path) throws FileNotFoundException
    {
        File file = new File(path.toUri());
        InputStream inputStream = new FileInputStream(file);
        Reader reader = new InputStreamReader(inputStream);
        this.buffer = new BufferedReader(reader);
    }

    public Index(InputStream inputStream)
    {
        Reader reader = new InputStreamReader(inputStream);
        this.buffer = new BufferedReader(reader);
    }

    public boolean hasMoreElements()
    {
        boolean hasMoreElement = true;

        try {
            this.buffer.mark(1000);
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
}
