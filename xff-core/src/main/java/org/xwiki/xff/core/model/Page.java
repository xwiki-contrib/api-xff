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
 * Represent the structure of a page.
 * 
 * @version $Id$
 * @since 7.1
 */
public class Page implements XElement
{
    /**
     * Prefix used in the path for pages.
     */
    public static final String PAGE_HINT = "pages";

    /**
     * Name of the file to describe a page.
     */
    public static final String PAGE_FILENAME = "page.xml";

    /**
     * Remember if there is a file describing the page.
     */
    private boolean hasFile;

    /**
     * List of paths for metadata for this page.
     */
    private List<Path> metadata;

    /**
     * List of paths for attachments in this page.
     */
    private Map<String, Attachment> attachments;

    /**
     * List of paths for classes in this page.
     */
    private Map<String, org.xwiki.xff.core.model.Class> classes;

    /**
     * List of paths for objects in this page.
     */
    private Map<String, org.xwiki.xff.core.model.Object> objects;

    /**
     * Empty constructors initializing attributes.
     */
    public Page()
    {
        this.hasFile = false;
        this.metadata = new ArrayList<Path>();
        this.attachments = new TreeMap<String, Attachment>();
        this.classes = new TreeMap<String, org.xwiki.xff.core.model.Class>();
        this.objects = new TreeMap<String, org.xwiki.xff.core.model.Object>();
    }

    @Override
    public void store(Path path)
    {
        if (Page.PAGE_FILENAME.equals(path.getFileName().toString())) {
            this.hasFile = true;
        } else if (path.toString().startsWith(Property.PROPERTY_HINT)) {
            if (path.getNameCount() == 2) {
                this.metadata.add(path);
            }

        } else if (path.toString().startsWith(Attachment.ATTACHMENTS_HINT)) {
            String attachmentName = path.getName(1).toString();
            Path subPath = path.subpath(1, path.getNameCount());
            if (!this.attachments.containsKey(attachmentName)) {
                this.attachments.put(attachmentName, new Attachment());
            }
            this.attachments.get(attachmentName).store(subPath);

        } else if (path.toString().startsWith(Class.CLASS_HINT)) {
            String className = path.getName(1).toString();
            Path subPath = path.subpath(1, path.getNameCount());
            if (!this.classes.containsKey(className)) {
                this.classes.put(className, new org.xwiki.xff.core.model.Class());
            }
            this.classes.get(className).store(subPath);
        } else if (path.toString().startsWith(Object.OBJECT_HINT)) {
            String objectName = path.subpath(1, 3).toString();
            Path subPath = path.subpath(3, path.getNameCount());
            if (!this.objects.containsKey(objectName)) {
                this.objects.put(objectName, new org.xwiki.xff.core.model.Object());
            }
            this.objects.get(objectName).store(subPath);
        }
    }

    @Override
    public List<Path> orderedPaths()
    {
        List<Path> paths = new ArrayList<Path>();
        if (this.hasFile) {
            paths.add(Paths.get(Page.PAGE_FILENAME));
        }
        for (Path metadataPath : this.metadata) {
            paths.add(metadataPath);
        }
        for (String attachmentName : this.attachments.keySet()) {
            List<Path> subPaths = this.attachments.get(attachmentName).orderedPaths();
            for (Path subPath : subPaths) {
                Path outPath = Paths.get(Attachment.ATTACHMENTS_HINT, subPath.toString());
                paths.add(outPath);
            }
        }
        for (String className : this.classes.keySet()) {
            List<Path> subPaths = this.classes.get(className).orderedPaths();
            for (Path subPath : subPaths) {
                Path outPath = Paths.get(org.xwiki.xff.core.model.Class.CLASS_HINT, subPath.toString());
                paths.add(outPath);
            }
        }
        for (String objectName : this.objects.keySet()) {
            List<Path> subPaths = this.objects.get(objectName).orderedPaths();
            for (Path subPath : subPaths) {
                Path outPath = Paths.get(org.xwiki.xff.core.model.Object.OBJECT_HINT, objectName, subPath.toString());
                paths.add(outPath);
            }
        }
        return paths;
    }
}
