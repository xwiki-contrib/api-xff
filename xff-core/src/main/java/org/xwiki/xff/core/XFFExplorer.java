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
package org.xwiki.xff.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xwiki.xff.core.model.XFF;

/**
 * This explorer will go through the folders of the package and provide interface to get the files in a correct order.
 * 
 * @version $Id$
 * @since 7.1
 */
public class XFFExplorer extends SimpleFileVisitor<Path> implements Iterator<Path>, Iterable<Path>
{
    /**
     * Root path to remind because XFF is only working with relative paths.
     */
    private Path rootPath;

    /**
     * List of paths of the XFF package (only referring to files).
     */
    private List<Path> paths;

    /**
     * Current index, used for the iterator.
     */
    private int index = -1;

    /**
     * Initialize the Walker by parsing the folder and ordering the files.
     * 
     * @param path is the root path where to look for a XFF package.
     * @throws IOException whenever there is problems in reading files or walking through the folders.
     */
    public XFFExplorer(Path path) throws IOException
    {
        this.init(path);
    }

    private void init(Path path) throws IOException
    {
        this.rootPath = path;
        this.paths = new ArrayList<Path>();
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                paths.add(rootPath.relativize(file));
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(path, visitor);
        XFF xff = new XFF();
        for (Path p : this.paths) {
            xff.store(p);
        }
        this.paths = xff.orderedPaths();
    }

    @Override
    public boolean hasNext()
    {
        return index + 1 < this.paths.size();
    }

    @Override
    public Path next()
    {
        this.index += 1;
        return this.paths.get(this.index);
    }

    @Override
    public Iterator<Path> iterator()
    {
        return this;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("The XFF Walker does not implement the remove method");
    }
    
    /**
     * Get the root path of the XFF package (a folder).
     * 
     * @return the root path of the XFF package
     */
    public Path getPath() {
        return this.rootPath;
    }
}
