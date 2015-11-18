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
package org.xwiki.xff.core.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represent the structure of a wiki.
 * 
 * @version $Id$
 * @since 7.1
 */
public class Wiki implements XElement
{
    /**
     * Prefix used in the path for wikis.
     */
    public static final String WIKI_HINT = "wikis";

    /**
     * Name of the file to describe a wiki.
     */
    public static final String WIKI_FILENAME = "wiki.xml";

    /**
     * Remember if there is a file describing the wiki.
     */
    private boolean hasFile;

    /**
     * List of paths for spaces in this wiki.
     */
    private Map<String, Space> spaces;

    /**
     * Empty constructors initializing attributes.
     */
    public Wiki()
    {
        this.hasFile = false;
        this.spaces = new TreeMap<String, Space>();
    }

    @Override
    public void store(Path path)
    {
        if (path.getFileName().toString().equals(Wiki.WIKI_FILENAME)) {
            this.hasFile = true;
        } else if (path.toString().startsWith(Space.SPACE_HINT)) {
            String spaceName = path.getName(1).toString();
            Path subPath = path.subpath(2, path.getNameCount());
            if (!this.spaces.containsKey(spaceName)) {
                this.spaces.put(spaceName, new Space());
            }
            this.spaces.get(spaceName).store(subPath);
        }
    }

    @Override
    public List<Path> orderedPaths()
    {
        List<Path> paths = new ArrayList<Path>();
        if (this.hasFile) {
            paths.add(Paths.get(Wiki.WIKI_FILENAME));
        }
        for (String spaceName : this.spaces.keySet()) {
            List<Path> subPaths = this.spaces.get(spaceName).orderedPaths();
            for (Path subPath : subPaths) {
                Path outPath = Paths.get(Space.SPACE_HINT, spaceName, subPath.toString());
                paths.add(outPath);
            }
        }
        return paths;
    }
}
