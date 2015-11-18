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
 * Represent the structure of a class.
 * 
 * @version $Id$
 * @since 7.1
 */
public class Class implements XElement
{
    /**
     * Prefix used in the path for classes.
     */
    public static final String CLASS_HINT = "classes";

    /**
     * Name of the file to describe a class.
     */
    public static final String CLASS_FILENAME = "class.xml";

    /**
     * Remember if there is a file describing the class.
     */
    private boolean hasFile;

    /**
     * List of paths for properties in this object.
     */
    private Map<String, Property> properties;

    /**
     * Empty constructors initializing attributes.
     */
    public Class()
    {
        this.hasFile = false;
        this.properties = new TreeMap<String, Property>();
    }

    @Override
    public void store(Path path)
    {
        if (path.getFileName().toString().equals(Class.CLASS_FILENAME)) {
            this.hasFile = true;
        } else if (path.toString().startsWith(Property.PROPERTY_HINT)) {
            String propertyName = path.getName(1).toString();
            Path subPath = path.subpath(2, path.getNameCount());
            if (!this.properties.containsKey(propertyName)) {
                this.properties.put(propertyName, new Property());
            }
            this.properties.get(propertyName).store(subPath);
        }
    }

    @Override
    public List<Path> orderedPaths()
    {
        List<Path> paths = new ArrayList<Path>();
        if (this.hasFile) {
            paths.add(Paths.get(Class.CLASS_FILENAME));
        }
        for (String propertyName : this.properties.keySet()) {
            List<Path> subPaths = this.properties.get(propertyName).orderedPaths();
            for (Path subPath : subPaths) {
                Path outPath = Paths.get(Property.PROPERTY_HINT, propertyName, subPath.toString());
                paths.add(outPath);
            }
        }
        return paths;
    }

}
