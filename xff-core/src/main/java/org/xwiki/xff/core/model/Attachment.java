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
import java.util.Set;
import java.util.TreeSet;

/**
 * Represent the structure of an attachment.
 * 
 * @version $Id$
 * @since 7.1
 */
public class Attachment implements XElement
{
    /**
     * Prefix used in the path for attachments.
     */
    public static final String ATTACHMENTS_HINT = "attachments";
    
    /**
     * List of paths for attachments.
     */
    private Set<String> attachments;

    /**
     * Empty constructors initializing attributes.
     */
    public Attachment()
    {
        this.attachments = new TreeSet<String>();
    }

    @Override
    public void store(Path path)
    {
        if (path.getNameCount() == 1) {
            this.attachments.add(path.toString());
        }
    }

    @Override
    public List<Path> orderedPaths()
    {
        List<Path> paths = new ArrayList<Path>();
        for (String attachmentName : this.attachments) {
            paths.add(Paths.get(attachmentName));
        }
        return paths;
    }
}
