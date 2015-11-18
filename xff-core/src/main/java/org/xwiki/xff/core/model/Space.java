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
 * Represent the structure of a space.
 * 
 * @version $Id$
 * @since 7.1
 */
public class Space implements XElement
{
    /**
     * Prefix used in the path for spaces.
     */
    public static final String SPACE_HINT = "spaces";

    /**
     * Name of the file to describe a space.
     */
    public static final String SPACE_FILENAME = "space.xml";

    /**
     * Remember if there is a file describing the space.
     */
    private boolean hasFile;

    /**
     * List of paths for pages in this space.
     */
    private Map<String, Page> pages;

    /**
     * List of paths for nested spaces in this space.
     */
    private Map<String, Space> spaces;

    /**
     * Empty constructors initializing attributes.
     */
    public Space()
    {
        this.hasFile = false;
        this.pages = new TreeMap<String, Page>();
        this.spaces = new TreeMap<String, Space>();
    }

    @Override
    public void store(Path path)
    {
        if (path.getFileName().toString().equals(Space.SPACE_FILENAME)) {
            this.hasFile = true;
        } else if (path.toString().startsWith(Page.PAGE_HINT)) {
            String pageName = path.getName(1).toString();
            Path subPath = path.subpath(2, path.getNameCount());
            if (!this.pages.containsKey(pageName)) {
                this.pages.put(pageName, new Page());
            }
            this.pages.get(pageName).store(subPath);
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
            paths.add(Paths.get(Space.SPACE_FILENAME));
        }
        for (String pageName : this.pages.keySet()) {
            List<Path> subPaths = this.pages.get(pageName).orderedPaths();
            for (Path subPath : subPaths) {
                Path outPath = Paths.get(Page.PAGE_HINT, pageName, subPath.toString());
                paths.add(outPath);
            }
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
