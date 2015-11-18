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
import java.util.List;

/**
 * Generic interface for the structure of the package.
 * 
 * @version $Id$
 * @since 7.1
 */
public interface XElement
{
    /**
     * Store the path if related to the current element, else send it to a child element.
     * 
     * @param path is the relative path to the file.
     */
    void store(Path path);

    /**
     * In order to get the files of the XFF package in the right order, this function is returning an ordered list of
     * relative paths.
     * 
     * @return a list of path correctly ordered for XFF.
     */
    List<Path> orderedPaths();
}
