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
package org.xwiki.filter.xff.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.xwiki.filter.xff.internal.Index;

public class IndexUnitTest
{
    @Test
    public void hasMoreElementTest() throws URISyntaxException, FileNotFoundException
    {
        URL url = getClass().getClassLoader().getResource(Index.INDEX_FILENAME);
        Path path = Paths.get(url.toURI());
        Index index = new Index(path);
        assertTrue(index.hasMoreElements());
        assertEquals(index.nextElement(), Paths.get("xwiki/wiki.xml"));
        assertTrue(index.hasMoreElements());
        assertEquals(index.nextElement(), Paths.get("xwiki/Space/space.xml"));
        assertFalse(index.hasMoreElements());
    }
}
