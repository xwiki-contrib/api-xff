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
 * Represent the structure of a package (may start with wikis, spaces, pages, etc.).
 * 
 * @version $Id$
 * @since 7.1
 */
public class XFF implements XElement
{

    /**
     * List of paths for elements in this package.
     */
    private Map<String, XElement> elements;

    /**
     * Empty constructors initializing attributes.
     */
    public XFF()
    {
        this.elements = new TreeMap<String, XElement>();
    }

    private void storeWiki(Path path)
    {
        String wikiName = path.subpath(0, 2).toString();
        Path subPath = path.subpath(2, path.getNameCount());
        if (!this.elements.containsKey(wikiName)) {
            this.elements.put(wikiName, new Wiki());
        }
        this.elements.get(wikiName).store(subPath);
    }

    private void storeSpace(Path path)
    {
        String spaceName = path.subpath(0, 2).toString();
        Path subPath = path.subpath(2, path.getNameCount());
        if (!this.elements.containsKey(spaceName)) {
            this.elements.put(spaceName, new Space());
        }
        this.elements.get(spaceName).store(subPath);
    }

    private void storePage(Path path)
    {
        String pageName = path.subpath(0, 2).toString();
        Path subPath = path.subpath(2, path.getNameCount());
        if (!this.elements.containsKey(pageName)) {
            this.elements.put(pageName, new Space());
        }
        this.elements.get(pageName).store(subPath);
    }

    private void storeAttachment(Path path)
    {
        String attachmentName = path.subpath(0, 2).toString();
        Path subPath = path.subpath(1, path.getNameCount());
        if (!this.elements.containsKey(attachmentName)) {
            this.elements.put(attachmentName, new Space());
        }
        this.elements.get(attachmentName).store(subPath);
    }

    private void storeClass(Path path)
    {
        String className = path.subpath(0, 1).toString();
        Path subPath = path.subpath(1, path.getNameCount());
        if (!this.elements.containsKey(className)) {
            this.elements.put(className, new Space());
        }
        this.elements.get(className).store(subPath);
    }

    private void storeObject(Path path)
    {
        String objectName = path.subpath(0, 3).toString();
        Path subPath = path.subpath(3, path.getNameCount());
        if (!this.elements.containsKey(objectName)) {
            this.elements.put(objectName, new Space());
        }
        this.elements.get(objectName).store(subPath);
    }

    @Override
    public void store(Path path)
    {
        if (path.toString().startsWith(Wiki.WIKI_HINT)) {
            this.storeWiki(path);
        } else if (path.toString().startsWith(Space.SPACE_HINT)) {
            this.storeSpace(path);
        } else if (path.toString().startsWith(Page.PAGE_HINT)) {
            this.storePage(path);
        } else if (path.toString().startsWith(Attachment.ATTACHMENTS_HINT)) {
            this.storeAttachment(path);
        } else if (path.toString().startsWith(Class.CLASS_HINT)) {
            this.storeClass(path);
        } else if (path.toString().startsWith(Object.OBJECT_HINT)) {
            this.storeObject(path);
        }
    }

    @Override
    public List<Path> orderedPaths()
    {
        List<Path> paths = new ArrayList<Path>();
        for (String elementName : this.elements.keySet()) {
            List<Path> subPaths = this.elements.get(elementName).orderedPaths();
            for (Path subPath : subPaths) {
                Path outPath = Paths.get(elementName, subPath.toString());
                paths.add(outPath);
            }
        }
        return paths;
    }
}
