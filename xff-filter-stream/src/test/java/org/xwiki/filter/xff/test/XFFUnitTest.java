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

import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.xff.internal.input.ClassReader;
import org.xwiki.filter.xff.internal.input.ObjectReader;
import org.xwiki.filter.xff.internal.input.PageReader;
import org.xwiki.filter.xff.internal.input.SpaceReader;
import org.xwiki.filter.xff.internal.input.WikiReader;
import org.xwiki.filter.xff.test.internal.TestReader;
import org.xwiki.rest.model.jaxb.Wiki;

public class XFFUnitTest
{
    @Test
    public void malformedXML()
    {
        TestReader testReader = new TestReader(null, null);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("wiki.xml");
        assertNull(testReader.publicUnmarshal(inputStream, Wiki.class));
    }

    @Test(expected = FilterException.class)
    public void wrongPathWiki() throws FilterException
    {
        WikiReader wikiReader = new WikiReader(null, null);
        Path path = Paths.get("/");
        wikiReader.route(path, null, null);
    }
    
    @Test(expected = org.xwiki.filter.FilterException.class)
    public void wrongPathSpace() throws FilterException
    {
        SpaceReader spaceReader = new SpaceReader(null, null);
        Path path = Paths.get("/");
        spaceReader.route(path, null, null);
    }
    
    @Test(expected = org.xwiki.filter.FilterException.class)
    public void wrongPathPage() throws FilterException
    {
        PageReader pageReader = new PageReader(null, null);
        Path path = Paths.get("/");
        pageReader.route(path, null, null);
    }
    
    @Test(expected = org.xwiki.filter.FilterException.class)
    public void wrongPathClass() throws FilterException
    {
        ClassReader classReader = new ClassReader(null, null);
        Path path = Paths.get("/");
        classReader.route(path, null, null);
    }
    
    @Test(expected = org.xwiki.filter.FilterException.class)
    public void notAllowedPathClass() throws FilterException
    {
        ClassReader classReader = new ClassReader(null, null);
        Path path = Paths.get("xwiki/Space/Page/class/notallowed");
        classReader.route(path, null, null);
    }
    
    @Test(expected = org.xwiki.filter.FilterException.class)
    public void wrongPathObject() throws FilterException
    {
        ObjectReader objectReader = new ObjectReader(null, null);
        Path path = Paths.get("/");
        objectReader.route(path, null, null);
    }
    
    @Test(expected = org.xwiki.filter.FilterException.class)
    public void notAllowedPathObject() throws FilterException
    {
        ObjectReader objectReader = new ObjectReader(null, null);
        Path path = Paths.get("xwiki/Space/Page/objects/Space.Page/0/notallowed");
        objectReader.route(path, null, null);
    }
}
